package com.project.seniorconnect.Config;


import java.util.Locale;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler successHandler;

    public SecurityConfig(OAuth2LoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        OidcUserService oidcUserService = new OidcUserService();

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                        .csrfTokenRequestHandler(
                                new CsrfTokenRequestAttributeHandler()
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .successHandler(successHandler)
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(userRequest -> {
                                    var user = oidcUserService.loadUser(userRequest);
                                    String email = user.getEmail();

                                    if (email == null
                                            || !email.toLowerCase(Locale.ROOT)
                                            .endsWith("@despu.edu.in")
                                            || !Boolean.TRUE.equals(user.getEmailVerified())) {
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
}