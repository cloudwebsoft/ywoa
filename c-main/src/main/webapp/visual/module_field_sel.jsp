<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb"%>
<%@ page import="org.json.*"%>
<%@ page import="java.util.regex.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.domain.NestFieldMaping"%>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.api.IModuleFieldSelectCtl" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%
    /*
     * 用于当表单设计时，选择表单域选择器宏控件，弹出本窗口
     */
    String openerFormCode = ParamUtil.get(request, "openerFormCode");
    if ("".equals(openerFormCode)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "请先创建表单，然后编辑表单时插入此控件！"));
        return;
    }
    try {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "openerFormCode", openerFormCode, getClass().getName());
    }
    catch (ErrMsgException e) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
        return;
    }
    String params = ParamUtil.get(request, "params");
    JSONObject jsonObject = null;
    String formCode = "";
    String otherField = "";
    String otherShowField = "";
    String moduleCode = "";
    String filter = "";
    int mode = 1;
    boolean isModuleExist = true;
    boolean canManualInput = false;
    boolean canOpenWinSel = false;
    boolean isMulti = false;
    boolean isSimilar = false;
    boolean isAjax = false;
    boolean isRealTime = false;
    JSONArray nestMaps = null;
    String requestParam = "";
    List<NestFieldMaping> nestList = null;
    if (params != null && !"".equals(params)) {
        // 从macros.jsp中传参过来的时候，"和'已被解码，此处需重新再编码，否则会导致json解析出错
        String patternStr = "", replacementStr = "";
        Pattern pattern;
        Matcher matcher;
        patternStr = "\""; // 双引号
        replacementStr = "%dq"; // double quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(params);
        params = matcher.replaceAll(replacementStr);

        patternStr = "'"; // 单引号
        replacementStr = "%sq"; // single quote
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(params);
        params = matcher.replaceAll(replacementStr);

        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
        IModuleFieldSelectCtl moduleFieldSelectCtl = macroCtlService.getModuleFieldSelectCtl();
        params = moduleFieldSelectCtl.formatJSONString(params);
        //System.out.println(getClass() + " params:" + params);
        try {
            jsonObject = new JSONObject(params);
            formCode = jsonObject.getString("formCode");
            otherField = jsonObject.getString("idField");
            otherShowField = jsonObject.getString("showField");
            moduleCode = jsonObject.getString("sourceFormCode");
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            if (msd!=null) {
                if (jsonObject.has("mode")) {
                    mode = jsonObject.getInt("mode");
                }

                if (jsonObject.has("canManualInput")) {
                    canManualInput = jsonObject.getBoolean("canManualInput");
                }

                if (jsonObject.has("isSimilar")) {
                    isSimilar = jsonObject.getBoolean("isSimilar");
                }

                if (jsonObject.has("isMulti")) {
                    isMulti = jsonObject.getBoolean("isMulti");
                }

                if (jsonObject.has("canOpenWinSel")) {
                    canOpenWinSel = jsonObject.getBoolean("canOpenWinSel");
                }

                if (jsonObject.has("isAjax")) {
                    isAjax = jsonObject.getBoolean("isAjax");
                }

                if (jsonObject.has("requestParam")) {
                    requestParam = jsonObject.getString("requestParam");
                }

                if (jsonObject.has("isAjax")) {
                    isAjax = jsonObject.getBoolean("isAjax");
                }

                if (jsonObject.has("isRealTime")) {
                    isRealTime = jsonObject.getBoolean("isRealTime");
                }

                filter = jsonObject.getString("filter");
                if ("none".equals(filter)) {
                    filter = "";
                }

                filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(filter);
                //System.out.println(getClass() + " filter:" + filter);

                nestList = new ArrayList<NestFieldMaping>();
                nestMaps = jsonObject.getJSONArray("maps");
                FormDb sourceFd = new FormDb(msd.getString("form_code"));
                FormDb openerFd = new FormDb(formCode);
                for(int i = 0 ; i < nestMaps.length(); i++){
                    JSONObject temp = new JSONObject();
                    temp = (JSONObject)nestMaps.get(i);
                    NestFieldMaping nfm = new NestFieldMaping();
                    nfm.setSourceFieldCode(temp.getString("sourceField"));
                    if ("id".equals(temp.getString("sourceField"))) {
                        nfm.setSourceFieldName("ID");
                    }
                    else if ("cws_id".equals(temp.getString("sourceField"))) {
                        nfm.setSourceFieldName("cws_id");
                    }
                    else {
                        nfm.setSourceFieldName(sourceFd.getFieldTitle(temp.getString("sourceField")));
                    }
                    nfm.setDestFieldCode(temp.getString("destField"));
                    nfm.setDestFieldName(openerFd.getFieldTitle(temp.getString("destField")));
                    nestList.add(nfm);
                }
            }
            else {
                isModuleExist = false;
            }
        }catch(JSONException e){
            // out.print("参数格式错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>模块表单域选择器</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
    <script src="../inc/common.js"></script>
    <script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css" />
    <script src="<%=request.getContextPath()%>/js/bootstrap/js/bootstrap.min.js"></script>

    <script src="../js/select2/select2.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet" />
    <script>
        var errFunc = function(response) {
            window.status = response.responseText;
        }

        var formCodeSel;
        function doGetField(response) {
            var rsp = response.responseText.trim();
            spanField.innerHTML = rsp;
            if ('<%=otherField%>' != null && '<%=otherField%>' != ""){
                $("#otherField").val('<%=otherField%>');
            }
            if ('<%=otherShowField%>' != null && '<%=otherShowField%>' != ""){
                $("#otherShowField").val('<%=otherShowField%>');
            }

            getFieldOptions(formCodeSel);
        }

        function getFieldOptions(formCode) {
            var str = "op=getOptions&code=" + formCode;
            var myAjax = new cwAjax.Request(
                "module_field_ajax.jsp",
                {
                    method:"post",
                    parameters:str,
                    onComplete:doGetFieldOptions,
                    onError:errFunc
                }
            );
        }

        function doGetFieldOptions(response) {
            var rsp = response.responseText.trim();
            // alert(rsp);
            $("#fieldRelated").empty();

            rsp = "<option value=''></option>" + rsp;

            rsp += "<option value='cws_id'>cws_id(关联主模块ID)</option>";
            rsp += "<option value='id'>ID(记录ID)</option>";

            $("#fieldRelated").append(rsp);
            $('#fieldRelated').select2();

            $("#sourceField").empty();
            $("#sourceField").append(rsp);
            $('#sourceField').select2();
        }

        var sources = [];

        function getFieldOfForm(theFormCode) {
            formCodeSel = theFormCode;
            var str = "code=" + formCodeSel;
            var myAjax = new cwAjax.Request(
                "module_field_ajax.jsp",
                {
                    method:"post",
                    parameters:str,
                    onComplete:doGetField,
                    onError:errFunc
                }
            );

            $.getJSON('../flowform_data_map_ajax.jsp', {"sourceFormCode":formCodeSel}, function(data) {
                sources = data.result;
            });
        }

        function window_onload() {
            getFieldOfForm(o("formCode").value);
        }
        function addMapForEdit(sourceFieldVal,sourceFieldName,destFieldVal,destFieldName) {
            var trId = "tr_" + sourceFieldVal + "_" + destFieldVal;

            var tr = "<tr id='" + trId + "' sourceField='" + sourceFieldVal + "' destField='" + destFieldVal + "'>";
            tr += "<td>字段</td>";
            tr += "<td>" + sourceFieldName + "</td>";
            tr += "<td>" + destFieldName + "</td>";
            tr += "<td>";
            tr += "<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
            tr += "</tr>";
            $("#mapTable tr:last").after(tr);
        }
    </script>
</head>
<body onload="window_onload()">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    // 当从模式对话框打开本窗口时，因为分属于不同的IE进程，SESSION会丢失，可以用cookie中置sessionId来解决这个问题
    String priv="read";
    if (!privilege.isUserPrivValid(request,priv)) {
        // out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        // return;
    }

    if (!isModuleExist) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块" + moduleCode + "不存在"));
    }

    boolean isComb = filter.startsWith("<items>") || filter.equals("");
    String cssComb = "", cssScript = "";
    String kind;
    if (isComb) {
        cssComb = "in active";
        kind = "comb";
    }
    else {
        cssScript = "in active";
        kind = "script";
    }
%>
<table class="tabStyle_1" id="mapTable" style="padding:0px; margin:0px;" width="100%" cellpadding="0" cellspacing="0">
    <tbody>
    <tr>
        <td height="28" colspan="4" class="tabStyle_1_title">&nbsp;请选择</td>
    </tr>
    <tr>
        <td height="22" colspan="4" align="left">&nbsp;记录模块
            <select name="formCode" onchange="getFieldOfForm(this.value)" id="formCode">
                <%
                    ModuleSetupDb msd = new ModuleSetupDb();
                    Vector v = msd.listUsed();
                    Iterator ir = v.iterator();
                    String jsonStr = "";
                    while (ir.hasNext()) {
                        msd = (ModuleSetupDb)ir.next();

                        if (jsonStr.equals("")) {
                            jsonStr = "{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";
                        } else {
                            jsonStr += ",{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";
                        }
                %>
                <option value="<%=msd.getString("code")%>"><%=msd.getString("name")%></option>
                <%
                    }
                %>
            </select>
            (<a id="btnSourceModule" href="javascript:" title="维护模块">维护</a>)
            <!--中的字段-->
            <span id="spanField"></span>
        </td>
        <script>
            var sourceFormCode = '<%=moduleCode%>';
            if (sourceFormCode != null && sourceFormCode != ""){
                $("#formCode").val(sourceFormCode);
            }
            $('#formCode').select2({width:300});
        </script>
    </tr>
    <tr>
        <td height="22" colspan="4" align="left">
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
                        <img src="../admin/images/gou.png" style="margin-bottom:-5px;width:20px;height:20px;display:<%=(isComb && !filter.equals(""))?"":"none" %>;" id="imgId"/>
                        <textarea id="condition" name="condition" style="display:none" cols="80" rows="5"><%=filter %></textarea>
                        &nbsp;&nbsp;（如果配置了条件，则根据配置条件过滤，如果未配置条件，则根据模块的条件过滤）
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
                        <a href="javascript:;" onclick="o('conds').value += ' in ({$admin.dept})';" title="用户可以管理的部门">当前用户管理的部门</a>&nbsp;&nbsp;(注：条件不能以and开头)
                        <input type="button" value="设计器" class="btn btn-default" onclick="openIdeWin()" />
                        <textarea id='condHelper' style="display:none"><%=filter%></textarea>
                        <script>
                            //设置原设置条件
                            var cond = $('#condHelper').val();
                            if (cond != ""){
                                $("#conds").val(cond);
                            }
                        </script>
                    </div>
                    <div style="float:left; padding-top:4px">源表单中的</div>
                    <div id="sourceCondFieldDiv" style="float:left">
                        <select id="fieldRelated" name="fieldRelated">

                        </select>
                    </div>
                    <div style="float:left">
                        &nbsp;
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
                            <!--<option value="parentId">主模块ID(用于从模块表单)</option>-->
                        </select>

                        <input type="button" class="btn btn-default" value="添加" onclick="addCond()" />
                    </div>
                </div>
            </div>
            <script>
                var kind = "<%=kind%>";

                $(function(){
                    $('#fieldOpener').select2();
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

    </tr>
    <tr>
        <td height="22" colspan="4" align="left">
            模式：
            <input id="modeWin" name="mode" value="1" type="radio" <%=mode==1?"checked":""%> />
            窗口选择&nbsp;&nbsp;
            <input id="modeSelect" name="mode" value="0" type="radio" <%=mode==0?"checked":""%> />
            下拉选择&nbsp;&nbsp;
            <span id="spanModeDropdown" style="display:none">
      	<input title="可以选择多个记录" id="isMulti" name="isMulti" type="checkbox" value="1"/>
      	支持多选
      	&nbsp;&nbsp;
     	<input title="可以输入新记录" id="canManualInput" name="canManualInput" type="checkbox" value="1"/>
     	手工输入
     	<span id="spanSimilar">
     	&nbsp;&nbsp;
     	<input title="通过全文检索查询出相似度最高的结果" id="isSimilar" name="isSimilar" type="checkbox" value="1"/>
     	按相似度查询
     	</span>
        &nbsp;&nbsp;
        <input title="显示弹窗选择按钮" id="canOpenWinSel" name="canOpenWinSel" type="checkbox" />
        弹窗选择
        &nbsp;&nbsp;
        <input title="ajax远程获取下拉框的选项，如勾选则点击下拉箭头时无下拉列表，反之则有下拉列表，最大显示<%=ConstUtil.MODULE_FIELD_SELECT_CTL_MAX_COUNT%>行" id="isAjax" name="isAjax" type="checkbox" />
        远程获取
      </span>
            &nbsp;&nbsp;&nbsp;&nbsp;
            request请求参数名称：<input id="requestParam" name="requestParam" title="增加记录时从request请求中接收的参数名称" value="<%=requestParam%>" />
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input id="isRealTime" value="true" type="checkbox"/> 实时映射
            <script>
                $(function() {
                    var mode = $('input:radio[name=mode]:checked').val();
                    if (mode=="0") {
                        $('#spanModeDropdown').show();
                    }
                    else {
                        $('#spanModeDropdown').hide();
                    }

                    if (<%=canManualInput%>) {
                        $('#canManualInput').attr("checked", true);
                    }

                    if (<%=isMulti%>) {
                        $('#isMulti').attr("checked", true);
                    }

                    if (<%=isSimilar%>) {
                        $('#isSimilar').attr("checked", true);
                    }

                    if (<%=canOpenWinSel%>) {
                        $('#canOpenWinSel').attr("checked", true);
                    }

                    if (<%=isAjax%>) {
                        $('#isAjax').attr("checked", true);
                    }

                    if (<%=isRealTime%>) {
                        $('#isRealTime').attr("checked", true);
                    }

                    $("input[name='mode']").click(function() {
                        if ($(this).val()=="0") {
                            $('#spanModeDropdown').show();
                        }
                        else {
                            $('#spanModeDropdown').hide();
                        }
                    });

                    <%
                    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                    if (!cfg.getBooleanProperty("isSegmentEnabled")) {
                    %>
                    $('#spanSimilar').hide();
                    <%}%>
                });
            </script>
        </td>
    </tr>
    <tr>
        <td height="22" colspan="4" align="left" class="tabStyle_1_title">映射表单域</td>
    </tr>
    <tr>
        <td width="6%" height="22" align="left">映射</td>
        <td width="19%" align="left"><select id="sourceField" name="sourceField">
        </select></td>
        <td width="18%" align="left">
            <select id="destField" name="destField" style="width: 150px">
                <option value=""></option>
                <%
                    ir = fdOpener.getFields().iterator();
                    String json = "";
                    while (ir.hasNext()) {
                        FormField ff = (FormField)ir.next();
                %>
                <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
                <%
                        if (json.equals("")) {
                            json = "{'id':'" + ff.getName() + "', 'name':'" + StrUtil.HtmlEncode(ff.getTitle()) + "', 'type':'" + ff.getType() + "', 'macroType':'" + ff.getMacroType() + "', 'defaultValue':'" + StrUtil.escape(ff.getDefaultValue()) + "'}";
                        } else {
                            json += ",{'id':'" + ff.getName() + "', 'name':'" + StrUtil.HtmlEncode(ff.getTitle()) + "', 'type':'" + ff.getType() + "', 'macroType':'" + ff.getMacroType() + "', 'defaultValue':'" + StrUtil.escape(ff.getDefaultValue()) + "'}";
                        }
                    }
                %>
            </select>
            <script>
                $('#destField').select2();
            </script>
        </td>
        <td width="57%" height="22" align="left">
            <input type="button" class="btn btn-default" value="添加" onclick="addMap()" />
            <%
                if(nestList != null && nestList.size() > 0){
                    for (NestFieldMaping nf : nestList){
            %>
            <script>
                addMapForEdit('<%=nf.getSourceFieldCode()%>','<%=nf.getSourceFieldName()%>','<%=nf.getDestFieldCode()%>','<%=nf.getDestFieldName()%>')
            </script>
            <%
                    }
                }
            %>
        </td>
    </tr>
    </tbody>
</table>
<div style="text-align: center; margin-top: 10px">
    <input class="btn btn-default" type="button" value="确定" onclick="doSel()" />
</div>
</body>
<script language="javascript">
    <!--
    var dests = [<%=json%>];

    function addMap() {
        if ($('#sourceField').val()=="" || $('#destField').val()=="") {
            alert("请选择表单域！");
            return;
        }

        // 如果类型匹配
        if (isTypeMatched($('#sourceField').val(), $('#destField').val(), sources, dests)) {
            var trId = "tr_" + $('#sourceField').val() + "_" + $('#destField').val();
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

            var isNestTable = false;
            for (var one in sources) {
                if (sources[one].id==$('#sourceField').val()) {
                    if (sources[one].macroType=="nest_table") {
                        isNestTable = true;
                        break;
                    }
                }
            }

            var tr = "<tr id='" + trId + "' sourceField='" + $('#sourceField').val() + "' destField='" + $('#destField').val() + "'>";
            tr += "<td>字段</td>";
            tr += "<td>" + $('#sourceField option:selected').text() + "</td>";
            tr += "<td>" + $('#destField option:selected').text() + "</td>";
            tr += "<td>";
            tr += "<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
            tr += "</tr>";
            $("#mapTable tr:last").after(tr);

            // 设置select2初始值，单选、多选均可用
            $("#sourceField").val(['']).trigger('change');
            $("#destField").val(['']).trigger('change');
        }
        else
            ; // alert("类型不匹配，无法映射！");

    }

    // 检查类型是否匹配
    function isTypeMatched(sourceValue, destValue, sources, dests) {
        for (var one in sources) {
            if (sources[one].id==sourceValue) {
                for (var key in dests) {
                    if (dests[key].id==destValue) {
                        // alert(dests[key].type + " " + sources[one].type);
                        var isCheck = false;
                        // 检查日期型及宏控件
                        if (dests[key].type=="DATE" || dests[key].type=="DATE_TIME" || sources[one].type=="DATE" || sources[one].type=="DATE_TIME")
                            isCheck = true;
                        if (dests[key].type=="<%=FormField.TYPE_MACRO%>" || sources[one].type=="<%=FormField.TYPE_MACRO%>")
                            isCheck = true;
                        if (isCheck) {
                            if (dests[key].type=="<%=FormField.TYPE_MACRO%>" || sources[one].type=="<%=FormField.TYPE_MACRO%>") {
                                if (dests[key].macroType=="nest_table" && sources[one].macroType=="nest_table") {
                                    if (dests[key].defaultValue==sources[one].defaultValue) {
                                        // alert(true);
                                        return true;
                                    }
                                }
                                if (dests[key].macroType!=sources[one].macroType) {
                                    alert("宏控件 " + dests[key].name + " 与 " + sources[one].name + "的类型不一致");
                                    return false;
                                }
                            }
                            else {
                                if (dests[key].type==sources[one].type) {
                                    // alert(true);
                                    return true;
                                }
                                else {
                                    alert("字段 " + dests[key].name + " 与 " + sources[one].name + "的类型不一致");
                                    return false;
                                }
                            }
                        }
                        else
                            break;
                    }
                }
                break;
            }
        }
        return true;
    }

    function makeMap() {
        // 组合成json字符串{sourceForm:..., destForm:..., maps:[{sourceField:..., destField:..., editable:true, appendable:true},...{...}]}
        var maps = "";
        $("#mapTable tr").each(function(k){
            // 判断是否为描述映射的行
            if ($(this)[0].id!="") {
                if ($(this).attr("id").indexOf("tr_")==0) {
                    if (maps=="") {
                        maps = "{'sourceField': '" + $(this).attr('sourceField') + "', 'destField':" + $(this).attr('destField') + "'}";
                    }
                    else {
                        maps += ",{'sourceField': '" + $(this).attr('sourceField') + "', 'destField':'" + $(this).attr('destField') + "'}";
                    }
                }
            }
        });

        // maps = "{sourceForm:" + forms.getValue() + ", maps:[" + maps + "]}";
        return maps;
    }

    // 对字符串中的引号进行编码，以免引起json解析问题
    function encodeJSON(jsonString) {
        jsonString = jsonString.replace(/=/gi, "%eq");
        jsonString = jsonString.replace(/\{/gi, "%lb");
        jsonString = jsonString.replace(/\}/gi, "%rb");

        jsonString = jsonString.replace(/,/gi, "%co"); // 逗号
        jsonString = jsonString.replace(/\"/gi, "%dq");
        jsonString = jsonString.replace(/'/gi, "%sq");

        jsonString = jsonString.replace(/\r\n/gi, "%rn");
        jsonString = jsonString.replace(/\n/gi, "%n");
        return jsonString;
    }

    function doSel() {
        // window.opener.setSequence(o("formCode").value + ":" + o("otherField").value + ":" + o("otherShowField").value + ":isMine=" + isMine + "|conds=" + o("conds").value, formCode.options[formCode.selectedIndex].text + "_" + otherShowField.options[otherShowField.selectedIndex].text);

        // {formCode:, idField:, showField:, filter:, maps:[{sourceField:, destField:},{sourceField:, destField:}], isParentSaveAndReload:, isMine:}
        var maps = makeMap();
        var conds = o("conds").value;
        if (conds==="") {
            conds = "none";
        }
        if (kind==="comb") {
            conds = o("condition").value;
        }
        // console.log("conds=" + encodeJSON(conds));
        var canManualInput = $('#canManualInput').is(':checked');
        var isMulti = $('#isMulti').is(':checked');
        var isSimilar = $('#isSimilar').is(':checked');
        var canOpenWinSel = $('#canOpenWinSel').is(':checked');
        var isAjax = $('#isAjax').is(':checked');
        var isRealTime = $('#isRealTime').is(':checked');
        var json = "{'formCode':'<%=openerFormCode%>', 'mode':" + getRadioValue("mode") + ", 'requestParam':" + o("requestParam").value + ", 'isMulti':" + isMulti + ", 'canManualInput':" + canManualInput + ", 'isSimilar':" + isSimilar + ", 'canOpenWinSel':'" + canOpenWinSel + "', 'isAjax':'" + isAjax + "', 'sourceFormCode':'" + o("formCode").value + "', 'idField':" + o("otherField").value + "', 'showField':'" + o("otherShowField").value + "', 'filter':'" + encodeJSON(conds) + "', 'isParentSaveAndReload':'true', 'isRealTime':" + isRealTime + ", 'maps':[" + maps + "]}";
        // 不能带有单或双引号，会使得赋值后，IE源码混乱，出现?号
        json = json.replace(/'/gi, "");
        window.opener.setSequence(json, formCode.options[formCode.selectedIndex].text + "_" + otherShowField.options[otherShowField.selectedIndex].text);
        closeWindow();
    }

    function addCond() {
        var str = o("fieldRelated").value + " " + o("token").value + " {$" + o("fieldOpener").value + "}";
        // 注意不能含有单引号'，否则会导致生成的创建字段的SQL通不过
        // 也不能用#，否则request传参数的时候会不认，因为#表示锚点
        if (o("token").value=="like")
            str = o("fieldRelated").value + " " + o("token").value + " {$@" + o("fieldOpener").value + "}";
        if (o("conds").value=="") {
            o("conds").value += str;
        }
        else {
            o("conds").value += " and " + str;
        }
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
        hideInput.type ="hidden";
        hideInput.name = "moduleCode";
        hideInput.value = $("#formCode").val();
        tempForm.appendChild(hideInput);

        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = "mainFormCode";
        hideInput.value =  "<%=openerFormCode %>";
        tempForm.appendChild(hideInput);

        document.body.appendChild(tempForm);
        tempForm.target="fieldWin";
        tempForm.submit();
        document.body.removeChild(tempForm);
    }

    function setCondition(val) {
        o("condition").value = val;
        if (val != '') {
            o('imgId').style.display = "";
        }
        else {
            o('imgId').style.display = "none";
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
                "scene":"module.moduleFieldSelect",
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

        $('#btnSourceModule').click(function() {
            var val = $("#formCode").val();
            if (val == '') {
                jAlert('请选择模块', '提示');
                return;
            }
            openWin('module_field_list.jsp?code=' + val, 800, 600);
        });
    });
    //-->
</script>
</html>