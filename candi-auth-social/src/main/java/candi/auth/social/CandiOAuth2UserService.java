package candi.auth.social;

import candi.auth.core.CandiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom OAuth2 user service that integrates with Candi's user model.
 * After loading the standard OAuth2User from the provider, maps it to a
 * {@link CandiUser} via the configured {@link SocialUserMapper}.
 *
 * <p>The resulting {@link CandiUser} is stored as an attribute on the
 * {@link OAuth2User} principal so it can be retrieved by the security context.</p>
 */
public class CandiOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CandiOAuth2UserService.class);

    /** Attribute key under which the mapped CandiUser is stored. */
    public static final String CANDI_USER_ATTR = "_candiUser";

    private final SocialUserMapper userMapper;

    public CandiOAuth2UserService(SocialUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.debug("OAuth2 user loaded from provider: {}", registrationId);

        // Map to CandiUser via the application's mapper
        CandiUser candiUser = userMapper.mapUser(registrationId, oAuth2User);

        // Create enhanced attributes with the CandiUser attached
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put(CANDI_USER_ATTR, candiUser);

        // Build authorities from CandiUser roles
        var authorities = candiUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());

        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
    }

    /**
     * Extracts the CandiUser from an OAuth2User principal.
     * Returns null if the principal was not processed by this service.
     */
    public static CandiUser extractCandiUser(OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return null;
        }
        Object attr = oAuth2User.getAttribute(CANDI_USER_ATTR);
        if (attr instanceof CandiUser candiUser) {
            return candiUser;
        }
        return null;
    }
}
