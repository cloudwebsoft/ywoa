package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.util.NetUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.visual.*;
import com.redmoon.oa.visual.FormDAO;

/**
 * <p>Title: 嵌套表单</p>
 *
 * <p>Description: 注意不能重复嵌套多个同一种表单，另外字段编码也不能与父表单重复</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class NestFormCtl extends AbstractMacroCtl {
    public NestFormCtl() {
        super();
    }

    /**
     * 通过NetUtil.gather方法取得控件的HTML代码
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     */
    public String getNestForm(HttpServletRequest request, FormField ff) {
        String cwsId = (String) request.getAttribute("cwsId");
        if (cwsId == null) {
            cwsId = "";
        }

        String op;
        String pageType = StrUtil.getNullStr((String) request.getAttribute(
                "pageType"));
        if (pageType.equals("show")) // module_show.jsp
            op = "view";
		else if ("flowShow".equals(pageType)) { // flow_modify.jsp
            int flowId = StrUtil.toInt(cwsId);
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Directory dir = new Directory();
            Leaf lf = dir.getLeaf(wf.getTypeCode());
            
            FormDb flowFd = new FormDb();
            flowFd = flowFd.getFormDb(lf.getFormCode());
            com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
            fdao = fdao.getFormDAO(flowId, flowFd);
            cwsId = String.valueOf(fdao.getId());
			op = "view";
		}
        else if (pageType.equals("add")) // module_add.jsp
            op = "add";
        else if (pageType.equals("flow")) { // flow_dispose.jsp中pageType=flow
            /*
        	int flowId = StrUtil.toInt(cwsId);
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Directory dir = new Directory();
            Leaf lf = dir.getLeaf(wf.getTypeCode());
            
            FormDb flowFd = new FormDb();
            flowFd = flowFd.getFormDb(lf.getFormCode());
            com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
            fdao = fdao.getFormDAO(flowId, flowFd);
            cwsId = String.valueOf(fdao.getId());
            */
            
            op = "flow";
            String workflowActionId = (String)request.getAttribute("workflowActionId");
            op += "&workflowActionId=" + workflowActionId;
        }
        else // module_edit.jsp中pageType=edit
            op = "edit";

        String path = "http://" + request.getServerName() + ":" +
                      request.getServerPort() + request.getContextPath() +
                      "/visual/nest_form_view.jsp?formCode=" +
                      ff.getDefaultValueRaw() + "&cwsId=" + cwsId + "&op=" + op;
        return NetUtil.gather(request, "utf-8", path);
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        LogUtil.getLog(getClass()).info("ff=" + ff.getName());
        String str = getNestForm(request, ff);
        // 转换javascript脚本中的\为\\
        str = str.replaceAll("\\\\", "\\\\\\\\");
        return str;
    }

    /**
     * 当创建父记录时，同步创建嵌套表单的记录（用于visual模块，流程中用不到，因为流程中事先生成了空的表单
     * @param macroField FormField
     * @param cwsId String
     * @param creator String
     * @param fu FileUpload
     * @return int
     * @throws ErrMsgException
     */
    public int createForNestCtl(HttpServletRequest request,
                                FormField macroField, String cwsId,
                                String creator,
                                FileUpload fu) throws ErrMsgException {
        String formCode = macroField.getDefaultValueRaw();
        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);

        Vector fields = com.redmoon.oa.visual.FormDAOMgr.getFieldsByForm(request, fd, fu, fd.getFields());

        // 对表单进行有效性验证
        com.redmoon.oa.flow.FormDAOMgr.validateFields(request, fu, fd.getFields(), null, false);

        // 通过检查接口对fields中的值进行有效性判断
        Config cfg = new Config();
        IModuleChecker ifv = cfg.getIModuleChecker(formCode);
        // logger.info("ifv=" + ifv + " formCode=" + formCode);
        boolean re = true;
        if (ifv != null)
            re = ifv.validateCreate(request, fu, fields);
        if (!re)
            throw new ErrMsgException("表单验证非法！");

        FormDAO fdao = new FormDAO(fd);
        fdao.setFields(fields);

        fdao.setCreator(creator);
        // 置用于关联模块的cws_id的值
        fdao.setCwsId(cwsId);
        // return fdao.create(request, fu) ? 1 : 0;
        // 嵌套表单中不保存附件，以免重复保存
        return fdao.create() ? 1 : 0;
    }

    /**
     * 保存嵌套表单中的记录，智能模块与流程中共用本方法
     * @param macroField FormField
     * @param cwsId String 父记录的ID，用于与父记录关联
     * @param creator String 
     * @param fu FileUpload
     * @return int
     * @throws ErrMsgException
     */
    public int saveForNestCtl(HttpServletRequest request, FormField macroField,
                              String cwsId,
                              String creator,
                              FileUpload fu) throws ErrMsgException {
        String formCode = macroField.getDefaultValueRaw();
        FormMgr fm = new FormMgr();
        FormDb fd = fm.getFormDb(formCode);
        // 从request中获取表单中域的值
        Vector fields = com.redmoon.oa.visual.FormDAOMgr.getFieldsByForm(request, fd, fu, fd.getFields());

        int flowId = StrUtil.toInt(fu.getFieldValue("flowId"), -1);
        
        if (flowId!=-1) { // 流程
            // int flowId = StrUtil.toInt(cwsId);
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Directory dir = new Directory();
            Leaf lf = dir.getLeaf(wf.getTypeCode());
            
            FormDb flowFd = new FormDb();
            flowFd = flowFd.getFormDb(lf.getFormCode());
            com.redmoon.oa.flow.FormDAO fdaoParent = new com.redmoon.oa.flow.FormDAO();
            fdaoParent = fdaoParent.getFormDAO(flowId, flowFd);
            long fdaoFlowId = fdaoParent.getId();

	        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
	        int fdaoId = fdao.getIDByCwsId((int)fdaoFlowId);
	        fdao = fdao.getFormDAO(fdaoId, fd);
	        
	        // 对表单进行有效性验证
	        com.redmoon.oa.flow.FormDAOMgr.validateFields(request, fu, fd.getFields(), fdao, true);	        
	        
	        fdao.setFields(fields);
	        fdao.setCreator(creator);
	
	        return fdao.save(request, fu, false) ? 1 : 0;
        }
        else {
	        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(
	                fd);
	        int fdaoId = fdao.getIDByCwsId(StrUtil.toInt(cwsId));
	        fdao = fdao.getFormDAO(fdaoId, fd);
	        
	        // 对表单进行有效性验证
	        com.redmoon.oa.flow.FormDAOMgr.validateFields(request, fu, fd.getFields(), fdao, false);
	
	        // 通过检查接口对fields中的值进行有效性判断
	        Config cfg = new Config();
	        IModuleChecker ifv = cfg.getIModuleChecker(formCode);
	        boolean re = true;
	        if (ifv != null)
	            re = ifv.validateUpdate(request, fu, fdao, fields);
	        if (!re)
	            throw new ErrMsgException("表单验证非法！");
	
	        fdao.setFields(fields);
	        fdao.setCreator(creator);
	
	        return fdao.save(request, fu, false) ? 1 : 0;
        }
    }

    public int onDelNestCtlParent(FormField macroField, String cwsId) {
        String formCode = macroField.getDefaultValueRaw();

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        if (!fd.isLoaded())
            return 0;
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(
                fd);
        int fdaoId = fdao.getIDByCwsId(StrUtil.toInt(cwsId));

        int r = 0;
        fdao = fdao.getFormDAO(fdaoId, fd);
        if (fdao.isLoaded()) {
            fdao.del();
            r = 1;
        }
        return r;
    }

    /**
     * 当初始化流程时，创建一条空记录
     * @param macroField FormField
     * @param flowId int
     * @param userName String
     * @return boolean
     */
    public boolean initNestCtlOnInitWorkflow(FormField macroField, int flowId,
                                             String userName) {
        String formCode = macroField.getDefaultValueRaw();

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        // 为流程创建一条空表单记录，记录中存储的为表单域的默认值
        com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO(flowId, fd);
        Iterator ir = fd.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            ff.setValue(ff.getDefaultValue());
        }
        
        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        
        fdao.setFields(fd.getFields()); // 设置默认值
        fdao.setUnitCode(user.getUnitCode());
        
        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        Directory dir = new Directory();
        Leaf lf = dir.getLeaf(wf.getTypeCode());
        FormDb flowFd = new FormDb();
        flowFd = flowFd.getFormDb(lf.getFormCode());
        com.redmoon.oa.flow.FormDAO fdaoParent = new com.redmoon.oa.flow.FormDAO();
        fdaoParent = fdaoParent.getFormDAO(flowId, flowFd);
        long fdaoId = fdaoParent.getId();

        fdao.setCwsId(String.valueOf(fdaoId));

        return fdao.create();
    }

    public String getControlType() {
        return "";
    }

    public String getControlValue(String userName, FormField ff) {
        return "";
    }

    public String getControlText(String userName, FormField ff) {
        return "";
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}
