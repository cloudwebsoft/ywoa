<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String mode = ParamUtil.get(request, "mode");
	if (!mode.equals("moduleTag")) {
		if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
			%>
			<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
			<%
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>报表 - 查看</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jqgrid/ui.jqgrid.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/core.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css" />

<script src="../../inc/common.js"></script>
<script src="../../js/jquery-1.9.0.min.js"></script>
<script src="../../js/jquery-migrate-1.2.1.min.js"></script>

<script src="../../js/jquery-ui/jquery-ui.js"></script>
<script src="../../js/jquery.form.js"></script>

<script type="text/javascript" src="../../js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="../../js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="../../js/tabpanel/Toolbar.js"></script>

<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>

<style>
html, body {
   margin: 0;  
   padding: 0;
   font-size: 100%;
}

.condFieldDiv{
	float:left;
	padding:3px;
	height:22px;
}
.condFieldLSpan{
	width:80px;
	display:block;
	float:left;
	padding:3px;
	pading-left:10px;
	background-color:#ECF3F9;	
}
.condFieldRSpan{
	padding-left:3px;
	float:left;
	display:block;
}
</style>
<%@ include file="../../inc/nocache.jsp"%>
</head>
<body>
<%
if (mode.equals("moduleTag")) {
	String tagName = ParamUtil.get(request, "tagName");
	%>
	<%@ include file="../../visual/module_inc_menu_top.jsp"%>
    <script>
	$("li[tagName='<%=tagName%>']").addClass("current");
	</script>
	<%
}
%>
<%
long id = ParamUtil.getLong(request, "reportId", -1);
if (id==-1) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
	return;
}
FormQueryReportDb fqrd = new FormQueryReportDb();
fqrd = (FormQueryReportDb)fqrd.getQObjectDb(new Long(id));

FormQueryDb fqd = new FormQueryDb();
fqd = fqd.getFormQueryDb(fqrd.getInt("query_id"));

int flowStatus = ParamUtil.getInt(request, "flowStatus", 1000);
String preDate = ParamUtil.get(request, "preDate");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");

java.util.Date beginDate = null;
java.util.Date endDate = null;
String op = ParamUtil.get(request, "op");

if (!preDate.equals("") && !preDate.equals("*")) {
	String[] aryDate = StrUtil.split(preDate, "\\|");
	strBeginDate = aryDate[0];
	strEndDate = aryDate[1];
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
}
else {
	if (preDate.equals("*")) {
		beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	}
	else {
		if (op.equals("")) {
			java.util.Date flowBeginDate1 = fqd.getFlowBeginDate1();
			java.util.Date flowBeginDate2 = fqd.getFlowBeginDate2();
			if (flowBeginDate1!=null) {
				preDate = "*";
				strBeginDate = DateUtil.format(flowBeginDate1, "yyyy-MM-dd");
			}
			if (flowBeginDate2!=null) {
				preDate = "*";
				strBeginDate = DateUtil.format(flowBeginDate2, "yyyy-MM-dd");
			}
		}
		if (preDate.equals("")) {
			strBeginDate = "";
			strEndDate = "";
		}
	}
}

if (op.equals("search")) {
	fqd.setFlowStatus(flowStatus);
	fqd.setFlowBeginDate1(beginDate);
	fqd.setFlowBeginDate2(endDate);
	fqd.save();	
}

