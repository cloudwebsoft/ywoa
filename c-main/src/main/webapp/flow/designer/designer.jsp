<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.dept.DeptMgr"%>
<%@ page import = "com.redmoon.oa.dept.DeptDb"%>
<%@ page import = "com.redmoon.oa.dept.DeptView"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>查询设计器</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/core.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/designer.css" />

<script src="../../inc/common.js"></script>
<script src="../../js/jquery-1.9.1.min.js"></script>
<script src="../../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="../../js/datepicker/jquery.datetimepicker.css"/>
<script src="../../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../../inc/map.js"></script>
<script src="../../inc/livevalidation_standalone.js"></script>
<script src="../../js/tabpanel/Toolbar.js" type="text/javascript"></script>
<script src="../../js/jquery.form.js"></script>
<script src="condition_value.js"></script>
<%
	if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}
	
	int id = ParamUtil.getInt(request, "id", -1);
	String queryName = "";
	String formCode = "";
	
	FormQueryDb aqd = new FormQueryDb();
	if (id!=-1) {
		aqd = aqd.getFormQueryDb(id);
		queryName = aqd.getQueryName();
		formCode = aqd.getTableCode();
		
		// 检查用户是否具备权限（是本人创建的，或者被授权）
		
		FormQueryPrivilegeMgr fqpm = new FormQueryPrivilegeMgr();
		if (!fqpm.canUserQuery(request, id)) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}

    String isSystem = ParamUtil.get(request, "isSystem");	
%>
<script type="text/javascript">
var isNew = false;

var curTableShortCode = "<%=formCode%>";

jQuery.fn.outerHTML = function(s) {
    return (s)? this.before(s).remove(): jQuery("<p>").append(this.eq(0).clone()).html();   
}

function itemFieldsSelectedOndblClick() {
	$(this).remove();
	// 删除时，如果删除的是当前选定表的，则应使其回到sortable1中
	if ($(this).attr('tableShortCode')==curTableShortCode) {
		var tmp = $("#" + $(this).attr('tableShortCode') + "_" + $(this).attr('code')).outerHTML();
		// 从sortable12中删除
		$("#" + $(this).attr('tableShortCode') + "_" + $(this).attr('code')).remove();
		// 添至sortable1中
		$("#sortable1").append(tmp);
	}
	// 刷新条件
	getConditionValue();
}

$(function() {
	$("ul.droptrue").sortable({
		connectWith: 'ul'
	},
	{ 
		receive: function(event, ui) {
			if (ui.sender.attr('id')=="sortable1") {
				$("#item_fields_selected_ul").append("<li title='双击删除' id='" + ui.item.attr('tableShortCode') + "-" + ui.item.attr('code') + "' code='" + ui.item.attr('code') + "' tableCode='" + ui.item.attr('tableCode') + "' tableShortCode='" + ui.item.attr('tableShortCode') + "'>" + ui.item.attr('name') + "</li>");
				$("#" + ui.item.attr('tableShortCode') + "-" + ui.item.attr('code')).bind("dblclick", itemFieldsSelectedOndblClick);
			}
			if (ui.sender.attr('id')=="sortable2") {
				var liId = ui.item.attr('tableShortCode') + "-" + ui.item.attr('code');
				$("#" + liId).remove();
			}
			
			// $(".rebbonItem").not("[id='item_fields_selected']").hide();
			// $("#item_fields_selected").show();
			
			getConditionValue();
			
			fixIEBug();
			
		}
	}
	);
	
	$("#sortable2").sortable({
			cancel: '#AUI_working_state'
		});

	$("#sortable1, #sortable2").disableSelection();
	
   	$(".ui-state-default").dblclick(switchLists);
	
});

function switchLists(e) {
   // determine which list they are in
   // this works if you only have 2 related lists.
   // otherwise you will need to specify the target list
   // the other list is one that has the connect with property but isn't
   // the current target's parent
   var otherList = $("[name^=sortUL]").not($(e.currentTarget).parent());

   // if the current list has no items, add a hidden one to keep style in place
   // when saving you will need to filter out items that have
   // display set to none to accommodate this scenario
   if ($(e.currentTarget).siblings().length == 0) {
      $(e.currentTarget).clone().appendTo($(e.currentTarget).parent()).css("display","none");
   }
   otherList.append(e.currentTarget);
   
   // otherList.children().removeClass($(e.currentTarget).attr("class"));
   // otherList.children().addClass(otherList.children().attr("class"));

   // remove any hidden siblings perhaps left over
   otherList.children(":hidden").remove();
      
   var obj = $(e.currentTarget);

	if (otherList.attr('id')=="sortable2") {
		$("#item_fields_selected_ul").append("<li title='双击删除' id='" + obj.attr('tableShortCode') + "-" + obj.attr('code') + "' code='" + obj.attr('code') + "' tableCode='" + obj.attr('tableCode') + "' tableShortCode='" + obj.attr('tableShortCode') + "'>" + obj.attr('name') + "</li>");
		$("#" + obj.attr('tableShortCode') + "-" + obj.attr('code')).bind("dblclick", itemFieldsSelectedOndblClick);
	}
	if (otherList.attr('id')=="sortable1") {
		var liId = obj.attr('tableShortCode') + "-" + obj.attr('code');
		$("#" + liId).remove();
	}
		
	getConditionValue();
	
	fixIEBug();   
}

