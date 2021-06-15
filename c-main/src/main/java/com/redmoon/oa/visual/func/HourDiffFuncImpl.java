package com.redmoon.oa.visual.func;

import java.util.ArrayList;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFuncImpl;
import com.redmoon.oa.flow.FormDb;

/**
 * 取得两个时间之间的小时数
 * 例如：
 * $getHourDiff($connStr(begin_hour+":"+ begin_time), $connStr(end_hour + ":" + end_time), "HH:mm", 1)
 * $getHourDiff(begin_hour+":"+ begin_time, end_hour + ":" + end_time, "HH:mm", 1)
 * "HH:mm"为时间格式，1表示小数点后的位数
 * @Description: 
 * @author: 
 * @Date: 2017-9-10下午07:31:28
 */
public class HourDiffFuncImpl implements IFuncImpl {

	/**
	 * 默认精确到小数点后1位，并四舍五入
	 * @Description: 
	 * @param fdao
	 * @param func
	 * @return
	 * @throws ErrMsgException
	 */
	@Override
	public String func(IFormDAO fdao, String[] func) throws ErrMsgException {
		String[] params = StrUtil.split(func[1], ",");
		if (params==null || params.length<3) {
			throw new ErrMsgException("参数必须为3个以上！");
		}
		
		String p0 = params[0].trim();
		String p1 = params[1].trim();
		
		// $getHourDiff(begin_hour+":"+ begin_time, end_hour + ":" + end_time, "HH:mm", 1)
		// p0及p1可能为表达式，如：begin_hour+":"+ begin_time
		p0 = ConnStrFuncImpl.doConn(fdao, p0);
		p1 = ConnStrFuncImpl.doConn(fdao, p1);
		
		if ("".equals(p0) || "".equals(p1)) {
			return "";
		}		
		
		String format = params[2].trim();
		if (format.startsWith("\"") && format.endsWith("\"")) {
			format = format.substring(1, format.length()-1);
		}
		
		int digit = 1;
		if (params.length==4) {
			digit = StrUtil.toInt(params[3], -1);
			if (digit==-1) {
				digit = 1;
			}
		}
		
		// 计算HH:mm格式
		if ("HH:mm".equalsIgnoreCase(format)) {
			p0 = "2017-01-01 " + p0 + ":00";
			
			String[] ary0 = StrUtil.split(p0, ":");
			String[] ary1 = StrUtil.split(p1, ":");
			
			int h0 = StrUtil.toInt(ary0[0], -1);
			int h1 = StrUtil.toInt(ary1[0], -1);
			
			if (h1<h0) {
				p1 = "2017-01-02 " + p1 + ":00";
			}
			else {
				p1 = "2017-01-01 " + p1 + ":00";
			}
		}
		
		java.util.Date d1 = DateUtil.parse(p0, "yyyy-MM-dd HH:mm:ss");
		java.util.Date d2 = DateUtil.parse(p1, "yyyy-MM-dd HH:mm:ss");
		if (d2!=null && d1!=null) {
			double r = d1.getTime() - d2.getTime();
			r = Math.abs(r / (60 * 1000 * 60));
			return NumberUtil.round(r, digit);
		}
		else {
			LogUtil.getLog(getClass() + " p0=" + p0 + " p1=" + p1);
			return "";
		}
	}
		
	@Override
	public ArrayList<String> getFieldsRelated(String[] func, FormDb fd) {
		String[] params = StrUtil.split(func[1], ",");
		
		// $getHourDiff($connStr(begin_hour+":"+ begin_time), $connStr(end_hour + ":" + end_time), "HH:mm", 1)
		// 当FuncUtil解析原子算式时，从子算式中返回后，p0可能变变成了1
		String p0 = params[0].trim();
		String p1 = params[1].trim();
		
		ArrayList<String> list0 = ConnStrFuncImpl.getFieldsRelated(fd, p0);
		ArrayList<String> list1 = ConnStrFuncImpl.getFieldsRelated(fd, p1);
		
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(list0);
		list.addAll(list1);
		return list;
	}
}
