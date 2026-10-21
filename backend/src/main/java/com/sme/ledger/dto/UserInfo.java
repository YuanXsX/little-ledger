package com.sme.ledger.dto;

import java.util.List;

/**
 * 当前登录用户信息：角色名列表（用于展示）与权限键列表（用于前端菜单/按钮控制）。
 */
public record UserInfo(
        String username,
        String realName,
        List<String> roles,
        List<String> permissions) {
}