String[] ary = null;
try {
	if (mode.equals("moduleTag")) {
		int moduleId = ParamUtil.getInt(request, "moduleId");
		String tagName = ParamUtil.get(request, "tagName");
		String moduleFormCode = ParamUtil.get(request, "moduleCode");
		FormDb fdModule = new FormDb();
		fdModule = fdModule.getFormDb(moduleFormCode);

		com.redmoon.oa.visual.FormDAO moduleFdao = new com.redmoon.oa.visual.FormDAO();
		moduleFdao = moduleFdao.getFormDAO(moduleId, fdModule);
		
		// 取得选项卡中的条件字段映射关系
		String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleFormCode, tagName);
		
		if (tagUrl.equals("")) {
			JSONObject jobject = new JSONObject();
			jobject.put("page", 1);
			jobject.put("total", 0);
			
			out.print(jobject);
			return;
		}
		
		JSONObject jsonTabSetup = new JSONObject(tagUrl);
		
		FormQueryReportRender fqrr = new FormQueryReportRender(moduleFdao, jsonTabSetup);
		fqrr.setForRelateModule(true);
		
		ary = fqrr.getJqGridDesc(request, id);		
	}
	else {	
		FormQueryReportRender fqrr = new FormQueryReportRender();
							
		// out.print(fqrr.rend(request, id));
		ary = fqrr.getJqGridDesc(request, id);
	}
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	e.printStackTrace();
	return;
}
if (ary[0]==null) {
	out.print(SkinUtil.makeInfo(request, "结果集为空！"));
	return;
}
%>

<%if (!mode.equals("moduleTag")) {%>
<div id="toolbar" style="height:25px;"></div>
<%}%>

<div id="searchDiv" style="display:none">
    <div class="condFieldDiv">
        <span class="condFieldLSpan">流程状态：</span>
        <span class="condFieldRSpan">
            <select id="flowStatus" name="flowStatus">
            <option value="1000" <%=fqd.getFlowStatus()==1000?"selected":""%>>不限</option>
            <option value="<%=WorkflowDb.STATUS_NOT_STARTED%>" <%=fqd.getFlowStatus()==WorkflowDb.STATUS_NOT_STARTED?"selected":""%>><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%></option>
            <option value="<%=WorkflowDb.STATUS_STARTED%>" <%=fqd.getFlowStatus()==WorkflowDb.STATUS_STARTED?"selected":""%>><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%></option>
            <option value="<%=WorkflowDb.STATUS_FINISHED%>" <%=fqd.getFlowStatus()==WorkflowDb.STATUS_FINISHED?"selected":""%>><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%></option>
            <option value="<%=WorkflowDb.STATUS_DISCARDED%>" <%=fqd.getFlowStatus()==WorkflowDb.STATUS_DISCARDED?"selected":""%>><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%></option>
            <option value="<%=WorkflowDb.STATUS_REFUSED%>" <%=fqd.getFlowStatus()==WorkflowDb.STATUS_REFUSED?"selected":""%>><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%></option>
            </select>
        </span>
        </span>
    </div>
    <div class="condFieldDiv">
        <span class="condFieldLSpan">时间：</span>
        <span class="condFieldRSpan">
    <select id="preDate" name="preDate" onChange="if (this.value=='*') o('dateSection').style.display=''; else o('dateSection').style.display='none'">
    <option selected="selected" value="">不限</option>
    <%
    java.util.Date[] aryDate = DateUtil.getDateSectOfToday();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">今天</option>
    <%
    aryDate = DateUtil.getDateSectOfYestoday();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">昨天</option>
    <%
    aryDate = DateUtil.getDateSectOfCurWeek();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">本周</option>
    <%
    aryDate = DateUtil.getDateSectOfLastWeek();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">上周</option>
    <%
    aryDate = DateUtil.getDateSectOfCurMonth();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">本月</option>
    <%
    aryDate = DateUtil.getDateSectOfLastMonth();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">上月</option>
    <%
    aryDate = DateUtil.getDateSectOfQuarter();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">本季度</option>
    <%
    aryDate = DateUtil.getDateSectOfCurYear();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">今年</option>
    <%
    aryDate = DateUtil.getDateSectOfLastYear();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">去年</option>
    <%
    aryDate = DateUtil.getDateSectOfLastLastYear();
    %>
    <option value="<%=DateUtil.format(aryDate[0], "yyyy-MM-dd")%>|<%=DateUtil.format(aryDate[1], "yyyy-MM-dd")%>">前年</option>
    <option value="*">自定义</option>
    </select>
    <span id="dateSection" style="display:<%=preDate.equals("*")?"":"none"%>">
    从
    <input id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
    至
    <input id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
    </span>
    <input type="hidden" name="reportId" value="<%=id%>" />
    <input type="hidden" name="op" value="search" />
        </span>
        </span>
    </div>
    <div style="text-align:center; height:22px; clear:both; display:none">
    <input type="submit" value=" 确定 " />
    </div>
