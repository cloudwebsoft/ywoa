package com.redmoon.oa.person;

public class WrongPasswordException extends Exception {
    public WrongPasswordException(String msg) {
        super(msg);
    }

    public WrongPasswordException() {
      super("密码错误，请检查大小写或长度是否有误！");
    }
}
