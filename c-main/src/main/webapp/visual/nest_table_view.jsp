<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.NumberUtil" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.redmoon.oa.flow.query.QueryScriptUtil" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.redmoon.oa.util.NestTableNodeVisitor" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.htmlparser.Node" %>
<%@ page import="org.htmlparser.NodeFilter" %>
<%@ page import="org.htmlparser.Parser" %>
<%@ page import="org.htmlparser.filters.*" %>
<%@ page import="org.htmlparser.util.NodeList" %>
<%@ page import="org.htmlparser.util.ParserException" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.redmoon.oa.base.IFormMacroCtl" %>
<%@ page import="com.redmoon.oa.util.RequestUtil" %>
<%@ page import="com.redmoon.oa.base.IFormDAO" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.Render" %>
<%@ page import="com.redmoon.oa.visual.FormDAO" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="org.htmlparser.tags.TableRow" %>
<%@ page import="org.htmlparser.tags.TableColumn" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.util.NestTableNodeAlignVisitor" %>
<%@ page import="com.cloudweb.oa.api.INestTableCtl" %>
<%@ page import="com.cloudweb.oa.service.MacroCtlService" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="com.cloudweb.oa.utils.SysUtil" %>
<%@ page import="cn.js.fan.util.ErrMsgException" %>
<script>
    // 初始化鼠标浮过行时高亮显示
    function bindNestTableMouseEvent() {
        $("table[id^='nestTable_'] td").mouseout(function () {
            // nestTable没有thead，所以$(this).parent().parent()必为tbody
            // 获得td是在第几行
            var trIndex = $(this).parent().parent().find("tr").index($(this).parent()[0]);
            if (trIndex != 0) {
                $(this).parent().find("td").each(function (i) {
                    $(this).removeClass("tdOver");
                });
            }
        });

        $("table[id^='nestTable_'] td").mouseover(function () {
            var trIndex = $(this).parent().parent().find("tr").index($(this).parent()[0]);
            if (trIndex != 0) {
                $(this).parent().find("td").each(function (i) {
                    $(this).addClass("tdOver");
                });
            }
        });
    }
