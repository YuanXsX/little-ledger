package com.sme.ledger.config;

import com.sme.ledger.security.PermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册权限校验拦截器（仅对标注 @RequirePermission 的方法生效）。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;

    public WebConfig(PermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/**");
    }
}
