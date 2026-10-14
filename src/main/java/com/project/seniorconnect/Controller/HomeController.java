package com.project.seniorconnect.Controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class HomeController {

    @GetMapping("/")
    public void home(@AuthenticationPrincipal OidcUser user, HttpServletResponse response) throws IOException {
        // Smoothly redirect authenticated users into the React frontend
        response.sendRedirect("http://localhost:5173/home");
    }
}
