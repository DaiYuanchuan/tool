package cn.novelweb.tool.http;

import cn.novelweb.config.ConstantConfiguration;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>状态返回类</p>
 * 2019-10-30 02:00
 *
 * @author Dai Yuanchuan
 **/
@ApiModel(value = "返回信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    @ApiModelProperty(value = "状态码")
    private String code;

    @ApiModelProperty(value = "描述")
    private String message;

    @ApiModelProperty(value = "对象")
    private T data;


    private Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private Result(String code, String message, T obj) {
        this.code = code;
        this.message = message;
        this.data = obj;
    }

    public static Result ok(Object data) {
        return new Result<>(ConstantConfiguration.success, "请求成功", data);
    }

    public static Result ok() {
        return new Result<>(ConstantConfiguration.success, "请求成功", "success");
    }

    public static Result ok(String code, String message) {
        return new Result(code, message);
    }

    public static Result ok(int code, String message) {
        return new Result(String.valueOf(code), message);
    }

    public static Result ok(String code, String message, Object data) {
        return new Result<>(code, message, data);
    }

    public static Result ok(int code, String message, Object data) {
        return new Result<>(String.valueOf(code), message, data);
    }

    public static Result fail(Object data) {
        return new Result<>(ConstantConfiguration.fail, "请求失败", data);
    }

    public static Result fail() {
        return new Result<>(ConstantConfiguration.fail, "请求失败", "");
    }

    public static Result fail(String message) {
        return new Result<>(ConstantConfiguration.fail, message, "");
    }

    public static Result fail(String code, String message) {
        return new Result<>(code, message);
    }

    public static Result fail(int code, String message) {
        return new Result<>(String.valueOf(code), message);
    }

    public static Result fail(String code, String message, Object data) {
        return new Result<>(code, message, data);
    }

    public static Result fail(int code, String message, Object data) {
        return new Result<>(String.valueOf(code), message, data);
    }

    public static Result authority(String message) {
        return new Result<>(ConstantConfiguration.noAuthority, message, "");
    }

    public static Result authority() {
        return new Result<>(ConstantConfiguration.noAuthority, "抱歉！您没有对应的权限", "");
    }

    public static Result refuse() {
        return new Result<>(ConstantConfiguration.refuse, "登录过期！请重新登录", "");
    }

    public static Result refuse(String message) {
        return new Result<>(ConstantConfiguration.refuse, message, "");
    }

    public static Result error(String message) {
        return new Result<>(ConstantConfiguration.systemError, message, "");
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
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