</script>
<%
    String formCode = ParamUtil.get(request, "formCode");
    if ("".equals(formCode)) {
        out.print(SkinUtil.makeErrMsg(request, "表单编码不存在"));
        return;
    }
    // 不能通过getFormDb从缓存中获取，因为在FormDAO.getFormDAO()方法中已经对缓存中的FormDb中的fields进行了赋值操作，取出来的fields可能已被赋值
    // fd = fd.getFormDb(formCode);
    // 传过来的formCode有可能是模块编码
    FormDb fd = new FormDb(formCode);
    if (!fd.isLoaded()) {
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(formCode);
        if (msd == null || !msd.isLoaded()) {
            out.print(SkinUtil.makeErrMsg(request, "模块不存在"));
            return;
        }
    }

    String op = ParamUtil.get(request, "op");
    String pageType = ParamUtil.get(request, "pageType");
    String formId = Render.FORM_FLEMENT_ID;
    if (!"show".equals(op)) {
        if ("flow".equals(pageType)) {
            formId = com.redmoon.oa.flow.Render.FORM_FLEMENT_ID;
        }
    }

    int queryId = -1;
    String parentFormCode = ParamUtil.get(request, "parentFormCode");
    String nestFieldName = ParamUtil.get(request, "nestFieldName");
    JSONObject json = null;
    JSONArray mapAry = new JSONArray();
    int formViewId = -1, isAddHighlight = 0;
    boolean canAdd = true, canEdit = true, canImport = true, canExport = true, canDel = true, canSel = true;
    boolean isAutoSel = false;
    int isNoShow = 1;
    String nestFilter = "";
    FormField nestField = null;
    String nestFormCode = "";
    boolean isPropStat = false;
    JSONObject jsonPropStat = null;
    StringBuffer trStatHtml = new StringBuffer(); // 统计行的html
    String propStat = "";
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
            try {
                canAdd = "true".equals(json.getString("canAdd"));
                canEdit = "true".equals(json.getString("canEdit"));
                canImport = "true".equals(json.getString("canImport"));
                canDel = "true".equals(json.getString("canDel"));
                canSel = "true".equals(json.getString("canSel"));
                nestFilter = json.getString("filter");
                if (json.has("canExport")) {
                    canExport = "true".equals(json.getString("canExport"));
                }
                if (json.has("isAutoSel")) {
                    isAutoSel = "1".equals(json.getString("isAutoSel"));
                }
                if (!json.isNull("queryId")) {
                    queryId = StrUtil.toInt((String)json.get("queryId"));
                }
                nestFormCode = json.getString("destForm");
                if (json.has("isAddHighlight")) {
                    isAddHighlight = json.getInt("isAddHighlight");
                }
                if (json.has("isNoShow")) {
                    isNoShow = json.getInt("isNoShow");
                }
                if (json.has("propStat")) {
                    propStat = json.get("propStat").toString(); // get("propStat")可能为{}，即json对象，而不是字符串，此时用json.getString("propStat")会报错
                    if (StringUtils.isNotEmpty(propStat)) {
                        if ("".equals(propStat)) {
                            propStat = "{}";
                        }
                        jsonPropStat = new JSONObject(propStat);
                        if (jsonPropStat.length()>0) {
                            isPropStat = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!json.isNull("maps")) {
                mapAry = (JSONArray) json.get("maps");
            }
            if (!json.isNull("formViewId")) {
                formViewId = StrUtil.toInt((String) json.get("formViewId"), -1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
        }
    }

    // 在ModuleFieldSetlectCtl中用到
    request.setAttribute("nestFormCode", nestFormCode);

    ModuleSetupDb msdNest = new ModuleSetupDb();
    msdNest = msdNest.getModuleSetupDb(nestFormCode);
    String[] fieldsAry = msdNest.getColAry(false, "list_field");
    String[] fieldsAlign = msdNest.getColAry(false, "list_field_align");
    Map<String, String> mapAlign = new HashMap();
    for (int i=0; i<fieldsAry.length; i++) {
        mapAlign.put(fieldsAry[i], fieldsAlign[i]);
    }

    String viewForm = "";
    String viewContent = "";
    if (formViewId!=-1) {
        FormViewDb formViewDb = new FormViewDb();
        formViewDb = formViewDb.getFormViewDb(formViewId);
        viewForm = formViewDb.getString("form");
        viewContent = formViewDb.getString("content");
    }
    else {
        try {
            viewContent = FormViewMgr.makeViewContent(msdNest);
        }
        catch (ErrMsgException e) {
            out.print(e.getMessage());
            return;
        }
        viewForm = FormViewMgr.makeViewForm(nestFormCode, viewContent);
    }

    // 清除content中表格的样式，置宽度为100%
    viewForm = viewForm.replaceAll(" class=\"tabStyle_8\"", " id=\"nestTable_" + nestFieldName + "\" formCode=\"" + nestFormCode + "\" class=\"nest-table\"");

    int headerRows = 0; // 表格头部所占的行数
    int rowId = 0; // 行的标识
    int flowId = -1;
    String tableHtml = ""; // 生成辅助用的表格，里面的行tr为模板
    String cwsId = ParamUtil.get(request, "cwsId");
    StringBuffer scriptsForDisableAndHideForAdd = new StringBuffer();
    StringBuilder sbTmpHtml = new StringBuilder();

    // 取出视图中最后一行tr
    NodeFilter filter = new CssSelectorNodeFilter("tr"); // .className tr
    filter = new AndFilter(filter, new NotFilter(new HasChildFilter(new CssSelectorNodeFilter("tr"))));
    Parser parser;
    try {
        parser = new Parser(viewForm);
        NodeList list = parser.extractAllNodesThatMatch(filter);

        headerRows = list.size() - 1;

        int lastTrIndex = list.size() - 1;
        if (lastTrIndex < 0) {
            out.print("视图中不存在表格行");
            return;
        }
        Node tr = list.elementAt(lastTrIndex);
        String trHtml = tr.toHtml();

        // 去掉最后一行
        int lastTrStartPos = tr.getStartPosition();
        int e = lastTrStartPos + trHtml.length();
        String c = viewForm.substring(0, lastTrStartPos);
        c += viewForm.substring(e);
        viewForm = c;

        // 解析出行中的字段，按td从左至右顺序
        MacroCtlService macroCtlService = SpringUtil.getBean(MacroCtlService.class);
        INestTableCtl nestTableCtl = macroCtlService.getNestTableCtl();
        Vector<FormField> fields = nestTableCtl.parseFieldsByView(fd, viewContent);
        MacroCtlMgr mm = new MacroCtlMgr();

        if (op.toLowerCase().contains("show")) {
            StringBuffer trsTmp = new StringBuffer();
            int k = 1;
            String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(cwsId) + " and cws_parent_form=" + StrUtil.sqlstr(parentFormCode) + " order by cws_order";
            FormDAO fdao = new FormDAO();
            Vector<FormDAO> vt = fdao.list(formCode, sql);
            for (FormDAO formDAO : vt) {
                fdao = formDAO;

                String cls = "";
                if (isAddHighlight == 1) {
                    if (fdao.getCwsQuoteId() == 0) {
                        cls = "row-add";
                    } else {
                        cls = "row-pull";
                    }
                }

                trsTmp.append("<tr class='").append(cls).append("'><td class='td-no' style=\"display: " + (isNoShow==1?"":"none") + "\">").append(k).append("</td>"); // 序号列
                int col = 0;
                for (FormField ff : fields) {
                    ff = fdao.getFormField(ff.getName());

                    String desc = "";
                    if (ff.getType().equals(FormField.TYPE_MACRO)) {
                        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                        if (mu != null) {
                            IFormDAO ifdaoOld = RequestUtil.getFormDAO(request);
                            RequestUtil.setFormDAO(request, fdao);
                            desc = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
                            if (ifdaoOld != null) {
                                // 恢复request中原来的fdao，以免可能有其它地方用到而带来影响
                                RequestUtil.setFormDAO(request, ifdaoOld);
                            }
                        }
                    } else {
                        desc = FuncUtil.renderFieldValue(fdao, ff);
                    }

                    trsTmp.append("<td align='" + fieldsAlign[col] + "'>" + desc + "</td>");
                    col ++;
                }
                trsTmp.append("</tr>");
                k++;
            }

            FormDb parentFd = new FormDb();
            parentFd = parentFd.getFormDb(parentFormCode);
            String strSums = FormUtil.getSums(fd, parentFd, cwsId).toString();
            com.alibaba.fastjson.JSONObject jsonSums = com.alibaba.fastjson.JSONObject.parseObject(strSums);
            if (isPropStat) {
                for (FormField formField : fields) {
                    Iterator<String> ir3 = jsonPropStat.keys();
                    boolean isFound = false;
                    while (ir3.hasNext()) {
                        String fieldName = ir3.next();

                        if (formField.getName().equals(fieldName)) {
                            isFound = true;

                            String modeStat = jsonPropStat.getString(fieldName);

                            FormField ff = fd.getFormField(fieldName);
                            if (ff == null) {
                                DebugUtil.e(getClass(), "合计字段", "field " + fieldName + " is not exist");
                            }
                            int fieldType = ff.getFieldType();

                            double sumVal = 0;
                            if (vt.size() > 0) {
                                sumVal = FormSQLBuilder.getSUMOfSQL(sql, fieldName);
                            }

                            if ("0".equals(modeStat)) {
                                if (fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG) {
                                    trStatHtml.append("<td align='" + mapAlign.get(fieldName) + "'><span id='cws_stat_" + fieldName + "' title='合计'>" + (long) sumVal + "</span></td>");
                                } else {
                                    trStatHtml.append("<td align='" + mapAlign.get(fieldName) + "'><span id='cws_stat_" + fieldName + "' title='合计'>" + NumberUtil.round(sumVal, 2) + "</span></td>");
                                }
                            } else if (modeStat.equals("1")) {
                                trStatHtml.append("<td align='" + mapAlign.get(fieldName) + "'><span id='cws_stat_" + fieldName + "' title='平均'>" + NumberUtil.round(sumVal / rowId, 2) + "</span></td>");
                            }
                        }
                    }

                    if (!isFound) {
                        trStatHtml.append("<td></td>");
                    }
                }

                trStatHtml.insert(0, "<tr id='trStat'><td align='center'>合计</td>");
                trStatHtml.append("</tr>");

                // 如果已经存在记录，则插入于表格中
                if (vt.size() > 0) {
                    trsTmp.append(trStatHtml);
                }
            }

            // 插入在最后一个</table>之前
            int p = viewForm.lastIndexOf("</tbody>");
            if (p==-1) {
                p = viewForm.lastIndexOf("</table>");
            }
            if (p==-1) {
                out.print("嵌套表格视图格式非法");
                return;
            }
            String tmpStrLeft = viewForm.substring(0, p);
            String tmpStrRight = viewForm.substring(p);
            viewForm = tmpStrLeft + trsTmp.toString() + tmpStrRight;
            out.print(viewForm);

            boolean isPageShow = pageType.toLowerCase().contains("show");
        %>
        <script>
        $(function() {
            // 增加序号列
            var tdHeaderCheckbox = "<td width=\"45\" rowspan=\"<%=headerRows%>\" style=\"display: <%=isNoShow==1?"":"none"%>\" align=\"center\" valign=\"middle\">序号</td>";
            $("#nestTable_<%=nestFieldName%> tr").first().prepend(tdHeaderCheckbox);
            <%
            if (isPageShow) {
            %>
            // 置主表中计算控件的值
            var nestSheetSums = <%=jsonSums.toString()%>;
            console.log('nest_table_view.jsp nestSheetSums', nestSheetSums, 'formCode', '<%=formCode%>');
            /*for (var o in nestSheetSums) {
                var elId = o;
                var $ctl = $("span[id='" + elId + "'][formCode='<%=formCode%>']");
                if (!$ctl[0]) {
                    // 向下兼容，旧版的sum型计算控件中没有属性formCode
                    $ctl = $("span[id='" + elId + "']");
                }
                $ctl.html(nestSheetSums[o]);
            }*/
            var keys = '';
            for (var o in nestSheetSums) {
                if (keys.indexOf(',' + o + ',') != -1) {
                    // 跳过已正常取得的字段，因为可能在sum时两个嵌套表中都含有同名的字段，而其中一个是有formCode属性的
                    continue;
                }
                console.log('keys', keys);
                var $ctl = $("span[id='" + o + "'][formCode='<%=formCode%>']");
                if (!$ctl[0]) {
                    // 向下兼容会带来问题，如果在sum时两个嵌套表中都含有同名的字段，会导致出现问题，故需带有formCode属性的计算控件字段记住
                    // 向下兼容，旧版的sum型计算控件中没有formCode
                    $ctl = $("span[id='" + o + "']");
                } else {
                    if (keys == '') {
                        keys = ',' + o + ',';
                    } else {
                        keys += o + ',';
                    }
                }
                $ctl.html(nestSheetSums[o]);
            }

            <%
            }
            %>
        });

        bindNestTableMouseEvent();
        </script>
        <%
            return;
        }

        // render最后一行的内容，生成表单
        com.redmoon.oa.visual.Render render = new com.redmoon.oa.visual.Render(request, fd);
        int actionId = ParamUtil.getInt(request, "actionId", -1);
        List<FormField>[] disabledAndHidedFields = null;
        if (actionId!=-1) {
            WorkflowActionDb wa = new WorkflowActionDb();
            wa = wa.getWorkflowActionDb(actionId);
            flowId = wa.getFlowId();

            disabledAndHidedFields = com.redmoon.oa.flow.Render.getDisabledAndHidedFieldOfNestTable(wa, fields);
        }

        StringBuilder outerHtmls = new StringBuilder();
        StringBuilder scripts = new StringBuilder();
        StringBuilder scriptsForDisableAndHide = new StringBuilder();

        if ("edit".equals(op)) {
            // 找到第一个td，在之前插入checkbox、rowId及记录的ID
            int p = trHtml.indexOf("<td");
            String tmpL = trHtml.substring(0, p);
            String tmpR = trHtml.substring(p);
            trHtml = tmpL + "<td align=\"center\" valign=\"middle\"><input name=\"chk" + nestFieldName + "\" type='checkbox'/><input name='rowId" + nestFieldName + "' type='hidden'/><input name='dataId" + nestFieldName + "' type='hidden' value=''/></td><td class='td-no' style=\"display: " + (isNoShow==1?"":"none") + "\"></td>" + tmpR;

            // 加入已有的记录，列出记录，通过rendForNestTable，将结果加至nestTable，再initDateCtlInLastTrOfTable
            String sql = "select id from " + fd.getTableNameByForm() + " where cws_id=" + StrUtil.sqlstr(cwsId) + " and cws_parent_form=" + StrUtil.sqlstr(parentFormCode) + " order by id"; // cws_order";
            FormDAO fdao = new FormDAO();
            Vector<FormDAO> vt = fdao.list(formCode, sql);
            Iterator<FormDAO> ir = vt.iterator();
            while (ir.hasNext()) {
                fdao = ir.next();
                Vector<FormField> rowFields = fdao.getFields();
                // 改变字段名
                Vector<FormField> fieldsCloned = new Vector<>();
                for (FormField ff : rowFields) {
                    FormField ffCloned = (FormField) ff.clone();
                    ffCloned.setName("nest_field_" + ffCloned.getName() + "_" + rowId);
                    fieldsCloned.add(ffCloned);
                }

                String curTrHtml = tmpL + "<td align=\"center\" valign=\"middle\"><input name=\"chk" + nestFieldName + "\" type='checkbox' value='" + rowId + "'/><input name='rowId" + nestFieldName + "' type='hidden' value='" + rowId + "'/><input name='dataId" + nestFieldName + "' type='hidden' value='" + fdao.getId() + "'/></td><td class='td-no' style=\"display:" + (isNoShow==1?"":"none") + "\"></td>" + tmpR;
                // 改变其中控件的name值为nest_field_***_rowId
                NodeList nodeList = Parser.createParser(curTrHtml, "utf-8").parse(new TagNameFilter("tr"));
                Node trObj = nodeList.elementAt(0);
                trObj.accept(new NestTableNodeVisitor(nestFieldName, rowId, fdao, mapAlign));

                // 置背景色
                String cls = "";
                if (isAddHighlight==1) {
                    if (fdao.getCwsQuoteId() == 0) {
                        cls = "row-add";
                    } else {
                        cls = "row-pull";
                    }
                }
                TableRow tableRow = (TableRow)trObj;
                tableRow.setAttribute("class", cls);
                // 置表格单元格对齐
                TableColumn[] td = tableRow.getColumns();
                for (int k = 2; k < td.length; k++) {
                    td[k].setAttribute("align", fieldsAlign[k - 2]);
                }

                curTrHtml = trObj.toHtml();

                // 展开宏控件
                // curTrHtml = render.getContentMacroReplaced(fdao, curTrHtml, fieldsCloned);
                curTrHtml = "<table><tbody>" + curTrHtml + "</tbody></table>";
                curTrHtml = render.getContentMacroReplaced(fdao, curTrHtml, fieldsCloned);
                int pCur = curTrHtml.indexOf("<tbody>");
                int qCur = curTrHtml.indexOf("</tbody>");
                curTrHtml = curTrHtml.substring(pCur + "<tbody>".length(), qCur);

                // 调整对齐样式
                nodeList = Parser.createParser(curTrHtml, "utf-8").parse(new TagNameFilter("tr"));
                trObj = nodeList.elementAt(0);
                trObj.accept(new NestTableNodeAlignVisitor(nestFieldName, mapAlign));
                curTrHtml = trObj.toHtml();

                String[] r = render.rendForNestTable(curTrHtml, fieldsCloned, formId, false, fdao);
                outerHtmls.append(r[1]);
                scripts.append(r[2]);

                // 将不可写的表单域disable
                if (actionId!=-1) {
                    for (FormField ff : disabledAndHidedFields[0]) {
                        boolean isFound = false;
                        FormField ffCloned = null;
                        for (Object o : fieldsCloned) {
                            ffCloned = (FormField) o;
                            if (ffCloned.getName().equals("nest_field_" + ff.getName() + "_" + rowId)) {
                                isFound = true;
                                break;
                            }
                        }

                        if (!isFound) {
                            DebugUtil.e(getClass(), "不可写字段：", ff.getName() + " 未找到");
                            continue;
                        }

                        if (ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu != null) {
                                IFormMacroCtl ifmc = mu.getIFormMacroCtl();
                                ifmc.setIFormDAO(fdao);
                                scriptsForDisableAndHide.append(ifmc.getDisableCtlScript(ffCloned, formId));
                            }
                        } else {
                            scriptsForDisableAndHide.append(FormField.getDisableCtlScript(ffCloned, formId));
                        }
                    }

                    for (FormField ff : disabledAndHidedFields[1]) {
                        boolean isFound = false;
                        FormField ffCloned = null;
                        for (Object o : fieldsCloned) {
                            ffCloned = (FormField) o;
                            if (ffCloned.getName().equals("nest_field_" + ff.getName() + "_" + rowId)) {
                                isFound = true;
                                break;
                            }
                        }

                        if (!isFound) {
                            DebugUtil.e(getClass(), "隐藏字段：", ff.getName() + " 未找到");
                            continue;
                        }

                        if (ff.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                            if (mu != null) {
                                scriptsForDisableAndHide.append(mu.getIFormMacroCtl().getHideCtlScript(ffCloned, formId));
                            }
                        } else {
                            scriptsForDisableAndHide.append(FormField.getHideCtlScript(ffCloned, formId));
                        }
                    }
                }

                String trTmp = r[0];
                // 将textarea转为input，清除 cws_span_***;
                trTmp = nestTableCtl.convertTextarea2InputAndClearCwsSpan(trTmp);

                // 插入在最后一个</table>之前
                p = viewForm.lastIndexOf("</tbody>");
                if (p==-1) {
                    p = viewForm.lastIndexOf("</table>");
                }
                if (p==-1) {
                    out.print("嵌套表格视图格式非法");
                    return;
                }
                String tmpStrLeft = viewForm.substring(0, p);
                String tmpStrRight = viewForm.substring(p);
                viewForm = tmpStrLeft + trTmp + tmpStrRight;

                rowId++;
            }

            // 生成合计字段行
            if (isPropStat) {
                Iterator irFields = fields.iterator();
                while (irFields.hasNext()) {
                    FormField formField = (FormField) irFields.next();
                    boolean isFound = false;

                    Iterator ir3 = jsonPropStat.keys();
                    while (ir3.hasNext()) {
                        String fieldName = (String) ir3.next();

                        if (formField.getName().equals(fieldName)) {
                            isFound = true;
                            String modeStat = jsonPropStat.getString(fieldName);

                            FormField ff = fd.getFormField(fieldName);
                            if (ff == null) {
                                DebugUtil.e(getClass(), "合计字段", "field " + fieldName + " is not exist");
                            }
                            int fieldType = ff.getFieldType();
                            String align = mapAlign.get(fieldName);
                            if ("center".equals(align)) {
                                align = "";
                            }
                            double sumVal = 0;
                            if (rowId > 0) {
                                sumVal = FormSQLBuilder.getSUMOfSQL(sql, fieldName);
                            }
                            if ("0".equals(modeStat)) {
                                if (fieldType == FormField.FIELD_TYPE_INT
                                        || fieldType == FormField.FIELD_TYPE_LONG) {
                                    trStatHtml.append("<td align='" + mapAlign.get(fieldName) + "'><input id='cws_stat_" + fieldName + "' title='合计' style='text-align:" + align + "' readonly kind='CALCULATOR' formula='sum(nest." + fieldName + ")' isroundto5='1' digit='2' class='input-stat' value='" + (long) sumVal + "'/></td>");
                                } else {
                                    trStatHtml.append("<td align='" + mapAlign.get(fieldName) + "'><input id='cws_stat_" + fieldName + "' title='合计' style='text-align:" + align + "' readonly kind='CALCULATOR' formula='sum(nest." + fieldName + ")' isroundto5='1' digit='2' class='input-stat' value='" + NumberUtil.round(sumVal, 2) + "'/></td>");
                                }
                            } else if (modeStat.equals("1")) {
                                trStatHtml.append("<td align='" + mapAlign.get(fieldName) + "'><input id='cws_stat_" + fieldName + "' title='平均' style='text-align:" + align + "' readonly class='input-stat' value='" + NumberUtil.round(sumVal / rowId, 2) + "'/></td>");
                            }
                        }
                    }

                    if (!isFound) {
                        trStatHtml.append("<td></td>");
                    }
                }

                trStatHtml.insert(0, "<tr id='trStat'><td></td><td align='center'>合计</td>");
                trStatHtml.append("</tr>");

                // 如果已经存在记录，则插入于表格中
                if (rowId > 0) {
                    // 插入在最后一个</table>之前
                    p = viewForm.lastIndexOf("</tbody>");
                    if (p==-1) {
                        p = viewForm.lastIndexOf("</table>");
                    }
                    if (p==-1) {
                        out.print("嵌套表格视图格式非法");
                        return;
                    }
                    String tmpStrLeft = viewForm.substring(0, p);
                    String tmpStrRight = viewForm.substring(p);
                    viewForm = tmpStrLeft + trStatHtml + tmpStrRight;
                }
            }
        }
        else if ("add".equals(op)) {
            // 找到第一个td，在之前插入checkbox、rowId及记录的ID
            int p = trHtml.indexOf("<td");
            String tmpL = trHtml.substring(0, p);
            String tmpR = trHtml.substring(p);
            trHtml = tmpL + "<td align=\"center\" valign=\"middle\"><input name=\"chk" + nestFieldName + "\" type='checkbox'/><input name='rowId" + nestFieldName + "' type='hidden'/><input name='dataId" + nestFieldName + "' type='hidden' value=''/></td><td class='td-no' style=\"display:" + (isNoShow==1?"":"none") + "\"></td>" + tmpR;

            if (isPropStat) {
                for (FormField formField : fields) {
                    boolean isFound = false;

                    Iterator ir3 = jsonPropStat.keys();
                    while (ir3.hasNext()) {
                        String fieldName = (String) ir3.next();

                        if (formField.getName().equals(fieldName)) {
                            isFound = true;

                            String modeStat = jsonPropStat.getString(fieldName);
                            FormField ff = fd.getFormField(fieldName);
                            if (ff == null) {
                                DebugUtil.e(getClass(), "合计字段", "field " + fieldName + " is not exist");
                            }
                            String align = mapAlign.get(fieldName);
                            if ("center".equals(align)) {
                                align = "";
                            }
                            int fieldType = ff.getFieldType();
                            if ("0".equals(modeStat)) {
                                if (fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_LONG) {
                                    trStatHtml.append("<td align='" + mapAlign.get(fieldName) + "'><input id='cws_stat_" + fieldName + "' title='合计' style='text-align:" + align + "' readonly kind='CALCULATOR' formula='sum(nest." + fieldName + ")' isroundto5='1' digit='2' class='input-stat' value='0.00'/></td>");
                                } else {
                                    trStatHtml.append("<td align='" + mapAlign.get(fieldName) + "'><input id='cws_stat_" + fieldName + "' title='合计' style='text-align:" + align + "' readonly kind='CALCULATOR' formula='sum(nest." + fieldName + ")' isroundto5='1' digit='2' class='input-stat' value='0.00'/></td>");
                                }
                            } else if ("1".equals(modeStat)) {
                                trStatHtml.append("<td align='" + mapAlign.get(fieldName) + "'><input id='cws_stat_" + fieldName + "' title='平均' style='text-align:" + align + "' readonly class='input-stat' value='0.00'/></td>");
                            }
                            break;
                        }
                    }

                    if (!isFound) {
                        trStatHtml.append("<td></td>");
                    }
                }

                trStatHtml.insert(0, "<tr id='trStat'><td></td><td style=\"display: " + (isNoShow==1?"":"none") + "\"></td>"); // 复选框列及序号列
                trStatHtml.append("</tr>");
            }
        }

        out.print(viewForm + outerHtmls.toString() + scripts.toString());

        // 对已有记录的字段作不可写及隐藏处理
        out.print("<script>$(function() {" + scriptsForDisableAndHide.toString() + "});</script>");

        // 创建辅助表格，其tr将用于添加新行
        // 补齐table、tbody，否则trHtml的格式为：<tr><td>...</tr>，jsoup会去掉tr、td
        trHtml = "<table><tbody>" + trHtml + "</tbody></table>";
        trHtml = render.getContentMacroReplaced(null, trHtml, fields);
        int p = trHtml.indexOf("<tbody>");
        int q = trHtml.indexOf("</tbody>");
        trHtml = trHtml.substring(p + "<tbody>".length(), q);

        // 调整对齐样式，不能对tableHtml作accept处理，HtmlParser解析会混乱
        NodeList nodeList = Parser.createParser(trHtml, "utf-8").parse(new TagNameFilter("tr"));
        Node trObj = nodeList.elementAt(0);
        // 置表格单元格对齐
        TableColumn[] td = ((TableRow)trObj).getColumns();
        for (int k = 2; k < td.length; k++) {
            td[k].setAttribute("align", fieldsAlign[k - 2]);
        }
        // 置控件对齐
        trObj.accept(new NestTableNodeAlignVisitor(nestFieldName, mapAlign));
        trHtml = trObj.toHtml();

        // 组装辅助表格
        String[] r = render.rendForNestTable("<table id='tableHelper'>" + trHtml + "</table>", fields, formId, true, null);
        tableHtml = r[0] + r[1] + r[2];

        scriptsForDisableAndHideForAdd = new StringBuffer();
        if (actionId!=-1) {
            for (FormField ff : disabledAndHidedFields[0]) {
                FormField ffCloned = (FormField)ff.clone();
                ffCloned.setValue("");
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        scriptsForDisableAndHideForAdd.append(mu.getIFormMacroCtl().getDisableCtlScript(ffCloned, formId));
                    }
                } else {
                    scriptsForDisableAndHideForAdd.append(FormField.getDisableCtlScript(ffCloned, formId));
                }
            }
            for (FormField ff : disabledAndHidedFields[1]) {
                FormField ffCloned = (FormField)ff.clone();
                ffCloned.setValue("");
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
                    if (mu != null) {
                        scriptsForDisableAndHideForAdd.append(mu.getIFormMacroCtl().getHideCtlScript(ffCloned, formId));
                    }
                } else {
                    scriptsForDisableAndHideForAdd.append(FormField.getHideCtlScript(ffCloned, formId));
                }
            }
        }

        // 将textarea转为input，清除cws_span_***
        tableHtml = nestTableCtl.convertTextarea2InputAndClearCwsSpan(tableHtml);

        // 对新增行中的字段作不可写及隐藏处理
        tableHtml += "<script>" + scriptsForDisableAndHideForAdd.toString() + "</script>";

        sbTmpHtml.append("<form id='" + formId + "'>" + tableHtml + "</form>");
    } catch (ParserException e) {
        e.printStackTrace();
    }

    SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);

    // 必须要escape，因为其中有脚本，可能会带来影响
