<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.db.Paginator" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="cn.js.fan.security.*" %>
<%@ page import="com.redmoon.oa.util.*" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="org.apache.commons.collections.map.HashedMap" %>
<%@ page import="com.cloudweb.oa.service.DataDictService" %>
<%
    String op = ParamUtil.get(request, "op");
    boolean isShowField = ParamUtil.getBoolean(request, "isShowField", false);
    boolean isFilterForm = ParamUtil.getBoolean(request, "isFilterForm", false);
%>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title><%=Global.AppName%> - <%=Global.server%></title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <style>
        .diff {
            color: red;
        }
        .tr-new {
            background-color: yellow;
        }
        .td-title-empty {
            background-color: #0badc1;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script type="text/javascript" src="../js/jquery.editinplace.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body>
<div style="text-align:center;margin:20px">
    <strong>请选择数据源</strong>
    <select id="dbSource" name="dbSource">
        <option value="">请选择</option>
        <%
            cn.js.fan.web.Config cfg = new cn.js.fan.web.Config();
            Iterator ir = cfg.getDBInfos().iterator();
            while (ir.hasNext()) {
                DBInfo di = (DBInfo) ir.next();
        %>
        <option value="<%=di.name%>" <%=di.isDefault ? "selected" : ""%>><%=di.name%>
        </option>
        <%
            }
        %>
    </select>
    <input type="button" value="确定" onclick="window.location.href='db_dict.jsp?dbSource=' + o('dbSource').value;"/>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <input id="isShowField" name="isShowField" type="checkbox" <%=isShowField?"checked":""%> value="true"/>
    查看字段
    &nbsp;&nbsp;&nbsp;&nbsp;
    <input id="isFilterForm" name="isFilterForm" type="checkbox" <%=isFilterForm?"checked":""%> value="true"/>
    过滤表单
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a href="javascript:;" onclick="synAll()">全部同步</a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <a href="db_diff.jsp">比对数据库差异，本机为新数据库</a>
    <script>
        $(function() {
            $('#isShowField').click(function() {
                window.location.href = "db_dict.jsp?isFilterForm=" + $('#isFilterForm').prop("checked") + "&isShowField=" + $('#isShowField').prop("checked");
            })

            $('#isFilterForm').click(function() {
                window.location.href = "db_dict.jsp?isFilterForm=" + $('#isFilterForm').prop("checked") + "&isShowField=" + $('#isShowField').prop("checked");
            })
        })
    </script>
</div>
<%
    String dbSource = ParamUtil.get(request, "dbSource");
%>
<script>
    $('#dbSource').val("<%=dbSource%>");
</script>
<%
    if ("".equals(dbSource)) {
        dbSource = Global.getDefaultDB();
    }
    TransmitData td = new TransmitData();
    String connName = dbSource;
    Conn connTable = new Conn(connName);
    ResultSet rsTable = null;
    try {
        rsTable = td.getTableNames(connTable.getCon());
    } catch (Exception e) {
        out.print("数据库连接错误！");
        return;
    }
    finally {
        connTable.close();
    }

    JdbcTemplate jt = new JdbcTemplate();
    jt.setAutoClose(false);

    // 取出数据字典置于map中
    Map mapDict = new HashMap();
    String sql = "select id,name,title from ft_data_dict_table order by id asc";
    ResultIterator riTable = jt.executeQuery(sql);
    while (riTable.hasNext()) {
        ResultRecord rrTable = (ResultRecord)riTable.next();
        long tableId = rrTable.getLong(1);
        String tableName = rrTable.getString("name");
        String title = StrUtil.getNullStr(rrTable.getString("title"));

        sql = "select id,name,title,def,len,nullable,data_type,is_autoincrement,remarks from ft_data_dict_column where cws_id=?";
        ResultIterator ri = jt.executeQuery(sql, new Object[]{tableId});
        mapDict.put(tableName, new Object[]{tableId, title, ri});
    }

    Vector<String> vNav = new Vector<String>(); // 导航
    int n = 0;
    Conn conn = new Conn(dbSource);
    String tableName = "";
    while (rsTable.next()) {
        tableName = rsTable.getObject(3).toString();

        if (isFilterForm) {
            if (tableName.startsWith("ft_")) {
                continue;
            }
        }

        int isDiff = 0; // 表格是否有差异

        n++;
        String tableTitle = "";
        boolean isNew = true;
        long tableId = -1;

        Object[] aryTable = (Object[])mapDict.get(tableName);
        if (aryTable!=null) {
            isNew = false;
            tableId = ((Long)aryTable[0]).longValue();
            String tit = (String)aryTable[1];
            if ("".equals(tit)) {
                tableTitle = "[名称未填]";
                isDiff = 1;
            }
            else {
                tableTitle = tit;
            }
        }
        else {
            isDiff = 2;
            tableTitle = "新增表格";
        }
%>
<div id="table<%=n%>" style="clear:both; margin-left:30px">
    <h3>
        <%--<%=n%>、--%><a name="#<%=tableName%>"><%=tableName%></a>
        <span id="<%=tableName%>_title" class="<%=isNew?"diff":""%>">
            <%=tableTitle%>
        </span>
        <span>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <a href="javascript:;" onclick="syn('<%=tableName%>')">同步</a>
        </span>
    </h3>
</div>
<script>
    var domEditor, enteredText, originalHtml;
    // 该插件会上传值：original_value、update_value
    $('#<%=tableName%>_title').editInPlace({
        url: "../datadict/editTableTitle.do",
        params: "tableName=<%=tableName%>",
        saving_text: "保存中...",
        saving_image: "../images/loading.gif",
        delegate: {
            shouldOpenEditInPlace: function(dom) {
                originalHtml = dom.html();
            },
            willCloseEditInPlace: function(dom) {
                domEditor = dom;
                enteredText = dom.find(':input').val();
            }
        },
        error: function (obj) {
            alert(JSON.stringify(obj));
        },
        success: function (data) {
            data = $.parseJSON(data);
            $.toaster({
                "priority": "info",
                "message": data.msg
            });
            if (data.ret==1) {
                domEditor.html(enteredText);
            }
            else {
                domEditor.html(originalHtml);
            }
        }
    });
</script>
<%
    if (isShowField) {
%>
<table id="tabCol_<%=tableName%>" class="tabStyle_1" align="left" style="width:1000px; margin-left:30px" border="0">
    <thead>
    <tr>
        <th width="15%">字段</th>
        <th width="25%">名称</th>
        <th width="10%">类型</th>
        <th width="10%">长度</th>
        <th width="10%">默认值</th>
        <th width="10%">允许空</th>
        <td width="10%">自增长</td>
        <td width="10%">注释</td>
    </tr>
    </thead>
    <tbody>
    <%
        // 如果数据字典中已存在该表，则取出其所有的字段信息置于mapCol中
        Map mapCol = new HashedMap();
        if (tableId != -1) {
            ResultIterator ri = (ResultIterator)aryTable[2];
            while (ri.hasNext()) {
                ResultRecord rr = ri.next();
                mapCol.put(rr.getString("name").toLowerCase(), rr);
            }
        }
        Connection con_new = conn.getCon();
        DatabaseMetaData dmd = con_new.getMetaData();
        // mysql-connector-java 6.0以下用这个方法，高版本用此方法会取出所有的库中的所有的表
        ResultSet rsColumn = dmd.getColumns(con_new.getCatalog(), con_new.getSchema(), tableName, null);
        while (rsColumn.next()) {
            String columnName = rsColumn.getObject(4).toString().toLowerCase();

            String def = StrUtil.getNullStr(rsColumn.getString("COLUMN_DEF")); // 默认值
            int dataType = rsColumn.getInt("DATA_TYPE");
            String type = DataDictService.getDataType(dataType);
            int columnSize = rsColumn.getInt("COLUMN_SIZE");

            boolean isColNew = true; // 列是否在字典中有记录
            String clsSize = "";
            String tipSize = "";
            int dbColumnSize = 0;
            String dbTitle = "";
            if (tableId != -1) {
                String dbType = "varchar";
                ResultRecord rr = (ResultRecord)mapCol.get(columnName);
                if (rr!=null) {
                    isColNew = false;
                    dbType = rr.getString("data_type");
                    dbTitle = StrUtil.getNullStr(rr.getString("title"));
                    dbColumnSize = rr.getInt("len");
                    if (dbColumnSize!=columnSize) {
                        isDiff = 4;
                        tipSize = "title='原长度为" + dbColumnSize + "'";
                        clsSize = "class='diff'";
                    }
                    if ("".equals(dbTitle)) {
                        isDiff = 5;
                    }
                }
            }
            String clsTr = "";
            if (!isNew && isColNew) {
                clsTr = "class='tr-new'";
                isDiff = 6;
            }
    %>
    <tr <%=clsTr%>>
        <td><%=columnName.toLowerCase()%></td>
        <td id="<%=tableName%>_<%=columnName%>_title" class="<%="".equals(dbTitle)? "td-title-empty": ""%>"><%=dbTitle%></td>
        <td><%=type%></td>
        <td <%=tipSize%> <%=clsSize%>>
            <%=columnSize%>
        </td>
        <script>
            // 该插件会上传值：original_value、update_value
            $('#<%=tableName%>_<%=columnName%>_title').editInPlace({
                url: "../datadict/editColumnTitle.do",
                params: "tableName=<%=tableName%>&columnName=<%=columnName%>",
                default_text: "",
                error: function (obj) {
                    alert(JSON.stringify(obj));
                },
                success: function (data) {
                    data = $.parseJSON(data);
                    $.toaster({
                        "priority": "info",
                        "message": data.msg
                    });
                    if (data.ret==1) {
                        $('#<%=tableName%>_<%=columnName%>_title').removeClass('diff');
                    }
                }
            });
        </script>
        <td><%=def%></td>
        <td><%=rsColumn.getInt("NULLABLE")==1? "是":"否"%></td>
        <td><%=rsColumn.getString("IS_AUTOINCREMENT").equals("YES")?"是":"否"%></td>
        <td><%=rsColumn.getString("REMARKS")%></td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<%
        }

        vNav.addElement(tableName + "," + isDiff);
    }

    conn.close();
    jt.close();
%>
</body>
<style>
    #nav {
        position: absolute;
        width: 300px;
        height: 500px;
        overflow-y: scroll;
    }
    #nav div {
        height: 22px;
    }
    #nav .is-diff  {
        background-color: #ff0000;
    }
