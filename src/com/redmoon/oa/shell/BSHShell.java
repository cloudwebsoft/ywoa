package com.redmoon.oa.shell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import bsh.EvalError;
import bsh.Interpreter;
import com.redmoon.oa.sys.DebugUtil;

public class BSHShell {
    Interpreter bsh;
    Console console;
    
    boolean error;
        
    public BSHShell() {
    	bsh = new Interpreter();
    	console = new Console();
    	error = false;
    	try {
			bsh.set("console", console);
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public Interpreter getInterpreter() {
    	return bsh;
    }
    
    public void set(String objName, Object obj) {
    	try {
			bsh.set(objName, obj);
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public boolean eval(String script) {
    	ByteArrayOutputStream baoStream = new ByteArrayOutputStream(1024);
    	PrintStream myStream = new PrintStream(baoStream);
    	PrintStream oldStream = System.out;
    	System.setOut(myStream); // 重定向

        try {
			bsh.eval(script);
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			DebugUtil.e(getClass(), "eval script", script);
			DebugUtil.e(getClass(), "eval", StrUtil.trace(e));
			error = true;
			console.log(StrUtil.trace(e));
		}
		finally {
	    	// System.out.println("System.out.println:test");
	    	System.setOut(oldStream);	// 还原到控制台输出
	    	console.log(baoStream.toString());
	    	
	    	// 将脚本中log的内容输出至console
	    	System.out.println(console.getLogs());
		}
		return true;
    }
    
    public Object get(String objName) {
    	try {
			return bsh.get(objName);
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			console.log(e.getMessage());			
		}
		return null;
    }
    
    public Console getConsole() {
    	return console;
    }
    
    public String getLogs() {
    	return console.getLogs();
    }
    
    public String getLogDesc() {
    	return console.getLogDesc();
    }
    
    public boolean isError() {
    	return error;
    }
}
