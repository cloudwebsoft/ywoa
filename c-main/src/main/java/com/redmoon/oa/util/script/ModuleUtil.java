package com.redmoon.oa.util.script;

import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.Directory;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowDb;

public class ModuleUtil {

	public void checkNestData() {
		int flowId = -1; // 在事件脚本中程序内部已赋值，在写注释的时候需注释掉
		FormDAO fdao = null; // 在事件脚本中程序内部已赋值，在写注释的时候需注释掉

		String nestFormCode = "....."; // 填写嵌套表的编码
		
		boolean ret = true;
		String errMsg = "";

		WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        Directory dir = new Directory();
        Leaf lf = dir.getLeaf(wf.getTypeCode());
        
        FormDb flowFd = new FormDb();
        flowFd = flowFd.getFormDb(lf.getFormCode());
        
        // com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
        // fdao = fdao.getFormDAO(flowId, flowFd);
        // long fdaoId = fdao.getId();
        
		com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(flowFd.getCode());
		String relateFieldValue = fdm.getRelateFieldValue(fdao.getId(), nestFormCode);
		if (relateFieldValue==null) {
			// out.print(SkinUtil.makeErrMsg(request, "请检查模块" + fd.getName() + "是否相关联"));
			// return false;
			ret = false;
			errMsg = "请检查模块是否相关联！";
			return;
		}
			
		String sql = "select id from form_table_" + nestFormCode + " where cws_id=" + StrUtil.sqlstr(relateFieldValue);
		sql += " order by cws_order";
			
		String type = "";
		
		// System.out.println(getClass() + " sql=" + sql);
		Vector fdaoV = fdao.list(nestFormCode, sql);	
		Iterator ir = fdaoV.iterator();
		while (ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			
			type = fdao.getFieldValue("type");
			
			if ("mobile".equals(type)) {
				float allPrice = StrUtil.toFloat(fdao.getFieldValue("allPrice"));
				
				
			}
			
		}
	}
}
