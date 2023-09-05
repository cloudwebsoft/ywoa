package com.redmoon.oa.shell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import bsh.TargetError;
import cn.js.fan.web.Global;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import bsh.EvalError;
import bsh.Interpreter;
import com.redmoon.oa.flow.WorkflowDb;
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
			LogUtil.getLog(getClass()).error(e);
		}
    }
    
    public Interpreter getInterpreter() {
    	return bsh;
    }
    
    public void set(String objName, Object obj) {
    	try {
			bsh.set(objName, obj);
		} catch (EvalError e) {
			LogUtil.getLog(getClass()).error(e);
		}
    }
    
    public boolean eval(String script) throws ErrMsgException {
		/*ByteArrayOutputStream baoStream = new ByteArrayOutputStream(1024);
		PrintStream myStream = new PrintStream(baoStream);

		PrintStream oldStream = System.out;
    	System.setOut(myStream); // 输出重定向*/

        try {
			bsh.eval(script);
		} catch (EvalError e) {
			LogUtil.getLog(getClass()).error("scene: " + StrUtil.getNullStr((String)get(ConstUtil.SCENE)));
			if (e instanceof TargetError) {
				TargetError err = (TargetError)e;
				LogUtil.getLog(getClass()).error(err);
				if (err.getTarget() instanceof IllegalArgumentException) {
					console.log(err.getMessage());
					throw ((IllegalArgumentException)err.getTarget());
				} else if (err.getTarget() instanceof ErrMsgException) {
					console.log(err.getMessage());
					throw (ErrMsgException)err.getTarget();
				} else if (err.getTarget() instanceof ClassCastException) {
					throw ((ClassCastException)err.getTarget());
				}
			} else {
				LogUtil.getLog(getClass()).error(e);
			}
			// DebugUtil.e(getClass(), "eval script", script);

			error = true;
			console.log(e.getMessage());
			throw new ErrMsgException("脚本运行错误：" + console.getLogDesc());
		}
		finally {
	    	// System.setOut(oldStream);	// 还原控制台输出，无效！！！
	    	// console.log(baoStream.toString());
	    	
	    	// 将脚本中log的内容输出至console
			Integer flowId = (Integer)get("flowId");
			String scene = StrUtil.getNullStr((String)get(ConstUtil.SCENE));
			String desc = "scene " + scene;
			if (flowId != null) {
				if (Global.getInstance().isDebug()) {
					WorkflowDb wf = new WorkflowDb();
					wf = wf.getWorkflowDb(flowId);
					desc = "flowId: " + flowId + " " + wf.getTitle() + " " + desc;
				}
				else {
					desc = "flowId: " + flowId + " " + desc;
				}
			}
			if (!"".equals(console.getLogs().trim())) {
				DebugUtil.i(getClass(), desc, console.getLogs().trim());
			}
			if (!"".equals(console.getErrors().trim())) {
				DebugUtil.e(getClass(), desc, console.getErrors().trim());
			}
		}
		return true;
    }
    
    public Object get(String objName) {
    	try {
			return bsh.get(objName);
		} catch (EvalError e) {
			LogUtil.getLog(getClass()).error(e);
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
