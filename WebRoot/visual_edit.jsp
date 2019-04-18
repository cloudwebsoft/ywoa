<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<script src="<%=request.getContextPath()%>/inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="<%=request.getContextPath()%>/js/jquery.raty.min.js"></script>
<script src="<%=request.getContextPath()%>/inc/livevalidation_standalone.js"></script>
<script src="<%=request.getContextPath()%>/inc/upload.js"></script>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	// out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}
String myname = privilege.getUser( request );

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

int id = ParamUtil.getInt(request, "id");
FormMgr fm = new FormMgr();
FormDb fdEdit = fm.getFormDb(formCode);

// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + id);
// 置嵌套表需要用到的pageType
request.setAttribute("pageType", "edit");

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdEdit);

if (op.equals("delAttach")) {
	int attachId = ParamUtil.getInt(request, "attachId");
	com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attachId);
	att.del();
}
%>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>

<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css" />
<script src="../js/jquery.bgiframe.js"></script>


<script>
function setradio(myitem,v)
{
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
       for (i=0; i<radioboxs.length; i++)
          {
            if (radioboxs[i].type=="radio")
              {
                 if (radioboxs[i].value==v)
				 	radioboxs[i].checked = true;
              }
          }
     }
}

function SubmitResult() {
	// 检查是否已选择意见
	if (getradio("resultValue")==null || getradio("resultValue")=="") {
		jAlert("您必须选择一项意见!","提示");
		return false;
	}
	visualForm.op.value='finish';
	visualForm.submit();
}

// 控件完成上传后，调用Operate()
function Operate() {
	// alert(redmoonoffice.ReturnMessage);
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

<!-- 
<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
 -->
 
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script type="text/javascript">
$(function() {
	SetNewDate()
});
</script>
 
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (op.equals("saveformvalue")) {
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = fdm.update(application, request);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
		%>
		<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");		
		</script>
		<%		
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("保存成功！","提示", request.getRequestURL() + "?id=" + id + "&formCode=" + StrUtil.UrlEncode(formCode)));
		return;
	}
	// out.print(StrUtil.Alert_Back("保存成功！"));
}
%>
<form action="?op=saveformvalue&id=<%=id%>&formCode=<%=StrUtil.UrlEncode(formCode)%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="left"><table width="100%">
        <tr>
          <td><%
			com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fdEdit);
			out.print(rd.rend());
		  %></td>
        </tr>
      </table></td>
    </tr>
    <tr>
      <td align="left"><script>initUpload()</script>
      </td>
    </tr>
    <tr>
      <td align="left"><%
com.redmoon.oa.visual.FormDAO fdaoEdit = fdm.getFormDAO(id);
Iterator ir = fdaoEdit.getAttachments().iterator();
				  while (ir.hasNext()) {
				  	com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) ir.next(); %>
          <table width="82%"  border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td width="5%" height="31" align="right"><img src="<%=Global.getRootPath()%>/images/attach.gif" /></td>
              <td>&nbsp; <a target="_blank" href="<%=Global.getRootPath()%>/visual_getfile.jsp?attachId=<%=am.getId()%>"><%=am.getName()%></a>&nbsp;&nbsp;<a href="#" onclick="jConfirm('您确定要删除吗？','提示',function(r){ if(!r){return;}else{ window.location.href='?op=delAttach&amp;id=<%=id%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>&amp;attachId=<%=am.getId()%>'}}) " style="cursor:pointer">删除</a><br />
              </td>
            </tr>
          </table>
        <%}
			  %></td>
    </tr>
    <tr>
      <td height="30" align="center"><input name="id" value="<%=id%>" type="hidden" />
          <input type="submit" class="btn" name="Submit" value=" 修 改 " />
        &nbsp;&nbsp;</td>
    </tr>
</table>
</form>