</div>

<div id="condDiv" style="clear:both; overflow:auto;"></div>
<div style="margin-top: 0px; margin-left:0px; clear:both">
<table id="gridTable"></table>
<div id="gridPager"></div>
</div>

</body>
<script>
$(document).ready(function(){
  var toolbar = new Toolbar({
    renderTo : 'toolbar',
    //border: 'top',
    items : [
	{
      type : 'button',
      text : '保存条件',
      bodyStyle : 'save',
      useable : 'T',
      handler : function(){
		$('#formConditionFieldCode').submit();
      }
    },{
      type : 'button',
      text : '刷新',
      bodyStyle : 'refresh',
      useable : 'T',
      handler : function(){
		window.location.href = "form_report_show_jqgrid.jsp?reportId=<%=id%>";
      }
    },{
      type : 'button',
      text : '报表设计',
      bodyStyle : 'design',
      useable : 'T',
      handler : function(){
		addTab("设计器", "flow/report/designer.jsp?id=<%=id%>");
      }
    },{
      type : 'button',
      text : '查询设计',
      bodyStyle : 'design_query',
      useable : 'T',
      handler : function(){
		addTab("设计器", "flow/designer/designer.jsp?id=<%=fqd.getId()%>");
      }
	},{
      type : 'button',
      text : '导出',
      bodyStyle : 'export',
      useable : 'T',
      handler : function(){
		window.open("form_report_jqgrid_excel.jsp?id=<%=id%>");
      }
	}
	
	]
  });

  toolbar.render();
  
  <%
  JSONArray json = new JSONArray(ary[1]);
  %>
 
  $("#gridTable").jqGrid({
	  page: 1,
	  total: <%=(int) Math.ceil((double) json.length() / 20)%>,
	  records: <%=json.length()%>,
	  
	  autowidth: true,
	  // height: 250,

	  datatype: "local",
	  /*
	  colNames:['编号','用户名', '性别', '邮箱', 'QQ','手机号','出生日期'],
	  colModel:[
		   {name:'id',index:'id', width:60, sorttype:"int"},           
		   {name:'birthday',index:'birthday', width:100, sorttype:"date"}
		   ],
	  sortname:'id',
	  sortorder:'asc',
	  */
	  <%=ary[0]!=null?ary[0].toString():""%>
	  <%
	  String orderby = StrUtil.getNullString(fqrd.getString("orderby"));
	  String sort = StrUtil.getNullString(fqrd.getString("sort"));
	  if (!orderby.equals("")) {
	  	%>
	  sortname:'<%=orderby%>',
	  sortorder:'<%=sort%>',
		<%
	  }
	  %>
	  viewrecords:true,
	  rowNum:20,
	  rowList:[10,20,30],
	  pager:"#gridPager",
	  footerrow: true,
	  gridComplete: completeMethod,
	  onPaging: function (pgButton) { // 分页事件
		   var page = jQuery("#gridTable").jqGrid('getGridParam','page');
		   jQuery("#gridTable").setGridParam({page:page}).trigger("reloadGrid");
	  },
	  
	  // shrinkToFit: true,
	  caption: "<%=fqrd.getString("title")%>"
  }).navGrid('#pager2',{edit:false,add:true,del:false});

  <%
  String mydata = "\"\"";
  if (ary[1]!=null)
	  mydata = ary[1].toString();
  %>
  var mydata = <%=mydata%>;
  
  /*
  var mydata = [

		  {id:"1",userName:"flySky",gender:"男",email:"skyfly@163.com",QQ:"8000000",mobilePhone:"13223423424",birthday:"1985-10-01"},
		   {id:"9",userName:"孙先",gender:"男",email:"xian@qq.com",QQ:"76454533",mobilePhone:"132290062",birthday:"1989-11-21"}

		  ];
  */
  
  for(var i = 0 ; i <= mydata.length ; i++) {
	  jQuery("#gridTable").jqGrid('addRowData',i+1,mydata[i]);
  }
  
  // 本地化数据，必须手动翻页
  jQuery("#gridTable").setGridParam({page:1}).trigger("reloadGrid");

  // 使caption居中
  $('#gridTable').closest("div.ui-jqgrid-view")
	  .children("div.ui-jqgrid-titlebar")
	  .css("text-align", "center")
	  .children("span.ui-jqgrid-title")
	  .css("float", "none"); 
});

