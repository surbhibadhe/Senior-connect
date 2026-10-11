package com.project.seniorconnect.Config;


import com.project.seniorconnect.Entity.User;
import com.project.seniorconnect.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OidcUser googleUser =
                (OidcUser) authentication.getPrincipal();

        String googleId = googleUser.getSubject();
        String email = googleUser.getEmail();
        String name = googleUser.getFullName();
        String profilePicture = googleUser.getPicture();

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .orElseGet(User::new));

        user.setGoogleId(googleId);
        user.setEmail(email);
        user.setName(name);
        user.setProfilePicture(profilePicture);

        userRepository.save(user);

        response.sendRedirect("/");




    }
}