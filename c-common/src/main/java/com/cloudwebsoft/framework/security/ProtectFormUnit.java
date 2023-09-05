package com.cloudwebsoft.framework.security;

import java.io.Serializable;
import java.util.List;

public class ProtectFormUnit implements Serializable {
    public ProtectFormUnit() {
    }

    public void renew() {
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    /**
     * 需排除的表单编码
     */
    private String formCode;

    /**
     * 需排除的字段，在配置文件中以半角逗号分隔
     */
    private List<String> fields;

    /**
     * 类型，包含或正则
     */
    private int type;
}
