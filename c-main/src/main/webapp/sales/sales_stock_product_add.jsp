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
// 取得皮肤路径
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))
	skincode = UserSet.defaultSkin;

SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String pageType = ParamUtil.get(request, "pageType");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加产品</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/jquery-simple-tree/jquery.simple.tree.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/jquery.simple.tree.js"></script>
<%@ include file="../inc/nocache.jsp"%>
<script type="text/javascript">
var simpleTreeCollection;
$(document).ready(function(){
	simpleTreeCollection = $('.simpleTree').simpleTree({
		drag: false,
		autoclose: true,
		isCloseNearby: false, // 只显示当前选中节点的子节点
		rootPath: '<%=request.getContextPath()%>', // trigger图片路径
		afterClick:function(node){                   
			// alert("您选择了：" + $('span:first',node).text() + "id为：" + $('span:first',node).attr("id"));
		},
		afterDblClick:function(node){
			//alert("text-"+$('span:first',node).text());//双击事件
			var productId = $('span:first', node).attr("productId");
			if (productId)
				addRow(productId);
		},
		afterMove:function(destination, source, pos){
			//alert("destination-"+destination.attr('id')+" source-"+source.attr('id')+" pos-"+pos);//拖拽事件
		},
		afterAjax:function()
		{
			//alert('Loaded');
		},
		animate:true
		//,docToFolderConvert:true
	});
});

var errFunc = function(response) {
	// alert('Error ' + response.status + ' - ' + response.statusText);
	alert(response.responseText);
}

function addRow(productId) {
	var myAjax = new cwAjax.Request(
		"sales_stock_product_add_ajax.jsp",
		{
			method:"post",
			parameters:"op=prop&productId=" + productId,
			onComplete:doAddRow,
			onError:errFunc
		}
	);
	/*
	$.ajax({
		type: "post",
		url: "sales_stock_product_add_ajax.jsp",
		data : {
			op: "prop222",
        	productId : productId
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			alert(data);
			if (data.trim()!="") {
				data = $.parseJSON(data);
				// alert(data.html);
				doAddRow(data);
			}
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});
	*/
}

function doAddRow(response) {
	var data = response.responseText.trim();
	data = $.parseJSON(data);
	
	if (isExist(data.productId)) {
		alert(data.productName + "已存在！");
		return;
	}
	
	var the_row = -1;
	var newrow = productTable.insertRow(the_row);
	for (var i=0;i<productTable.rows[0].cells.length;i++) {
		the_cell = newrow.insertCell(i);
		if (i==0)
			the_cell.innerHTML = "<input name='product_realshow' value='" + data.productName + "' size=6><input name='productId' size=5 value='" + data.productId + "' type='hidden'>";
		else if (i==1)
			the_cell.innerHTML = "<input name='num' size=3>" + data.measure_unit;
		else if (i==2)
			the_cell.innerHTML = "<input name='remark' size=10>";
		else if (i==3) {
			the_cell.innerHTML = "<a href='javascript:;' onclick='delRow(" + the_cell.parentElement.rowIndex + ")'>删除</a>";
		}
	}
}

function delRow(index) {
	productTable.deleteRow(index);
}

function isExist(productId) {
	var obj = document.getElementById("productTable");
	var rows = obj.rows.length;
	var str = "";
	for (var i=1; i<rows; i++) {
		var cel = obj.rows.item(i).cells;
		if (cel[0].children.length>1) {
			cellInput = cel[0].children[1];
			if (cellInput.value==productId) {
				return true;
			}
		}
	}
	return false;
}

function ok() {
	var obj = document.getElementById("productTable");
	var rows = obj.rows.length;
	var str = "";
	
	// 清除原来嵌套表中的数据
	nestTable = window.opener.document.getElementById("cwsNestTable");
	var nestRowsLen = nestTable.rows.length;
	for (var i=nestRowsLen-1; i>=1; i--) {
		nestTable.deleteRow(i);
	}
	
	for (var i=1; i<rows; i++) {
		var cel = obj.rows.item(i).cells;
		var newRow = window.opener.add_row(nestTable);
		newCells = newRow.cells;
		for (var j=0; j<cel.length-1; j++) {
			var k = j;
			<%
			if (!pageType.equals("add")) {
				%>
				k++;
				<%
			}
			%>
			if (cel[j].children.length>1) {
				var cellInput = cel[j].children[0];
				newCells[k].innerHTML = cellInput.value;
				cellInput = cel[j].children[1];
				newCells[k].setAttribute("value", cellInput.value);
			}
			else
				newCells[k].innerHTML = cel[j].children[0].value;
		}
	}
	window.close();
}

function onload() {
	var nestTable = window.opener.document.getElementById("cwsNestTable");
	
	var rows = nestTable.rows.length;
	var the_row = -1;
	for (var i=1; i<rows; i++) {
		var cel = nestTable.rows.item(i).cells;

		var newRow = productTable.insertRow(the_row);

		var j = 0;
		<%
		if (!pageType.equals("add")) {
			%>
			j++;
			<%
		}
		%>
		the_cell = newRow.insertCell(0);
		the_cell.innerHTML = "<input name='productName' value='" + cel[j].innerText + "' size=6><input name='productId' value='" + cel[j].getAttribute("value") + "' type='hidden'>";
		the_cell = newRow.insertCell(1);
		the_cell.innerHTML = "<input name='num' size=3 value='" + cel[j+1].innerText + "'>";
		the_cell = newRow.insertCell(2);
		the_cell.innerHTML = "<input name='remark' size=10 value='" + cel[j+2].innerText + "'>";
		the_cell = newRow.insertCell(3);
		the_cell.innerHTML = "<a href='javascript:;' onclick='delRow(" + the_cell.parentElement.rowIndex + ")'>删除</a>";
	}
}
</script>
</head>
<body onload="onload()">
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">添加产品</td>
    </tr>
  </tbody>
</table>

<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="43%">
    <ul class="simpleTree">
        <li class="root"><span>产品分类</span>
            <ul class="ajax">
              <li id='sales_product_type'>{url:sales_stock_product_add_ajax.jsp?op=dir&dirCode=sales_product_type}</li>
            </ul>
        </li>
    </ul>
	</td>
    <td width="57%" align="center" valign="top"><table class="tabStyle_1 percent98" id="productTable" width="98%" border="0" cellspacing="0" cellpadding="0">
	  <thead>
	    <tr>
	      <td width="24%">产品名称</td>
	      <td width="24%">数量</td>
	      <td width="27%">备注</td>
	      <td width="25%">操作</td>
	      </tr>
	    </thead>
        <tbody></tbody>
    </table>
    <input value="确定" onclick="ok()" type="button" />
    </td>
  </tr>
</table>




</body>
</html>