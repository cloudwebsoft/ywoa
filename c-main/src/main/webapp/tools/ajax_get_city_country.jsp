<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String province = ParamUtil.get(request, "province");
boolean isCity = ParamUtil.get(request, "isCity").equals("true");
boolean isCountry = ParamUtil.get(request, "isCountry").equals("true");
String firstCity = ParamUtil.get(request, "city");

// 防SQL注入	
if (!cn.js.fan.db.SQLFilter.isValidSqlParam(province)) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ tools/ajax_get_city_country.jsp province=" + province);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
	return;
}

if (!cn.js.fan.db.SQLFilter.isValidSqlParam(firstCity)) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ tools/ajax_get_city_country.jsp firstCity=" + firstCity);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
	return;
}

if (province.equals("")) {
	// 当选择了城市时
	String city = ParamUtil.get(request, "city");
	if (!cn.js.fan.db.SQLFilter.isValidSqlParam(city)) {
		com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ tools/ajax_get_city_country.jsp city=" + city);
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
		return;
	}
	
	if (isCountry) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql =
				"select parent_id from sq_china_region where region_id=" +
				city;
		int parent_id = -1;
		ResultIterator ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord) ri.next();
			parent_id = rr.getInt(1);
		}
		%>
		<select id="city_id" name="city_id" onChange="if (this.value!='') ajaxShowCityCountry('', this.value)">		
		<%
		sql = "select region_id,region_name from sq_china_region where region_type=2 and parent_id=" + parent_id
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
		</select>|<select id="country_id" name="country_id">	
		<%
		sql = "select region_id,region_name from sq_china_region where region_type=3 and parent_id=" + city
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
		</select>
		<%
	}
}
else {
	if (isCity && !province.equals("")) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select region_id,region_name from sq_china_region where region_type=2 and parent_id=" + province + " order by region_id";
		ResultIterator ri = jt.executeQuery(sql);
		%>
		<select id="city_id" name="city_id" onChange="if (this.value!='') ajaxShowCityCountry('', this.value)">
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
		</select>
	<%}%>|<select id="country_id" name="country_id">
	<%
	if (isCity && isCountry && !province.equals("") && !firstCity.equals("")) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "select region_id,region_name from sq_china_region where region_type=3 and parent_id=" + firstCity;
		ResultIterator ri = jt.executeQuery(sql);
		%>
		<%
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
		%>
		<option value="<%=rr.getInt(1)%>"><%=rr.getString(2)%></option>
		<%
		}
		%>
	<%}%>
	</select>
<%}%>