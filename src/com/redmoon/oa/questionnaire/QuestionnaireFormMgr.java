package com.redmoon.oa.questionnaire;

import org.apache.log4j.Logger;

import com.redmoon.oa.workplan.Privilege;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import java.util.Vector;
import java.util.Iterator;

public class QuestionnaireFormMgr {
    Logger logger = Logger.getLogger(QuestionnaireFormMgr.class.getName());

    public QuestionnaireFormMgr() {
    }

    public int create(HttpServletRequest request) throws ErrMsgException {
        int flag = -1;

        String errmsg = "";
        String formName = ParamUtil.get(request, "form_name");
        String description = ParamUtil.get(request, "description");
        int isOpen = ParamUtil.getInt(request, "isOpen");
        if (formName.equals("")) {
            errmsg += "名称不能为空！\\n";
        }
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
        
        java.util.Date beginDate = DateUtil.parse(ParamUtil.get(request, "beginDate"), "yyyy-MM-dd");
        java.util.Date endDate = DateUtil.parse(ParamUtil.get(request, "endDate"), "yyyy-MM-dd");
        if (beginDate==null)
        	throw new ErrMsgException("开始时间格式错误!");
        if (endDate==null)
        	throw new ErrMsgException("结束时间格式错误!");
        
        Privilege pvg = new Privilege();
        String unitCode = pvg.getUserUnitCode(request);
        
        int isPublic = ParamUtil.getInt(request, "isPublic", 0);
        
        QuestionnaireFormDb qfd = new QuestionnaireFormDb();

        qfd.setFormName(formName);
        qfd.setDescription(description);
        qfd.setOpen(isOpen==1);
        qfd.setUnitCode(unitCode);
        qfd.setBeginDate(beginDate);
        qfd.setEndDate(endDate);
        qfd.setPublic(isPublic==1);
        flag = qfd.create() ? qfd.getFormId() : -1;
        return flag;
    }

    public QuestionnaireFormDb getQuestionnaireFormDb(int formId) {
            QuestionnaireFormDb qfd = new QuestionnaireFormDb();
            return qfd.getQuestionnaireFormDb(formId);
    }

    public boolean save(HttpServletRequest request) throws ErrMsgException {
        boolean re = false;

        String errmsg = "";

        int formId = ParamUtil.getInt(request, "form_id");
        String formName = ParamUtil.get(request, "form_name");
        String description = ParamUtil.get(request, "description");
        int isOpen = ParamUtil.getInt(request, "isOpen");

        if (formName.equals("")) {
            errmsg += "名称不能为空！\\n";
        }
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }

        java.util.Date beginDate = DateUtil.parse(ParamUtil.get(request, "beginDate"), "yyyy-MM-dd");
        java.util.Date endDate = DateUtil.parse(ParamUtil.get(request, "endDate"), "yyyy-MM-dd");
        if (beginDate==null)
        	throw new ErrMsgException("开始时间格式错误!");
        if (endDate==null)
        	throw new ErrMsgException("结束时间格式错误!"); 
        
        int isPublic = ParamUtil.getInt(request, "isPublic", 0);
        
        QuestionnaireFormDb qfd = getQuestionnaireFormDb(formId);

        qfd.setFormName(formName);
        qfd.setDescription(description);
        qfd.setOpen(isOpen==1);
        qfd.setBeginDate(beginDate);
        qfd.setEndDate(endDate);
        qfd.setPublic(isPublic==1);
        re = qfd.save();
        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        boolean re = false;
        int formId = ParamUtil.getInt(request, "form_id");
        QuestionnaireFormDb qfd = getQuestionnaireFormDb(formId);
        if (qfd==null || !qfd.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }
        re = qfd.del();
        /* 删除表单项目 */
        String sql = "select item_id from oa_questionnaire_form_item where form_id=" + formId;
        QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
        Vector vItems = qfid.list(sql);
        Iterator iItems = vItems.iterator();
        while(iItems.hasNext()) {
            qfid = (QuestionnaireFormItemDb)iItems.next();
            re = qfid.del();
        }
        /* 删除表单数据 */
        sql = "select id from oa_questionnaire_item where form_id=" + formId;
        QuestionnaireItemDb qid = new QuestionnaireItemDb();
        vItems = qid.list(sql);
        iItems = vItems.iterator();
        while(iItems.hasNext()) {
            qid = (QuestionnaireItemDb)iItems.next();
            re = qid.del();
        }
        sql = "select subitem_id from oa_questionnaire_subitem where questionnaire_form_id=" + formId;
        QuestionnaireSubitemDb qsd = new QuestionnaireSubitemDb();
        vItems = qsd.list(sql);
        iItems = vItems.iterator();
        while(iItems.hasNext()) {
            qsd = (QuestionnaireSubitemDb)iItems.next();
            re = qsd.del();
        }
        return re;
    }

}
