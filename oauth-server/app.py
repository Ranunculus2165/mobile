from flask import Flask, request, jsonify, render_template_string, session, redirect
from flask_cors import CORS
from werkzeug.security import gen_salt
from models import db, User, OAuth2Client, OAuth2AuthorizationCode, OAuth2Token
from oauth2 import config_oauth
from authlib.integrations.flask_oauth2 import current_token
import os
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY', 'dev-secret-key')
app.config['SQLALCHEMY_DATABASE_URI'] = os.getenv('DATABASE_URL', 'sqlite:///oauth2.db')
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# CORS for Android app
CORS(app, resources={r"/*": {"origins": "*"}})

# Initialize database
db.init_app(app)

# Initialize OAuth2
authorization, require_oauth = config_oauth(app)


# Error handler for OAuth token validation failures
@app.errorhandler(401)
def handle_unauthorized(e):
    """Handle 401 Unauthorized errors from OAuth token validation"""
    auth_header = request.headers.get('Authorization', '')
    token_preview = ''
    token_value = ''
    
    if auth_header.startswith('Bearer '):
        token_value = auth_header[7:]  # Remove 'Bearer ' prefix
        if len(token_value) > 0:
            # Log token preview for debugging (first 10 chars + last 5 chars)
            token_preview = f"{token_value[:10]}...{token_value[-5:]}" if len(token_value) > 15 else token_value[:15]
    
    print(f"❌ OAuth Token Validation Failed:")
    print(f"   Endpoint: {request.path}")
    print(f"   Method: {request.method}")
    print(f"   Authorization Header Present: {bool(auth_header)}")
    print(f"   Token Preview: {token_preview if token_preview else 'N/A'}")
    print(f"   Token Length: {len(token_value)}")
    print(f"   Error: {str(e)}")
    
    # DB에서 토큰 조회 시도
    if token_value:
        db_token = OAuth2Token.query.filter_by(access_token=token_value).first()
        if db_token:
            import time
            current_time = int(time.time())
            print(f"   🔍 Token found in DB:")
            print(f"      DB ID: {db_token.id}")
            print(f"      Client ID: {db_token.client_id}")
            print(f"      User ID: {db_token.user_id}")
            print(f"      Scope: {db_token.scope}")
            print(f"      Expires At: {db_token.expires_at}")
            print(f"      Current Time: {current_time}")
            print(f"      Time Until Expiry: {db_token.expires_at - current_time} seconds")
            print(f"      Is Expired: {db_token.is_expired()}")
            print(f"      Is Revoked: {db_token.is_revoked()}")
        else:
            print(f"   🔍 Token NOT found in DB!")
            # 유사한 토큰 검색 (처음 10자로)
            if len(token_value) >= 10:
                similar_tokens = OAuth2Token.query.filter(
                    OAuth2Token.access_token.like(f"{token_value[:10]}%")
                ).all()
                if similar_tokens:
                    print(f"   🔍 Found {len(similar_tokens)} similar tokens (first 10 chars match):")
                    for t in similar_tokens:
                        print(f"      Token: {t.access_token[:10]}...{t.access_token[-5:] if len(t.access_token) > 15 else ''}")
                else:
                    print(f"   🔍 No similar tokens found in DB")
            # 전체 토큰 개수 확인
            total_tokens = OAuth2Token.query.count()
            print(f"   🔍 Total tokens in DB: {total_tokens}")
    
    return jsonify({
        'error': 'invalid_token',
        'error_description': 'The access token provided is expired, revoked, malformed, or invalid for other reasons.'
    }), 401


