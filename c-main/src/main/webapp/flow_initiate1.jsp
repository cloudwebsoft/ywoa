<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.post.PostFlowMgr" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Vector" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "flow.init";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String userName = privilege.getUser(request);
    int emailId = ParamUtil.getInt(request, "emailId", -1);
    String op = ParamUtil.get(request, "op"); // 会议申请，op为typeCode
    String flowTitle = "";
    if (!"".equals(op)) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(op);
        if (lf != null) {
            flowTitle = lf.getName(request);
        } else {
            out.print(SkinUtil.makeErrMsg(request, "流程类型不存在！"));
            return;
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>发起流程</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link href="lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet">
    <style>
        .leaf-box {
            margin: 10px 0 20px;
            float: left;
            width: 150px;
            height: 80px;
            overflow: hidden;
            text-align: center;
            cursor: pointer;
        }
        .icon-box {
            text-align: center;
            margin-bottom: 10px;
        }
        .icon {
            width: 1em;
            height: 1em;
            vertical-align: -0.15em;
            fill: currentColor;
            overflow: hidden;
            font-size: 32px;
        }
    </style>
    <script src="inc/common.js"></script>
    <script src="js/jquery-1.9.1.min.js"></script>
    <script src="js/jquery-migrate-1.2.1.min.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
    <script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
    <script src="inc/flow_js.jsp"></script>
    <link href="js/select2/select2.css" rel="stylesheet"/>
    <script src="js/select2/select2.js"></script>
    <script type="text/javascript" src="js/jquery.toaster.js"></script>
    <script type="text/javascript" src="inc/livevalidation_standalone.js"></script>
    <script src="fonts/flow/iconfont.js"></script>
    <script language="JavaScript" type="text/JavaScript">
        function sel(code, name, type) {
            if (type == 0) {
                jAlert("请选择流程类型！", "提示");
                return;
            }
            $("typeCode").value = code;
            $("divName").innerHTML = name;
            $("title").value = name + $("curTime").value;
        }

        function form1_onsubmit() {
            if (o("typeCode").value === "not") {
                jAlert("请选择正确的流程类型！", "提示");
                return false;
            }
        }

        function onload() {
            <%
            if (!"".equals(op)) {
            %>
            o('form1').submit();
            <%
            }
            %>
        }
    </script>
</head>
<body onload="onload()">
<%--<%if ("".equals(op)) {%>
<table cellspacing="0" cellpadding="0" width="100%">
    <tbody>
    <tr>
        <td class="tdStyle_1"><lt:Label res="res.flow.Flow" key="InitiateProcess"/></td>
    </tr>
    </tbody>
</table>
<%}%>--%>
<%
    Leaf lf2 = new Leaf();
    lf2 = lf2.getLeaf(Leaf.CODE_ROOT);

    Directory dir = new Directory();
    Leaf rootlf = dir.getLeaf(Leaf.CODE_ROOT);
    DirectoryView dv = new DirectoryView(rootlf);
    LeafChildrenCacheMgr leafChildrenCacheMgr = new LeafChildrenCacheMgr(Leaf.CODE_ROOT);
    Vector<Leaf> children = leafChildrenCacheMgr.getList();
    Iterator<Leaf> ri = children.iterator();
%>
<table align="center" class="percent98">
    <%if ("".equals(op)) {%>
    <tr>
        <td>
            <div style="margin-top: 10px">
                <select id="flowTypeCode">
                    <option value="">请选择</option>
                    <%
                        while (ri.hasNext()) {
                            Leaf childlf = (Leaf) ri.next();
                            // 发起流程界面
                            if (childlf.isOpen() && dv.canUserSeeWhenInitFlow(request, childlf)) {
                                LeafChildrenCacheMgr childrenCacheMgr = new LeafChildrenCacheMgr(childlf.getCode());
                                for (Leaf chlf : childrenCacheMgr.getList()) {
                                    if (chlf.isOpen() && dv.canUserSeeWhenInitFlow(request, chlf)) {
                    %>
                    <option value="<%=chlf.getCode()%>"><%=chlf.getName()%></option>
                    <%
                                    }
                                }
                            }
                        }
                    %>
                </select>
                <button class="btn btn-default" id="btnInit">快速发起</button>
            </div>
            <%
                PostFlowMgr pfMgr = new PostFlowMgr();
                ArrayList<String> list = pfMgr.listCanUserStartFlow(userName);
                ri = children.iterator();
                while (ri.hasNext()) {
                    Leaf childlf = (Leaf) ri.next();
                    if (childlf.isOpen() && dv.canUserSeeWhenInitFlow(request, childlf)) {
            %>
            <div style="line-height:1.5; margin-top:5px; margin-bottom:10px; padding:5px;">
                <div style="width:98%; font-weight:bold; padding-bottom:10px; border-bottom:1px dashed #cccccc; margin-bottom:6px; clear:both">
                    <i class="fa fa-tags"></i>
                    <%=childlf.getName(request)%>
                </div>
                <%
                    LeafChildrenCacheMgr childrenCacheMgr = new LeafChildrenCacheMgr(childlf.getCode());
                    for (Leaf chlf : childrenCacheMgr.getList()) {
                        if (chlf.isOpen() && dv.canUserSeeWhenInitFlow(request, chlf)) {
                            // 如果是属于绩效考核类型
                            if (Leaf.CODE_PERFORMANCE.equals(chlf.getParentCode())) {
                                if (list.contains(chlf.getCode())) {
                %>
                <div class="leaf-box" onclick="initFlow('<%=chlf.getCode()%>', '<%=chlf.getName(request)%>', '<%=chlf.getType()%>')">
                    <div class="icon-box">
                        <svg class="icon svg-icon" aria-hidden="true">
                            <use id="useFontIcon" xlink:href="<%=chlf.getIcon()%>"></use>
                        </svg>
                    </div>
                    <a href="javascript:"><%=chlf.getName(request)%> </a>
                </div>
                <%
                                }
                            } else {
                %>
                <div class="leaf-box" onclick="initFlow('<%=chlf.getCode()%>', '<%=chlf.getName(request)%>', '<%=chlf.getType()%>')">
                    <div class="icon-box">
                        <svg class="icon svg-icon" aria-hidden="true">
                            <use id="useFontIcon" xlink:href="<%=chlf.getIcon()%>"></use>
                        </svg>
                    </div>
                    <a href="javascript:"><%=chlf.getName(request)%>
                    </a>
                </div>
                <%
                                }
                            }
                        }
                    }
                %>
            </div>
            <%
                }
            %>
        </td>
    </tr>
    <%
    }
    %>
</table>
<div id="dlg" style="display:none">
    <form action="flow_initiate1_do.jsp" method=post name="form1" id=form1 onSubmit="return form1_onsubmit()">
        <table width="48%" class="percent98">
            <tr>
                <td>流程类型：
                    <span id="divName">
              <%
                  if (!"".equals(op)) {
                      lf2 = lf2.getLeaf(op);
                      if (lf2 == null) {
                          out.print("流程类型" + op + "未找到！");
                      } else {
                          out.print(lf2.getName());
                      }
                  } else {
              %>
                          <span id="spanFlowTypeName">请点击选择流程类型</span>
              <%
                  }
              %>
                    </span>
                </td>
            </tr>
            <%
                long projectId = ParamUtil.getLong(request, "projectId", -1);
            %>
            <tr id="prjTr" style="display:<%=projectId==-1?"none":""%>">
                <td>关联项目：
                    <%
                        String prjName = "";
                        if (projectId != -1) {
                            FormMgr fm = new FormMgr();
                            FormDb fd = fm.getFormDb("project");

                            com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
                            com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(projectId);

                            prjName = fdao.getFieldValue("name");
                        }
                    %>
                    <input id="projectId" name="projectId" type="hidden" value="<%=projectId%>"/>
                    <input id="emailId" name="emailId" type="hidden" value="<%=emailId%>"/>
                    <input id="projectId_realshow" name="projectId_realshow" readonly value="<%=prjName%>"/>
                    <input name="button" type="button" onclick='openWinProjectList(projectId)' value='选择' class="btn"/>
                </td>
            </tr>
            <tr>
                <td>流程等级：
                    <input name="level" type="radio" value="<%=WorkflowDb.LEVEL_NORMAL%>" checked/><img src="images/general.png" align="absmiddle"/>普通
                    <input name="level" type="radio" value="<%=WorkflowDb.LEVEL_IMPORTANT%>"/><img src="images/important.png" align="absmiddle"/>&nbsp;重要
                    <input name="level" type="radio" value="<%=WorkflowDb.LEVEL_URGENT%>"/><img src="images/urgent.png" align="absmiddle"/>&nbsp;紧急
                </td>
            </tr>
            <tr>
                <td>流程名称：
                    <%
                        java.util.Date d = new java.util.Date();
                    %>
                    <input name="curTime" value="[<%=DateUtil.format(d, "yyyy-MM-dd HH:mm:ss")%>]" type="hidden">
                    <input id="title" name="title" type="text" size="30" value="<%=flowTitle%>"/>
                    <input id="typeCode" name="typeCode" type="hidden" value="<%=op%>">
                </td>
            </tr>
        </table>
        <%
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);
                if (paramValues.length == 1) {
                    String paramValue = paramValues[0];
                    // 过滤掉formCode
                    if ("typeCode".equals(paramName) || "title".equals(paramName) || "op".equals(paramName)) {
                        ;
                    }
                    else {%>
                        <input name="<%=paramName%>" value="<%=paramValue%>" type="hidden"/>
                    <%
                    }
                }
            }
        %>
    </form>
