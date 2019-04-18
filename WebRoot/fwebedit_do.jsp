<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<%
    if (!privilege.isUserLogin(request)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    String op = ParamUtil.get(request, "op");

    JSONObject json = new JSONObject();
    boolean re = false;
    try {
        re = docmanager.Operate(application, request, privilege);
    } catch (ErrMsgException e) {
        String action = ParamUtil.get(request, "action");
        if ("fckwebedit_new".equals(action)) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
            out.print(json.toString());
            return;
        } else {
            out.print(e.getMessage());
        }
        return;
    }
    if (re) {
        String action = ParamUtil.get(request, "action");
        if (action.equals("fckwebedit_new")) {
            com.redmoon.kit.util.FileUpload fu = docmanager.getFileUpload();
            op = fu.getFieldValue("op");
            if (op.equals("edit")) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
                out.print(json.toString());
                return;
            } else {
                String pageUrl = "";
                if (docmanager.getDirCode().indexOf("cws_prj_") == 0) {
                    String projectId = docmanager.getDirCode().substring(8);
                    // 如果projectId中含有下划线_，则截取出其ID
                    int p = projectId.indexOf("_");
                    if (p != -1) {
                        projectId = projectId.substring(0, p);
                    }
                    pageUrl = "fileark/document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(docmanager.getDirCode()) + "&projectId=" + projectId + "&parentId=" + projectId + "&formCode=project";
                }
                json.put("ret", 1);
                json.put("msg", "操作成功！");
                json.put("redirectUri", pageUrl);
                out.print(json.toString());
                return;
            }
        } else if (action.equals("wikiPost")) {
            out.print(StrUtil.Alert_Redirect("操作成功！", "fileark/wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(docmanager.getDirCode())));
            return;
        } else {
            Document doc = docmanager.getDocument(); // 取得新创建的文档
            if (doc.getExamine() == Document.EXAMINE_NOT)
                out.print("操作成功，正在等待审核中...");
            else
                out.print("操作成功！");
        }
    } else {
        json.put("ret", 0);
        json.put("msg", "操作失败！");
        out.print(json.toString());
        return;
    }
%>