<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.Properties" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>流程处理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="inc/common.js"></script>
</head>
<body>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
String action = ParamUtil.get(request, "action");
com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
action = cn.js.fan.security.ThreeDesUtil.decrypthexstr(ssoCfg.getKey(), action);

String[] ary = StrUtil.split(action, "\\|");
if (ary==null) {
	out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
	return;
}
int len = ary.length;
Map map = new HashMap();
for (int i = 0; i < len; i++) {
	String[] pair = ary[i].split("=");
	// System.out.println(getClass() + " " + pair[0] + " " + pair[1]);
	if (pair.length == 2)
		map.put(pair[0], pair[1]);
}

String userName = StrUtil.getNullStr((String)map.get("userName"));

UserDb user = new UserDb();
user = user.getUserDb(userName);

if (!user.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "用户不存在！"));
	return;
}

if (!user.isValid()) {
	out.print(SkinUtil.makeErrMsg(request, "用户非法！"));
	return;
}

privilege.doLogin(request, user.getName(), user.getPwdMD5());

// 根据皮肤重定向
String url = "";
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();	
boolean isSpecified = cfg.get("styleMode").equals("2"); 
// 指定风格
if (isSpecified) {
	int styleSpecified = StrUtil.toInt(cfg.get("styleSpecified"), -1);
	if (styleSpecified!=-1) {
		if (styleSpecified==UserSetupDb.UI_MODE_PROFESSION) {
			// response.sendRedirect("oa.jsp" + queryStr);
			url = "oa.jsp";
		} else if (styleSpecified==UserSetupDb.UI_MODE_FLOWERINESS) {
			// response.sendRedirect("mydesktop.jsp" + queryStr);
			url = "mydesktop.jsp";					
		} else if (styleSpecified == UserSetupDb.UI_MODE_PROFESSION_NORMAL) {
			// 经典型传统菜单
			url = "oa_main.jsp";
		}
		else if (styleSpecified == UserSetupDb.UI_MODE_LTE) {
			url = "lte/index.jsp";
		}				
		else {
			url = "main.jsp";
		}
	}
}
else {
	String os = ParamUtil.get(request, "os");
	// Safari
	if (os.equals("4")) {			
		url = "main.jsp";
	}	
	else {
		UserSetupDb usd = new UserSetupDb();
		usd = usd.getUserSetupDb(privilege.getUser(request));
		if (usd.getUiMode()==UserSetupDb.UI_MODE_NONE) {
			if (usd.getMenuMode()==UserSetupDb.MENU_MODE_NEW) {
				url = "oa.jsp";
			}
			else {
				url = "oa_main.jsp";
			}			
		}
		else if (usd.getUiMode()==UserSetupDb.UI_MODE_PROFESSION) {
			if (usd.getMenuMode()==UserSetupDb.MENU_MODE_NEW) {
				url = "oa.jsp";
			}
			else {
				url = "oa_main.jsp";
			}		
		}
		else if (usd.getUiMode() == UserSetupDb.UI_MODE_LTE) {
			url = "lte/index.jsp";
		}				
		else if (usd.getUiMode()==UserSetupDb.UI_MODE_FLOWERINESS) {
			url = "mydesktop.jsp";
	}
	else
		url = "main.jsp";
	}
}
	
String strMyActionId = StrUtil.getNullStr((String)map.get("myActionId"));
String strFlowId = StrUtil.getNullStr((String)map.get("flowId"));
String op = StrUtil.getNullStr((String)map.get("op"));

if (op.equals("show")) {
	// WorkflowMgr wfm = new WorkflowMgr();
	// WorkflowDb wf = wfm.getWorkflowDb(StrUtil.toInt(strFlowId));
	
	WorkflowDb wf = new WorkflowDb(StrUtil.toInt(strFlowId));
	
	WorkflowPredefineDb wfp = new WorkflowPredefineDb();
	wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

	String mainPage;
	
	if (wfp.isLight()) {
		mainPage = "flow_dispose_light_show.jsp?flowId=" + strFlowId;
	} else {
		mainPage = "flow_modify.jsp?flowId=" + strFlowId; // wf.getId();
	}
	
	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("查看流程") + "&mainPage=" + mainPage);
}
else {
	long myActionId = StrUtil.toLong(strMyActionId);
	MyActionDb mad = new MyActionDb();
	mad = mad.getMyActionDb(myActionId);
	if (!mad.isLoaded()) {
		out.print(SkinUtil.makeErrMsg(request, "动作不存在！"));
		return;
	}
	
	// System.out.println(userName + " ddd" + mad.getUserName());
	
	if (!mad.getUserName().equals(userName)) {
		out.print(SkinUtil.makeErrMsg(request, "动作与处理者不一致！"));
		return;
	}
	WorkflowMgr wfm = new WorkflowMgr();
	WorkflowDb wf = wfm.getWorkflowDb((int)mad.getFlowId());
	com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
	lf = lf.getLeaf(wf.getTypeCode());
	
	String mainPage;
	
	WorkflowPredefineDb wfp = new WorkflowPredefineDb();
	wfp = wfp.getPredefineFlowOfFree(lf.getCode());
	
	if (wfp.isLight()) {
		mainPage = "flow_dispose_light.jsp?myActionId=" + myActionId;
	} else if (lf.getType()==Leaf.TYPE_LIST) {
		mainPage = "flow_dispose.jsp?myActionId=" + myActionId;
	} else {
		mainPage = "flow_dispose_free.jsp?myActionId=" + myActionId;
	}
	
	// request.getRequestDispatcher("../oa.jsp?mainTitle=" + StrUtil.UrlEncode("待办流程") + "&mainPage=flow_dispose.jsp?myActionId=" + myActionId).forward(request, response);

	response.sendRedirect("../" + url + "?mainTitle=" + StrUtil.UrlEncode("待办流程") + "&mainPage=" + mainPage);
}
%>
</body>
</head>