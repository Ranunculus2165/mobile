import requests
import json
import os

OAUTH_SERVER_URL = 'http://localhost:3000'
ENV_FILE = '.env'

def register_client():
    print(f"Connecting to {OAUTH_SERVER_URL}...")
    
    url = f"{OAUTH_SERVER_URL}/admin/register_client"
    payload = {
        "client_name": "Web Dashboard",
        "redirect_uris": "http://localhost:5000/callback",
        "scope": "store profile"
    }
    
    try:
        response = requests.post(url, json=payload)
        if response.status_code == 201:
            data = response.json()
            client_id = data['client_id']
            client_secret = data['client_secret']
            
            print("\n✅ Client Registered Successfully!")
            print(f"Client ID: {client_id}")
            print(f"Client Secret: {client_secret}")
            
            # Write to .env file
            with open(ENV_FILE, 'w') as f:
                f.write(f"CLIENT_ID={client_id}\n")
                f.write(f"CLIENT_SECRET={client_secret}\n")
                f.write(f"OAUTH_SERVER_URL={OAUTH_SERVER_URL}\n")
                f.write(f"API_SERVER_URL=http://localhost:8080\n")
                f.write(f"SECRET_KEY=dev-secret-key\n")
            
            print(f"\n✅ Configuration saved to {ENV_FILE}")
            print("You can now run the Flask app.")
        else:
            print(f"❌ Registration Failed: {response.status_code} {response.text}")
            
    except requests.exceptions.ConnectionError:
        print(f"❌ Could not connect to OAuth Server at {OAUTH_SERVER_URL}")
        print("Please ensure the OAuth Server (port 3000) is running.")

if __name__ == "__main__":
    register_client()
