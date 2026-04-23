package kouahai.springboot.project.utils.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

public class SecurityMatchersBuilder {

    private SecurityMatchersBuilder() {
        // bax matkonx instrantiation
    }

    public static AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry configurePublicEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {

        return auth
                .requestMatchers(SecurityConstants.AUTH_ENDPOINTS).permitAll()
                .requestMatchers(SecurityConstants.PUBLIC_TEST_ENDPOINTS).permitAll()
                .requestMatchers(SecurityConstants.API_DOCS_ENDPOINTS).permitAll()
                .requestMatchers(SecurityConstants.DB_TEST_ENDPOINTS).permitAll()
                .requestMatchers(SecurityConstants.WEBSOCKET_ENDPOINTS).permitAll()
                .anyRequest().authenticated();
    }

    public static class Builder {
        private AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth;

        public Builder(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
            this.auth = auth;
        }

        public Builder permitAuthEndpoints() {
            auth = auth.requestMatchers(SecurityConstants.AUTH_ENDPOINTS).permitAll();
            return this;
        }

        public Builder permitPublicTestEndpoints() {
            auth = auth.requestMatchers(SecurityConstants.PUBLIC_TEST_ENDPOINTS).permitAll();
            return this;
        }

        public Builder permitApiDocsEndpoints() {
            auth = auth.requestMatchers(SecurityConstants.API_DOCS_ENDPOINTS).permitAll();
            return this;
        }

        public Builder permitDbTestEndpoints() {
            auth = auth.requestMatchers(SecurityConstants.DB_TEST_ENDPOINTS).permitAll();
            return this;
        }

        public Builder permitWebSocketEndpoints() {
            auth = auth.requestMatchers(SecurityConstants.WEBSOCKET_ENDPOINTS).permitAll();
            return this;
        }

        public Builder permitCustomEndpoints(String... endpoints) {
            auth = auth.requestMatchers(endpoints).permitAll();
            return this;
        }

        public AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authenticateRest() {
            return auth.anyRequest().authenticated();
        }
    }
}
