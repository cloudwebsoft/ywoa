<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDAO"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
 * �������<select>...</select>����outHTML�滻����ʹ��livevaidtion���¼���ʧ
*/
String province = ParamUtil.get(request, "province");
boolean isCity = ParamUtil.get(request, "isCity").equals("true");
boolean isCountry = ParamUtil.get(request, "isCountry").equals("true");

String rid = ParamUtil.get(request, "rid");
String cityId = ParamUtil.get(request, "cityId");
String countryId = ParamUtil.get(request, "countryId");

String firstCity = ParamUtil.get(request, "city");

if (province.equals("")) {
	// ��ѡ���˳���ʱ
	String city = ParamUtil.get(request, "city");
	if (isCountry && !city.equals("")) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql =
				"select parent_id from oa_china_region where region_id=" +
				city;
		int parent_id = -1;
		ResultIterator ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			parent_id = rr.getInt(1);
		}
		%>
		<%
		sql = "select region_id,region_name from oa_china_region where region_type=2 and parent_id=" + parent_id
			  + " order by region_id";
		ri = jt.executeQuery(sql);
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			if (rr.getString(1).equals(city)) {
				out.print("<option value='" + rr.getInt(1) + "' selected>" +
						rr.getString(2) +
						"</option>");			
			}
			else {
				out.print("<option value='" + rr.getInt(1) + "'>" +
						rr.getString(2) +
						"</option>");
			}
		}
		%>
		|
		<%
		sql = "select region_id,region_name from oa_china_region where region_type=3 and parent_id=" + city
			  + " order by region_id";
		ri = jt.executeQuery(sql);
		%>	
		<%
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			out.print("<option value='" + rr.getInt(1) + "'>" +
					rr.getString(2) +
					"</option>");
		}
		%>
		<%
	}
}
else {
	if (isCity && !province.equals("")) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select region_id,region_name from oa_china_region where region_type=2 and parent_id=" + province + " order by region_id";
		ResultIterator ri = jt.executeQuery(sql);
		%>
		<%
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			if (firstCity.equals(""))
				firstCity = ""+rr.getInt(1);
		%>
		<option value="<%=rr.getInt(1)%>"><%=rr.getString(2)%></option>
		<%
		}
		%>
	<%}%>|<%
	if (isCity && isCountry && !province.equals("")) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select region_id,region_name from oa_china_region where region_type=3 and parent_id=" + firstCity;
		ResultIterator ri = jt.executeQuery(sql);
		%>
		<%
		while (ri.hasNext()) {
			ResultRecord rr = ri.next();
		%>
		<option value="<%=rr.getInt(1)%>"><%=rr.getString(2)%></option>
		<%
		}
		%>
	<%}
}%>