function getFieldsSelected() {
	var fieldsSelected = "";
	$("#item_fields_selected_ul > li").each(function(k){
			if (fieldsSelected=="")
				fieldsSelected = $(this).attr("code");
			else
				fieldsSelected += "," + $(this).attr("code");
		});
	return fieldsSelected;
}

function switchMainPanel(item) {
	$("#mainPanel > div").hide();
	$("#mainPanel #" + item).show();
}

function getTableFields(tableCode, tableName) {
	<%
	if (id==-1) {
	%>
		if (curTableShortCode=="" && tableCode!="")
			jConfirm("您确定要选择" + tableName + "么？","提示", function(r){
				if(!r){
					return;
				}
				else{
					doGetTableFields(tableCode, tableName);
				}
			})
	<%
	}
	else {
	%>
		doGetTableFields(tableCode, tableName);
	<%}%>
}

function doGetTableFields(tableCode, tableName) {
	switchMainPanel("sortDiv");

	curTableShortCode = tableCode;

	$("#sortable1").html("");
	$("#sortable2").html("");
	$.get(
			"ajax_get_table_field.jsp",
			{formCode:tableCode},
			function(jsonStr){
				var json;
				try {
				json = jQuery.parseJSON(jsonStr);
				}
				catch (e) {
					jAlert(jsonStr,"提示");
					return;
				}
				// 页面刚载入时
				// alert(json + "--" + jsonStr);
				if (json==null || json=="")
					return;
		 		$.each(json, function(i){
					var isSelected = false;
					$("#item_fields_selected_ul > li").each(function(k){
							if ($(this).attr("code")==json[i].code) {
								if (json[i].tableShortCode==$(this).attr("tableShortCode")) {
								  isSelected = true;
								  return;
								}
							}
						});					
					
					// 根据被选择的情况，分别加至sortable1与sortable2
					var obj;
					if (isSelected)
						obj = $("#sortable2");
					else
						obj = $("#sortable1");
					obj.append("<li id=\"" + json[i].tableShortCode + "_" + json[i].code + "\" code=\"" + json[i].code + "\" tableShortCode=\"" + json[i].tableShortCode + "\" tableCode=\"" + json[i].tableShortCode + "\" name=\"" + json[i].name + "\" class=\"ui-state-default\">" + json[i].name + "</li>");
				})
			   	$(".ui-state-default").dblclick(switchLists);
				
				$("[name=table_name]").not($("#table_" + curTableShortCode)).hide();

			}
		 );

	<%if (id==-1) {%>
	initDivCalcuField();
	<%}%>
}

function initDivCalcuField() {
	if (curTableShortCode!="") {
		$.ajax({
		   type: "POST",
		   url: "ajax_calcu_field.jsp",
		   data: "op=modifyOthers&id=" + $("#id").val() + "&formCode=" + curTableShortCode,
		   success: function(html){
				$("#divCalcuField").html(html);
			}
		});
	}
}

function getConditionValue() {
	$.get(
			"ajax_condition_value.jsp",
			{id:$("#id").val(), fieldsSelected:getFieldsSelected(), formCode:curTableShortCode, isSystem:"<%=isSystem%>"},
			function(data) {
				 //alert(data);
				// console.log(data);
				$("#conditionPanel").html('');
				// $("#conditionPanel").html(data);
				//data不为空则执行，若不做判断则会报js错误
				if (data.replace(/(^\s*)|(\s*$)/g,'').length != 0){
				    $(data).appendTo($('#conditionPanel'));
                }
				var options = {
					type: "POST",
					success:   showSaveCondResponse,  // post-submit callback 
					dataType:  'json'        // 'xml', 'script', or 'json' (expected server response type) 
				};
				$('#formConditionFieldCode').submit(function() {
					$(this).ajaxSubmit(options);
					return false;
				});
				
		  		fixIEBug();
			}
		 );	
}

var isModifyDoing = false;

function showSaveCondResponse(data)  {
	if (data.re=="true") {
	}
	else {
		jAlert("保存条件失败：" + data.msg ,"提示");
	}
}

function modifyDeptCodesAndOthers(isMute) {
	isModifyDoing = true;
	ajaxResult = 0;
	
	modifyOthers(isMute);
}

