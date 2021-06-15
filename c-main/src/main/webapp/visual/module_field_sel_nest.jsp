<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.flow.FormViewDb" %>
<%@ page import="com.redmoon.oa.flow.macroctl.domain.NestFieldMaping" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Vector" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>嵌套表属性</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css"/>
    <link href="../js/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
    <style>
        .row {
            margin-left: 0px;
        }
    </style>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery.flexbox.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../js/bootstrap/js/bootstrap.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
</head>
<body>
<%
    String editFlag = ParamUtil.get(request, "editFlag");
    if (editFlag.equals("")) {
%>
<%@ include file="module_field_sel_inc_menu_top.jsp" %>
<script>
    o("menu2").className = "current";
</script>
<%
    }
%>
<div class="spacerH"></div>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    // 当从模式对话框打开本窗口时，因为分属于不同的IE进程，SESSION会丢失，可以用cookie中置sessionId来解决这个问题
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    // 主表单的编码
    String openerFormCode = ParamUtil.get(request, "openerFormCode");
    //原关联嵌套表编码
    String oldRelateCode = ParamUtil.get(request, "oldRelateCode");

    String nestType = ParamUtil.get(request, "nestType");
    String params = ParamUtil.get(request, "params");
    String newRelateCode = ParamUtil.get(request, "newRelateCode");
    JSONObject jsonObject = null;
    if (params != null && !"".equals(params)) {
        jsonObject = new JSONObject(params);
    }

    ModuleRelateDb mrd = new ModuleRelateDb();
    String op = ParamUtil.get(request, "op");
    if ("add".equals(op)) {
        mrd = mrd.getModuleRelateDb(openerFormCode, newRelateCode);
        if (mrd != null) {
            return;
        }
        mrd = new ModuleRelateDb();
        //获取所有关联模块
        Vector tempV = mrd.getModulesRelated(openerFormCode);
        String conds = "";
        boolean re = mrd.create(new JdbcTemplate(), new Object[]{
                openerFormCode, newRelateCode, "id",
                new Integer(ModuleRelateDb.TYPE_MULTI),
                new Double(tempV.size() + 1), new Integer(com.redmoon.oa.flow.FormDAO.STATUS_DONE), conds});
        return;
    } else if ("edit".equals(op)) {
        //删除原关联
        mrd = mrd.getModuleRelateDb(openerFormCode, oldRelateCode);
        if (mrd != null) {
            mrd.del();
        }
        mrd = null;
        //添加新关联
        mrd = new ModuleRelateDb();
        //获取所有关联模块
        Vector v = mrd.getModulesRelated(openerFormCode);

        int cwsStatus = 1; // 从模块其对应关联模块选项卡的列表记录的状态过滤
        String conds = "";
        boolean re = mrd.create(new JdbcTemplate(), new Object[]{
                openerFormCode, newRelateCode, "id",
                new Integer(ModuleRelateDb.TYPE_MULTI),
                new Double(v.size() + 1), cwsStatus, conds});
        return;
    }
