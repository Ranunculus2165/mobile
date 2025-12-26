from app import app, db, User, OAuth2Client

def init_db():
    with app.app_context():
        db.create_all()
        print("Creating test data...")

        # Create customer user
        if not User.query.filter_by(username='customer1').first():
            customer = User(username='customer1', email='consumer1@wheats.local', role='customer')
            customer.set_password('password123')
            db.session.add(customer)
            print("  ✓ Created user: customer1")
        else:
            print("  ⊙ User already exists: customer1")

        # Create store owner user
        if not User.query.filter_by(username='storeowner1').first():
            store_owner = User(username='storeowner1', email='merchant1@wheats.local', role='store')
            store_owner.set_password('password123')
            db.session.add(store_owner)
            print("  ✓ Created user: storeowner1")
        else:
            print("  ⊙ User already exists: storeowner1")

        # Create test OAuth client
        if not OAuth2Client.query.filter_by(client_id='android_app_client').first():
            client = OAuth2Client(
                client_id='android_app_client',
                client_name='Android Test App',
                redirect_uris='app://oauth2callback com.example.app://oauth2callback com.example.deliveryapp://oauth2callback com.example.mobile://oauth2callback',
                grant_types='authorization_code password refresh_token',
                response_types='code',
                scope='profile customer store'
            )
            client.set_client_secret('secret123')
            db.session.add(client)
            print("  ✓ Created OAuth client: android_app_client")
        else:
            print("  ⊙ OAuth client already exists: android_app_client")

        db.session.commit()
        print("\n✅ Initialization Complete! Now restart 'python app.py'")

if __name__ == '__main__':
    init_db()
