package com.cloudwebsoft.framework.template.plugin;

import java.io.Serializable;
import com.cloudwebsoft.framework.template.ITemplate;
import com.cloudwebsoft.framework.util.LogUtil;

public class PluginUnit implements Serializable {

    public PluginUnit() {
    }

    public void renew() {
    }

    public PluginUnit(String code) {
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setClassUnit(String classUnit) {
        this.classUnit = classUnit;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getClassUnit() {
        return classUnit;
    }

    public String getDesc() {
        return desc;
    }

    public ITemplate getITemplate() {
        ITemplate ipu = null;
        try {
            ipu = (ITemplate) Class.forName(classUnit).newInstance();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getITemplate:" + e.getMessage());
        }
        return ipu;
    }

    private String code;
    private String classUnit;
    private String desc;
}
