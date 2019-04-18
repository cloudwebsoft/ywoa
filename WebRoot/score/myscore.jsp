<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@page import="com.redmoon.oa.post.*"%>
<%@page import="com.redmoon.oa.visual.FormDAO"%>
<%@page import="com.redmoon.oa.pointsys.*"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="cn.js.fan.security.ThreeDesUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String userName = privilege.getUser(request);
int year = ParamUtil.getInt(request, "year", -1);
int month = ParamUtil.getInt(request, "month", -1); 
Calendar c1 = Calendar.getInstance();
if (year == -1){
	year = c1.get(Calendar.YEAR);
}
if (month == -1){
	month = c1.get(Calendar.MONTH) + 1;
}

PointBean pb = PointSystemUtil.getPointInit();

PointSystemConfig cfg = PointSystemConfig.getInstance();
int initYear = cfg.getIntProperty("initYear");

FormDAO fdao = new FormDAO();
FormDAO pfdao = new FormDAO();
String sql = "select id from form_table_pointsys_score_mon where cws_status=1 and user_name=" + StrUtil.sqlstr(userName) + " and p_year=" + year + " and p_mon=" + month;
try {
	Vector v = fdao.list("pointsys_score_mon", sql);
	Iterator it = v.iterator();
	if (it.hasNext()) {
		fdao = (FormDAO) it.next();
	}
} catch (Exception e) {
}

sql = "select id from form_table_personbasic where user_name=" + StrUtil.sqlstr(userName);
try {
	Vector v = pfdao.list("personbasic", sql);
	Iterator it = v.iterator();
	if (it.hasNext()) {
		pfdao = (FormDAO) it.next();
	}
} catch (Exception e) {
}

com.redmoon.forum.Config qcfg = com.redmoon.forum.Config.getInstance();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>我的积分</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<style type="text/css">
	span {
		display:-moz-inline-box;	/*下一句在某些浏览器中无效*/
		display:inline-block;
		width:120px; 
	}
</style>
<script>
function onTypeCodeChange(){
	 location.href = "myscore.jsp?year=" + $('#year').val() + "&month=" + $('#month').val();
}

function yearChange(isChange){
	var y = parseInt($('#year').val());
	if (isChange) {
		if (y == <%=initYear%>) {
			return;
		}
		location.href = "myscore.jsp?year=" + (y - 1) + "&month=" + $('#month').val();
	} else {
		if (y == <%=year%>) {
			return;
		}
		location.href = "myscore.jsp?year=" + (y + 1) + "&month=" + $('#month').val();
	}
}
function monthChange(isChange){
	var y = parseInt($('#year').val());
	var m = parseInt($('#month').val());
	if (isChange) {
		if (m == 1) {
			if (y == <%=initYear%>) {
				return;
			}
			m = 12;
			y--;
		} else {
			m--;
		}
	} else {
		if (m == 12) {
			if (y == <%=year%>) {
				return;
			}
			m = 1;
			y++;
		} else {
			m++;
		}
	}
	location.href = "myscore.jsp?year=" + year + "&month=" + m;
}
</script>
</head>
<body>
<div class="spacerH"></div>
<table width="98%" align="center" class="tabStyle_1 percent80" >
  <tr>
    <td class="tabStyle_1_title" align="left" >
	<a href="javascript:;" onclick="yearChange(true)"><img title="上一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="../plan/images/1.gif" /></a>
	&nbsp;
	<a href="javascript:;" onclick="monthChange(true)"><img title="上一月" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="../plan/images/4.gif" /></a>
	&nbsp;
	<select id="year" name="year" onChange="onTypeCodeChange()" >	
	<%
	   for(int i = initYear; i <= year; i++){
	     if (i == year) {
		 %>
		 <option value="<%=i%>" selected="selected" ><%=i%>年</option>
		<%}else{%>
			 <option value="<%=i%>"><%=i%>年</option>
		<%
		   }
	   }
	%>
    </select>  
	<select id="month" name="month" onchange="onTypeCodeChange()">
	<%
	for (int i = 1; i <= 12; i++) {
	  if (month == i) {
	%>
	<option value="<%=i%>" selected="selected"><%=i%>月</option>
	<%}else{%>
	<option value="<%=i%>"><%=i%>月</option>
	<%}
	}%>
	</select>
	&nbsp;
	<a href="javascript:;" onclick="monthChange(false)"><img title="下一月" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="../plan/images/3.gif" /></a>
	&nbsp;
	<a href="javascript:;" onclick="yearChange(false)"><img title="下一年" style="onmouseover=this.className='cws_BtnMouseOverUp'; onmouseout=this.className='cws_Btn'" src="../plan/images/2.gif" /></a>
	</td>
  </tr>
