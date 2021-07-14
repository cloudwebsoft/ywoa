package com.redmoon.oa.flow.macroctl.domain;

/**
 * 嵌套表源表字段与嵌套表字段映射关系类
 *
 * @author jfy
 * @date Apr 7, 2015
 */
public class NestFieldMaping {
    private String sourceFieldCode;
    private String destFieldCode;
    private String sourceFieldName;
    private String destFieldName;

    private boolean realTime;

    public String getSourceFieldCode() {
        return sourceFieldCode;
    }

    public void setSourceFieldCode(String sourceFieldCode) {
        this.sourceFieldCode = sourceFieldCode;
    }

    public String getDestFieldCode() {
        return destFieldCode;
    }

    public void setDestFieldCode(String destFieldCode) {
        this.destFieldCode = destFieldCode;
    }

    public String getSourceFieldName() {
        return sourceFieldName;
    }

    public void setSourceFieldName(String sourceFieldName) {
        this.sourceFieldName = sourceFieldName;
    }

    public String getDestFieldName() {
        return destFieldName;
    }

    public void setDestFieldName(String destFieldName) {
        this.destFieldName = destFieldName;
    }

	public boolean isRealTime() {
		return realTime;
	}

	public void setRealTime(boolean realTime) {
		this.realTime = realTime;
	}
}