function modifyOthers(isMute) {
	if(arguments.length==0)
		isMute = false;
	var orderFieldCodes = "";
	$("select[id='orderFieldCode']").each(function(i) {
		if ($(this).val()!="") {
			if (orderFieldCodes=="")
				orderFieldCodes = $(this).val();
			else
				orderFieldCodes += "," + $(this).val();
		}
	})
	
	// 字段合计描述字符串处理
	var calcCodesStr = "";
	var calcFuncs = $("select[name='calcFunc']");
	
	var map = new Map();
	var isFound = false;
	$("select[name='calcFieldCode']").each(function(i) {
		if ($(this).val()!="") {
			if (!map.containsKey($(this).val()))
				map.put($(this).val(), $(this).val());
			else {
				isFound = true;
				jAlert($(this).find("option:selected").text() + "出现重复！","提示");
				return false;
			}

			if (calcCodesStr=="")
				calcCodesStr = "\"" + $(this).val() + "\":\"" + calcFuncs.eq(i).val() + "\"";
			else
				calcCodesStr += "," + "\"" + $(this).val() + "\":\"" + calcFuncs.eq(i).val() + "\"";
		}
	})
	if (isFound)
		return;

	calcCodesStr = "{" + calcCodesStr + "}";

	$.ajax({
	   type: "POST",
	   async: false, // 同步处理
	   url: "ajax_do.jsp",
	   data: "op=modifyOthers&id=" + $("#id").val() + "&queryName=" + $("#queryName").val() + "&formCode=" + curTableShortCode + "&orderFieldCodes=" + orderFieldCodes + "&queryRelated=" + $("#queryRelated").val() + "&statDesc=" + calcCodesStr + "&flowStatus=" + $("#flowStatus").val() + "&flowBeginDate1=" + $("#flowBeginDate1").val() + "&flowBeginDate2=" + $("#flowBeginDate2").val(),
	   success: function(html){
			isModifyDoing = false;
		    var json = jQuery.parseJSON(html);
			if (json.re=="true") {
				if (!isMute)
					jAlert("保存成功！","提示");				
			   	$("[name=table_name]").not($("#table_" + curTableShortCode)).hide();
			}
			else {
				jAlert("保存名称等失败！" + json.msg ,"提示");
			}
		}
	});
}