# Simple HTML template for login page
LOGIN_TEMPLATE = '''
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; }
        input { width: 100%; padding: 10px; margin: 5px 0; box-sizing: border-box; }
        .btn { width: 100%; padding: 10px; margin: 10px 0; cursor: pointer; background-color: #4CAF50; color: white; border: none; }
        .error { color: red; }
    </style>
</head>
<body>
    <h2>Login</h2>
    {% if error %}
    <p class="error">{{ error }}</p>
    {% endif %}
    <form method="post">
        <input type="text" name="username" placeholder="Username" required>
        <input type="password" name="password" placeholder="Password" required>
        <button type="submit" class="btn">Login</button>
    </form>
    <p><small>Test accounts: customer1/password123 or storeowner1/password123</small></p>
</body>
</html>
'''

# Simple HTML template for authorization page
AUTHORIZE_TEMPLATE = '''
<!DOCTYPE html>
<html>
<head>
    <title>Authorize</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; }
        .btn { width: 100%; padding: 10px; margin: 10px 0; cursor: pointer; background-color: #4CAF50; color: white; border: none; }
        .btn-deny { background-color: #f44336; }
    </style>
</head>
<body>
    <h2>Authorization Request</h2>
    <p><strong>{{ client.client_name }}</strong> is requesting access to your account.</p>
    <p><strong>Scope:</strong> {{ scope }}</p>
    <form method="post">
        <button type="submit" name="confirm" value="yes" class="btn">Approve</button>
        <button type="submit" name="confirm" value="no" class="btn btn-deny">Deny</button>
    </form>
</body>
</html>
'''


@app.route('/')
def index():
    return jsonify({
        'message': 'OAuth 2.0 Authorization Server',
        'version': '1.0.0',
        'endpoints': {
            'authorize': '/oauth/authorize',
            'token': '/oauth/token',
            'revoke': '/oauth/revoke'
        }
    })


@app.route('/health')
def health():
    return jsonify({'status': 'healthy'})


# DB 스키마 및 토큰 정보 확인 엔드포인트 (디버깅용)
@app.route('/debug/db-info', methods=['GET'])
def debug_db_info():
    """DB 스키마 및 토큰 정보 확인 (디버깅용)"""
    import time
    from sqlalchemy import inspect
    
    info = {
        'database_uri': app.config['SQLALCHEMY_DATABASE_URI'],
        'tables': [],
        'oauth2_tokens': {
            'count': 0,
            'sample_tokens': []
        },
        'oauth2_clients': {
            'count': 0,
            'clients': []
        },
        'users': {
            'count': 0,
            'users': []
        }
    }
    
    try:
        # 테이블 목록
        inspector = inspect(db.engine)
        info['tables'] = inspector.get_table_names()
        
        # OAuth2Token 테이블 정보
        if 'oauth2_tokens' in info['tables']:
            tokens = OAuth2Token.query.all()
            info['oauth2_tokens']['count'] = len(tokens)
            current_time = int(time.time())
            
            for token in tokens[:5]:  # 최대 5개만
                token_info = {
                    'id': token.id,
                    'access_token_preview': f"{token.access_token[:10]}...{token.access_token[-5:]}" if len(token.access_token) > 15 else token.access_token[:15],
                    'client_id': token.client_id,
                    'user_id': token.user_id,
                    'scope': token.scope,
                    'expires_at': token.expires_at,
                    'current_time': current_time,
                    'time_until_expiry': token.expires_at - current_time,
                    'is_expired': token.is_expired(),
                    'is_revoked': token.is_revoked(),
                    'created_at': str(token.created_at) if token.created_at else None
                }
                info['oauth2_tokens']['sample_tokens'].append(token_info)
        
        # OAuth2Client 테이블 정보
        if 'oauth2_clients' in info['tables']:
            clients = OAuth2Client.query.all()
            info['oauth2_clients']['count'] = len(clients)
            for client in clients:
                info['oauth2_clients']['clients'].append({
                    'client_id': client.client_id,
                    'client_name': client.client_name,
                    'redirect_uris': client.redirect_uris,
                    'scope': client.scope
                })
        
        # User 테이블 정보
        if 'users' in info['tables']:
            users = User.query.all()
            info['users']['count'] = len(users)
            for user in users:
                info['users']['users'].append({
                    'id': user.id,
                    'username': user.username,
                    'email': user.email,
                    'role': user.role
                })
        
    except Exception as e:
        info['error'] = str(e)
    
    return jsonify(info)


