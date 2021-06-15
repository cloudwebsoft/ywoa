<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.alibaba.fastjson.JSONArray" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.cloudweb.oa.api.IMyflowUtil" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.Config" %>
<%@ page import="com.redmoon.oa.kernel.License" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    int flow_id = ParamUtil.getInt(request, "flowId");
    WorkflowMgr wfm = new WorkflowMgr();
    WorkflowDb wf = wfm.getWorkflowDb(flow_id);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>修改流程</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
    <script src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>
    <script src="js/jquery.toaster.js"></script>
    <link rel="stylesheet" type="text/css" href="js/flow/myflow.css">
    <link type="text/css" href="js/flow/lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" rel="stylesheet"/>
    <script type="text/javascript" src="js/flow/lib/raphael-min.js"></script>
    <script type="text/javascript" src="js/flow/lib/jquery-ui-1.8.4.custom/js/jquery-ui.min.js"></script>
    <script type="text/javascript" src="js/crypto-js.min.js"></script>
    <script type="text/javascript" src="js/flow/myflow.min.js"></script>
    <script type="text/javascript" src="js/flow/myflow.jpdl.js"></script>
    <script type="text/javascript" src="js/flow/myflow.editors.js"></script>
    <script>
        function hideDesigner() {
            $("#designerDiv").hide();
        }
    </script>
</head>
<body onunload="hideDesigner()">
<%
    int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
    if (isShowNav==1) {
%>
<%@ include file="flow_modify_inc_menu_top.jsp" %>
<script>
    o("menu2").className = "current";
</script>
<div class="spacerH"></div>
<%
    }

    com.redmoon.oa.Config cfg = Config.getInstance();
    String flowExpireUnit = cfg.get("flowExpireUnit");
    boolean isHour = !flowExpireUnit.equals("day");
    if (flowExpireUnit.equals("day")) {
        String str = LocalUtil.LoadString(request, "res.flow.Flow", "day");
        flowExpireUnit = str;
    } else {
        String str = LocalUtil.LoadString(request, "res.flow.Flow", "hour");
        flowExpireUnit = str;
    }

    boolean canUserSeeFlowChart = cfg.getBooleanProperty("canUserSeeFlowChart");
    boolean canUserSeeFlowImage = cfg.getBooleanProperty("canUserSeeFlowImage");
    boolean canUserSeeDesignerWhenDispose = cfg.getBooleanProperty("canUserSeeDesignerWhenDispose");

    if (canUserSeeFlowChart) {
        String flowJson;
        if (StringUtils.isEmpty(wf.getFlowJson())) {
            IMyflowUtil myflowUtil = SpringUtil.getBean(IMyflowUtil.class);
            flowJson = myflowUtil.toMyflow(wf.getFlowString());
        } else {
            flowJson = wf.getFlowJson();
        }
%>
    <div style="text-align: center; margin: 10px">
        <input class="btn" name="btnPlay" type="button" reserve="true" onclick="PlayDesigner()" value='<lt:Label res="res.flow.Flow" key="playbackProcess"/>'/>
    </div>
    <div id="myflow"></div>
    <textarea id="flowJson" style="display:none;"><%=flowJson%></textarea>
<%
    }
    else {
%>
<table align="center" class="tabStyle_1 percent80">
    <tr>
        <td class="tabStyle_1_title"><%=wf.getTitle()%>&nbsp;<lt:Label res="res.flow.Flow" key="flowChart"/></td>
    </tr>
    <tr>
        <td style="background-color: #fff">
            <div id="designerDiv">
                <%
                    if (canUserSeeFlowImage) {
                %>
                <div id="Designer" class="flow-image-box" style="width:0px; height:0px; overflow-x:scroll; overflow-y: scroll;">
                    <img id="flowImage" src="<%=wf.getImgVisualPath()%>/<%=wf.getId()%>.jpg" style="width:2593px;height:2161px"/>
                </div>
                <script>
                    function detectZoom() {
                        var ratio = 1,
                            screen = window.screen;
                        var os = getOS();
                        if (os == 1) { // ie
                            if (window.devicePixelRatio) {
                                ratio = window.devicePixelRatio;
                            } else if (screen.deviceXDPI && screen.logicalXDPI) {
                                ratio = screen.deviceXDPI / screen.logicalXDPI;
                            }
                        } else if (os == 3) { // chrome
                            ratio = window.top.outerWidth / window.top.innerWidth;
                        } else if (window.outerWidth !== undefined && window.innerWidth !== undefined) { // firefox、opera
                            if (window.devicePixelRatio) {
                                ratio = window.devicePixelRatio;
                            } else {
                                ratio = window.outerWidth / window.innerWidth;
                            }
                        }
                        ratio = Math.round(ratio * 100) / 100;
                        return ratio;
                    }

                    $(function () {
                        var radio = detectZoom();
                        if (radio != 1) {
                            $('#flowImage').width($('#flowImage').width() / radio);
                            $('#flowImage').height($('#flowImage').height() / radio);
                            $('#Designer').css({"width": "1200px", "height": "515px"});
                        }
                    })
                </script>
                <%
                } else {
                %>
                <object id="Designer" classid="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" codebase="activex/cloudym.CAB#version=1,3,0,0" style="width:100%; height:515px">
                    <param name="Workflow" value="<%=wf.getFlowString()%>"/>
                    <param name="Mode" value="view"/>
                    <!--debug user initiate complete-->
                    <param name="CurrentUser" value="<%=privilege.getUser(request)%>"/>
                    <param name="ExpireUnit" value="<%=flowExpireUnit%>"/>
                    <%
                        com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();
                    %>
                    <param name="Organization" value="<%=license.getCompany()%>"/>
                    <param name="Key" value="<%=license.getKey()%>"/>
                    <param name="Company" value="<%=license.getName()%>"/>
                    <param name="LicenseType" value="<%=license.getType()%>"/>
                </object>
                <%
                    }
                %>
            </div>
        </td>
    </tr>
    <%
        if (canUserSeeDesignerWhenDispose) {
    %>
    <tr>
        <td align="center">
            <input class="btn" name="btnPlay" type="button" reserve="true" onclick="PlayDesigner()" value='<lt:Label res="res.flow.Flow" key="playbackProcess"/>'/>
        </td>
    </tr>
    <%
        }
    %>
</table>
<%
    }
