package com.sme.ledger.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 轻量 JWT 工具（HS256，标准三段式：header.payload.signature）。
 * 自实现签名，不引入第三方 JWT 依赖，满足「低成本、易维护」。
 */
@Component
public class JwtUtil {

    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final String secret;
    private final long expireMillis;
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expire-minutes:120}") long expireMinutes) {
        this.secret = secret;
        this.expireMillis = expireMinutes * 60_000L;
    }

    /** 生成 token，payload 携带用户 id、用户名、权限列表与过期时间。 */
    public String generateToken(Long userId, String username, List<String> authorities) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("uid", userId);
        payload.put("sub", username);
        payload.put("auth", authorities);
        payload.put("exp", System.currentTimeMillis() + expireMillis);

        String headerPart = b64(HEADER.getBytes(StandardCharsets.UTF_8));
        String bodyPart = b64(json(payload).getBytes(StandardCharsets.UTF_8));
        String unsigned = headerPart + "." + bodyPart;
        return unsigned + "." + sign(unsigned);
    }

    /** 校验签名与过期时间，返回 claims；非法或过期返回 null。 */
    public Claims parse(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String unsigned = parts[0] + "." + parts[1];
        if (!sign(unsigned).equals(parts[2])) {
            return null;
        }
        try {
            Map<String, Object> payload = mapper.readValue(
                    new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8),
                    new TypeReference<Map<String, Object>>() {});
            long exp = ((Number) payload.get("exp")).longValue();
            if (exp < System.currentTimeMillis()) {
                return null;
            }
            Long uid = payload.get("uid") == null ? null : ((Number) payload.get("uid")).longValue();
            String sub = (String) payload.get("sub");
            List<String> auth = ((List<?>) payload.get("auth")).stream().map(Object::toString).toList();
            return new Claims(uid, sub, auth);
        } catch (Exception e) {
            return null;
        }
    }

    private String json(Map<String, Object> map) {
        try {
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new IllegalStateException("JWT payload 序列化失败", e);
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return b64(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("JWT 签名失败", e);
        }
    }

    private String b64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /** 解析后的令牌内容。 */
    public record Claims(Long userId, String username, List<String> authorities) {
    }
}
