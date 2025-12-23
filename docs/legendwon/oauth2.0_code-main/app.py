from flask import Flask, request, jsonify, render_template_string, session, redirect
from flask_cors import CORS
from werkzeug.security import gen_salt
from models import db, User, OAuth2Client, OAuth2AuthorizationCode, OAuth2Token
from oauth2 import config_oauth
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
    <title>Authorization</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 500px; margin: 50px auto; padding: 20px; }
        .btn { padding: 10px 20px; margin: 5px; cursor: pointer; }
        .approve { background-color: #4CAF50; color: white; border: none; }
        .deny { background-color: #f44336; color: white; border: none; }
    </style>
</head>
<body>
    <h2>Authorization Request</h2>
    <p><strong>{{ client.client_name }}</strong> is requesting access to your account.</p>
    <p><strong>Scope:</strong> {{ scope }}</p>
    <form method="post">
        <button type="submit" name="confirm" value="yes" class="btn approve">Approve</button>
        <button type="submit" name="confirm" value="no" class="btn deny">Deny</button>
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
            'register': '/auth/register',
            'login': '/auth/login',
            'authorize': '/oauth/authorize',
            'token': '/oauth/token',
            'user_info': '/api/me'
        }
    })


# User Registration Endpoint
@app.route('/auth/register', methods=['POST'])
def register():
    data = request.get_json()

    if not data or not data.get('username') or not data.get('password') or not data.get('email'):
        return jsonify({'error': 'Missing required fields'}), 400

    # Check if user already exists
    if User.query.filter_by(username=data['username']).first():
        return jsonify({'error': 'Username already exists'}), 409

    if User.query.filter_by(email=data['email']).first():
        return jsonify({'error': 'Email already exists'}), 409

    # Create new user
    role = data.get('role', 'customer')  # Default to 'customer' if not specified
    if role not in ['customer', 'store']:
        return jsonify({'error': 'Invalid role. Must be customer or store'}), 400

    user = User(username=data['username'], email=data['email'], role=role)
    user.set_password(data['password'])

    db.session.add(user)
    db.session.commit()

    return jsonify({
        'message': 'User registered successfully',
        'user_id': user.id,
        'username': user.username,
        'role': user.role
    }), 201


# User Login Endpoint
@app.route('/auth/login', methods=['POST'])
def login():
    data = request.get_json()

    if not data or not data.get('username') or not data.get('password'):
        return jsonify({'error': 'Missing username or password'}), 400

    user = User.query.filter_by(username=data['username']).first()

    if not user or not user.check_password(data['password']):
        return jsonify({'error': 'Invalid username or password'}), 401

    # Store user in session for OAuth flow
    session['user_id'] = user.id

    return jsonify({
        'message': 'Login successful',
        'user_id': user.id,
        'username': user.username
    }), 200


# Login page for OAuth flow
@app.route('/oauth/login', methods=['GET', 'POST'])
def oauth_login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')

        user = User.query.filter_by(username=username).first()

        if user and user.check_password(password):
            session['user_id'] = user.id
            # Redirect back to authorize endpoint with original query params
            from urllib.parse import urlencode
            next_url = request.args.get('next', '/oauth/authorize')
            if request.args:
                # Preserve original OAuth parameters
                return f'<script>window.location.href="{next_url}?" + window.location.search.substring(1);</script>'
            return redirect(next_url)
        else:
            return render_template_string(LOGIN_TEMPLATE, error='Invalid username or password')

    return render_template_string(LOGIN_TEMPLATE)


# OAuth 2.0 Authorization Endpoint
@app.route('/oauth/authorize', methods=['GET', 'POST'])
def authorize():
    # Get current user from session
    user_id = session.get('user_id')
    if not user_id:
        # Redirect to login page with current URL params
        from urllib.parse import urlencode
        login_url = f'/oauth/login?next=/oauth/authorize&{urlencode(request.args)}'
        return f'<script>window.location.href="{login_url}";</script>'

    user = User.query.get(user_id)

    if request.method == 'GET':
        try:
            grant = authorization.get_consent_grant(end_user=user)
            client = grant.client
            scope = grant.request.scope

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


