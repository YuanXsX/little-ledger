package com.sme.ledger.common;

/**
 * 统一接口返回结构。
 *
 * @param <T> 业务数据类型
 */
public class ApiResult<T> {

    /** 状态码：200 成功，其余为业务/系统错误 */
    private int code;
    private String message;
    private T data;

    public ApiResult() {
    }

    public ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(200, "success", data);
    }

    public static <T> ApiResult<T> ok() {
        return ok(null);
    }

    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