function initNew() {
	$("#item_fields_selected_ul").html('');
	// $("#item_fields_selected_ul").append("<li title='双击删除' id='AUI-working_state' code='working_state' tableCode='ARCHIVE_USER_INFO' tableShortCode='AUI'>在职情况</li>");
	
	$("#conditionPanel").html();
	
	$("input[id^='chk_']").each(function(i) {
		$(this).attr("checked", false);
	})
	
	$("select[id='orderFieldCode']").each(function(i) {
		$(this).val('');
	})
		
	getConditionValue();
}
</script>
<script type="text/javascript">
// IE8下，因为缓存的原因，$.get只会运行一次，所以需关闭缓存
$.ajaxSetup ({
     cache: false //关闭AJAX相应的缓存
});
// js的注释与html的注释放开,再看一下效果
//ready方法修改为load，ready在页面结构加载完成后执行，ready执行时可能部分元素如图片未加载完成，导致出现js错误（Syntax error, unrecognized expression）
$(document).ready(function(){
  var toolbar = new Toolbar({
    renderTo : 'toolbar',
    //border: 'top',
    items : [{
      type : 'button',
      text : '新建',
      bodyStyle : 'new',
      useable : 'T',
      handler : function(){
		if (isNew)
			return;
		$.get(
			"ajax_do.jsp",
			{op:"new", formCode: curTableShortCode, isSystem:"<%=isSystem%>"},
			function(jsonStr){
				var json = jQuery.parseJSON(jsonStr);
				$.each(json, function(i){
					if (json[i].re=="true") {
						isNew = true;
						$("#queryName").val(json[i].name);
						$("#id").val(json[i].id);
													
						initNew();
						
						initUI();
						window.location.href = "designer.jsp?id=" + json[i].id + "&isSystem=<%=isSystem%>";
					}
					else {
						jAlert("操作失败！","提示");
					}
				});
			}
		 );
      }
    },'-',{
      type : 'button',
      text : '保存',
      bodyStyle : 'save',
      useable : 'T',
      handler : function(){
		  save();
      }
    },'-',{
      type : 'button',
      text : '刷新',
      bodyStyle : 'refresh',
      useable : 'T',
      handler : function(){
		  window.location.href = "designer.jsp?id=" + $("#id").val() + "&isSystem=<%=isSystem%>";
      }	  
    },
	/*'-',{
      type : 'button',
      text : '授权',
      bodyStyle : 'role-setup',
      useable : 'T',
      handler : function(){
        top.mainFrame.tabpanel.addTab({title:'查询授权', html:'<iframe src="<%=request.getContextPath()%>/archive/archive_query_user.jsp?id=' + $("#id").val() + '" width="100%" height="100%" frameborder="0"></iframe>'});
      }
    },
	*/
	/*'-',{
      type : 'button',
      text : '条件',
      bodyStyle : 'role-setup',
      useable : 'T',
      handler : function(){
		getConditionValue();
      }
    },
	*/
	'-',{
      type : 'button',
      text : '图表',
      bodyStyle : 'chart',
      useable : 'T',
      handler : function(){
        addTab("图表", "<%=request.getContextPath()%>/flow/form_query_chart_pie.jsp?id=" + $("#id").val());
      }	  
    },'-',{
      type : 'button',
      text : '运行',
      bodyStyle : 'run',
      useable : 'T',
	  handler: function(){
	  	saveBeforeLaunch();
		
		if (top.mainFrame)
			top.mainFrame.addTab($("#queryName").val(), '<%=request.getContextPath()%>/flow/form_query_list_do.jsp?id=' + $("#id").val() + '&op=query');	  
		else
			window.location.href='<%=request.getContextPath()%>/flow/form_query_list_do.jsp?id=' + $("#id").val() + '&op=query';
	  }
    },'-',{
      type : 'button',
      text : '删除',
      bodyStyle : 'delete',
      useable : 'T',
      handler : function(){
      		jConfirm("您确定要删除么？","提示",function(r){
      			if(!r){return;}
      			else{
      				 $.ajax({
						 type: "POST",
						 url: "ajax_do.jsp",
						 data: "op=del&ids=" + $("#id").val() + "&isSystem=<%=isSystem%>",
						 success: function(html){
							  var json = jQuery.parseJSON(html);
							  if (json.re=="true") {
								  jAlert("删除成功！","提示");
								  window.location.href = "designer.jsp?isSystem=<%=isSystem%>";
							  }
							  else {
								  jAlert("删除失败：" + json.msg + "！","提示");
							  }
						  }
					  }); 
      			}
      		})
      }
    }],
    filters : [{
      id : 'name',
      title : '名称',
      bodyStyle : 'btn-all',
      handler : function(){
		$(".rebbonItem").not("[id='item_name']").hide();
		$("#item_name").show();
      }
    },
	{
      id : 'fieldsOrder',
      title : '排序',
      bodyStyle : 'filter-order',
      handler : function(){
		$(".rebbonItem").not("[id='item_order']").hide();
		$("#item_order").show();
      }
    },
	{
      id : 'fieldsSelected',
      title : '已选字段',
      bodyStyle : 'filter-read-y',
      handler : function(){
		$(".rebbonItem").not("[id='item_fields_selected']").hide();
		$("#item_fields_selected").show();
      }
    }],
    active : 'name'//激活哪个
  });

  toolbar.render();
  getTableFields("<%=formCode%>");
  
  getConditionValue();

  $( "#seperator" ).draggable({
        axis: "y",
        opacity: 0.7,
        stop: function(event, ui) {
          var mainTdch = o("mainTd").clientHeight;
          o("mainPanel").style.height = (o("mainPanel").clientHeight + parseInt($(this).css("top"))) + "px";
          o("seperator").style.top = "0px"; // 相对位置
          
          // 拖动停止时mainTd的clientHeight会发生变化
          o("conditionPanel").style.height = (mainTdch - o("mainPanel").clientHeight - o("seperator").offsetHeight) + "px";       
          
          
          fixIEBug();
          
        }
      });

  initUI();

  initSize();
  
  fixIEBug();
 $("div[name='table_name']").bind("click", function() {
      var tableNameObj = $("div[name='table_name']");
      $.each(tableNameObj, function (i, obj) {
          var div = $(obj);
          div.attr('class', 'tableName');
      });
      $(this).attr('class', 'tableNameHover');
  });
  
});

function fixIEBug() {
  // 修复IE6下面的滚动BUG
  if (isIE6) {
    // only apply to IE
	if (!/*@cc_on!@*/0) return;  
	var all = document.getElementsByTagName('*'), i = all.length;
	while (i--) {    
		// if the scrollWidth (the real width) is greater than   
		// the visible width, then apply style changes 
		if (all[i].scrollWidth > all[i].offsetWidth) {
			if (all[i].id=="mainPanel" || all[i].id=="conditionPanel" || all[i].id=="rightPanel") {
				all[i].style['paddingBottom'] = '20px'; 
				all[i].style['overflowY'] = 'auto';
			}
		}
		if (all[i].scrollHeight > all[i].offsetHeight) {
			if (all[i].id=="mainPanel" || all[i].id=="conditionPanel" || all[i].id=="rightPanel") {
				all[i].style['paddingBottom'] = '0px'; 
				all[i].style['overflowX'] = 'hidden';
				all[i].style['overflowY'] = 'auto';
			}
		}
	}
	
	// 防止拖动后conditionPanel变宽，致上下滚动条错位
	o("conditionPanel").style.width = o("mainPanel").offsetWidth + "px";	
  }
}

function initSize() {
  o("seperator").style.width = o("mainPanel").style.width; // 相对位置
  
  o("mainPanel").style.height = (document.documentElement.clientHeight - o("toolbar").clientHeight)*0.6 + "px";
  o("conditionPanel").style.height = ((document.documentElement.clientHeight - o("toolbar").clientHeight)*0.4 - o("seperator").offsetHeight) + "px";
  
  o("rightPanel").style.height = (document.documentElement.clientHeight - o("toolbar").clientHeight) + "px";
  o("rightPanel").style.width = o("rightTd").style.width;
  
  // o("panelCond").style.height = (document.documentElement.clientHeight - o("toolbar").clientHeight)*0.6 + "px";
  // o("seperatorR").style.height = (document.documentElement.clientHeight - o("toolbar").clientHeight) + "px";
}

