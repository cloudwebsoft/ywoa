<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "java.util.*"%>
<%@ page import = "java.util.Date"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<%@ page import = "com.redmoon.oa.kaoqin.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.StrUtil"%>
<%@ page import = "cn.js.fan.util.ParamUtil"%>
<%@ page import = "cn.js.fan.util.ErrMsgException"%>
<%@ page import = "cn.js.fan.util.DateUtil"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="java.text.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "read")) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");	
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String op = ParamUtil.get(request, "op");
	if(op.equals("checkName")) {
		boolean re = false;
		try {
		    String name = ParamUtil.get(request, "name");	
			String sql = "select id from form_table_sales_customer where customer="+StrUtil.sqlstr(name);			
            JdbcTemplate jt = new JdbcTemplate();		   
		    ResultIterator ri = jt.executeQuery(sql);	
			ResultRecord rr = null;
			if (ri.hasNext()) {
				// 当传入id参数时，说明是在编辑页面
				long id = ParamUtil.getLong(request, "id", -1);
				if (id!=-1) {
					rr = (ResultRecord)ri.next();
					// 如果编辑时，根据输入的公司名称取其数据库中的ID，如果该ID等于页面中传入的ID参数，则说明判断成功
					if (id==rr.getLong(1)) {
						JSONObject json = new JSONObject();
						json.put("ret", "1");
						out.print(json);
						return;				
					}
				}
			    JSONObject json = new JSONObject();
				json.put("ret", "0");
				out.print(json);
				return;				
			}		
		} catch (java.sql.SQLException e) {
			JSONObject json = new JSONObject();
			json.put("ret", "1");
			json.put("msg", e.getMessage());
			out.print(json);
			return;
		}
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		out.print(json);
		return;
	}
%>
