package com.yohm.springcloud.file.vo;

/**
 * 功能简述
 * (返回前端的Json对象)
 *
 * @author 海冰
 * @create 2019-04-14
 * @since 1.0.0
 */
public class JsonResponse<T> {

    private Integer code;
    private String message;
    private T result;

    public JsonResponse(){}

    public JsonResponse(Integer code) {
        this(code,null,null);
    }

    public JsonResponse(Integer code, String message) {
        this(code,message,null);
    }

    public JsonResponse(Integer code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    @Override
    public String toString() {
        return "JsonResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