function initUI() {
	if ($("#id").val()=="") {
		$(".mainBox").hide();
		
		o("panelWelcome").style.height = (document.documentElement.clientHeight - o("toolbar").clientHeight) + "px";
		o("rightTd").style.height = (document.documentElement.clientHeight - o("toolbar").clientHeight) + "px";
		
		$("#panelWelcome").show();
		$("#rightPanel").hide();
	}
	else {
		$(".mainBox").hide();
		
		$("#panelCond").show();
		/*
		$("#seperator").show();
		$("#conditionPanel").show();
		*/
		$("#rightPanel").show();
	}
}

window.onresize = initSize;
</script>
</head>
<body>
<table border="0" cellspacing="0" id="all" style="width:100%;height:100%;margin:0px">
<tr>
<td colspan="2">
<div id="toolbar" style="height:25px"></div>
</td>
</tr>
<tr>
<td id="mainTd" valign="top">
<div id="panelWelcome" class="mainBox" style="display:none">
</div>
<div id="panelCond" class="mainBox">
    <div id="mainPanel" style="height:300px; overflow:auto; position:relative;">
          <div id="sortDiv">
              <ul id="sortable1" name="sortUL1" class='droptrue'>
              </ul>
              <ul id="sortableArrowUI">
              <li><div id="sortableArrow" title="请双击或拖动至右侧选择字段"></div></li>
              </ul>
              <ul id="sortable2" name="sortUL2" class='droptrue'>
              </ul>
              <br clear="both" />
              </div>
          </div>
    </div>
    <div id="seperator" style="height:3px; font-size:1px; background-color:#B7D0EF; cursor:n-resize; position:relative; margin:0px; z-index:1000;"></div>
    <div id="conditionPanel" style="overflow:auto; margin:0px; padding:0px; background-color:#ffffff; position:relative;"></div>
