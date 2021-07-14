package com.cloudweb.oa.config;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.pojo.ErrorResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseEntity ValidationExceptionHandle(ValidationException exception) {
        if(exception instanceof ConstraintViolationException){
            ConstraintViolationException exs = (ConstraintViolationException) exception;
            StringBuffer errorMsg = new StringBuffer();
            Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
            for (ConstraintViolation<?> item : violations) {
                // 打印验证不通过的信息
                // System.out.println(item.getMessage());
                errorMsg.append(item.getMessage() + "\r\n");
            }

            log.error("---ConstraintViolationException Handler--- ERROR: {}", errorMsg.toString());

            ErrorResponseEntity result = new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, errorMsg.toString());
            return result;
        }
        else {
            log.error("---ConstraintViolationException Handler--- ERROR: {}", exception.getMessage());
            ErrorResponseEntity result = new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, exception.getMessage());
            return result;
        }
    }

    // 用于spring boot内置的hibernate-validator
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ErrorResponseEntity methodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        StringBuffer errorMsg = new StringBuffer();
        errors.stream().forEach(x -> errorMsg.append(x.getDefaultMessage()).append(";"));

        log.error("---MethodArgumentNotValidException Handler--- ERROR: {}", errorMsg.toString());
        ErrorResponseEntity result = new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, errorMsg.toString());
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
        return new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, ex.getMessage());
    }

    @ExceptionHandler(ValidateException.class)
    public ErrorResponseEntity ValidateExceptionHandler(ValidateException ex) {
        return new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponseEntity exceptionHandler(Exception ex) {
        log.error(StrUtil.trace(ex));
        return new ErrorResponseEntity(ErrorResponseEntity.CODE_FAIL, ex.getMessage());
    }

}