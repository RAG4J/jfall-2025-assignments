# Understanding OAuth 2.0: A Beginner's Guide

*A practical introduction to OAuth 2.0 flows and how they work in our workshop project*

## What is OAuth 2.0?

OAuth 2.0 (Open Authorization 2.0) is like a digital bouncer for your applications. Instead of sharing passwords everywhere, OAuth allows applications to access resources on behalf of users without ever seeing their credentials.

### The Real-World Analogy

Imagine you want to use a photo printing service that can access your Google Photos:

- **❌ Old Way**: Give the printing service your Google username and password
- **✅ OAuth Way**: Google gives the printing service a special "access card" that only works for viewing your photos

The printing service never sees your Google password, and you can revoke the access card anytime!

## Why Do We Need OAuth?

### Problems OAuth Solves

1. **🔐 Password Sharing**: Users don't have to share passwords with third-party apps
2. **🎯 Limited Access**: Apps only get the permissions they actually need
3. **⏰ Time-Limited**: Access can expire automatically
4. **🚪 Revocable**: Users can revoke access without changing passwords
5. **🔒 Secure**: No passwords stored in multiple places

### Without OAuth
```
User → Enters password in third-party app
Third-party app → Uses password directly with service
❌ App stores your password
❌ App has full access to your account
❌ Hard to revoke access
```

### With OAuth
```
User → Authenticates with trusted service
Trusted service → Gives app a limited access token  
Third-party app → Uses token to access specific resources
✅ No password sharing
✅ Limited, specific permissions
✅ Easy to revoke
```

## Core OAuth 2.0 Concepts

### The Players (Roles)

1. **👤 Resource Owner** (User)
   - The person who owns the data
   - *Example: You, wanting to print photos*

2. **📱 Client** (Application)
   - The app that wants to access resources
   - *Example: Photo printing service*

3. **🏢 Resource Server** (API)
   - Where the actual data lives
   - *Example: Google Photos API*

4. **🛡️ Authorization Server** (Our OAuth Server!)
   - Issues access tokens and manages permissions
   - *Example: Google's OAuth service*

### Key Terms

- **🎫 Access Token**: A time-limited pass that grants access to specific resources
- **🔄 Refresh Token**: A longer-lived token used to get new access tokens
- **📋 Scope**: What permissions the token grants (e.g., "read photos", "send emails")
- **🔗 Redirect URI**: Where to send the user after authorization
- **🏷️ Client ID**: Public identifier for the application
- **🔑 Client Secret**: Private password for the application (server-side only)

## OAuth 2.0 Flows (Grant Types)

Our workshop implements two main flows:

### 1. Authorization Code Flow 🌐
*Perfect for web applications where users interact via browsers*

#### When to Use
- Web applications with user login
- Mobile apps with secure storage
- When you need user consent

#### The Flow
```mermaid
sequenceDiagram
    participant User
    participant WebApp
    participant AuthServer
    participant API

    User->>WebApp: 1. Clicks "Login with OAuth"
    WebApp->>User: 2. Redirects to Authorization Server
    User->>AuthServer: 3. Enters credentials & grants permission
    AuthServer->>WebApp: 4. Redirects back with authorization code
    WebApp->>AuthServer: 5. Exchanges code for access token
    AuthServer->>WebApp: 6. Returns access token (+ refresh token)
    WebApp->>API: 7. Uses access token to access user's data
```

#### Step-by-Step Breakdown

1. **🚀 Initiation**: User clicks "Login" in your web app
2. **📍 Redirect to Auth Server**: App redirects user to authorization server with:
   - `client_id`: Who's asking for permission
   - `redirect_uri`: Where to send the user back
   - `scope`: What permissions are requested
   - `state`: Random value to prevent CSRF attacks

3. **🔐 User Authentication**: User logs in at the authorization server
4. **✅ User Consent**: User sees what permissions are requested and approves
5. **📨 Authorization Code**: Auth server redirects back to your app with a short-lived code
6. **🔄 Token Exchange**: Your app exchanges the code for tokens (server-to-server, secure)
7. **🎫 Access Granted**: Your app now has an access token to call APIs on behalf of the user

