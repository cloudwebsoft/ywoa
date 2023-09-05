package com.redmoon.oa.visual.func;

import java.text.DecimalFormat;
import java.util.ArrayList;

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
import com.redmoon.oa.visual.FuncUtil;

/**
 * 连接字符串 $connStr(a+b+1)
 * @Description: 
 * @author: 
 * @Date: 2017-9-10下午09:20:05
 */
public class ConnStrFuncImpl implements IFuncImpl {

	@Override
	public String func(IFormDAO fdao, String[] func) throws ErrMsgException {
		String formula = func[1];
		return doConn(fdao, formula);
	}
	
	public static String doConn(IFormDAO fdao, String formula) {
		boolean hasField = false;
		boolean hasPlus = false;
		ArrayList<String> aryList = CalculateFuncImpl.getSymbolsWithBracket(formula);
	    Object[] ary = aryList.toArray();
		for (int i=0; i<ary.length; i++) {
			String el = (String)ary[i];
	        if (!"+".equals(el)) {
		    	// 判断el是否为表单域，如果不是，则可能为含有函数或表达式，则先计算此算式
		    	FormField ffSub = fdao.getFormDb().getFormField(el);
		    	if (ffSub==null) {
		    		;
		    	}
		    	else {
		    		hasField = true;
		    		// 是否为函数型，递归，展开算式，继续从原子算式开始计算
		    		// 如：$calc(yz - ljzj -jzzb, 2)，而ljzj=$getMonthDiff(qyrq)*yzje
		    		if (ffSub.isFunc()) {
		    			ary[i] = FuncUtil.render(ffSub, ffSub.getDefaultValueRaw(), fdao);
		    		}
		    		else {
						String v = StrUtil.getNullStr(fdao.getFieldValue(el));
						ary[i] = v;
		    		}
		    	}
	        }
	        else {
	        	hasPlus = true;
	        }
	    }
		
	    formula = "";
	    for (int i=0; i < ary.length; i++) {
	        formula += ary[i];
	    }
		
	    // 如果无+号，则有可能为“否”这样的字符串，再用scriptengine来eval会报错
		if (!hasPlus) {
			return formula;
		}

    	ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        try {
        	// LogUtil.getLog(ConnStrFuncImpl.class).info("formula=" + formula);
        	return String.valueOf(engine.eval(formula));
        }
        catch (ScriptException ex) {
        	LogUtil.getLog(ConnStrFuncImpl.class.getName()).info("doConn: error formula=" + formula);
        	LogUtil.getLog(ConnStrFuncImpl.class.getName()).info(StrUtil.trace(ex));
        	LogUtil.getLog(ConnStrFuncImpl.class).error(ex);
        	return formula;
        }
	}
	
	@Override
	public ArrayList<String> getFieldsRelated(String[] func, FormDb fd) {
		String[] params = StrUtil.split(func[1], ",");
		String formula = params[0];
		return getFieldsRelated(fd, formula);
	}	
	
	public static ArrayList<String> getFieldsRelated(FormDb fd, String formula) {
		ArrayList<String> list = new ArrayList<String>();		
		ArrayList<String> aryList = CalculateFuncImpl.getSymbolsWithBracket(formula);		
	    Object[] ary = aryList.toArray();
		for (Object o : ary) {
			String el = (String) o;
			if (!"+".equals(el)) {
				FormField ffSub = fd.getFormField(el);
				if (ffSub != null) {
					list.add(ffSub.getName());
				}
			}
		}
		return list;		
	}
	
}