# User registration
@app.route('/auth/register', methods=['POST'])
def register():
    data = request.get_json()
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')

    if not username or not email or not password:
        return jsonify({'error': 'Missing required fields'}), 400

    if User.query.filter_by(username=username).first():
        return jsonify({'error': 'Username already exists'}), 400

    user = User(username=username, email=email, role='customer')
    user.set_password(password)
    db.session.add(user)
    db.session.commit()

    return jsonify({
        'message': 'User registered successfully',
        'user_id': user.id,
        'username': user.username
    }), 201


# Login page for OAuth flow
@app.route('/oauth/login', methods=['GET', 'POST'])
def oauth_login():
    # prompt=login 파라미터가 있으면 강제 로그인 (세션 삭제)
    if request.args.get('prompt') == 'login':
        session.pop('user_id', None)
        print("🔓 Force login: Session cleared")
    
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')

        user = User.query.filter_by(username=username).first()

        if user and user.check_password(password):
            # 세션에 사용자 ID 저장
            session['user_id'] = user.id
            
            # 원래 가려던 주소(Authorize)와 파라미터 복구
            from urllib.parse import urlencode
            next_url = request.args.get('next', '/oauth/authorize')
            # NOTE: prompt=login은 "로그인 강제" 용도이며, 로그인 이후 authorize로 그대로 넘기면
            # authorize에서 다시 세션을 지우게 되어 login->authorize 무한 루프가 발생할 수 있음.
            # 따라서 로그인 성공 후에는 prompt 파라미터를 제거한다.
            params = {k: v for k, v in request.args.items() if k not in ('next', 'prompt')}
            
            if params:
                target_url = f"{next_url}?{urlencode(params)}"
            else:
                target_url = next_url
            
            print(f"✅ Login successful for {username}, redirecting to {target_url}")
            return redirect(target_url)
        else:
            return render_template_string(LOGIN_TEMPLATE, error='Invalid username or password')

    return render_template_string(LOGIN_TEMPLATE)


# OAuth 2.0 Authorization Endpoint
@app.route('/oauth/authorize', methods=['GET', 'POST'])
def authorize():
    # prompt=login 이면 "세션이 남아있더라도" 무조건 로그인 화면을 다시 보여줘야 한다.
    # (CustomTabs/Chrome 쿠키에 OAuth 서버 세션이 남아있으면 앱 로그아웃(토큰 삭제)만으로는
    #  서버 로그인 상태가 유지되어 Approve 화면으로 바로 넘어가는 현상이 발생할 수 있음)
    if request.args.get('prompt') == 'login':
        session.pop('user_id', None)
        print("🔓 Force login (authorize): Session cleared")

    # Get current user from session
    user_id = session.get('user_id')
    if not user_id:
        # Redirect to login page with current URL params
        from urllib.parse import urlencode
        login_url = f'/oauth/login?next=/oauth/authorize&{urlencode(request.args)}'
        return redirect(login_url)

    user = User.query.get(user_id)

    if request.method == 'GET':
        try:
            grant = authorization.get_consent_grant(end_user=user)
            client = grant.client
            scope = grant.request.scope
            
            # 이미 승인된 토큰이 있는지 확인 (자동 승인)
            existing_token = OAuth2Token.query.filter_by(
                user_id=user.id,
                client_id=client.client_id
            ).filter(
                OAuth2Token.scope.contains(scope)  # 요청한 scope가 이미 승인된 scope에 포함되어 있는지
            ).first()
            
            if existing_token and not existing_token.is_expired():
                # 이미 승인된 토큰이 있으면 자동으로 승인 (Approve 화면 건너뛰기)
                print(f"✅ Auto-approving: User {user.username} already authorized {client.client_name}")
                return authorization.create_authorization_response(grant_user=user)

            # 승인된 토큰이 없으면 Approve/Deny 화면 표시
            return render_template_string(
                AUTHORIZE_TEMPLATE,
                client=client,
                scope=scope
            )
        except Exception as e:
            return jsonify({'error': str(e)}), 400

    # POST - User confirmed or denied
    confirm = request.form.get('confirm')
    if confirm == 'yes':
        grant_user = user
    else:
        grant_user = None

    return authorization.create_authorization_response(grant_user=grant_user)


