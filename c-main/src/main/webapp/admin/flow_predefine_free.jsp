<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="org.json.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>自由流程设置</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        function setRoleField(fieldWrite, fieldValue, fieldText) {
            var f = o(fieldWrite);
            var ft = o(fieldWrite + "_text");
            f.value = fieldValue;
            ft.value = fieldText;
        }

        function openWin(url, width, height) {
            var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width=" + width + ",height=" + height);
        }

        function getRoleFieldValue(fieldWrite) {
            var f = o(fieldWrite);
            return f.value;
        }

        function getRoleFieldText(fieldWrite) {
            var ft = o(fieldWrite + "_text");
            return ft.value;
        }
    </script>
</head>
<body>
<jsp:useBean id="roleMgr" scope="page" class="com.redmoon.oa.pvg.RoleMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin.flow")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
%>
<%
    String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
    WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
    WorkflowPredefineDb wfp = new WorkflowPredefineDb();
    wfp = wfp.getPredefineFlowOfFree(flowTypeCode);

    String op = StrUtil.getNullString(request.getParameter("op"));
    boolean re = false;
    if (op.equals("edit")) {
        try {
%>
<script>
    $(".treeBackground").addClass("SD_overlayBG2");
    $(".treeBackground").css({"display": "block"});
    $(".loading").css({"display": "block"});
</script>
<%
    re = wpm.modifyFree(request);
%>
<script>
    $(".loading").css({"display": "none"});
    $(".treeBackground").css({"display": "none"});
    $(".treeBackground").removeClass("SD_overlayBG2");
</script>
<%
            if (re) {
                out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "flow_predefine_free.jsp?flowTypeCode=" + flowTypeCode));
            }
        } catch (ErrMsgException e) {
            out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
        }
        return;
    }
%>
<%@ include file="flow_inc_menu_top.jsp" %>
<script>
    o("menu2").className = "current";
</script>
<div class="spacerH"></div>
<%
    com.redmoon.oa.flow.Leaf flf = new com.redmoon.oa.flow.Leaf();
    flf = flf.getLeaf(wfp.getTypeCode());
    FormDb fd = new FormDb();
    fd = fd.getFormDb(flf.getFormCode());
    if (!fd.isLoaded()) {
        out.print("表单: " + flf.getFormCode() + " 不存在");
        return;
    }

    String code;
    String desc;
    RoleDb roleDb = new RoleDb();
    Vector result = roleDb.list();
    Iterator ir = result.iterator();
