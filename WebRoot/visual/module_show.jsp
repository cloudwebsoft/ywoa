<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String myname = privilege.getUser( request );

String op = ParamUtil.get(request, "op");

String code = ParamUtil.get(request, "code");
if ("".equals(code)) {
	code = ParamUtil.get(request, "formCode");
}
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(code);
if (msd==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
}

int id = ParamUtil.getInt(request, "id");

ModulePrivDb mpd = new ModulePrivDb(code);
if (!mpd.canUserView(privilege.getUser(request))) {
	boolean canShow = false;
	// 从嵌套表格查看时访问
	String visitKey = ParamUtil.get(request, "visitKey");
	if (!"".equals(visitKey)) {
		String fId = String.valueOf(id);
		com.redmoon.oa.sso.Config ssoconfig = new com.redmoon.oa.sso.Config();
		String desKey = ssoconfig.get("key");
		visitKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(desKey, visitKey);
		if (visitKey.equals(fId)) {
			canShow = true;
		}
	}
	if (!canShow) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String formCode = msd.getString("form_code");
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

if (msd.getInt("view_show")==ModuleSetupDb.VIEW_SHOW_TREE) {
	boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
	if (!isInFrame) {
		response.sendRedirect(request.getContextPath() + "/" + "visual/module_show_frame.jsp?id=" + id + "&code=" + code);
		return;
	}
}

// 置嵌套表及关联查询选项卡生成链接需要用到的cwsId
request.setAttribute("cwsId", "" + id);
// 置嵌套表需要用到的页面类型
request.setAttribute("pageType", "show");
// 置NestSheetCtl需要用到的formCode
request.setAttribute("formCode", formCode);

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);

com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>智能模块设计-显示内容</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />
<script src="../inc/common.js"></script>
<script src="../inc/map.js"></script>
<script src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery.raty.min.js"></script>
<script src="../inc/flow_dispose_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCode%>.jsp?pageType=show&id=<%=id %>"></script>
<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

<link href="../js/select2/select2.css" rel="stylesheet" />
<script src="../js/select2/select2.js"></script>
<script src="../js/select2/i18n/zh-CN.js"></script>
<script>
function setradio(myitem,v) {
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
</script>
<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
</head>
<body>
<%
if (isShowNav==1) {
%>
<%@ include file="module_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<%}%>
<div class="spacerH"></div>
<div id="visualDiv">
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="left"><table width="100%">
      <form name="visualForm" id="visualForm">
        <tr>
          <td>
		  <%
			com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
			out.print(rd.report(msd));
		  %>
		  </td>
        </tr>
      </form>
    </table></td>
  </tr>
  <tr>
    <td align="left"></td>
  </tr>
  <tr>
    <td align="left">
		<%
        com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
        Iterator ir = fdao.getAttachments().iterator();
          while (ir.hasNext()) {
            com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) ir.next();
            if (am.getFieldName()!=null && !"".equals(am.getFieldName())) {
                // if (!am.getFieldName().startsWith("att")) {
                //	  continue;
                // }
            }
            %>
        <table width="85%"  border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td width="5%" height="31" align="right"><img src="<%=request.getContextPath()%>/images/attach.gif" /></td>
            <td>&nbsp; <a target="_blank" href="<%=request.getContextPath()%>/visual_getfile.jsp?attachId=<%=am.getId()%>"><%=am.getName()%></a>&nbsp;&nbsp;<a href="?op=delAttach&id=<%=id%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&attachId=<%=am.getId()%>"></a><br />
            </td>
          </tr>
        </table>
      <%}%></td>
  </tr>
  <tr>
    <td height="30" align="center"><input name="id" value="<%=id%>" type="hidden" />
    <%if (msd.getInt("btn_print_display")==1) {%>
      	<input class="btn" type="button" onclick="showFormReport()" value="打印"/>
    <%}%>
      <!--
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input class="btn" type="button" onclick="exportToWord()" value="导出至Word"/>
      -->
	<%
    if (msd.getInt("btn_edit_display")==1 && (mpd.canUserModify(privilege.getUser(request)) || mpd.canUserManage(privilege.getUser(request)))) {
    %>
      	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    	<input class="btn" type="button" onclick="window.location.href='module_edit.jsp?parentId=<%=id%>&id=<%=id%>&isShowNav=<%=isShowNav%>&code=<%=msd.getString("code")%>'" value="编辑" />
    <%}%>
    </td>
  </tr>
</table>
</div>
<form name="formWord" target="_blank" action="module_show_word.jsp" method="post">
<textarea name="cont" style="display:none"></textarea>
</form>
</body>
<script>
    function getPrintContent() {
        var str = "<div style='text-align:center;margin-top:10px'>" + $('#visualDiv').html() + "</div>";
        return str;

    }

function showFormReport() {
	var preWin=window.open('../print_preview.jsp?print=true','','left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=1');
}

function exportToWord() {
	formWord.cont.value = formDiv.innerHTML;
	formWord.submit();
}
// i从1开始
function getCellValue(i, j) {
    var obj = document.getElementById("cwsNestTable");  
    var cel = obj.rows.item(i).cells;

    var fieldType = Main_Tab.rows[0].cells[j].getAttribute("type");
    var macroType = Main_Tab.rows[0].cells[j].getAttribute("macroType");

    // 标值控件
    if (macroType=="macro_raty") {
        if(cel[j].children[0] && cel[j].children[0].tagName=="SPAN") {
            var ch = cel[j].children[0].children;
            for (var k=0; k<ch.length; k++) {
                if (ch[k].tagName=="INPUT") {
                    return ch[k].value;
                }
            }
        }
    }
    // 在clear_color时，会置宏控件所在单元格的value属性为控件的值
    else if (cel[j].getAttribute("value")) {
        return cel[j].getAttribute("value");
    }
    else {
        if (cel[j].children.length>0) {
            var cellDiv = cel[j].children[0];
            return cellDiv.innerText.trim();
        }
        else {
            return cel[j].innerText.trim();
        }
    }
    return "";
}
</script>
</html>