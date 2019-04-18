package com.redmoon.oa.flow;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import com.redmoon.oa.kernel.License;
import cn.js.fan.util.ParamUtil;

public class FormMgr {
    public FormMgr() {
    }

    public FormDb getFormDb(String code) {
        FormDb ftd = new FormDb();
        return ftd.getFormDb(code);
    }

    public synchronized boolean move(HttpServletRequest request) throws
            ErrMsgException {
        FormForm fc = new FormForm();
        fc.checkMove(request);

        FormDb ft = getFormDb(fc.getFormDb().getCode());
        return ft.move(fc.getDirection());
    }

    public synchronized boolean create(HttpServletRequest request) throws
            ErrMsgException {
        // 许可证验证
        License.getInstance().validate(request);

        FormForm fc = new FormForm();
        fc.checkCreate(request);

        String ieVersion = ParamUtil.get(request, "ieVersion");

        FormDb ft = fc.getFormDb();
        ft.setIeVersion(ieVersion);
        return ft.create();
    }

    public synchronized boolean del(HttpServletRequest request) throws
            ErrMsgException {
        FormForm fc = new FormForm();
        fc.checkDel(request);

        FormDb ft = fc.getFormDb();

        Leaf lf = new Leaf();
        Iterator ir = lf.getLeavesUseForm(ft.getCode()).iterator();
        while (ir.hasNext()) {
            lf = (Leaf)ir.next();
            WorkflowDb wfd = new WorkflowDb();
            int count = wfd.getWorkflowCountOfType(lf.getCode());
            if (count>0)
                throw new ErrMsgException("流程 " + lf.getName() + " 中已有流程 " + count + " 个，表单不能被删除！");
        }

        ft = ft.getFormDb(ft.getCode());
        return ft.del();
    }

    public synchronized boolean modify(HttpServletRequest request) throws
            ErrMsgException {
        // 许可性验证
        License.getInstance().validate(request);

        FormForm fc = new FormForm();
        fc.checkModify(request);

        FormDb ft = fc.getFormDb();

        FormDb ftd = getFormDb(ft.getCode());
        ftd.setName(ft.getName());
        ftd.setOldContent(ftd.getContent());
        ftd.setContent(ft.getContent());
        ftd.setFlowTypeCode(ft.getFlowTypeCode());
        // System.out.println(getClass() + " ft.isHasAttachment()=" + ft.isHasAttachment());
        ftd.setHasAttachment(ft.isHasAttachment());

        String ieVersion = ParamUtil.get(request, "ieVersion");
        ftd.setIeVersion(ieVersion);
        ftd.setLog(ft.isLog());
        ftd.setProgress(ft.isProgress());
        
        ftd.setUnitCode(ft.getUnitCode());
        ftd.setOnlyCamera(ft.isOnlyCamera());
        
        ftd.setFlow(ft.isFlow());

        boolean re = ftd.save();
        if (re) {
        	// 重新生成视图
    		FormParser fp = new FormParser();
        	FormViewDb fvd = new FormViewDb();
        	Iterator ir = fvd.getViews(ft.getCode()).iterator();
        	while (ir.hasNext()) {
        		fvd = (FormViewDb)ir.next();
        		String form = fp.generateView(fvd.getString("content"), fvd.getString("ie_version"), ft.getCode());
        		fvd.set("form", form);
        		try {
					fvd.save();
				} catch (ResKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}

        }
        
        return re;
    }


}
