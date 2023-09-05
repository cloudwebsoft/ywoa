package com.cloudweb.oa.vo;

public interface CommonConstant {

	/** {@code 500 Server Error} (HTTP/1.0 - RFC 1945) */
    public static final Integer SC_INTERNAL_SERVER_ERROR_500 = 500;

    /** {@code 200 OK} (HTTP/1.0 - RFC 1945) */
    public static final Integer SC_OK_200 = 200;

    /**
     * 访问权限认证未通过
     */
    public static final Integer SC_UN_AUTHZ=401;

}