</style>
<script>
    <%
    // 显示右侧导航
    if (isShowField) {
    %>
    $(function() {
        <%
        StringBuffer sbNav = new StringBuffer();
        sbNav.append("<div id='nav'>");
        Iterator<String> irNav = vNav.iterator();
        n = 1;
        while (irNav.hasNext()) {
            String tbNameDesc = irNav.next();
            String[] ary = StrUtil.split(tbNameDesc, ",");
            String tbName = ary[0];
            int isDiff = StrUtil.toInt(ary[1], 0);
            String cls = isDiff==0? "":"is-diff"; // 如果表格或字段有差异
            sbNav.append("<div class=" + cls + ">" + n + "、<a href='#" + tbName + "'>");
            sbNav.append(tbName);
            sbNav.append("</a></div>");
            n ++;
        }
        sbNav.append("</div>");
        %>
        var strNav = "<%=sbNav.toString()%>";
        $('body').append(strNav);
        var topNav = 100; // $('#table1').offset().top;
        var leftNav = 1000 + 50;
        $('#nav').offset({top: topNav, left: leftNav});
    })

    $(window).scroll(function(){
        $('#nav').offset({top: $(document).scrollTop() + 100});
    });
    <%
    }
    %>
    function syn(tableName) {
        jConfirm('您确定要同步么？', '提示', function(r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "../datadict/synTable.do",
                    contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                        tableName: tableName
                    },
                    dataType: "html",
                    beforeSend: function(XMLHttpRequest){
                        $('body').showLoading();
                    },
                    success: function(data, status){
                        data = $.parseJSON(data);
                        if (data.ret=="0") {
                            jAlert(data.msg, "提示");
                        }
                        else {
                            jAlert(data.msg, "提示");
                            if (data.isTableNew) {
                                $('#' + tableName + '_title').html('[名称未填]');
                                $('#' + tableName + '_title').removeClass('diff');
                            }
                            $('#tabCol_' + tableName).find('.diff').removeClass('diff');
                            $('#tabCol_' + tableName).find('.tr-new').removeClass('tr-new');
                        }
                    },
                    complete: function(XMLHttpRequest, status){
                        $('body').hideLoading();
                    },
                    error: function(XMLHttpRequest, textStatus){
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }

    function synAll() {
        jConfirm('您确定要全部同步么？', '提示', function (r) {
            if (r) {
                $.ajax({
                    type: "post",
                    url: "../datadict/synAllTable.do",
                    contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
                    data: {
                        dbSource: "<%=dbSource%>"
                    },
                    dataType: "html",
                    beforeSend: function(XMLHttpRequest){
                        $('body').showLoading();
                    },
                    success: function(data, status) {
                        data = $.parseJSON(data);
                        if (data.ret=="0") {
                            jAlert(data.msg, "提示");
                        }
                        else {
                            jAlert(data.msg, "提示", function () {
                                window.location.href = "db_dict.jsp?dbSource=" + $('#dbSource').val() + "&isFilterForm=" + $('#isFilterForm').prop("checked") + "&isShowField=" + $('#isShowField').prop("checked");;
                            });
                        }
                    },
                    complete: function(XMLHttpRequest, status){
                        $('body').hideLoading();
                    },
                    error: function(XMLHttpRequest, textStatus){
                        // 请求出错处理
                        alert(XMLHttpRequest.responseText);
                    }
                });
            }
        });
    }
</script>
</html>
