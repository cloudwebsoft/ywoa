package com.redmoon.oa.alicom_dysms;

public abstract  class AbDySmsApi {
    protected String accessKeyId;
    protected String accessKeySecret;
    protected String signName;
    protected String templateCode;
    protected String code;
    protected String mobile;
    public  int smsExpired;
    //产品名称:云通信短信API产品,开发者无需替换
    public final static  String PRODUCT ="Dysmsapi";
    //产品域名,开发者无需替换
    public final static String DOMAIN = "dysmsapi.aliyuncs.com";
    private Config config;
    public abstract boolean sendMsg();
    public AbDySmsApi(){
        config = Config.getInstance();
        accessKeyId = config.getProperty("accessKeyId");
        accessKeySecret = config.getProperty("accessKeySecret");
        signName = config.getProperty("signName");
        templateCode  = config.getProperty("templateCode");
        smsExpired = config.getIntProperty("smsExpired");
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public boolean isUseAliDysms(){
        return  config.isUseAliDysms();
    }
}