%>
<table width="100%" align="center" cellPadding="0" cellSpacing="0" class="tabStyle_1" id="mapTable">
    <tbody>
    <tr>
        <td height="28" colspan="4" class="tabStyle_1_title">嵌套表格</td>
    </tr>
    <tr>
        <td width="11%" height="42" align="center">
            源模块
        </td>
        <td width="30%" height="42" align="center">
            <div id="forms" style="float:left"></div>
            <span><a id="btnSourceMoudle" href="javascript:" title="维护模块">维护</a></span>
            <%
                //获取源表单与嵌套表单信息
                String sourceFormCode = "";
                String destFormCode = "";
                String sourceFormName = "";
                String destFormName = "";
                String cond = "";
                int formViewId = -1;
                String canAdd = "", canEdit = "", canImport = "", canDel = "", canSel = "", canExport = "", propStat = "";
                int isTab = 0, isPage = 0, pageSize = 20, isSearchable = 0, isAddHighlight = 0;
                boolean isAgainst = false;
                boolean isAutoSel = false;
                JSONArray maps = new JSONArray();
                List<NestFieldMaping> list = null;
                if (jsonObject != null) {
                    sourceFormCode = jsonObject.getString("sourceForm");
                    destFormCode = jsonObject.getString("destForm");
                    cond = jsonObject.getString("filter");

                    cond = com.redmoon.oa.visual.ModuleUtil.decodeFilter(cond);
                    maps = jsonObject.getJSONArray("maps");

                    try {
                        canAdd = jsonObject.getString("canAdd");
                        canEdit = jsonObject.getString("canEdit");
                        canImport = jsonObject.getString("canImport");
                        canDel = jsonObject.getString("canDel");
                        canSel = jsonObject.getString("canSel");
                        isAgainst = "1".equals(jsonObject.getString("isAgainst"));
                        isAutoSel = "1".equals(jsonObject.getString("isAutoSel"));

                        if (jsonObject.has("formViewId")) {
                            formViewId = StrUtil.toInt(jsonObject.getString("formViewId"), -1);
                        }

                        if (jsonObject.has("propStat")) {
                            propStat = jsonObject.getString("propStat");
                        }

                        if (jsonObject.has("canExport")) {
                            canExport = jsonObject.getString("canExport");
                        }

                        if (jsonObject.has("isTab")) {
                            isTab = jsonObject.getInt("isTab");
                        }

                        if (jsonObject.has("isPage")) {
                            isPage = jsonObject.getInt("isPage");
                            pageSize = jsonObject.getInt("pageSize");
                        }

                        if (jsonObject.has("isSearchable")) {
                            isSearchable = jsonObject.getInt("isSearchable");
                        }
                        if (jsonObject.has("isAddHighlight")) {
                            isAddHighlight = jsonObject.getInt("isAddHighlight");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    NestFieldMaping nfm = null;
                    list = new ArrayList<NestFieldMaping>();
                    for (int i = 0; i < maps.length(); i++) {
                        //Map map = (HashMap)maps.get(i);
                        JSONObject temp = new JSONObject();
                        nfm = new NestFieldMaping();
                        temp = (JSONObject) maps.get(i);
                        nfm.setSourceFieldCode(temp.getString("sourceField"));
                        nfm.setDestFieldCode(temp.getString("destField"));
                        list.add(nfm);
                    }
                }

                boolean isComb = cond.startsWith("<items>") || "".equals(cond);
                String cssComb = "", cssScript = "";
                String kind;
                if (isComb) {
                    cssComb = "in active";
                    kind = "comb";
                } else {
                    cssScript = "in active";
                    kind = "script";
                }

                //若原嵌套表单编码为空，则获取设置嵌套表单编码
                if (oldRelateCode == null || "".equals(oldRelateCode)) {
                    oldRelateCode = destFormCode;
                }

                ModuleSetupDb msd = new ModuleSetupDb();
                if (!"".equals(sourceFormCode)) {
                    msd = msd.getModuleSetupDb(sourceFormCode);
                    if (msd == null) {
                        out.print(SkinUtil.makeErrMsg(request, "表单 " + sourceFormCode + " 不存在"));
                        return;
                    }
                    sourceFormName = msd.getString("name");
                    msd = msd.getModuleSetupDb(destFormCode);
                    destFormName = msd.getString("name");
                }
                Vector v = msd.listUsed();
                Iterator ir = v.iterator();
                String json = "";
                while (ir.hasNext()) {
                    msd = (ModuleSetupDb) ir.next();
                    if ("".equals(json)) {
                        json = "{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";
                    } else {
                        json += ",{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";
                    }
                }
            %>
            <script>
                var sourceField;
                var sources = [];
                var sourceCondField;
                var forms = $('#forms').flexbox({
                    "results": [<%=json%>],
                    "total":<%=v.size()%>
                }, {
                    initialValue: '<%=sourceFormName%>',
                    watermark: '请选择表单',
                    paging: false,
                    maxVisibleRows: 10,
                    onSelect: function () {
                        o("sourceFieldTd").innerHTML = "<div id='sourceField'></div>";
                        o("sourceCondFieldDiv").innerHTML = "<div id='sourceCondField'></div>";
                        $.getJSON('../flow/form_data_map_ajax.jsp', {
                            "mode": "module",
                            "sourceFormCode": $("input[name=forms]").val()
                        }, function (data) {
                            sources = data.result;
                            sourceField = $('#sourceField').flexbox(
                                {
                                    "results": data.result,
                                    "total": data.total
                                },
                                {
                                    watermark: '请选择表单域',
                                    paging: false,
                                    maxVisibleRows: 8
                                }
                            );

                            sourceCondField = $("#sourceCondField").flexbox(
                                {
                                    "results": data.result,
                                    "total": data.total
                                },
                                {
                                    watermark: '请选择表单域',
                                    paging: false,
                                    maxVisibleRows: 8
                                }
                            );
                        });
                    }
                });

                //显示原设置源表单
                if ('<%=sourceFormName%>' != "") {
                    // console.log("sourceFormName=<%=sourceFormName%>,sourceFormCode=<%=sourceFormCode%>");
                    $("#sourceFieldTd").html("<div id='sourceField'></div>");
                    $("#sourceCondFieldDiv").html("<div id='sourceCondField'></div>");
                    $(function () {
                        $.getJSON('../flow/form_data_map_ajax.jsp', {
                            "mode": "module",
                            "sourceFormCode": '<%=sourceFormCode%>'
                        }, function (data) {
                            // console.log("data=" + data);
                            sources = data.result;
                            sourceField = $('#sourceField').flexbox(
                                {
                                    "results": data.result,
                                    "total": data.total
                                },
                                {
                                    watermark: '请选择表单域',
                                    paging: false,
                                    maxVisibleRows: 8
                                }
                            );

                            sourceCondField = $("#sourceCondField").flexbox(
                                {
                                    "results": data.result,
                                    "total": data.total
                                },
                                {
                                    watermark: '请选择表单域',
                                    paging: false,
                                    maxVisibleRows: 8
                                }
                            );
                        });
                    });
                }

                // 检查类型是否匹配
                function isTypeMatched(sourceValue, destValue, sources, dests) {
                    for (var one in sources) {
                        if (sources[one].id == sourceValue) {
                            for (var key in dests) {
                                if (dests[key].id == destValue) {
                                    // alert(dests[key].type + " " + sources[one].type);
                                    var isCheck = false;
                                    // 检查日期型及宏控件
                                    if (dests[key].type == "DATE" || dests[key].type == "DATE_TIME" || sources[one].type == "DATE" || sources[one].type == "DATE_TIME")
                                        isCheck = true;
                                    if (dests[key].type == "<%=FormField.TYPE_MACRO%>" || sources[one].type == "<%=FormField.TYPE_MACRO%>")
                                        isCheck = true;
                                    if (isCheck) {
                                        if (dests[key].type == "<%=FormField.TYPE_MACRO%>" || sources[one].type == "<%=FormField.TYPE_MACRO%>") {
                                            if (dests[key].macroType == "nest_table" && sources[one].macroType == "nest_table") {
                                                if (dests[key].defaultValue == sources[one].defaultValue) {
                                                    // alert(true);
                                                    return true;
                                                }
                                            }
                                            if (dests[key].macroType != sources[one].macroType) {
                                                if (dests[key].type == "<%=FormField.TYPE_TEXTFIELD%>" || dests[key].type == "<%=FormField.TYPE_TEXTAREA%>"
                                                    || sources[one].id == "<%=com.redmoon.oa.visual.FormDAO.FormDAO_NEW_ID%>"
                                                    || sources[one].id == "FormDAO_ID") {
                                                    ;
                                                } else {
                                                    alert("宏控件 " + dests[key].name + " 与 " + sources[one].name + "的类型不一致");
                                                    return false;
                                                }
                                            }
                                        } else {
                                            if (dests[key].type == sources[one].type
                                                || sources[one].id == "<%=com.redmoon.oa.visual.FormDAO.FormDAO_NEW_ID%>"
                                                || sources[one].id == "FormDAO_ID") {
                                                // alert(true);
                                                return true;
                                            } else {
                                                alert("字段 " + dests[key].name + " 与 " + sources[one].name + "的类型不一致");
                                                return false;
                                            }
                                        }
                                    } else
                                        break;
                                }
                            }
                            break;
                        }
                    }
                    return true;
                }

                function addMapNest1(sourceFieldValue, sourceFieldName, destFieldValue, destFieldName) {
                    if (sourceFieldValue == "" || destFieldValue == "") {
                        return;
                    }
                    // 如果类型匹配
                    if (isTypeMatched(sourceFieldValue, destFieldValue, sources, dests)) {
                        var trId = "tr_" + sourceFieldValue + "_" + destFieldValue;
                        // 检测trId是否已存在
                        var isFound = false;
                        $("#mapTableNest tr").each(function (k) {
                            if ($(this).attr("id") == trId) {
                                isFound = true;
                                return;
                            }
                        });

                        if (isFound) {
                            alert("存在重复映射！");
                            return;
                        }

                        var tr = "<tr id='" + trId + "' sourceField='" + sourceFieldValue + "' destField='" + destFieldValue + "'>";
                        tr += "<td align='center'>来源</td>";
                        tr += "<td align='center'>" + sourceFieldName + "</td>";
                        tr += "<td align='center'>目标</td>";
                        tr += "<td align='center'>" + destFieldName + "</td>";
                        tr += "<td align='center'>";
                        tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$(this).parent().parent().remove()\">删除</a></td>";
                        tr += "</tr>";
                        $("#mapTableNest tr:last").after(tr);
                    } else {
                        return; // alert("类型不匹配，无法映射！");
                    }
                }
            </script>
        </td>
        <td width="17%" colspan="-1" align="center">嵌套表单</td>
        <td align="center">
            <div id="destForm" style="float: left"></div>
            <span><a id="btnDestMoudle" href="javascript:" title="维护模块">维护</a></span>
            <script>
                var destField;
                var dests = [];
                var destCode = "";
                var destForm = $('#destForm').flexbox({
                    "results": [<%=json%>],
                    "total":<%=v.size()%>
                }, {
                    initialValue: '<%=destFormName%>',
                    watermark: '请选择表单',
                    paging: false,
                    maxVisibleRows: 10,
                    onSelect: function () {
                        o("nestFieldTd").innerHTML = "<div id='destField'></div>";
                        destCode = $("input[name=destForm]").val();
                        $.getJSON('../flow/form_data_map_ajax.jsp', {
                            "mode": "module",
                            "sourceFormCode": destCode
                        }, function (data) {
                            dests = data.result;
                            destField = $('#destField').flexbox(
                                {
                                    "results": data.result,
                                    "total": data.total
                                },
                                {
                                    watermark: '请选择字段',
                                    paging: false,
                                    maxVisibleRows: 8
                                }
                            );
                        });
                        $("#params").val(combinationStr4DestFormChange());
                        $("#changeForm").submit();
                    }
                });
                //显示原设置嵌套表单
                if ('<%=destFormName%>' != "") {
                    $("#nestFieldTd").html("<div id='destField'></div>");
                    $(function () {
                        $.getJSON('../flow/form_data_map_ajax.jsp', {
                            "mode": "module",
                            "sourceFormCode": '<%=destFormCode%>'
                        }, function (data) {
                            dests = data.result;
                            destField = $('#destField').flexbox(
                                {
                                    "results": data.result,
                                    "total": data.total
                                },
                                {
                                    watermark: '请选择表单域',
                                    paging: false,
                                    maxVisibleRows: 8
                                }
                            );
                        });
                    });
                }
            </script>
        </td>
    </tr>
    <tr>
        <td height="42" align="center">自动冲抵</td>
        <td height="42" align="left">
            <%
                String strAgainstChecked = isAgainst?"checked":"";
                String strAutoSelChecked = isAutoSel?"checked":"";
            %>
            <input id="isAgainst" name="isAgainst" value="1" type="checkbox" <%=strAgainstChecked%> title="流程结束时自动冲抵源表单中的记录" />
        </td>
        <td height="42" align="center">
            自动拉单
        </td>
        <td height="42" align="left">
            <input id="isAutoSel" name="isAutoSel" value="1" type="checkbox" <%=strAutoSelChecked%> />
        </td>
    </tr>
    <tr>
        <td height="42" align="center">新增高亮</td>
        <td height="42" align="left"><input id="isAddHighlight" name="isAddHighlight" title="区别于拉单的数据，手工新增的数据高亮显示" value="1" type="checkbox" <%=isAddHighlight==1?"checked":""%> /></td>
        <td height="42" align="center">
            <%
                if (!"nest_table".equals(nestType)) {
            %>
            显示为选项卡
            <%
                }
            %>
        </td>
        <td height="42" align="left">
            <input type="checkbox" id="isTab" name="isTab" value="1" <%=isTab==1?"checked":"" %> style="display: <%=!"nest_table".equals(nestType) ? "" : "none"%>;" />
        </td>
    </tr>
    <%
        String dis = "";
        if (ConstUtil.NEST_TABLE.equals(nestType)) {
            dis = "display:none";
        }
    %>
    <tr style="<%=dis%>">
        <td height="42" align="center">分页</td>
        <td height="42" align="left">
            <input id="isPage" name="isPage" value="1" type="checkbox" <%=isPage==1?"checked":"" %> title="是否分页" />
        </td>
        <td height="42" align="center">
            每页条数
        </td>
        <td width="12%" height="42" align="left">
            <input id="pageSize" name="pageSize" value="<%=pageSize%>"/>
        </td>
    </tr>
    <tr style="<%=dis%>">
        <td height="42" align="center">查询</td>
        <td height="42" align="left">
            <input id="isSearchable" name="isSearchable" value="1" type="checkbox" <%=isSearchable==1?"checked":"" %> title="是否能查询" />
        </td>
        <td width="17%" height="42" align="center">&nbsp;</td>
        <td height="42" align="left"></td>
    </tr>
    <tr>
        <td height="42" align="center">过滤条件</td>
        <td height="42" colspan="3" align="left">
            <ul id="myTab" class="nav nav-tabs">
                <li class="dropdown active">
                    <a href="#" id="myTabDrop1" class="dropdown-toggle" data-toggle="dropdown">
                        条件<b class="caret"></b></a>
                    <ul class="dropdown-menu" role="menu" aria-labelledby="myTabDrop1">
                        <li><a href="#comb" kind="comb" tabindex="-1" data-toggle="tab">组合条件</a></li>
                        <li><a href="#script" kind="script" tabindex="-1" data-toggle="tab">脚本条件</a></li>
                    </ul>
                </li>
            </ul>
            <div id="myTabContent" class="tab-content">
                <div class="tab-pane fade <%=cssComb %>" id="comb">
                    <div style="margin:10px">
                        <img src="../admin/images/combination.png" style="margin-bottom:-5px;"/>&nbsp;<a href="javascript:;" onclick="openCondition()">配置条件</a>&nbsp;
                        <img src="../admin/images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:<%=(isComb && !cond.equals(""))?"":"none" %>;" id="imgId"/>
                        <textarea id="condition" name="condition" style="display:none" cols="80" rows="5"><%=cond %></textarea>
                    </div>
                </div>
                <div class="tab-pane fade <%=cssScript %>" id="script">
                    <div>
                        <textarea id="conds" name="conds" style="width:600px; height:150px"></textarea>
                        <br />
                        <a href="javascript:;" onclick="o('conds').value += ' {$curDate}';" title="当前日期">当前日期</a>
                        &nbsp;&nbsp;
                        <a href="javascript:;" onclick="o('conds').value += ' ={$curUser}';" title="当前用户">当前用户</a>
                        &nbsp;&nbsp;
                        <a href="javascript:;" onclick="o('conds').value += ' in ({$curUserDept})';" title="当前用户">当前用户所在的部门</a>
                        &nbsp;&nbsp;
                        <a href="javascript:;" onclick="o('conds').value += ' in ({$curUserRole})';" title="当前用户的角色">当前用户的角色</a>
                        &nbsp;&nbsp;
                        <a href="javascript:;" onclick="o('conds').value += ' {$mainId}';" title="主模块记录的ID，可用于流程中拉单时获取流程表单记录的ID、模块编辑时的主模块ID或与操作列中自定义链接联用">主模块记录的ID</a>
                        &nbsp;&nbsp;
                        <a href="javascript:;" onclick="o('conds').value += ' in ({$admin.dept})';" title="用户可以管理的部门">当前用户管理的部门</a>&nbsp;&nbsp;(注：条件不能以and开头)
                        <input type="button" value="设计器" class="btn btn-default" onclick="openIdeWin()" />
                        <textarea id="condsHelper" style="display:none"><%=cond %></textarea>
                        <script>
                            //设置原设置条件
                            var cond = $('#condsHelper').val();
                            if (cond != ""){
                                $("#conds").val(cond);
                            }
                        </script>
                    </div>
                    <div style="float:left; padding-top:4px">源表单中的</div>
                    <div id="sourceCondFieldDiv" style="float:left"><div id="sourceCondField" style="float:left"></div></div>
                    <div style="float:left">
                        <select id="token" name="token">
                            <option value="=" selected="selected">等于</option>
                            <option value="&gt;=">大于等于</option>
                            <option value="&gt;">大于</option>
                            <option value="&lt;=">小于等于</option>
                            <option value="&lt;">小于</option>
                            <option value="&lt;&gt;">不等于</option>
                            <option value="like">包含</option>
                        </select>
                        主表单中的
                        <select id="fieldOpener" name="fieldOpener">
                            <%
                                FormDb fdOpener = new FormDb();
                                fdOpener = fdOpener.getFormDb(openerFormCode);
                                ir = fdOpener.getFields().iterator();
                                while (ir.hasNext()) {
                                    FormField ff = (FormField)ir.next();
                            %>
                            <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
                            <%
                                }
                            %>
                        </select>

                        <input type="button" class="btn btn-default" value="添加" onclick="addCond()" />
                    </div>
                </div>
            </div>
            <script>
                var kind = "<%=kind%>";

                $(function(){
                    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                        kind = $(e.target).attr("kind");
                        if (kind=="script") {
                            if (o("conds").value.indexOf("<items>")==0) {
                                o("conds").value = "";
                            }
                        }
                    });
                });
            </script>

        </td>
    </tr>
    </tbody>
