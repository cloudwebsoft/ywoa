package com.redmoon.oa.visual.func;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cn.js.fan.util.DateUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFuncImpl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;

/**
 * getMonthDiff(a)
 * @Description: 取得至当前时间的月份数
 * @author: 
 * @Date: 2017-9-8上午08:04:25
 */
public class MonthDiffFuncImpl implements IFuncImpl {

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
			int m = getMonthDiff(new java.util.Date(), d);
			return String.valueOf(m);
		}
		else {
			return "1";
		}					
	}

    public int getMonthDiff(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        if(c1.getTimeInMillis() < c2.getTimeInMillis())
        	return 0;
        int year1 = c1.get(Calendar.YEAR);
        int year2 = c2.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        // 获取年的差值 假设 d1 = 2015-8-16  d2 = 2011-9-30
        int yearInterval = year1 - year2;
        // 如果 d1的 月-日 小于 d2的 月-日 那么 yearInterval-- 这样就得到了相差的年数
        if(yearInterval!=0 && month1 < month2) {
        	yearInterval --;
        }
        // 获取月数差值
        int monthInterval =  (month1 + 12) - month2;
        // 不足1月，则减去1
        // if(day1 < day2)
        // 	monthInterval --;
        monthInterval %= 12;
        return yearInterval * 12 + monthInterval;
    }	
		
	public ArrayList<String> getFieldsRelated(String[] func, FormDb fd) {
		String fieldName = func[1];		
		ArrayList<String> list = new ArrayList<String>();
		list.add(fieldName);
		return list;
	}
    
}
