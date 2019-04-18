package com.redmoon.oa.visual;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFuncImpl;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.func.CalculateFuncImpl;
import com.redmoon.oa.visual.func.FuncMgr;
import com.redmoon.oa.visual.func.FuncUnit;

public class FuncUtil {
	
	/**
	 * 解析方法名
	 * @param funcStr
	 * @return
	 */
	public static String[] parseFunc(String funcStr) {
		if (funcStr==null) { // || !funcStr.startsWith("$")) {
			return null;
		}
		
        Pattern p = Pattern.compile(
                "\\$([A-Z0-9a-z-_]+)\\((.*?)\\)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(funcStr);
        // StringBuffer sb = new StringBuffer();
        if (m.find()) {
        	String[] ary = new String[2];
            ary[0] = m.group(1); // 方法名
            ary[1] = m.group(2); // 参数
            return ary;
        }
        
        return null;
	}
	
	/**
	 * 判断是否含有函数或表达式
	 * @Description: 
	 * @param str
	 * @return
	 */
	public static boolean hasFunc(String str) {
		if (str==null || "".equals(str)) {
			return false;
		}
		if (parseFunc(str)!=null) {
			return true;
		}
		else {
			// 判断其中是否有加减乘除
			if (str.indexOf("+")>0 || str.indexOf("-")>0 || str.indexOf("*")>0 || str.indexOf("/")>0) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * 调用方法呈现字段，如处理人事列表中的年龄字段（已弃用），改为在年龄默认值中用$Age(csrq)实现展现，同时便于导出
	 * @param funcStr
	 * @param ff
	 * @return
	 */
	public static String render(FormField ff, String funcStr, IFormDAO fdao) {
		// 调用函数
		boolean isMatched = false;
		do {
			isMatched = false;
			// 找到原子算式，因为算式有可能是嵌套的
	        Pattern p = Pattern.compile(
	                "\\$([A-Z0-9a-z-_]+)\\(([^\\$]*?)\\)",
	                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	        Matcher m = p.matcher(funcStr);
	        StringBuffer sb = new StringBuffer();
	        while (m.find()) {
	        	isMatched = true;
	            	            
	        	String[] myfunc = new String[2];
	        	myfunc[0] = m.group(1); // 方法名
	        	myfunc[1] = m.group(2); // 参数	            
	            
	            String val = "1";
	            
	            FuncMgr fm = new FuncMgr();
	            FuncUnit fu = fm.getFuncUnit(myfunc[0]);
	            if (fu!=null) {
	            	IFuncImpl ifil = fu.getIFuncImpl();
	            	try {
	            		val = StrUtil.getNullStr(ifil.func(fdao, myfunc));
	            	}
	            	catch (ErrMsgException e) {
	            		e.printStackTrace();
	            		return m.group() + " " + e.getMessage();
	            		// val = e.getMessage();
	            	}
	            }
	            else {
	            	LogUtil.getLog(FuncUtil.class).error(myfunc[0] + "不存在！");
	            	return myfunc[0] + "不存在！";
	            }

            	m.appendReplacement(sb, val);
	        }
	        m.appendTail(sb);	
        
	        funcStr = sb.toString();	        
		}
		while (isMatched);
		
		// 判断是否为数字，如果不是，则再做一次四则运算
		if (!"".equals(funcStr) && !StrUtil.isDouble(funcStr)) {
			if (ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE
					|| ff.getFieldType()==FormField.FIELD_TYPE_INT
					|| ff.getFieldType()==FormField.FIELD_TYPE_FLOAT
					|| ff.getFieldType()==FormField.FIELD_TYPE_LONG
					|| ff.getFieldType()==FormField.FIELD_TYPE_PRICE					
			) {
				CalculateFuncImpl cfi = new CalculateFuncImpl();
				try {
					funcStr = StrUtil.getNullStr(cfi.calculate(fdao, funcStr, 2, true));
				}
				catch (ErrMsgException e) {
					return e.getMessage();
				}			
			}
		}

		return funcStr;

	}
	
	public static String AgeXXX(IFormDAO fdao, String[] func, FormField ff) {
		String fieldName = func[1];
		String val;
		if (fieldName==null || "".equals(fieldName)) {
			val = ff.getValue();
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
	
	/**
	 * 显示经过处理的字段值
	 * @param fdao
	 * @param ff
	 * @return
	 */
	public static String renderFieldValue(IFormDAO fdao, FormField ff) {
		if (ff == null) {
			return "";
		}
		
		if (fdao.getFormDb().getCode().equals("sales_customer") && ff.getName().equals("dept_code")) {
			DeptDb dd = new DeptDb(fdao.getFieldValue(ff.getName()));
			if (dd == null || !dd.isLoaded()) {
				return "";
			} else {
				return StrUtil.getNullStr(dd.getName());
			}
		}		
		
		if (!ff.isFunc()) {
			return ff.convertToHtml();
		}
		
		String dv = ff.getDefaultValueRaw();
		return render(ff, dv, fdao);
	}
	
	/**
	 * 取得函数的参数中关联的表单域，以逗号分隔
	 * @Description: 
	 * @param ff
	 * @return
	 */
	public static String getFieldsRelatedOnChangeJS(FormDb fd, FormField ff) {
		String funcStr = ff.getDefaultValueRaw();
		
		ArrayList<String> aryAll = new ArrayList<String>();
		boolean isMatched = false;

		do {
			isMatched = false;
			StringBuffer sb = new StringBuffer();

			// 找到原子算式，因为算式有可能是嵌套的
			Pattern p = Pattern.compile("\\$([A-Z0-9a-z-_]+)\\(([^\\$]*?)\\)",
					Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(funcStr);
			while (m.find()) {
				isMatched = true;

				String[] myfunc = new String[2];
				myfunc[0] = m.group(1); // 方法名
				myfunc[1] = m.group(2); // 参数

				FuncMgr fm = new FuncMgr();
				FuncUnit fu = fm.getFuncUnit(myfunc[0]);
				if (fu != null) {
					IFuncImpl ifil = fu.getIFuncImpl();
					ArrayList<String> ary = ifil.getFieldsRelated(myfunc, fd);
					if (ary.size() > 0) {
						aryAll.addAll(ary);
					}
				} else {
					LogUtil.getLog(FuncUtil.class).error(myfunc[0] + "不存在！");
					return "";
				}
				String val = "1";
				m.appendReplacement(sb, val);
			}
			m.appendTail(sb);
			
			funcStr = sb.toString();
		} while (isMatched);     
		
		// 如果没有匹配的，则说明是四则运算表达式
		if (!isMatched) {
			ArrayList<String> aryList = CalculateFuncImpl.getSymbolsWithBracket(funcStr);
		    Object[] ary = aryList.toArray();
			for (int i=0; i<ary.length; i++) {
				String el = (String)ary[i];
		        if (!CalculateFuncImpl.isOperator(el)) {
		            // ary[i]可能为0.2这样的系数
				    if (!StrUtil.isDouble(el)) {
				    	FormField ffSub = fd.getFormField(el);
				    	if (ffSub!=null) {
				    		aryAll.add(ffSub.getName());
				    	}
				    }
		        }
			}

		}

		StringBuffer sb = new StringBuffer();
		int len = aryAll.size();
		for (int i=0; i<len; i++) {
			String fieldName = aryAll.get(i);
			StrUtil.concat(sb, ",", fieldName);
		}
		
		return "bindFuncFieldRelateChangeEvent(\"" + fd.getCode() + "\", \"" + ff.getName() + "\", \"" + sb.toString() + "\");\n";
	}
	
	public static String doGetFieldsRelatedOnChangeJS(FormDb fd) {
		String str = "<script>\n";
		Iterator ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			if (ff.isFunc()) {
				str += getFieldsRelatedOnChangeJS(fd, ff);
			}
		}
		str += "</script>\n";
		return str;
	}

}