#### In Our Workshop Project
```http
# Step 1-4: User goes through browser flow
GET http://localhost:9000/oauth2/authorize?response_type=code&client_id=favourites-web&redirect_uri=http://localhost:8080/authorized&scope=openid%20profile%20favourites.read&state=xyz123

# Step 5-6: Your app exchanges code for token
POST http://localhost:9000/oauth2/token
Authorization: Basic ZmF2b3VyaXRlcy13ZWI6d2ViLXNlY3JldA==
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&code=AUTH_CODE_FROM_REDIRECT&redirect_uri=http://localhost:8080/authorized
```

### 2. Client Credentials Flow 🤖
*Perfect for server-to-server communication, no user involved*

#### When to Use
- Backend services talking to each other
- Scheduled jobs needing API access
- Microservices authentication
- Our MCP (Model Context Protocol) server!

#### The Flow
```mermaid
sequenceDiagram
    participant ServiceA
    participant AuthServer
    participant ServiceB

    ServiceA->>AuthServer: 1. Authenticates with client credentials
    AuthServer->>ServiceA: 2. Returns access token
    ServiceA->>ServiceB: 3. Uses access token to access resources
```

#### Step-by-Step Breakdown

1. **🔑 Client Authentication**: Service identifies itself with client ID and secret
2. **🎫 Token Issued**: Auth server validates credentials and issues access token
3. **📡 API Access**: Service uses token to access protected resources

#### In Our Workshop Project
```http
# One-step process: Get token directly
POST http://localhost:9000/oauth2/token
Authorization: Basic ZmF2b3VyaXRlcy1tY3A6bWNwLXNlY3JldA==
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&scope=mcp.invoke
```

## Our Workshop Implementation

### Architecture Overview
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Web App       │    │  OAuth Server    │    │   MCP Server    │
│  (favourites-   │    │  (Port 9000)     │    │  (Port 8081)    │
│   web client)   │    │                  │    │                 │
│                 │    │  - User Auth     │    │                 │
│ Authorization   │◄──►│  - Token Issue   │◄──►│ Client          │
│ Code Flow       │    │  - Token Verify  │    │ Credentials     │
│                 │    │                  │    │ Flow            │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Configured Clients

#### 1. Web Application Client (`favourites-web`)
```yaml
Client ID: favourites-web
Client Secret: web-secret
Grant Types: authorization_code, refresh_token
Scopes: openid, profile, email, favourites.read, favourites.write
Redirect URIs:
  - http://localhost:8080/login/oauth2/code/favourites
  - http://localhost:8080/authorized
```

#### 2. MCP Server Client (`favourites-mcp`)
```yaml
Client ID: favourites-mcp  
Client Secret: mcp-secret
Grant Types: client_credentials
Scopes: mcp.invoke
```

### Demo Users
- **Username**: `user`, **Password**: `password` (Role: USER)
- **Username**: `admin`, **Password**: `admin` (Roles: USER, ADMIN)

## Token Deep Dive

### Access Tokens 🎫
- **Purpose**: Grant access to protected resources
- **Format**: JWT (JSON Web Token) in our implementation
- **Lifetime**: 1 hour
- **Contains**: User info, scopes, expiration time

### Refresh Tokens 🔄
- **Purpose**: Get new access tokens without user interaction
- **Lifetime**: 7 days
- **Only for**: Authorization Code flow (not Client Credentials)
- **Security**: Single use - new refresh token issued each time

### JWT Structure
```
Header.Payload.Signature
```

**Header**: Metadata about the token
```json
{
  "kid": "key-id",
  "alg": "RS256"
}
```

**Payload**: The actual claims/data
```json
{
  "sub": "favourites-mcp",
  "aud": "favourites-mcp", 
  "iss": "http://auth-server:9000",
  "scope": ["mcp.invoke"],
  "exp": 1759082344,
  "iat": 1759078744
}
```

**Signature**: Cryptographic proof of authenticity

## Security Considerations

### Best Practices We Implement

