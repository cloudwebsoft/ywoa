<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormMgr"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="org.json.*"%>
<%
/*
- 功能描述：嵌套表格2中删除行
- 访问规则：从nest_sheet_view.jsp中访问
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：2013-5-29
==================
- 修改者：
- 修改时间：2017-10-15
- 修改原因：优化，改为返回json
- 修改点：
*/
String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
String moduleCode = ParamUtil.get(request, "moduleCode");
if (moduleCode.equals("")) {
	moduleCode = formCodeRelated;
}
String action = ParamUtil.get(request, "action");
JSONObject json = new JSONObject();
if (action.equals("del")) {
	FormMgr fm = new FormMgr();
	FormDb fdRelated = fm.getFormDb(formCodeRelated);
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdRelated);
	try {
		boolean isNestSheet = true;
		if (fdm.del(request, isNestSheet, moduleCode)) {
			String mode = ParamUtil.get(request, "mode");

			// 如果不是在智能模块的添加页面删除，则刷新，否则如果刷新，添加的多条记录都会消失
			if (!mode.equals("delForTmpAdd")) {
			}
			
			json.put("ret", "1");
			json.put("msg", "操作成功！");
		}
		else {
			json.put("ret", "0");
			json.put("msg", "操作失败！");
		}
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		// e.printStackTrace();
	}
	out.print(json);
}
%>