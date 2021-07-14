<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String formCode = "sales_order";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

// 取得皮肤路径
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String mode = ParamUtil.get(request, "mode");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>销售排行</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/jqgrid/ui.jqgrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>

<script type="text/javascript" src="../js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="../js/jquery.jqGrid.min.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<%@ include file="../inc/nocache.jsp"%>
<style>
.ui-jqgrid-sortable{
    cursor:pointer;
    color:#666;
}
.ui-state-default{
 border: 1px solid #a4d3ee !important; 
font-weight: bold !important; 
color: #fff !important;
}
</style>
</head>

<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">销售排行</td>
    </tr>
  </tbody>
</table>
<%
String op = ParamUtil.get(request, "op");

String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String preDate = ParamUtil.get(request, "preDate");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "preDate", preDate, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");

java.util.Date beginDate = null;
java.util.Date endDate = null;

if (!preDate.equals("") && !preDate.equals("*")) {
	String[] ary = StrUtil.split(preDate, "\\|");
	strBeginDate = ary[0];
	strEndDate = ary[1];
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
}
else {
	if (preDate.equals("*")) {
		beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	}
	else {
		strBeginDate = "";
		strEndDate = "";
	}
}
%>
<br />
<form action="sales_rank.jsp" method="get">
<table width="98%" align="center" class="percent98">
<tr><td align="left">
<select id="preDate" name="preDate" onchange="if (this.value=='*') o('dateSection').style.display=''; else o('dateSection').style.display='none'">
<option selected="selected" value="">不限</option>
<%
java.util.Date[] ary = DateUtil.getDateSectOfToday();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今天</option>
<%
ary = DateUtil.getDateSectOfYestoday();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">昨天</option>
<%
ary = DateUtil.getDateSectOfCurWeek();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本周</option>
<%
ary = DateUtil.getDateSectOfLastWeek();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上周</option>
<%
ary = DateUtil.getDateSectOfCurMonth();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本月</option>
<%
ary = DateUtil.getDateSectOfLastMonth();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上月</option>
<%
ary = DateUtil.getDateSectOfQuarter();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本季度</option>
<%
ary = DateUtil.getDateSectOfCurYear();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今年</option>
<%
ary = DateUtil.getDateSectOfLastYear();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">去年</option>
<%
ary = DateUtil.getDateSectOfLastLastYear();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">前年</option>
<option value="*">自定义</option>
</select>
<script>
o("preDate").value = "<%=preDate%>";
</script>
<span id="dateSection" style="display:<%=preDate.equals("*")?"":"none"%>">
从
<input id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
至
<input id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
</span>
<input type="submit" class="btn" value="查询" />
<select id="group" name="group">
<option value="">不分组</option>
<option value="dept">按部门分组</option>
</select></td></tr></table>
</form>
<div style="padding-left:10px">
<table id="mytable"></table>
<div id="pmytable"></div>
</div>
<script>
jQuery("#mytable").jqGrid({
   	url:'sales_rank_ajax.jsp?mode=<%=mode%>&preDate=' + o('preDate').value + '&beginDate=' + o('beginDate').value + '&endDate=' + o('endDate').value,
	datatype: "json",
   	colNames:['序号','姓名', '部门', '客户数', '行动数','商机数','商机成功率','订单数','产品销量', '销售额', '操作'],
   	colModel:[

   		{name:'idx',index:'idx', width:55, editable:true, sorttype:'int', summaryType:'count', summaryTpl : '合计({0})'},
   		{name:'realName',index:'realName', width:90},
   		{name:'dept',index:'dept', width:100},
   		/*
		此句行不通，因为userName无法指定
		{name:'customerCount',index:'customerCount', width:50, align:"right", sortable:false, sorttype:'number',summaryType:'sum', formatter:'showlink', formatoptions:{baseLinkUrl:'customer_list.jsp', addParam: '&op=search&find_date_cond=0&userName=&find_dateFromDate=<%=strBeginDate%>&find_dateToDate=<%=DateUtil.format(DateUtil.addDate(endDate, -1), "yyyy-MM-dd")%>'}},
		*/
   		{name:'customerCount',index:'customerCount', width:50, align:"right", sortable:false, sorttype:'number',summaryType:'sum'},
   		{name:'actionCount',index:'actionCount', width:80, align:"right", sortable:false, sorttype:'number',formatter:'number',summaryType:'sum'},
   		{name:'chanceCount',index:'chanceCount', width:80, align:"right", sortable:false,sorttype:'number',formatter:'number',summaryType:'sum'},		
   		{name:'chanceRate',index:'chanceRate', width:80,align:"right", sortable:false,sorttype:'number',formatter:'number', summaryType:'sum'},
   		{name:'orderCount',index:'orderCount', width:80,align:"right", sortable:false,sorttype:'number',formatter:'number', summaryType:'sum'},
   		{name:'sellCount',index:'sellCount', width:80,align:"right",sorttype:'number',formatter:'number', summaryType:'sum'},
   		{name:'sellSum',index:'sellSum', width:80,align:"right", sortable:false,sorttype:'number',formatter:'number', summaryType:'sum'},
		{name:'op',index:'op', width:100, sortable:false, editable:true}

   	],
   	rowNum:20,
   	rowList:[10,20,30,50,80,100],
   	height: 'auto',
   	pager: '#pmytable',
   	sortname: 'sellSum',
    viewrecords: true,
    sortorder: "desc",
    /*
	caption:"Grouping with remote data",
	*/
	jsonReader:{
        repeatitems : false
    },
    grouping: <%=mode.equals("grouping")?true:false%>,
   	groupingView : {
   		groupField : ['dept'],
   		groupColumnShow : [true],
   		groupText : ['<b>{0}</b>'],
   		groupCollapse : false,
		groupOrder: ['asc'],
		groupSummary : [true],
		groupDataSorted : true // 分组字段用于后台排序，在排序的时候传递参数类似：dept asc, sellSum
   	},
    footerrow: true,
    userDataOnFooter: true
});
/*
repeatitems : false  jqGrid要求的JSON格式中的行记录中，字段名不需要，而json object却会带过来，且似乎必须要按定义好的顺序，所以要置repeatitems为false

I suppose that you checked not all jqGrid modules which you needed during the jqGrid dounload. The navGrid function are used mostly for form editing functionality. So you should check "Form Edit" and "Common" modules from the "Editing" block.

If you want to verify which modules you use in the jquery.jqGrid.min.js you can open it with a text editor and you will see in the comment at the begining of the file text (typically in the line 8) starting with the following:

Modules: grid.base.js; jquery.fmatter.js; grid.custom.js; grid.common.js; grid.formedit.js
*/

