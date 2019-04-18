<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="salary.admin";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");
// formCode = "contract";
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);
if (fd==null || !fd.isLoaded()) {
	out.println(StrUtil.Alert("表单不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserAppend(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

String modUrlList = StrUtil.getNullStr(msd.getString("url_list"));

if (modUrlList.equals("")) {
	modUrlList = request.getContextPath() + "/" + "salary/salary_list.jsp?formCode=" + StrUtil.UrlEncode(formCode);
}
else {
	modUrlList = request.getContextPath() + "/" + modUrlList;
}
// 置嵌套表需要用到的pageType
request.setAttribute("pageType", "add");

if (op.equals("saveformvalue")) {
	boolean re = false;
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		re = fdm.create(application, request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect("保存成功！", modUrlList));
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计-添加内容</title>
<meta name="renderer" content="ie-stand">
<script src="../inc/livevalidation_standalone.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery.raty.min.js"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=Global.getRootPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../inc/upload.js"></script>
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
		alert("您必须选择一项意见!");
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
</head>
<body>
<%@ include file="salary_inc_menu_top.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<div class="spacerH"></div>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" bgcolor="#FFFFFF">
  <form action="?op=saveformvalue&formCode=<%=StrUtil.UrlEncode(formCode)%>" method="post" enctype="multipart/form-data" name="visualForm" id="visualForm">
    <tr>
      <td align="left">
         
	  <%com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, fd);
			out.print(rd.rendForAdd());
		  %>
          <br />
	
    </td>
    </tr>
 
    <tr>
      <td height="30" align="center"><input class="btn" type="submit" name="Submit" value=" 添 加 " />
        &nbsp;&nbsp;</td>
    </tr>
  </form>
</table><br />
</body>
</html>
