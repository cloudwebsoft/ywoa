<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.base.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.worklog.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "admin")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String myName = privilege.getUser(request);
	
	String op = StrUtil.getNullString(request.getParameter("op"));
	int type = ParamUtil.getInt(request, "logType", WorkLogDb.TYPE_NORMAL);
	String real_name = ParamUtil.get(request, "realName");
	String dept_names = ParamUtil.get(request, "deptNames");
	String dept_codes = ParamUtil.get(request, "deptCodes");
	String content = ParamUtil.get(request,"content");
	String startDate = ParamUtil.get(request, "startDate");
	String endDate = ParamUtil.get(request, "endDate");
	int pagesize = ParamUtil.getInt(request, "pagesize", 20);
	int curpage	= ParamUtil.getInt(request, "CPages", 1);
	
	String querystr = "op=" + op + "&logType=" + type;
	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=8">
<title>工作报告管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<style type="text/css">
.searchTable {
	line-height: 35px;
	width: 98%;
	margin-top: 20px;
	margin-right: auto;
	margin-bottom: 20px;
	margin-left: auto;
	border-top-width: 1px;
	border-right-width: 1px;
	border-left-width: 1px;
	border-top-style: solid;
	border-right-style: solid;
	border-left-style: solid;
	border-top-color: #e9e9e9;
	border-right-color: #e9e9e9;
	border-left-color: #e9e9e9;
}

.searchTable td {white-space: nowrap; overflow: hidden;text-overflow: ellipsis;-o-text-overflow: ellipsis;border-bottom-width: 1px;border-bottom-style: solid;border-bottom-color: #e4e4e4;}
.searchTable input[type="text"],.searchTable select[name="select"] {
	height: 20px;
	line-height: 20px;
	border: 1px solid #cccccc;
	width: 170px;
	margin-right: 10px;
}
.searchTable tr td .pages {
	height: 25px;
	width: 25px;
	text-align: center;
}

.searchTable .btn {
	cursor: pointer;
	width: 55px;
	height: 30px;
	color: #FFF;
	border: 0;
	font-weight: bold;
	background: url(<%=SkinMgr.getSkinPath(request)%>/images/blue_btn_55.png);	
}
</style>
<body>
<%
if (op.equals("delete")) {
		int id = ParamUtil.getInt(request, "id");
		WorkLogDb wld = new WorkLogDb(id);
		try {
			wld.del();
		}catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		}
		return;	
	}
 %>
<form name="form1" method="post" action="workLogManage.jsp?op=search">
   <table class="searchTable" cellpadding="0"; cellspacing="0">
	  <tr bgcolor="#f3efe9">
	    <td width="2%">&nbsp;</td>
	    <td colspan="3">报告类型：<select id="logType" name="logType">
	         <option value="<%=WorkLogDb.TYPE_NORMAL %>" >日报</option>
			 <option value="<%=WorkLogDb.TYPE_WEEK %>" >周报</option>
			 <option value="<%=WorkLogDb.TYPE_MONTH %>" >月报</option>
	       </select>
	       <script>
	 			$("select:[id='logType'] option").each(function () {
	            	if($(this).val() == <%=type%>){
	            		$(this).attr("selected","true");
	            	}
	            })
	       </script>
	    </td>
	  </tr>
	  <tr>
	    <td>&nbsp;</td>
	    <td width="24%">真实姓名：<input type="text" name="realName" id="userName" value="<%=real_name%>"></td>
	    <td width="24%">所属部门：<input type="text" name="deptNames" id="deptNames" value="<%=dept_names%>" onClick="openWinDepts('../dept_multi_sel.jsp?openType=open','500','360')">
	    	<input type="hidden" name="deptCodes" id="deptCodes" value="<%=dept_codes%>">	
	    </td>
	    <td width="52%">报告内容：<input type="text" name="content" value="<%=content%>"></td>
	  </tr>
	  <tr>
	    <td>&nbsp;</td>
	    <td>起始日期：<input type="text" name="startDate" id="startDate" value="<%=startDate%>" readonly="true"/>
         	<script type="text/javascript">
                
            </script>	
	    </td>
	    <td>结束日期：<input type="text" name="endDate" id="endDate" value="<%=endDate%>" readonly="true"/>
         	<script type="text/javascript">
               
             </script>
	    </td>
	    <td></td>
	  </tr>
	  <tr><td align="center" colspan="4" style="padding:8px 0px 8px 0px;"><input class="btn" type="submit" name="Submit" value="查找"></td></tr>
  </table>
