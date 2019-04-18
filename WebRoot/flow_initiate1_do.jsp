<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.file.*" %>
<%@ page import="com.redmoon.oa.workplan.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.kit.util.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.visual.ModuleUtil" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="org.json.JSONObject" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    // 此处的flowTitle已失去用处，被WorkflowMgr.makeTitle取代
    String flowTitle = ParamUtil.get(request, "title");
    String typeCode = ParamUtil.get(request, "typeCode");

    if (typeCode.equals("") || typeCode.equals("not")) {
        String str = LocalUtil.LoadString(request, "res.flow.Flow", "selectTypeProcess");
        out.print(StrUtil.Alert_Back(str));
        return;
    }

    Leaf lf = new Leaf();
    lf = lf.getLeaf(typeCode);

    if (lf == null) {
        String str = LocalUtil.LoadString(request, "res.flow.Flow", "processType");
        out.print(StrUtil.Alert_Back(str));
        return;
    }

    if (lf.getType() == Leaf.TYPE_NONE) {
        String str = LocalUtil.LoadString(request, "res.flow.Flow", "selectTypeProcess");
        out.print(StrUtil.Alert_Back(str));
        return;
    }

    if (flowTitle.equals("")) {
        // 不能判断为空后就退出，因为流程有时候需要直接从菜单上发起
        // out.print(StrUtil.Alert_Back("标题不能为空！"));
        // return;
        flowTitle = lf.getName(request);
    }

    flowTitle = WorkflowMgr.makeTitle(request, privilege, lf);

    WorkflowMgr wm = new WorkflowMgr();
    long startActionId = -1;

    long projectId = ParamUtil.getLong(request, "projectId", -1);
    int emailId = ParamUtil.getInt(request, "emailId", -1);
    int level = ParamUtil.getInt(request, "level", WorkflowDb.LEVEL_NORMAL);

    if (lf.getType() == Leaf.TYPE_FREE) {
        try {
            startActionId = wm.initWorkflowFree(privilege.getUser(request), typeCode, flowTitle, projectId, level);
        } catch (ErrMsgException e) {
            out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
            return;
        }

        WorkflowPredefineDb wfp = new WorkflowPredefineDb();
        wfp = wfp.getPredefineFlowOfFree(typeCode);
        if (wfp.isLight()) {
            response.sendRedirect("flow_dispose_light.jsp?myActionId=" + startActionId + "&emailId=" + emailId);
        } else {
            response.sendRedirect("flow_dispose_free.jsp?myActionId=" + startActionId);
        }
    } else {
        String op = ParamUtil.get(request, "op");
        if (op.equals("workplanTaskReport")) {
            long taskId = ParamUtil.getLong(request, "taskId");
            String strAddDate = ParamUtil.get(request, "addDate");
            Date addDate = DateUtil.parse(strAddDate, "yyyy-MM-dd");
            WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
            WorkflowDb wf = wpad.getWorkflowDbOfTask(taskId, addDate);
            // 如果addDate的流程已存在，则检测流程，若流程已存在，则转至查看流程界面
            if (wf != null) {
                response.sendRedirect("flow_modify.jsp?flowId=" + wf.getId());
                return;
            }
        }

        try {
            startActionId = wm.initWorkflow(privilege.getUser(request), typeCode, flowTitle, projectId, level);
        } catch (ErrMsgException e) {
            out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
            e.printStackTrace();
            // 不能按下行处理，否则如当发起流程时，如果角色不存在或者不允许，回到flo1w_initiate1.jsp就会致出现窗口炸弹
            // out.print(fchar.Alert_Back(e.getMessage()));
            return;
        }

        // 如果是来自于智能模块的操作列，则映射字段值
        if (op.equals("opLinkFlow")) {
            long moduleId = ParamUtil.getLong(request, "moduleId", -1);
            if (moduleId != -1) {
                String moduleCode = ParamUtil.get(request, "moduleCode");
                String linkName = ParamUtil.get(request, "linkName");

                int i = 0;
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                FormDb fdModule = new FormDb();
                fdModule = fdModule.getFormDb(msd.getString("form_code"));
                com.redmoon.oa.visual.FormDAO fdaoModule = new com.redmoon.oa.visual.FormDAO();
                fdaoModule = fdaoModule.getFormDAO(moduleId, fdModule);

                String op_link_name = StrUtil.getNullStr(msd.getString("op_link_name"));
                String[] linkNames = StrUtil.split(op_link_name, ",");
                String op_link_url = StrUtil.getNullStr(msd.getString("op_link_url"));
                String[] linkHrefs = StrUtil.split(op_link_url, ",");

                // 取操作列的配置，映射字段值
                int len = 0;
                if (linkNames != null)
                    len = linkNames.length;
                for (i = 0; i < len; i++) {
                    String lkName = linkNames[i];
                    if (lkName.equals(linkName)) {
                        String url = StrUtil.decodeJSON(linkHrefs[i]);
                        try {
                            JSONObject json = new JSONObject(url);
                            String maps = json.getString("params");

                            MyActionDb mad = new MyActionDb();
                            mad = mad.getMyActionDb(startActionId);
                            long flowId = mad.getFlowId();
                            FormDb fdFlow = new FormDb(lf.getFormCode());
                            FormDAO fdaoFlow = new FormDAO();
                            fdaoFlow = fdaoFlow.getFormDAO((int) flowId, fdFlow);

                            try {
                                // 映射字段值
                                ModuleUtil.doMapOnFlow(request, fdaoModule, fdaoFlow, maps);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ErrMsgException e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // 创建汇报流程记录
        if (op.equals("workplanTaskReport")) {
            MyActionDb mad = new MyActionDb();
            mad = mad.getMyActionDb(startActionId);
            WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
            long taskId = ParamUtil.getLong(request, "taskId");
            wpad.createForReportFlow(taskId, (int) mad.getFlowId(), privilege.getUser(request));
        }

        // 将request中其它参数也传至url中，表单域选择窗体可能会接收此参数
        String param = "";
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                // 过滤掉formCode
                if (paramName.equals("typeCode"))
                    ;
                else
                    param += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
            }
        }

        if (!"".equals(lf.getParams())) {
            String lfParams = lf.getParams();
            lfParams = lfParams.replaceFirst("\\$userName", StrUtil.UrlEncode(privilege.getUser(request)));
            param += "&" + lfParams;
        }

        boolean isFromPaperSW = ParamUtil.get(request, "isFromPaperSW").equals("true");
        if (isFromPaperSW) {
            int paperFlowId = ParamUtil.getInt(request, "paperFlowId", -1);
            if (paperFlowId != -1) {
                WorkflowDb wf = new WorkflowDb();
                wf = wf.getWorkflowDb(paperFlowId);
                int doc_id = wf.getDocId();
                DocumentMgr dm = new DocumentMgr();
                Document doc = dm.getDocument(doc_id);
                if (doc != null) {
                    java.util.Vector attachments = doc.getAttachments(1);
                    java.util.Iterator ir = attachments.iterator();
                    FormDAO fdao = new FormDAO();
                    String visualPath = fdao.getVisualPath();
                    while (ir.hasNext()) {
                        com.redmoon.oa.flow.Attachment am = (com.redmoon.oa.flow.Attachment) ir.next();

                        String randName = FileUpload.getRandName();
                        String ext = StrUtil.getFileExt(am.getName());

                        String diskName = am.getDiskName();
                        String fileName = am.getName();

                        if (ext.equals("doc") || ext.equals("docx")) {
                            randName += ".pdf";
                            int p = diskName.lastIndexOf(".");
                            diskName = diskName.substring(0, p);
                            diskName += ".pdf";

                            p = fileName.lastIndexOf(".");
                            fileName = fileName.substring(0, p);
                            fileName += ".pdf";
                        } else {
                            randName += "." + ext;
                        }

                        MyActionDb mad = new MyActionDb();
                        mad = mad.getMyActionDb(startActionId);
                        int flowId = (int) mad.getFlowId();

                        WorkflowDb mywf = new WorkflowDb();
                        mywf = mywf.getWorkflowDb(flowId);
                        int myDocId = mywf.getDocId();

                        com.redmoon.oa.flow.Attachment att = new com.redmoon.oa.flow.Attachment();
                        String fPathDest = Global.getRealPath() + visualPath + "/" + randName;
                        att.setFullPath(fPathDest);
                        att.setDocId(myDocId);
                        att.setName(fileName);
                        att.setDiskName(randName);
                        att.setVisualPath(visualPath);
                        att.setPageNum(1);
                        att.setOrders(am.getOrders());
                        att.setFieldName("");
                        att.setCreator(privilege.getUser(request));
                        att.setSize(am.getSize());
                        att.create();
                        FileUtil.CopyFile(Global.getRealPath() + am.getVisualPath() + "/" + diskName, fPathDest);
                    }
                }
            }
        }
        response.sendRedirect("flow_dispose.jsp?myActionId=" + startActionId + param);
    }
%>