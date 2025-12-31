package org.example.project1.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * TODO
 *
 * @Author djy
 * @Date 2025/12/30 11:57
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化时忽略 null 字段
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：200 成功，其他为异常 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 返回数据（可为对象、列表、分页等） */
    private T data;

    // ========== 私有构造器 ==========
    private Result() {}

    // ========== 成功 ==========
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = message;
        result.data = data;
        return result;
    }

    // ========== 失败 ==========
    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.code = 500;
        result.message = "操作失败";
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = fail();
        result.message = message;
        return result;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }
}