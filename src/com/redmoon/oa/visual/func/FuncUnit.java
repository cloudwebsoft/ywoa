package com.redmoon.oa.visual.func;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.base.IFuncImpl;

public class FuncUnit implements Serializable {

    public FuncUnit() {
    }

    public void renew() {
    }

    public FuncUnit(String code) {
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

    public void setName(String name) {
        this.name = name;
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

    public String getName() {
        return name;
    }

    public IFuncImpl getIFuncImpl() {
    	IFuncImpl ifu = null;
        try {
            ifu = (IFuncImpl) Class.forName(className).newInstance();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getIFuncImpl: code=" + code + " className=" + className + " " + StrUtil.trace(e));
        }
        return ifu;
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

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

	private String code;
    private String author;
    private String className;
    private String data;
    private String name;
    private boolean display = true;
    
    private float version = 1;
    

}
