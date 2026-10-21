package com.sme.ledger.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;

/**
 * JWT 认证过滤器：解析 Authorization 头中的 Bearer token，
 * 将用户信息与权限写入请求属性（供 {@link PermissionInterceptor} 使用）并建立安全上下文。
 * 注意：本类不注册为 Bean，由 SecurityConfig 直接实例化加入过滤器链，避免重复注册。
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            JwtUtil.Claims claims = jwtUtil.parse(header.substring(7));
            if (claims != null) {
                request.setAttribute("userId", claims.userId());
                request.setAttribute("username", claims.username());
                request.setAttribute("authorities", new HashSet<>(claims.authorities()));

                var auth = new UsernamePasswordAuthenticationToken(
                        claims.username(), null,
                        claims.authorities().stream().map(SimpleGrantedAuthority::new).toList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
