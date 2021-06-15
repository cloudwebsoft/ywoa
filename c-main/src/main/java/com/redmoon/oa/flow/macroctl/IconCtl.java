package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.upgrade.service.SpringHelper;
import com.redmoon.oa.visual.SQLBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

/**
 * <p>Title: 利用JQuery raty实现的图标控件</p>
 *
 * <p>
 * </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class IconCtl extends AbstractMacroCtl {

    public IconCtl() {
    }

    /**
     * 用于列表中显示宏控件的值
     *
     * @param request    HttpServletRequest
     * @param ff         FormField
     * @param fieldValue String
     * @return String
     */
    @Override
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String v = StrUtil.getNullStr(fieldValue);

        String props = ff.getDescription();

        JSONObject jsonProps = null;
        try {
            jsonProps = JSONObject.parseObject(props);
        }
        catch (JSONException e) {
            return "控件描述格式非法";
        }

        JSONArray jsonArr = jsonProps.getJSONArray("options");
        if (jsonArr == null) {
            return "控件描述中选项格式非法";
        }

        boolean isOnlyIcon = true;
        if (jsonProps.containsKey("isOnlyIcon")) {
            isOnlyIcon = jsonProps.getBoolean("isOnlyIcon");
        }

        String iconUrl = "";
        JSONObject json = getByVal(jsonProps, v);
        if (json != null) {
            String name = json.getString("name");
            String icon = json.getString("icon");

            if (request==null || !"true".equals(request.getAttribute("isForExport"))) {
                iconUrl = "<img class='icon-ctl' src='" + Global.getRootPath() + "/images/icons/" + icon + "'/>";
                if (!isOnlyIcon) {
                    iconUrl += name;
                }
            }
            else {
                iconUrl = name;
            }
        }

        return iconUrl;
    }

    /**
     * @param request HttpServletRequest
     * @param ff      FormField
     * @return String
     */
    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        // 控件默认值：0,5（0表示默认不选，5表示5级）
        String props = ff.getDescription();
        JSONObject jsonProps = null;
        try {
            jsonProps = JSONObject.parseObject(props);
        }
        catch (JSONException e) {
            return "控件描述格式非法";
        }

        JSONArray jsonArr = jsonProps.getJSONArray("options");
        if (jsonArr == null) {
            return "控件描述中选项格式非法";
        }

        String style = "";
        if (!"".equals(ff.getCssWidth())) {
            style = "style='width:" + ff.getCssWidth() + "'";
        }

        StringBuffer sb = new StringBuffer();
        String strReadOnly = "";
        if (ff.isReadonly()) {
            strReadOnly = " readonly='readonly' ";
        }
        String defaultVal = null;
        sb.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "' " + style + strReadOnly + ">");
        sb.append("<option value=''>无</option>");
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject json = jsonArr.getJSONObject(i);
            boolean selected = false;
            if (json.containsKey("selected")) {
                selected = json.getBoolean("selected");
                if (selected) {
                    defaultVal = json.getString("value");
                }
            }
            String icon = json.getString("icon");
            sb.append("<option value=\"" + json.getString("value") + "\" " + (selected ? "selected" : "") + " style=\"background-image: url('" + request.getContextPath() + "/images/icons/" + icon + "');\">" + json.getString("name") + "</option>");
        }
        sb.append("</select>");

        if (request.getAttribute("isIconCtlJS_" + ff.getName()) == null) {
            String pageType = (String) request.getAttribute("pageType");
            sb.append("<script src='" + request.getContextPath()
                    + "/flow/macro/macro_js_icon_ctl.jsp?pageType=" + pageType
                    + "&formCode=" + StrUtil.UrlEncode(ff.getFormCode())
                    + "&fieldName=" + ff.getName() + "&isHidden=" + ff.isHidden() + "&editable=" + ff.isEditable()
                    + "'></script>\n");
            request.setAttribute("isIconCtlJS_" + ff.getName(), "y");
        }

        String pageType = StrUtil.getNullStr((String) request.getAttribute("pageType"));
        if (!pageType.contains("show")) {
            sb.append("<script>\n");
            sb.append("$(function() {\n");
            if (ff.isReadonly()) {
                // 顺序得在formatStatePrompt之前，否则会丢失图标
                sb.append("$('#" + ff.getName() + "').select2({'disabled':'readonly'});\n"); // 会被禁用
                // 注意select2的disabled同时会将隐藏的实际字段变为disabled，需将其置为非disabled，否则提交表单验证时可能会报错
                // sb.append("$('select[name=" + ff.getName() + "]').attr('disabled', false);\n"); // 无效，会取消只读
                // sb.append("o('" + ff.getName() + "').removeAttribute('disabled');\n"); // 也无效，同样会取消只读
                // sb.append("$('#" + ff.getName() + "').select2('readonly', true);\n"); // 会被禁用，且看不到图标
            }
            sb.append("     $('#" + ff.getName() + "').select2({templateResult: formatStatePrompt,templateSelection: formatStatePrompt});\n");
            sb.append("});\n");
            // 设置默认值
            if (null != defaultVal) {
                sb.append("$('#" + ff.getName() + "').val(['" + defaultVal +"']).trigger('change');\n");
            }
            sb.append("</script>");
        }
        return sb.toString();
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        if (ff.getCondType().equals(SQLBuilder.COND_TYPE_FUZZY)) {
            return super.convertToHTMLCtlForQuery(request, ff);
        }
        String props = ff.getDescription();
        JSONObject jsonProps = null;
        try {
            jsonProps = JSONObject.parseObject(props);
        }
        catch (JSONException e) {
            return "控件描述格式非法";
        }

        JSONArray jsonArr = jsonProps.getJSONArray("options");
        if (jsonArr == null) {
            return "控件描述中选项格式非法";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "'>");
        sb.append("<option value=''></option>");
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject json = jsonArr.getJSONObject(i);
            boolean selected = json.getBoolean("selected");
            String icon = json.getString("icon");
            sb.append("<option value=\"" + json.getString("value") + "\"" + (selected ? "selected" : "") + " style=\"background-image: url('" + request.getContextPath() + "/images/icons/" + icon + "');\">" + json.getString("name") + "</option>");
        }
        sb.append("</select>");
        return sb.toString();
    }

    public JSONObject getByVal(JSONObject jsonProps, String fieldValue) {
        String v = StrUtil.getNullStr(fieldValue);
        JSONArray jsonArr = jsonProps.getJSONArray("options");
        if (jsonArr == null) {
            DebugUtil.e(getClass(), "getByVal", "控件描述中选项格式非法");
        }

        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject json = jsonArr.getJSONObject(i);
            String value = json.getString("value");
            if (value.equals(v)) {
                return json;
            }
        }
        return null;
    }

    @Override
    public String getReplaceCtlWithValueScript(IFormDAO ifdao, FormField ff) {
        if (ff.getValue() == null) {
            return "";
        }

        String v = converToHtml(null, ff, ff.getValue());
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "',\"" + v + "\");\n";
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     *
     * @return String
     */
    @Override
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        String pageType = StrUtil.getNullStr((String) request.getAttribute("pageType"));
        if ("edit".equals(pageType)) {
            if (ff.getValue() != null && !ff.getValue().equals("")) {
                return "$('#" + ff.getName() + "').val('" + ff.getValue() + "').trigger('change');\n";
            } else {
                return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
            }
        } else {
            return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
        }
    }

    @Override
    public String getControlType() {
        return "select";
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
        return "";
    }

    @Override
    public String getControlText(String userName, FormField ff) {
        return "";
    }

    @Override
    public String getControlOptions(String userName, FormField ff) {
        org.json.JSONArray selects = new org.json.JSONArray();

        String props = ff.getDescription();
        JSONObject jsonProps = null;
        try {
            jsonProps = JSONObject.parseObject(props);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return selects.toString();
        }

        JSONArray jsonArr = jsonProps.getJSONArray("options");
        if (jsonArr == null) {
            return selects.toString();
        }
        for (int i = 0; i < jsonArr.size(); i++) {
            org.json.JSONObject select = new org.json.JSONObject();
            JSONObject json = jsonArr.getJSONObject(i);
            try {
                select.put("name", json.getString("name"));
                select.put("value", json.getString("value"));
                selects.put(select);
            } catch (org.json.JSONException ex) {
                ex.printStackTrace();
            }
        }
        return selects.toString();
    }

}
