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
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/html;charset=utf-8");

	if (!privilege.isUserPrivValid(request, "read")) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");	
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	String op = ParamUtil.get(request, "op");
	if(op.equals("getMoney")) {
		boolean re = false;
		try {
		    String bz = ParamUtil.get(request, "bz");	
			String num = ParamUtil.get(request, "num");	
			double money_result = 0;
			double num_result = StrUtil.toDouble(num);
			
			JdbcTemplate jt = new JdbcTemplate();
			String sql = "select rate from rate_manage where id=?";
			ResultIterator ri = jt.executeQuery(sql, new Object[]{bz});	
			float rate = 0;
			if (ri.hasNext()) {
				ResultRecord rr = ri.next();
				rate = rr.getFloat("rate");
				money_result = num_result* rate;
				String money_result_sring = NumberUtil.roundRMB(money_result);
				//System.out.println(money_result_sring);
				//money_result = Math.abs(money_result).toFixed(2);
				JSONObject json = new JSONObject();
				json.put("ret", "1");
				json.put("money_result", money_result_sring);				
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
		json.put("ret", "0");
		json.put("msg", "汇率无法计算！");
		out.print(json);
		
		return;
	}
%>