</div>
</td>
<td id="rightTd" valign="top" style="width:240px; padding:0px;">
<div id="rightPanel" style="height:100%;overflow:auto;">
    <div id="rebbon" style="height:100%;padding:0px; margin:0px">
    <div id="item_name" class="rebbonItem">
      <div>
        <div class="item_title">查询名称</div>
        <div class="item_content">
        <input id="queryName" name="name" value="<%=queryName%>" />
        <input id="id" name="id" value="<%=(id==-1)?"":""+id%>" type="hidden" />
        </div>
      </div>
      <div>
        <div class="item_title">字段合计&nbsp;&nbsp;[<a href="javascript:" onclick="addCalcuField(curTableShortCode)">添加</a>]</div>
        <div class="item_content">
          <div id="divCalcuField">
          <%
          if (id!=-1) {
              FormDb fd = new FormDb();
              fd = fd.getFormDb(formCode);
              int curCalcuFieldCount = 0;
              String statDesc = aqd.getStatDesc();
              if (statDesc.equals(""))
                  statDesc = "{}";
              JSONObject json = new JSONObject(statDesc);
              Iterator ir3 = json.keys();
              while (ir3.hasNext()) {
                  String key = (String) ir3.next();
                  %>
                    <div id="divCalcuField<%=curCalcuFieldCount%>">
                    <select id="calcFieldCode<%=curCalcuFieldCount%>" name="calcFieldCode">
                    <option value="">无</option>
                    <%
                    Iterator ir = fd.getFields().iterator();
                    while (ir.hasNext()) {
                        FormField ff = (FormField)ir.next();
                        if (!ff.isCanQuery())
                            continue;
                        if (ff.getFieldType()==FormField.FIELD_TYPE_INT
                            || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT
                            || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE
                            || ff.getFieldType()==FormField.FIELD_TYPE_PRICE
                            || ff.getFieldType()==FormField.FIELD_TYPE_LONG
                            ) {
                        %>
                        <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
                        <%}
                    }
                    %>
                    </select>
                    <select id="calcFunc<%=curCalcuFieldCount%>" name="calcFunc">
                    <option value="0">求和</option>
                    <option value="1">求平均值</option>
                    </select>
                    <a href='javascript:;' onclick="var pNode=this.parentNode; pNode.parentNode.removeChild(pNode);">×</a>
                    </div>
                    <script>
                    $("#calcFieldCode<%=curCalcuFieldCount%>").val("<%=key%>");
                    $("#calcFunc<%=curCalcuFieldCount%>").val("<%=json.get(key)%>");
                    </script>
                  <%
                  curCalcuFieldCount ++;
              }
          }
          %>
          </div>
        </div>
      </div>        
      <div>
      <div class="item_title">流程</div>
        <div class="item_content">
            <div id="divRelated">
              状态
              <select id="flowStatus" name="flowStatus">
              <option value="1000" selected>状态不限</option>
              <option value="<%=WorkflowDb.STATUS_NOT_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%></option>
              <option value="<%=WorkflowDb.STATUS_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%></option>
              <option value="<%=WorkflowDb.STATUS_FINISHED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%></option>
              <option value="<%=WorkflowDb.STATUS_DISCARDED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%></option>
              <option value="<%=WorkflowDb.STATUS_REFUSED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%></option>
              <option value="2000" selected>智能模块</option>
              </select>
              <%if (id!=-1) {%>
              <script>
              o("flowStatus").value = <%=aqd.getFlowStatus()%>;
              </script>
              <%}%>
              <br />
              开始时间从<input id="flowBeginDate1" name="flowBeginDate1" size="6" value="<%=DateUtil.format(aqd.getFlowBeginDate1(), "yyyy-MM-dd")%>" />
              <script type="text/javascript">
                $('#flowBeginDate1').datetimepicker({
                	//yearOffset:222,
                	lang:'ch',
                	timepicker:false,
                	format:'Y-m-d',
                	formatDate:'Y/m/d',
                	//minDate:'-1970/01/02', // yesterday is minimum date
                	//maxDate:'+1970/01/02' // and tommorow is maximum date calendar
                });
                </script> 
              至
              <input id="flowBeginDate2" name="flowBeginDate2" size="6" value="<%=DateUtil.format(aqd.getFlowBeginDate2(), "yyyy-MM-dd")%>" />
              <script type="text/javascript">
                $('#flowBeginDate2').datetimepicker({
                	//yearOffset:222,
                	lang:'ch',
                	timepicker:false,
                	format:'Y-m-d',
                	formatDate:'Y/m/d',
                	//minDate:'-1970/01/02', // yesterday is minimum date
                	//maxDate:'+1970/01/02' // and tommorow is maximum date calendar
                });
                </script>                
            </div>
        </div>
      </div>        
      <div class="item_title">主表关联查询&nbsp;&nbsp;[<a href="javascript:;" onclick="selQuery(curTableShortCode)">选择</a>]</div>
        <div class="item_content">
            <div id="divRelatedQuery">
            <%
            String queryRelated = aqd.getQueryRelated();
            String[] qary = StrUtil.split(queryRelated, ",");
            int len = 0;
            if (qary!=null)
              len = qary.length;
            for (int i=0; i<len; i++) {
              FormQueryDb fq = aqd.getFormQueryDb(StrUtil.toInt(qary[i]));
              %>
              <div id="divQuery<%=fq.getId()%>"><a href="javascript:;" onclick="loadDesignerFrame('<%=fq.getId()%>', '<%=id%>')"><%=fq.getQueryName()%></a><span style="color:red; font-size:14px; padding-left:5px; cursor:pointer" title="删除" onclick="delQuery('<%=fq.getId()%>')">×</span></div>
              <%
            }
            %>
            </div>
            <input id="queryRelated" name="queryRelated" type="hidden" value="<%=aqd.getQueryRelated()%>" />
        </div>
        
        <div style="display:none">
          <div class="item_title">副表关联查询</div>
          <div class="item_content">
              <div id="divRelated">
              <%
			  Iterator ir2 = aqd.getSubRelatedQuery().iterator();
              while (ir2.hasNext()) {
                FormQueryDb fq = (FormQueryDb)ir2.next();				
                %>
                <div id="divQuery<%=fq.getId()%>"><a href="javascript:;" onclick="addMyTab('<%=fq.getId()%>', '<%=fq.getQueryName()%>');"><%=fq.getQueryName()%></a>&nbsp;&nbsp;[<a href="javascript:;" onclick="launch('<%=fq.getId()%>', '<%=fq.getQueryName()%>')">运行</a>]</div>
                <%
              }
              %>
              <script>
              function addMyTab(qId, qName) {
				var url = "<%=request.getContextPath()%>/flow/designer/designer.jsp?id=" + qId;
				if (window.top.mainFrame) {
					addTab({'title':qName, 'html':'<iframe src=' + url + ' width=100% height=100% frameborder=0></iframe>'});
				}              
              }
              </script>
              </div>
          </div>
        </div>        
        <%
			FormDb fd = new FormDb();
			String curUnitCode = privilege.getUserUnitCode(request);		
		%>
        <div class="item_title">数据表</div>
          <div class="item_content">
       		<%
			// System.out.println(getClass() + " formCode=" + formCode );
			if (id!=-1 && !formCode.equals("")) {
				fd = fd.getFormDb(formCode);
				%>
                <div class="tableName" id="table_<%=fd.getCode()%>" name="table_name" style="cursor:pointer" onclick="getTableFields('<%=fd.getCode()%>', '<%=fd.getName()%>')"><%=fd.getName()%></div>
				<%
			}
			else {
				String sql = "select code from " + fd.getTableName() + " where unit_code=" + StrUtil.sqlstr(curUnitCode) + " order by code asc";	
				String clsName = "tableName";
				Iterator ir = fd.list(sql).iterator();
				while (ir.hasNext()) {
					fd = (FormDb) ir.next();
					ModulePrivDb mpd = new ModulePrivDb(fd.getCode());
					if (!mpd.canUserManage(privilege.getUser(request))) {
						continue;
					}
					if (fd.getCode().equals(formCode)) {
						clsName = "tableNameHover";
					}
					%>
					<div class="<%=clsName%>" id="table_<%=fd.getCode()%>" name="table_name" style="cursor:pointer" onclick="getTableFields('<%=fd.getCode()%>', '<%=fd.getName()%>')"><%=fd.getName()%></div>
					<%
					clsName = "tableName";
				}
			}
       		%>
          </div>
      </div>

      <div id="item_order" class="rebbonItem" style="display:none">
        <div class="item_title">排序</div>
          <div class="item_content">        
        <table cellSpacing="0" cellPadding="0" width="100%" align="center" border="0">
          <tbody>
            <%
				String orderFieldCodeStr = "";
				if (id!=-1)
					orderFieldCodeStr = aqd.getOrderFieldCode();
				String[] orderFieldCodeArr = orderFieldCodeStr.split(",");
				int orderFieldCodeArrLen = 0;
				if (orderFieldCodeArr!=null)
					orderFieldCodeArrLen = orderFieldCodeArr.length;
				
				Iterator irOrder = fd.getFields().iterator();
				while (irOrder!=null && irOrder.hasNext()) {
				   FormField ff = (FormField)irOrder.next();
				   String fieldCode = ff.getName();
				   int j = 0;
				   String asc = "", desc = "";
				   while(j < orderFieldCodeArrLen){
					  if(orderFieldCodeArr[j].equals(fieldCode +" ASC")){
						 asc = "selected";
					  }
					  if(orderFieldCodeArr[j].equals(fieldCode +" DESC")){
						 desc = "selected";
					  }
					  j++;
				   }
        %>
            <tr>
              <td noWrap width="109"><%=ff.getTitle()%></td>
              <td noWrap>
                <select id="orderFieldCode" name="orderFieldCode">
                  <option value="">无</option>
                  <option value="<%=fieldCode%> ASC" <%if(!asc.equals("")){out.print(asc);}%>>升续</option>
                  <option value="<%=fieldCode%> DESC" <%if(!desc.equals("")){out.print(desc);}%>>降续</option>                  
                  </select>
                </td>
              </tr>
            <%
               }
        %>              
            </tbody>
          </table>
        </div>
      </div>
      
      <div id="item_fields_selected" class="rebbonItem" style="display:none">
        <div class="item_title">已选字段</div>
        <div class="item_content">             
        <ul id="item_fields_selected_ul">
          <%
	if (id!=-1) {
        FormQueryConditionDb aqcd = new FormQueryConditionDb();
        Vector vt = aqcd.list(FormSQLBuilder.getFormQueryCondition(id));
        Iterator ir = vt.iterator();
		
		fd = fd.getFormDb(formCode);

		HashMap map = new HashMap();
        while (ir != null && ir.hasNext()) {
            aqcd = (FormQueryConditionDb) ir.next();
			if (aqcd.getFieldCode().equalsIgnoreCase("user_name"))
				continue;
			if (map.containsKey(aqcd.getConditionFieldCode()))
				continue;
			map.put(aqcd.getConditionFieldCode(), aqcd.getConditionFieldCode());
			
			FormField ff = fd.getFormField(aqcd.getFieldCode());
			
			if (ff==null)
				continue;
			
			String tableShortCode = formCode;
			out.print("<li title='双击删除' id='" + tableShortCode + "-" + aqcd.getFieldCode() + "' code='" + aqcd.getFieldCode() + "' tableCode='" + formCode + "' tableShortCode='" + formCode + "'>" + ff.getTitle() + "</li>");
        }
	}
	%>
          </ul>
    <script>
	$("#item_fields_selected_ul > li").each(function(k) {
		$(this).bind("dblclick", itemFieldsSelectedOndblClick);
	});
	</script>
    	</div>
      </div>        
      </div>
    </div>
