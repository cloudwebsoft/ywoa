<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>流程移交</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>

<script>
var curObj, curObjShow
function setPerson(deptCode, deptName, userName, userRealName)
{
	curObj.value = userName;
	curObjShow.value = userRealName;
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
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin.flow";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("handover")) {
	WorkflowMgr wfm = new WorkflowMgr();
	String[] typeCode = ParamUtil.getParameters(request, "typeCode");
	// System.out.println(getClass() + " " + typeCode.length);
	int ret = 0;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		ret = wfm.handover(request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	out.print(StrUtil.jAlert_Redirect("操作成功，共移交流程" + ret + "个！","提示", "flow_handover.jsp"));
	return;
}
%>
<form action="flow_handover.jsp?op=handover" method="post">
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">流程移交</td>
    </tr>
  </tbody>
</table>
<table width="100%" align="center" cellPadding="0" cellSpacing="0" class="tabStyle_1 percent80" id="mapTable" style="padding:0px; margin:10px;">
  <tbody>
    <tr>
      <td height="28" colspan="2" class="tabStyle_1_title">流程移交</td>
    </tr>
    <tr>
      <td height="42" align="center">
		选择流程
	  </td>
      <td width="86%" height="42" align="left">
      <select name="typeCode" size="15" multiple="multiple" id="typeCode" style="width:300px;height:150px">
      <%
	  Leaf lf = new Leaf();
	  lf = lf.getLeaf(Leaf.CODE_ROOT);
	  Iterator ir = lf.getChildren().iterator();
	  while (ir.hasNext()) {
		  lf = (Leaf)ir.next();
		  
		  LeafPriv lp = new LeafPriv(lf.getCode());
          if (!lp.canUserExamine(privilege.getUser(request)))
		  	continue;
		  
		  if (lf.getType()!=0) {
	  %>
          <option value="<%=lf.getCode()%>"><%=lf.getName()%></option>
      <%}
	  	else {
			Iterator ir2 = lf.getAllChildOfUnit(new Vector(), lf, privilege.getUserUnitCode(request)).iterator();
			while (ir2.hasNext()) {
				lf = (Leaf)ir2.next();
				if (lf.getType()!=0) {
					%>
                    <option value="<%=lf.getCode()%>"><%=lf.getName(request)%></option>
                    <%
				}
			}
		}
	  }
	  %>
      </select>
      <br />
      <input type="button" class="btn" value="全选" onclick="$('#typeCode').children().attr('selected', true)" />
      <input type="button" class="btn" value="不选" onclick="$('#typeCode').children().attr('selected', false)" />
      按住Ctrl键可多选
      </td>
    </tr>
    <tr>
      <td width="14%" align="center">
      原办理人</td>
      <td align="left">
      	<input id="oldUserName" name="oldUserName" type="hidden" />
      	<input id="oldUserRealName" name="oldUserRealName" readonly="readonly" />
        <input class="btn" type="button" onclick="javascript:curObj=o('oldUserName');curObjShow=o('oldUserRealName');openWin('../user_sel.jsp', 800, 600)" value="选择用户" /></td>
    </tr>
    <tr>
      <td align="center">移交给</td>
      <td align="left">
      	<input id="newUserName" name="newUserName" type="hidden" />
      	<input id="newUserRealName" name="newUserRealName" readonly="readonly" />
   	  <input class="btn" type="button" onclick="javascript:curObj=o('newUserName');curObjShow=o('newUserRealName');openWin('../user_sel.jsp', 800, 600)" value="选择用户" /></td>
    </tr>
    <tr>
      <td align="center">日期</td>
      <td align="left">
      从
      <input id="begin_date" name="begin_date" size=10 readonly>
      至
      <input id="end_date" name="end_date" size=10 readonly>
            
			<script>
              var oldUserRealName = new LiveValidation('oldUserRealName');
              oldUserRealName.add(Validate.Presence);		
              var newUserRealName = new LiveValidation('newUserRealName');
              newUserRealName.add(Validate.Presence);		
            </script>         
            
      </td>
    </tr>
    <tr>
      <td colspan="2" align="center">
      <input class="btn" type="submit" value="确定" />
      </td>
    </tr>
  </tbody>
</table>
</form>
<script>
$(function(){
	$('#begin_date').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
    $('#end_date').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
})
</script>
</body>
</html>