</table>

<table id="mapTableNest" class="tabStyle_1" width="100%" border="0" align="center" cellspacing="0">
    <tr>
        <td colspan="5" class="tabStyle_1_title">字段映射（如无映射，则不显示选择按钮）</td>
    </tr>
    <tr>
        <td width="13%" align="center">源字段</td>
        <td width="30%" id="sourceFieldTd">
            <div id="sourceField" style="float:left"></div>
        </td>
        <td width="14%" align="center">目标字段 </td>
        <td width="23%" align="center" id="nestFieldTd"><div id="destField" style="float:left"></div></td>
        <td width="20%" align="center"><input type="button" class="btn btn-default" value="添加" onclick="addMapNest()" /></td>
    </tr>
    <%
        if (list != null){
            FormDb sourceFd = new FormDb(sourceFormCode);
            FormDb destFd = new FormDb(destFormCode);
            for (NestFieldMaping nf : list) {
                String sourceFieldCode = nf.getSourceFieldCode();
                String title = "";
                if (sourceFieldCode.startsWith("main:")) {
                    String[] subFields = StrUtil.split(sourceFieldCode, ":");
                    if (subFields.length == 3) {
                        FormDb subfd = new FormDb(subFields[1]);
                        title = subfd.getFieldTitle(subFields[2]);
                    }
                } else if (sourceFieldCode.startsWith("other:")) {
                    String[] otherFields = StrUtil.split(sourceFieldCode, ":");
                    if (otherFields.length == 5) {
                        FormDb otherFormDb = new FormDb(otherFields[2]);
                        title = otherFormDb.getFieldTitle(otherFields[4]);
                    }
                }
                else if (sourceFieldCode.equalsIgnoreCase(com.redmoon.oa.visual.FormDAO.FormDAO_NEW_ID)) {
                    title = "ID";
                }
                else {
                    title = sourceFd.getFieldTitle(sourceFieldCode);
                }
                // System.out.println(getClass() + " " + sourceFieldCode + " title=" + title);
                nf.setSourceFieldName(title);
                nf.setDestFieldName(destFd.getFieldTitle(nf.getDestFieldCode()));
    %>
    <script>
        // 显示已设置字段映射内容
        addMapNest1('<%=nf.getSourceFieldCode()%>','<%=nf.getSourceFieldName()%>','<%=nf.getDestFieldCode()%>','<%=nf.getDestFieldName()%>');
    </script>
    <%
            }
        }
    %>
