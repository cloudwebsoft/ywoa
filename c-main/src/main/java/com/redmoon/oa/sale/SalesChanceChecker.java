package com.redmoon.oa.sale;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.visual.IModuleChecker;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import com.redmoon.oa.visual.FormDAO;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormDb;
import cn.js.fan.util.DateUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.sale.SalePrivilege;

/**
 * <p>Title: 订单的有效性验证</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SalesChanceChecker implements IModuleChecker {
    public SalesChanceChecker() {
        super();
    }

    @Override
    public boolean validateUpdate(HttpServletRequest request, FileUpload fu, FormDAO fdaoBeforeUpdate, Vector fields) throws ErrMsgException {

        return true;
    }
    
    @Override
	public boolean validateCreate(HttpServletRequest request, FileUpload fu, Vector fields) throws ErrMsgException {

        return true;
    }

    @Override
	public boolean validateDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {

        return true;
    }

    @Override
	public boolean onDel(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
        return true;
    }

    @Override
	public boolean onCreate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
    	long customerId = StrUtil.toInt(fdao.getFieldValue("customer"));
    	
    	FormDb fd = new FormDb();
    	fd = fd.getFormDb("sales_customer");
    	FormDAO fdaoCust = new FormDAO();
    	fdaoCust = fdaoCust.getFormDAO(customerId, fd);
    	
    	fdao.setCwsId("" + fdaoCust.getId());
    	fdao.save();
    	
		// 在更新商机的时候，根据商机的意向，修改对应的客户阶段
		// 并强制使赢率与商机阶段保持一致
    	String state = fdao.getFieldValue("state");
    	String formCode = "sales_stage_ratio";
        FormDAO fdaoStage = new FormDAO();
        String sql = "select id from " + FormDb.getTableName(formCode);
        Iterator ir = fdaoStage.list(formCode, sql).iterator();
        while (ir.hasNext()) {
        	fdaoStage = (FormDAO)ir.next();
        	String intent_stage = fdaoStage.getFieldValue("intent_stage");
        	String win_ratio = fdaoStage.getFieldValue("win_ratio");
        	String customer_stage = fdaoStage.getFieldValue("customer_stage");
        	if (intent_stage.equals(state)) {
        		// 如果商机中的赢率与预先配置的不符，则以预配的为准
        		if (!fdao.getFieldValue("possibility").equals(win_ratio)) {
        			fdao.setFieldValue("possibility", win_ratio);
        			fdao.save();
        		}
        		
        		// 置客户阶段
        		fdaoCust.setFieldValue("customer_type", customer_stage);
        		fdaoCust.save();
        		
        		break;
        	}
        }  
        return true;
    }

    @Override
	public boolean onNestTableCtlAdd(HttpServletRequest request, HttpServletResponse response, JspWriter out) {
    	return false;
    } 
    
    @Override
	public boolean onUpdate(HttpServletRequest request, FormDAO fdao) throws ErrMsgException {
		// 在更新商机的时候，根据商机的意向，修改对应的客户阶段
		// 并强制使赢率与商机阶段保持一致
    	String state = fdao.getFieldValue("state");
    	String formCode = "sales_stage_ratio";
        FormDAO fdaoStage = new FormDAO();
        String sql = "select id from " + FormDb.getTableName(formCode);
        Iterator ir = null;
        try {
        	ir = fdaoStage.list(formCode, sql).iterator();
        }
        catch (ErrMsgException e) {
        	e.printStackTrace();
        	LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        	return false;
        }
        while (ir.hasNext()) {
        	fdaoStage = (FormDAO)ir.next();
        	String intent_stage = fdaoStage.getFieldValue("intent_stage");
        	String win_ratio = fdaoStage.getFieldValue("win_ratio");
        	String customer_stage = fdaoStage.getFieldValue("customer_stage");
        	if (intent_stage.equals(state)) {
        		// 如果商机中的赢率与预先配置的不符，则以预配的为准
        		if (!fdao.getFieldValue("possibility").equals(win_ratio)) {
        			fdao.setFieldValue("possibility", win_ratio);
        			fdao.save();
        		}
        		
        		String customer = fdao.getFieldValue("customer");
        		long customerId = StrUtil.toLong(customer, -1);
        		FormDAO fdaoCust = new FormDAO();
        		FormDb fdCust = new FormDb();
        		fdCust = fdCust.getFormDb("sales_customer");
        		fdaoCust = fdaoCust.getFormDAO(customerId, fdCust);
        		// 置客户阶段
        		fdaoCust.setFieldValue("customer_type", customer_stage);
        		fdaoCust.save();
        		
        		break;
        	}
        }
    	
  	
        return true;
    }    
}