# OAuth 2.0 Token endpoint
@app.route('/oauth/token', methods=['POST'])
def issue_token():
    return authorization.create_token_response()


# OAuth 2.0 Revoke endpoint
@app.route('/oauth/revoke', methods=['POST'])
def revoke_token():
    return authorization.create_endpoint_response('revocation')


# Protected resource: User info
@app.route('/api/me', methods=['GET'])
@require_oauth('profile')
def get_user_info():
    """
    OAuth2 protected endpoint for user information.
    
    This endpoint requires:
    - Valid Bearer token in Authorization header
    - Token must have 'profile' scope
    - Token must not be expired or revoked
    
    Authlib automatically:
    - Extracts Bearer token from Authorization header
    - Validates token against database
    - Checks expiration and revocation status
    - Verifies scope requirements
    """
    # Authlib 표준: request.oauth 가 아니라 current_token 을 사용한다.
    # (request.oauth 는 환경/버전에 따라 존재하지 않아 500을 유발할 수 있음)
    token = current_token
    user = token.user
    
    # 디버깅: 토큰 검증 성공 로그
    auth_header = request.headers.get('Authorization', '')
    token_preview = ''
    token_value = ''
    if auth_header.startswith('Bearer '):
        token_value = auth_header[7:]
        token_preview = f"{token_value[:10]}...{token_value[-5:]}" if len(token_value) > 15 else token_value[:15]
    
    # DB에서 토큰 조회하여 상세 정보 로깅
    db_token = OAuth2Token.query.filter_by(access_token=token_value).first()
    import time
    current_time = int(time.time())
    
    print(f"✅ OAuth Token Validation Success:")
    print(f"   Endpoint: {request.path}")
    print(f"   Token Preview: {token_preview}")
    print(f"   Token Length: {len(token_value)}")
    if db_token:
        print(f"   DB Token ID: {db_token.id}")
        print(f"   DB Expires At: {db_token.expires_at}")
        print(f"   Current Time: {current_time}")
        print(f"   Time Until Expiry: {db_token.expires_at - current_time} seconds")
        print(f"   DB Is Expired: {db_token.is_expired()}")
        print(f"   DB Is Revoked: {db_token.is_revoked()}")
    else:
        print(f"   ⚠️ WARNING: Token not found in DB!")
    print(f"   User: {user.username}")
    print(f"   Scope: {token.get_scope() if hasattr(token, 'get_scope') else getattr(token, 'scope', '')}")
    
    return jsonify({
        'id': user.id,
        'username': user.username,
        'email': user.email,
        'role': user.role
    })


# Protected resource: Customer orders
@app.route('/api/customer/orders', methods=['GET'])
@require_oauth('customer')
def get_customer_orders():
    token = current_token
    user = token.user
    return jsonify({
        'message': 'Customer orders endpoint',
        'user': user.username,
        'role': user.role,
        'scope': token.get_scope() if hasattr(token, 'get_scope') else getattr(token, 'scope', ''),
        'orders': []
    })


# Protected resource: Store dashboard (privileged)
@app.route('/api/store/dashboard', methods=['GET'])
@require_oauth('store')
def get_store_dashboard():
    token = current_token
    user = token.user
    return jsonify({
        'message': 'Store owner dashboard - PRIVILEGED ACCESS',
        'user': user.username,
        'role': user.role,
        'scope': token.get_scope() if hasattr(token, 'get_scope') else getattr(token, 'scope', ''),
        'revenue': 125000,
        'pending_orders': 5,
        'customer_data': []
    })