</table>
<table id="mapTableNest" class="tabStyle_1" width="100%" border="0" align="center" cellspacing="0">
    <tr>
        <td colspan="5" class="tabStyle_1_title">权限</td>
    </tr>
    <tr>
        <td colspan="5">
            <input id="canAdd" name="canAdd" type="checkbox" value="true" />
            &nbsp;增加
            &nbsp;&nbsp;<input id="canEdit" name="canEdit" type="checkbox" value="true" />
            &nbsp;修改
            &nbsp;&nbsp;<input id="canImport" name="canImport" type="checkbox" value="true" />
            &nbsp;导入
            &nbsp;&nbsp;<input id="canExport" name="canExport" type="checkbox" value="true" />
            &nbsp;导出
            &nbsp;&nbsp;<input id="canDel" name="canDel" type="checkbox" value="true" />
            &nbsp;删除
            &nbsp;&nbsp;<input id="canSel" name="canSel" type="checkbox" value="true" />
            &nbsp;选择
        </td>
    </tr>
</table>
<table width="100%" align="center" cellPadding="0" cellSpacing="0" class="tabStyle_1" id="mapTablePreview">
    <tr>
        <td>
            <%
                if (!ConstUtil.NEST_TABLE.equals(nestType)) {
            %>
            <jsp:include page="module_field_inc_preview.jsp">
                <jsp:param name="code" value="<%=destFormCode%>"/>
                <jsp:param name="formCode" value="<%=destFormCode%>"/>
                <jsp:param name="resource" value="nest"/>
            </jsp:include>
            <%
            } else {
            %>
            视图：
            <%
                if (!"".equals(destFormCode)) {
            %>
            <select id="formView" name="formView" title="视图">
                <option value="-1">请选择</option>
                <%
                    FormViewDb formViewDb = new FormViewDb();
                    Vector vView = formViewDb.getViews(destFormCode);
                    Iterator irView = vView.iterator();
                    while (irView.hasNext()) {
                        formViewDb = (FormViewDb)irView.next();
                %>
                <option value="<%=formViewDb.getLong("id")%>"><%=formViewDb.getString("name")%></option>
                <%
                    }
                %>
            </select>
            <%
                    }
                }
            %>
        </td>
    </tr>
    <tr>
        <td class="tabStyle_1_title">
            合计字段
        </td>
    </tr>
    <tr>
        <td>
            <%
                if (StringUtils.isNotEmpty(destFormCode)) {
            %>
            <a href="javascript:" onclick="addCalcuField()">添加字段</a>
            <div id="divCalcuField" style="text-align:left; margin-top:3px;">
                <%
                    StringBuffer optsFieldsNum = new StringBuffer();
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(destFormCode);
                    Iterator irField = fd.getFields().iterator();
                    while (irField.hasNext()) {
                        FormField ff = (FormField) irField.next();
                        int fieldType = ff.getFieldType();
                        if (fieldType == FormField.FIELD_TYPE_INT
                                || fieldType == FormField.FIELD_TYPE_FLOAT
                                || fieldType == FormField.FIELD_TYPE_DOUBLE
                                || fieldType == FormField.FIELD_TYPE_PRICE
                                || fieldType == FormField.FIELD_TYPE_LONG
                        ) {
                            optsFieldsNum.append("<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>");
                        }
                    }

                    int curCalcuFieldCount = 0;
                    if (propStat.equals("")) {
                        propStat = "{}";
                    }
                    JSONObject jsonPropStat = new JSONObject(propStat);
                    Iterator irPropStat = jsonPropStat.keys();
                    while (irPropStat.hasNext()) {
                        String key = (String) irPropStat.next();
                %>
                <div id="divCalcuField<%=curCalcuFieldCount%>" style="float:left">
                    <select id="calcFieldCode<%=curCalcuFieldCount%>" name="calcFieldCode">
                        <option value="">无</option>
                        <%=optsFieldsNum.toString()%>
                    </select>
                    <select id="calcFunc<%=curCalcuFieldCount%>" name="calcFunc">
                        <option value="0">求和</option>
                        <%--<option value="1">求平均值</option>--%>
                    </select>
                    <a href='javascript:;' onclick="var pNode=this.parentNode; pNode.parentNode.removeChild(pNode);">×</a>
                    &nbsp;
                </div>
                <script>
                    $("#calcFieldCode<%=curCalcuFieldCount%>").val("<%=key%>");
                    $("#calcFunc<%=curCalcuFieldCount%>").val("<%=jsonPropStat.get(key)%>");
                </script>
                <%
                        curCalcuFieldCount++;
                    }
                %>
            </div>
            <%
                }
            %>
        </td>
    </tr>
