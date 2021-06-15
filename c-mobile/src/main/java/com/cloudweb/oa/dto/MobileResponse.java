package com.cloudweb.oa.dto;

import lombok.Data;

@Data
public class MobileResponse<T> {
    private int res;
    private String msg;
    private T result;

    public MobileResponse(T result) {
        this.result = result;
        this.res = 0;
        this.msg = "操作成功";
    }
}
