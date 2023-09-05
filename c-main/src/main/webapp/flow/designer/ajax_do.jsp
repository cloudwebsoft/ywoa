<%@ page contentType="text/html; charset=utf-8"%><%@ page import = "java.util.*"%><%@ page import = "com.redmoon.oa.ui.*"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "cn.js.fan.web.*"%><%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
	if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
		// out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		// return;
	}

	String op = ParamUtil.get(request, "op");
	if (op.equals("new")) {
        FormQueryDb aqd = new FormQueryDb();
		String isSystem = ParamUtil.get(request, "isSystem");
		String queryName = "新建查询";
        aqd.setQueryName(queryName);
		aqd.setSaved(false);
        aqd.setUserName(privilege.getUser(request));
		aqd.setTimePoint(new java.util.Date());
		aqd.setSystem(isSystem.equals("true"));
        boolean re = aqd.create();
		response.setContentType("text/plain");
		String data = "{\"re\":\"" + re + "\", \"name\":\"" + queryName + "\", \"id\":\"" + aqd.getId() + "\"}";
		data = "[" + data + "]";
		out.print(data);
	}
    else if (op.equals("modifyConditionFieldCode")) {
		boolean re = false;
		String msg = "";
		try {
			FormQueryMgr aqm = new FormQueryMgr();
			re = aqm.modifyConditionFieldCode(request);
		}
		catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
			msg = e.getMessage();
		}
		response.setContentType("text/plain"); 
		String data = "{\"re\":\"" + re + "\", \"msg\":\"" + msg + "\"}";
		out.print(data);
	}
	else if (op.equals("modifyOthers")) {
        boolean re = false;
		String msg = "";
		
        int id = ParamUtil.getInt(request, "id");
        //ajax提交，name乱码问题
	String queryName = request.getParameter("queryName");//ParamUtil.get(request, "queryName");
		
        String deptCodes = ParamUtil.get(request, "deptCodes");
		
		String orderFieldCodes = ParamUtil.get(request, "orderFieldCodes");
		String formCode = ParamUtil.get(request, "formCode");
        String queryRelated = ParamUtil.get(request, "queryRelated");
		
		// String[] calcFieldCodes = ParamUtil.getParameters(request, "calcFieldCode");
		// String[] calcFuncs = ParamUtil.getParameters(request, "calcFunc");
		String statDesc = ParamUtil.get(request, "statDesc");
		int flowStatus = ParamUtil.getInt(request, "flowStatus", -1);

        java.util.Date flowBeginDate1 = DateUtil.parse(ParamUtil.get(request, "flowBeginDate1"), "yyyy-MM-dd");
        java.util.Date flowBeginDate2 = DateUtil.parse(ParamUtil.get(request, "flowBeginDate2"), "yyyy-MM-dd");
				
		if (msg.equals("")) {
			FormQueryDb aqd = new FormQueryDb();
			aqd = aqd.getFormQueryDb(id);
			aqd.setQueryName(queryName);
			aqd.setOrderFieldCode(orderFieldCodes);
			aqd.setTableCode(formCode);
			aqd.setSaved(true);
			aqd.setQueryRelated(queryRelated);
			aqd.setStatDesc(statDesc);
			aqd.setFlowStatus(flowStatus);
			aqd.setFlowBeginDate1(flowBeginDate1);
			aqd.setFlowBeginDate2(flowBeginDate2);
					
			/*
			if (calcFieldCodes!=null) {
				String str = "";
				int len = calcFieldCodes.length;
				for (int i=0; i<len; i++) {
					if (calcFieldCodes[i]=="")
						continue;
					if (str=="")
						str = "\"" + calcFieldCodes[i] + "\":\"" + calcFuncs[i] + "\"";
					else
						str += ",\"" + calcFieldCodes[i] + "\":\"" + calcFuncs[i] + "\"";
				}
				str = "{" + str + "}";
			}
			*/
			
			re = aqd.save();
		}
		response.setContentType("text/plain"); 
		String data = "{\"re\":\"" + re + "\", \"msg\":\"" + msg + "\"}";
		out.print(data);
	}
	else if (op.equals("del")) {
		boolean re = false;
		String msg = "";
		try {
			FormQueryMgr aqm = new FormQueryMgr();			
			re = aqm.del(request);
		}
		catch (ErrMsgException e) {
			msg = e.getMessage();
		}
		response.setContentType("text/plain"); 
		String data = "{\"re\":\"" + re + "\", \"msg\":\"" + msg + "\"}";
		out.print(data);
	}
%>