%>
<code id="tmpTableHtml<%=nestFieldName%>" style="display: none"><%=StrUtil.escape(sbTmpHtml.toString())%></code>
<script>
    var rowId<%=nestFieldName%> = <%=rowId%>;
    var $nestTr<%=nestFieldName%>;

    function getIframeContent<%=nestFieldName%>() {
        return unescape($('#tmpTableHtml<%=nestFieldName%>').html());
    }

    function setNestTr<%=nestFieldName%>(html) {
        // 重新生成元素，否则因为之后删除了iframe，IE中会报没有权限
        $nestTr<%=nestFieldName%> = $(html);

        <%
        if (isAddHighlight==1) {
        %>
        $nestTr<%=nestFieldName%>.addClass('row-add');
        <%
        }
        %>
    }

    // 如果没有则创建，因为当配置了自动拉单时，会在拉单时再次调用nest_table_view.jsp
    if (!$('#iframe<%=nestFieldName%>')[0]) {
        // 在containerDiv获取辅助表格的tr，以免当存在与主表同名的字段时，scriptsForDisableAndHideForAdd中的脚本在disable时会带来影响
        var html = getIframeContent<%=nestFieldName%>();
        var containerDiv = document.createElement("DIV");
        containerDiv.innerHTML = '<div id="container<%=nestFieldName%>">' + html + '</div>';
        var $nestTr = $(containerDiv).find("#tableHelper tr");
        setNestTr<%=nestFieldName%>($nestTr.prop('outerHTML'));
    }

    // 初始化日期控件
    $(function() {
        // console.log("init nest table");
        // 加入工具条
        var toolbar = "<div id='toolbar_<%=nestFieldName%>' class='nest-toolbar'>";
        <%
        if (("edit".equals(op) || "add".equals(op)) && canAdd) {
        %>
        toolbar += "<span class='nest-btn' onclick='addNestTr<%=nestFieldName%>()'><i class=\"fa fa-plus-circle link-icon link-icon-add\"></i>添加</span>";
        <%
        }

        if (("edit".equals(op) || "add".equals(op)) && canDel) {
        %>
        toolbar += "<span class='nest-btn' onclick='delNestTr<%=nestFieldName%>()'><i class=\"fa fa-trash-o link-icon link-icon-del\"></i>删除</span>";
        <%
        }

        if ("edit".equals(op) && canImport) {
        %>
        toolbar += "<span class='nest-btn' onclick='importExcel<%=nestFieldName%>()'><i class=\"fa fa-arrow-circle-o-down link-icon link-icon-edit\"></i>导入</span>";
        <%
        }

        if ("edit".equals(op) && canExport) {
        %>
        toolbar += "<span class='nest-btn' onclick='exportExcel<%=nestFieldName%>()'><i class=\"fa fa-arrow-circle-o-up link-icon link-icon-edit\"></i>导出</span>";
        <%
        }

	    // 模块添加页面不支持拉单，因为拉单后会刷新嵌套表格，而此时parentId为-1，无法关联记录
        if ("edit".equals(op) && canSel) {
            if (queryId!=-1) {
        %>
            toolbar += "<span class='nest-btn' onclick='sel<%=nestFieldName%>(<%=cwsId%>, true)'><i class=\"fa fa-check-square-o link-icon link-icon-show\"></i>选择</span>";
        <%
            }
            else {
        %>
            toolbar += "<span class='nest-btn' onclick='sel<%=nestFieldName%>(<%=cwsId%>)'><i class=\"fa fa-check-square-o link-icon link-icon-show\"></i>选择</span>";
            <%
            }
        }
        %>
        toolbar += "</div>";
        $("#nestTable_<%=nestFieldName%>").before(toolbar);

        // 增加checkbox列
        var tdHeaderCheckbox = "<td width=\"30\" rowspan=\"<%=headerRows%>\" align=\"center\" valign=\"middle\"><input id='chk<%=nestFieldName%>' type='checkbox'/></td><td rowspan=\"<%=headerRows%>\" style=\"display: <%=isNoShow==1?"":"none"%>\" width='45' align='center'>序号</td>";
        $("#nestTable_<%=nestFieldName%> tr").first().prepend(tdHeaderCheckbox);
        $('#chk<%=nestFieldName%>').click(function() {
            if ($(this).prop("checked")) {
                $('#nestTable_<%=nestFieldName%>').find("input[name='chk<%=nestFieldName%>'][type=checkbox]").prop("checked", true);
            }
            else {
                $('#nestTable_<%=nestFieldName%>').find("input[name='chk<%=nestFieldName%>'][type=checkbox]").prop("checked", false);
            }
        });

        <%
        if (!"add".equals(op)) {
        %>
        // 初始化表格中已有的数据行
        var headerRows = <%=headerRows%>;
        $("#nestTable_<%=nestFieldName%> tr").each(function(index) {
            if (index >= headerRows) {
                initTr('<%=nestFieldName%>', $(this), index - headerRows, false);
            }
        });
        <%
        }
        %>

        $('#toolbar_<%=nestFieldName%>').on('mouseover', '.nest-btn', function(e) {
            $(this).addClass("nest-btn-hover");
        });
        $('#toolbar_<%=nestFieldName%>').on('mouseout', '.nest-btn', function(e) {
            $(this).removeClass("nest-btn-hover");
        });

        // 初始化序号
        initTdNo();
    });

    var isTrStatAdded = false;
    <%
    if (rowId > 0) {
    %>
        isTrStatAdded = true;
    <%
    }
    %>
    // 添加行
    function addNestTr<%=nestFieldName%>() {
        if (<%=isPropStat%>) {
            // 在统计行之前插入
            var $curLastTr = $('#nestTable_<%=nestFieldName%> tr').last();
            // 在发起流程时表格一开始为空，没有统计行，即只有表头那一行，其prev()为undefined
            if (!$curLastTr.prev()[0]) {
                $curLastTr.after($nestTr<%=nestFieldName%>.clone());
                $lastTr = $("#nestTable_<%=nestFieldName%> tr").last();
            }
            else {
                $('#nestTable_<%=nestFieldName%> tr').last().prev().after($nestTr<%=nestFieldName%>.clone());
                $lastTr = $("#nestTable_<%=nestFieldName%> tr").last().prev();
            }
            // clone后得到的tr需要再次初始化日期控件
            initTr('<%=nestFieldName%>', $lastTr, rowId<%=nestFieldName%>, true);
        }
        else {
            $('#nestTable_<%=nestFieldName%>').append($nestTr<%=nestFieldName%>.clone());
            $lastTr = $("#nestTable_<%=nestFieldName%> tr").last();
            // clone后得到的tr需要再次初始化日期控件
            initTr('<%=nestFieldName%>', $lastTr, rowId<%=nestFieldName%>, true);
        }

        // 添加统计行，注意只能添加一次
        <%
            if (isPropStat) {
        %>
        if (!isTrStatAdded) {
            $lastTr.after("<%=trStatHtml.toString()%>");
            isTrStatAdded = true;
        }
        <%
            }
        %>
        rowId<%=nestFieldName%> ++;
        // 初始化序号
        initTdNo();

        bindNestTableMouseEvent();
    }

    function delTr($trs) {
        var ids = '';
        var nestFieldName = "<%=nestFieldName%>";
        $trs.each(function () {
            var id = $('#nest_field_dataId' + nestFieldName + '_' + $(this).val()).val();
            if (id == undefined) {
                // 如果为空，则为新增的记录，而非选择的记录，直接删除
                $(this).parent().parent().remove();
                initTdNo();
                callCalculateOnloadNestTable('nestTable_<%=nestFieldName%>');
                return;
            }
            if (ids == '') {
                ids = id;
            }
            else {
                ids += ',' + id;
            }
        });
        if (ids == '') {
            return;
        }
        var ajaxData = {
            ids: ids,
            formCode: "<%=nestFormCode%>"
        };
        let path = '/flow/macro/delNestTableRows';
        ajaxPost(path, ajaxData).then((data) => {
            // console.log('data', data);
            if (data.ret=="1") {
                // 删除原在数据库中已存在的行
                $trs.each(function () {
                    $(this).parents("tr:eq(0)").remove();
                });

                initTdNo();
                callCalculateOnloadNestTable('nestTable_<%=nestFieldName%>');
            }
            else {
                myMsg(data.msg);
            }
        });
    }

    // 删除行
    function delNestTr<%=nestFieldName%>() {
        var $trs = $("input[type=checkbox][name='chk<%=nestFieldName%>']:checked");
        if ($trs.length == 0) {
            myMsg('请选择记录', 'warning');
            return;
        }
        myConfirm('提示', '您确定要删除么', function() { delTr($trs) });
    }

    function importExcel<%=nestFieldName%>() {
        // openWin("<%=request.getContextPath()%>/visual/nest_table_import_excel.jsp?flowId=<%=flowId%>&parentId=<%=cwsId%>&parentFormCode=<%=parentFormCode%>&formCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=nestFieldName%>", 480, 110);
        openImportExcelModal("<%=cwsId%>", "<%=formCode%>", "<%=parentFormCode%>", <%=flowId%>, "<%=nestFieldName%>", "nest_table");
    }

    function exportExcel<%=nestFieldName%>() {
        // openWin('<%=request.getContextPath()%>/visual/exportExcelRelate.do?parentId=<%=cwsId%>&formCode=<%=ParamUtil.get(request, "parentFormCode")%>&formCodeRelated=<%=formCode%>&nestType=<%=MacroCtlUnit.NEST_TYPE_TABLE%>&nestFieldName=<%=nestFieldName%>', 480, 320);
        exportExcelRelate(
            '<%=MacroCtlUnit.NEST_TYPE_NORMAIL%>',
            '<%=cwsId%>',
            '<%=parentFormCode%>',
            '<%=formCode%>',
            '<%=fd.getName()%>',
        )
    }

    function sel<%=nestFieldName%>(parentId, isQuery) {
        if (isQuery) {
            var fieldParams = "";
            <%
            if (queryId!=-1) {
                // 取得查询脚本中需从request中获取的字段值
                QueryScriptUtil qsu = new QueryScriptUtil();
                for (String fName : qsu.parseField(queryId, parentFormCode)) {
            %>
                fieldParams += '&<%=fName%>=' + encodeURI(o('<%=fName%>').value);
            <%
                }
            }
            %>
            openWin("<%=request.getContextPath()%>/flow/form_query_script_list_do.jsp?op=query&flowId=<%=flowId%>&id=<%=queryId%>&mode=sel&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_table&parentId=" + parentId + fieldParams, 800, 600);
        }
        else {
            // openWin("<%=request.getContextPath()%>/visual/moduleListNestSel.do?parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFormCode=<%=StrUtil.UrlEncode(formCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&nestType=nest_table&parentId=" + parentId + "&mainId=" + parentId, 800, 600);
            <%
                String condFields = String.join("|", ModuleUtil.getModuleListNestSelCondFields(nestFilter));
            %>
            openWinModuleListNest("<%=parentFormCode%>", "<%=formCode%>", "<%=nestFieldName%>", "nest_table", parentId, parentId, "<%=condFields%>");
        }
    }

    // 初始化序号
    function initTdNo() {
        var k = 1;
        $("#nestTable_<%=nestFieldName%> tr").each(function (index) {
            var $tdNo = $(this).find('.td-no');
            if ($tdNo[0]) {
                $tdNo.html(k);
                k++;
            }
        });
    }

    // $(function() {
        initNestTableCalculate('nestTable_<%=nestFieldName%>');

        bindNestTableMouseEvent();
    // })

    $('input, select, textarea').each(function() {
        $(this).attr('autocomplete', 'off');
    });
</script>