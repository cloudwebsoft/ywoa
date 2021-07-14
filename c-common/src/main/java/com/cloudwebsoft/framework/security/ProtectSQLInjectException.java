package com.cloudwebsoft.framework.security;

public class ProtectSQLInjectException extends Exception {
	String param;
	String value;
	
    public ProtectSQLInjectException(String param, String value) {
    	this.param = param;
    	this.value = value;
    }
    
    public String getParam() {
    	return param;
    }
    
    public String getValue() {
    	return value;
    }
}
