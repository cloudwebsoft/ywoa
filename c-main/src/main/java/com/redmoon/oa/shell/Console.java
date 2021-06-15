package com.redmoon.oa.shell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.js.fan.util.StrUtil;
import bsh.Interpreter;

public class Console {
   
    private StringBuilder sbLog;

    private StringBuilder sbError;
    
    public Console() {
    	sbLog = new StringBuilder();
		sbError = new StringBuilder();
    }
    
    public void log(String str) {
    	sbLog.append(str).append("\r\n");
    }

    public void error(String str) {
    	sbLog.append("error:").append(str).append("\r\n");
    	sbError.append(str).append("\r\n");
	}

	public String getErrors() {
    	return sbError.toString();
	}
    
    public String getLogs() {
    	return sbLog.toString();
    }
    
    public String getLogDesc() {
    	String str = sbLog.toString().trim();
    	
    	// •Sourced file: inline evaluation of: ``import com.redmoon.oa.flow.*; import com.cloudwebsoft.framework.db.*; import cn. . . . '' : Typed variable declaration : at Line: 23 : in file: inline evaluation of: ``import com.redmoon.oa.flow.*; import com.cloudwebsoft.framework.db.*; import cn. . . . '' : fu .getFieldValue ( "sqbm" ) Target exception: java.lang.NullPointerException: Null Pointer in Method Invocation at bsh.UtilTargetError.toEvalError(Unknown Source) at bsh.UtilEvalError.toEvalError(Unknown Source) at bsh.BSHMethodInvocation.eval(Unknown Source) at bsh.BSHPrimaryExpression.eval(Unknown Source) at bsh.BSHPrimaryExpression.eval(Unknown Source) at bsh.BSHVariableDeclarator.eval(Unknown Source) at bsh.BSHTypedVariableDeclaration.eval(Unknown Source) at bsh.Interpreter.eval(Unknown Source) at bsh.Interpreter.eval(Unknown Source) at bsh.Interpreter.eval(Unknown Source) at com.redmoon.oa.shell.BSHShell.eval(BSHShell.java:41) at com.redmoon.oa.flow.FormDAOMgr.runValidateScript(FormDAOMgr.java:795) at com.redmoon.oa.flow.FormDAOMgr.runValidateScript(FormDAOMgr.java:752) at org.apache.jsp.flow.flow_005fdo_jsp._jspService(flow_005fdo_jsp.java:237) at org.apache.jasper.runtime.HttpJspBase.service(HttpJspBase.java:70) at javax.servlet.http.HttpServlet.service(HttpServlet.java:723) at org.apache.jasper.servlet.JspServletWrapper.service(JspServletWrapper.java:388) at org.apache.jasper.servlet.JspServlet.serviceJspFile(JspServlet.java:313) at org.apache.jasper.servlet.JspServlet.service(JspServlet.java:260) at javax.servlet.http.HttpServlet.service(HttpServlet.java:723) at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:290) at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206) at org.apache.struts2.dispatcher.ActionContextCleanUp.doFilter(ActionContextCleanUp.java:102) at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:235) at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206) at com.cloudwebsoft.framework.security.ProtectFilter.doFilter(ProtectFilter.java:61) at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:235) at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206) at com.redmoon.oa.DownloadFilter.doFilter(DownloadFilter.java:139) at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:235) at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206) at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:233) at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:191) at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:127) at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:103) at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:109) at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:293) at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:861) at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:620) at org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:489) at java.lang.Thread.run(Thread.java:662)

   		String pat = "at Line: ([0-9]*) :";
		Pattern p = Pattern.compile(pat, Pattern.DOTALL	| Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str); // "<textarea style=\"color\" name=ss' id=\"color\" title=hh' style=\"color\" value=蓝风>onclick=func</textarea>");
		if (m.find()) {
			String lineNum = m.group(1);
			
			pat = "Target exception: (.*?):";
			p = Pattern.compile(pat, Pattern.DOTALL	| Pattern.CASE_INSENSITIVE);
			m = p.matcher(str);
			if (m.find()) {
				String ex = m.group(1);
				str = "第" + lineNum + "行出错：" + ex;
			}
		}
		
    	return str;
    }

}
