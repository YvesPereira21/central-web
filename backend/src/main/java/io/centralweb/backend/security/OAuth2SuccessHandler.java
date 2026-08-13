package io.centralweb.backend.security;

import io.centralweb.backend.model.Profile;
import io.centralweb.backend.model.RefreshToken;
import io.centralweb.backend.model.User;
import io.centralweb.backend.model.enums.AuthProvider;
import io.centralweb.backend.model.enums.ProfileType;
import io.centralweb.backend.model.enums.UserRole;
import io.centralweb.backend.repository.ProfileRepository;
import io.centralweb.backend.repository.UserRepository;
import io.centralweb.backend.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    @Value("${app.oauth2.redirect-uri:http://localhost:4200/oauth2-redirect}")
    private String redirectUri;

    public OAuth2SuccessHandler(UserRepository userRepository,
                                ProfileRepository profileRepository,
                                TokenService tokenService,
                                RefreshTokenService refreshTokenService,
                                CookieUtil cookieUtil) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.cookieUtil = cookieUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        if (email == null) {
            throw new IllegalArgumentException("E-mail não fornecido pelo provedor OAuth2");
        }

        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        final String finalName = name;
        final String finalGoogleId = googleId != null ? googleId : email;

        User user = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, finalGoogleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existingUser -> {
                            if (existingUser.getProviderId() == null) {
                                existingUser.setProvider(AuthProvider.GOOGLE);
                                existingUser.setProviderId(finalGoogleId);
                                userRepository.save(existingUser);
                            }
                            return existingUser;
                        })
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setEmail(email);
                            newUser.setRole(UserRole.PERSON);
                            newUser.setProvider(AuthProvider.GOOGLE);
                            newUser.setProviderId(finalGoogleId);
                            User savedUser = userRepository.save(newUser);

                            Profile profile = new Profile();
                            profile.setName(finalName);
                            profile.setBio("Usuário cadastrado via Google");
                            profile.setProfileType(ProfileType.SELFTAUGHT);
                            profile.setUser(savedUser);
                            profileRepository.save(profile);

                            return savedUser;
                        })
                );

        String token = tokenService.generateToken(user);

        refreshTokenService.deleteByUserId(user.getUserId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(token);
        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(refreshToken.getToken());

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
