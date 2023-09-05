<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
    String op = ParamUtil.get(request, "op");

    WorkflowPredefineDb wpd = new WorkflowPredefineDb();
    wpd = wpd.getDefaultPredefineFlow(flowTypeCode);

    if (wpd != null) {
        op = "edit";
    }
%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>预定义流程</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="../js/flow/myflow.css">
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
    <link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
    <%@ include file="../inc/nocache.jsp" %>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <script src="../js/activebar2.js" type="text/javascript"></script>
    <script type="text/javascript" src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script type="text/javascript" src="../js/flow/lib/raphael-min.js"></script>
    <script type="text/javascript" src="../js/flow/myflow.min.js"></script>
    <script type="text/javascript" src="../js/flow/myflow.jpdl.js"></script>
    <script type="text/javascript" src="../js/flow/myflow.editors.js"></script>
    <script type="text/javascript" src="../js/crypto-js.min.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
</head>
<body>
<%
    LeafPriv lp = new LeafPriv(flowTypeCode);
    if (!(lp.canUserExamine(privilege.getUser(request)))) {
        if (lp.canUserQuery(privilege.getUser(request))) {
            response.sendRedirect("flow_list.jsp?typeCode=" + StrUtil.UrlEncode(flowTypeCode));
            return;
        } else {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
            return;
        }
    }

    String flowJson = "";
    String title = "";
    if (op.equals("edit")) {
        // mode = "user";
        flowJson = wpd.getFlowJson();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(flowTypeCode);
        if (lf != null) {
            title = lf.getName();
        }
    } else {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(flowTypeCode);
        if (lf != null) {
            title = lf.getName();
        }
    }

    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    String flowExpireUnit = cfg.get("flowExpireUnit");
    if (flowExpireUnit.equals("day")) {
        flowExpireUnit = "天";
    } else {
        flowExpireUnit = "小时";
    }
%>
<%@ include file="flow_inc_menu_top.jsp" %>
<script>
    o("menu2").className = "current";
</script>
<table width="100%" align="center" style="background-color: #fff; border-bottom: 1px solid #efefef; height: 50px" class="form-inline">
    <tr>
        <td align="center">
            <span id="infoSpan" style="color:red"></span>
            <%=title%>&nbsp;&nbsp;
            <select id="flowTemplate" name="flowTemplate" class="form-control">
                <%
                    Leaf rootlf = new Leaf();
                    rootlf = rootlf.getLeaf(Leaf.CODE_ROOT);
                    DirectoryView flowdv = new DirectoryView(rootlf);
                    flowdv.ShowDirectoryAsOptions(request, out, rootlf, rootlf.getLayer());
                %>
            </select>
            <input type="button" class="btn btn-default" value="套用" title="套用流程图" onclick="applyFlow()"/>
            <%
                if (lp.canUserExamine(privilege.getUser(request))) {
            %>
            &nbsp;&nbsp;<input type="button" class="btn btn-default" onclick="openWin('flow_designer_myflow.jsp?flowTypeCode=<%=flowTypeCode%>', screen.width, screen.height)" value="设计"/>
            <%}%>
        </td>
    </tr>
</table>

<div id="myflow">

</div>

<textarea id="flowData" style="display: none;"><%=flowJson%></textarea>
<script src="../js/layui/layui.js" charset="utf-8"></script>
</body>
<script>
    var layer;
    layui.use('layer', function(){
        layer = layui.layer;
    });

    function applyFlow() {
        if (o("flowTemplate").value == "not") {
            layer.open({
                type: 1
                ,offset: 'auto' //具体配置参考：http://www.layui.com/doc/modules/layer.html#offset
                ,id: 'dlg' //防止重复弹出
                ,content: '<div style="padding: 20px 100px;">请选择流程</div>'
                ,btn: '确定'
                ,btnAlign: 'c' //按钮居中
                ,shade: 0 //不显示遮罩
            });
            return;
        }

        layer.open({
            type: 1
            ,offset: 'auto' //具体配置参考：http://www.layui.com/doc/modules/layer.html#offset
            ,id: 'dlg' //防止重复弹出
            ,content: '<div style="padding: 20px 50px;">套用流程时同时附带事件脚本么</div>'
            ,btn: ['是', '否', '取消']
            ,btnAlign: 'c' //按钮居中
            ,shade: 0 //不显示遮罩
            ,yes: function(index, layero){
                //按钮【按钮一】的回调
                postApplyFlow(true);
                layer.close(index);
            }
            ,btn2: function(index, layero){
                //按钮【按钮二】的回调
                postApplyFlow(false);
                // return false; // 开启该代码可禁止点击该按钮关闭
            }
            ,btn3: function(index, layero){
                //按钮【按钮三】的回调
                // layer.close(index);
            }
            ,cancel: function(){
                //右上角关闭回调
                //return false 开启该代码可禁止点击该按钮关闭
            }
        });
    }

    function postApplyFlow(isWithScript) {
        $(".tabStyle_1_title").parent().parent().parent().hide();

        $.ajax({
            type: "post",
            url: "applyFlow.do",
            contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
            data: {
                flowTypeCode: "<%=flowTypeCode%>",
                isWithScript: isWithScript,
                templateCode: o("flowTemplate").value
            },
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == "1") {
                    layer.open({
                        content: '<div style="padding: 20px 100px;">' + data.msg + '</div>'
                        ,btn: '确定'
                        ,yes: function(index, layero){
                            window.location.reload();
                        }
                    });
                } else {
                    $.toaster({priority : 'info', message : data.msg });
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                alert(XMLHttpRequest.responseText);
            }
        });
    }

    $(function () {
        var flowData = o("flowData").value;
        // eval这种方式转json不安全，eval会执行json串中的表达式，反斜杠会丢失，所以要先将\转为\\。
        flowData = flowData.replaceAll("\\\\", "\\\\");
        // 因为flowData书写不规范，键上面没有带引号，所以JSON.parse及$.parseJSON解析时都会报错，但是java的org.json.JSONObject则不会
        if (flowData == "") {
            flowData = "{}";
        }
        flowData = eval("(" + flowData + ")");
        // console.log(JSON.stringify(flowData));

        $('#myflow').myflow({
            allowStateMultiLine: false,
            editable: false,
            restore: flowData,
            expireUnit: "<%=flowExpireUnit%>",
            licenseKey: "<%=License.getInstance().getKey()%>",
            activeRects: {},
            finishRects: {},
            rootPath: "<%=request.getContextPath()%>",
            tools: {}
        });
    });
</script>
</html>