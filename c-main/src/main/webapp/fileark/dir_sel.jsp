<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>选择目录</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        img {
            margin-right: 5px;
        }
    </style>
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script>
        function ShowChild(imgobj, name) {
            var tableobj = o("childof" + name);
            if (tableobj == null) {
                // document.frames.ifrmGetChildren.location.href = "dir_ajax_getchildren.jsp?op=singleSel&parentCode=" + name;
                getChildren(name);

                if (imgobj.src.indexOf("i_puls-root-1.gif") != -1)
                    imgobj.src = "images/i_puls-root.gif";
                if (imgobj.src.indexOf("i_plus.gif") != -1) {
                    imgobj.src = "images/i_minus.gif";
                } else
                    imgobj.src = "images/i_plus.gif";
                return;
            }
            if (tableobj.style.display == "none") {
                tableobj.style.display = "";
                if (imgobj.src.indexOf("i_puls-root-1.gif") != -1)
                    imgobj.src = "images/i_puls-root.gif";
                if (imgobj.src.indexOf("i_plus.gif") != -1)
                    imgobj.src = "images/i_minus.gif";
                else
                    imgobj.src = "images/i_plus.gif";
            } else {
                tableobj.style.display = "none";
                if (imgobj.src.indexOf("i_plus.gif") != -1)
                    imgobj.src = "images/i_minus.gif";
                else
                    imgobj.src = "images/i_plus.gif";
            }
        }

        function selectNode(code, name) {
            jConfirm("您确定要选择 " + name + " 么？", "提示", function (r) {
                if (!r) {
                    return;
                } else {
                    window.opener.selectNode(code, name);
                    window.close();
                }
            })
        }
    </script>
</head>
<body>
<table width="90%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe">
    <tr>
        <td height="24" colspan="2" align="center" background="images/top-right.gif" class="right-title"><strong>请选择目录</strong></td>
    </tr>
    <form id="form1" name="form1" method="post">
        <tr>
            <td width="24" height="87">&nbsp;</td>
            <td width="249">
                <%
                    String dirCode = ParamUtil.get(request, "dirCode");
                    if (dirCode.equals(""))
                        dirCode = Leaf.ROOTCODE;

                    Directory dir = new Directory();
                    Leaf leaf = dir.getLeaf(dirCode);
                    DirView tv = new DirView(request, leaf);
                    tv.SelectSingleAjax(out, "selectNode", "", "", true);
                %></td>
        </tr>
    </form>
</table>
</body>
<script>
    function getChildren(parentCode) {
        $.ajax({
            type: "post",
            url: "getChildrenHtml.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                op: "singleSel",
                parentCode: parentCode
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $("body").showLoading();
            },
            success: function (data, status) {
                var obj = document.getElementById(parentCode);
                $(obj).after(data);
            },
            complete: function (XMLHttpRequest, status) {
                $("body").hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

</script>
</HTML>