</table>
<table id="mainTable" width="98%" align="center" cellPadding="3" cellSpacing="0" class="tabStyle_1 percent80">
    <tr>
      <td width="100%" align="center" colspan=2 class="tabStyle_1_title">
      	我的<%=year %>年<%=month %>月积分详情
      </td>
	 </tr>
	<%if (fdao == null || !fdao.isLoaded()) {%>
	<tr>
		<td align="center" colspan=2>
			暂无积分详情
		</td>
	</tr>
	<%} else { %>
	   <tr>
    	<td width="40%" align="right">
			<b>A分：</b>
		</td>
		<td><b><%=fdao.getFieldValue("point_a") %></b></td>
		</tr>
	   <tr>
		<td width="40%" align="right">
    		工龄：
    	</td>
    	<td>
			<%
			Date date = DateUtil.parse(pfdao.getFieldValue("employed"), "yyyy-MM-dd");
			int years = DateUtil.datediff(new Date(), date) / 365;
			out.print(years);
			%>
		</td>
		</tr>
	   <tr>
		<td width="40%" align="right">
    		工龄积分：
    	</td>
    	<td>
			<%=fdao.getFieldValue("score_employed") %>
		</td>
		</tr>
	   <tr>
		<td width="40%" align="right">
    		学历：
    	</td>
    	<td>
			<%
			int eduId = StrUtil.toInt(StrUtil.getNullStr(pfdao.getFieldValue("education")), 0);
			if (eduId > 0) {
				FormDb fd = new FormDb("pointsys_education");
				FormDAO edudao = new FormDAO(eduId, fd);
				if (edudao.isLoaded()) {
					out.print(edudao.getFieldValue("education"));
				}
			}
			%>
		</td>
		</tr>
	   <tr>
		<td width="40%" align="right">
    		学历积分：
    	</td>
    	<td>
			<%=fdao.getFieldValue("score_edu") %>
		</td>
		</tr>
		<%if (pb.isPostInit()) { %>
	   <tr>
		<td width="40%" align="right">
    		岗位：
    	</td>
    	<td>
			<%
			PostUserMgr puMgr = new PostUserMgr();
			puMgr.setUserName(userName);
			PostUserDb pudb = puMgr.postByUserName();
			if (pudb != null && pudb.isLoaded()) {
				PostDb pdb = new PostDb();
				pdb = pdb.getPostDb(pudb.getInt("post_id"));
				if (pdb != null && pdb.isLoaded()) {
					out.print(pdb.getString("name"));
				}
			}
			%>
		</td>
		</tr>
		<tr>
		<td width="40%" align="right">
    		岗位积分：
    	</td>
    	<td>
			<%=fdao.getFieldValue("score_job") %>
		</td>
		</tr>
		<%} %>
		<%if (pb.isScoreAssessInit()) { %>
		 <tr>
		<td width="40%" align="right">
			<%
			int y = year;
			int m = month;
			if (m - 1 == 0) {
				m = 12;
				y--;
			} else {
				m--;
			}
			PostAssessBean pab = PointSystemUtil.getAssessScore(userName, y, m);
			%>
    		<%=m %>月岗位考核成绩：
    	</td>
    	<td>
			<span><%=fdao.getFieldValue("assess_score") %></span>
			<%if (pab != null) { %>
			<a href='javascript:;' onclick="addTab('<%=m %>月岗位考核流程','<%=Global.getFullRootPath(request) %>/flow_modify.jsp?flowId=<%=pab.getFlowId() %>')">查看流程</a>
			<%} %>
		</td>
		</tr>
		<%} %>
	   <tr>
		<td width="40%" align="right">
    		<b>B分：</b>
		</td>
		<td><b><%=fdao.getFieldValue("point_b") %></b></td>
		</tr>
		<tr>
		<td width="40%" align="right">
    		B固定积分：
    	</td>
    	<td>
			<%=fdao.getFieldValue("score_fixed") %>
		</td>
		</tr>
		<tr>
		<td width="40%" align="right">
    		奖分：
    	</td>
    	<td>
			<span><%=fdao.getFieldValue("point_b_plus") %></span>
			<%
			String qsql = "select id from form_table_score_reported where cws_status=1 and is_plus=1 and target="
				+ StrUtil.sqlstr(userName)
				+ " and " + SQLFilter.year("cur_time") + "=" + year
				+ " and " + SQLFilter.month("cur_time") + "=" + month;
			%>
			<a href='javascript:;' onclick="addTab('<%=month %>月奖分详情','<%=Global.getFullRootPath(request) %>/visual/module_list.jsp?code=score_reported&formCode=score_reported&query=<%=ThreeDesUtil.encrypt2hex(qcfg.getKey(), qsql) %>')">查看详情</a>
		</td>
		</tr>
		<tr>
		<td width="40%" align="right">
    		扣分：
    	</td>
    	<td>
			<span><%=fdao.getFieldValue("point_b_minus") %></span>
			<%
			qsql = "select id from form_table_score_reported where cws_status=1 and is_plus=0 and target="
				+ StrUtil.sqlstr(userName)
				+ " and " + SQLFilter.year("cur_time") + "=" + year
				+ " and " + SQLFilter.month("cur_time") + "=" + month;
			%>
			<a href='javascript:;' onclick="addTab('<%=month %>月扣分详情','<%=Global.getFullRootPath(request) %>/visual/module_list.jsp?code=score_reported&formCode=score_reported&query=<%=ThreeDesUtil.encrypt2hex(qcfg.getKey(), qsql) %>')">查看详情</a>
		</td>
		</tr>
		<tr>
		<td width="40%" align="right">
    		已兑换积分：
    	</td>
    	<td>
			<span><%=fdao.getFieldValue("used_point") %></span>
			<%
			qsql = "select id from form_table_integration_exchan where cws_status=1 and user_name="
				+ StrUtil.sqlstr(userName);
			%>
			<a href='javascript:;' onclick="addTab('积分兑换详情','<%=Global.getFullRootPath(request) %>/visual/module_list.jsp?code=integration_exchan&formCode=integration_exchan&query=<%=ThreeDesUtil.encrypt2hex(qcfg.getKey(), qsql) %>')">查看详情</a>
		</td>
		</tr>
	   <tr>
		<td width="40%" align="right">
    		<b><%=month %>月积分：</b>
    	</td>
    	<td>
			<b><%=fdao.getFieldValue("score_mon") %></b>
		</td>
    </tr>
    <tr>
		<td width="40%" align="right">
    		<b>累计积分：</b>
    	</td>
    	<td>
			<b><%=fdao.getFieldValue("score_all") %></b>
		</td>
    </tr>
    <%} %>
</table>
</body>
</html>