package com.sme.ledger.security;

import com.sme.ledger.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 权限校验拦截器：对标注 {@link RequirePermission} 的方法做细粒度校验。
 * 当前用户权限由 {@link JwtAuthFilter} 写入请求属性 authorities。
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequirePermission require = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (require == null) {
            return true;
        }
        Set<String> authorities = (Set<String>) request.getAttribute("authorities");
        if (authorities == null || !authorities.contains(require.value())) {
            throw new BusinessException(403, "无此操作权限，请联系管理员");
        }
        return true;
    }
}
