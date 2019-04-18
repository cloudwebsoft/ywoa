package com.redmoon.oa.visual.func;

import java.util.ArrayList;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFuncImpl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FuncUtil;

/**
 * 
 * @Description: $Max(a, b)
 * @author: 
 * @Date: 2017-9-8上午07:53:17
 */
public class MaxFuncImpl implements IFuncImpl {
	
	public String func(IFormDAO fdao, String[] func) throws ErrMsgException {
		String[] params = StrUtil.split(func[1], ",");
		if (params==null || params.length!=2) {
			throw new ErrMsgException("参数必须为2个！");
		}
		
		String p0 = params[0].trim();
		if (!NumberUtil.isNumeric(p0)) {
			p0 = CalculateFuncImpl.calculate(fdao, p0, -1, true);
		}
		
		String p1 = params[1].trim();
		if (!NumberUtil.isNumeric(p1)) {
			p1 = CalculateFuncImpl.calculate(fdao, p1, -1, true);
		}		

		double a = StrUtil.toDouble(p0);
		double b = StrUtil.toDouble(p1);
		
		if (a>=b) {
			return p0;
		}
		else {
			return p1;
		}
	}
	
	public ArrayList<String> getFieldsRelated(String[] func, FormDb fd) {
		ArrayList<String> list = new ArrayList<String>();
		String[] params = StrUtil.split(func[1], ",");
		if (params==null || params.length!=2) {
			LogUtil.getLog(getClass()).error("参数必须为2个！");
		}
		
		String p0 = params[0];
		if (!NumberUtil.isNumeric(params[0])) {
			// 判断是否为函数
			if (FuncUtil.parseFunc(p0)==null) {
				list.add(p0);
			}
		}
		
		String p1 = params[1];
		if (!NumberUtil.isNumeric(params[1])) {
			if (FuncUtil.parseFunc(p0)==null) {			
				list.add(p1);
			}
		}		
		return list;
		
	}

}