</div>
</td></tr></table>
<iframe id="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
</body>
<%
String rootPath = request.getContextPath();
%>
<script>
var errDoDeptOnClick = function(response) {
	window.status = 'Error ' + response.status + ' - ' + response.statusText;
	// alert(response.responseText);
}

function doDeptOnClick(response){
	var rsp = response.responseText.trim();
	if(rsp!=""){
		var curObj = document.getElementById("chk_" + curDeptCode);
		var temp = rsp.split(",");
		var length = temp.length;
  		for(var i = 0; i < length; i ++){
  			var obj = document.getElementById("chk_"+temp[i]);
  			if(obj){
				if (curObj.checked)
  					obj.checked = true;
				else
					obj.checked = false;
  			}
  		}
	}
}

function ShowChild(imgobj, name) {
	var tableobj = findObj("childof"+name);
	if (tableobj==null) {
		document.frames.ifrmGetChildren.location.href = "../../admin/dept_ajax_getchildren.jsp?op=funcCheckbox&func=func&target=_self&isOnlyShowDeptAdmined=true&parentCode=" + name;
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "<%=rootPath%>/images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1) {
			imgobj.src = "<%=rootPath%>/images/i_minus.gif";
		}
		else
			imgobj.src = "<%=rootPath%>/images/i_plus.gif";
		return;
	}
	if (tableobj.style.display=="none")	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "<%=rootPath%>/images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "<%=rootPath%>/images/i_minus.gif";
		else
			imgobj.src = "<%=rootPath%>/images/i_plus.gif";
	}
	else {
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "<%=rootPath%>/images/i_minus.gif";
		else
			imgobj.src = "<%=rootPath%>/images/i_plus.gif";
	}	
}