// 写合计
<%=ary[2]!=null?ary[2]:""%>

<%
	String sql = "select distinct condition_field_code from form_query_condition where query_id=" + fqd.getId();
	String fieldsSelected = "";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql);
	while (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		if ("".equals(fieldsSelected))
			fieldsSelected = rr.getString(1);
		else
			fieldsSelected += "," + rr.getString(1);
	}
%>

$(function() {
	<%
	if (mode.equals("moduleTag")) {
	%>
		// 如果是来自于模块关联，则带有选项卡
		$("#gridTable").setGridHeight($(window).height() - $('#toolbar').height() - $('#condDiv').height() - $('#gridPager').height() - $("#tabs1").height() - 70);
	<%
	}
	else {
	%>
	// 当不关联选项卡时载入查询条件
	$.get(
			"../designer/ajax_condition_value.jsp",
			{id:<%=fqd.getId()%>, fieldsSelected:"<%=fieldsSelected%>", formCode:"<%=fqd.getTableCode()%>", "mode":"", "moduleFormCode":"", "tagName":""},
			function(data) {
				$("#condDiv").append(data);

				// 在ajax获取的条件中加入流程状态及时间
				// $("#formConditionFieldCode").append("<span id='searchSpan'>" + $("#searchDiv").html() + "</span>");
				
				var options = {
					success:   showSaveCondResponse,  // post-submit callback 
					dataType:  'json'        // 'xml', 'script', or 'json' (expected server response type) 
				};
				$('#formConditionFieldCode').submit(function() {
					$(this).ajaxSubmit(options);
					return false;
				});
				
				// 去掉seachDiv中的元素，以免preDate有重复引起脚本问题
				$("#searchDiv").html("");
				
				if (o("preDate")) {
					o("preDate").value = "<%=preDate%>";
				
					Calendar.setup({
						inputField     :    "beginDate",      // id of the input field
						ifFormat       :    "%Y-%m-%d",       // format of the input field
						showsTime      :    false,            // will display a time selector
						singleClick    :    false,           // double-click mode
						align          :    "Tl",           // alignment (defaults to "Bl")		
						step           :    1                // show all years in drop-down boxes (instead of every other year as default)
					});
			
					Calendar.setup({
						inputField     :    "endDate",      // id of the input field
						ifFormat       :    "%Y-%m-%d",       // format of the input field
						showsTime      :    false,            // will display a time selector
						singleClick    :    false,           // double-click mode
						align          :    "Tl",           // alignment (defaults to "Bl")		
						step           :    1                // show all years in drop-down boxes (instead of every other year as default)
					});
				}

				$("#gridTable").setGridHeight($(window).height() - $('#toolbar').height() - $('#condDiv').height() - $('#gridPager').height() - 70);
				
				$(window).bind('resize', function() {
					$("#gridTable").setGridHeight($(window).height() - $('#toolbar').height() - $('#condDiv').height() - $('#gridPager').height() - 70);
				});
				
			}
		 );
	<%}%>
});

function showSaveCondResponse(data)  {
	if (data.re=="true") {
		o("formConditionFieldCode").action = "form_report_show_jqgrid.jsp";
		o("formConditionFieldCode").submit();
	}
	else {
		alert("保存条件失败！");
	}
}
</script>
</html>