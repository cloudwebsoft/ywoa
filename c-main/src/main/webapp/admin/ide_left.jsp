<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.json.*" %>
<%@ page import="org.jdom.*" %>
<%@ page import="org.jdom.input.*" %>
<%@ page import="org.jdom.output.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConfigUtil" %>
<%@ page import="org.jdom.Document" %>
<%@ page import="org.xml.sax.InputSource" %>
<%@ page import="com.cloudweb.oa.base.IConfigUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.utils.ProxoolUtil" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String op = ParamUtil.get(request, "op");
    if ("delConn".equals(op)) {
        boolean r = false;
        JSONObject json = new JSONObject();
        String dbSource = ParamUtil.get(request, "dbSource");
        if (dbSource.equals("oa")) {
            json.put("ret", "0");
            json.put("msg", "不能删除oa");
            out.print(json);
            return;
        }

        try {
            IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
            String xml = configUtil.getXml("config_sys");
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(new InputSource(new StringReader(xml)));

            Element root = doc.getRootElement();
            Element which = root.getChild("DataBase");
            // 先删后加
            List list = which.getChildren("db");
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element e = (Element) ir.next();
                if (e.getChildText("name").equals(dbSource)) {
                    which.removeContent(e);
                    break;
                }
            }

            configUtil.putXml("config_sys", doc);
        } catch (org.jdom.JDOMException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        try {
            ProxoolUtil proxoolUtil = SpringUtil.getBean(ProxoolUtil.class);
            Document doc = proxoolUtil.getDoc();

            Element root = doc.getRootElement();

            // 先删后加
            List list = root.getChildren();
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element e = (Element) ir.next();
                if (e.getChildText("alias").equals(dbSource)) {
                    root.removeContent(e);
                    break;
                }
            }

            proxoolUtil.write();

            Global.getInstance().init();

            org.logicalcobwebs.proxool.ProxoolFacade.removeAllConnectionPools(5000); //
            org.logicalcobwebs.proxool.configuration.JAXPConfigurator.configure(proxoolUtil.getCfgPath(), false);

            r = true;
        } finally {

        }
        if (r) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        out.print(json);
        return;
    } else if (op.equals("getTables")) {
        String dbSource = ParamUtil.get(request, "dbSource");
%>
<option value="">请选择</option>
<%
    try {
        JdbcTemplate jt = new JdbcTemplate(dbSource);
        Iterator ir = jt.getTableNames().iterator();
        while (ir.hasNext()) {
            String tableName = (String) ir.next();
%>
<option value="<%=tableName%>"><%=tableName%>
</option>
<%
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return;
} else if (op.equals("getFields")) {
    String table = ParamUtil.get(request, "table");
    String dbSource = ParamUtil.get(request, "dbSource");

    String sql = "select * from " + table;
    com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
    try {
        conn.setMaxRows(1); //尽量减少内存的使用
        ResultSet rs = conn.executeQuery(sql);
        ResultSetMetaData rm = rs.getMetaData();
        int colCount = rm.getColumnCount();
        for (int i = 1; i <= colCount; i++) {
            //System.out.println(rm.getColumnName(i));
%>
<div><a href="javascript:;" onclick="selField(' <%=rm.getColumnName(i)%>');"><%=rm.getColumnName(i)%>
</a></div>
<%
        }
    } finally {
        conn.close();
    }
    return;
} else if (op.equals("getFormFields")) {
    String formCode = ParamUtil.get(request, "formCode");
    FormDb fd = new FormDb();
    fd = fd.getFormDb(formCode);
    Iterator ir = fd.getFields().iterator();
    while (ir.hasNext()) {
        FormField ff = (FormField) ir.next();
%>
<div class="item"><a title="点击插入字段" href="javascript:;" onclick="selField('<%=ff.getName()%>')"><%=ff.getTitle()%>
</a></div>
<%
        }
        return;
    } else if (op.equals("getScriptTemplate")) {
        long id = ParamUtil.getLong(request, "id");
        FormDb fd = new FormDb();
        fd = fd.getFormDb("script_template");
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        fdao = fdao.getFormDAO(id, fd);
        out.print(fdao.getFieldValue("script"));
        return;
    } else if (op.equals("getLoadSql")) {
        String dbSource = ParamUtil.get(request, "dbSource");
        String tableName = ParamUtil.get(request, "tableName");
        String sql = "select * from " + tableName;
        com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
        try {
            conn.setMaxRows(1); //尽量减少内存的使用
            ResultSet rs = conn.executeQuery(sql);
            ResultSetMetaData rm = rs.getMetaData();
            int colCount = rm.getColumnCount();
            String fields = "", token = "";
            for (int i = 1; i <= colCount; i++) {
                if (fields.equals("")) {
                    fields = rm.getColumnName(i);
                } else {
                    fields += ", " + rm.getColumnName(i);
                }
            }
            sql = "sql = \"select " + fields + " from " + tableName + "\";";
            out.print(sql);
        } finally {
            conn.close();
        }
        return;
    } else if (op.equals("getInsertSql")) {
        String dbSource = ParamUtil.get(request, "dbSource");
        String tableName = ParamUtil.get(request, "tableName");
        String sql = "select * from " + tableName;
        com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
        try {
            conn.setMaxRows(1); //尽量减少内存的使用
            ResultSet rs = conn.executeQuery(sql);
            ResultSetMetaData rm = rs.getMetaData();
            int colCount = rm.getColumnCount();
            String fields = "", token = "";
            for (int i = 1; i <= colCount; i++) {
                if (fields.equals("")) {
                    fields = rm.getColumnName(i);
                    token = "?";
                } else {
                    fields += ", " + rm.getColumnName(i);
                    token += ", ?";
                }
            }
            sql = "sql = \"insert into " + tableName + " (" + fields + ") values (" + token + ")\";";
            out.print(sql);
        } finally {
            conn.close();
        }
        return;
    } else if (op.equals("getUpdateSql")) {
        String dbSource = ParamUtil.get(request, "dbSource");
        String tableName = ParamUtil.get(request, "tableName");
        String sql = "select * from " + tableName;
        com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
        try {
            conn.setMaxRows(1); //尽量减少内存的使用
            ResultSet rs = conn.executeQuery(sql);
            ResultSetMetaData rm = rs.getMetaData();
            int colCount = rm.getColumnCount();
            String fields = "";
            for (int i = 1; i <= colCount; i++) {
                if (fields.equals("")) {
                    fields = rm.getColumnName(i) + "=?";
                } else {
                    fields += ", " + rm.getColumnName(i) + "=?";
                }
            }
            sql = "// 修改语句，请检查主键是否正确\r\n";
            sql += "sql = \"update " + tableName + " set " + fields + " where id=?\";";
            out.print(sql);
        } finally {
            conn.close();
        }
        return;
    } else if (op.equals("getDeleteSql")) {
        String dbSource = ParamUtil.get(request, "dbSource");
        String tableName = ParamUtil.get(request, "tableName");
        String sql = "// 删除语句，请检查主键是否正确\r\n";
        sql += "sql = \"delete from " + tableName + " where id=?\";";
        out.print(sql);

        return;
    }

%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>脚本设计器 - 菜单</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <%@ include file="../inc/nocache.jsp" %>
    <style>
        html {
            min-height: 100%;
            _height: 100%;
            height: 100%;
        }

        body {
            margin: 0;
            padding: 0;
            min-height: 100%;
            _height: 100%;
            height: 100%;
        }

        .tabDiv {
            min-height: 100%;
            _height: 100%;
            height: 100%;
            padding-top: 10px;
        }

        .item {
            padding: 3px;
            margin-left: 5px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="../js/jquery.form.js"></script>
    <script src="../inc/livevalidation_standalone.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body style="margin:0px;padding:0px">
<%
    /*
    LeafPriv lp = new LeafPriv(flowTypeCode);
    if (!(lp.canUserExamine(privilege.getUser(request)))) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    */
    String formCode = ParamUtil.get(request, "formCode");

    FormDb fd = new FormDb();
    if (!formCode.equals("")) {
        fd = fd.getFormDb(formCode);
    }
%>
<div id="tabs" style="height:100%">
    <ul>
        <li><a href="#tabs-1">表单</a></li>
        <li><a href="#tabs-2">脚本库</a></li>
        <li><a href="#tabs-3">数据源</a></li>
    </ul>
    <div id="tabs-1" class="tabDiv">
        <div class="item">
            <strong>表单</strong>
            <select id="formCode" name="formCode">
                <option value="">请选择</option>
                <%
                    Iterator ir = fd.list().iterator();
                    while (ir.hasNext()) {
                        FormDb fd2 = (FormDb) ir.next();
                %>
                <option value="<%=fd2.getCode()%>"><%=fd2.getName()%>
                </option>
                <%
                    }
                %>
            </select>
            <script>
                $('#formCode').val('<%=formCode%>');
                o("formCode").value = "<%=formCode%>";

                $('#formCode').select2();
            </script>
        </div>
        <%
            String formTableName = "";
            if (!formCode.equals("")) {
                formTableName = FormDb.getTableName(formCode);
            }
        %>
        <script>
            var formTableName = "<%=formTableName%>";
        </script>
        <div class="item">
            表单编码：<a title="点击插入字段" href="javascript:;" onclick="selField('<%=formCode%>')"><%=formCode%>
        </a>
        </div>
        <div class="item">
            SQL:&nbsp;&nbsp;<a href="javascript:;" onClick="addLoadSql('<%=Global.getDefaultDB()%>', formTableName)">选择</a>
            &nbsp;&nbsp;<a href="javascript:;" onClick="addInsertSql('<%=Global.getDefaultDB()%>', formTableName)">增加</a>
            &nbsp;&nbsp;<a href="javascript:;" onClick="getDeleteSql('<%=Global.getDefaultDB()%>', formTableName)">删除</a>
            &nbsp;&nbsp;<a href="javascript:;" onClick="getUpdateSql('<%=Global.getDefaultDB()%>', formTableName)">修改</a>
        </div>
        <%
            MacroCtlMgr mm = new MacroCtlMgr();
            FormDb fdNest = new FormDb();
            ir = fd.getFields().iterator();
            while (ir.hasNext()) {
                FormField ff = (FormField) ir.next();
                if (ff.getType().equals(FormField.TYPE_MACRO) && (ff.getMacroType().equals(ConstUtil.NEST_SHEET) || ff.getMacroType().equals(ConstUtil.NEST_TABLE))) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    String nestFormCode = mu.getIFormMacroCtl().getFormCode(request, ff);
                    fdNest = fdNest.getFormDb(nestFormCode);
        %>
        <div class="item"><a title="点击插入" href="javascript:;" onclick="selField('<%=nestFormCode%>')"><%=fdNest.getName()%>(<%=nestFormCode%>)</a></div>
        <%
                }
            }
        %>
        <div class="item"><b>---&nbsp;字段&nbsp;---</b></div>
        <div id="formFieldsDiv">
            <%
                ir = fd.getFields().iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField) ir.next();
            %>
            <div class="item"><a title="点击插入" href="javascript:;" onClick="selField('<%=ff.getName()%>')"><%=ff.getTitle()%>(<%=ff.getName()%>)</a></div>
            <%
                }
            %>
        </div>
    </div>
    <div id="tabs-2" class="tabDiv">
        <%
            String scriptFormCode = "script_template";
            String sql = "select id from " + FormDb.getTableName(scriptFormCode) + " order by id desc";
            com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
            ir = fdao.list(scriptFormCode, sql).iterator();
            while (ir.hasNext()) {
                fdao = (com.redmoon.oa.visual.FormDAO) ir.next();
        %>
        <div class="item"><a title="点击插入脚本" href="javascript:;" onclick="selScriptTemplate(<%=fdao.getId()%>)"><%=fdao.getFieldValue("name")%>
        </a></div>
        <%
            }
        %>
    </div>
    <div id="tabs-3" class="tabDiv">
        <div class="item">
            <div style="text-align:center">
                <div style="margin: 5px 0px 10px 0px">
                    <a href="javascript:;" onclick="openWin('db_conn_add.jsp', 640, 480)">增加</a>
                    &nbsp;&nbsp;
                    <a href="javascript:;" onclick="if ('oa'==$('#dbSource').val()) {alert('不能修改默认的数据源oa'); return;} openWin('db_conn_edit.jsp?dbSource=' + $('#dbSource').val(), 640, 480)">修改</a>
                    &nbsp;&nbsp;
                    <a href="javascript:;" onclick="delConn()">删除</a>
                </div>
                <script>
                    function delConn() {
                        if ("oa" == $('#dbSource').val()) {
                            jAlert("不能删除oa", "提示");
                            return;
                        }
                        jConfirm('您确定要删除么？', '提示', function (r) {
                            if (r) {
                                $.ajax({
                                    type: "post",
                                    url: "ide_left.jsp",
                                    data: {
                                        op: "delConn",
                                        dbSource: $('#dbSource').val()
                                    },
                                    dataType: "html",
                                    beforeSend: function (XMLHttpRequest) {
                                        //  ShowLoading();
                                    },
                                    success: function (data, status) {
                                        var r = $.parseJSON(data);
                                        if (r.ret == "1") {
                                            jAlert(r.msg, "提示", function () {
                                                $("#dbSource option[value='" + $('#dbSource').val() + "']").remove();
                                            });
                                        } else {
                                            jAlert(r.msg, "提示");
                                        }
                                    },
                                    complete: function (XMLHttpRequest, status) {
                                        // HideLoading();
                                    },
                                    error: function (XMLHttpRequest, textStatus) {
                                        // 请求出错处理
                                        alert(XMLHttpRequest.responseText);
                                    }
                                });
                            }
                        });
                    }
                </script>
            </div>
            <strong>数据源</strong>
            <select id="dbSource" name="dbSource">
                <option value="">请选择</option>
                <%
                    cn.js.fan.web.Config cfg = new cn.js.fan.web.Config();
                    ir = cfg.getDBInfos().iterator();
                    while (ir.hasNext()) {
                        DBInfo di = (DBInfo) ir.next();
                %>
                <option value="<%=di.name%>" <%=di.isDefault ? "selected" : ""%>><%=di.name%>
                </option>
                <%
                    }
                %>
            </select>
        </div>
        <div class="item">
            表名
            <select id="tables" name="tables">
            </select>
        </div>
        <div class="item">
            SQL&nbsp;&nbsp;
            <a href="javascript:;" onclick="addLoadSql(o('dbSource').value, o('tables').value)">选择</a>
            &nbsp;&nbsp;<a href="javascript:;" onclick="addInsertSql(o('dbSource').value, o('tables').value)">增加</a>
            &nbsp;&nbsp;<a href="javascript:;" onclick="getDeleteSql(o('dbSource').value, o('tables').value)">删除</a>
            &nbsp;&nbsp;<a href="javascript:;" onclick="getUpdateSql(o('dbSource').value, o('tables').value)">修改</a>
        </div>
        <div class="item">
            <div id="fieldsDiv"></div>
        </div>
    </div>