jQuery("#mytable").jqGrid('navGrid','#pmytable',{view:true, edit:false, add:false, del:false, search:false},
				{
				  // 用于居中显示编辑表单，grid[0].id应为mytable
				  beforeShowForm: function(form) {
                  // "editmodlist"
				  // alert("here");
                  var dlgDiv = $("#editmod" + grid[0].id);
                  var parentDiv = dlgDiv.parent(); // div#gbox_list
                  var dlgWidth = dlgDiv.width();
                  var parentWidth = parentDiv.width();
                  var dlgHeight = dlgDiv.height();
                  var parentHeight = parentDiv.height();
                  // TODO: change parentWidth and parentHeight in case of the grid
                  //       is larger as the browser window
                  dlgDiv[0].style.top = Math.round((parentHeight-dlgHeight)/2) + "px";
                  dlgDiv[0].style.left = Math.round((parentWidth-dlgWidth)/2) + "px";
				}
              });
// jQuery("#mytable").jqGrid('navGrid', '#pmytable',{view:true, del:false});
jQuery("#mytable").jqGrid('sortableRows');

</script>
</body>
<script>
$(function(){
	$('#beginDate').datetimepicker({
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
})
jQuery("#group").change(function(){
	var vl = $(this).val();
	if(vl == "") {
		// jQuery("#mytable").jqGrid('groupingRemove',true);
		window.location.href = "sales_rank.jsp?preDate=<%=StrUtil.UrlEncode(preDate)%>";
	} else {
		// jQuery("#mytable").jqGrid('groupingGroupBy',vl);
		window.location.href = "sales_rank.jsp?mode=grouping?preDate=<%=StrUtil.UrlEncode(preDate)%>";
	}
});

jQuery("#group").val("<%=mode.equals("grouping")?"dept":""%>");
</script>
</html>