%>
<form name="form1" action="flow_predefine_free.jsp?op=edit&flowTypeCode=<%=flowTypeCode%>" method=post>
    <table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="98%" align="center">
        <tbody>
        <tr>
            <td colspan="4" noWrap class="tabStyle_1_title"><%=wfp.getTitle()%>
            </td>
        </tr>
        <tr>
            <td colspan="4" noWrap><input name="title" value="<%=wfp.getTitle()%>" type="hidden">
                <input name="id" type="hidden" value="<%=wfp.getId()%>">
                <input type=hidden name="flowString" value="">
                <input type=hidden name="typeCode" value="<%=flowTypeCode%>">
                <select name="dirCode" style="display:none" onChange="if(this.options[this.selectedIndex].value=='not'){jAlert(this.options[this.selectedIndex].text+' 不能被选择！','提示'); return false;}">
                    <option value="" selected>无</option>
                    <%
                        com.redmoon.oa.fileark.Directory dir = new com.redmoon.oa.fileark.Directory();
                        com.redmoon.oa.fileark.Leaf lf = dir.getLeaf("root");
                        com.redmoon.oa.fileark.DirectoryView dv = new com.redmoon.oa.fileark.DirectoryView(request, lf);
                        dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
                    %>
                </select>
                <select name="examine" style="display:none">
                    <option value="<%=com.redmoon.oa.fileark.Document.EXAMINE_NOT%>">未审核</option>
                    <option value="<%=com.redmoon.oa.fileark.Document.EXAMINE_PASS%>">已通过</option>
                </select>
                <script>
                    o("dirCode").value = "<%=wfp.getDirCode()%>";
                    o("examine").value = "<%=wfp.getExamine()%>";
                </script>
                <span style="display: none">
                <input type="checkbox" id="isLight" name="isLight" value="1" <%=wfp.isLight() ? "checked" : ""%> />
                简易流程(支持@用户)
                </span>
                <span id="spanIsRecall">
                <input type="checkbox" id="isRecall" name="isRecall" value="1" <%=wfp.isRecall()?"checked":""%>>
                能否撤回
                </span>
                <span id="spanIsReactive">
                <input type="checkbox" id="isReactive" name="isReactive" title="能否在流程已转交下一步后激活节点" value="1" <%=wfp.isReactive()?"checked":""%>>
                能否变更&nbsp;&nbsp;
                <input type="checkbox" title="流程中能否回复" id="isReply" name="isReply" value="1" <%=wfp != null && wfp.isReply() ? "checked" : ""%> />
                能否回复&nbsp;&nbsp;
                </span>
                <script>
                    function displayOnLightChange() {
                        if ($('#isLight').attr("checked")) {
                            $('#spanIsReactive').hide();
                            $('#spanIsRecall').hide();
                            // $("[id^=rolesTr]").hide();
                            // 让每个人都能发起简易流程
                            $("input[name='member_start']").attr("checked", "checked");
                            $("#roleCodes_member").attr("checked", "checked");
                        } else {
                            $('#spanIsReactive').show();
                            $('#spanIsRecall').show();
                            // $('[id^=rolesTr]').show();
                        }
                    }

                    $('#isLight').click(function () {
                        displayOnLightChange();
                    });

                    <%if (wfp.isLight()) {%>
                    $(function () {
                        displayOnLightChange();
                    });
                    <%}%>
                </script>
            </td>
        </tr>
        <tr id="rolesTrOp">
            <td noWrap width="4%">&nbsp;</td>
            <td colspan="2" noWrap>角色</td>
            <td width="86%" noWrap>操作</td>
        </tr>
        <%
            String[][] rolePrivs = wfp.getRolePrivsOfFree();
            int privLen = rolePrivs.length;

            while (ir.hasNext()) {
                RoleDb rd = (RoleDb) ir.next();
                code = rd.getCode();
                desc = rd.getDesc();
                String chk0 = "", chk1 = "", chk2 = "", chk3 = "", chk4 = "", chk5 = "", chk6 = "", chk7 = "";
                String fields = "", fieldNames = "";
                for (int i = 0; i < privLen; i++) {
                    if (code.equals(rolePrivs[i][0])) {
                        chk0 = "checked";
                        chk1 = StrUtil.getNullStr(rolePrivs[i][1]).equals("1") ? "checked" : "";
                        chk2 = StrUtil.getNullStr(rolePrivs[i][2]).equals("1") ? "checked" : "";
                        chk3 = StrUtil.getNullStr(rolePrivs[i][3]).equals("1") ? "checked" : "";
                        chk4 = StrUtil.getNullStr(rolePrivs[i][4]).equals("1") ? "checked" : "";
                        chk5 = StrUtil.getNullStr(rolePrivs[i][5]).equals("1") ? "checked" : "";
                        chk6 = StrUtil.getNullStr(rolePrivs[i][6]).equals("1") ? "checked" : "";
                        fields = StrUtil.getNullStr(rolePrivs[i][7]).replaceAll("\\|", ",");

                        if (rolePrivs[i].length >= 9) {
                            chk7 = StrUtil.getNullStr(rolePrivs[i][8]).equals("1") ? "checked" : "";
                        }

                        String[] fieldAry = fields.split(",");
                        // 找出嵌套表
                        MacroCtlMgr mm = new MacroCtlMgr();
                        FormDb nestfd = new FormDb();
                        Vector vfd = new Vector();
                        Vector v = fd.getFields();
                        Iterator irFd = v.iterator();
                        while (irFd.hasNext()) {
                            FormField ff = (FormField) irFd.next();
                            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                                if (mu != null && mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                                    String nestFormCode = ff.getDefaultValue();
                                    try {
                                        String defaultVal;
                                        if (mu.getNestType() == MacroCtlUnit.NEST_DETAIL_LIST) {
                                            defaultVal = StrUtil.decodeJSON(ff.getDescription());
                                        } else {
                                            defaultVal = StrUtil.decodeJSON(ff.getDefaultValueRaw()); // ff.getDefaultValueRaw()
                                        }
                                        JSONObject json = new JSONObject(defaultVal);
                                        nestFormCode = json.getString("destForm");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    FormDb nestFormDb = nestfd.getFormDb(nestFormCode);
                                    vfd.addElement(nestFormDb);
                                    // break;
                                }
                            }
                        }

                        int len = fieldAry.length;
                        for (int k = 0; k < len; k++) {
                            if (StrUtil.isEmpty(fieldNames)) {
                                if (fieldAry[k].startsWith("nest.")) {
                                    Iterator irvfd = vfd.iterator();
                                    while (irvfd.hasNext()) {
                                        nestfd = (FormDb) irvfd.next();
                                        String nestFieldName = nestfd.getFieldTitle(fieldAry[k].substring("nest.".length()));
                                        if (!nestFieldName.equals("")) {
                                            fieldNames = nestFieldName + "(嵌套表)";
                                            break;
                                        }
                                    }
                                } else {
                                    fieldNames = fd.getFieldTitle(fieldAry[k]);
                                }
                            } else {
                                if (fieldAry[k].startsWith("nest.")) {
                                    Iterator irvfd = vfd.iterator();
                                    while (irvfd.hasNext()) {
                                        nestfd = (FormDb) irvfd.next();
                                        String nestFieldName = nestfd.getFieldTitle(fieldAry[k].substring("nest.".length()));
                                        if (!nestFieldName.equals("")) {
                                            fieldNames += "," + nestFieldName + "(嵌套表)";
                                            break;
                                        }
                                    }
                                } else {
                                    fieldNames += "," + fd.getFieldTitle(fieldAry[k]);
                                }
                            }
                        }

                        break;
                    }
                }
        %>
        <tr id="rolesTr<%=code%>" class="row" style="BACKGROUND-COLOR: #ffffff">
            <td align="center"><input type="checkbox" id="roleCodes_<%=code%>" <%=chk0%> name="roleCodes" value="<%=code%>"></td>
            <td colspan="2" align="left"><%=desc%>
            </td>
            <td>
                <input type="checkbox" id="<%=code%>_start" <%=chk1%> name="<%=code%>_start" value="1">&nbsp;发起&nbsp;
                <input type="checkbox" id="<%=code%>_stop" <%=chk2%> name="<%=code%>_stop" title="拒绝同时结束流程" value="1">&nbsp;结束&nbsp;&nbsp;
                <%if (!wfp.isLight()) {%>
                <input type="checkbox" id="<%=code%>_archive" <%=chk3%> name="<%=code%>_archive" value="1">
                存档&nbsp;&nbsp;
                <%} %>
                <input type="checkbox" id="<%=code%>_discard" <%=chk4%> name="<%=code%>_discard" value="1">
                放弃&nbsp;&nbsp;
                <input type="checkbox" id="<%=code%>_del" <%=chk5%> name="<%=code%>_del" value="1">
                删除&nbsp;
                <input type="checkbox" id="<%=code%>_editAttach" <%=chk7%> name="<%=code%>_editAttach" value="1">
                编辑附件&nbsp;
                <input type="checkbox" id="<%=code%>_delAttach" <%=chk6%> name="<%=code%>_delAttach" value="1">
                删除附件&nbsp;&nbsp;<a href="javascript:;" onClick="table_<%=code%>.style.display='';openWin('flow_predefine_free_role_field.jsp?roleCode=<%=code%>&flowTypeCode=<%=flowTypeCode%>&field=<%=code+"_fieldWrite"%>', 601, 240)">表单域</a>&nbsp;[<a href="javascript:;" onClick="table_<%=code%>.style.display=''">查看</a>]
                <div id="table_<%=code%>" style="display:none">
                    <textarea id="<%=code%>_fieldWrite_text" name="<%=code%>_fieldWrite_text" cols="80" rows="3"><%=fieldNames%></textarea>
                    <input id="<%=code%>_fieldWrite" name="<%=code%>_fieldWrite" type="hidden" value="<%=fields%>">
                </div>
            </td>
        </tr>
        <%}%>
        <tr class="row" style="BACKGROUND-COLOR: #ffffff">
            <td colspan="4" align="center">
                <input type="submit" value="确定" class="btn"></td>
        </tr>
        </tbody>
    </table>
</form>
<DIV style="WIDTH: 95%" align=right></DIV>
</body>
<script language="javascript">
    <!--
    //-->
</script>
</html>