%>
</body>
<script>
    <%
        if (canUserSeeFlowChart) {
            // 取出激活节点和已办节点
            JSONArray activeActions = new JSONArray();
            JSONArray finishActions = new JSONArray();
            JSONArray ignoreActions = new JSONArray();
            JSONArray discardActions = new JSONArray();
            JSONArray returnActions = new JSONArray();

            Iterator ir = wf.getActions().iterator();
            while (ir.hasNext()) {
                WorkflowActionDb wa = (WorkflowActionDb)ir.next();
                if (wa.getStatus()==WorkflowActionDb.STATE_DOING) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    activeActions.add(json);
                }
                else if (wa.getStatus()==WorkflowActionDb.STATE_FINISHED) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    finishActions.add(json);
                }
                else if (wa.getStatus()==WorkflowActionDb.STATE_IGNORED) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    ignoreActions.add(json);
                }
                else if (wa.getStatus()==WorkflowActionDb.STATE_DISCARDED) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    discardActions.add(json);
                }
                else if (wa.getStatus()==WorkflowActionDb.STATE_RETURN) {
                    JSONObject json = new JSONObject();
                    json.put("ID", wa.getInternalName());
                    returnActions.add(json);
                }
            }

            String cloudUrl = cfg.get("cloudUrl");
    %>
            var $flow;
            $(function () {
                var flowData = $('#flowJson').val();
                // console.log(flowData);
                $flow = $('#myflow').myflow({
                    allowStateMultiLine: false,
                    editable: false,
                    expireUnit: "<%=flowExpireUnit%>",
                    licenseKey: "<%=License.getInstance().getKey()%>",
                    cloudUrl: "<%=cloudUrl%>",
                    restore: eval("(" + flowData + ")"),
                    activeRects: {"rects": <%=activeActions.toString()%>},
                    finishRects: {"rects": <%=finishActions.toString()%>},
                    ignoreRects: {"rects": <%=ignoreActions.toString()%>},
                    discardRects: {"rects": <%=discardActions.toString()%>},
                    returnRects: {"rects": <%=returnActions.toString()%>}
                });
            });

            function PlayDesigner() {
                $flow.resetAllRectStatus();
                doPlayDesigner();
            }

            var playCount = 0;
            function doPlayDesigner() {
                var ary = new Array();
                <%
                    MyActionDb mad = new MyActionDb();
                    Vector v = mad.getMyActionDbOfFlow(flow_id);

                    ir = v.iterator();
                    int kk = 0;
                    while (ir.hasNext()) {
                        mad = (MyActionDb)ir.next();
                        WorkflowActionDb wa = new WorkflowActionDb();
                        wa = wa.getWorkflowActionDb((int)mad.getActionId());
                        %>
                        ary[<%=kk%>] = [<%=mad.getReceiveDate().getTime()%>, "<%=wa.getInternalName()%>", "<%=mad.getActionStatus()%>"]; // 到达
                        <%
                        kk++;
                        %>
                        ary[<%=kk%>] = [<%=mad.getCheckDate()!=null?mad.getCheckDate().getTime()+"":"999999999999999"%>, "<%=wa.getInternalName()%>", "<%=mad.isChecked()?WorkflowActionDb.STATE_FINISHED:mad.getActionStatus()%>"]; // 处理
                        <%
                        kk++;
                    }
                %>
                if (playCount == 0) {
                    // 对ary中的元素按照时间排序
                    ary.sort(function (a, b) {
                        return parseInt(a[0]) - parseInt(b[0]);
                    })
                }

                var rectId = ary[playCount][1];
                var status = ary[playCount][2];
                if (status==<%=WorkflowActionDb.STATE_DOING%>) {
                    status = 'active';
                }
                else if (status==<%=WorkflowActionDb.STATE_FINISHED%>) {
                    status = 'finish';
                }
                else if (status==<%=WorkflowActionDb.STATE_RETURN%>) {
                    status = 'return';
                }
                else if (status==<%=WorkflowActionDb.STATE_IGNORED%>) {
                    status = 'ignore';
                }
                else if (status==<%=WorkflowActionDb.STATE_DISCARDED%>) {
                    status = 'discard';
                }

                $flow.setRectStatus(rectId, status);

                playCount++;

                if (playCount == ary.length) {
                    $.toaster({priority: 'info', message: '<lt:Label res="res.flow.Flow" key="endPlayback"/>'});
                    playCount = 0;
                    return;
                }

                timeoutid = window.setTimeout("doPlayDesigner()", "1000");
            }
    <%
        }
        else {
    %>
            var playCount = 0;

            function PlayDesigner() {
                if (Designer.style.width == "0px") {
                    ShowDesigner();
                }

                var ary = new Array();
                <%
                    MyActionDb mad = new MyActionDb();
                    Vector v = mad.getMyActionDbOfFlow(flow_id);

                    java.util.Iterator ir = v.iterator();
                    int kk = 0;
                    while (ir.hasNext()) {
                        mad = (MyActionDb)ir.next();
                        WorkflowActionDb wa = new WorkflowActionDb();
                        wa = wa.getWorkflowActionDb((int)mad.getActionId());
                        %>
                        ary[<%=kk%>] = [<%=mad.getReceiveDate().getTime()%>, "<%=wa.getInternalName()%>", "<%=mad.getActionStatus()%>"]; // 到达
                        <%
                        kk++;
                        %>
                        ary[<%=kk%>] = [<%=mad.getCheckDate()!=null?mad.getCheckDate().getTime()+"":"999999999999999"%>, "<%=wa.getInternalName()%>", "<%=mad.isChecked()?4:mad.getActionStatus()%>"]; // 处理
                        <%
                        kk++;
                    }
                %>
                if (playCount == 0) {
                    // 对ary中的元素按照时间排序
                    ary.sort(function (a, b) {
                        return parseInt(a[0]) - parseInt(b[0]);
                    })
                }

                Designer.SelectAction(ary[playCount][1]);
                Designer.ActionTitle = Designer.ActionTitle;
                Designer.ActionJobCode = Designer.ActionJobCode;
                Designer.ActionJobName = Designer.ActionJobName;
                Designer.ActionUser = Designer.ActionUser;
                Designer.ActionUserRealName = Designer.ActionUserRealName;
                Designer.ActionFlag = Designer.ActionFlag;
                Designer.ActionDeptMode = Designer.ActionDeptMode;

                Designer.ActionCheckState = ary[playCount][2];
                Designer.ModifyAction();

                playCount++;

                if (playCount == ary.length) {
                    //jAlert('<lt:Label res="res.flow.Flow" key="endPlayback"/>','提示');
                    $.toaster({priority: 'info', message: '<lt:Label res="res.flow.Flow" key="endPlayback"/>'});
                    playCount = 0;
                    return;
                }

                timeoutid = window.setTimeout("PlayDesigner()", "1000");
            }
    <%
        }
    %>
</script>
</html>
