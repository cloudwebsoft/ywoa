package com.redmoon.oa.visual.func;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFuncImpl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.FuncUtil;

/**
 * 
 * @Description: 
 * $if(exp, a, b, c, d)
 * 当exp为真时，取值为a，否则为b
 * a、b可能为表达式，如：field3 + 10
 * c表示小数点精度位数
 * d表示是否四舍五入
 * @author: 
 * @Date: 2017-3-15下午05:03:51
 */
public class IfImpl implements IFuncImpl {
	
	@Override
	public String func(IFormDAO fdao, String[] func) throws ErrMsgException {
		String[] params = StrUtil.split(func[1], ",");
		if (params==null || params.length<3) {
			throw new ErrMsgException("参数必须至少有3个！");
		}
		
		int digit = -1;
		boolean isRoundTo5 = true;
		if (params.length>3) {
			digit = StrUtil.toInt(params[3], -1);
		}
		
		if (params.length>4) {
			isRoundTo5 = Boolean.valueOf(params[4]);
		}
		
		// 第一个参数为布尔表达式
		// if (exp, a, b)
		boolean re = evalBoolean(fdao, params[0]);
		
		String a, b;
		if (params.length == 3) {
			a = params[1];
			b = params[2];
		}
		else {
			a = CalculateFuncImpl.calculate(fdao, params[1], -1, true);
			b = CalculateFuncImpl.calculate(fdao, params[2], -1, true);
		}

		String r = "";
		if (re) {
			r = a;
		}
		else {
			r = b;
		}

		// 如果长度为3,则直接返回，因为a、b有可能为字符串
		if (params.length == 3) {
			return r;
		}

		Double ret = StrUtil.toDouble(r);
		if (digit==-1) {
    		// digit=-1 表示不需要精度
    		return String.valueOf(ret.doubleValue());
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
	
	public static ArrayList getSymbolsWithBracket(String str) {
	    // 去除空格
	    str = str.replaceAll(" ", "");
	    if (str.indexOf("+")==0)
	        str = str.substring(1); // 去掉开头的+号
	    ArrayList<String> list = new ArrayList<String>();
	    int curPos = 0;
	    int prePos = 0;
	    int len = str.length();
	    for (int i = 0; i < len; i++) {
	        char s = str.charAt(i);
	        
	        if (s == '+' || s == '-' || s == '*' || s == '/' || s=='(' || s==')' || s=='>' || s=='<' || s=='=') {
	            if (prePos<curPos) {
	            	//  检查是否为表单域，如果该域有算式，则展开算式
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
	
	/**
	 * 计算
	 * @Description: 
	 * @param fdao
	 * @param formula
	 * @return
	 */
	public boolean evalBoolean(IFormDAO fdao, String formula) throws ErrMsgException {
		// 将now替换为毫秒
		formula = formula.replaceAll("now", String.valueOf(System.currentTimeMillis()));

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
			    		LogUtil.getLog(getClass()).error(el + "不是字段！");
			    	}
			    	else {
			    		// 是否为函数型，递归，展开算式，继续从原子算式开始计算
			    		// 如：$calc(yz - ljzj -jzzb, 2)，而ljzj=$getMonthDiff(qyrq)*yzje
			    		if (ffSub.isFunc()) {
			    			ary[i] = FuncUtil.render(ffSub, ffSub.getDefaultValueRaw(), fdao);
			    		}
			    		else {
							String v = fdao.getFieldValue(el);
							if (ffSub.getType().equals(FormField.TYPE_DATE)) {
								v = String.valueOf(DateUtil.parse(v, "yyyy-MM-dd").getTime());
							}
							else if (ffSub.getType().equals(FormField.TYPE_DATE_TIME)) {
								v = String.valueOf(DateUtil.parse(v, "yyyy-MM-dd HH:mm:ss").getTime());
							}

							if ("".equals(v)) {
								ary[i] = "0";
							} else if (!StrUtil.isDouble(v)) {
								ary[i] = "0";
							} else {
								ary[i] = "(" + v + ")";
							}
			    		}
			    	}
				}
	        }
	    }
		
	    String formula2 = "";
	    for (int i=0; i < ary.length; i++) {
	    	formula2 += ary[i];
	    }

    	ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        try {
        	Boolean ret = (Boolean)engine.eval(formula2);
        	return ret.booleanValue();
        }
        catch (ScriptException ex) {
        	LogUtil.getLog(getClass()).error(ex);
        	LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        	throw new ErrMsgException(ex.getMessage());
        }		
        catch (ClassCastException e) {
			LogUtil.getLog(getClass()).error(e);
        	throw new ErrMsgException(e.getMessage());
        }
	}	

	public static boolean isOperator(String str) {
	    if (str.equals(">") || str.equals("<") || str.equals("=") || str.equals("+") || str.equals("*") || str.equals("/") || str.equals("-") || str.equals("(") || str.equals(")")) {
	        return true;
	    }
	    else
	        return false;
	}
	
	public ArrayList<String> getFieldsRelated(String[] func, FormDb fd) {
		String fieldName = func[1];		
		ArrayList<String> list = new ArrayList<String>();
		
		String[] params = StrUtil.split(func[1], ",");
		if (params==null || params.length<3) {
			LogUtil.getLog(getClass()).error("参数必须至少有3个！");
		}
		
		String exp = params[0];
		ArrayList<String> aryList = getSymbolsWithBracket(exp);
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
		
		// 无需考虑a或b为算子的情况，因为在FuncUtil中已处理
		
		String a = params[1];
	    if (!StrUtil.isDouble(a)) {
			aryList = getSymbolsWithBracket(a);
		    ary = aryList.toArray();
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
	    }
		
		String b = params[2];	
	    if (!StrUtil.isDouble(b)) {
			aryList = getSymbolsWithBracket(b);
		    ary = aryList.toArray();
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
	    }
	    
		return list;
	}	
}