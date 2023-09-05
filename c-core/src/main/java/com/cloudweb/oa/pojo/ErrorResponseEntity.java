package com.cloudweb.oa.pojo;

import lombok.Data;

/**
 * @author fgf
 */
@Data
public class ErrorResponseEntity {
    public static final int CODE_FAIL = 500;

    int ret = 0;

    public ErrorResponseEntity(int code, String msg, String uuid) {
        this.code = code;
        this.msg = msg;
        this.uuid = uuid;
    }

    private int code;
    private String msg;
    private String uuid;
}
