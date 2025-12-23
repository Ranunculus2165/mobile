from authlib.integrations.flask_oauth2 import (
    AuthorizationServer,
    ResourceProtector,
)
from authlib.integrations.sqla_oauth2 import (
    create_query_client_func,
    create_save_token_func,
    create_bearer_token_validator,
)
from authlib.oauth2.rfc6749 import grants
from authlib.oauth2.rfc7636 import CodeChallenge
from werkzeug.security import gen_salt
from models import db, User, OAuth2Client, OAuth2AuthorizationCode, OAuth2Token
import time


class AuthorizationCodeGrant(grants.AuthorizationCodeGrant):
    TOKEN_ENDPOINT_AUTH_METHODS = ['client_secret_basic', 'client_secret_post', 'none']

    def save_authorization_code(self, code, request):
        code_challenge = request.data.get('code_challenge')
        code_challenge_method = request.data.get('code_challenge_method')

        auth_code = OAuth2AuthorizationCode(
            code=code,
            client_id=request.client.client_id,
            redirect_uri=request.redirect_uri,
            scope=request.scope,
            user_id=request.user.id,
            code_challenge=code_challenge,
            code_challenge_method=code_challenge_method,
            expires_at=int(time.time()) + 300,  # 5 minutes
        )
        db.session.add(auth_code)
        db.session.commit()
        return auth_code

    def query_authorization_code(self, code, client):
        auth_code = OAuth2AuthorizationCode.query.filter_by(
            code=code, client_id=client.client_id
        ).first()
        if auth_code and not auth_code.is_expired():
            return auth_code

    def delete_authorization_code(self, authorization_code):
        db.session.delete(authorization_code)
        db.session.commit()

    def authenticate_user(self, authorization_code):
        return User.query.get(authorization_code.user_id)


class PasswordGrant(grants.ResourceOwnerPasswordCredentialsGrant):
    # Enable refresh token generation
    GRANT_TYPE_REFRESH_TOKEN = True

    def authenticate_user(self, username, password):
        user = User.query.filter_by(username=username).first()
        if user and user.check_password(password):
            return user


class RefreshTokenGrant(grants.RefreshTokenGrant):
    def authenticate_refresh_token(self, refresh_token):
        token = OAuth2Token.query.filter_by(refresh_token=refresh_token).first()
        if token and not token.is_expired():
            return token

    def authenticate_user(self, credential):
        return User.query.get(credential.user_id)

    def revoke_old_credential(self, credential):
        credential.revoked = True
        db.session.add(credential)
        db.session.commit()

    # VULNERABILITY: Scope escalation via refresh token
    # This method bypasses ALL scope validation
    def _validate_token_scope(self, token):
        # INSECURE: Should validate that requested scope is subset of original scope
        # Currently does NO validation at all - allows arbitrary scope escalation!
        # This is the vulnerability: we just pass without checking anything
        pass


query_client = create_query_client_func(db.session, OAuth2Client)

def save_token(token, request):
    """Custom save_token function to handle expires_in -> expires_at conversion"""
    if request.user:
        user_id = request.user.get_user_id()
    else:
        user_id = request.client.user_id if hasattr(request.client, 'user_id') else None

    client = request.client

    # Convert expires_in to expires_at
    expires_in = token.pop('expires_in', 3600)
    issued_at = token.get('issued_at', int(time.time()))

    # Generate refresh token if not present
    refresh_token = token.get('refresh_token')
    if not refresh_token:
        from werkzeug.security import gen_salt
        refresh_token = gen_salt(48)

    item = OAuth2Token(
        client_id=client.client_id,
        user_id=user_id,
        token_type=token.get('token_type', 'Bearer'),
        access_token=token.get('access_token'),
        refresh_token=refresh_token,
        scope=token.get('scope', ''),
        expires_at=issued_at + expires_in
    )
    db.session.add(item)
    db.session.commit()

    # Add refresh_token to token dict for response
    token['refresh_token'] = refresh_token

    return item


def config_oauth(app):
    # INSECURE: Allow HTTP for testing/development only!
    import os
    os.environ['AUTHLIB_INSECURE_TRANSPORT'] = '1'

    authorization = AuthorizationServer()
    require_oauth = ResourceProtector()

    # Initialize authorization server
    authorization.init_app(app, query_client=query_client, save_token=save_token)

    # Register supported grant types
    authorization.register_grant(AuthorizationCodeGrant, [CodeChallenge(required=False)])
    authorization.register_grant(PasswordGrant)
    authorization.register_grant(RefreshTokenGrant)

    # Initialize resource protector
    bearer_cls = create_bearer_token_validator(db.session, OAuth2Token)
    require_oauth.register_token_validator(bearer_cls())

    return authorization, require_oauth
