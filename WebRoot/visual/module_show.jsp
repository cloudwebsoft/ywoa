<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.person.UserMgr" %>
<%@ page import="com.redmoon.oa.Config" %>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String code = ParamUtil.get(request, "code");
    if ("".equals(code)) {
        code = ParamUtil.get(request, "formCode");
    }
    ModuleSetupDb msd = new ModuleSetupDb();
    msd = msd.getModuleSetupDb(code);
    if (msd == null) {
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
    
    if (msd.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_TREE) {
        boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
        if (!isInFrame) {
            response.sendRedirect(request.getContextPath() + "/" + "visual/module_show_frame.jsp?id=" + id + "&code=" + code);
            return;
        }
    }
    
    Config cfg = Config.getInstance();
    // 创建浏览日志
    if (cfg.getBooleanProperty("isModuleLogRead") && !formCode.equals("module_log") && !formCode.equals("module_log_read") && !formCode.equals("formula")) {
        FormDb fdModule = new FormDb(formCode);
        FormDb fd = new FormDb("module_log_read");
        FormDAO fdao = new FormDAO(fd);
        fdao.setFieldValue("read_type", String.valueOf(FormDAOLog.READ_TYPE_MODULE));
        fdao.setFieldValue("log_date", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        fdao.setFieldValue("module_code", code);
        fdao.setFieldValue("form_code", formCode);
        fdao.setFieldValue("module_id", String.valueOf(id));
        fdao.setFieldValue("form_name", fdModule.getName());
        fdao.setFieldValue("user_name", privilege.getUser(request));
        fdao.setCreator(privilege.getUser(request)); // 参数为用户名（创建记录者）
        fdao.setUnitCode(privilege.getUserUnitCode(request)); // 置单位编码
        fdao.setFlowTypeCode(String.valueOf(System.currentTimeMillis())); // 置冗余字段“流程编码”，可用于取出刚插入的记录，也可以为空
        fdao.create();
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

    request.setAttribute("id", id);
    request.setAttribute("code", code);
    request.setAttribute("isShowNav", isShowNav);
    request.setAttribute("isHasAttachment", fd.isHasAttachment());
    request.setAttribute("skinPath", SkinMgr.getSkinPath(request));

    com.redmoon.oa.visual.Render rd = new com.redmoon.oa.visual.Render(request, id, fd);
    request.setAttribute("rend", rd.report(msd));

    com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
    Vector vAttach = fdao.getAttachments();

    request.setAttribute("vAttach", vAttach);
    request.setAttribute("canUserLog", mpd.canUserLog(privilege.getUser(request)));

    request.setAttribute("btn_print_display", msd.getInt("btn_print_display")==1);
    request.setAttribute("btn_edit_display", msd.getInt("btn_edit_display") == 1 && (mpd.canUserModify(privilege.getUser(request)) || mpd.canUserManage(privilege.getUser(request))));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>智能模块设计-显示内容</title>
    <link type="text/css" rel="stylesheet" href="${skinPath}/css.css"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <style type="text/css">
        #attDiv {
            margin-top: 10px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../js/jquery1.7.2.min.js"></script>
    <script src="../js/jquery.raty.min.js"></script>
    <script src="../inc/flow_dispose_js.jsp"></script>
    <script src="../inc/flow_js.jsp"></script>
    <script src="../flow/form_js/form_js_${formCode}.jsp?pageType=show&id=${id}"></script>
    <script src="../inc/ajax_getpage.jsp"></script>
    
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <script src="../js/select2/i18n/zh-CN.js"></script>
    <style type="text/css">
        @import url("../util/jscalendar/calendar-win2k-2.css");
    </style>
    <script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
    <script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
    <script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
</head>
<body>
<c:if test="${isShowNav==1}">
<%@ include file="module_inc_menu_top.jsp" %>
<script>
    o("menu1").className = "current";
</script>
</c:if>
<div class="spacerH"></div>
<div id="visualDiv">
    <table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td align="left">
                <table width="100%">
                    <form name="visualForm" id="visualForm">
                        <tr>
                            <td>
                                ${rend}
                            </td>
                        </tr>
                    </form>
                </table>
            </td>
        </tr>
        <tr>
            <td align="left"></td>
        </tr>
        <c:if test="${isHasAttachment}">
        <tr>
            <td align="left">
                <c:if test="${fn:length(vAttach)>0}">
                <div id="attDiv">
                    <table id="attTable" class="tabStyle_1 percent98" width="98%" border="0" align="center"
                           cellpadding="0" cellspacing="0">
                        <tr>
                            <td height="31" align="right" class="tabStyle_1_title">&nbsp;</td>
                            <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="fileName"/></td>
                            <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="creator"/></td>
                            <td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="time"/></td>
                            <td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="size"/></td>
                            <td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="operate"/></td>
                        </tr>
                        <c:forEach items="${vAttach}" var="att" >
                        <tr id="trAtt${att.id}%>">
                            <td width="2%" height="31" align="center"><img src="../images/attach.gif"/></td>
                            <td width="51%" align="left">
                                &nbsp;
                                <span id="spanAttLink${att.id}">
                                <a href="../visual_getfile.jsp?attachId=${att.id}" target="_blank">
                                    <span id="spanAttName${att.id}">${att.name}</span>
                                </a>
                                </span>
                            </td>
                            <td width="10%" align="center">${att.creatorRealName}
                            </td>
                            <td width="15%" align="center"><fmt:formatDate value='${att.createDate}' pattern='yyyy-MM-dd HH:mm' />
                            </td>
                            <td width="11%" align="center">${att.fileSizeMb}M
                            </td>
                            <td width="11%" align="center">
                                <a href="../visual_getfile.jsp?attachId=${att.id}" target="_blank">
                                    <lt:Label res="res.flow.Flow" key="download"/>
                                </a>
                                <c:if test="${canUserLog}">
                                    &nbsp;&nbsp;<a href="javascript:;" onClick="addTab('${att.name} 日志', '${pageContext.request.contextPath}/visual/att_log_list.jsp?attId=${att.id}')">日志</a>
                                </c:if>
                                <c:if test="${att.previewUrl!=''}">
                                    &nbsp;&nbsp;<a href="javascript:;" onClick="addTab('${att.name}', '${pageContext.request.contextPath}/${att.previewUrl}">预览</a>
                                </c:if>
                            </td>
                        </tr>
                        </c:forEach>
                    </table>
                </div>
                </c:if>
            </td>
        </tr>
        </c:if>
        <tr>
            <td height="30" align="center"><input name="id" value="${id}" type="hidden"/>
                <c:if test="${btn_print_display}">
                <input class="btn" type="button" onclick="showFormReport()" value="打印"/>
                </c:if>
                <!--
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input class="btn" type="button" onclick="exportToWord()" value="导出至Word"/>
                -->
                <c:if test="${btn_edit_display}">
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input class="btn" type="button"
                       onclick="window.location.href='module_edit.jsp?parentId=${id}&id=${id}&isShowNav=${isShowNav}&code=${code}'" value="编辑"/>
                </c:if>
            </td>
        </tr>
    </table>
</div>
<form id="formWord" name="formWord" target="_blank" action="module_show_word.jsp" method="post">
    <textarea name="cont" style="display:none"></textarea>
</form>
</body>
<script>
    function getPrintContent() {
        var str = "<div style='text-align:center;margin-top:10px'>" + $('#visualDiv').html() + "</div>";
        return str;
    }

    function showFormReport() {
        window.open('../print_preview.jsp?print=true', '', 'left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=1');
    }

    function exportToWord() {
        o("cont").value = o("formDiv").innerHTML;
        o("formWord").submit();
    }

    // i从1开始
    function getCellValue(i, j) {
        var obj = document.getElementById("cwsNestTable");
        var cel = obj.rows.item(i).cells;

        var fieldType = Main_Tab.rows[0].cells[j].getAttribute("type");
        var macroType = Main_Tab.rows[0].cells[j].getAttribute("macroType");

        // 标值控件
        if (macroType == "macro_raty") {
            if (cel[j].children[0] && cel[j].children[0].tagName == "SPAN") {
                var ch = cel[j].children[0].children;
                for (var k = 0; k < ch.length; k++) {
                    if (ch[k].tagName == "INPUT") {
                        return ch[k].value;
                    }
                }
            }
        }
        // 在clear_color时，会置宏控件所在单元格的value属性为控件的值
        else if (cel[j].getAttribute("value")) {
            return cel[j].getAttribute("value");
        } else {
            if (cel[j].children.length > 0) {
                var cellDiv = cel[j].children[0];
                return cellDiv.innerText.trim();
            } else {
                return cel[j].innerText.trim();
            }
        }
        return "";
    }
</script>
</html>