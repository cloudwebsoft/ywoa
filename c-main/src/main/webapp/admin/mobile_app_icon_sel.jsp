<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.cloudweb.oa.utils.JarFileUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="java.util.ArrayList" %>
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
        JarFileUtil jarFileUtil = SpringUtil.getBean(JarFileUtil.class);
        List<String> list = new ArrayList<>();
        jarFileUtil.loadFiles("static/images/mobileAppIcons", "", list);
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
                            for (String fileName : list) {
                                if (fileName.lastIndexOf("gif") != -1 || fileName.lastIndexOf("jpg") != -1 || fileName.lastIndexOf("png") != -1 || fileName.lastIndexOf("bmp") != -1 && fileName.indexOf("face") != -1) {
                                    if (k == 0) {
                                        out.print("<tr align=center style='height:50px;'>");
                                    }
                                    String name = fileName.substring(fileName.lastIndexOf("/") + 1);
                        %>
                        <img class="icon" onClick="changeface('<%=name%>')" src="../showImgInJar.do?path=<%=fileName%>" border="0" style="width: 60px; height: 60px"/>
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