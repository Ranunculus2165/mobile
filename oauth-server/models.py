from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash
import time

db = SQLAlchemy()


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(255), nullable=False)
    role = db.Column(db.String(20), default='customer')  # customer, store, admin

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    def get_user_id(self):
        return self.id

    def __repr__(self):
        return f'<User {self.username}>'


class OAuth2Client(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    client_id = db.Column(db.String(48), unique=True, nullable=False, index=True)
    client_secret = db.Column(db.String(48))
    client_name = db.Column(db.String(100))
    redirect_uris = db.Column(db.Text)
    grant_types = db.Column(db.Text)
    response_types = db.Column(db.Text)
    scope = db.Column(db.Text)
    default_redirect_uri = db.Column(db.Text)
    default_scope = db.Column(db.Text)

    def get_allowed_scope(self, scope):
        if not scope:
            return ''
        allowed = set(self.scope.split()) if self.scope else set()
        requested = set(scope.split())
        return ' '.join(allowed & requested)

    def check_redirect_uri(self, redirect_uri):
        if not self.redirect_uris:
            return False
        return redirect_uri in self.redirect_uris.split()

    def check_client_secret(self, client_secret):
        return self.client_secret == client_secret

    def check_endpoint_auth_method(self, method, endpoint):
        if endpoint == 'token':
            return method in ['client_secret_basic', 'client_secret_post', 'none']
        return True

    def check_grant_type(self, grant_type):
        if not self.grant_types:
            return False
        return grant_type in self.grant_types.split()

    def check_response_type(self, response_type):
        if not self.response_types:
            return False
        return response_type in self.response_types.split()


class OAuth2AuthorizationCode(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    code = db.Column(db.String(120), unique=True, nullable=False, index=True)
    client_id = db.Column(db.String(48), nullable=False)
    redirect_uri = db.Column(db.Text)
    scope = db.Column(db.Text)
    user_id = db.Column(db.Integer, nullable=False)
    code_challenge = db.Column(db.String(128))
    code_challenge_method = db.Column(db.String(10))
    expires_at = db.Column(db.Integer, nullable=False)

    def is_expired(self):
        return int(time.time()) >= self.expires_at

    def get_redirect_uri(self):
        return self.redirect_uri

    def get_scope(self):
        return self.scope or ''

    def __repr__(self):
        return f'<OAuth2AuthorizationCode {self.code}>'


class OAuth2Token(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    client_id = db.Column(db.String(48), nullable=False, index=True)
    user_id = db.Column(db.Integer)
    token_type = db.Column(db.String(40))
    access_token = db.Column(db.String(255), unique=True, nullable=False, index=True)
    refresh_token = db.Column(db.String(255), unique=True, index=True)
    scope = db.Column(db.Text)
    expires_at = db.Column(db.Integer, nullable=False)
    revoked = db.Column(db.Boolean, default=False)

    def is_expired(self):
        return int(time.time()) >= self.expires_at

    def get_scope(self):
        return self.scope or ''

    def get_expires_in(self):
        if self.is_expired():
            return 0
        return self.expires_at - int(time.time())

    def __repr__(self):
        return f'<OAuth2Token {self.access_token[:10]}...>'

