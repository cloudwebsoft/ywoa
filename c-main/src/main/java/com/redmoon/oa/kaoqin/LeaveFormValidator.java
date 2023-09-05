package com.redmoon.oa.kaoqin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormValidator;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.person.UserDb;
import org.json.*;

public class LeaveFormValidator implements IFormValidator {

    private boolean used = true;
    private String extraData = "";
    
    private boolean checkNJ = true;

    public LeaveFormValidator() {
    }

    public void onWorkflowFinished(WorkflowDb wf, WorkflowActionDb lastAction) throws ErrMsgException {

    }
    
    public boolean isCheckNJ() {
    	return checkNJ;
    }

    /**
     * isValid
     *
     * @param httpServletRequest HttpServletRequest
     * @return boolean
     * @todo Implement this com.redmoon.oa.base.IFormValidator method
     */
    public boolean validate(HttpServletRequest httpServletRequest, FileUpload fu, int flowId, Vector fields) throws ErrMsgException {
        // fields来自于数据库
    	/*
    	double dayCount = 0;
        String userName = null;
        String jqlb = null;
        java.util.Date ksDate = null;
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            if (ff.getName().equals("day_count")) {
                dayCount = StrUtil.toDouble(ff.getValue(), 0.0);
            }
            else if (ff.getName().equals("applier")) {
            	userName = ff.getValue();
            }
            else if (ff.getName().equals("jqlb")) {
            	jqlb = ff.getValue();
            }
            else if (ff.getName().equals("qjkssj")) {
            	ksDate = DateUtil.parse(ff.getValue(), "yyyy-MM-dd");
            }
        }
        */
    	
    	String jqlb = fu.getFieldValue("jqlb");

    	if (checkNJ && jqlb.equals("年假")) {
        	String userName = fu.getFieldValue("applier");
        	double dayCount = StrUtil.toDouble(fu.getFieldValue("day_count"));
        	java.util.Date ksDate = DateUtil.parse(fu.getFieldValue("qjkssj"), "yyyy-MM-dd");
        	
        	// 取得工作年限
        	UserDb user = new UserDb();
        	user = user.getUserDb(userName);
        	String idCard = "", cjgzsj = "";
			java.util.Date workDate = null;

			String sql = "select id from ft_personbasic where user_name=" + StrUtil.sqlstr(userName);
			com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
			Iterator ir = fdao.list("personbasic", sql).iterator();
			if (ir.hasNext()) {
				fdao = (com.redmoon.oa.visual.FormDAO) ir.next();
				idCard = fdao.getFieldValue("idcard");
				cjgzsj = fdao.getFieldValue("cjgzsj");
				if ("".equals(idCard)) {
					throw new ErrMsgException("用户" + user.getRealName() + "档案信息中的身份证未填写！");
				}

				workDate = DateUtil.parse(cjgzsj, "yyyy-MM-dd");
				if (workDate==null) {
					throw new ErrMsgException("用户" + user.getRealName() + "档案信息中的参加工作时间未填写！");
				}
			}
			else {
				throw new ErrMsgException("用户" + user.getRealName() + "没有档案信息！");
			}

    		int workDateYear = DateUtil.getYear(workDate);
    		int workDateMonth = DateUtil.getMonth(workDate);
    		
    		int ksMonth = DateUtil.getMonth(ksDate);
    		int ksYear = DateUtil.getYear(ksDate);
    		
    		// 计算工作满多少整年
    		int y = ksYear - workDateYear;
    		if (ksMonth < workDateMonth)
    			y--;

    		if (y<1) {
    			throw new ErrMsgException("职工累计工作不满1年的，不享受年休假");
    		}
    		
    		double bjDayCount = LeaveMgr.getLeaveCount(ksYear, userName, "病假");
    		
        	// 职工请事假累计20天以上且单位按照规定不扣工资的
    		double sjDayCount = LeaveMgr.getLeaveCount(ksYear, userName, "事假", true);
    		if (sjDayCount>20) {
				throw new ErrMsgException("职工请事假累计20天以上且单位按照规定不扣工资的，不享受当年的年休假");    			
    		}

        	// 累计工作满1年不满10年的职工，请病假累计2个月以上的
    		if (y>=1 && y<10) {
    			if (bjDayCount>=60)
    				throw new ErrMsgException("累计工作满1年不满10年的职工，请病假累计2个月以上的，不享受当年的年休假");
    		}
    		
        	// 累计工作满10年不满20年的职工，请病假累计3个月以上的
    		if (y>=10 && y<20) {
    			if (bjDayCount>=90) {
    				throw new ErrMsgException("累计工作满10年不满20年的职工，请病假累计3个月以上的，不享受当年的年休假");
    			}
    		}
        	// 累计工作满20年以上的职工，请病假累计4个月以上的。
    		if (y>=20) {
    			if (bjDayCount>=120) {
    				throw new ErrMsgException("累计工作满满20年的职工，请病假累计4个月以上的，不享受当年的年休假");
    			}    			
    		}
    		
    		// 职工累计工作已满1年不满10年的，年休假5天；已满10年不满20年的，年休假10天；已满20年的，年休假15天。
    		
    		// 当前已请年假天数
    		double njDayCount = LeaveMgr.getLeaveCount(ksYear, userName, "年假");
    		double dc = dayCount + njDayCount;
    		if (y>=1 && y<10) {
    			if (dc>5) {
    				throw new ErrMsgException("职工累计工作已满1年不满10年的，年休假5天！");
    			}
    		}
    		if (y>=10 && y<20) {
    			if (dc>10) {
    				throw new ErrMsgException("职工累计工作已满10年不满20年的，年休假10天，您当前已休" + njDayCount + "天！");
    			}
    		}
    		if (y>=20) {
    			if (dc>15) {
    				throw new ErrMsgException("职工累计工作已满20年的，年休假15天！");
    			}    			
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

	public void setExtraData(String extraData) {
		// TODO Auto-generated method stub
		this.extraData = extraData;
		
		JSONObject json;
		try {
			json = new JSONObject(extraData);
			checkNJ = "true".equals(json.getString("isCheckNJ"));
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}
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
