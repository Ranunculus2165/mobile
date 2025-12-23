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
    <title>Authorize</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; }
        .btn { width: 100%; padding: 10px; margin: 10px 0; cursor: pointer; background-color: #4CAF50; color: white; border: none; }
        .btn-deny { background-color: #f44336; }
    </style>
</head>
<body>
    <h2>Authorize Application</h2>
    <p>Application <strong>{{ client_name }}</strong> wants to access your account with the following permissions:</p>
    <ul>
        {% for scope in scopes %}
        <li>{{ scope }}</li>
        {% endfor %}
    </ul>
    <form method="post">
        <button type="submit" name="confirm" value="yes" class="btn">Authorize</button>
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


# User login (non-OAuth, for testing)
@app.route('/auth/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({'error': 'Missing username or password'}), 400

    user = User.query.filter_by(username=username).first()
    if not user or not user.check_password(password):
        return jsonify({'error': 'Invalid credentials'}), 401

    session['user_id'] = user.id
    return jsonify({
        'message': 'Login successful',
        'user_id': user.id,
        'username': user.username
    })


# OAuth 2.0 Authorization endpoint
@app.route('/oauth/authorize', methods=['GET', 'POST'])
def authorize():
    if request.method == 'GET':
        # Check if user is logged in
        user_id = session.get('user_id')
        if not user_id:
            # Redirect to login page
            return render_template_string(LOGIN_TEMPLATE, error=None)

        user = User.query.get(user_id)
        if not user:
            return render_template_string(LOGIN_TEMPLATE, error='User not found')

        # Get authorization request
        try:
            grant = authorization.get_consent_grant(end_user=user)
        except Exception as e:
            return jsonify({'error': str(e)}), 400

        client = grant.client
        scopes = grant.request.scope.split() if grant.request.scope else []

        return render_template_string(
            AUTHORIZE_TEMPLATE,
            client_name=client.client_name or client.client_id,
            scopes=scopes
        )

    # POST: User confirmed or denied
    user_id = session.get('user_id')
    if not user_id:
        return jsonify({'error': 'Not logged in'}), 401

    user = User.query.get(user_id)
    confirm = request.form.get('confirm')

    if confirm == 'yes':
        try:
            grant = authorization.get_consent_grant(end_user=user)
            return authorization.create_authorization_response(grant_user=user)
        except Exception as e:
            return jsonify({'error': str(e)}), 400
    else:
        return authorization.create_authorization_response(grant_user=None)


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
    user = request.oauth.user
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
    user = request.oauth.user
    return jsonify({
        'message': 'Customer orders endpoint',
        'user': user.username,
        'role': user.role,
        'scope': request.oauth.scope,
        'orders': []
    })


# Protected resource: Store dashboard (privileged)
@app.route('/api/store/dashboard', methods=['GET'])
@require_oauth('store')
def get_store_dashboard():
    user = request.oauth.user
    return jsonify({
        'message': 'Store owner dashboard - PRIVILEGED ACCESS',
        'user': user.username,
        'role': user.role,
        'scope': request.oauth.scope,
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
        client_secret=client_secret,
        client_name=client_name,
        redirect_uris=redirect_uris,
        grant_types='authorization_code password refresh_token',
        response_types='code',
        scope=scope
    )
    db.session.add(client)
    db.session.commit()

    return jsonify({
        'client_id': client_id,
        'client_secret': client_secret,
        'client_name': client_name
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
        redirect_uris='app://oauth2callback com.example.app://oauth2callback com.example.deliveryapp://oauth2callback com.example.mobile://oauth2callback',
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

