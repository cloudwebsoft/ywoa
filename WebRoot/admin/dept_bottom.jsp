<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import = "cn.js.fan.cache.jcs.*"%>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<META HTTP-EQUIV="pragma" CONTENT="no-cache"> 
<META HTTP-EQUIV="Cache-Control" CONTENT= "no-cache, must-revalidate"> 
<META HTTP-EQUIV="expires" CONTENT= "Wed, 26 Feb 1997 08:21:57 GMT">
<title>部门信息管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script>
function form1_onsubmit() {
	form1.root_code.value = window.parent.dirmainFrame.getRootCode();
}

function selectNode(code, name) {
	o("parentCode").value = code;
	o("parentName").innerHTML = name;
}

</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div> 
<%
String op = ParamUtil.get(request, "op");
%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><span class="thead" style="PADDING-LEFT: 10px">
      <%if (op.equals("modify")) {%>
      部门修改
      <%}else{%>
      部门增加
      <%}%>
      </span></td>
    </tr>
  </tbody>
</table>
<%
Privilege pvg = new Privilege();
String parent_code = ParamUtil.get(request, "parent_code");
if (parent_code.equals(""))
	parent_code = "root";
String parent_name = ParamUtil.get(request, "parent_name");
String code = ParamUtil.get(request, "code");
String name = ParamUtil.get(request, "name");
String description = ParamUtil.get(request, "description");
String shortName = ParamUtil.get(request, "shortName");
int type = 0;
if (op.equals(""))
	op = "AddChild";
	
