package io.github.uncleacc.yunlog.exception;

import io.github.uncleacc.yunlog.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Object> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 参数异常处理（IllegalArgumentException）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常：{}", e.getMessage());
        return ApiResponse.badRequest(e.getMessage());
    }
    
    /**
     * 参数验证异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("参数验证失败：{}", e.getMessage());
        
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        return ApiResponse.badRequest("参数验证失败").setData(errors);
    }
    
    /**
     * 约束违反异常处理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束验证失败：{}", e.getMessage());
        
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        Map<String, String> errors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        }
        
        return ApiResponse.badRequest("参数验证失败").setData(errors);
    }
    
    /**
     * 其他异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleException(Exception e) {
        log.error("系统异常：", e);
        return ApiResponse.error("系统内部错误");
    }
}