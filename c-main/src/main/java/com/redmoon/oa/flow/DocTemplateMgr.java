package com.redmoon.oa.flow;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.pvg.Privilege;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Vector;

public class DocTemplateMgr {
    public DocTemplateMgr() {
    }
    
    /**
     * 判断模板对用户是否可见
     * @param request
     * @param dtd
     * @return
     */
    public boolean canUserSee(HttpServletRequest request, DocTemplateDb dtd) {
    	// DocTemplateDb dtd = getDocTemplateDb(templateId);
    	String depts = dtd.getDepts();
    	if ("".equals(depts)) {
            return true;
        }
    	DeptUserDb dud = new DeptUserDb();
    	Privilege pvg = new Privilege();
    	if (pvg.isUserPrivValid(request, "admin")) {
    		return true;
    	}
    	Vector<DeptDb> v = dud.getDeptsOfUser(pvg.getUser(request));
    	String[] ary = StrUtil.split(depts, ",");
        for (String dept : ary) {
            for (DeptDb dd : v) {
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
        return ld.create(lf.fileUpload);
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

        return ld.save(lf.getFileUpload());
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

        return ld.save(lf.getFileUpload());
    }

    public DocTemplateDb getDocTemplateDb(int id) {
        DocTemplateDb ld = new DocTemplateDb();
        return ld.getDocTemplateDb(id);
    }

}
