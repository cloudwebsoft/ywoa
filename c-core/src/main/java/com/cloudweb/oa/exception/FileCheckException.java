package com.cloudweb.oa.exception;

/**
 * @author litb
 * @date 2021/03/11 15:16
 * @description 自定义异常
 */
public class FileCheckException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileCheckException() {
        super();
    }

    public FileCheckException(String message) {
        super(message);
    }

    public FileCheckException(Throwable cause) {
        super(cause);
    }

    public FileCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}