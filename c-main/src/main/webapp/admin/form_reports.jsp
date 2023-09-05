<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="cn.js.fan.db.ResultIterator"%>
<%@page import="cn.js.fan.db.ResultRecord"%>
<%@page import="com.redmoon.oa.report.ReportManageDb"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
	<title>报表关联</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<script src="../inc/common.js"></script>
	<script language=JavaScript src='../js/jquery-1.9.1.min.js'></script>
	<script type="text/javascript" src="../js/jquery.toaster.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
  </head>
  <%
  	  boolean re= false;
	  String sql = "select id,name from report_manage";
	  String formCode = ParamUtil.get(request,"code");
	  String code = ParamUtil.get(request, "code");	  
	  
	  ReportManageDb rmd = new ReportManageDb();
	  Vector v = rmd.list(sql);
	  Iterator i = v.iterator();
	  String op = ParamUtil.get(request,"op");
	  String par = ParamUtil.get(request,"par");
	  if(op.equals("add")){
	  	try {
		  	 int id = ParamUtil.getInt(request,"reportId",0);
		  	 String name = ParamUtil.get(request,"reportName");
		  	 sql = "select id from visual_module_reports where form_code = "+ StrUtil.sqlstr(code) + " and report_id = "+id;
	  		 JdbcTemplate jt = new JdbcTemplate();		  	 
		  	 ResultIterator ri = jt.executeQuery(sql);
		  	 if(ri.hasNext()){
		  	 	re = true;
		  	 }
		  	 if(re){
		  	 	response.sendRedirect("form_reports.jsp?op=alt&par=exit&code="+code + "&formCode=" + formCode);
		  	 	return;
		  	 	//out.print(StrUtil.jAlert_Redirect("操作ss","提示","form_reports.jsp?code="+code));
		  	 }else{
			  	 sql = "insert into visual_module_reports (form_code,report_id,report_name) values ("+ StrUtil.sqlstr(code) +"," + id + ","+StrUtil.sqlstr(name)+")";
			  	 re = jt.executeUpdate(sql) >=1 ? true : false;
			  	 if(re){
			  	 	response.sendRedirect("form_reports.jsp?op=alt&par=suc&code="+code + "&formCode=" + formCode);
			  	 }else{
			  	 	response.sendRedirect("form_reports.jsp?op=alt&par=err&code="+code + "&formCode=" + formCode);
			  	 }
			  }
	  	 }catch(Exception e){
	  	 	e.printStackTrace();
	  	 }
	  }else if(op.equals("del")){
	  	try{
	  		int id = ParamUtil.getInt(request,"reportId",0);
	  		sql = "delete from visual_module_reports where form_code="+StrUtil.sqlstr(code)+" and report_id="+id;
	  		JdbcTemplate jt = new JdbcTemplate();		  	 	  		
	  		re = jt.executeUpdate(sql) >=1 ? true : false;
		  	 if(re){
		  	 	response.sendRedirect("form_reports.jsp?op=alt&par=suc&code="+code + "&formCode=" + formCode);
		  	 }else{
		  	 	response.sendRedirect("form_reports.jsp?op=alt&par=err&code="+code + "&formCode=" + formCode);
		  	 }
	  	}catch(Exception e){
	  	 	e.printStackTrace();
	  	 }
	  }
   %>
  <body>
  <%@ include file="../visual/module_setup_inc_menu_top.jsp"%>
  <script>
  o("menu8").className="current"; 
  </script>
  <div class="spacerH"></div>
  	<script>
  		var op = "<%=op%>";
  		if(op=="alt"){
  			var par = "<%=par%>";
  			if(par=="exit"){
  				$.toaster({ priority : 'info', message : '该报表已经存在！' });
  			}else if(par=="suc"){
  				$.toaster({ priority : 'info', message : '操作成功！' });
  			}else if(par=="err"){
  				$.toaster({ priority : 'info', message : '操作成功！' });
  			}
  		}
  		
  	</script>
  	<table cellSpacing="0" class="tabStyle_1 percent80" cellPadding="3" width="95%" align="center">
	    <tr>
	      <td colspan="6" align="center" noWrap class="tabStyle_1_title">报表关联</td>
	    </tr>
	    <tr>
	    	<td colspan="1" width="10%" align="center">选择报表</td>
	    	<td colspan="1" width="10%">
	    		<select id="reportIds">
	    		<%
	    		 while (i.hasNext()) {
					rmd = (ReportManageDb) i.next();
					try {
						int reportId = rmd.getInt("id");
						String reportName= rmd.getString("name");
						int flag = reportName.indexOf(".");
						reportName = reportName.substring(0,flag);
						%>
							<option value="<%=reportId %>"><%=reportName %></option>
						<%
					}catch(Exception e) {
						e.printStackTrace();
					}
				  }
	    		 %>
	    		 </select>
	    	</td>
	    	<td colspan="4">
	    		<input type="button" class="btn" value="确定" onclick="check()"/>
	    	</td>
	    </tr>
    </table>
  	<table cellSpacing="0" class="tabStyle_1 percent80" cellPadding="3" width="95%" align="center">
  		<tr>
  			<td class="tabStyle_1_title"  align="center">报表名称</td>
  			<td class="tabStyle_1_title"  align="center">操作</td>
  		</tr>
		<%
			sql = "select * from visual_module_reports where form_code="+StrUtil.sqlstr(formCode);
	  		JdbcTemplate jt = new JdbcTemplate();		  	 			
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			Date d = new Date();
			String date = StrUtil.FormatDate(d,"yyyy-MM-dd");
			int year = Integer.parseInt(date.substring(0,4));
			try {
				while(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				String reportName = rr.getString("report_name");
				int reportId = rr.getInt("report_id");
		%>
			<tr>
				<td align="center">
					<%=reportName %>
				</td>
				<td  align="center">
					<a href="javascript:;" style="cursor:pointer" onclick="addTab('<%=reportName %>','<%=request.getContextPath()%>/reportJsp/showReport.jsp?id=<%=reportId %>&year=<%=year %>')" >查看</a>
					&nbsp;&nbsp;&nbsp;
					<a href="javascript:;" style="cursor:pointer" onclick="del('<%=reportId %>','<%=formCode %>')">取消关联</a>
				</td>
			</tr>
		<%
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		 %>
  	</table>
  	
  	<script>
  		function check(){
  			jConfirm("确定选择度关联该报表？","提示",function(r){
	  			if(!r){
	  				return;
	  			}else{
	  				var reportId = $("#reportIds").find("option:checked").val();
		  			var reportName = $("#reportIds").find("option:checked").html();
		  			window.location.href="?op=add&reportId="+reportId+"&reportName="+reportName+"&code=<%=formCode%>&formCode=<%=formCode%>";
	  			}
  			})
  		}
  		function del(id,code){
  			jConfirm("确定取消关联该报表？","提示",function(r){
	  			if(!r){
	  				return;
	  			}else{
	  				window.location.href="?op=del&reportId="+id+"&code="+code + "&formCode=<%=formCode%>";
	  			}
  			})
  		}
  	</script>
  </body>
</html>
