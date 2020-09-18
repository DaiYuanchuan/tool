package cn.novelweb.tool.http;

import cn.novelweb.config.ConstantConfiguration;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Optional;

/**
 * <p>状态返回类</p>
 * 2019-10-30 02:00
 *
 * @author Dai Yuanchuan
 **/
@ApiModel(value = "返回信息")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

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

    /**
     * 判断返回是否为成功
     *
     * @param result Result
     * @return 是否成功
     */
    public static boolean isSuccess(@Nullable Result<?> result) {
        return Optional.ofNullable(result)
                .map(x -> ObjectUtils.nullSafeEquals(ConstantConfiguration.success, x.code))
                .orElse(Boolean.FALSE);
    }

    /**
     * 判断返回是否为成功
     *
     * @param result Result
     * @return 是否成功
     */
    public static boolean isNotSuccess(@Nullable Result<?> result) {
        return !Result.isSuccess(result);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ConstantConfiguration.success, "请求成功", data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(ConstantConfiguration.success, "请求成功");
    }

    public static <T> Result<T> ok(String code, String message) {
        return new Result<>(code, message);
    }

    public static <T> Result<T> ok(int code, String message) {
        return new Result<>(String.valueOf(code), message);
    }

    public static <T> Result<T> ok(String code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> ok(int code, String message, T data) {
        return new Result<>(String.valueOf(code), message, data);
    }

    public static <T> Result<T> fail(T data) {
        return new Result<>(ConstantConfiguration.fail, "请求失败", data);
    }

    public static <T> Result<T> fail() {
        return new Result<>(ConstantConfiguration.fail, "请求失败", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ConstantConfiguration.fail, message, null);
    }

    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(code, message);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(String.valueOf(code), message);
    }

    public static <T> Result<T> fail(String code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(String.valueOf(code), message, data);
    }

    public static <T> Result<T> authority(String message) {
        return new Result<>(ConstantConfiguration.noAuthority, message, null);
    }

    public static <T> Result<T> authority() {
        return new Result<>(ConstantConfiguration.noAuthority, "抱歉！您没有对应的权限", null);
    }

    public static <T> Result<T> refuse() {
        return new Result<>(ConstantConfiguration.refuse, "登录过期！请重新登录", null);
    }

    public static <T> Result<T> refuse(String message) {
        return new Result<>(ConstantConfiguration.refuse, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ConstantConfiguration.systemError, message, null);
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
