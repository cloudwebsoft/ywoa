<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int flow_id = ParamUtil.getInt(request, "flowId");
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flow_id);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>修改流程</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
<script src="inc/upload.js"></script>
<script src="js/jquery.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery.toaster.js"></script>
<script>
function testAction() {
}

function showProp() {
}

function hideDesigner() {
	$("#designerDiv").hide();
}
</script>
</head>
<body onunload="hideDesigner()">
<%@ include file="flow_modify_inc_menu_top.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<div class="spacerH"></div>
<%
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String flowExpireUnit = cfg.get("flowExpireUnit");
boolean isHour = !flowExpireUnit.equals("day");	
if (flowExpireUnit.equals("day")){
	String str = LocalUtil.LoadString(request,"res.flow.Flow","day");
	flowExpireUnit = str;
}
else{
	String str = LocalUtil.LoadString(request,"res.flow.Flow","hour");
	flowExpireUnit = str;
}
%>
<table align="center" class="tabStyle_1 percent80">
    <tr>
      <td class="tabStyle_1_title"><%=wf.getTitle()%>&nbsp;<lt:Label res="res.flow.Flow" key="flowChart"/></td>
    </tr>
    <tr>
      <td style="background-color: #fff">
      <div id="designerDiv">
      <object id="Designer" classid="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" codebase="activex/cloudym.CAB#version=1,3,0,0" style="width:100%; height:515px">
        <param name="Workflow" value="<%=wf.getFlowString()%>" />
        <param name="Mode" value="view" />
        <!--debug user initiate complete-->
        <param name="CurrentUser" value="<%=privilege.getUser(request)%>" />
        <param name="ExpireUnit" value="<%=flowExpireUnit%>" />
		<%
        com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();	  
        %>
        <param name="Organization" value="<%=license.getCompany()%>" />
        <param name="Key" value="<%=license.getKey()%>" />     
        <param name="LicenseType" value="<%=license.getType()%>" />        
      </object>
      </div>
      </td>
    </tr>
    <tr>
      <td align="center"><input class="btn" name="btnPlay" type="button" reserve="true" onclick="PlayDesigner()" value='<lt:Label res="res.flow.Flow" key="playbackProcess"/>' /></td>
    </tr>
</table>
</body>
<script>
var playCount = 0;

function PlayDesigner() {
	if (Designer.style.width=="0px")
		ShowDesigner();
		
	var ary = new Array();
<%
	String sql = "select id from flow_my_action where flow_id=" + flow_id + " order by receive_date asc";
	MyActionDb mad = new MyActionDb();
	Vector v = mad.list(sql);

	java.util.Iterator ir = v.iterator();
	int kk = 0;
	while (ir.hasNext()) {
		mad = (MyActionDb)ir.next();
		WorkflowActionDb wa = new WorkflowActionDb();
		wa = wa.getWorkflowActionDb((int)mad.getActionId());
		%>
		ary[<%=kk%>] = [<%=mad.getReceiveDate().getTime()%>, "<%=wa.getInternalName()%>", "<%=mad.getActionStatus()%>"]; // 到达
		<%
		kk++;
		%>
		ary[<%=kk%>] = [<%=mad.getCheckDate()!=null?mad.getCheckDate().getTime()+"":"999999999999999"%>, "<%=wa.getInternalName()%>", "<%=mad.isChecked()?4:mad.getActionStatus()%>"]; // 处理
		<%
		kk++;
	}
%>
	if (playCount==0) {
		// 对ary中的元素按照时间排序
		ary.sort(function(a,b){return parseInt(a[0])-parseInt(b[0])}) 
	}
	
	Designer.SelectAction(ary[playCount][1]);
	Designer.ActionTitle = Designer.ActionTitle;
	Designer.ActionJobCode = Designer.ActionJobCode;
	Designer.ActionJobName = Designer.ActionJobName;
	Designer.ActionUser = Designer.ActionUser;
	Designer.ActionUserRealName = Designer.ActionUserRealName;	
	Designer.ActionFlag = Designer.ActionFlag;
	Designer.ActionDeptMode = Designer.ActionDeptMode;

	Designer.ActionCheckState = ary[playCount][2];
	Designer.ModifyAction();

	playCount ++;
	
	if (playCount==ary.length) {
		//jAlert('<lt:Label res="res.flow.Flow" key="endPlayback"/>','提示');
		$.toaster({ priority : 'info', message : '<lt:Label res="res.flow.Flow" key="endPlayback"/>' });
		playCount = 0;
		return;
	}
	
	timeoutid = window.setTimeout("PlayDesigner()", "1000");
}
</script>
</html>
