<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
    String skincode = UserSet.getSkin(request);
    if (skincode == null || skincode.equals("")) skincode = UserSet.defaultSkin;
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
%>
<!DOCTYPE html>
<html>
<head><title>选择图标</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css"/>
    <script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <style>
        td {
            font-size: 10pt;
        }

        .icon {
            width: 80px;
            height: 80px;
            cursor: pointer;
            margin: 10px;
        }
    </style>
</head>
<div align="center">
    <center>
    </center>
    <script language=javascript>
        function changeface(face) {
            window.opener.selIcon(face);
            window.close();
        }
    </script>
    <body class="menu_sel_body">
        <%
        com.redmoon.forum.ui.FileViewer fileViewer = new com.redmoon.forum.ui.FileViewer(Global.getAppPath(request) + "/images/mobileAppIcons/");
        fileViewer.init();
        %>
    <table width="100%" class="tTable">
        <center>
            <thead>
            <tr>
                <td align=center class="tTd">请点击选择图标</td>
            </tr>
            </thead>
            <tr>
                <td valign="center">
                    <table width="100%" class="cTable">
                        <tbody>
                        <td>
                        <%
                            int k = 0;
                            while (fileViewer.nextFile()) {
                                if (fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1 && fileViewer.getFileName().indexOf("face") != -1) {
                                    if (k == 0)
                                        out.print("<tr align=center style='height:50px;'>");
                                    String fileName = fileViewer.getFileName();
                        %>
                        <img class="icon" onClick="changeface('<%=fileName%>')" alt='<lt:Label res="res.label.forum.user" key="check_selected"/>' src="<%=request.getContextPath()%>/images/mobileAppIcons/<%=fileViewer.getFileName()%>" border="0"/>
                        <%
                                    k++;
                                    if (k == 10)
                                        out.write("</tr>");
                                    if (k == 10) k = 0;
                                }
                            }%>
                        </td>
                        </tbody>
                    </table>
                </td>
            </tr>
        </center>
    </table>
</div>
</body>
</html>