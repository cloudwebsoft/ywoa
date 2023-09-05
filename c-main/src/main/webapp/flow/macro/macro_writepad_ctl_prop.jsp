<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@page import="com.redmoon.oa.ui.SkinMgr" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>手写板宏控件属性</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../../js/jquery-1.9.1.min.js"></script>
    <script src="../../js/jquery-migrate-1.2.1.min.js"></script>
    <script>
        $(function() {
            var win = window.opener;
            var desc = win.document.getElementById('orgvalue').value;
            if (desc=="") {
                desc = win.document.getElementById('description').value;
            }
            if (desc=="") {
                $('#width').val("200");
                $('#height').val("100");
                return;
            }
            if (desc.indexOf('{')==0) {
                var json = $.parseJSON(desc);
                if (json.width) {
                    $('#width').val(json.width);
                    $('#height').val(json.height);
                }
                else {
                    $('#width').val("100%");
                    $('#height').val("600px");
                }
                $('#templateId').val(json.templateId);
            }
        })
    </script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table width="100%" cellPadding="0" cellSpacing="0">
    <tbody>
    <tr>
        <td height="28" colspan="2" class="tabStyle_1_title">&nbsp;属性</td>
    </tr>
    <tr>
        <td width="15%" height="42" align="center">宽度</td>
        <td width="85%" align="left">
            <input id="width" name="width"/>&nbsp;px
        </td>
    </tr>
    <tr style="display: none;">
        <td height="42" align="center">高度</td>
        <td align="left">
            <input id="height" name="height"/>&nbsp;px
        </td>
    </tr>
    <tr>
      <td height="42" colspan="2" align="center">
          <input type="button" class="btn" value="确定" onclick="ok()"/>
          &nbsp;&nbsp;
          <input type="button" class="btn" value="取消" onclick="window.close()"/>
      </td>
      </tr>
    </tbody>
</table>
</body>
<script language="javascript">
    function ok() {
        var json = {};
        json.width = $('#width').val();
        json.height = $('#height').val();
        window.opener.setSequence(JSON.stringify(json), "");
        window.close();
    }
</script>
</html>