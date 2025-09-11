First we need an authorization server. As we are working with the Spring ecosystem, we will use Spring Authorization Server. Look at the auth-server directory in the project for the code.

If you have never worked with OAuth, we have added a brief introduction to OAuth in the file oauth-basics.md.

Follow along to set up the authorization server.

```
> Add the following to your `hosts` file:
127.0.0.1   auth-server
``` 

Start reading the README.md file in the auth-server directory.

Start the authorization server:

```bash
cd auth-server
mvn spring-boot:run
```

Use the requests in the readme file to test the server. If you use Intellij, you can use the test-requests.http file in the auth-server directory.

You now have a running authorization server. Next, you secure your remote sse server. First add the dependency to your pom.xml in the project favourites-mcp-remote:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

Then add the following to your application.yml of the same project:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://auth-server:9000
```

Finally, you need to add some security configuration to your application. Create a class called SecurityConfig.java in the config package. Note that we do not secure the normal http calls. The web pages that let us work with the MCP server are not secured. Only the endpoints that the MCP client will use are secured. In production, you would not add these kind of pages to the MCP server.

```java
@Configuration
public class MCPServerSecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Configure authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/sse",
                                "/mcp/**"
                        ).authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}

```

Now you can start the remote MCP server and check if the urls still work. Browse to localhost:8081 and play around with the favourites. 

```bash
cd favourites-mcp-remote
mvn spring-boot:run
```

Next, you can start the web application containing the agent that will connect to the remote MCP server. Before you do that, check if the Spring AI variant of the agent is configured with the remote MCP server. Include the right jar in the web-app/pom.xml. Set the right profile in application.yml of the web-app. Configure the remote mcp server in the application.yml for the agent:

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        type: SYNC
        sse:
          connections:
            location-mcp:
              url: http://localhost:8081
              sse-endpoint: /sse
```

We also need to configure the agent to use OAuth. Therefore, the springai-agent needs to replace the normal mcp client dependency with the webflux dependency. Remove the dependency  `spring-ai-starter-mcp-client`. Add the dependencies in the web-app/pom.xml:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

Add the security config class to the project of the springai-agent in the mcp package. This is a custom configuration that will create the webflux mcp transports with OAuth support.

```java
@Configuration
@EnableConfigurationProperties(McpSseClientProperties.class)
public class McpSecurityConfig {

    @Bean
    public List<NamedClientMcpTransport> webFluxClientTransports(McpSseClientProperties sseProperties,
                                                                 ObjectProvider<WebClient.Builder> webClientBuilderProvider,
                                                                 ObjectProvider<ObjectMapper> objectMapperProvider,
                                                                 OAuth2AuthorizedClientManager authorizedClientManager) {
        List<NamedClientMcpTransport> sseTransports = new ArrayList<>();
        WebClient.Builder webClientBuilderTemplate =
                webClientBuilderProvider.getIfAvailable(WebClient::builder);
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);

        for (Map.Entry<String, McpSseClientProperties.SseParameters> serverParameters :
                sseProperties.getConnections().entrySet()) {
            ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                    new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
            oauth2Client.setDefaultClientRegistrationId("favourites-mcp");

            WebClient.Builder webClientBuilder =
                    webClientBuilderTemplate.clone().baseUrl(serverParameters.getValue().url());
            String sseEndpoint =
                    serverParameters.getValue().sseEndpoint() != null ?
                            serverParameters.getValue().sseEndpoint() : "/sse";
            WebFluxSseClientTransport transport =
                    WebFluxSseClientTransport.builder(webClientBuilder.apply(oauth2Client.oauth2Configuration())).sseEndpoint(sseEndpoint).objectMapper(objectMapper).build();
            sseTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));
        }

        return sseTransports;
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

}

```

Now add the config so autoconfiguration is disable for webflux, do this is the application.yml of the web-app:
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.ai.mcp.client.autoconfigure.SseWebFluxTransportAutoConfiguration
```

Now, add the last missing config to the application.yml of the web-app to configure OAuth client:

```yaml
spring:
  # OAuth2 Security Configuration
  security:
    oauth2:
      client:
        registration:
          web-app:
            provider: web-app
            client-id: web-app-client
            client-secret: web-secret
            client-authentication-method: client_secret_basic
            authorization-grant-type: authorization_code
            redirect-uri: "http://localhost:8080/login/oauth2/code/web-app"
            scope: [ openid, profile, email, favourites.read, favourites.write ]
            client-name: "Agent Web App Client"          
          favourites-mcp:
            provider: auth-server
            client-id: favourites-mcp
            client-secret: mcp-secret
            authorization-grant-type: client_credentials
            scope: [ mcp.invoke ]
            client-name: "Favourites MCP Client"
        provider:
          web-app:
            issuer-uri: http://auth-server:9000
            user-name-attribute: sub          
          auth-server:
            token-uri: http://auth-server:9000/oauth2/token
            user-name-attribute: sub
```

Before you can login to the app and use the agent, you need to add security to the web application. Start by adding the dependencies to the pom.xml of the web-app:

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.thymeleaf.extras</groupId>
            <artifactId>thymeleaf-extras-springsecurity6</artifactId>
        </dependency>
```

Next step, add the security config to the web application in the form of a java class.

```java
package org.rag4j.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Security configuration for the Meeting Planner Web Application.
 * Configures OAuth2 login with the auth-server.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Main security filter chain configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configure session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry())
                )

                // Configure authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/public/**",
                                "/static/**",
                                "/webjars/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error",
                                "/actuator/health",
                                "/oauth2/logout"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // Configure OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/web-app")
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(authorizationRequestRepository())
                        )
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Configure logout
                .logout(logout -> logout
                        .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Configure OAuth2 client
                .oauth2Client(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Authorization request repository for storing OAuth2 authorization requests.
     */
    @Bean
    public HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    /**
     * Session registry for managing concurrent sessions.
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}

```

Finally, you need to add configuration to the web application.yml.

```yaml
server:
  port: 8080
  error:
    include-message: always
    include-binding-errors: always
  servlet:
    session:
      cookie:
        same-site: lax
        secure: false  # Set to true in production with HTTPS
        http-only: true
```

Now you can start all three applications.

- Start the auth-server, running on port 9000
- Start the favourites-mcp-remote, running on port 8081
- Start the web-app, running on port 8080

Browse to localhost:8080 and try to fetch the token or use the chat. You will be redirected to a login page. Play around with the agent.

```text
> Search for a talk
> Ask to add that talk to your favourites
> Use the favourites page to see the change. Or ask the agent to list your favourites.
```

If you are ready for a challenge, try to use the name of the logged in user in the MCP server by passing the token with the name. Remove the user to work for in the form with the agent. Beware, there is no solution or guidance for this challenge. You need to figure it out yourself.