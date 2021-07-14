package com.cloudwebsoft.framework.security;

public class ProtectXSSException extends Exception {
	String param;
	String value;
	
    public ProtectXSSException(String param, String value) {
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