<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*,
                 java.text.*,
                 com.cloudwebsoft.framework.base.*,
                 cn.js.fan.module.cms.site.*,
                 com.redmoon.oa.fileark.*,
                 cn.js.fan.util.*,
                 com.redmoon.oa.ui.*,
                 cn.js.fan.web.*,
                 cn.js.fan.module.pvg.*"
%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
    String siteCode = Leaf.ROOTCODE;
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv=Content-Type content="text/html; charset=utf-8"/>
    <title>图片轮播 - 编辑</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        .label {
            border: 1px solid #ccc;
            margin: 5px 5px;
            padding: 5px 5px;
            border-radius: 5px;
            display: block;
            float: left;
        }

        .close {
            margin-left: 5px;
            cursor: pointer;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        function openWin(url, width, height) {
            var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width=" + width + ",height=" + height);
        }

        var urlObj;

        function SelectImage(urlObject) {
            urlObj = urlObject;
            openWin("flash_image_sel.jsp", 800, 600);
        }

        function setImgUrl(visualPath, id, title) {
            o("url" + urlObj).value = visualPath;
            o("link" + urlObj).value = "<%=request.getContextPath()%>/doc_show.jsp?id=" + id;
            o("title" + urlObj).value = title;
        }
    </script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
    <TBODY>
    <TR>
        <TD class="tdStyle_1"><a href="flash_image_list.jsp?siteCode=<%=siteCode%>">图片轮播</a></TD>
    </TR>
    </TBODY>
</TABLE>
<br>
<form id="form1" name="form1" action="flash_image_list.jsp?op=add&siteCode=<%=siteCode%>" method=post>
    <table width="92%" align="center" class="tabStyle_1 percent80">
        <thead>
        <tr>
            <td height="22" colspan="4" class="tabStyle_1_title">添加</td>
        </tr>
        </thead>
        <tr>
            <td height="22">名称</td>
            <td height="22" colspan="3"><input id="name" name="name">
                <script>
                    var name = new LiveValidation('name');
                    name.add(Validate.Presence);
                </script>
            </td>
        </tr>
        <tr>
            <td height="22">自动获取</td>
            <td height="22" colspan="3">
                <input id="is_auto" name="is_auto" checked type="checkbox" value="1"/>
            </td>
        </tr>
        <tr class="tr-auto">
            <td height="22">目录</td>
            <td height="22" colspan="3">
                <select id="dir">
                    <option value="not" selected="selected">请选择目录</option>
                    <%
                        Directory dir = new Directory();
                        Leaf lf = dir.getLeaf("root");
                        DirectoryView dv = new DirectoryView(request, lf);
                        dv.ShowDirectoryAsOptions(out, lf, lf.getLayer());
                    %>
                </select>

                <div id="filearkSelected" style="margin: 10px 0px">

                </div>

                <script>
                    var mapSel = new Map();
                    var oldVal = $('#dir').val();
                    $('#dir').change(function () {
                        if (this.options[this.selectedIndex].value == 'not') {
                            jAlert(this.options[this.selectedIndex].text + ' 不能被选择！', '提示');
                            $(this).val(oldVal);
                            return false;
                        } else {
                            var v = $(this).val();
                            var t = this.options[this.selectedIndex].text;
                            t = t.replace('├『', '');
                            t = t.replace('』', '');
                            t = $.trim(t);
                            t = t.replace('╋ ', '');
                            if (!mapSel.containsKey(v)) {
                                oldVal = v;
                                mapSel.put(v, '');
                                // 如果还没被选择的，则置标题
                                if (mapSel.size() == 0) {
                                    $('#titleFileark').val(t);
                                }
                                $('#filearkSelected').append("<span class='label'><span><a href=\"javascript:;\" onclick=\"addTab('" + t + "', '<%=request.getContextPath()%>/fileark/document_list_m.jsp?dir_code=" + v + "')\">" + t + "</a></span><input type=\"hidden\" name=\"dir_code\" value=\"" + v + "\"/><a class=\"close\">x</a></span>");
                            } else {
                                jAlert(t + ' 已被选择', '提示');
                            }
                        }
                    });

                    $("#filearkSelected").on("click", ".close", function () {
                        $(this).parent().remove();
                    })
                </script>
            </td>
        </tr>
        <tr class="tr-manual">
            <td height="22">图片设置</td>
            <td height="22">地址</td>
            <td height="22">链接</td>
            <td height="22">文字</td>
        </tr>
        <tr class="tr-manual">
            <td height="22">图片1
                <input name="site_code" value="<%=siteCode%>" type=hidden></td>
            <td><input name="url1">
                <input name="button" type="button" onclick="SelectImage(1)" value="选择"/></td>
            <td><input name="link1"></td>
            <td><input name="title1"></td>
        </tr>
        <tr class="tr-manual">
            <td height="22">图片2</td>
            <td><input name="url2">
                <input name="button2" type="button" onclick="SelectImage(2)" value="选择"/></td>
            <td><input name="link2"></td>
            <td><input name="title2"></td>
        </tr>
        <tr class="tr-manual">
            <td height="22">图片3</td>
            <td><input name="url3">
                <input name="button5" type="button" onclick="SelectImage(3)" value="选择"/></td>
            <td><input name="link3"></td>
            <td><input name="title3"></td>
        </tr>
        <tr class="tr-manual">
            <td height="22">图片4</td>
            <td><input name="url4">
                <input name="button3" type="button" onclick="SelectImage(4)" value="选择"/></td>
            <td><input name="link4"></td>
            <td><input name="title4"></td>
        </tr>
        <tr class="tr-manual">
            <td width="17%" height="22">图片5</td>
            <td width="32%"><input name="url5">
                <input name="button4" type="button" onclick="SelectImage(5)" value="选择"/></td>
            <td width="28%"><input name="link5"></td>
            <td width="23%"><input name="title5"></td>
        </tr>
        <tr>
            <td height="22" colspan="4" align="center">
                <input type="submit" class="btn" value="确 定">
                &nbsp;&nbsp;&nbsp;&nbsp;
                <input type="button" value="返 回" class="btn" onClick="window.location.href='flash_image_list.jsp?siteCode=<%=StrUtil.UrlEncode(siteCode)%>'"/>
            </td>
        </tr>
    </table>
</form>
</body>
<script>
    function checkIsAuto() {
        if ($('#is_auto').prop('checked')) {
            $('.tr-manual').hide();
            $('.tr-auto').show();
        }
        else {
            $('.tr-manual').show();
            $('.tr-auto').hide();
        }
    }
    $(function() {
        $('#is_auto').click(function() {
            checkIsAuto();
        });

        checkIsAuto();
    });
</script>
</html>
