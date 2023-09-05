<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.db.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="com.redmoon.oa.sys.SysUtil" %>
<%@ page import="com.baomidou.mybatisplus.core.toolkit.StringUtils" %>
<%
    String op = request.getParameter("op");
    String tableName = request.getParameter("formName");
    String nowId = request.getParameter("nowId");

    int len = 95;

    String[][] tables = SysUtil.getSequenceOATables();

    if ("start".equals(op)) {
        int cmsId = 0; // 记录常量的值也就是redmoonid表idType的值
        int id = 0;    // 记录redmoonid表ID初始化后的值
        String sql = "";

        if (!"".equals(nowId)) { // 修
            int nowId2 = Integer.parseInt(nowId);
            JdbcTemplate jt = new JdbcTemplate();
            for (int i = 0; i < len; i++) {
                if (tables[i][0].equals(tableName)) {
                    try {
                        sql = "update redmoonid set id=" + nowId2 + " where idType=" + i;
                        int r = jt.executeUpdate(sql);
                        out.print(StrUtil.Alert_Redirect("操作成功", "init_redmoon_id.jsp"));
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        } else if ("all".equals(tableName)) {
            SysUtil.initSequenceOATables();
            out.print(StrUtil.Alert_Redirect("操作成功", "init_redmoon_id.jsp"));
            return;
        } else {
            // 初始化
            int s = 0;
            String tableId = "id";
            for (int i = 0; i < len; i++) {
                if (tables[i][0]!=null && tables[i][0].equals(tableName)) {
                    cmsId = i;
                    s++;
                    if (tables[i][2] != null) {
                        tableId = tables[i][2];
                    }
                }
            }
            if (s == 0) {
                out.print(StrUtil.Alert_Back("表不存在"));
                return;
            }
            JdbcTemplate jt = new JdbcTemplate();
            try {
                sql = "select max(" + tableId + ") from " + tableName;
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
                sql = "update redmoonid set id=" + id + " where idType=" + cmsId;
                int r = jt.executeUpdate(sql);
                if (r == 0) {
                    sql = "insert into redmoonid (id, idType) values (" + id + "," + cmsId + ")";
                    jt.executeUpdate(sql);
                }
                out.println(StrUtil.Alert_Redirect("操作成功", "init_redmoon_id.jsp"));
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
    SequenceManager.init();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>redmoonid初始化</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1">自增长ID初始化</td>
    </tr>
    </tbody>
</table>

<form name="form1" id="form1" method="get" action="?op=start" style="margin-top: 10px">
    <input type="hidden" name="op" value="start"/>
    <input type="hidden" name="formName" value=""/>
    <input type="hidden" name="nowId" value=""/>
    <table class="tabStyle_1 percent80">
        <tr>
            <td class=tabStyle_1_title width="20%">表名称</td>
            <td class=tabStyle_1_title width="20%">idType</td>
            <td class=tabStyle_1_title width="20%">当前表中最大id</td>
            <td class=tabStyle_1_title width="20%">自增长id</td>
            <td class=tabStyle_1_title width="20%"><input type="button" class="btn" onclick="allTable()" value="初始化全部表"/></td>
        </tr>
        <%
            for (int k = 0; k < len; k++) {
                int tId = 0;
                String tName = "";
                int mid = 0;
                int nid = 0;
                String tableId2 = "id";
                if (!StringUtils.isEmpty(tables[k][0])) {
                    tName = tables[k][0];
                    tId = k;
                    if (tables[k][2] != null) {
                        tableId2 = tables[k][2];
                    }
                    String sql2 = "select max(" + tableId2 + ") from " + tName;
                    // System.out.println(getClass() + " " + sql2);
                    JdbcTemplate jtl = new JdbcTemplate();
                    try {
                        ResultIterator rir = jtl.executeQuery(sql2);
                        if (rir.hasNext()) {
                            ResultRecord rsd = (ResultRecord) rir.next();
                            mid = rsd.getInt(1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    try {
                        sql2 = "select id from redmoonid where idType=" + k;
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
            <td><%=tName%><input type="hidden" id="tname<%=k%>" value="<%=tName%>"/></td>
            <td><%=k%>
                <%
                    boolean isValid = true;
                    if (nid < mid) {
                        isValid = false;
                    }
                %>
            </td>
            <td <%=!isValid ? "style='color:red'" : ""%>>
                <%=mid%>
            </td>
            <td><input type="text" id=nid<%=k%> value="<%=nid%>"/></td>
            <td align="center"><input type="button" onclick="csh(<%=k%>)" class="btn" value="初始化"/>
                &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" onclick="xg(<%=k%>)" class="btn" value="修改"/></td>
        </tr>
        <%
            }
        %>
    </table>
</form>
</body>
<script>
    function allTable() {
        if (confirm("您确定要初始化全部表么？")) {
            o("formName").value = "all";
            o("form1").submit();
        }
    }

    function csh(k) {
        o("formName").value = o("tname" + k).value;
        o("form1").submit();
    }

    function xg(k) {
        o("formName").value = o("tname" + k).value;
        o("nowId").value = o("nid" + k).value;
        o("form1").submit();
    }
</script>
</html>
