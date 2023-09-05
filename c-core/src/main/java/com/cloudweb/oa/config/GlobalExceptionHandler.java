package com.cloudweb.oa.config;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.pojo.ErrorResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseEntity ValidationExceptionHandle(ValidationException exception) {
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        if(exception instanceof ConstraintViolationException){
            ConstraintViolationException exs = (ConstraintViolationException) exception;
            StringBuffer errorMsg = new StringBuffer();
            Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
            for (ConstraintViolation<?> item : violations) {
                // 打印验证不通过的信息
                errorMsg.append(item.getMessage() + "\r\n");
            }

            log.error("uuid: " + uuid + " ---ConstraintViolationException Handler--- ERROR: {}", errorMsg.toString());
            ErrorResponseEntity result = new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, errorMsg.toString(), uuid);
            return result;
        }
        else {
            log.error("uuid: " + uuid + " ---ConstraintViolationException Handler--- ERROR: {}", exception.getMessage());
            ErrorResponseEntity result = new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, exception.getMessage(), uuid);
            return result;
        }
    }

    // 用于spring boot内置的hibernate-validator
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ErrorResponseEntity methodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        StringBuffer errorMsg = new StringBuffer();
        errors.stream().forEach(x -> errorMsg.append(x.getDefaultMessage()).append(";"));

        String uuid = UUID.randomUUID().toString().replaceAll("-","");

        log.error("uuid: " + uuid + " ---MethodArgumentNotValidException Handler--- ERROR: {}", errorMsg.toString());
        ErrorResponseEntity result = new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, errorMsg.toString(), uuid);
        result.setMsg(errorMsg.toString());
        return result;
    }

    /**
     * 定义要捕获的异常 可以多个 @ExceptionHandler({})
     *
     * @return 响应结果
     */
    @ExceptionHandler(ErrMsgException.class)
    public ErrorResponseEntity ErrMsgExceptionHandler(ErrMsgException ex) {
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        return new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, ex.getMessage(), uuid);
    }

    @ExceptionHandler(ValidateException.class)
    public ErrorResponseEntity ValidateExceptionHandler(ValidateException ex) {
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        return new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, ex.getMessage(), uuid);
    }

    /**
     * 如果参数类型非法，则会进入该方法
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ErrorResponseEntity exceptionHandler(Exception ex) {
        if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException e = (MethodArgumentTypeMismatchException)ex;
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            log.error("uuid: " + uuid + " msg: " + e.getName() + ": " + e.getParameter());
            log.error(StrUtil.trace(ex));
            return new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, e.getName() + ":" + ex.getMessage(), uuid);
        }
        /*else if (ex instanceof AccessDeniedException) {
            // @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")当无权限时，会报：不允许访问，但Spring Security 中无法找到导致该出错的原因，
            AccessDeniedException e = (AccessDeniedException)ex;
            log.error(e.getMessage() + ": " + e.getLocalizedMessage());
            log.error(StrUtil.trace(ex));
            return new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, ex.getMessage());
        }*/
        else {
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            log.error("uuid: " + uuid + " msg: " + StrUtil.trace(ex));
            return new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, ex.getMessage(), uuid);
        }
    }

}