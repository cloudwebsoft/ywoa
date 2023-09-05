package com.redmoon.oa.flow;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.AbstractForm;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.web.Global;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;

public class DocTemplateForm extends AbstractForm {
    public FileUpload fileUpload;

    public DocTemplateForm() {
    }

    public FileUpload doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"doc", "docx", "wps"};
        fileUpload.setValidExtname(extnames);//设置可上传的文件类型

        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != fileUpload.RET_SUCCESS) {
                throw new ErrMsgException("ret=" + ret + " " + fileUpload.getErrMessage(request));
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public int getSort() {
        return sort;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public String chkTitle() {
        title = fileUpload.getFieldValue("title");
        if (title==null || title.equals("")) {
            log("名称必须填写！");
        }
        return title;
    }
    
    public String chkUnitCode() {
        unitCode = fileUpload.getFieldValue("unitCode");
        return unitCode;
    }    

    public int chkId(HttpServletRequest request) {
        try {
            id = ParamUtil.getInt(request, "id");
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return id;
    }

    public int chkId() {
        try {
            id = Integer.parseInt(fileUpload.getFieldValue("id"));
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return id;
    }

    public int chkSort() {
        sort = StrUtil.toInt(fileUpload.getFieldValue("sort"), 0);
        return sort;
    }
    
    public String chkDepts() {
    	depts = fileUpload.getFieldValue("depts");
    	return depts;
    }

    public boolean checkAdd(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        init();
        doUpload(application, request);
        chkTitle();
        chkSort();
        chkDepts();
        chkUnitCode();
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        report();
        return true;
    }

    public boolean checkModify(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        init();
        doUpload(application, request);
        chkTitle();
        chkSort();
        chkId();
        chkDepts();
        report();
        return true;
    }

    public boolean checkModifyByWeboffice(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        init();
        doUpload(application, request);
        // chkTitle();
        chkSort();
        chkId();
        report();
        return true;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public void setDepts(String depts) {
		this.depts = depts;
	}

	public String getDepts() {
		return depts;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public String getUnitCode() {
		return unitCode;
	}

	private int sort;
    private String title;
    private int id;
    
    private String depts;
    
    private String unitCode;
}
