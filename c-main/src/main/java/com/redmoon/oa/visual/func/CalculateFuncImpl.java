package com.redmoon.oa.visual.func;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFuncImpl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.FuncUtil;

/**
 * 
 * @Description: 
 * $calc(参数1, 参数2)
 * 参数1为算式，如：field1 + field2
 * 参数2为小数点后的位数，如：2
 * 支持calc的嵌套，如：$calc($calc(field3-field1) + field2, 2)
 * @author: 
 * @Date: 2017-9-8上午07:40:47
 */
public class CalculateFuncImpl implements IFuncImpl {

	@Override
	public String func(IFormDAO fdao, String[] func) throws ErrMsgException {
		String[] params = StrUtil.split(func[1], ",");
		String formula = params[0];
		int digit = StrUtil.toInt(params[1], 2);
		
		return calculate(fdao, formula, digit, true);
	}
	
	/**
	 * 计算
	 * @Description: 
	 * @param fdao
	 * @param formula
	 * @param digit 当为-1时保留原来的精度
	 * @param isRoundTo5
	 * @return
	 */
	public static String calculate(IFormDAO fdao, String formula, int digit, boolean isRoundTo5) throws ErrMsgException {		
		ArrayList<String> aryList = getSymbolsWithBracket(formula);
	    Object[] ary = aryList.toArray();
		for (int i=0; i<ary.length; i++) {
			String el = (String)ary[i];
	        if (!isOperator(el)) {
	            // ary[i]可能为0.2这样的系数
			    if (!StrUtil.isDouble(el)) {
			    	// 判断el是否为表单域，如果不是，则可能为含有函数或表达式，则先计算此算式
			    	FormField ffSub = fdao.getFormDb().getFormField(el);
			    	if (ffSub==null) {
			    		// 如果el不是字段，由可能出现了书写错误
			    		ary[i] = "1";
			    		LogUtil.getLog(CalculateFuncImpl.class).error(el + "不是字段！");
			    	}
			    	else {
			    		// 是否为函数型，递归，展开算式，继续从原子算式开始计算
			    		// 如：$calc(yz - ljzj -jzzb, 2)，而ljzj=$getMonthDiff(qyrq)*yzje
			    		if (ffSub.isFunc()) {
			    			ary[i] = FuncUtil.render(ffSub, ffSub.getDefaultValueRaw(), fdao);
			    		}
			    		else {
							String v = StrUtil.getNullStr(fdao.getFieldValue(el));
							if ("".equals(v) || !StrUtil.isDouble(v)) {
								// ary[i] = "1"; // 置为0会导致出现除0问题
								throw new ErrMsgException(ffSub.getTitle() + "为空");
							}
							else {
								ary[i] = "(" + v + ")";
							}
			    		}
			    	}
				}
	        }
	    }
		
	    formula = "";
	    for (int i=0; i < ary.length; i++) {
	        formula += ary[i];
	    }

    	ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        try {
        	DebugUtil.i(CalculateFuncImpl.class, "formula" ,formula);
        	Double ret = StrUtil.toDouble(engine.eval(formula).toString(), -65536);
        	if (digit==-1) {
        		// digit=-1 表示不需要精度
        		if (ret==null) {
        			return null;
        		}
        		else {
        			return String.valueOf(ret.doubleValue());
        		}
        	}
		    if (isRoundTo5) {
		    	String str = NumberUtil.round(ret.doubleValue(), digit);
		    	if ("∞".equals(str)) {
		    		return null;
		    	}
		    	else {
		    		return str;
		    	}
		    }
		    else {
		    	DecimalFormat formater = new DecimalFormat("#0.##");
		    	String str = formater.format(ret.doubleValue());
		    	
		    	if ("∞".equals(str)) {
		    		return null;
		    	}
		    	else {
		    		return str;
		    	}		    	
		    }
        }
        catch (ScriptException ex) {
        	LogUtil.getLog(CalculateFuncImpl.class).error("formula=" + formula);        	
        	LogUtil.getLog(CalculateFuncImpl.class).error(StrUtil.trace(ex));
        }			
        return null;
	}	

	// 四则运算拆分算式
	public static ArrayList<String> getSymbolsWithBracket(String str) {
	    // 去除空格
	    str = str.replaceAll(" ", "");
	    if (str.indexOf("+")==0) {
			str = str.substring(1); // 去掉开头的+号
		}
	    ArrayList<String> list = new ArrayList<String>();
	    int curPos = 0;
	    int prePos = 0;
	    int len = str.length();
	    for (int i = 0; i < len; i++) {
	        char s = str.charAt(i);
	        
	        if (s == '+' || s == '-' || s == '*' || s == '/' || s=='(' || s==')') {
	            if (prePos<curPos) {
	            	// 用于检查是否为表单域，如果该域有算式，则展开算式
	                list.add(str.substring(prePos, curPos).trim());
	            }
	            
	            list.add(String.valueOf(s));
	            prePos = curPos + 1;
	        }
	        curPos++;
	    }
	    if (prePos <= str.length() - 1)
	        list.add(str.substring(prePos).trim());
	    return list;
	}
	
	// 是否四则运算符
	public static boolean isOperator(String str) {
	    if (str.equals("+") || str.equals("*") || str.equals("/") || str.equals("-") || str.equals("(") || str.equals(")")) {
	        return true;
	    }
	    else
	        return false;
	}
	
	/**
	 * 取得算式中相关联的表单域，无需考虑嵌套算式的情况，因为在FuncUtil中按照先取出原子算子的方式来分析
	 * @Description: 
	 * @param func
	 * @param fd
	 * @return
	 */
	public ArrayList<String> getFieldsRelated(String[] func, FormDb fd) {
		ArrayList<String> list = new ArrayList<String>();
		String[] params = StrUtil.split(func[1], ",");
		String formula = params[0];
		ArrayList<String> aryList = getSymbolsWithBracket(formula);
	    Object[] ary = aryList.toArray();
		for (int i=0; i<ary.length; i++) {
			String el = (String)ary[i];
	        if (!isOperator(el)) {
	            // ary[i]可能为0.2这样的系数
			    if (!StrUtil.isDouble(el)) {
			    	// 判断el是否为表单域，如果不是，则可能为含有函数或表达式，则先计算此算式
			    	FormField ffSub = fd.getFormField(el);
			    	if (ffSub!=null) {
			   			list.add(ffSub.getName());
			    	}
			    }
	        }
		}
		return list;
	}
}

