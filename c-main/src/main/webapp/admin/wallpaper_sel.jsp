<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="com.redmoon.oa.ui.menu.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String skincode = UserSet.getSkin(request);
    if (skincode == null || skincode.equals("")) skincode = UserSet.defaultSkin;
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
%>
<html>
<head><title>选择壁纸</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css"/>
    <style>
        td {
            font-size: 10pt;
            color: #fff;
        }
    </style>
</head>
<body class="menu_sel_body">
<div align="center">
    <center>
    </center>
    <script language=javascript>
        <!--
        function changeIcon(icon) {
            window.opener.selWallpaper(icon);
            window.close();
        }

        //-->
    </script>
    <%
        com.redmoon.forum.ui.FileViewer fileViewer = new com.redmoon.forum.ui.FileViewer(Global.getRealPath() + "images/wallpaper/");
        fileViewer.init();
    %>
    <table class="tTable" border="0" width="60%" cellpadding="1" cellspacing="1">
        <center>
            <tr>
                <td align=center class="tTd">请点击选择壁纸</td>
            <tr>
                <td valign="center">
                        <%
                    WallpaperDb wd = new WallpaperDb();
                    String imgPath = wd.getImgPath(privilege.getUser(request));
                    if (imgPath!=null && !"".equals(imgPath)) {
                    %>
                    <table border="0" width="100%" cellspacing="1" cellpadding="1">
                        <tbody>
                        <tr>
                            <td valign="middle" width="30%">我上传的壁纸：</td>
                            <td align="left" valign="middle"><img style='cursor:hand' onclick="changeIcon('#')" alt='<lt:Label res="res.label.forum.user" key="check_selected"/>' src="../upfile/wallpaper/<%=imgPath%>" width="60" height="60" border="0"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                        <%
                    }
                    %>
                    <table border="0" width="100%" cellspacing="1" cellpadding="1" style="margin-top:30px">
                        <tbody>
                        <tr>
                            <td>选择系统壁纸：</td>
                        </tr>
                        <%
                            int k = 0;
                            while (fileViewer.nextFile()) {
                                if (fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1 && fileViewer.getFileName().indexOf("face") != -1) {
                                    if (k == 0)
                                        out.print("<tr align=center>");
                                    String fileName = fileViewer.getFileName();
                        %>
                        <td>&nbsp;<img style='cursor:hand' onClick="changeIcon('<%=fileName%>')" alt='<lt:Label res="res.label.forum.user" key="check_selected"/>' src="../images/wallpaper/<%=fileViewer.getFileName()%>" width="60" height="60" border="0"/></td>
                        <%
                                    k++;
                                    if (k == 10)
                                        out.write("</tr>");
                                    if (k == 10) k = 0;
                                }
                            }%>
                        </tbody>
                    </table>
        </center>
    </table>
</div>
</body>