# Admin: Register OAuth client
@app.route('/admin/register_client', methods=['POST'])
def register_client():
    data = request.get_json()
    client_name = data.get('client_name')
    redirect_uris = data.get('redirect_uris', '')
    scope = data.get('scope', 'profile')

    client_id = gen_salt(48)
    client_secret = gen_salt(48)

    client = OAuth2Client(
        client_id=client_id,
        client_name=client_name,
        redirect_uris=redirect_uris,
        grant_types='authorization_code password refresh_token',
        response_types='code',
        scope=scope
    )
    # Use set_client_secret to hash the secret securely
    # Note: We store the hash but return the plaintext secret to the client
    # This is a one-time operation - the client must save the secret securely
    client.set_client_secret(client_secret)
    db.session.add(client)
    db.session.commit()

    return jsonify({
        'client_id': client_id,
        'client_secret': client_secret,  # Return plaintext (one-time only)
        'client_name': client_name
    }), 201


# Initialize database
@app.cli.command()
def initdb():
    """Initialize the database."""
    db.create_all()
    print('✅ Database initialized!')
    print(f'   Database URI: {app.config["SQLALCHEMY_DATABASE_URI"]}')
    
    # 테이블 생성 확인
    from sqlalchemy import inspect
    inspector = inspect(db.engine)
    tables = inspector.get_table_names()
    print(f'   Created tables: {", ".join(tables)}')
    
    # OAuth2Token 테이블 구조 확인
    if 'oauth2_tokens' in tables:
        columns = [col['name'] for col in inspector.get_columns('oauth2_tokens')]
        print(f'   oauth2_tokens columns: {", ".join(columns)}')


# Create test data
@app.cli.command()
def create_test_data():
    """Create test users and OAuth client."""
    db.create_all()
    print("Creating test data...")

    # Create customer user (wheats DB와 매핑: consumer1@wheats.local) - only if not exists
    if not User.query.filter_by(username='customer1').first():
        customer = User(username='customer1', email='consumer1@wheats.local', role='customer')
        customer.set_password('password123')
        db.session.add(customer)
        print("  ✓ Created user: customer1")
    else:
        print("  ⊙ User already exists: customer1")

    # Create store owner user (wheats DB와 매핑: merchant1@wheats.local) - only if not exists
    if not User.query.filter_by(username='storeowner1').first():
        store_owner = User(username='storeowner1', email='merchant1@wheats.local', role='store')
        store_owner.set_password('password123')
        db.session.add(store_owner)
        print("  ✓ Created user: storeowner1")
    else:
        print("  ⊙ User already exists: storeowner1")

    # Create test OAuth client - only if not exists
    if not OAuth2Client.query.filter_by(client_id='android_app_client').first():
        client = OAuth2Client(
            client_id='android_app_client',
            client_name='Android Test App',
            redirect_uris='app://oauth2callback com.example.app://oauth2callback com.example.deliveryapp://oauth2callback com.example.mobile://oauth2callback',
            grant_types='authorization_code password refresh_token',
            response_types='code',
            scope='profile customer store'
        )
        # Use set_client_secret to hash the secret securely
        client.set_client_secret('secret123')
        db.session.add(client)
        print("  ✓ Created OAuth client: android_app_client")
    else:
        print("  ⊙ OAuth client already exists: android_app_client")

    db.session.commit()

    print('✅ Test data created successfully!')
    print('')
    print('=== Customer Account ===')
    print('Username: customer1')
    print('Password: password123')
    print('Role: customer')
    print('')
    print('=== Store Owner Account ===')
    print('Username: storeowner1')
    print('Password: password123')
    print('Role: store')
    print('')
    print('=== OAuth Client ===')
    print('Client ID: android_app_client')
    print('Client Secret: secret123')
    print('Allowed Scopes: profile customer store')


if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(host='0.0.0.0', port=int(os.getenv('PORT', 3000)), debug=True)

