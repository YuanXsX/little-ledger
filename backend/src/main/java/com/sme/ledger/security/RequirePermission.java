package com.sme.ledger.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级权限校验注解：标注后由 {@link PermissionInterceptor} 校验当前用户是否拥有该权限。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /** 所需权限键，如 finance:delete、report:export。 */
    String value();
}
