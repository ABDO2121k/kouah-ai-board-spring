package kouahai.springboot.project.utils.security;

/**
 * Constants for security configuration
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // bax matkonx instrantiation
    }

    public static final String[] AUTH_ENDPOINTS = {
            "/api/auth/**"
    };

    public static final String[] PUBLIC_TEST_ENDPOINTS = {
            "/api/test/public"
    };

    public static final String[] API_DOCS_ENDPOINTS = {
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    public static final String[] DB_TEST_ENDPOINTS = {
            "/api/db-test/**"
    };

    public static final String[] WEBSOCKET_ENDPOINTS = {
            "/ws/**"
    };

    public static final String[][] ALL_PUBLIC_ENDPOINTS = {
            AUTH_ENDPOINTS,
            PUBLIC_TEST_ENDPOINTS,
            API_DOCS_ENDPOINTS,
            DB_TEST_ENDPOINTS,
            WEBSOCKET_ENDPOINTS
    };

    // CORS Configuration Constants
    public static final String[] CORS_ALLOWED_ORIGINS = {
            "http://localhost:5173",
            "http://localhost:3000"
    };

    public static final String[] CORS_ALLOWED_METHODS = {
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
    };

    public static final String[] CORS_ALLOWED_HEADERS = {
            "*"
    };

    public static final String[] CORS_EXPOSED_HEADERS = {
            "Authorization"
    };

    public static final boolean CORS_ALLOW_CREDENTIALS = true;

    public static final long CORS_MAX_AGE = 3600L;
}
