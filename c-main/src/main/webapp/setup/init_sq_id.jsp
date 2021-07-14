<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.db.*" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="com.redmoon.oa.sys.SysUtil" %>
<%
    String op = request.getParameter("op");
    String tableName = request.getParameter("formName");
    String nowId = request.getParameter("nowId");

    String[][] tables = SysUtil.getSequenceForumTables();

    if ("start".equals(op)) {
        int cmsId = 0; // 记录常量的值也就是sq_id表idType的值
        int id = 0;    // 记录sq_id表ID初始化后的值
        String sql = "";
        if (!"".equals(nowId)) {
            int nowId2 = Integer.parseInt(nowId);
            JdbcTemplate jt = new JdbcTemplate();
            for (int i = 0; i < 87; i++) {
                if (tables[i][0].equals(tableName)) {
                    try {
                        sql = "update sq_id set id=" + nowId2 + " where idType=" + i;
                        jt.executeUpdate(sql);
                        out.println(StrUtil.Alert_Redirect("操作成功", "init_sq_id.jsp"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        } else if ("all".equals(tableName)) {
            SysUtil.initSequenceForumTables();
        } else {
            int s = 0;
            for (int i = 0; i < 87; i++) {
                if (tables[i][0].equals(tableName)) {
                    cmsId = i;
                    s++;
                }
            }
            if (s == 0) {
                out.println(StrUtil.Alert_Back("表不存在"));
            }
            JdbcTemplate jt = new JdbcTemplate();
            try {
                sql = "select max(id) from " + tableName;
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rd = (ResultRecord) ri.next();
                    id = rd.getInt(1);
                }
                id++;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            try {
                sql = "update sq_id set id=" + id + " where idType=" + cmsId;
                jt.executeUpdate(sql);
                out.println(StrUtil.Alert_Redirect("操作成功", "init_sq_id.jsp"));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        SequenceManager.init();
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>sq_id初始化</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
</head>
<body style>
<table cellSpacing="0" cellPadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">sq_id初始化</td>
    </tr>
    </tbody>
</table>

<form name="form1" id="form1" method="get" action="?op=start">
    <input type="hidden" name="op" value="start"/>
    <input type="hidden" name="formName" value=""/>
    <input type="hidden" name="nowId" value=""/>
    <table class="tabStyle_1 percent80">
        <tr>
            <td class=tabStyle_1_title width="20%">表名称</td>
            <td class=tabStyle_1_title width="20%">idType</td>
            <td class=tabStyle_1_title width="20%">当前表中最大id</td>
            <td class=tabStyle_1_title width="20%">自增长id</td>
            <td class=tabStyle_1_title width="20%"><input type="submit" class="btn" name="submit" onclick="allTable()" value="初始化全部表"/></td>
        </tr>
        <%
            for (int k = 0; k < 87; k++) {
                int tId = 0;
                String tName = "";
                int mid = 0;
                int nid = 0;
                if (!tables[k][0].equals("")) {
                    tName = tables[k][0];
                    tId = k;
                    String sql2 = "select max(id) from " + tName;
                    JdbcTemplate jtl = new JdbcTemplate();
                    try {
                        ResultIterator rir = jtl.executeQuery(sql2);
                        if (rir.hasNext()) {
                            ResultRecord rsd = (ResultRecord) rir.next();
                            mid = rsd.getInt(1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    try {
                        sql2 = "select id from sq_id where idType=" + k;
                        ResultIterator rir = jtl.executeQuery(sql2);
                        if (rir.hasNext()) {
                            ResultRecord rsd = (ResultRecord) rir.next();
                            nid = rsd.getInt(1);
                            //System.out.println(" nid= "+nid);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    continue;
                }
        %>
        <tr>
            <td><%=tName%><input type="hidden" id=tname<%=k%> value=<%=tName%>/></td>
            <td><%=k%>
            </td>
            <td><%=mid%>
            </td>
            <td><input type="text" id=nid<%=k%> value="<%=nid%>"/></td>
            <td align="center"><input type="submit" name="submit" onclick="csh(<%=k%>)" class="btn" value="初始化"/>
                &nbsp;&nbsp;&nbsp;&nbsp;<input type="submit" name="submit" onclick="xg(<%=k%>)" class="btn" value="修改"/></td>
        </tr>
        <%
            }
        %>
    </table>
</form>
</body>
<script>
    function allTable() {
        if (confirm("您确定要初始化全部表么？"))
            form1.formName.value = "all";
    }

    function csh(k) {
        form1.formName.value = document.getElementById("tname" + k).value;
    }

    function xg(k) {
        form1.formName.value = document.getElementById("tname" + k).value;
        form1.nowId.value = document.getElementById("nid" + k).value;
    }
</script>
</html>