</form>	
    <%
	String sql = "select a.id from work_log a ";	 				
	if(op.equals("search")) {
		if(!"".equals(real_name) && real_name!=null){
	 		sql += " ,users d ";
	 	}
	
		if(!"".equals(dept_codes) && dept_codes!=null){
			sql += " ,dept_user b,department c ";
		}
	
	 	sql += " where a.log_type = "+type;
	 	if(!"".equals(real_name) && real_name!=null){
	 		sql += " and a.userName = d.name and d.realName = "+StrUtil.sqlstr(real_name);
	 		querystr += "&realName=" + real_name;
	 	}
	 	if(!"".equals(dept_codes) && dept_codes!=null){
	 		//如果选择的不是全部部门
	 		if(dept_codes.indexOf("root") == -1){
	 			sql += " and a.userName = b.user_name and b.dept_code = c.code and c.code in (";
		 		//将部门拆分
		 		String[] deptArr = StrUtil.split(dept_codes, ",");
		 		int num = deptArr.length;
		 		int index = 0;
		 		for(String dept : deptArr){
		 			sql += StrUtil.sqlstr(dept);
		 			if((++index) < num){
		 				sql += ",";
		 			}
		 		}
		 		sql += ") ";
		 	//如果选择的有全部部门，那么就去掉没有部门用户的报告
	 		}else{
	 			sql += " and a.userName = b.user_name and b.dept_code = c.code and c.code in ( select code from department ) ";
	 		}
	 		querystr += "&deptNames=" + dept_names+"&deptCodes="+dept_codes;
	 	}
	 	if(!"".equals(content) && content!=null){
	 		sql += " and a.content like "+StrUtil.sqlstr("%"+content+"%");
	 		querystr += "&content=" + content;
	 	}
	 	if(!"".equals(startDate) && startDate!=null){
	 		sql += " and a.myDate >= "+SQLFilter.getDateStr(startDate+" 00:00:00","yyyy-MM-dd HH:mm:ss");
	 		querystr += "&startDate=" + startDate;
	 	}
	 	if(!"".equals(endDate) && endDate!=null){
	 		sql += " and a.myDate <= "+SQLFilter.getDateStr(endDate+" 23:59:59","yyyy-MM-dd HH:mm:ss");
	 		querystr += "&endDate=" + endDate;
	 	}
	}else{
		sql += " where a.log_type = "+WorkLogDb.TYPE_NORMAL;
	}	
	sql += " order by a.myDate desc";	
	
	WorkLogDb workLogdb = new WorkLogDb();
	WorkLogCache wc = new WorkLogCache(workLogdb);
	wc.refreshList();
	int total = workLogdb.getObjectCount(sql);
	int totalpages;
	Paginator paginator = new Paginator(request, total, pagesize);
	//设置当前页数和总页数
	totalpages = paginator.getTotalPages();
	if (totalpages==0)
	{
		curpage = 1;
		totalpages = 1;
	}
	
	%>
	      <table id="mainTable" width="98%" border="0" align="left" cellpadding="2" cellspacing="0">
	        <thead>
	         <tr align="center">
	          <th width="150" align="center">用户名</th>
	          <th width="150" align="center">真实姓名</th>
		      <th width="150" align="center">部门</th>
		      <th id="logTypeValue" width="320" align="center"></th>
		      <th width="150" align="center">日期</th>
		      <th width="150" align="center">操作</th>
	         </tr>  
	        </thead>
	        <tbody>
	        	<%
	        		int start = (curpage-1)*pagesize;
					int end = curpage*pagesize;
	        		ObjectBlockIterator ir = workLogdb.getObjects(sql, start, end);
	        		while (ir.hasNext()) {
	        			WorkLogDb workLog = (WorkLogDb)ir.next();
	        			int logId = workLog.getId();
				    	String userName = workLog.getUserName();
				    	String workContent = workLog.getContent();
				    	java.util.Date myDate = workLog.getMyDate();
				    	//根据用户名，得到用户真正的名字
				    	UserDb user = new UserDb(userName);
				    	String realName = user.getRealName();
				    	//根据用户名，得到用户所在的部门
				    	DeptUserDb dud = new DeptUserDb(userName);
				    	String deptName = dud.getDeptName();
				 	%>
				 	<tr align="center">
					 	<td align="center" style="padding-top:10px;"><%=userName %></td>
					 	<td align="center" style="padding-top:10px;"><%=realName %></td>
						<td align="center" style="padding-top:10px;"><%=deptName %></td>
						<td align="center" style="white-space:nowrap; overflow: hidden;" ><%=workContent %></td>
						<td align="center" style="padding-top:10px;"><%=DateUtil.format(myDate, "yyyy-MM-dd") %></td>
						<td align="center" style="padding-top:10px;">
							<a title="查看" href="javascript:;" onclick="addTab('<%=DateUtil.format(myDate, "yyyy-MM-dd") %>', '<%=request.getContextPath()%>/ymoa/showWorkLogById.action?workLogId=<%=logId%>&userName=<%=StrUtil.UrlEncode(myName)%>&isNav=1')">查看</a>
							&nbsp;<a title="删除" href="javascript:;" onclick="removeWorkLog('<%=logId %>')">删除</a>
						</td>
					</tr> 
				 	<%
				 	}
		     	%>
	        </tbody>
	</table>
	<!-- <table class="percent98" width="92%" border="0" cellspacing="1" cellpadding="3" align="center">
	  <tr>
	   <td width="59%" align="right"><%
	    //out.print(paginator.getCurPageBlock("workLogManage.jsp?"+querystr));
	%></td>
	  </tr>
	</table> -->