1. **🔐 HTTPS in Production**: Always use TLS for token transmission
2. **⏰ Short-lived Access Tokens**: 1-hour expiration limits exposure
3. **🔄 Refresh Token Rotation**: New refresh token with each use
4. **🎯 Scope Limitation**: Tokens only grant necessary permissions
5. **🛡️ State Parameter**: Prevents CSRF attacks in auth code flow
6. **🏠 Redirect URI Validation**: Prevents token hijacking

### Security Features
- **RS256 Signing**: Asymmetric crypto for JWT signatures
- **BCrypt Hashing**: Secure password/secret storage
- **No Consent Skip**: Users see what they're authorizing (can be configured)

## Common Use Cases

### 1. Web Application Login
**Scenario**: User wants to log into your web app using OAuth
**Flow**: Authorization Code
**User Experience**: Redirected to login page, grants permission, redirected back logged in

### 2. Mobile App Access
**Scenario**: Mobile app needs to access user's data
**Flow**: Authorization Code (with PKCE for extra security)
**User Experience**: Opens browser/webview, logs in, returns to app

### 3. Backend Service Integration
**Scenario**: Your server needs to call another API
**Flow**: Client Credentials  
**User Experience**: None - happens behind the scenes

### 4. Third-party Integration
**Scenario**: User wants app to integrate with external service
**Flow**: Authorization Code
**User Experience**: "Connect to Google/GitHub/etc" flow

## Testing the Implementation

### 1. Test Authorization Code Flow
1. Open browser to: `http://localhost:9000/oauth2/authorize?response_type=code&client_id=favourites-web&redirect_uri=http://localhost:8080/authorized&scope=openid%20profile%20favourites.read&state=xyz123`
2. Login with `user`/`password`
3. Get redirected with authorization code
4. Exchange code for tokens

### 2. Test Client Credentials Flow
```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "favourites-mcp:mcp-secret" \
  -d "grant_type=client_credentials&scope=mcp.invoke"
```

## Troubleshooting Common Issues

### 1. "invalid_client" Error
- **Problem**: Wrong client ID or secret
- **Solution**: Check credentials match configuration

### 2. "invalid_grant" Error  
- **Problem**: Client not configured for requested grant type
- **Solution**: Verify client supports the flow you're using

### 3. "unauthorized_client" Error
- **Problem**: Client not allowed for authorization code flow
- **Solution**: Check client configuration and redirect URIs

### 4. "access_denied" Error
- **Problem**: User declined to grant permissions
- **Solution**: User needs to approve the authorization request

## Going Deeper

### Advanced Topics to Explore
- **🔒 PKCE (Proof Key for Code Exchange)**: Extra security for mobile/SPA apps
- **🎭 OpenID Connect**: Identity layer on top of OAuth 2.0
- **🔑 JWT vs Opaque Tokens**: Different token formats and use cases
- **⚡ Token Introspection**: Validating tokens at resource servers
- **🌐 OAuth 2.1**: Latest specification updates and security improvements

### Recommended Reading
- [RFC 6749 - OAuth 2.0 Authorization Framework](https://tools.ietf.org/html/rfc6749)
- [OAuth 2.0 Security Best Practices](https://tools.ietf.org/html/draft-ietf-oauth-security-topics)
- [JWT.io](https://jwt.io/) - JWT decoder and learning resources
- [OAuth.com](https://www.oauth.com/) - Comprehensive OAuth guide

### Spring Security Resources
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Spring Authorization Server](https://spring.io/projects/spring-authorization-server)

## Conclusion

OAuth 2.0 might seem complex at first, but it's built on simple principles:

1. **🔐 Never share passwords** - Use tokens instead
2. **🎯 Limit access** - Only grant necessary permissions  
3. **⏰ Time-bound access** - Tokens expire
4. **🚪 Revocable access** - Users stay in control

Our workshop implementation demonstrates both major flows:
- **Authorization Code** for user-facing web applications
- **Client Credentials** for server-to-server communication

Understanding these fundamentals will help you build secure, modern applications that integrate well with the broader ecosystem of APIs and services.

Happy coding! 🚀

---

*This OAuth server runs on port 9000 and is ready for your workshop experiments. Check out `test-requests.http` for hands-on examples of each flow.*