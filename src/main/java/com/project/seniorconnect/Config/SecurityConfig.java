package com.project.seniorconnect.Config;

import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final OAuth2LoginSuccessHandler successHandler;

    public SecurityConfig(OAuth2LoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        OidcUserService oidcUserService = new OidcUserService();

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler()
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reviews", "/api/csrf").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.pathPattern("/api/**")
                        )
                )
                .oauth2Login(oauth -> oauth
                        .successHandler(successHandler)
                        .failureHandler((request, response, exception) -> {
                            log.warn("OAuth2 Authentication failed: {}", exception.getMessage());
                            response.sendRedirect("http://localhost:5173/signin?error=" +
                                    java.net.URLEncoder.encode(exception.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
                        })
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(userRequest -> {
                                    var user = oidcUserService.loadUser(userRequest);
                                    
                                    String email = user.getEmail();
                                    if (email == null && user.getAttribute("email") != null) {
                                        email = user.getAttribute("email").toString();
                                    }
                                    if (email != null) {
                                        email = email.trim();
                                    }

                                    String hostedDomain = user.getClaimAsString("hd");
                                    Object emailVerifiedClaim = user.getClaim("email_verified");

                                    log.info("OAuth2 login verification attempt - Email: {}, HostedDomain (hd): {}, email_verified: {}",
                                            email, hostedDomain, emailVerifiedClaim);

                                    // Domain check: Must end with @despu.edu.in or have Google Workspace hosted domain 'despu.edu.in'
                                    boolean isDespuDomain = (email != null && email.toLowerCase(Locale.ROOT).endsWith("@despu.edu.in"))
                                            || "despu.edu.in".equalsIgnoreCase(hostedDomain);

                                    // Verification check:
                                    // 1. Explicit true boolean or string "true"
                                    // 2. Google Workspace hosted domain 'despu.edu.in' (institutional directory accounts are verified by definition)
                                    // 3. Fallback when claim is omitted/null for @despu.edu.in accounts
                                    boolean isVerified = Boolean.TRUE.equals(user.getEmailVerified())
                                            || "true".equalsIgnoreCase(String.valueOf(emailVerifiedClaim))
                                            || ("despu.edu.in".equalsIgnoreCase(hostedDomain) && !Boolean.FALSE.equals(user.getEmailVerified()))
                                            || (isDespuDomain && user.getEmailVerified() == null);

                                    if (!isDespuDomain || !isVerified) {
                                        log.warn("Unauthorized domain rejected. Email: {}, Domain verified: {}, isVerified: {}",
                                                email, isDespuDomain, isVerified);
                                        throw new OAuth2AuthenticationException(
                                                new OAuth2Error("unauthorized_domain"),
                                                "Please use a verified @despu.edu.in account."
                                        );
                                    }

                                    return user;
                                })
                        )
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("X-XSRF-TOKEN", "Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}