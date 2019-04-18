<%@ page contentType="text/html; charset=utf-8" %><%@ page import="com.redmoon.oa.fileark.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="com.redmoon.kit.util.*"%><%@ page import="com.redmoon.oa.flow.*"%><%@ page import = "org.json.*"%>
<%@page import="com.redmoon.oa.ui.LocalUtil"%><%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%><%
	WorkflowMgr wfm = new WorkflowMgr();
    try {
		int ret = wfm.writeDocument(application, request);
		if (ret!=-1){
			String str = LocalUtil.LoadString(request,"res.common","RET_SUCCESS");
		    out.print(str);
	    }
		else {
			String str = LocalUtil.LoadString(request,"res.common","RET_FAIL");
			out.print(str);
		}
	}
	catch (ErrMsgException e) {
		out.print(e.getMessage());
	}	
%>