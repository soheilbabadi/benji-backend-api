package social.benji.benji_backend_api.security;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import social.benji.benji_backend_api.lookup.controller.LookupController;
import social.benji.benji_backend_api.usermanagement.config.JwtProperties;
import social.benji.benji_backend_api.usermanagement.domain.User;
import social.benji.benji_backend_api.usermanagement.domain.UserRole;
import social.benji.benji_backend_api.usermanagement.domain.UserStatus;
import social.benji.benji_backend_api.usermanagement.repository.UserRepository;
import social.benji.benji_backend_api.usermanagement.security.JwtAuthenticationFilter;
import social.benji.benji_backend_api.usermanagement.security.JwtTokenProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtVerificationTest {

    static final byte[] KEY = "0123456789abcdef0123456789abcdef".getBytes(); // 32 bytes
    static int passed = 0, failed = 0;

    static void check(String name, boolean cond) {
        if (cond) { passed++; System.out.println("PASS  " + name); }
        else      { failed++; System.out.println("FAIL  " + name); }
    }

    static String mint(SecretKey key, UUID sub, Instant exp) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(sub == null ? null : sub.toString())
                .expirationTime(exp == null ? null : Date.from(exp))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).build(), claims);
        jwt.sign(new MACSigner(key.getEncoded()));
        return jwt.serialize();
    }

    public static void main(String[] args) throws Exception {
        SecretKey key = new SecretKeySpec(KEY, "HmacSHA256");
        JwtProperties props = new JwtProperties(new String(KEY), "HS256", Duration.ofMinutes(15));
        JwtTokenProvider provider = new JwtTokenProvider(props, key);

        // --- token-level verification ---
        UUID adminId = UUID.randomUUID(), userId = UUID.randomUUID();
        String goodAdmin = provider.createAccessToken(adminId, "+989120000000", List.of("ADMIN"), true);
        check("valid admin token verifies to subject", adminId.toString().equals(provider.verify(goodAdmin)));

        // forged: signed with a different key
        SecretKey evilKey = new SecretKeySpec("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes(), "HmacSHA256");
        String forged = mint(evilKey, adminId, Instant.now().plus(Duration.ofMinutes(5)));
        check("token signed with wrong key rejected", provider.verify(forged) == null);

        // expired
        String expired = mint(key, adminId, Instant.now().minus(Duration.ofMinutes(1)));
        check("expired token rejected", provider.verify(expired) == null);

        // no exp claim
        String noExp = mint(key, adminId, null);
        check("token without exp rejected", provider.verify(noExp) == null);

        // garbage
        check("garbage token rejected", provider.verify("not.a.jwt") == null);

        // attacker re-uses valid signature but swaps subject via alg none? just tamper payload
        String tampered = goodAdmin.substring(0, goodAdmin.length() - 2) + "xy";
        check("tampered token rejected", provider.verify(tampered) == null);

        // --- filter behavior (POST/PUT/DELETE requests) ---
        UserRepository repo = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider, repo);
        when(repo.findById(adminId)).thenReturn(Optional.of(
                User.builder().id(adminId).mobileNumber("+989120000000")
                    .role(UserRole.ADMIN).status(UserStatus.ACTIVE).build()));
        when(repo.findById(userId)).thenReturn(Optional.of(
                User.builder().id(userId).mobileNumber("+989121111111")
                    .role(UserRole.USER).status(UserStatus.BLOCKED).build()));

        SecurityContextHolder.clearContext();
        MockHttpServletRequest post = new MockHttpServletRequest("POST", "/api/lookups");
        post.addHeader("Authorization", "Bearer " + goodAdmin);
        filter.doFilter(post, new MockHttpServletResponse(), new MockFilterChain());
        var auth = SecurityContextHolder.getContext().getAuthentication();
        check("POST with valid admin JWT -> authenticated ADMIN authority",
                auth != null && auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        SecurityContextHolder.clearContext();
        MockHttpServletRequest put = new MockHttpServletRequest("PUT", "/api/lookups/1");
        put.addHeader("Authorization", "Bearer " + mint(key, userId, Instant.now().plus(Duration.ofMinutes(5))));
        filter.doFilter(put, new MockHttpServletResponse(), new MockFilterChain());
        check("PUT with JWT of BLOCKED user -> not authenticated (DB status re-checked)",
                SecurityContextHolder.getContext().getAuthentication() == null);

        SecurityContextHolder.clearContext();
        MockHttpServletRequest del = new MockHttpServletRequest("DELETE", "/api/lookups/1");
        del.addHeader("Authorization", "Bearer " + forged);
        filter.doFilter(del, new MockHttpServletResponse(), new MockFilterChain());
        check("DELETE with forged JWT -> not authenticated",
                SecurityContextHolder.getContext().getAuthentication() == null);

        SecurityContextHolder.clearContext();
        MockHttpServletRequest anonPost = new MockHttpServletRequest("POST", "/api/lookups");
        filter.doFilter(anonPost, new MockHttpServletResponse(), new MockFilterChain());
        check("POST without Authorization header -> anonymous (chain continues, entry point returns 401)",
                SecurityContextHolder.getContext().getAuthentication() == null);

        // --- controller annotations on POST/PUT/DELETE ---
        Class<?> c = LookupController.class;
        for (var m : c.getDeclaredMethods()) {
            boolean write = m.isAnnotationPresent(PostMapping.class) || m.isAnnotationPresent(PutMapping.class)
                    || m.isAnnotationPresent(DeleteMapping.class) || m.isAnnotationPresent(PreAuthorize.class) && m.isAnnotationPresent(org.springframework.web.bind.annotation.PatchMapping.class);
            if (m.isAnnotationPresent(PostMapping.class) || m.isAnnotationPresent(PutMapping.class) || m.isAnnotationPresent(DeleteMapping.class)) {
                PreAuthorize pa = m.getAnnotation(PreAuthorize.class);
                check(m.getName() + " (" + (m.isAnnotationPresent(PostMapping.class) ? "POST" : m.isAnnotationPresent(PutMapping.class) ? "PUT" : "DELETE") + ") guarded by @PreAuthorize ADMIN",
                        pa != null && pa.value().contains("ADMIN"));
            }
        }
        System.out.println("\nRESULT passed=" + passed + " failed=" + failed);
        System.exit(failed == 0 ? 0 : 1);
    }
}
