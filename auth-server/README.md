# OAuth2 Authorization Server

This module provides OAuth2 authorization services for the Agentic workshop components.

## Overview

The auth-server acts as a centralized OAuth2 Authorization Server that issues JWT tokens to client applications (web-app and sse components). It implements the OAuth2 Authorization Code flow, Client Credentials flow, and PKCE for public clients.

## Quick Start

### Building and Running

```bash
# Build the auth-server module
mvn clean install -pl auth-server

# Run the auth server
mvn spring-boot:run -pl auth-server

# Or run with JAR (after building)
java -jar auth-server/target/auth-server-1.0.0-SNAPSHOT.jar
```

The authorization server will start on **port 9000**.

### Access URLs

- **Authorization Endpoint**: http://localhost:9000/oauth2/authorize
- **Token Endpoint**: http://localhost:9000/oauth2/token
- **JWK Set**: http://localhost:9000/oauth2/jwks
- **OpenID Connect Discovery**: http://localhost:9000/.well-known/openid-configuration
- **Login Page**: http://localhost:9000/login
- **Health Check**: http://localhost:9000/actuator/health (Note: Protected by Spring Security)

## Demo Configuration

### Demo Users

| Username | Password | Roles                    |
|----------|----------|--------------------------|
| `user`   | password | USER                     |
| `admin`  | admin    | USER, ADMIN              |

### OAuth2 Clients

#### Web App Client (Authorization Code Flow)
- **Client ID**: `favourites-web`
- **Client Secret**: `web-secret`
- **Grant Types**: `authorization_code`, `refresh_token`
- **Redirect URIs**: 
  - `http://localhost:8080/login/oauth2/code/favourites`
  - `http://localhost:8080/authorized`
- **Post Logout Redirect URI**: `http://localhost:8080/`
- **Scopes**: `openid`, `profile`, `email`, `favourites.read`, `favourites.write`
- **Token Settings**:
  - Access Token TTL: 1 hour
  - Refresh Token TTL: 7 days
  - Refresh Token Rotation: Enabled

#### MCP Client (Client Credentials Flow - Server-to-Server)
- **Client ID**: `favourites-mcp`
- **Client Secret**: `mcp-secret`
- **Grant Types**: `client_credentials`
- **Scopes**: `mcp.invoke`
- **Token Settings**:
  - Access Token TTL: 1 hour

## Testing the Authorization Server

### 1. Authorization Code Flow (Web App)

Navigate to:
```
http://localhost:9000/oauth2/authorize?response_type=code&client_id=favourites-web&redirect_uri=http://localhost:8080/authorized&scope=openid%20profile%20favourites.read&state=xyz
```

Login with demo credentials and you'll be redirected with an authorization code.

### 2. Client Credentials Flow (MCP Server)

```bash
# Request access token for MCP client
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "favourites-mcp:mcp-secret" \
  -d "grant_type=client_credentials&scope=mcp.invoke"
```

### 3. Token Introspection

```bash
# Introspect a token (replace TOKEN with actual token)
curl -X POST http://localhost:9000/oauth2/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "favourites-web:web-secret" \
  -d "token=TOKEN"
```

### 4. JWK Set

```bash
# Get public keys for JWT verification
curl http://localhost:9000/oauth2/jwks
```

## Integration with Other Components

### Web App Integration

The web-app should be configured as an OAuth2 client:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          favourites:
            client-id: favourites-web
            client-secret: web-secret
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/login/oauth2/code/favourites
            scope: openid,profile,email,favourites.read,favourites.write
        provider:
          favourites:
            issuer-uri: http://localhost:9000
```

### MCP Component Integration

The MCP component should use client credentials flow:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          mcp:
            client-id: favourites-mcp
            client-secret: mcp-secret
            authorization-grant-type: client_credentials
            scope: mcp.invoke
        provider:
          mcp:
            token-uri: http://localhost:9000/oauth2/token
```

## Architecture Notes

### Security Features
- **JWT Tokens**: Uses RS256 (RSA with SHA-256) for signing
- **PKCE Support**: Required for public clients
- **Refresh Tokens**: Supported with configurable TTL
- **Scope-based Authorization**: Fine-grained permissions

### Token Settings
- **Access Token TTL**: 1 hour (both web and MCP clients)
- **Refresh Token TTL**: 7 days (web client only)
- **Refresh Token Rotation**: Enabled (no reuse)

### Development Mode
- **Debug Logging**: Enabled for OAuth2 and Security components
- **DevTools**: Hot reloading enabled
- **No Consent Required**: Streamlined for development

## Production Considerations

When deploying to production, consider:

1. **External Key Management**: Replace in-memory RSA key generation
2. **Database Storage**: Replace in-memory client/user stores
3. **TLS/HTTPS**: Enable secure transport
4. **Client Secret Rotation**: Implement secret management
5. **Monitoring**: Enable actuator endpoints with security
6. **Rate Limiting**: Add OAuth2 endpoint protection

## Troubleshooting

### Common Issues

1. **Port Conflicts**: Ensure ports 9000 and 9001 are available
2. **JWT Verification**: Check system clock synchronization
3. **Redirect URI Mismatch**: Verify exact URI matching in client configuration
4. **Scope Issues**: Ensure requested scopes are registered for the client

### Debug Logging

Enable additional logging in `application.yml`:
```yaml
logging:
  level:
    org.springframework.security.oauth2: TRACE
    org.springframework.security.web: TRACE
```