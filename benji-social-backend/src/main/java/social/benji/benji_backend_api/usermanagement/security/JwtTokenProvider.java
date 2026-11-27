package social.benji.benji_backend_api.usermanagement.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import social.benji.benji_backend_api.usermanagement.config.JwtProperties;

/**
 * Stateless access-token provider built on nimbus-jose-jwt (HS256).
 * Refresh tokens are opaque random strings handled by TokenService, not here.
 */
@Service
public class JwtTokenProvider {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_MOBILE_VERIFIED = "mv";

    private final SecretKey key;
    private final JWSAlgorithm algorithm;
    private final Duration accessTokenTtl;

    public JwtTokenProvider(JwtProperties properties, SecretKey jwtSigningKey) {
        this.key = jwtSigningKey;
        this.algorithm = JWSAlgorithm.parse(properties.algorithm());
        this.accessTokenTtl = properties.accessTokenTtl();
    }

    public Duration accessTokenTtl() {
        return accessTokenTtl;
    }

    public String createAccessToken(UUID userId, String mobileNumber, List<String> roles, boolean mobileVerified) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .claim("mobile", mobileNumber)
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_MOBILE_VERIFIED, mobileVerified)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(accessTokenTtl)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(algorithm).build(), claims);
        try {
            jwt.sign(new MACSigner(key.getEncoded()));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to sign access token", e);
        }
    }

    /** Returns the verified subject (user id) or empty for any invalid/expired token. */
    public String verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(key.getEncoded()))) {
                return null;
            }
            Date exp = jwt.getJWTClaimsSet().getExpirationTime();
            if (exp == null || exp.toInstant().isBefore(Instant.now())) {
                return null;
            }
            return jwt.getJWTClaimsSet().getSubject();
        } catch (java.text.ParseException | JOSEException e) {
            return null;
        }
    }
}