</table>
<form id="changeForm">
    <textarea name="params" id="params" style="display:none"><%=params%></textarea>
    <input name="nestType" id="nestType" type="hidden" value="<%=nestType%>"></input>
    <input name="openerFormCode" id="openerFormCode" type="hidden" value="<%=openerFormCode%>"></input>
    <input name="oldRelateCode" id="oldRelateCode" type="hidden" value="<%=oldRelateCode%>"></input>
    <input name="editFlag" id="editFlag" type="hidden" value="<%=editFlag%>"></input>
</form>
<div style="text-align:center; margin-top:5px; margin-bottom:20px"><input type="button" class="btn btn-default" value="确定" onclick="makeMap()" />
</div>
</body>
<script>
    <%
    if (canAdd.equals("true")) {
    %>
    setCheckboxChecked("canAdd", "true");
    <%
    }
    %>
    <%
    if (canEdit.equals("true")) {
    %>
    setCheckboxChecked("canEdit", "true");
    <%
    }
    %>
    <%
    if (canImport.equals("true")) {
    %>
    setCheckboxChecked("canImport", "true");
    <%
    }
    %>
    <%
    if (canExport.equals("true")) {
    %>
    setCheckboxChecked("canExport", "true");
    <%
    }
    %>
    <%
    if (canDel.equals("true")) {
    %>
    setCheckboxChecked("canDel", "true");
    <%
    }
    %>
    <%
    if (canSel.equals("true")) {
    %>
    setCheckboxChecked("canSel", "true");
    <%
    }
    %>
    $('#formView').val('<%=formViewId%>');

    /*
    function addAllMap() {
        for (one in sources) {
            for (key in dests) {
                if (sources[one].id==dests[key].id) {
                    var trId = "tr_" + sources[one].id + "_" + dests[key].id;
                    // 检测trId是否已存在
                    var isFound = false;
                    $("#mapTable tr").each(function(k){
                        if ($(this).attr("id")==trId) {
                            isFound = true;
                            return;
                        }
                    });

                    if (isFound) {
                        alert("存在重复映射！");
                        return;
                    }

                    var tr = "<tr id='" + trId + "' sourceField='" + sources[one].id + "' destField='" + sources[one].id + "'>";
                    tr += "<td align='center'>字段</td>";
                    tr += "<td align='center'>" + sources[one].id + "</td>";
                    tr += "<td align='center'>" + sources[one].name + "</td>";
                    tr += "<td align='center'>";
                    tr += "<input id='" + trId + "_editable' type='checkbox' value='true'>修改&nbsp;&nbsp;";
                    tr += "<input id='" + trId + "_appendable' type='checkbox' value='true'>添加&nbsp;&nbsp;";
                    tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
                    tr += "</tr>";
                    $("#mapTable tr:last").after(tr);

                    break;
                }
            }
        }
    }

    function addMap() {
        if (sourceField.getValue()=="" || destField.getValue()=="") {
            alert("请选择表单域！");
            return;
        }
        // 如果类型匹配
        if (isTypeMatched(sourceField.getValue(), destField.getValue(), sources, dests)) {
            var trId = "tr_" + sourceField.getValue() + "_" + destField.getValue();
            // 检测trId是否已存在
            var isFound = false;
            $("#mapTable tr").each(function(k){
                if ($(this).attr("id")==trId) {
                    isFound = true;
                    return;
                }
            });

            if (isFound) {
                alert("存在重复映射！");
                return;
            }

            var tr = "<tr id='" + trId + "' sourceField='" + sourceField.getValue() + "' destField='" + destField.getValue() + "'>";
            tr += "<td align='center'>字段</td>";
            tr += "<td align='center'>" + sourceField.getText() + "</td>";
            tr += "<td align='center'>" + destField.getText() + "</td>";
            tr += "<td align='center'>";
            tr += "<input id='" + trId + "_editable' type='checkbox' value='true'>修改&nbsp;&nbsp;";
            // 如果是嵌套表，则appendable才有效
            tr += "<input id='" + trId + "_appendable' type='checkbox' value='true'>添加&nbsp;&nbsp;";
            tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
            tr += "</tr>";
            $("#mapTable tr:last").after(tr);
        }
        else
            ; // alert("类型不匹配，无法映射！");

        sourceField.setValue('');
        destField.setValue('');
    }
    */

    function addMapNest() {
        if (sourceField==null || destField==null) {
            alert("请选择源表单及嵌套表单！");
            return;
        }
        if (sourceField.getValue()=="" || destField.getValue()=="") {
            alert("请选择源表单字段及嵌套表单字段！");
            return;
        }
        // 如果类型匹配
        if (isTypeMatched(sourceField.getValue(), destField.getValue(), sources, dests)) {
            var trId = "tr_" + sourceField.getValue() + "_" + destField.getValue();
            // 检测trId是否已存在
            var isFound = false;
            $("#mapTableNest tr").each(function(k){
                if ($(this).attr("id")==trId) {
                    isFound = true;
                    return;
                }
            });

            if (isFound) {
                alert("存在重复映射！");
                return;
            }

            var tr = "<tr id='" + trId + "' sourceField='" + sourceField.getValue() + "' destField='" + destField.getValue() + "'>";
            tr += "<td align='center'>来源</td>";
            tr += "<td align='center'>" + sourceField.getText() + "</td>";
            tr += "<td align='center'>目标</td>";
            tr += "<td align='center'>" + destField.getText() + "</td>";
            tr += "<td align='center'>";
            tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
            tr += "</tr>";
            $("#mapTableNest tr:last").after(tr);

            sourceField.setValue('');
            destField.setValue('');
        }
        else
            return; // alert("类型不匹配，无法映射！");

        sourceField.setValue('');
        destField.setValue('');
    }

    function makeMap() {
        if (destForm.getValue()=="") {
            jAlert("请选择嵌套表单！", "提示");
            return;
        }
        if (forms.getValue()=="") {
            jAlert("请选择源表单！", "提示");
            return;
        }

        <%
            if (ConstUtil.NEST_TABLE.equals(nestType)) {
        %>
        if ($('#formView').val()=='-1') {
            // jAlert("请选择视图", "提示");
            // return;
        }
        <%
        }
        %>

        if (kind=="comb") {
            o("conds").value = o("condition").value;
        }

        var str = combinationStr();
        str = encodeJSON(str);
        window.opener.setSequence(str, destForm.getText());
        //写关联模块
        var editFlag = $("#editFlag").val();
        var opFlag = "";
        if (editFlag == ""){
            opFlag = "add";
        }else{
            opFlag = "edit";
        }
        $.ajax({
            type: "post",
            async: false, // 设为同步，以免窗口关闭致调用不成功
            url: "module_field_sel_nest.jsp",
            data: {
                openerFormCode: "<%=openerFormCode%>",
                newRelateCode: "<%=destFormCode%>",
                oldRelateCode:"<%=oldRelateCode%>",
                op : opFlag
            },
            dataType: "json",
            beforeSend: function(XMLHttpRequest){
                //ShowLoading();
            },
            success: function(data, status){

            },
            complete: function(XMLHttpRequest, status){
                //HideLoading();
            },
            error: function(){
                //请求出错处理
            }
        });
        closeWindow();
    }

    function combinationStr(){
        // 组合成json字符串{maps:[{sourceField:..., destField:..., editable:true, appendable:true},...{...}]}
        var mapsNest = "";
        $("#mapTableNest tr").each(function(k){
            // 判断是否为描述映射的行
            if ($(this)[0].id!="") {
                if ($(this).attr("id").indexOf("tr_")==0) {
                    if (mapsNest=="") {
                        mapsNest = "{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\"}";
                    }
                    else {
                        mapsNest += ",{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\"}";
                    }
                }
            }
        });
        var sourceFormVal = forms.getValue();
        if (sourceFormVal == '<%=sourceFormName%>'){
            sourceFormVal = '<%=sourceFormCode%>';
        }
        var destFormVal = destForm.getValue();
        if (destFormVal == '<%=destFormName%>'){
            destFormVal = '<%=destFormCode%>';
        }
        var condVal = o("conds").value;
        condVal = condVal.replace(/"/gi, "%simq");
        // 201711-30 fgf 注释掉，因为当为脚本时，不能转为单引号
        // condVal = condVal.replace(/"/gi, "'");

        var canAdd = getCheckboxValue("canAdd");
        var canEdit = getCheckboxValue("canEdit");
        var canImport = getCheckboxValue("canImport");
        var canExport = getCheckboxValue("canExport");
        var canDel = getCheckboxValue("canDel");
        var canSel = getCheckboxValue("canSel");
        var canStr = "\"canAdd\":\"" + canAdd + "\", \"canEdit\":\"" + canEdit + "\", \"canImport\":\"" + canImport + "\", \"canDel\":\"" + canDel + "\", \"canSel\":\"" + canSel + "\", \"canExport\":\"" + canExport + "\"";

        var isAgainst = getCheckboxValue("isAgainst");
        var isAutoSel = getCheckboxValue("isAutoSel");

        var isTab = 0;
        if (o("isTab").checked) {
            isTab = 1;
        }

        // 字段合计描述字符串处理
        var calcCodesStr = "";
        var calcFuncs = $("select[name='calcFunc']");
        var map = new Map();
        var isFound = false;
        $("select[name='calcFieldCode']").each(function(i) {
            if ($(this).val()!="") {
                if (!map.containsKey($(this).val()))
                    map.put($(this).val(), $(this).val());
                else {
                    isFound = true;
                    jAlert($(this).find("option:selected").text() + "存在重复！","提示");
                    return false;
                }

                if (calcCodesStr=="")
                    calcCodesStr = "\"" + $(this).val() + "\":\"" + calcFuncs.eq(i).val() + "\"";
                else
                    calcCodesStr += "," + "\"" + $(this).val() + "\":\"" + calcFuncs.eq(i).val() + "\"";
            }
        })
        if (isFound)
            return;
        calcCodesStr = "{" + calcCodesStr + "}";

        var isPage = 0;
        if (o("isPage").checked) {
            isPage = 1;
        }
        var pageSize = o("pageSize").value;
        if (pageSize == "") {
            pageSize = 20;
        }

        var isSearchable = 0;
        if (o("isSearchable").checked) {
            isSearchable = 1;
        }

        var isAddHighlight = 0;
        if (o("isAddHighlight").checked) {
            isAddHighlight = 1;
        }

        return "{\"sourceForm\":\"" + sourceFormVal + "\", \"destForm\":\"" + destFormVal + "\", \"propStat\":" + calcCodesStr + ", \"isTab\":" + isTab + ", \"isSearchable\":" + isSearchable + ", \"isAddHighlight\":" + isAddHighlight + ", \"isPage\":" + isPage + ", \"pageSize\":" + pageSize + ", \"isAgainst\":\"" + isAgainst + "\", \"isAutoSel\":\"" + isAutoSel + "\", \"filter\":\"" + condVal + "\", \"maps\":[" + mapsNest + "], \"formViewId\":\"" + $('#formView').val() + "\", " + canStr + "}";
    }

    function combinationStr4DestFormChange(){
        // 组合成json字符串{maps:[{sourceField:..., destField:..., editable:true, appendable:true},...{...}]}
        var mapsNest = "";

        var sourceFormVal = forms.getValue();
        if (sourceFormVal == '<%=sourceFormName%>'){
            sourceFormVal = '<%=sourceFormCode%>';
        }
        var destFormVal = destForm.getValue();
        if (destFormVal == '<%=destFormName%>'){
            destFormVal = '<%=destFormCode%>';
        }
        var condVal = o("conds").value;
        condVal = condVal.replace(/"/gi, "%simq");
        // 201711-30 fgf 注释掉，因为当为脚本时，不能转为单引号
        // condVal = condVal.replace(/"/gi, "'");
        var str = "{\"sourceForm\":\"" + sourceFormVal + "\", \"destForm\":\"" + destFormVal + "\", \"filter\":\"" + condVal + "\", \"maps\":[" + mapsNest + "]}";
        return str;
    }

    // 对字符串中的引号进行编码，以免引起json解析问题
    function encodeJSON(jsonString) {
        jsonString = jsonString.replace(/\"/gi, "%dq");
        jsonString = jsonString.replace(/'/gi, "%sq");
        jsonString = jsonString.replace(/</gi, "%lt");
        jsonString = jsonString.replace(/>/gi, "%gt");

        jsonString = jsonString.replace(/\r\n/gi, "%rn");
        jsonString = jsonString.replace(/\n/gi, "%n");

        return jsonString;
    }

    function addCond() {
        if (sourceCondField==null) {
            alert("请先选择源表单！");
            return;
        }
        if (sourceCondField.getValue()=="") {
            alert("请选择源表单中的字段！");
            return;
        }
        var str = sourceCondField.getValue() + " " + o("token").value + " {$" + o("fieldOpener").value + "}";
        // 不能用#，否则request传参数的时候会不认，因为#表示锚点
        if (o("token").value=="like")
            str = sourceCondField.getValue() + " " + o("token").value + " {$@" + o("fieldOpener").value + "}";
        o("conds").value += str;
    }

    function closeWindow(){
        window.opener=null;
        window.open('', '_self', '');
        window.close();
    }

    function openWin(url,width,height) {
        var newwin=window.open(url,"fieldWin","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
        return newwin;
    }

    function openCondition(){
        openWin("",1024,568);

        var url = "module_combination_condition.jsp";
        var tempForm = document.createElement("form");
        tempForm.id="tempForm1";
        tempForm.method="post";
        tempForm.action=url;

        var hideInput = document.createElement("input");
        hideInput.type="hidden";
        hideInput.name= "condition";
        hideInput.value= o("condition").value;
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type="hidden";
        hideInput.name= "fromValue";
        hideInput.value=  "" ;
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type="hidden";
        hideInput.name= "toValue";
        hideInput.value=  ""
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type="hidden";
        hideInput.name= "moduleCode";

        var sourceCode = $("input[name=forms]").val();
        if (sourceCode=="<%=sourceFormName %>") {
            sourceCode = "<%=sourceFormCode %>";
        }

        hideInput.value=  sourceCode;
        tempForm.appendChild(hideInput);

        var destCode = $("input[name=destForm]").val();
        if (destCode=="<%=destFormName %>") {
            destCode = "<%=destFormCode %>";
        }

        hideInput = document.createElement("input");
        hideInput.type="hidden";
        hideInput.name= "mainFormCode";
        hideInput.value=  "<%=openerFormCode %>";
        tempForm.appendChild(hideInput);

        document.body.appendChild(tempForm);
        tempForm.target="fieldWin";
        tempForm.submit();
        document.body.removeChild(tempForm);
    }

    function setCondition(val) {
        o("condition").value = val;
        if (val!="") {
            $('#imgId').show();
        }
        else {
            $('#imgId').hide();
        }
    }

    function getScript() {
        return $('#conds').val();
    }

    function setScript(script) {
        $('#conds').val(script);
    }

    <%
    com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
    com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
    String version = StrUtil.getNullStr(oaCfg.get("version"));
    String spVersion = StrUtil.getNullStr(spCfg.get("version"));
    %>
    var ideUrl = "../admin/script_frame.jsp";
    var ideWin;
    var cwsToken = "";

    function openIdeWin() {
        ideWin = openWinMax(ideUrl);
    }

    var onMessage = function(e) {
        var d = e.data;
        var data = d.data;
        var type = d.type;
        if (type=="setScript") {
            setScript(data);
            if (d.cwsToken!=null) {
                cwsToken = d.cwsToken;
                ideUrl = "../admin/script_frame.jsp?cwsToken=" + cwsToken;
            }
        }
        else if (type=="getScript") {
            var data={
                "type":"openerScript",
                "version":"<%=version%>",
                "spVersion":"<%=spVersion%>",
                "scene":"nestsheet.pull",
                "data":getScript()
            }
            ideWin.leftFrame.postMessage(data, '*');
        }
        else if (type == "setCwsToken") {
            cwsToken = d.cwsToken;
            ideUrl = "../admin/script_frame.jsp?cwsToken=" + cwsToken;
        }
    }

    $(function() {
        if (window.addEventListener) { // all browsers except IE before version 9
            window.addEventListener("message", onMessage, false);
        } else {
            if (window.attachEvent) { // IE before version 9
                window.attachEvent("onmessage", onMessage);
            }
        }
    });

    $.fn.outerHTML = function () {
        return $("<p></p>").append(this.clone()).html();
    };

    function addCalcuField() {
        if (o("divCalcuField0")) {
            $("#divCalcuField").append($("#divCalcuField0").outerHTML());
        }
        else {
            initDivCalcuField();
        }
    }

    function initDivCalcuField() {
        $.ajax({
            type: "POST",
            url: "module_field_calcu_field_ajax.jsp",
            data : {
                formCode: '<%=destFormCode%>'
            },
            success: function(html) {
                $("#divCalcuField").html(html);
            },
            error: function(XMLHttpRequest, textStatus){
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText,"提示");
            }
        });
    }

    $(function() {
        $('#btnSourceMoudle').click(function() {
            var sourceFormVal = forms.getValue();
            if (sourceFormVal == '<%=sourceFormName%>'){
                sourceFormVal = '<%=sourceFormCode%>';
            }
            if (sourceFormVal == '') {
                jAlert('请选择源模块', '提示');
                return;
            }
            openWin('module_field_list.jsp?code=' + sourceFormVal, 800, 600);
        });

        $('#btnDestMoudle').click(function() {
            var destFormVal = destForm.getValue();
            if (destFormVal == '<%=destFormName%>'){
                destFormVal = '<%=destFormCode%>';
            }
            if (destFormVal == '') {
                jAlert('请选择嵌套表单', '提示');
                return;
            }
            openWin('module_field_list.jsp?code=' + destFormVal, 800, 600);
        });
    });
</script>
</html>