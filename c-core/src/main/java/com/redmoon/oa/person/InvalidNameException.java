package com.redmoon.oa.person;

public class InvalidNameException extends Exception {
    public InvalidNameException() {
        super("用户名非法");
    }
}
