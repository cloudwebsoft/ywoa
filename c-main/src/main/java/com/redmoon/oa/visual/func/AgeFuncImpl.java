package com.redmoon.oa.visual.func;

import java.util.ArrayList;

import cn.js.fan.util.DateUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFuncImpl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;

public class AgeFuncImpl implements IFuncImpl {

	@Override
	public String func(IFormDAO fdao, String[] func) {
		String fieldName = func[1];
		String val;
		if (fieldName==null || "".equals(fieldName)) {
			LogUtil.getLog(getClass()).error(fieldName + "不存在！");			
			val = "";
		}
		else {
			val = fdao.getFieldValue(fieldName);
		}
		java.util.Date d = DateUtil.parse(val, "yyyy-MM-dd");
		if (d!=null) {
			int days = DateUtil.datediff(new java.util.Date(), d);
			return String.valueOf(days/365 + 1);
		}
		else {
			return "";
		}
	}
	
	@Override
	public ArrayList<String> getFieldsRelated(String[] func, FormDb fd) {
		String fieldName = func[1];		
		ArrayList<String> list = new ArrayList<String>();
		list.add(fieldName);
		return list;
	}	

}