</body>
<script>
$(document).ready(function() {
	flex = $("#mainTable").flexigrid
	(
		{
		/*
		searchitems : [
			{display: 'ISO', name : 'iso'},
			{display: 'Name', name : 'name', isdefault: true}
			],
		sortname: "iso",
		sortorder: "asc",
		*/
		url: false,
		usepager: true,
		checkbox: false,
		page: <%=curpage%>,
		total: <%=total%>,
		useRp: true,
		rp: <%=pagesize%>,
		
		// title: "通知",
		singleSelect: true,
		resizable: false,
		showTableToggleBtn: true,
		showToggleBtn: true,
		
		onChangeSort: changeSort,
		
		onChangePage: changePage,
		onRpChange: rpChange,
		onReload: onReload,

		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);	
	$('#startDate').datetimepicker({
                    lang:'ch',
                    timepicker:false,
                    format:'Y-m-d',
                    formatDate:'Y/m/d'
                });
     $('#endDate').datetimepicker({
                    lang:'ch',
                    timepicker:false,
                    format:'Y-m-d',
                    formatDate:'Y/m/d'
                });
});

function changeSort(sortname, sortorder) {
	window.location.href = "workLogManage.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "workLogManage.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
}

function rpChange(pagesize) {
	window.location.href = "workLogManage.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
	
	$("#logTypeValue").text($("#logType").children("option:selected").text());
	
	function openWinDepts(url,width,height){
		window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top="+($(window).height()-height)/2+",left="+($(window).width()-width)/2+",width="+width+",height="+height);
	}
	
	function setDepts(depts){
		form1.deptNames.value = "";
		form1.deptCodes.value = "";
		for (var i=0; i<depts.length; i++) {
			if (form1.deptNames.value=="") {
				form1.deptCodes.value += depts[i][0];
				form1.deptNames.value += depts[i][1];
			}
			else {
				form1.deptCodes.value += "," + depts[i][0];
				form1.deptNames.value += "," + depts[i][1];
			}
		}
	}
	
	function getDepts(){
		return form1.deptCodes.value;
	}
	
	function removeWorkLog(id){
		jConfirm('您要确定要删除此项报告吗？','提示',function(r){
			if(!r){return;}
			else{
				$.ajax({
					url: "workLogManage.jsp?op=delete",
					type: "post",
					data: {
						"id":id
					},
					success: function(data, status){
						jAlert('删除报告成功！','提示');
						window.location.href = "workLogManage.jsp?<%=querystr%>";
					},
					error: function(XMLHttpRequest, textStatus){
						jAlert('删除报告失败！','提示');
					}
				});	
			}
		})
	}
</script>
</html>