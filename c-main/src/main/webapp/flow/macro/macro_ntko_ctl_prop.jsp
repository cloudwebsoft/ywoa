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
    <title>Office在线编辑宏控件属性</title>
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
                $('#width').val("100%");
                $('#height').val("600px");
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
<%
    String formCode = ParamUtil.get(request, "formCode");
    if (formCode.equals("")) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "请先创建表单，然后编辑表单时插入此控件！"));
        return;
    }

    DocTemplateDb ld = new DocTemplateDb();
    String sql = "select id from " + ld.getTableName() + " order by unit_code asc, sort asc";
    Iterator ir = ld.list(sql).iterator();

    String opts = "<option value='-1'>无</option>";
    while (ir.hasNext()) {
        ld = (DocTemplateDb)ir.next();
        opts += "<option value='" + ld.getId() + "'>" + ld.getTitle() + "</option>";
    }
%>
<table width="100%" height="324" cellPadding="0" cellSpacing="0">
    <tbody>
    <tr>
        <td height="28" colspan="2" class="tabStyle_1_title">&nbsp;映射字段</td>
    </tr>
    <tr>
        <td width="15%" height="42" align="center">宽度</td>
        <td width="85%" align="left">
            <input id="width" name="width"/>
        </td>
    </tr>
    <tr>
        <td height="42" align="center">高度</td>
        <td align="left">
            <input id="height" name="height"/>
        </td>
    </tr>
    <tr>
      <td height="42" align="center">公文模板</td>
      <td align="left">
          <select id="templateId" name="templateId">
              <%=opts%>
          </select>（仅用于智能模块）
      </td>
    </tr>
    <tr>
      <td height="42" colspan="2" align="center">
          <input type="button" class="btn" value="确定" onclick="ok()"/>
          &nbsp;&nbsp;
          <input type="button" class="btn" value="取消" onclick="window.close()"/>
      </td>
      </tr>
    <tr>
      <td height="42" colspan="2" align="center">注：无需映射，则选择空</td>
    </tr>
    </tbody>
</table>
</body>
<script language="javascript">
    function ok() {
        var json = {};
        json.width = $('#width').val();
        json.height = $('#height').val();
        json.templateId = $('#templateId').val();
        window.opener.setSequence(JSON.stringify(json), "");
        window.close();
    }
</script>
</html>