function insertAdjacentHTML(objId,code,isStart){
	var obj = document.getElementById(objId);
	if(isIE())
		obj.insertAdjacentHTML(isStart ? "afterbegin" : "afterEnd",code);
	else{ 
		var range=obj.ownerDocument.createRange();
		range.setStartBefore(obj);
		var fragment = range.createContextualFragment(code);
		if(isStart) 
			obj.insertBefore(fragment,obj.firstChild);
		else
			obj.appendChild(fragment);
	}
}

function errFunc(response) {
	window.status = 'Error ' + response.status + ' - ' + response.statusText;
}

function checkAll(isChecked){
   var ary = new Array();
   var j = 0;
   for(var i=0; i<formDept.elements.length; i++) {
   		if (formDept.elements[i].type=="checkbox"){
			if(formDept.elements[i].disabled == false){
				formDept.elements[i].checked = isChecked;
			}
   		}
   }
   return ary;
}

function handlerOnClick() {
	var obj = window.event.srcElement;
	if (obj.type=="checkbox") {
		;
	}
}

function doSelQuery(id, title) {
	if (id=="<%=aqd.getId()%>") {
		jAlert("查询不能与自身关联！","提示");
		return;
	}
	// 检查是否有重复
	var ids = $("#queryRelated").val();
	if (("," + ids + ",").indexOf("," + id + ",")!=-1) {
		jAlert("该查询已关联！","提示");
		return;
	}
	if (ids=="")
		ids = id;
	else
		ids += "," + id;
	$("#queryRelated").val(ids);
	
	$("#divRelatedQuery").html($("#divRelatedQuery").html() + "<div id='divQuery" + id + "'><a href='javascript:;' onclick=\"loadDesignerFrame('" + id + "', '<%=id%>')\">" + title + "</a><span style='color:red; font-size:14px; padding-left:5px; cursor:pointer' title='删除' onclick=\"delQuery('" + id + "')\">×</span></div>");
	
	save();
}

function selQuery(formCode) {
	if ($('#queryRelated').val()!="") {
		jAlert("只能关联一条查询！","提示");
		return;
	}
	openWin("../form_query_list_sel.jsp?formCode=" + formCode + "&isSystem=<%=isSystem%>", 800, 600);
}

function delQuery(id) {
	$('#divQuery' + id).remove();
	var ary = $("#queryRelated").val().split(",");
	var str = "";
	for (var i=0; i<ary.length; i++) {
		if (ary[i]==id) {
			continue;
		}
		if (str=="")
			str = ary[i];
		else
			str += "," + ary[i];
	}
	$('#queryRelated').val(str);
}

$.fn.outerHTML = function(){  return $("<p></p>").append(this.clone()).html(); }

function addCalcuField() {
	if (o("divCalcuField0"))
		$("#divCalcuField").append($("#divCalcuField0").outerHTML());
	else
		initDivCalcuField();
}

function loadDesignerFrame(mainId, subId) {
	if (!window.parent.designerLeftFrame)
		window.location.href="designer_frame.jsp?mainFormQueryId=" + mainId + "&subFormQueryId=" + subId;
	else
		window.parent.location.href="designer_frame.jsp?mainFormQueryId=" + mainId + "&subFormQueryId=" + subId;
}

function save(isMute) {
	$('#formConditionFieldCode').submit();
	modifyDeptCodesAndOthers(isMute);
	isNew = false;
}

function saveBeforeLaunch() {
	// 如果主表查询一起在框架页内显示
	if (window.parent.designerLeftFrame) {
		if (window.parent.designerLeftFrame==this)
			window.parent.designerRightFrame.save(true);
		else
			window.parent.designerLeftFrame.save(true);
		save(true);
	}
	else
		save(true);
}

function launch(id, title) {
	if (top.mainFrame)
		top.mainFrame.addTab(title, '<%=request.getContextPath()%>/flow/form_query_list_do.jsp?id=' + id + '&op=query');	  
	else
		window.location.href='<%=request.getContextPath()%>/flow/form_query_list_do.jsp?id=' + id + '&op=query';
}
</script>
</html>