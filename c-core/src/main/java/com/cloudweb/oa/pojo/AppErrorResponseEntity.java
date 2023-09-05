package com.cloudweb.oa.pojo;

public class AppErrorResponseEntity {
    private int code;
    private String message;
    private Object data;
    private boolean more;
    private int ret;
    private String msg;

    public AppErrorResponseEntity(int code, String message, Object data) {
        this.code = code;
        this.ret = code;
        this.msg = message;
        this.message = message;
        this.data = data;
    }

    public AppErrorResponseEntity() {
        this.code = Status.SUCCESS.getCode();
        this.message = Status.SUCCESS.getStandardMessage();
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isMore() {
        return more;
    }

    public void setMore(boolean more) {
        this.more = more;
    }

    public static AppErrorResponseEntity init(int code, String message) {
        return new AppErrorResponseEntity(code, message, null);
    }

    public static AppErrorResponseEntity init(Object data) {
        return new AppErrorResponseEntity(Status.SUCCESS.getCode(), Status.SUCCESS.getStandardMessage(), data);
    }

    public static AppErrorResponseEntity init(Status status) {
        return new AppErrorResponseEntity(status.getCode(), status.getStandardMessage(), null);
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    // HttpStatus类里面都有
    public enum Status {
        /**
         * 200错误
         */
        SUCCESS(200, "OK"),
        BAD_REQUEST(400, "Bad Request"),
        NOT_FOUND(404, "Not Found"),
        INTERNAL_SERVER_ERROR(500, "Unknown Internal Error"),
        NOT_VALID_PARAM(40005, "Not valid Params"),
        NOT_SUPPORTED_OPERATION(40006, "Operation not supported"),
        NOT_LOGIN(50000, "Not Login"),
        PROTECT(-1000, "XSS CSRF or SQL Inj");

        private int code;
        private String standardMessage;

        Status(int code, String standardMessage) {
            this.code = code;
            this.standardMessage = standardMessage;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getStandardMessage() {
            return standardMessage;
        }

        public void setStandardMessage(String standardMessage) {
            this.standardMessage = standardMessage;
        }
    }
}