# OAuth 2.0 Token Endpoint
@app.route('/oauth/token', methods=['POST'])
def issue_token():
    return authorization.create_token_response()


# Protected Resource - Get Current User Info
@app.route('/api/me', methods=['GET'])
@require_oauth('profile')
def api_me():
    from flask import g
    token = g.authlib_server_oauth2_token
    user = User.query.get(token.user_id)
    return jsonify({
        'id': user.id,
        'username': user.username,
        'email': user.email,
        'role': user.role
    })


# Customer-only endpoint (requires 'customer' scope)
@app.route('/api/customer/orders', methods=['GET'])
@require_oauth('customer')
def customer_orders():
    from flask import g
    token = g.authlib_server_oauth2_token
    user = User.query.get(token.user_id)
    return jsonify({
        'message': 'Customer orders endpoint',
        'user': user.username,
        'role': user.role,
        'scope': token.scope,
        'orders': [
            {'id': 1, 'item': 'Pizza', 'status': 'delivered'},
            {'id': 2, 'item': 'Burger', 'status': 'pending'}
        ]
    })


# Store-only endpoint (requires 'store' scope) - SENSITIVE
@app.route('/api/store/dashboard', methods=['GET'])
@require_oauth('store')
def store_dashboard():
    from flask import g
    token = g.authlib_server_oauth2_token
    user = User.query.get(token.user_id)
    return jsonify({
        'message': 'Store owner dashboard - PRIVILEGED ACCESS',
        'user': user.username,
        'role': user.role,
        'scope': token.scope,
        'revenue': 125000,
        'pending_orders': 5,
        'customer_data': [
            {'name': 'Customer A', 'phone': '010-1234-5678'},
            {'name': 'Customer B', 'phone': '010-9876-5432'}
        ]
    })


# Revoke Token Endpoint
@app.route('/oauth/revoke', methods=['POST'])
def revoke_token():
    token = request.form.get('token')
    token_type_hint = request.form.get('token_type_hint', 'access_token')

    if token_type_hint == 'access_token':
        token_obj = OAuth2Token.query.filter_by(access_token=token).first()
    else:
        token_obj = OAuth2Token.query.filter_by(refresh_token=token).first()

    if token_obj:
        db.session.delete(token_obj)
        db.session.commit()

    return jsonify({'message': 'Token revoked successfully'}), 200


# Admin: Register OAuth Client (for testing)
@app.route('/admin/register_client', methods=['POST'])
def register_client():
    data = request.get_json()

    client_id = gen_salt(24)
    client_secret = gen_salt(48)

    client = OAuth2Client(
        client_id=client_id,
        client_secret=client_secret,
        client_name=data.get('client_name', 'Android App'),
        redirect_uris=data.get('redirect_uris', 'app://oauth2callback'),
        grant_types='authorization_code password refresh_token',
        response_types='code',
        scope=data.get('scope', 'profile email')
    )

    db.session.add(client)
    db.session.commit()

    return jsonify({
        'client_id': client_id,
        'client_secret': client_secret,
        'client_name': client.client_name
    }), 201


# Initialize database
@app.cli.command()
def initdb():
    """Initialize the database."""
    db.create_all()
    print('Database initialized!')


# Create test data
@app.cli.command()
def create_test_data():
    """Create test users and OAuth client."""
    db.create_all()

    # Create customer user
    customer = User(username='customer1', email='customer@example.com', role='customer')
    customer.set_password('password123')
    db.session.add(customer)

    # Create store owner user
    store_owner = User(username='storeowner1', email='store@example.com', role='store')
    store_owner.set_password('password123')
    db.session.add(store_owner)

    # Create test OAuth client
    client = OAuth2Client(
        client_id='android_app_client',
        client_secret='secret123',
        client_name='Android Test App',
        redirect_uris='app://oauth2callback com.example.app://oauth2callback com.example.deliveryapp://oauth2callback',
        grant_types='authorization_code password refresh_token',
        response_types='code',
        scope='profile customer store'
    )
    db.session.add(client)

    db.session.commit()

    print('Test data created!')
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
