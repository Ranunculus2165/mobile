from flask import Flask, redirect, request, session, url_for, render_template
import requests
import os
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)
app.secret_key = os.getenv('SECRET_KEY', 'web-secret-key-super-secure')

# Configuration (Defaults based on project structure)
OAUTH_SERVER_URL = os.getenv('OAUTH_SERVER_URL', 'http://localhost:3000')
API_SERVER_URL = os.getenv('API_SERVER_URL', 'http://localhost:8080')

# Client Credentials (Must match what is registered in OAuth Server)
CLIENT_ID = os.getenv('CLIENT_ID', 'web_dashboard_client')
CLIENT_SECRET = os.getenv('CLIENT_SECRET', 'web_secret_123')
REDIRECT_URI = 'http://localhost:5000/callback'

@app.route('/')
def home():
    if 'access_token' in session:
        return redirect(url_for('dashboard'))
    return render_template('login.html')

@app.route('/login')
def login():
    # Redirect user to OAuth Server to authorize
    auth_url = (
        f"{OAUTH_SERVER_URL}/oauth/authorize"
        f"?response_type=code"
        f"&client_id={CLIENT_ID}"
        f"&redirect_uri={REDIRECT_URI}"
        f"&scope=store profile"
    )
    return redirect(auth_url)

@app.route('/callback')
def callback():
    code = request.args.get('code')
    if not code:
        return "Error: No code provided", 400

    # Exchange Authorization Code for Access Token
    token_url = f"{OAUTH_SERVER_URL}/oauth/token"
    payload = {
        'grant_type': 'authorization_code',
        'code': code,
        'redirect_uri': REDIRECT_URI,
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_SECRET
    }
    
    try:
        resp = requests.post(token_url, data=payload)
        resp.raise_for_status() # Raise error for 4xx/5xx
        
        token_data = resp.json()
        session['access_token'] = token_data.get('access_token')
        
        # Optional: Get User Info to verify role
        # user_resp = requests.get(f"{OAUTH_SERVER_URL}/api/me", 
        #    headers={'Authorization': f"Bearer {session['access_token']}"})
        
        return redirect(url_for('dashboard'))
        
    except requests.exceptions.RequestException as e:
        print(f"Token Exchange Failed: {e}")
        if resp:
            print(f"Response: {resp.text}")
        return "Login Failed: Could not retrieve token.", 400

@app.route('/dashboard')
def dashboard():
    if 'access_token' not in session:
        return redirect(url_for('home'))
    
    # Fetch Store Data from API Server
    headers = {'Authorization': f"Bearer {session['access_token']}"}
    
    # HARDCODED STORE ID = 1 for Prototype
    # In production, we would get the store ID from the user's profile
    store_id = 1 
    
    try:
        resp = requests.get(f"{API_SERVER_URL}/api/stores/{store_id}", headers=headers)
        
        if resp.status_code == 401:
            session.pop('access_token', None)
            return redirect(url_for('home'))
            
        if resp.status_code != 200:
            return f"Error fetching store info: {resp.status_code} {resp.text}"
        
        response_data = resp.json()
        store_data = response_data.get('store') # Extract actual store object
        
        print(f"👀 Dashboard Render: Store Status is [{store_data.get('status')}]")
        return render_template('dashboard.html', store=store_data)
        
    except requests.exceptions.RequestException as e:
        return f"API Connection Error: {e}"

@app.route('/store/status', methods=['POST'])
def update_status():
    if 'access_token' not in session:
        return redirect(url_for('home'))

    # Debug: Form Data 확인
    is_open_val = request.form.get('is_open')
    print(f"🔘 Button Clicked! Received value: {is_open_val}")

    # 'on'이면 True(열기), 그 외(off)면 False(닫기)
    is_open = (is_open_val == 'on')
    print(f"🔄 Requesting Status Change to: {is_open} (True=Open, False=Closed)")
    
    store_id = 1 
    
    headers = {
        'Authorization': f"Bearer {session['access_token']}",
        'Content-Type': 'application/json'
    }
    payload = {'isOpen': is_open}
    
    try:
        url = f"{API_SERVER_URL}/api/stores/{store_id}/status"
        print(f"🚀 Calling API: PUT {url} | Payload: {payload}")
        
        resp = requests.put(url, json=payload, headers=headers)
        
        print(f"📩 API Response: {resp.status_code}")
        print(f"📄 Body: {resp.text}")

        resp.raise_for_status()
    except requests.exceptions.RequestException as e:
        print(f"❌ Error: {e}")
        return f"Failed to update status: {e}"
    
    return redirect(url_for('dashboard'))

@app.route('/logout')
def logout():
    session.pop('access_token', None)
    return redirect(url_for('home'))

if __name__ == '__main__':
    app.run(port=5000, debug=True)
