package com.cloudweb.oa.cond;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormMgr;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.visual.SQLBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public class FieldCondUnit extends CondUnit {

    public FieldCondUnit(HttpServletRequest request, FormDb fd, String fieldName, String fieldTitle, String condType, Map<String, String> checkboxGroupMap, ArrayList<String> list, String queryValue) {
        super(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
    }

    @Override
    public void init() {
        FormMgr fm = new FormMgr();
        FormField ff = null;
        String title = "";
        FormDb someFd = fd;
        boolean isSub = false;
        FormDb subFormDb = null;

        if (fieldName.startsWith("main:")) { // 关联的主表
            String[] aryField = StrUtil.split(fieldName, ":");
            String field = fieldName.substring(5);
            if (aryField.length == 3) {
                FormDb mainFormDb = fm.getFormDb(aryField[1]);
                ff = mainFormDb.getFormField(aryField[2]);
                if (ff == null) {
                    fieldTitle = fieldName + "不存在";
                    return;
                }
                title = ff.getTitle();
            } else {
                fieldTitle = field + " 不存在";
                return;
            }
        } else if (fieldName.startsWith("other:")) { // 映射的字段，多重映射不支持
            String[] aryField = StrUtil.split(fieldName, ":");
            if (aryField.length < 5) {
                fieldTitle = "<font color='red'>格式非法</font>";
                return;
            } else {
                FormDb otherFormDb = fm.getFormDb(aryField[2]);
                someFd = otherFormDb;
                String showFieldName = aryField[4];
                // 映射字段，显示ID
                if ("id".equalsIgnoreCase(showFieldName)) {
                    title = otherFormDb.getName() + "ID";
                    ff = new FormField();
                    ff.setTitle(title);
                    ff.setType(FormField.TYPE_TEXTFIELD);
                    ff.setFieldType(FormField.FIELD_TYPE_LONG);
                } else {
                    ff = otherFormDb.getFormField(aryField[4]);
                    if (ff == null) {
                        fieldTitle = fieldName + "不存在";
                        return;
                    }
                    title = ff.getTitle();
                }
            }
        } else if (fieldName.startsWith("sub:")) { // 关联的子表
            isSub = true;
            String[] aryField = StrUtil.split(fieldName, ":");
            String field = fieldName.substring(5);
            if (aryField.length == 3) {
                subFormDb = fm.getFormDb(aryField[1]);
                someFd = subFormDb;
                ff = subFormDb.getFormField(aryField[2]);
                if (ff == null) {
                    fieldTitle = fieldName + "不存在";
                    return;
                }
                if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                    String desc = StrUtil.getNullStr(ff.getDescription());
                    if (!"".equals(desc)) {
                        title = desc;
                    } else {
                        title = ff.getTitle();
                    }
                    String chkGroup = StrUtil.getNullStr(ff.getDescription());
                    if (!"".equals(chkGroup)) {
                        if (!checkboxGroupMap.containsKey(chkGroup)) {
                            checkboxGroupMap.put(chkGroup, "");
                        } else {
                            fieldTitle = null;
                            return;
                        }
                    }
                } else {
                    title = ff.getTitle();
                }
            } else {
                title = field + " 不存在";
            }
        } else {
            ff = fd.getFormField(fieldName);
            if (ff == null) {
                fieldTitle = fieldName + "不存在";
                return;
            }

            if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                String desc = StrUtil.getNullStr(ff.getDescription());
                if (!"".equals(desc)) {
                    title = desc;
                } else {
                    title = ff.getTitle();
                }
                String chkGroup = StrUtil.getNullStr(ff.getDescription());
                if (!"".equals(chkGroup)) {
                    if (!checkboxGroupMap.containsKey(chkGroup)) {
                        checkboxGroupMap.put(chkGroup, "");
                    } else {
                        fieldTitle = null;
                        return;
                    }
                }
            } else {
                title = ff.getTitle();
            }
        }
        // 用于给convertToHTMLCtlForQuery辅助传值
        ff.setCondType(condType);

        if ("#".equals(fieldTitle)) {
            fieldTitle = title;
        }

        if (ff.getType().equals(FormField.TYPE_MACRO)) {
            try {
                String[] ary = getMacro(ff);
                html = ary[0];
                script = ary[1];
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_INT || ff.getFieldType() == FormField.FIELD_TYPE_DOUBLE || ff.getFieldType() == FormField.FIELD_TYPE_FLOAT || ff.getFieldType() == FormField.FIELD_TYPE_LONG || ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
            String[] ary = getNumeric(ff);
            html = ary[0];
            script = ary[1];
        }
        else if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
            CondUnit condUnit = new DateCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, dateFieldNamelist, queryValue);
            html = condUnit.getHtml();
            script = condUnit.getScript();
        }
        else {
            String[] ary = getElse(ff, someFd, isSub, subFormDb);
            html = ary[0];
            script = ary[1];
        }
    }

    public String[] getElse(FormField ff, FormDb someFd, boolean isSub, FormDb subFormDb) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbScript = new StringBuilder();
        boolean isSpecial = false;
        if (condType.equals(SQLBuilder.COND_TYPE_NORMAL)) {
            if (ff.getType().equals(FormField.TYPE_SELECT)) {
                isSpecial = true;
                sb.append("<input name=\"" + fieldName + "_cond\" value=\"" + condType + "\" type=\"hidden\" />");
                sb.append("<select id=\"" + fieldName + "\" name=\"" + fieldName + "\">");
                sb.append(FormParser.getOptionsOfSelect(someFd, ff));
                sb.append("</select>");
                sbScript.append("$(document).ready(function() {\n");
                sbScript.append("o(\"" + fieldName + "\").value = \"" + queryValue + "\";\n");
                sbScript.append("});\n");
            } else if (ff.getType().equals(FormField.TYPE_RADIO)) {
                isSpecial = true;
                sb.append("<input name=\"" + fieldName + "_cond\" value=\"" + condType + "\" type=\"hidden\" />");
                String[][] aryRadio = FormParser.getOptionsArrayOfRadio(someFd, ff);
                for (int k = 0; k < aryRadio.length; k++) {
                    String val = aryRadio[k][0];
                    String text = aryRadio[k][1];
                    sb.append("<input type=\"radio\" id=\"" + fieldName + "\" name=\"" + fieldName + "\" value=\"" + val + "\"/>" + text + "");
                }
            } else if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
                isSpecial = true;
                String[][] aryChk = null;
                aryChk = FormParser.getOptionsArrayOfCheckbox(someFd, ff);
                for (int k = 0; k < aryChk.length; k++) {
                    String val = aryChk[k][0];
                    String fName = aryChk[k][1];
                    if (isSub) {
                        fName = "sub:" + subFormDb.getCode() + ":" + fName;
                    }
                    String text = aryChk[k][2];
                    queryValue = ParamUtil.get(request, fName);

                    sb.append("<input name=\"" + fName + "_cond\" value=\"" + condType + "\" type=\"hidden\" />");
                    sb.append("<input type=\"checkbox\" id=\"" + fName + "\" name=\"" + fName + "\" value=\"" + val + "\" style=\"" + (aryChk.length > 1 ? "width:20px" : "") + "\"/>");

                    sbScript.append("$(function() {\n");
                    sbScript.append("o('" + fName + "').checked = " + (queryValue.equals(val) ? "true" : "false") + ";\n");
                    sbScript.append("})\n");

                    if (aryChk.length > 1) {
                        sb.append(text);
                    }
                }
            }
        } else if (condType.equals(SQLBuilder.COND_TYPE_MULTI)) {
            if (ff.getType().equals(FormField.TYPE_SELECT)) {
                isSpecial = true;
                sb.append("<input name=\"" + fieldName + "_cond\" value=\"" + condType + "\" type=\"hidden\" />");
                String[][] aryOpt = FormParser.getOptionsArrayOfSelect(someFd, ff);
                for (int k = 0; k < aryOpt.length; k++) {
                    if ("".equals(aryOpt[k][1].trim())) {
                        aryOpt[k][0] = "无";
                    }
                    sb.append("<input name=\"" + fieldName + "\" type=\"checkbox\" value=\"" + aryOpt[k][1] + "\" style=\"width:20px\"/>" + aryOpt[k][0] + "");
                }
                String[] aryVal = ParamUtil.getParameters(request, fieldName);
                if (aryVal != null) {
                    for (String s : aryVal) {
                        if ("".equals(queryValue)) {
                            queryValue = s;
                        } else {
                            queryValue += "," + s;
                        }
                    }
                }
                if (!"".equals(queryValue)) {
                    sbScript.append("$(document).ready(function() {\n");
                    sbScript.append("setMultiCheckboxChecked(\"" + fieldName + "\", \"" + queryValue + "\");\n");
                    sbScript.append("});\n");
                }
            }
        }
        if (!isSpecial) {
            sb.append("<input name=\"" + fieldName + "_cond\" value=\"" + condType + "\" type=\"hidden\" />");
            sb.append("<input id=\"field" + fieldName + "\" name=\"" + fieldName + "\" size=\"5\" />");
            sb.append("<a id=\"arrow" + fieldName + "\" href=\"javascript:;\"><i class=\"fa fa-caret-down\"></i></a>");
            sbScript.append("$(document).ready(function() {\n");
            sbScript.append("o(\"" + fieldName + "\").value = \"" + queryValue + "\";\n");

            // 使=空或者<>空，获得焦点时即为选中状态，以便于修改条件的值
            sbScript.append("$(\"#field" + fieldName + "\").focus(function() {\n");
            sbScript.append("if ($(this).val()=='" + SQLBuilder.IS_EMPTY + "' || $(this).val()=='" + SQLBuilder.IS_NOT_EMPTY + "') {\n");
            sbScript.append("this.select();\n");
            sbScript.append("}\n");
            sbScript.append("});\n");

            sbScript.append("var menu = new BootstrapMenu('#arrow" + fieldName + "', {\n");
            sbScript.append("menuEvent: 'click',\n");
            sbScript.append("actions: [{\n");
            sbScript.append("   name: '等于空',\n");
            sbScript.append("   onClick: function() {\n");
            sbScript.append("       $('#field" + fieldName + "').val('" + SQLBuilder.IS_EMPTY + "');\n");
            sbScript.append("   }\n");
            sbScript.append("}, {\n");
            sbScript.append("   name: '不等于空',\n");
            sbScript.append("   onClick: function() {\n");
            sbScript.append("       $('#field" + fieldName + "').val('" + SQLBuilder.IS_NOT_EMPTY + "');\n");
            sbScript.append("   }\n");
            sbScript.append("}]\n");
            sbScript.append("});\n");
            sbScript.append("});\n");
        }
        String[] ary = new String[2];
        ary[0] = sb.toString();
        ary[1] = sbScript.toString();
        return ary;
    }

    public String[] getNumeric(FormField ff) {
        String nameCond = ParamUtil.get(request, fieldName + "_cond");
        if ("".equals(nameCond)) {
            nameCond = condType;
        }

        StringBuilder sb = new StringBuilder();
        StringBuilder sbScript = new StringBuilder();

        if (condType.equals(SQLBuilder.COND_TYPE_SCOPE)) {
            String fCond = ParamUtil.get(request, fieldName + "_cond_from");
            String tCond = ParamUtil.get(request, fieldName + "_cond_to");
            String fVal = ParamUtil.get(request, fieldName + "_from");
            String tVal = ParamUtil.get(request, fieldName + "_to");

            sb.append("<input name=\"" + fieldName + "_cond\" value=\"" + condType + "\" type=\"hidden\" />");
            sb.append("<select name=\"" + fieldName + "_cond_from\">");
            sb.append("<option value=\">\">></option>");
            sb.append("<option value=\">=\" selected=\"selected\">>=</option></option>");
            sb.append("</select>");
            sb.append("<input name=\"" + fieldName + "_from\" size=\"3\" />");
            sb.append("<select name=\"" + fieldName + "_cond_to\">");
            sb.append("<option value=\"&lt;\"><</option>");
            sb.append("<option value=\"&lt;=\" selected=\"selected\"><=</option></option>");
            sb.append("</select>");
            sb.append("<input name=\"" + fieldName + "_to\" size=\"3\" />");

            sbScript.append("$(document).ready(function() {\n");
            sbScript.append("o(\"" + fieldName + "_cond_from\").value = \"" + fCond + "\";\n");
            sbScript.append("o(\"" + fieldName + "_from\").value = \"" + fVal + "\";\n");
            sbScript.append("o(\"" + fieldName + "_cond_to\").value = \"" + tCond + "\";\n");
            sbScript.append("o(\"" + fieldName + "_to\").value = \"" + tVal + "\";\n");
            sbScript.append("});\n");
        } else {
            sb.append("<select name=\"" + fieldName + "_cond\">");
            sb.append("<option value=\"=\" selected=\"selected\">=</option>");
            sb.append("<option value=\">\">></option>");
            sb.append("<option value=\"&lt;\"><</option>");
            sb.append("<option value=\">=\">>=</option></option>");
            sb.append("<option value=\"&lt;=\"><=</option>");
            sb.append("</select>");
            sb.append("<input name=\"" + fieldName + "\" size=\"5\" />");

            sbScript.append("$(document).ready(function() {\n");
            sbScript.append("o(\"" + fieldName + "_cond\").value = \"" + nameCond + "\";\n");
            sbScript.append("o(\"" + fieldName + "\").value = \"" + queryValue + "\";\n");
            sbScript.append("});\n");
        }
        String[] ary = new String[2];
        ary[0] = sb.toString();
        ary[1] = sbScript.toString();
        return ary;
    }

    public String[] getMacro(FormField ff) throws CloneNotSupportedException {
        String[] ary = new String[2];
        MacroCtlMgr mm = new MacroCtlMgr();
        StringBuilder sb = new StringBuilder();
        StringBuilder sbScript = new StringBuilder();

        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
        if (mu != null) {
            String queryValueRealShow = ParamUtil.get(request, fieldName + "_realshow");
            // 用main及other映射字段的描述替换其name，以使得生成的查询控件的id及name中带有main及other
            FormField ffQuery = (FormField) ff.clone();
            ffQuery.setName(fieldName);
            IFormMacroCtl ifmc = mu.getIFormMacroCtl();
            int fieldType = ifmc.getFieldType(ff);
            if (fieldType == FormField.FIELD_TYPE_INT || fieldType == FormField.FIELD_TYPE_DOUBLE || fieldType == FormField.FIELD_TYPE_FLOAT || fieldType == FormField.FIELD_TYPE_LONG || fieldType == FormField.FIELD_TYPE_PRICE) {
                String nameCond = ParamUtil.get(request, fieldName + "_cond");
                if ("".equals(nameCond)) {
                    nameCond = condType;
                }
                if (condType.equals(SQLBuilder.COND_TYPE_SCOPE)) {
                    String fCond = ParamUtil.get(request, fieldName + "_cond_from");
                    String tCond = ParamUtil.get(request, fieldName + "_cond_to");
                    String fVal = ParamUtil.get(request, fieldName + "_from");
                    String tVal = ParamUtil.get(request, fieldName + "_to");

                    sb.append("<input name=\"" + fieldName + "_cond\" value=\"" + condType + "\" type=\"hidden\" />");
                    sb.append("<select name=\"" + fieldName + "_cond_from\">");
                    sb.append("<option value=\">\">></option>");
                    sb.append("<option value=\">=\" selected=\"selected\">>=</option></option>");
                    sb.append("</select>");

                    FormField ffTemp = (FormField) ffQuery.clone();
                    ffTemp.setName(ffQuery.getName() + "_from");
                    ffTemp.setCondType(ffQuery.getCondType());
                    sb.append(ifmc.convertToHTMLCtlForQuery(request, ffTemp));

                    sb.append("<select name=\"" + fieldName + "_cond_to\">");
                    sb.append("<option value=\"&lt;\"><</option>");
                    sb.append("<option value=\"&lt;=\" selected=\"selected\"><=</option></option>");
                    sb.append("</select>");

                    ffTemp.setName(ffQuery.getName() + "_to");
                    sb.append(ifmc.convertToHTMLCtlForQuery(request, ffTemp));

                    sbScript.append("$(document).ready(function() {\n");
                    sbScript.append("    o(\"" + fieldName + "_cond_from\").value = \"" + fCond + "\";\n");
                    sbScript.append("    o(\"" + fieldName + "_from\").value = \"" + fVal + "\";\n");
                    sbScript.append("    o(\"" + fieldName + "_cond_to\").value = \"" + tCond + "\";\n");
                    sbScript.append("    o(\"" + fieldName + "_to\").value = \"" + tVal + "\";\n");
                    sbScript.append("});\n");
                } else {
                    sb.append("<select name=\"" + fieldName + "_cond\">");
                    sb.append("<option value=\"=\" selected=\"selected\">=</option>");
                    sb.append("<option value=\">\">></option>");
                    sb.append("<option value=\"&lt;\"><</option>");
                    sb.append("<option value=\">=\">>=</option></option>");
                    sb.append("<option value=\"&lt;=\"><=</option>");
                    sb.append("</select>");

                    sb.append(ifmc.convertToHTMLCtlForQuery(request, ffQuery));

                    sbScript.append("$(document).ready(function() {\n");
                    sbScript.append("o(\"" + fieldName + "_cond\").value = \"" + nameCond + "\";\n");
                    sbScript.append("o(\"" + fieldName + "\").value = \"" + queryValue + "\";\n");
                    sbScript.append("});\n");
                }
            } else {
                sb.append(ifmc.convertToHTMLCtlForQuery(request, ffQuery));

                sb.append("<input name=\"" + fieldName + "_cond\" value=\"" + condType + "\" type=\"hidden\" />");

                if ("text".equals(ifmc.getControlType()) || "img".equals(ifmc.getControlType()) || "textarea".equals(ifmc.getControlType())) {
                    sb.append("<a id=\"arrow" + fieldName + "\" href=\"javascript:;\"><i class=\"fa fa-caret-down\"></i></a>");
                }

                sbScript.append("$(document).ready(function() {\n");
                // 如果是多选
                if (condType.equals(SQLBuilder.COND_TYPE_MULTI)) {
                    String[] aryVal = ParamUtil.getParameters(request, fieldName);
                    if (aryVal != null) {
                        for (String s : aryVal) {
                            if ("".equals(queryValue)) {
                                queryValue = s;
                            } else {
                                queryValue += "," + s;
                            }
                        }
                    }
                    if (!"".equals(queryValue)) {
                        sbScript.append("$(document).ready(function() {\n");
                        sbScript.append("    setMultiCheckboxChecked(\"" + fieldName + "\", \"" + queryValue + "\");\n");
                        sbScript.append("});\n");
                    }
                } else {
                    sbScript.append("if (o(\"" + fieldName + "\")) {\n");
                    sbScript.append("    o(\"" + fieldName + "\").value = \"" + queryValue + "\";\n");
                    sbScript.append("}\n");
                    sbScript.append("try {\n");
                    sbScript.append("    o(\"" + fieldName + "_realshow\").value = \"" + queryValueRealShow + "\";\n");
                    sbScript.append("} catch (e) {}\n");
                }
                if ("text".equals(ifmc.getControlType()) || "img".equals(ifmc.getControlType()) || "textarea".equals(ifmc.getControlType())) {
                    // 使=空或者<>空，获得焦点时即为选中状态，以便于修改条件的值
                    sbScript.append("$(\"input[name='" + fieldName + "']\").focus(function() {\n");
                    sbScript.append("    if ($(this).val()=='" + SQLBuilder.IS_EMPTY + "' || $(this).val()=='" + SQLBuilder.IS_NOT_EMPTY + "') {\n");
                    sbScript.append("        this.select();\n");
                    sbScript.append("    }\n");
                    sbScript.append("});\n");

                    sbScript.append("var menu = new BootstrapMenu('#arrow" + fieldName + "', {\n");
                    sbScript.append("menuEvent: 'click',\n");
                    sbScript.append("actions: [{\n");
                    sbScript.append("     name: '等于空',\n");
                    sbScript.append("     onClick: function() {\n");
                    sbScript.append("         o('" + fieldName + "').value = '" + SQLBuilder.IS_EMPTY + "';\n");
                    sbScript.append("     }\n");
                    sbScript.append("}, {\n");
                    sbScript.append("     name: '不等于空',\n");
                    sbScript.append("     onClick: function() {\n");
                    sbScript.append("         o('" + fieldName + "').value = '" + SQLBuilder.IS_NOT_EMPTY + "';\n");
                    sbScript.append("     }\n");
                    sbScript.append("}]\n");
                    sbScript.append("});\n");
                }
                sbScript.append("});\n");
            }
        }
        ary[0] = sb.toString();
        ary[1] = sbScript.toString();
        return ary;
    }
}
