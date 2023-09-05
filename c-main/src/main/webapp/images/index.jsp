<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.io.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>表情列表</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <script language=javascript>
        <!--
        function changeface(face) {
            window.opener.document.getElementById("RealPic").value = face;
            window.opener.showimage();
            window.close();
        }

        //-->
        function addBorderClass(obj) {
            obj.setAttribute("class", "borderClass");
        }

        function removeBorderClass(obj) {
            if (obj.className == "borderClass") {
                obj.removeAttribute("class");
            }
        }
    </script>
    <style>
        .borderClass1 {
            border: 1px solid #f3f3f3;
        }

        .borderClass2 {
            border: 1px solid #2a9ceb;
        }
    </style>
</head>
<div align="center">
    <center>
        <br></center>
    <%
        String path = Global.getRootPath() + "/forum/images/face/";
        com.cloudweb.oa.utils.FileViewer fileViewer = new com.cloudweb.oa.utils.FileViewer(cn.js.fan.web.Global.realPath + "/forum/images/face/");
        fileViewer.init();
    %>
    <table style="border: solid 2px #bfcee3" width=80% cellpadding="0" cellspacing="0">
        <center>
            <tr style="background-color:#86C3F0;height:35px;color:white;font-weight:bold; ">
                <td align=center>表情列表</td>
            <tr>
                <td valign="center">
                    <table border="0" width="100%" cellspacing="0" cellpadding="0">
                        <tbody>
                        <%
                            int k = 0;
                            while (fileViewer.nextFile()) {
                                if (fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1 && fileViewer.getFileName().indexOf("face") != -1) {
                                    if (k == 0) {
                                        out.print("<tr align=center>");
                                    }
                                    String fileName = fileViewer.getFileName();
                        %>
                        <td class="borderClass1" onmouseout="this.className='borderClass1'" onmouseover="this.className='borderClass2'">
                            &nbsp;<img style='cursor:hand' onClick="changeface('<%=fileName%>')" src="../forum/images/face/<%=fileViewer.getFileName()%>" width="32" height="32" border="0"/></td>
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
    <p align=center>点击选定</p>
</div>
</body>
</html>