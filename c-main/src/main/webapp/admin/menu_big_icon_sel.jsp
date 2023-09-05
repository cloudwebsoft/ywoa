<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.cloudweb.oa.utils.JarFileUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%
    String skincode = UserSet.getSkin(request);
    if (skincode == null || skincode.equals("")) {
        skincode = UserSet.defaultSkin;
    }
    SkinMgr skm = new SkinMgr();
    Skin skin = skm.getSkin(skincode);
    String skinPath = skin.getPath();
%>
<!DOCTYPE html>
<html>
<head>
    <title>选择图标</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css"/>
    <style>
    </style>
</head>
<body class="menu_sel_body">
<div align="center">
    <script language=javascript>

        function changeIcon(icon) {
            window.opener.selBigIcon(icon);
            window.close();
        }

    </script>
    <%
        JarFileUtil jarFileUtil = SpringUtil.getBean(JarFileUtil.class);
        List<String> list = new ArrayList<>();
        jarFileUtil.loadFiles("static/images/bigicons", "", list);
    %>
    <table border="0" width=80% class="tTable">
        <center>
            <thead>
            <tr>
                <td align=center class="tTd">请点击选择图标</td>
            <tr>
            </thead>
            <td valign="center">
                <table border="0" class="cBigTable">
                    <tbody>
                    <%
                        int k = 0;
                        for (String fileName : list) {
                            if (fileName.lastIndexOf("gif") != -1 || fileName.lastIndexOf("jpg") != -1 || fileName.lastIndexOf("png") != -1 || fileName.lastIndexOf("bmp") != -1 && fileName.contains("face")) {
                                if (k == 0) {
                                    out.print("<tr align=center class=''>");
                                }
                                String name = fileName.substring(fileName.lastIndexOf("/") + 1);
                    %>
                    <td class="imgBg">&nbsp;<img style="cursor:pointer;" onClick="changeIcon('<%=name%>')" src="../showImgInJar.do?path=<%=fileName%>" border="0"/></td>
                    <%
                                k++;
                                if (k == 10) {
                                    out.write("</tr>");
                                }
                                if (k == 10) {
                                    k = 0;
                                }
                            }
                        }%>
                    </tbody>
                </table>
        </center>
    </table>
</div>
</body>