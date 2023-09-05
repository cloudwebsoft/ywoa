package com.redmoon.oa.flow.macroctl;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormMacroCtl;

public class MacroCtlUnit implements Serializable {
    public static int NEST_TYPE_NONE = 0;

    /**
     * 对应于macroType
     */
    public static final String NEST_SHEET = "nest_sheet";
    public static final String NEST_TABLE = "nest_table";
    public static final String NEST_DETAIL = "macro_detaillist_ctl";

    /**
     * 嵌套表单、嵌套表格2，似乎嵌套表格2应该2@task
     */
    public static int NEST_TYPE_NORMAIL = 1;
    
    /**
     * 嵌套表格
     */
    public static int NEST_TYPE_TABLE = 2;
    
    /**
     * 明细表
     */
    public static int NEST_DETAIL_LIST = 3;

    public MacroCtlUnit() {
    }

    public String getName(HttpServletRequest request) {
        return SkinUtil.LoadString(request, "res.config.score", code);
    }

    /**
     * 数据库中对应的字段类型
     */
    private String fieldType;

    public void renew() {
    }

    public MacroCtlUnit(String code) {
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public void setNestType(int nestType) {
        this.nestType = nestType;
    }

    public String getCode() {
        return code;
    }

    public String getAuthor() {
        return author;
    }

    public String getClassName() {
        return className;
    }

    public String getInputValue() {
        return inputValue;
    }

    public String getName() {
        return name;
    }

    public String getFieldType() {
        return fieldType;
    }

    public int getNestType() {
        return nestType;
    }

    public IFormMacroCtl getIFormMacroCtl() {
        IFormMacroCtl ipu = null;
        try {
            ipu = (IFormMacroCtl) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LogUtil.getLog(getClass()).error("getIFormMacroCtl: code=" + code + " className=" + className + " " + StrUtil.trace(e));
            LogUtil.getLog(getClass()).error(e);
        }

        return ipu;
    }

    public void setDisplay(boolean display) {
		this.display = display;
	}

	public boolean isDisplay() {
		return display;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	public float getVersion() {
		return version;
	}

	public void setForm(boolean form) {
		this.form = form;
	}

	public boolean isForm() {
		return form;
	}

	public void setFormCode(String formCode) {
		this.formCode = formCode;
	}

	public String getFormCode() {
		return formCode;
	}

	private String code;
    private String author;
    private String className;
    private String inputValue;
    private String name;
    private int nestType = 0;
    private boolean display = true;
    
    private float version = 1;
    
    private boolean form = false;
    private String formCode = "";

}
