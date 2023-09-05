<%@ page contentType="text/html; charset=gb2312" %>
<%@ page import="java.io.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.security.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="jxl.*" %>
<%@ page import="jxl.write.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.redmoon.oa.flow.FormViewDb" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.api.INestTableCtl" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConfigUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "read";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String nestFieldName = ParamUtil.get(request, "nestFieldName");
    String parentFormCode = ParamUtil.get(request, "parentFormCode");
    String formCode = ParamUtil.get(request, "formCode");

    FormDb fd = new FormDb();
    String[] fields = null;

    JSONObject json = null;
    int formViewId = -1;
    FormField nestField = null;
    if (!"".equals(nestFieldName)) {
        FormDb parentFd = new FormDb();
        parentFd = parentFd.getFormDb(parentFormCode);
        nestField = parentFd.getFormField(nestFieldName);
        if (nestField == null) {
            out.print("父表单（" + parentFormCode + "）中的嵌套表字段：" + nestFieldName + " 不存在");
            return;
        }
        try {
            String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
            json = new JSONObject(defaultVal);

            boolean isView = false;
            if (!json.isNull("formViewId")) {
                formViewId = StrUtil.toInt((String) json.get("formViewId"), -1);
                if (formViewId!=-1) {
                    isView = true;
                }
            }
            if (isView) {
                FormViewDb formViewDb = new FormViewDb();
                formViewDb = formViewDb.getFormViewDb(formViewId);

                fd = fd.getFormDb(formCode);
                if (!fd.isLoaded()) {
                    out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "表单不存在！"));
                    return;
                }

                MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
                INestTableCtl nestTableCtl = macroCtlService.getNestTableCtl();
                Vector fieldsV = nestTableCtl.parseFieldsByView(fd, formViewDb.getString("content"));
                fields = new String[fieldsV.size()];
                int i = 0;
                Iterator ir = fieldsV.iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField)ir.next();
                    fields[i] = ff.getName();
                    i++;
                }
            }
            else {
                String code = ParamUtil.get(request, "formCode");
                String moduleCode = ParamUtil.get(request, "moduleCode");
                if ("".equals(moduleCode)) {
                    moduleCode = code;
                }
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                if (msd==null) {
                    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
                    return;
                }

                fields = msd.getColAry(false, "list_field");

                formCode = msd.getString("form_code");

                fd = fd.getFormDb(formCode);
                if (!fd.isLoaded()) {
                    out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "表单不存在！"));
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    response.setContentType("application/vnd.ms-excel");
    response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(fd.getName()) + ".xls");
    OutputStream os = response.getOutputStream();

    try {
        /*File file = new File(Global.realPath + "visual/template/blank.xls");
        Workbook wb = Workbook.getWorkbook(file);*/
        ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
        InputStream inputStream = configUtil.getFile("templ/blank.xls");
        Workbook wb = Workbook.getWorkbook(inputStream);

        WorkbookSettings settings = new WorkbookSettings();
        settings.setWriteAccess(null);

        // 打开一个文件的副本，并且指定数据写回到原文件
        WritableWorkbook wwb = Workbook.createWorkbook(os, wb, settings);
        WritableSheet ws = wwb.getSheet(0);

        int len = 0;
        if (fields!=null) {
            len = fields.length;
        }
        for (int i=0; i<len; i++) {
            String fieldName = fields[i];
            String title = "创建者";
            if (!"cws_creator".equals(fieldName)) {
                title = fd.getFieldTitle(fieldName);
            }

            Label a = new Label(i, 0, title);
            ws.addCell(a);
        }

        wwb.write();
        wwb.close();
        wb.close();
    } catch (Exception e) {
        out.println(e.toString());
    } finally {
        os.close();
    }

    out.clear();
    out = pageContext.pushBody();
%>