</div>
<script>
    var title = new LiveValidation('title');
    title.add(Validate.Presence, {failureMessage: '请填写名称！'});
</script>
</body>
<script>
    function relateProject() {
        o("prjTr").style.display = "";
    }

    function initFlow(code, name, type) {
        <%
        String isFromPaperSW=ParamUtil.get(request, "isFromPaperSW");
        long paperFlowId = ParamUtil.getLong(request, "paperFlowId", -1);
        %>

        addTab(name, "<%=request.getContextPath()%>/flow_initiate1_do.jsp?typeCode=" + code + "&projectId=<%=projectId%>&title=" + encodeURI("<%=flowTitle%>") + "&level=<%=WorkflowDb.LEVEL_NORMAL%>&curTime=" + encodeURI("[<%=DateUtil.format(d, "yyyy-MM-dd HH:mm:ss")%>]") + "&isFromPaperSW=<%=isFromPaperSW%>&paperFlowId=<%=paperFlowId%>");
    }

    // 取得select2所选的多个值
    function getSelect2Val(id) {
        return $.map($("#" + id).select2('data'), function (value) {
            return value.id
        }).join(",");
    }

    // 取得select2所选的多个文本
    function getSelect2Text(id) {
        return $.map($("#" + id).select2('data'), function (value) {
            return value.text
        }).join(",");
    }

    $(function () {
        $('#flowTypeCode').select2();

        $('#btnInit').click(function () {
            var code = getSelect2Val('flowTypeCode');
            var name = getSelect2Text('flowTypeCode');
            if (code === '') {
                $.toaster({priority: 'info', message: '请选择流程类型'});
                return;
            }
            initFlow(code, name);
        })
    })
</script>
</html>