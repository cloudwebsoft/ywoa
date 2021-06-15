package com.redmoon.dingding.util;

public class DdException extends Exception {

    public static final int ERR_RESULT_RESOLUTION = -2;

    public DdException(String field) {
        this(ERR_RESULT_RESOLUTION, "Cannot resolve field " + field + " from oapi resonpse");
    }

	public DdException(int errCode, String errMsg) {
		super("error code: " + errCode + ", error message: " + errMsg);
	}

	public DdException(int errCode, String errMsg, String url, String postData) {
        super("error code: " + errCode + ", error message: " + errMsg + " url: " + url + " postData: " + postData);
    }
}
