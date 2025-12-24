from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime
import time

db = SQLAlchemy()


class User(db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False, index=True)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(255), nullable=False)
    role = db.Column(db.String(20), default='customer')  # 'customer' or 'store'
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    def get_user_id(self):
        return self.id

    def __repr__(self):
        return f'<User {self.username}>'


class OAuth2Client(db.Model):
    __tablename__ = 'oauth2_clients'

    id = db.Column(db.Integer, primary_key=True)
    client_id = db.Column(db.String(48), unique=True, nullable=False, index=True)
    client_secret = db.Column(db.String(120), nullable=False)
    client_name = db.Column(db.String(100), nullable=False)

    # OAuth 2.0 required fields
    redirect_uris = db.Column(db.Text, nullable=False)  # Space-separated URIs
    grant_types = db.Column(db.Text, nullable=False)    # Space-separated grant types
    response_types = db.Column(db.Text, nullable=False) # Space-separated response types
    scope = db.Column(db.Text, default='')

    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def get_client_id(self):
        return self.client_id

    def get_default_redirect_uri(self):
        return self.redirect_uris.split()[0] if self.redirect_uris else None

    def get_allowed_scope(self, scope):
        if not scope:
            return ''
        allowed = set(self.scope.split())
        requested = set(scope.split())
        return ' '.join(allowed & requested)

    def check_redirect_uri(self, redirect_uri):
        return redirect_uri in self.redirect_uris.split()

    def check_client_secret(self, client_secret):
        return self.client_secret == client_secret

    def check_grant_type(self, grant_type):
        return grant_type in self.grant_types.split()

    def check_response_type(self, response_type):
        return response_type in self.response_types.split()

    def check_endpoint_auth_method(self, method, endpoint):
        """Check if the client supports the given auth method for the endpoint."""
        # Allow all methods for simplicity (client_secret_basic, client_secret_post, none)
        return True

    def __repr__(self):
        return f'<OAuth2Client {self.client_name}>'


class OAuth2AuthorizationCode(db.Model):
    __tablename__ = 'oauth2_authorization_codes'

    id = db.Column(db.Integer, primary_key=True)
    code = db.Column(db.String(120), unique=True, nullable=False, index=True)
    client_id = db.Column(db.String(48), nullable=False)
    redirect_uri = db.Column(db.Text, nullable=False)
    scope = db.Column(db.Text, default='')
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'))

    code_challenge = db.Column(db.String(128))
    code_challenge_method = db.Column(db.String(10))

    expires_at = db.Column(db.Integer, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    user = db.relationship('User')

    def is_expired(self):
        return self.expires_at < time.time()

    def get_redirect_uri(self):
        return self.redirect_uri

    def get_scope(self):
        return self.scope

    def __repr__(self):
        return f'<OAuth2AuthorizationCode {self.code}>'


class OAuth2Token(db.Model):
    __tablename__ = 'oauth2_tokens'

    id = db.Column(db.Integer, primary_key=True)
    client_id = db.Column(db.String(48), nullable=False)
    token_type = db.Column(db.String(40), default='Bearer')
    access_token = db.Column(db.String(255), unique=True, nullable=False, index=True)
    refresh_token = db.Column(db.String(255), unique=True, index=True)
    scope = db.Column(db.Text, default='')
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'))

    expires_at = db.Column(db.Integer, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    user = db.relationship('User')

    def is_expired(self):
        return self.expires_at < time.time()

    def is_revoked(self):
        # For simplicity, tokens are not revoked by default
        # In production, you would check a revoked_at field
        return False

    def check_client(self, client):
        """Check if the token belongs to the given client"""
        return self.client_id == client.client_id

    def get_scope(self):
        return self.scope

    def get_expires_in(self):
        return self.expires_at - int(time.time())

    def __repr__(self):
        return f'<OAuth2Token {self.access_token[:10]}...>'