if (op.equals("AddChild")) {
	if (!pvg.canUserAdminDept(request, parent_code)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
	
DeptDb leaf = null;
DeptMgr dir = new DeptMgr();

// 取得最初父节点最后一个孩子节点的编码（最初表示在移动之前，以为不能直接将父节点最后一个孩子节点加1变成新节点的编码，因为节点被移动后，编码是不变的，这个节点有可能被移动到其他节点下面了）
String parentLastChildCode = "";

if (op.equals("modify")) {
	if (!Privilege.canUserAdminDept(request, code)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	leaf = dir.getDeptDb(code);
	name = leaf.getName();
	description = leaf.getDescription();
	type = leaf.getType();
	shortName = leaf.getShortName();
} else {
	int codeCount = 0;
	int index = 0;
	if(!"root".equals(parent_code)){
		index = parent_code.length();
	}
	DeptDb pdd = new DeptDb();
	pdd = pdd.getDeptDb(parent_code);//得到父节点
	Vector children = dir.getChildren(parent_code);
	if(children.isEmpty()){
		codeCount = 1;
	}else{
		int count = children.size();
		Iterator ri = children.iterator();
		int i = 0;
		int[] arr = new int[count];
		while (ri.hasNext()) {
			DeptDb childlf = (DeptDb) ri.next();
			String eachCode = childlf.getCode();
			try{
				String diffCode = eachCode.substring(index);//去掉父节点code的前缀
				int NumberCode = Integer.valueOf(diffCode);
				arr[i] = NumberCode;
			}catch(Exception e){
			}
			i++;
		}
		Arrays.sort(arr);
		codeCount = arr[arr.length-1] +1;
	}
	parentLastChildCode = StrUtil.PadString(String.valueOf(codeCount), '0', 4, true);
	//DeptDb pdd = new DeptDb();
	//pdd = pdd.getDeptDb(parent_code);
	//得到父节点最初的子节点个数
	//int count = pdd.getChildCount() + 1;
	//parentLastChildCode = StrUtil.PadString(String.valueOf(count), '0', 4, true);
}

%>
<table align="center" class="tabStyle_1 percent80" >
  <form action="dept_do.jsp?op=<%=op%>" method="post" name="form1" target="dirhidFrame" id="form1" onsubmit="return form1_onsubmit()">
    <tr>
      <td align="left" id='codeText2'><span style="word-break:break-all">当前节点：
        <font color="blue"><%=parent_name.equals("")?"所有部门":parent_name%></font></span></td>
    </tr>
    <tr style="display:none">
      <td align="left" id='codeText'>编码：<%=code%></td>
        <input type="hidden" name="code" id="code" value="<%=code%>" <%=op.equals("modify")?"readonly":""%> />
        <%if (!op.equals("modify")) {%>
            <script>
                <%if (parent_code.equals("root")) {%>
	                o("code").value = "<%=parentLastChildCode%>";
	                $('#codeText').text("编码：<%=parentLastChildCode%>");
	            <%}else{%>
	                o("code").value = "<%=parent_code%><%=parentLastChildCode%>";
	                $('#codeText').text("编码：<%=parent_code%><%=parentLastChildCode%>");
	            <%}%>
            </script>
        <%}%>
    </tr>
    <tr>
      <td align="left">名称：
      <input name="name" id="name" value="<%=name%>" size=12/>
      <input type="hidden" name="parent_code" value="<%=parent_code%>" />
      <input type="hidden" name="root_code" value="" /></td>
    </tr>
    <tr>
      <td align="left">简称：
        <input id="shortName" name="shortName" value="<%=shortName%>" size=12/></td>
    </tr>
    <tr>
      <td align="left">显示：
      <select id="show" name="show">
      <option value="1" selected>是</option>
      <option value="0">否</option>
      </select>
	  <%if (op.equals("modify")) {%>
      <script>
      form1.show.value = "<%=leaf.isShow()?1:0%>";
      </script>
      <%}%>  
      </td>
    </tr>
    <%if (License.getInstance().isGroup() || License.getInstance().isPlatform()) {%>
    <tr>
      <td align="left">子单位
	  <select name="type" id="type">
	  <option value="<%=DeptDb.TYPE_DEPT%>" selected>否</option>
	  <option value="<%=DeptDb.TYPE_UNIT%>">是</option>
	  </select>
	  <%if (op.equals("modify")) {%>
      <script>
      form1.type.value = "<%=leaf.getType()%>";
      </script>
      <%}%>  
	  </td>
    </tr>
    <%}else{%>
    <tr style="display:none">
      <td align="left">
      <input name="type" value="<%=DeptDb.TYPE_DEPT%>" type="hidden" />
	  </td>
    </tr>    
    <%}%>
	<!--
    <tr>
      <td align="left">描述
        <input name="description" value="<%=description%>" /></td>
    </tr>-->
    <tr>
      <td align="left"><%if (op.equals("modify")) {%>
        <script>
		var bcode = "<%=leaf.getCode()%>";
		</script>
        <%if (code.equals(leaf.ROOTCODE)) {%>
        <input type="hidden" name="parentCode" value="<%=leaf.getParentCode()%>" />
        <%}else if (code.equals(pvg.getUserUnitCode(request))) {%>
        <input type="hidden" name="parentCode" value="<%=leaf.getParentCode()%>" />
        <%}else{%>
        父节点：
        <input id="parentCode" name="parentCode" type="hidden" value="<%=leaf.getParentCode()%>">
        <span id="parentName"><%=dir.getDeptDb(leaf.getParentCode()).getName()%></span>
		&nbsp;&nbsp;<a href="javascript:;" onclick="window.open('../dept_sel.jsp?dirCode=<%=DeptDb.ROOTCODE%>','_blank','toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=640,height=480')">选择</a>
        <%}%>
      <%}%></td>
    </tr>
    <tr>
      <td align="center"><input type="button" class="btn" onclick="mysubmit()" value="提交" />
        &nbsp;&nbsp;&nbsp;
      <input type="button" class="btn" onclick="myreset()" value="重置" /></td>
    </tr>
  </form>
</table>
<script>
function mysubmit(){
	form1.root_code.value = window.parent.dirmainFrame.getRootCode();
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display":"block"});
	$(".loading").css({"display":"block"});
	$.ajax({
		url: "dept_do.jsp?op=<%=op%>",
		type: "post",
		dataType: "json",
		data: $('#form1').serialize(),
		success: function(data, status){
			$(".loading").css({"display":"none"});
			$(".treeBackground").css({"display":"none"}); 
			$(".treeBackground").removeClass("SD_overlayBG2");
			if(data.ret == 1){
				window.parent.dirmainFrame.shensuo();
				if("modify"=="<%=op%>"){
					window.parent.dirmainFrame.modifyTitle($("#name").val(),$("#type").val());
				}else if("AddChild"=="<%=op%>"){
					window.parent.dirmainFrame.addNewNode($("#code").val(),$("#name").val(),$("#type").val());
				}
			}else if(data.ret == 2){
				jAlert(data.msg,"提示");
			}
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});	
}

function myreset(){
	document.getElementById("name").value = "";
	document.getElementById("shortName").value = "";
	document.getElementById("show").options[0].selected  = true;
	document.getElementById("type").options[0].selected = true;
}
</script>
</body>
</html>
