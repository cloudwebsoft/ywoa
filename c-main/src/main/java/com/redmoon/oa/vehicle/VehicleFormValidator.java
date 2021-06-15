package com.redmoon.oa.vehicle;

import javax.servlet.http.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.*;
import java.util.Vector;
import cn.js.fan.util.ErrMsgException;

import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;

import java.util.Iterator;
import cn.js.fan.db.Conn;
import cn.js.fan.web.Global;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class VehicleFormValidator implements IFormValidator {
    Logger logger = Logger.getLogger(VehicleFormValidator.class.getName());
    
    private String extraData = "";
    private boolean used = true;

    public VehicleFormValidator() {
    }

    public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction) throws ErrMsgException {
    	FormDb fd = new FormDb();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        fd = fd.getFormDb(lf.getFormCode());
        
        FormDAO fdao = new FormDAO(wf.getId(), fd);
        fdao.load();
        
        // fdao.getFieldValue("result");
        
        Vector v = fdao.getFields();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            LogUtil.getLog(getClass()).info("ff.getName=" + ff.getName() + " ff.getValue=" + ff.getValue());
        }
    }

    /**
     * isValid
     *
     * @param httpServletRequest HttpServletRequest
     * @return boolean
     * @todo Implement this com.redmoon.oa.base.IFormValidator method
     */
    public boolean validate(HttpServletRequest httpServletRequest, FileUpload fu, int flowId, Vector fields) throws ErrMsgException {
        // 检查当前时间段内该车是否已被批准使用
        Iterator ir = fields.iterator();
        FormField beginDateField = null, endDateField = null, licenseNoField = null;
        int count = 0;
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getName().equals("beginDate")) {
                beginDateField = ff;
                count += 1;
            }
            else if (ff.getName().equals("endDate")) {
                endDateField = ff;
                count += 1;
            }
            else if (ff.getName().equals("licenseNo")) {
                licenseNoField = ff;
                if (licenseNoField.getValue().equals(""))
                    throw new ErrMsgException("请选择车牌号码！");
                count += 1;
            }
            else if (ff.getName().equals("result")) {
                // if (ff.getValue().trim().equals(""))
                //     throw new ErrMsgException("请选择是否同意！");
            }
            if (count>=4)
                break;
        }

        if (count<3)
            throw new ErrMsgException("请检查表单中是否含有起始时间、结束时间和车牌号码！");
        String sql = "select flowId from form_table_vehicle_apply where beginDate>=? and endDate<=? and myresult='是' and flowId<>? and licenseNo=?";
        ResultSet rs = null;
        PreparedStatement ps = null;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, beginDateField.getValue());
            ps.setString(2, endDateField.getValue());
            ps.setInt(3, flowId);
            ps.setString(4, licenseNoField.getValue());
            rs = conn.executePreQuery();
            if (rs!=null && rs.next())
                throw new ErrMsgException("车牌为" + licenseNoField.getValue()  + "的车已被使用！");
        }
        catch (SQLException e) {
            logger.error("validate:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }
    
	public String getExtraData() {
		// TODO Auto-generated method stub
		return extraData;
	}

	public boolean isUsed() {
		// TODO Auto-generated method stub
		return used;
	}

	public void setExtraData(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setIsUsed(boolean arg0) {
		// TODO Auto-generated method stub
		used = arg0;
		
	}

	@Override
	public void onActionFinished(HttpServletRequest arg0, int arg1,
			FileUpload arg2) {
		// TODO Auto-generated method stub
		
	}    
}
