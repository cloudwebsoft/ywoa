package com.redmoon.oa.flow;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.pvg.Privilege;

public class DocTemplateMgr {
    public DocTemplateMgr() {
    }
    
    /**
     * 判断模板对用户是否可见
     * @param request
     * @param templateId
     * @return
     */
    public boolean canUserSee(HttpServletRequest request, DocTemplateDb dtd) {
    	// DocTemplateDb dtd = getDocTemplateDb(templateId);
    	String depts = dtd.getDepts();
    	if (depts.equals(""))
    		return true;
    	DeptUserDb dud = new DeptUserDb();
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(request, "admin")) {
    		return true;
    	}
    	Vector v = dud.getDeptsOfUser(pvg.getUser(request));
    	String[] ary = StrUtil.split(depts, ",");
    	for (int i=0; i<ary.length; i++) {
    		String dept = ary[i];
    		Iterator ir = v.iterator();
    		while (ir.hasNext()) {
    			DeptDb dd = (DeptDb)ir.next();
    			if (dept.equals(dd.getCode())) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

    public boolean add(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        DocTemplateForm lf = new DocTemplateForm();
        lf.checkAdd(application, request);

        DocTemplateDb ld = new DocTemplateDb();
        ld.setTitle(lf.getTitle());
        ld.setSort(lf.getSort());
        ld.setDepts(lf.getDepts());
        ld.setUnitCode(lf.getUnitCode());
        boolean re = ld.create(lf.fileUpload);
        return re;
    }

    public boolean del(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        DocTemplateForm lf = new DocTemplateForm();
        lf.checkDel(request);

        DocTemplateDb ld = new DocTemplateDb();
        ld = ld.getDocTemplateDb(lf.getId());
        return ld.del(new JdbcTemplate());
    }

    public boolean modify(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        DocTemplateForm lf = new DocTemplateForm();
        lf.checkModify(application, request);

        DocTemplateDb ld = new DocTemplateDb();
        ld = ld.getDocTemplateDb(lf.getId());
        ld.setTitle(lf.getTitle());
        ld.setSort(lf.getSort());
        ld.setDepts(lf.getDepts());

        boolean re = ld.save(lf.getFileUpload());
        return re;
    }

    /**
     * 用于编辑word文件
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean modifyByWeboffice(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        DocTemplateForm lf = new DocTemplateForm();
        lf.checkModifyByWeboffice(application, request);

        DocTemplateDb ld = new DocTemplateDb();
        ld = ld.getDocTemplateDb(lf.getId());
        // ld.setTitle(lf.getTitle());
        ld.setSort(lf.getSort());

        boolean re = ld.save(lf.getFileUpload());
        return re;
    }

    public DocTemplateDb getDocTemplateDb(int id) {
        DocTemplateDb ld = new DocTemplateDb();
        return ld.getDocTemplateDb(id);
    }

}
