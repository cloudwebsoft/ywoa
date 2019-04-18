<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.oa.notice.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
long id = ParamUtil.getLong(request, "id", -1);
int queryId = ParamUtil.getInt(request, "query_id", -1);
String title = "";
String content = "";
if (id!=-1) {
	FormQueryReportDb rd = new FormQueryReportDb();
	rd = (FormQueryReportDb)rd.getQObjectDb(new Long(id));
	queryId = rd.getInt("query_id");
	title = rd.getString("title");
	content = rd.getString("content");
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>报表设计器框架-右侧</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/designer.css" />
<script type="text/javascript" src="inc/common.js"></script>
<script type="text/javascript" src="js/jquery.js"></script>
<style>
html,body{height:100%}
</style>
<body style="margin:0px; padding:0px">
<table width="100%" border="0" cellpadding="0" cellspacing="0" style="margin:0px; padding:0px">
<tr><td style="font-size:10pt; padding:0px">
<div id="item_name" class="rebbonItem">
          <div>
            <div class="item_title">报表名称</div>
            <div class="item_content">
                <input id="title" name="title" value="<%=title%>" />
                
                <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" />
                <input type="submit" value="保存" />
				<%if (id==-1) {%>
                <input name="op" value="add" type="hidden" />
                <%}else{%>
                <input name="op" value="edit" type="hidden" />
                <input type="button" value="查看" onclick="addTab('<%=title%>', 'flow/report/form_report_show.jsp?id=<%=id%>')" />
               <%}%>
                <input name="id" value="<%=id%>" type="hidden" />
                <input id="query_id" name="query_id" value="<%=queryId%>" type="hidden" />
            </div>
          </div>
          
          <div>
            <div class="item_title">查询结果集&nbsp;&nbsp;[<a href="javascript:;" onclick="selQuery()">选择</a>]</div>
            <div class="item_content">
				<div id="divRelatedQuery">
                <%
				if (queryId!=-1) {
					FormQueryDb fqd = new FormQueryDb();
					fqd = fqd.getFormQueryDb((int)queryId);
					%>
					<%=fqd.getQueryName()%>
					<%
				}
				%>
                </div>

            </div>
          </div>

          <div>
            <div class="item_title">X轴</div>
            <div class="item_content">
				<%
				String optsX = "";
				String opts = "";
				if (queryId!=-1) {
					FormQueryDb fqd = new FormQueryDb();
					fqd = fqd.getFormQueryDb(queryId);
					String formCode = fqd.getTableCode();
					FormDb fd = new FormDb();
					fd = fd.getFormDb(formCode);
					%>
						<%=fd.getName()%>
						<br />
						X轴
					<%
					// 取得关联查询中默认的colProps
					String queryRelated = fqd.getQueryRelated();
					if (!queryRelated.equals("")) {
						int queryRelatedId = StrUtil.toInt(queryRelated, -1);
						FormQueryDb aqdRelated = fqd.getFormQueryDb(queryRelatedId);
						FormDb fdRelated = fd.getFormDb(aqdRelated.getTableCode());
						Iterator ir = fdRelated.getFields().iterator();
						while (ir.hasNext()) {
							FormField ff = (FormField)ir.next();
							// if (!ff.isCanList())
							// 	continue;
							opts += "<option value='rel." + ff.getName() + "'>" + ff.getTitle() + "</option>";
						}
					}
					
					Iterator ir = fd.getFields().iterator();
					while (ir.hasNext()) {
						FormField ff = (FormField)ir.next();
						// if (!ff.isCanList())
						// 	continue;
						opts += "<option value='" + ff.getName() + "'>[ " + ff.getTitle() + " ]</option>";
						optsX += "<option value='" + ff.getName() + "'>[ " + ff.getTitle() + " ]</option>";
					}					
					%>
					<select id="fieldName" name="fieldName">
					<%=optsX%>
                    </select>
                    <select id="formula" name="formula">
                      <option value="sum">求和</option>
                      <option value="average">平均值</option>
                      <option value="count">数量</option>
                    </select>
                    <input type="button" value="确定" onClick="insertXCol()" />
                <%}%>
            </div>
          </div>

          <div>
            <div class="item_title">Y轴</div>
            <div class="item_content">
            	<%if (queryId!=-1) {%>
                    Y轴
                    <select id="fieldNameY" name="fieldNameY">
                    <%=opts%>
                    </select>
                    
                    <select id="fieldtype" name="fieldtype">
                    <option value="">请选择类型</option>
                    <option value="<%=FormQueryReportDb.TYPE_USER%>">姓名</option>
                    <option value="<%=FormQueryReportDb.TYPE_DEPT%>">部门</option>
                    <option value="<%=FormQueryReportDb.TYPE_FIELD%>">字段</option>
                    </select>
                    <input value="确定" onClick="insertYCol()" type="button" />
                <%}%>
            </div>
          </div>          
          
      </div>
</td>
</tr>
</table>
</body>
</html>