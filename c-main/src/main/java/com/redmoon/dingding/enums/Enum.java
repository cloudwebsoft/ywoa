package com.redmoon.dingding.enums;

public class Enum {
    public static final int ROOT_DEPT_ID = 1;
    public static final String INIT_PWD = "123";

    //订单状态
    public enum emErrorCode {
        ;
        public final static int emErrorException = -501;//程序异常
        public final static int emErrorUrlConnect = -500;//url请求问题
        public final static int emSuccess = 0;//请求成功
    }

    //关联绑定账号枚举
    public enum emBindAcc {
        ;
        public final static int emUserName = 1;
        public final static int emEmail = 2;
        public final static int emMobile = 3;
    }
}