</div>
</body>
<script>
    $(function () {
        // 创建tabs
        $('#tabs').tabs();

        $('#dbSource').change(function () {
            if ($(this).val() == "")
                return;
            // 取所选数据源的表名
            var str = "op=getTables&dbSource=" + $(this).val();
            var myAjax = new cwAjax.Request(
                "ide_left.jsp",
                {
                    method: "post",
                    parameters: str,
                    onComplete: doGetTableOptions,
                    onError: errFunc
                }
            );
        });

        $('#tables').change(function () {
            if ($(this).val() == "")
                return;
            // 取所选数据源的表名
            var str = "op=getFields&table=" + $(this).val() + "&dbSource=" + $('#dbSource').val();
            var myAjax = new cwAjax.Request(
                "ide_left.jsp",
                {
                    method: "post",
                    parameters: str,
                    onComplete: doGetFields,
                    onError: errFunc
                }
            );
        });

        $('#formCode').change(function () {
            formTableName = "ft_" + $(this).val();
            if ($(this).val() == "")
                return;
            // 取所选数据源的表名
            var str = "op=getFormFields&formCode=" + $(this).val();
            var myAjax = new cwAjax.Request(
                "ide_left.jsp",
                {
                    method: "post",
                    parameters: str,
                    onComplete: doGetFormFields,
                    onError: errFunc
                }
            );
        });

        // 取默认数据源的表名
        var str = "op=getTables&dbSource=<%=Global.getDefaultDB()%>";
        var myAjax = new cwAjax.Request(
            "ide_left.jsp",
            {
                method: "post",
                parameters: str,
                onComplete: doGetTableOptions,
                onError: errFunc
            }
        );
    });

    function doGetTableOptions(response) {
        var rsp = response.responseText.trim();
        // alert(rsp);
        $("#tables").empty();
        $("#tables").append(rsp);
    }

    function doGetFields(response) {
        var rsp = response.responseText.trim();
        // alert(rsp);
        $("#fieldsDiv").html(rsp);
    }

    function doGetFormFields(response) {
        var rsp = response.responseText.trim();
        // alert(rsp);
        $("#formFieldsDiv").html(rsp);
    }

    var errFunc = function (response) {
        window.status = response.responseText;
    }

    function selField(fieldName) {
        // window.top.mainScriptFrame.insertScript(" " + fieldName);
        var data = {
            "type": "insertScript",
            "data": "" + fieldName
        }

        window.top.mainFrame.mainScriptFrame.postMessage(data, '*');
    }

    function selScriptTemplate(id) {
        $.ajax({
            type: "post",
            url: "ide_left.jsp",
            data: {
                op: "getScriptTemplate",
                id: id
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {
                selField("\r\n\r\n" + data.trim());
            },
            complete: function (XMLHttpRequest, status) {
                //HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function addLoadSql(dbSource, tableName) {
        if (tableName == "") {
            alert("请选择数据表！");
            return;
        }
        $.ajax({
            type: "post",
            url: "ide_left.jsp",
            data: {
                op: "getLoadSql",
                dbSource: dbSource,
                tableName: tableName
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {
                selField("\r\n\r\n" + data.trim());
            },
            complete: function (XMLHttpRequest, status) {
                //HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }


    function addInsertSql(dbSource, tableName) {
        if (tableName == "") {
            alert("请选择数据表！");
            return;
        }
        $.ajax({
            type: "post",
            url: "ide_left.jsp",
            data: {
                op: "getInsertSql",
                dbSource: dbSource,
                tableName: tableName
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {
                selField("\r\n\r\n" + data.trim());
            },
            complete: function (XMLHttpRequest, status) {
                //HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function getUpdateSql(dbSource, tableName) {
        if (tableName == "") {
            jAlert("请选择数据表！", "提示");
            return;
        }

        $.ajax({
            type: "post",
            url: "ide_left.jsp",
            data: {
                op: "getUpdateSql",
                dbSource: dbSource,
                tableName: tableName
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {
                selField("\r\n\r\n" + data.trim());
            },
            complete: function (XMLHttpRequest, status) {
                //HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    function getDeleteSql(dbSource, tableName) {
        if (tableName == "") {
            alert("请选择数据表！");
            return;
        }

        $.ajax({
            type: "post",
            url: "ide_left.jsp",
            data: {
                op: "getDeleteSql",
                dbSource: dbSource,
                tableName: tableName
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                //ShowLoading();
            },
            success: function (data, status) {
                selField("\r\n\r\n" + data.trim());
            },
            complete: function (XMLHttpRequest, status) {
                //HideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    <%
        com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
        com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
        String version = StrUtil.getNullStr(oaCfg.get("version"));
        String spVersion = StrUtil.getNullStr(spCfg.get("version"));
    %>

    var onMessage = function (e) {
        var d = e.data;
        var type = "insertScript";
        if (d.type == "openerScript") {
            type = "openerScript";
        }
        var data = {
            "type": type,
            "version": "<%=version%>",
            "spVersion": "<%=spVersion%>",
            "data": d.data,
            "scene": d.scene
        };

        if (window.top.mainFrame.mainScriptFrame) {
            window.top.mainFrame.mainScriptFrame.postMessage(data, '*');
        }
    };

    $(function () {
        if (window.addEventListener) { // all browsers except IE before version 9
            window.addEventListener("message", onMessage, false);
        } else {
            if (window.attachEvent) { // IE before version 9
                window.attachEvent("onmessage", onMessage);
            }
        }

        var data = {
            "type": "getScript"
        }
        var win = window.top.opener;
        win.postMessage(data, '*');
    });
</script>
</html>
