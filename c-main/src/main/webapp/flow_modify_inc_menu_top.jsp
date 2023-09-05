<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.flow.WorkflowMgr" %>
<%@ page import="com.redmoon.oa.flow.WorkflowRuler" %>
<%@ page import="com.redmoon.oa.flow.WorkflowDb" %>
<%@ page import="com.redmoon.oa.flow.Directory" %>
<%@ page import="com.redmoon.oa.flow.Leaf" %>
<%@ page import="com.redmoon.oa.flow.LeafPriv" %>
<%@ page import="com.redmoon.oa.flow.PaperDistributeDb" %>
<%@ page import="com.redmoon.oa.flow.WorkflowPredefineDb" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<%
    int flowIdTop = ParamUtil.getInt(request, "flowId");
    Privilege pvgTop = new Privilege();
    WorkflowRuler wrTop = new WorkflowRuler();
    WorkflowMgr wfmTop = new WorkflowMgr();
    WorkflowDb wfTop = wfmTop.getWorkflowDb(flowIdTop);

    com.redmoon.oa.flow.Directory dirTop = new com.redmoon.oa.flow.Directory();
    Leaf ftTop = dirTop.getLeaf(wfTop.getTypeCode());
    boolean isFreeTop = false;
    if (ftTop != null) {
        isFreeTop = ftTop.getType() != Leaf.TYPE_LIST;
    }

    com.redmoon.oa.Config cfgTop = new com.redmoon.oa.Config();
    boolean canUserSeeFlowChartTop = cfgTop.getBooleanProperty("canUserSeeFlowChart");
    boolean canUserSeeFlowImageTop = cfgTop.getBooleanProperty("canUserSeeFlowImage");
    String canUserSeeDesignerWhenDisposeTop = cfgTop.get("canUserSeeDesignerWhenDispose");
    boolean isFlowManagerTop = false;
    if (pvgTop.isUserPrivValid(request, "admin")) {
        isFlowManagerTop = true;
    } else {
        LeafPriv lpTop = new LeafPriv(wfTop.getTypeCode());
        if (pvgTop.isUserPrivValid(request, "admin.flow")) {
            if (lpTop.canUserExamine(pvgTop.getUser(request))) {
                isFlowManagerTop = true;
            }
        }
    }
    String visitKeyTop = ParamUtil.get(request, "visitKey");
%>
<DIV id="tabs1">
    <ul>
        <li id="menu1"><a href="<%=request.getContextPath()%>/flowShowPage.do?flowId=<%=flowIdTop%>"><span><lt:Label res="res.flow.Flow" key="transferProcess"/></span></a></li>
        <%if (!isFreeTop && (canUserSeeFlowChartTop || canUserSeeFlowImageTop || canUserSeeDesignerWhenDisposeTop.equals("true") || isFlowManagerTop)) {%>
        <li id="menu2"><a href="<%=request.getContextPath()%>/flowShowChartPage.do?flowId=<%=flowIdTop%>"><span><lt:Label res="res.flow.Flow" key="flowChart"/></span></a></li>
        <%}

            if (pvgTop.isUserPrivValid(request, "admin") || isFlowManagerTop || wrTop.canUserModifyFlow(request, wfTop)) {
        %>
        <li id="menu6"><a href="<%=request.getContextPath()%>/flowModifyTitlePage?flowId=<%=flowIdTop%>&visitKey=<%=visitKeyTop%>"><span><lt:Label res="res.flow.Flow" key="modifyTitle"/></span></a></li>
        <%
            }

            PaperDistributeDb pddTop = new PaperDistributeDb();
            int paperCountTop = pddTop.getCountOfWorkflow(flowIdTop);
            if (paperCountTop > 0) {
                String disBtnName = cn.js.fan.web.SkinUtil.LoadStr(request, "res.flow.Flow", "notify"); // "流程抄送";
                String kindTop = com.redmoon.oa.kernel.License.getInstance().getKind();
                if (kindTop.equalsIgnoreCase(com.redmoon.oa.kernel.License.KIND_COM)) {
                    disBtnName = "流程知会";
                }
        %>
        <li id="menu7"><a href="<%=request.getContextPath()%>/paper/paper_distribute_flow_list.jsp?flowId=<%=flowIdTop%>&visitKey=<%=visitKeyTop%>"><span><%=disBtnName%></span></a></li>
        <%
            }

            // 如果配置了自动存档目录
            WorkflowPredefineDb wpdTop = new WorkflowPredefineDb();
            wpdTop = wpdTop.getDefaultPredefineFlow(wfTop.getTypeCode());
            if (!"".equals(wpdTop.getDirCode())) {
        %>
        <li id="menu9"><a href="<%=request.getContextPath()%>/flow/flow_doc_list.jsp?flowId=<%=flowIdTop%>&visitKey=<%=visitKeyTop%>"><span><lt:Label res="res.flow.Flow" key="reactivationList"/></span></a></li>
        <%
            }

            if (pvgTop.isUserPrivValid(request, "admin") || isFlowManagerTop) {
        %>
        <li id="menu8"><a href="<%=request.getContextPath()%>/admin/flow_intervene.jsp?flowId=<%=flowIdTop%>&visitKey=<%=visitKeyTop%>"><span><lt:Label res="res.flow.Flow" key="interventionProcess"/></span></a></li>
        <%
            }
        %>
    </ul>
</DIV>

