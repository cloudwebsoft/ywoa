package com.cloudweb.oa.vo;

import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 接口返回数据格式
 */
@ApiModel(value = "接口返回对象", description = "接口返回对象")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回处理消息
     */
    @ApiModelProperty(value = "返回处理消息")
    private String msg;

    /**
     * 返回代码
     */
    @ApiModelProperty(value = "返回代码")
    private Integer code = CommonConstant.SC_OK_200;

    /**
     * 返回数据对象 data
     */
    @ApiModelProperty(value = "返回数据对象")
    private T data;

    /**
     * 时间戳
     */
    @ApiModelProperty(value = "时间戳")
    private long timestamp = System.currentTimeMillis();

    public Result() {
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        this.msg = i18nUtil.get("info_op_success");
    }

    public Result(boolean re) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        if (re) {
            this.msg = i18nUtil.get("info_op_success");
            json.put("res", 0);
        } else {
            this.msg = i18nUtil.get("info_op_fail");
            this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
            json.put("res", 1);
        }
        data = (T) json;
    }

    public Result(boolean re, String msg) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        if (re) {
            this.msg = msg;
            json.put("res", 0);
        } else {
            this.msg = msg;
            this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
            json.put("res", 1);
        }
        data = (T) json;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Result(T data) {
        this.data = data;
        if (data instanceof JSONObject) {
            JSONObject json = (JSONObject)data;
            if (!json.containsKey("res")) {
                json.put("res", 0);
            }
        }
    }

    public void error() {
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        this.msg = i18nUtil.get("info_op_fail");
        this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
    }

    public void setResult(boolean re) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        if (re) {
            this.msg = i18nUtil.get("info_op_success");
            json.put("res", 0);
        } else {
            this.msg = i18nUtil.get("info_op_fail");
            this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
            json.put("res", 1);
        }
        data = (T) json;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void error(String msg) {
        error500(msg);
    }

    public void error500(String msg) {
        this.msg = msg;
        this.code = CommonConstant.SC_INTERNAL_SERVER_ERROR_500;
    }

    public void error(Integer code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    public void success() {
        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        this.msg = i18nUtil.get("info_op_success");
    }

    public void success(T data) {
        this.data = data;

        I18nUtil i18nUtil = SpringUtil.getBean(I18nUtil.class);
        msg = i18nUtil.get("info_op_success");
    }

    public void success(T data, String msg) {
        this.data = data;
        this.msg = msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 无权限访问返回结果
     */
    public void unAuth(String msg) {
        error(CommonConstant.SC_UN_AUTHZ, msg);
    }
}