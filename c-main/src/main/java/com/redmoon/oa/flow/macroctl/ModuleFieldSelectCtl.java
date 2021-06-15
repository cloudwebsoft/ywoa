package com.redmoon.oa.flow.macroctl;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.IModuleUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.HanLPUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: 表单域选择控件</p>
 * 格式： {formCode:aaa, sourceFormCode:sales_linkman, idField:id, showField:msn, filter:none, isParentSaveAndReload:true, isMine:0, maps:[{sourceField: msn, destField:bmjc}]}
 * filter中的字符串经过了encodeJSON，如果filter没有值，则必须赋予值(如none)，否则服务器端json解析会出错
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ModuleFieldSelectCtl extends AbstractMacroCtl {
    /**
     * 下拉菜单模式时，最多取不超过500条
     */
    public static final int MAX_COUNT = 500;

    /**
     * 窗口选择
     */
    public static final int MODE_WIN = 1;
    /**
     * 下拉选择
     */
    public static final int MODE_SELECT = 0;

    public ModuleFieldSelectCtl() {
        super();
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
        if (!"".equals(v)) {
            String strDesc = StrUtil.getNullStr(ff.getDescription());
            // 向下兼容
            if ("".equals(strDesc)) {
                strDesc = ff.getDefaultValueRaw();
            }
            JSONObject json = null;
            try {
                strDesc = formatJSONStr(strDesc);
                json = new JSONObject(strDesc);
                String moduleCode = json.getString("sourceFormCode");
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                String sourceFormCode = msd.getString("form_code");

                String byFieldName = json.getString("idField");
                String showFieldName = json.getString("showField");

                if ("id".equals(byFieldName)) {
                    FormDAO fdao = getFormDAO(sourceFormCode, StrUtil.toLong(v));
                    if (fdao.isLoaded()) {
                        v = StrUtil.getNullStr(fdao.getFieldValue(showFieldName));
                        FormField formField = fdao.getFormField(showFieldName);
                        if (formField != null) {
                            if (formField.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlMgr mm = new MacroCtlMgr();
                                MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
                                if (mu != null) {
                                    // 取得request中原来的fdao
                                    IFormDAO ifdao = null;
                                    // getFormAbstractTable中所传的request为null
                                    if (request != null) {
                                        ifdao = RequestUtil.getFormDAO(request);
                                        RequestUtil.setFormDAO(request, fdao);
                                    }
                                    v = mu.getIFormMacroCtl().converToHtml(request, formField, v);
                                    if (ifdao != null) {
                                        // 恢复request中原来的fdao，以免ModuleController中setFormDAO的值被修改为本方法中的fdao
                                        RequestUtil.setFormDAO(request, ifdao);
                                    }
                                }
                            }
                        }
                    }
                    else {
                        v = "无";
                    }
                } else {
                    // LogUtil.getLog(getClass()).info("formCode=" + sourceFormCode + " showFieldName=" + showFieldName + " byFieldName=" + byFieldName + " fieldValue=" + fieldValue);
                    FormDAOMgr fdm = new FormDAOMgr(sourceFormCode);
                    v = fdm.getFieldValueOfOther(fieldValue, byFieldName, showFieldName);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                v = "json 格式非法";
            }

            return v;
        } else {
            return "";
        }
    }

    public FormDAO getFormDAO(String formCode, long id) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        return new FormDAO(id, fd);
    }

    /**
     * 自动映射，仅用于手机端，PC端通过从macro_module_field_select_ctl_js.jsp中调用getOnSelect映射
     *
     * @param flowId
     * @param value
     * @param ff
     * @Description:
     */
    public void autoMap(HttpServletRequest request, int flowId, String value, FormField ff) {
        if (flowId == -1) {
            return;
        }

        WorkflowDb wf = new WorkflowDb();
        wf = wf.getWorkflowDb(flowId);
        com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
        lf = lf.getLeaf(wf.getTypeCode());
        FormDb parentFd = new FormDb();
        parentFd = parentFd.getFormDb(lf.getFormCode());
        com.redmoon.oa.flow.FormDAO flowFormDAO = new com.redmoon.oa.flow.FormDAO();
        flowFormDAO = flowFormDAO.getFormDAO(flowId, parentFd);
        flowFormDAO.setFieldValue(ff.getName(), value);
        try {
            String strDesc = StrUtil.getNullStr(ff.getDescription());
            if ("".equals(strDesc)) {
                strDesc = ff.getDefaultValueRaw();
            }
            strDesc = formatJSONStr(strDesc);
            JSONObject json = new JSONObject(strDesc);
            String moduleCode = json.getString("sourceFormCode");
            String byFieldName = json.getString("idField");

            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String sourceFormCode = msd.getString("form_code");

            JSONArray mapAry = new JSONArray();
            if (json.has("maps")) {
                mapAry = (JSONArray) json.get("maps");
            }

            FormDAO fdao = null;
            if ("id".equals(byFieldName)) {
                fdao = getFormDAO(sourceFormCode, StrUtil.toLong(value));
            } else {
                String sql = "select id from form_table_" + sourceFormCode + " where " + byFieldName + "=" + StrUtil.sqlstr(value);
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    fdao = getFormDAO(sourceFormCode, rr.getLong(1));
                }
                else {
                    LogUtil.getLog(getClass()).error("表单" + sourceFormCode + "中" + byFieldName + "=" + value + "的记录不存在");
                    return;
                }
            }

            for (int i = 0; i < mapAry.length(); i++) {
                json = (JSONObject) mapAry.get(i);
                String destF = (String) json.get("destField");    // 父页面
                String sourceF = (String) json.get("sourceField");    // module_list_sel.jsp页面
                String setValue = StrUtil.getNullStr(fdao.getFieldValue(sourceF));
                flowFormDAO.setFieldValue(destF, setValue);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            flowFormDAO.save();
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param request HttpServletRequest
     * @param ff      FormField
     * @return String
     */
    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        String v = "";

        LogUtil.getLog(getClass()).info("StrUtil.toInt(ff.getValue())=" + ff.getValue());

        String openerFormCode = (String) request.getAttribute("formCode");
        if (openerFormCode == null || "".equals(openerFormCode)) {
            openerFormCode = (String) request.getAttribute("formCodeRelated");
        }

        String pageType = StrUtil.getNullStr((String) request.getAttribute("pageType"));

        // 20170814 fgf 在打开module_list_sel.jsp页面时选择后，因为在此页面中的funs中的convertToHTMLCtl生成的HTML替换了原控件
        // 而当convertToHTMLCtl时，在module_list_sel.jsp中的request是不存在formCode或formCodeRelated属性的
        // 所以此时应再通过ParamUtil去取
        if (openerFormCode == null || "".equals(openerFormCode)) {
            openerFormCode = ParamUtil.get(request, "openerFormCode");
            // 当在nest_table_view.jsp中调用render.getContentMacroReplaced时，此时openerFormCode为nestFormCode
            if ("".equals(openerFormCode)) {
                openerFormCode = (String)request.getAttribute("nestFormCode");
            }
        }

        // LogUtil.getLog(getClass()).info("convertToHTMLCtl: openerFormCode=" + openerFormCode);

        String moduleCode = "";

        String strDesc = StrUtil.getNullStr(ff.getDescription());
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        JSONObject json = null;
        try {
            strDesc = formatJSONStr(strDesc);
            json = new JSONObject(strDesc);
            // 20171230 fgf 将sourceFormCode改为模块编码，原来是主表单编码
            moduleCode = json.getString("sourceFormCode");
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String sourceFormCode = msd.getString("form_code");

            String byFieldName = json.getString("idField");
            String showFieldName = json.getString("showField");
            // 解码，替换%sq %dq，即单引号、双引号
            String filter = StrUtil.decodeJSON(json.getString("filter"));

            JSONArray mapAry = new JSONArray();
            if (json.has("maps")) {
                mapAry = (JSONArray) json.get("maps");
            }

            int mode = 1; // 默认选择窗体
            if (json.has("mode")) {
                mode = json.getInt("mode");
            }

            boolean isMulti = false;
            if (json.has("isMulti")) {
                isMulti = json.getBoolean("isMulti");
            }

            boolean canOpenWinSel = false;
            if (json.has("canOpenWinSel")) {
                canOpenWinSel = json.getBoolean("canOpenWinSel");
            }

            // 设为true以向下兼容，因为旧版控件均为ajax
            boolean isAjax = true;
            if (json.has("isAjax")) {
                isAjax = json.getBoolean("isAjax");
            }

            int flowId = StrUtil.toInt((String) request.getAttribute("cwsId"), -1);

            boolean isValueFromRequest = false;
            String value = StrUtil.getNullStr(ff.getValue());
            if ("".equals(value)) {
                // 如果有指定的requestParam
                if (json.has("requestParam") && !"".equals(json.getString("requestParam"))) {
                    value = ParamUtil.get(request, json.getString("requestParam"));
                }

                if ("".equals(value)) {
                    value = ParamUtil.get(request, ff.getName());
                    if (!"".equals(value)) {
                        isValueFromRequest = true;
                    }
                } else {
                    isValueFromRequest = true;
                }
            }
            if (!"".equals(value)) {
                String[] ary = StrUtil.split(value, ",");
                for (String key : ary) {
                    if ("id".equals(byFieldName)) {
                        FormDAO fdao = getFormDAO(sourceFormCode, StrUtil.toInt(key));
                        String vTemp = fdao.getFieldValue(showFieldName);
                        FormField formField = fdao.getFormField(showFieldName);
                        if (formField != null) {
                            if (formField.getType().equals(FormField.TYPE_MACRO)) {
                                MacroCtlMgr mm = new MacroCtlMgr();
                                MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
                                if (mu != null) {
                                    if ("".equals(v)) {
                                        v = mu.getIFormMacroCtl().converToHtml(request, formField, vTemp);
                                    } else {
                                        v += "," + mu.getIFormMacroCtl().converToHtml(request, formField, vTemp);
                                    }
                                }
                            } else {
                                if ("".equals(v)) {
                                    v = vTemp;
                                } else {
                                    v += "," + vTemp;
                                }
                            }
                        } else {
                            LogUtil.getLog(getClass()).error(showFieldName + " is not exist in " + sourceFormCode + "!");
                        }
                    } else {
                        FormDAOMgr fdm = new FormDAOMgr(sourceFormCode);
                        if (fdm != null) {
                            if ("".equals(v)) {
                                v = fdm.getFieldValueOfOther(key, byFieldName, showFieldName);
                            } else {
                                v += "," + fdm.getFieldValueOfOther(key, byFieldName, showFieldName);
                            }
                        }
                    }
                }
                // LogUtil.getLog(getClass()).info("mobile=" + fdao.getFieldValue("mobile"));
            }

            // 如果是request传值，则自动映射，只能用于记录ID的情况
            if (isValueFromRequest && flowId != -1) {
                // 这里的autoMap在数据库中是生成了，但是render处理到本字段时，被映射字段的值有可能已经先输出了
                // 所以改在macro_module_field_select_ctl_js.jsp中，根据isValueFromRequest去映射
                // autoMap(request, flowId, value, ff);
            }

            // 如果是可写状态，则转换为下拉模式
            if (mode == MODE_SELECT && ff.isEditable()) {
                // if (request.getAttribute("isModuleFieldSelectCtlJS") == null) {
                str = "\n<script src='" + request.getContextPath()
                        + "/flow/macro/macro_module_field_select_ctl_js.jsp?mode=" + mode + "&formCode=" + ff.getFormCode() + "&isReadonly=" + ff.isReadonly()
                        + "&fieldName=" + ff.getName() + "&flowId=" + flowId + "&defaultOptVal=" + StrUtil.UrlEncode(value) + "&defaultOptText=" + StrUtil.UrlEncode(v)
                        + "&pageType=" + pageType
                        + "'></script>\n" + str;
                // request.setAttribute("isModuleFieldSelectCtlJS", "y");
                // }

                String style = "";
                if (!"".equals(ff.getCssWidth())) {
                    style = "style='width:" + ff.getCssWidth() + "'";
                }
                else {
                    style = "style='width:150px'";
                }

                if (isMulti) {
                    style += " multiple='multiple'";
                }

                str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "' field='" + getNestTableFieldName(ff.getName()) + "' title='" + ff.getTitle() + "' " + style + " >";
				/*				
  				if (!"".equals(value)) {
					str += "<option value='" + value + "' selected >" + v + "</option>";
				}
				if (isMulti) {
					if (!"".equals(value)) {
						if (!value.equals("")) {
							String[] ary = StrUtil.split(value, ",");
							for (String key : ary) {
					            if (byFieldName.equals("id")) {
					                FormDAO fdao = getFormDAO(sourceFormCode, StrUtil.toInt(key));
					                v = fdao.getFieldValue(showFieldName);
					                FormField formField = fdao.getFormField(showFieldName);
					                if (formField != null) {
					                	if (formField.getType().equals(FormField.TYPE_MACRO)) {
					                		MacroCtlMgr mm = new MacroCtlMgr();
					                		MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
					                		if (mu != null) {
					                			v = mu.getIFormMacroCtl().converToHtml(request, formField, v);
					                		}
					                	}
					                }
					            }
					            else {
					                FormDAOMgr fdm = new FormDAOMgr(sourceFormCode);
					                if(fdm != null){
					                	v = fdm.getFieldValueOfOther(key, byFieldName, showFieldName);
					                }
					            }
								str += "<option value='" + key + "' selected='selected' >" + v + "</option>";						
							}
				        }		
					}
				}
				*/

				// 如果select2是ajax型的，则在此加入options无效，点击下拉箭头的时候，不会显示options
                if (!isAjax) {
                    try {
                        str += getOptions(request, ff);
                    } catch (ErrMsgException e) {
                        e.printStackTrace();
                    }
                }

                str += "</select>";

                if (!isMulti && !ff.isReadonly() && canOpenWinSel) {
                    str +=
                            "&nbsp;<input id='" + ff.getName() + "_btn' class='btnSearch' type=button onclick='openWinModuleFieldList(" +
                                    "o(\"" + ff.getName() + "\"),\"" + moduleCode + "\", \"" + byFieldName + "\", \"" + showFieldName + "\",\"" + StrUtil.UrlEncode(filter) + "\", \"" + StrUtil.UrlEncode(openerFormCode) + "\")'>";
                }
                return str;
            } else {
                // if (request.getAttribute("isModuleFieldSelectCtlJS") == null) {
                str = "\n<script src='" + request.getContextPath()
                        + "/flow/macro/macro_module_field_select_ctl_js.jsp?mode=" + mode + "&isValueFromRequest=" + isValueFromRequest + "&formCode=" + ff.getFormCode() + "&isReadonly=" + ff.isReadonly()
                        + "&fieldName=" + ff.getName() + "&flowId=" + flowId + "&value=" + StrUtil.UrlEncode(value) + "&valueShow=" + StrUtil.UrlEncode(v)
                        + "&pageType=" + pageType + "&defaultOptVal=" + StrUtil.UrlEncode(value) + "&defaultOptText=" + StrUtil.UrlEncode(v)
                        + "'></script>\n" + str;
                // request.setAttribute("isModuleFieldSelectCtlJS", "y");
                // }

                str += "<input id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow' title='" + ff.getTitle() + "' style='width:" + ff.getCssWidth() + "' value='" + StrUtil.getNullStr(v) + "' size=15 readonly>";
                str += "<input id='" + ff.getName() + "' title='" + ff.getTitle() + "' name='" + ff.getName() + "' field='" + getNestTableFieldName(ff.getName()) + "' value='" + value + "' type='hidden'>";
                // 如果可写且非只读，才能出现查询按钮
                if (ff.isEditable() && !ff.isReadonly()) {
                    // 不能用o(ff.getName())，因为在nest_table_view.jsp中修改元素的id及name
                    // str += "<input id='" + ff.getName() + "_btn' class='btnSearch' type=button onclick='openWinModuleFieldList(" +
                    //         "o(\"" + ff.getName() + "\"),\"" + moduleCode + "\", \"" + byFieldName + "\", \"" + showFieldName + "\",\"" + StrUtil.UrlEncode(filter) + "\", \"" + StrUtil.UrlEncode(openerFormCode) + "\", " + flowId + ", \"" + pageType + "\")'>";

                    // 不能用prev，因为可能会为livevalidation生成的*
                    // str += "<input id='" + ff.getName() + "_btn' class='btnSearch' type='button' onclick='openWinModuleFieldList(" +
                    //         "$(this).prev()[0],\"" + moduleCode + "\", \"" + byFieldName + "\", \"" + showFieldName + "\",\"" + StrUtil.UrlEncode(filter) + "\", \"" + StrUtil.UrlEncode(openerFormCode) + "\", " + flowId + ", \"" + pageType + "\")'>";
                    str += "<input id='" + ff.getName() + "_btn' class='btnSearch' type='button' onclick=\"openWinModuleFieldList(" +
                            "$('#' + $(this).attr('id').substring(0, $(this).attr('id').lastIndexOf('_btn')))[0],'" + moduleCode + "', '" + byFieldName + "', '" + showFieldName + "','" + StrUtil.UrlEncode(filter) + "', '" + StrUtil.UrlEncode(openerFormCode) + "', " + flowId + ", '" + pageType + "')\">";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            str = "json 格式非法";
        }

        return str;
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        try {
            String strDesc = StrUtil.getNullStr(ff.getDescription());
            // 向下兼容
            if ("".equals(strDesc)) {
                strDesc = ff.getDefaultValueRaw();
            }
            JSONObject json;
            strDesc = formatJSONStr(strDesc);
            json = new JSONObject(strDesc);
            String moduleCode = json.getString("sourceFormCode");
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);

            boolean isAjax = true;
            if (json.has("isAjax")) {
                isAjax = json.getBoolean("isAjax");
            }

            if (!isAjax) {
                String str = "";
                str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
                str += "<option value=''>无</option>";
                try {
                    str += getOptions(request, ff);
                } catch (ErrMsgException | JSONException e) {
                    e.printStackTrace();
                }
                str += "</select>";
                return str;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return super.convertToHTMLCtlForQuery(request, ff);
    }

    /**
     * 取得嵌套表格中字段的名称，因为此时字段的名称有变化，例：nest_field_jkr_0
     * @param fieldName
     * @return
     */
    public static String getNestTableFieldName(String fieldName) {
        if (fieldName.startsWith(ConstUtil.NEST_TABLE_FIELD_PREFIX)) {
            int p = fieldName.lastIndexOf("_");
            return fieldName.substring(ConstUtil.NEST_TABLE_FIELD_PREFIX.length(), p);
        }
        else {
            return fieldName;
        }
    }

    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String v = StrUtil.getNullStr(ff.getValue());
        String val = v;
        long id = -1;
        String moduleCode = "";
        String byFieldName = "";
        if (!"".equals(v)) {
            if (v.equals(ff.getDefaultValueRaw()) || v.equals(ff.getDescription())) {
                v = "";
            } else {
                JSONObject json = null;
                try {
                    String strDesc = StrUtil.getNullStr(ff.getDescription());
                    // 向下兼容
                    if ("".equals(strDesc)) {
                        strDesc = ff.getDefaultValueRaw();
                    }

                    strDesc = formatJSONStr(strDesc);
                    json = new JSONObject(strDesc);
                    moduleCode = json.getString("sourceFormCode");
                    byFieldName = json.getString("idField");
                    ModuleSetupDb msd = new ModuleSetupDb();
                    msd = msd.getModuleSetupDb(moduleCode);
                    String sourceFormCode = msd.getString("form_code");

                    byFieldName = json.getString("idField");
                    String showFieldName = json.getString("showField");
                    if (!"".equals(StrUtil.getNullStr(ff.getValue()))) {
                        if ("id".equals(byFieldName)) {
                            FormDAO fdao = getFormDAO(sourceFormCode, StrUtil
                                    .toInt(ff.getValue()));
                            id = fdao.getId();
                            v = fdao.getFieldValue(showFieldName);

                            FormField formField = fdao.getFormField(showFieldName);
                            if (formField != null) {
                                if (formField.getType().equals(FormField.TYPE_MACRO)) {
                                    MacroCtlMgr mm = new MacroCtlMgr();
                                    MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
                                    if (mu != null) {
                                        v = mu.getIFormMacroCtl().converToHtml(null, formField, v);
                                    }
                                }
                            }
                        } else {
                            FormDAOMgr fdm = new FormDAOMgr(sourceFormCode);
                            v = fdm.getFieldValueOfOther(ff.getValue(), byFieldName, showFieldName);
                            FormDb fd = new FormDb();
                            fd = fd.getFormDb(sourceFormCode);
                            FormField formField = fd.getFormField(showFieldName);
                            if (formField != null) {
                                if (formField.getType().equals(FormField.TYPE_MACRO)) {
                                    MacroCtlMgr mm = new MacroCtlMgr();
                                    MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
                                    if (mu != null) {
                                        v = mu.getIFormMacroCtl().converToHtml(null, formField, v);
                                    }
                                }
                            }
                        }
                        // LogUtil.getLog(getClass()).info("mobile=" +
                        // fdao.getFieldValue("mobile"));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        String str = "";
        if ("id".equals(byFieldName)) {
            str += "DisableCtl('" + ff.getName() + "', '" + ff.getType()
                    + "','','" + val + "');\n";
        }
        else {
            str += "DisableCtl('" + ff.getName() + "', '" + ff.getType()
                    + "','" + v + "','" + val + "');\n";
        }
        str += "if (o('" + ff.getName() + "_realshow')) {\n";
        str += "o('" + ff.getName() + "_realshow').value='" + v + "';\n";
        str += "o('" + ff.getName() + "_realshow').style.display='none';\n";
        str += "}\n";
		
		/*
    	String addTab = "addTab('" + ff.getTitle() + "', '" + Global.getRootPath() + "/visual/module_show.jsp?id=" + id + "&parentId=" + id + "&code=" + moduleCode + "')";
    	str += "function addMyTab" + ff.getName() + "() { " + addTab + "} \n";
    	String linkStr = "<a href=\"javascript:;\" onclick=\"addMyTab" + ff.getName() + "()\">" + v + "</a>";        	
    	*/
        if ("id".equals(byFieldName)) {
            String[] aryVal = StrUtil.split(ff.getValue(), ",");
            String[] aryV = StrUtil.split(v, ",");
            if (aryVal != null && aryV != null) {
                StringBuffer sbLink = new StringBuffer();
                // StringBuffer sbFunc = new StringBuffer();
                for (int i = 0; i < aryVal.length; i++) {
                    String idVal = aryVal[i];
                    String text = aryV[i];
                    // function addMyTab...在ajaxpage加载nest_table_view.jsp时，会丢失
                    // StrUtil.concat(sbLink, "，", "<a href=\"javascript:;\" onclick=\"addMyTab" + ff.getName() + idVal + "()\">" + text + "</a>");
                    // String addTab = "addTab('" + ff.getTitle() + "', '" + Global.getRootPath() + "/visual/module_show.jsp?id=" + idVal + "&parentId=" + idVal + "&code=" + moduleCode + "')";
                    // sbFunc.append("function addMyTab" + ff.getName() + idVal + "() { " + addTab + "} \n");

                    // 将单引号转义为：&#39，如果不转义，则在after('" + sbLink.toString() + "')时，js会报错
                    String addTab = "addTab(&#39" + ff.getTitle() + "&#39, &#39" + Global.getRootPath() + "/visual/module_show.jsp?id=" + idVal + "&parentId=" + idVal + "&code=" + moduleCode + "&#39)";
                    StrUtil.concat(sbLink, "，", "<a href=\"javascript:;\" onclick=\"" + addTab + "\">" + text + "</a>");
                }
                // str += sbFunc.toString();
                str += "$(o('" + ff.getName() + "')).after('" + sbLink.toString() + "');\n";
            }
        }

        str += "if (o('" + ff.getName() + "_btn'))\n";
        str += "	o('" + ff.getName() + "_btn').style.display='none';\n";
        return str;
        // return "DisableCtl('" + ff.getName() + "', '" + ff.getType() + "','" + "" + "','');\n";
    }

    /**
     * 当report时，取得用来替换控件的脚本
     *
     * @param ff FormField
     * @return String
     */
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = ""; // 显示值
        String value = ""; // 值
        long id = -1;
        String moduleCode = "";
        if (ff.getValue() != null && !"".equals(ff.getValue()) && !ff.getValue().equals(ff.getDefaultValueRaw()) && !ff.getValue().equals(ff.getDescription())) {
            String strDesc = StrUtil.getNullStr(ff.getDescription());
            // 向下兼容
            if ("".equals(strDesc)) {
                strDesc = ff.getDefaultValueRaw();
            }

            JSONObject json = null;
            try {
                strDesc = formatJSONStr(strDesc);
                json = new JSONObject(strDesc);
                moduleCode = json.getString("sourceFormCode");
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                String sourceFormCode = msd.getString("form_code");

                String byFieldName = json.getString("idField");
                String showFieldName = json.getString("showField");

                if ("id".equals(byFieldName)) {
                    value = ff.getValue();
                }
                else {
                    value = "";
                }

                boolean isMulti = false;
                if (json.has("isMulti")) {
                    isMulti = json.getBoolean("isMulti");
                }

                if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
                    String[] ary = StrUtil.split(ff.getValue(), ",");
                    for (String key : ary) {
                        if ("id".equals(byFieldName)) {
                            FormDAO fdao = getFormDAO(sourceFormCode, StrUtil.toInt(key));
                            String vTemp;
                            if (fdao.isLoaded()) {
                                vTemp = fdao.getFieldValue(showFieldName);
                            } else {
                                vTemp = " ";
                            }
                            id = fdao.getId();
                            FormField formField = fdao.getFormField(showFieldName);
                            if (formField != null) {
                                if (formField.getType().equals(FormField.TYPE_MACRO)) {
                                    // formField.setName(ff.getName());
                                    MacroCtlMgr mm = new MacroCtlMgr();
                                    MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
                                    if (mu != null) {
                                        if ("".equals(v)) {
                                            v = mu.getIFormMacroCtl().converToHtml(null, formField, vTemp);
                                        } else {
                                            v += "," + mu.getIFormMacroCtl().converToHtml(null, formField, vTemp);
                                        }
                                        // hw 20160617 此处存在一定的技巧，要判断是否为宏控件，如果为宏控件的话则需要调用其getReplaceCtlWithValueScript方法
                                        // 但此处为映射关系，所以两个表单的映射字段的ff.getName()不一定相同，所以需要用当前的name替换映射字段的name
                                        // 有点绕，读到此处的人请好好理解这里的奥妙。。。
                                        // 20170803 fgf 此处注释是因为在报表状态下需要生成超链接
                                        // return mu.getIFormMacroCtl().getReplaceCtlWithValueScript(formField);
                                    }
                                } else {
                                    if ("".equals(v)) {
                                        v = vTemp;
                                    } else {
                                        v += "," + vTemp;
                                    }
                                }
                            }
                        } else {
                            FormDAOMgr fdm = new FormDAOMgr(sourceFormCode);
                            String vTemp = fdm.getFieldValueOfOther(key, byFieldName,
                                    showFieldName);
                            id = fdm.getVisualObjId();
                            if ("".equals(value)) {
                                value = String.valueOf(id);
                            }
                            else {
                                value += "," + String.valueOf(id);
                            }

                            FormDb fd = new FormDb();
                            fd = fd.getFormDb(sourceFormCode);
                            FormField formField = fd.getFormField(showFieldName);
                            if (formField != null) {
                                if (formField.getType().equals(FormField.TYPE_MACRO)) {
                                    MacroCtlMgr mm = new MacroCtlMgr();
                                    MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
                                    if (mu != null) {
                                        if ("".equals(v)) {
                                            v = mu.getIFormMacroCtl().converToHtml(null, formField, vTemp);
                                        } else {
                                            v += "," + mu.getIFormMacroCtl().converToHtml(null, formField, vTemp);
                                        }
                                    }
                                } else {
                                    if ("".equals(v)) {
                                        v = vTemp;
                                    } else {
                                        v += "," + vTemp;
                                    }
                                }
                            }
                        }
                        // LogUtil.getLog(getClass()).info("mobile=" + fdao.getFieldValue("mobile"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        v = StrUtil.getNullStr(v);
        if (!"".equals(v.trim())) { // 有时发现为空格，可能是浏览器兼容性问题
            String[] aryVal = StrUtil.split(value, ",");
            String[] aryV = StrUtil.split(v, ",");
            StringBuffer sbLink = new StringBuffer();
            StringBuffer sbFunc = new StringBuffer();
            for (int i = 0; i < aryVal.length; i++) {
                String val = aryVal[i];
                String text = aryV[i];
                // 替换掉text中的单引号
                text = text.replaceAll("'", "&#039;");

                StrUtil.concat(sbLink, "，", "<a href=\"javascript:;\" onclick=\"addMyTab" + ff.getName() + val + "()\">" + text + "</a>");

                String addTab = "addTab('" + ff.getTitle() + "', '" + Global.getRootPath() + "/visual/module_show.jsp?id=" + val + "&parentId=" + val + "&code=" + moduleCode + "')";
                sbFunc.append("function addMyTab" + ff.getName() + val + "() { " + addTab + "} \n");
            }
            String ret = sbFunc.toString();
            ret += "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() + "', '" + sbLink.toString() + "');\n";
            return ret;
        } else {
            return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() + "','" + v + "');\n";
        }
    }

    @Override
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        String moduleCode = "";
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }

        strDesc = formatJSONStr(strDesc);

        String str = "";
        JSONObject json = null;
        try {
            json = new JSONObject(strDesc);
            moduleCode = json.getString("sourceFormCode");
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String sourceFormCode = msd.getString("form_code");

            String byFieldName = json.getString("idField");
            String showFieldName = json.getString("showField");

            int mode = 1; // 默认选择窗体
            if (json.has("mode")) {
                mode = json.getInt("mode");
            }

            String value = StrUtil.getNullStr(ff.getValue());
            if (value.equals("") || value.equals(ff.getDefaultValueRaw()) || value.equals(ff.getDescription())) {
                if (json.has("requestParam") && !"".equals(json.getString("requestParam"))) {
                    // 来自于指定的参数名称
                    value = ParamUtil.get(request, json.getString("requestParam"));
                } else {
                    // 默认以字段名作为参数从request中获取
                    value = ParamUtil.get(request, ff.getName());
                }
            }

            // 设为true以向下兼容，因为旧版控件均为ajax，当ajax为false时，点击下拉箭头，显示下拉列表
            boolean isAjax = true;
            if (json.has("isAjax")) {
                isAjax = json.getBoolean("isAjax");
            }

            if (!"".equals(value)) {
                String v = "";
                if ("id".equals(byFieldName)) {
                    FormDAO fdao = getFormDAO(sourceFormCode, StrUtil.toLong(value));
                    FormField formField = fdao.getFormField(showFieldName);
                    v = fdao.getFieldValue(showFieldName);
                    if (formField != null) {
                        if (formField.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlMgr mm = new MacroCtlMgr();
                            MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
                            if (mu != null) {
                                v = mu.getIFormMacroCtl().converToHtml(request, formField, v);
                            }
                        }
                    }
                } else {
                    FormDAOMgr fdm = new FormDAOMgr(sourceFormCode);
                    v = StrUtil.getNullStr(fdm.getFieldValueOfOther(value, byFieldName, showFieldName));
                }

                if (v != null) {
                    v = v.replaceAll("'", "&#039;");
                    value = value.replaceAll("'", "&#039;");
                }
                else {
                    v = "";
                }

                // LogUtil.getLog(getClass()).info("mobile=" + fdao.getFieldValue("mobile"));
                if (mode == 1) {
                    str += "if (o('" + ff.getName() + "_realshow')) o('" + ff.getName() + "_realshow').value='" + v + "';\n";
                    str += "if (o('" + ff.getName() + "'))\n";
                    str += "	o('" + ff.getName() + "').style.display='none';\n";
                    str += "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '" + value + "');\n";
                } else {
                    if (!ff.isReadonly() && ff.isEditable()) {
                        if (!isAjax) {
                            str += "$('#" + ff.getName() + "').val('" + ff.getValue() + "').trigger('change');\n";
                        }
                        else {
                            str += "$('#" + ff.getName() + "').empty().append(\"<option id='" + value + "' value='" + value + "'>" + v + "</option>\").trigger('change');\n";
                        }
                    }
                }
            } else {
                str = "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '');\n";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    public String getControlType() {
        return "ModuleFieldSelect";
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }

    @Override
    public String getControlText(String userName, FormField ff) {
        String res = "";
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        strDesc = formatJSONStr(strDesc);

        String value = StrUtil.getNullStr(ff.getValue());
        if (!"".equals(strDesc) && !"".equals(value)) {
            JSONObject json;
            try {
                json = new JSONObject(strDesc);
                String moduleCode = json.getString("sourceFormCode");
                String byFieldName = json.getString("idField");

                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                String sourceFormCode = msd.getString("form_code");

                String showFieldName = json.getString("showField");
                FormDb fd = new FormDb(sourceFormCode);
                if (value != null && !value.equals("")) {
                    FormDAO fdao = null;
                    if ("id".equals(byFieldName)) {
                        fdao = getFormDAO(sourceFormCode, StrUtil.toLong(value));
                    } else {
                        String sql = "select id from form_table_" + sourceFormCode + " where " + byFieldName + "=" + StrUtil.sqlstr(value);
                        JdbcTemplate jt = new JdbcTemplate();
                        ResultIterator ri = jt.executeQuery(sql);
                        if (ri.hasNext()) {
                            ResultRecord rr = (ResultRecord) ri.next();
                            fdao = getFormDAO(sourceFormCode, rr.getLong(1));
                        }
                        else {
                            LogUtil.getLog(getClass()).error("表单" + sourceFormCode + "中" + byFieldName + "=" + value + "的记录不存在");
                            return "";
                        }
                    }

                    FormField formField = fdao.getFormField(showFieldName);
                    if (formField != null) {
                        if (formField.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlMgr mm = new MacroCtlMgr();
                            MacroCtlUnit mu = mm.getMacroCtlUnit(formField.getMacroType());
                            if (mu != null) {
                                // 多重映射
                                res = mu.getIFormMacroCtl().getControlText(userName, formField);
                            }
                        } else {
                            res = fdao.getFieldValue(showFieldName);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (java.lang.NumberFormatException e2) {
                e2.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    @Override
    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

    /**
     * 取得对应的表单编码，用于模块添加的时候，可以自动关联主表
     *
     * @param request
     * @param ff
     * @return
     */
    @Override
    public String getFormCode(HttpServletRequest request, FormField ff) {
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        strDesc = formatJSONStr(strDesc);
        String value = StrUtil.getNullStr(ff.getValue());
        if (!strDesc.equals("") && !value.equals("")) {
            JSONObject json;
            try {
                json = new JSONObject(strDesc);
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(json.getString("sourceFormCode"));
                return msd.getString("form_code");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (java.lang.NumberFormatException e2) {
                e2.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 将json字符串正规化，加上双引号
     *
     * @param defaultVal
     * @return
     * @Description:
     */
    public static String formatJSONStr(String defaultVal) {
        // 两种不同的非标准json格式，用于测试
        // defaultVal = "{formCode:capex, sourceFormCode:xmysmx1, idField:id, showField:pp, filter:1=1, isParentSaveAndReload:true, maps:[{sourceField: xmbh, destField:xmbh}]}";
        // defaultVal = "{formCode:prj_task, sourceFormCode:prj, idField:id, showField:prj_name, filter:prj_manager %eq %lb$manager%rb and cws_status %eq 0, isParentSaveAndReload:true, maps:[]}";
        // defaultVal = "{formCode:capex, sourceFormCode:htgz, idField:id, showField:htmc, filter:none, isParentSaveAndReload:true, maps:[{sourceField: hhtbh, destField:htbh},{sourceField: htmc, destField:htmc},{sourceField: htzje, destField:htzje}]}";
        if ("".equals(defaultVal)) {
            return "{}";
        }

        // 如果是从手机端传入参数，以为经过了JSON.stringify处理后的字符串会带有  "{ 开头 以及  }" 结尾，要进行剔除处理
        if (defaultVal.startsWith("\"{") && defaultVal.endsWith("}\"")) {
            defaultVal = defaultVal.substring(1, defaultVal.length() - 1);
        }
    	
    	/*
    	 * gson与jackson都不能解决值没有双引号的问题，jackson能够解决键没有双引号的问题
    	Map<String,String> map = new HashMap<String,String>(); 
    	ObjectMapper mapper = new ObjectMapper(); 
    	
    	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES , false);
    	mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);

    	mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false); 
    	mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, false);

    	try {
			map = mapper.readValue(defaultVal, new TypeReference<HashMap<String,String>>(){});
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
    	System.out.println(map); 	
    	
    	
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();

    	System.out.println("ModuleFieldSelectCtl gson=" + gson.toJson(defaultVal));
    	return gson.toJson(defaultVal);
    	*/


        StringBuffer sb = new StringBuffer();

        // 去掉前后的{}
        defaultVal = defaultVal.substring(1, defaultVal.length() - 1);

        // filter":"find_in_set(%lb$curUser%rb%cozrr1)"，2017-11-27 fgf JS中的encodeJSON加入了对于逗号的处理，逗号转换为%co

        // 使json字符串标准化，否则无法解析
        String[] ary = StrUtil.split(defaultVal, ",");
        for (int i = 0; i < ary.length; i++) {
            String[] pair = StrUtil.split(ary[i].trim(), ":");

            StringBuffer pairBuf = new StringBuffer();
            if (pair.length == 1) {
                StrUtil.concat(pairBuf, ":", "\"" + pair[0] + "\"");
                StrUtil.concat(pairBuf, ":", "\"\"");
            } else {
                for (int j = 0; j < pair.length; j++) {
                    String str = pair[j].trim();

                    boolean isNeedCheck = true;
                    if ((str.startsWith("[") && str.endsWith("]")) || (str.startsWith("{") && str.endsWith("}"))) {
                        isNeedCheck = false;
                    }

                    boolean isLeft = false;
                    boolean isRight = false;

                    boolean isCurLeft = false; // 花括弧
                    boolean isCurRight = false;

                    // 当前面有[{时，如[{sourceField:...
                    if (str.startsWith("[{")) {
                        str = str.substring(2);
                        isLeft = true;
                    }

                    // ..., destField:xmbh}]
                    if (str.endsWith("}]")) {
                        str = str.substring(0, str.length() - 2);
                        isRight = true;
                    }

                    // maps:[{sourceField: hhtbh, destField:htbh},
                    if (str.startsWith("{")) {
                        str = str.substring(1);
                        isCurLeft = true;
                    }

                    if (str.endsWith("}")) {
                        // 判断以避免：jkr = {$sqr}
                        if (!isCurLeft) {
                            str = str.substring(0, str.length() - 1);
                            isCurRight = true;
                        }
                    }


                    if (isNeedCheck) {
                        if (!str.startsWith("\"")) {
                            str = "\"" + str + "\"";
                        }
                    }

                    if (isLeft) {
                        str = "[{" + str;
                    }
                    if (isRight) {
                        str = str + "}]";
                    }

                    if (isCurLeft) {
                        str = "{" + str;
                    }

                    if (isCurRight) {
                        str = str + "}";
                    }

                    StrUtil.concat(pairBuf, ":", str);
                }
            }
            StrUtil.concat(sb, ",", pairBuf.toString());
        }

        defaultVal = "{" + sb.toString() + "}";

        return defaultVal;
    }

    /**
     * 用于手机端处理流程时，得到嵌套表格、嵌套表格2、明细表宏控件的描述json，用于传值给H5界面
     *
     * @param ff
     * @return
     * @Description: fgf 20170412
     */
    public static JSONObject getCtlDesc(FormField ff) {
        String defaultVal = "";
        try {
            // 20131123 fgf 添加
            defaultVal = formatJSONStr(ff.getDescription());

            JSONObject jsonObj = new JSONObject();
            JSONObject json = new JSONObject(defaultVal);

            // defaultVal = StrUtil.decodeJSON(ff.getDescription());

            // fastjson也不能解析非标准的格式
            // com.alibaba.fastjson.JSONObject fastjson = JSON.parseObject(defaultVal);
            // JSONObject json = new JSONObject(fastjson.toString());

            String sourceFormCode = json.getString("sourceFormCode");
            String idField = json.getString("idField");
            String showField = json.getString("showField");
            String formCode = json.getString("formCode");
            boolean isRealTime = false;
            if (json.has("isRealTime")) {
                isRealTime = json.getBoolean("isRealTime");
            }

            // 解码，替换%sq %dq，即单引号、双引号
            String filter = StrUtil.decodeJSON(json.getString("filter"));

            // 取得过滤条件中的父窗口的字段
            StringBuffer parentFields = new StringBuffer();
            if (!"".equals(filter)) {
                Pattern p = Pattern.compile(
                        "\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(filter);
                while (m.find()) {
                    String fieldName = m.group(1);
                    // 当条件为包含时，fieldName以@开头
                    if (fieldName.startsWith("@")) {
                        fieldName = fieldName.substring(1);
                    } else if ("cwsCurUser".equals(fieldName)) { // 当前用户
                        continue;
                    }

                    StrUtil.concat(parentFields, ",", fieldName);
                }
            }

            jsonObj.put("sourceFormCode", sourceFormCode);
            jsonObj.put("idField", idField);
            jsonObj.put("showField", showField);
            jsonObj.put("parentFields", parentFields.toString());
            jsonObj.put("formCode", formCode);
            jsonObj.put("isRealTime", isRealTime);
            return jsonObj;

        } catch (JSONException e) {
            System.out.println(ff.getTitle() + " code=" + ff.getName() + " desc=" + ff.getDescription() + " json=" + defaultVal);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getValueByName(FormField ff, String name) {
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        strDesc = formatJSONStr(strDesc);

        JSONObject json = null;
        try {
            json = new JSONObject(strDesc);
            String moduleCode = json.getString("sourceFormCode");
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String sourceFormCode = msd.getString("form_code");

            String byFieldName = json.getString("idField");
            String showFieldName = json.getString("showField");
            name = getModuleFieldSelectValue(sourceFormCode, showFieldName, name);
            String sql = "select " + byFieldName + " from form_table_" + sourceFormCode + " where " + showFieldName + "=?";
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri;
            try {
                ri = jt.executeQuery(sql, new Object[]{name});
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    return rr.getString(1);
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 如果传入的字段为表单域选择控件,则继续取其实际值，否则，返回原始值
     */
    public String getModuleFieldSelectValue(String sourceFormCode, String fieldName, String value) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb(sourceFormCode);
        FormField ff = fd.getFormField(fieldName);
        MacroCtlUnit mc = new MacroCtlUnit(ff.getMacroType());
        if (mc != null) {
            if (mc.getCode().equals("module_field_select")) {
                //是表单域选择控件
                return getValueByName(ff, value);
            }
        }
        return value;
    }

    /**
     * 用于流程处理时，生成表单默认值
     *
     * @param ff FormField
     * @return Object
     */
    @Override
    public Object getValueForCreate(int flowId, FormField ff) {
        return "";
    }

    /**
     * 取得根据名称（而不是值）查询时需用到的SQL语句，如果没有特定的SQL语句，则返回空字符串
     *
     * @param request
     * @param ff      当前被查询的字段
     * @param value
     * @param isBlur  是否模糊查询
     * @return
     */
    @Override
    public String getSqlForQuery(HttpServletRequest request, FormField ff, String value, boolean isBlur) {
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        JSONObject json = null;
        try {
            strDesc = formatJSONStr(strDesc);
            json = new JSONObject(strDesc);
            String moduleCode = json.getString("sourceFormCode");
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String sourceFormCode = msd.getString("form_code");

            boolean isAjax = true;
            if (json.has("isAjax")) {
                isAjax = json.getBoolean("isAjax");
            }

            String byFieldName = json.getString("idField");
            String showFieldName = json.getString("showField");
            if (isAjax) {
                if (isBlur) {
                    return "select " + byFieldName + " from form_table_" + sourceFormCode + " where " + showFieldName + " like " +
                            StrUtil.sqlstr("%" + value + "%");
                } else {
                    return "select " + byFieldName + " from form_table_" + sourceFormCode + " where " + showFieldName + "=" +
                            StrUtil.sqlstr(value);
                }
            }
            else {
                if ("id".equals(byFieldName)) {
                    return value;
                }
                if (isBlur) {
                    return "select " + byFieldName + " from form_table_" + sourceFormCode + " where " + byFieldName + " like " +
                            StrUtil.sqlstr("%" + value + "%");
                } else {
                    return "select " + byFieldName + " from form_table_" + sourceFormCode + " where " + byFieldName + "=" +
                            StrUtil.sqlstr(value);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static JSONArray getAjaxOptions(HttpServletRequest request, FormField ff) throws ErrMsgException, JSONException {
        String sourceFormCode = "", byFieldName = "", showFieldName = "", filter = "";
        boolean isSimilar = false;
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        try {
            strDesc = ModuleFieldSelectCtl.formatJSONStr(strDesc);
            JSONObject json = new JSONObject(strDesc);
            sourceFormCode = json.getString("sourceFormCode");
            byFieldName = json.getString("idField");
            showFieldName = json.getString("showField");
            filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(json.getString("filter"));
            if (json.has("isSimilar")) {
                isSimilar = json.getBoolean("isSimilar");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String orderBy = ParamUtil.get(request, "orderBy");
        if ("".equals(orderBy)) {
            orderBy = "id";
        }
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }

        // 过滤条件
        String conds = filter;
        if ("none".equals(conds)) {
            conds = "";
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(sourceFormCode);
        String formCode = msd.getString("form_code");

        // 取得查询参数
        String what = ParamUtil.getParam(request, "q");

        String sql = "select distinct t1.id from form_table_" + formCode + " t1 where " + showFieldName + " like " + StrUtil.sqlstr("%" + what + "%");
        if (isSimilar) {
            // 排除关键词
            Config config = new Config();
            String wordsExcepted = config.get("segmentKeywordsExcepted");
            String[] words = wordsExcepted.split(",");
            for (String word : words) {
                what = what.replaceAll(word, "");
            }
            StringBuilder keywords = new StringBuilder();
            List<String> list = HanLPUtil.segment(what);
            if (list.size() > 0) {
                for (String word : list) {
                    if (word.length() > 1) {
                        if (keywords.length() == 0) {
                            keywords.append("+").append(word);
                        } else {
                            keywords.append(" +").append(word);
                        }
                    }
                }
                if (keywords.length() > 0) {
                    sql = "select distinct t1.id from form_table_" + sourceFormCode + " t1 where MATCH (" + showFieldName + ") AGAINST ('" + keywords + "' IN BOOLEAN MODE)";
                    // System.out.println(getClass() + " " + sql);
                }
            }
        }

        if (!"".equals(conds)) {
            String[] ary = ModuleUtil.parseFilter(request, formCode, conds);
            if (ary[0] != null) {
                conds = ary[0];
            }
            if (!sql.contains(" where ")) {
                sql += " where " + conds;
            } else {
                sql += " and " + conds;
            }
        }

        if (sql.contains(" where ")) {
            // 以便于手工在表单域选择控件中设定cws_status条件，用于任务管理中取流程未走完的项目，但此时项目的任务已开始分配
            if (!sql.toLowerCase().contains("cws_status")) {
                sql += " and t1.cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
            }
        } else {
            sql += " where t1.cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
        }

        if (!sql.contains(" order by")) {
            sql += " order by " + orderBy + " " + sort;
        }

        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        Vector<FormDAO> v = fdao.list(formCode, sql);

        JSONArray arr = new JSONArray();
        for (FormDAO formDAO : v) {
            fdao = formDAO;
            JSONObject json = new JSONObject();
            if ("id".equals(byFieldName)) {
                json.put("id", fdao.getId());
            } else {
                json.put("id", fdao.getFieldValue(byFieldName));
            }
            json.put("text", fdao.getFieldValue(showFieldName));
            arr.put(json);
        }

        return arr;
    }

    /**
     * 当下拉模式时，convertToHtml中列出不超过500条选项
     * @param request
     * @param ff
     * @return
     * @throws ErrMsgException
     * @throws JSONException
     */
    public String getOptions(HttpServletRequest request, FormField ff) throws ErrMsgException, JSONException {
        String sourceFormCode = "", byFieldName = "", showFieldName = "", filter = "";
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        try {
            strDesc = ModuleFieldSelectCtl.formatJSONStr(strDesc);
            JSONObject json = new JSONObject(strDesc);
            sourceFormCode = json.getString("sourceFormCode");
            byFieldName = json.getString("idField");
            showFieldName = json.getString("showField");
            filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(json.getString("filter"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String orderBy = ParamUtil.get(request, "orderBy");
        if ("".equals(orderBy)) {
            orderBy = "id";
        }
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }

        // 过滤条件
        String conds = filter;
        if ("none".equals(conds)) {
            conds = "";
        }
        String isNestSheetQuery = (String)request.getAttribute("isNestSheetQuery");
        if (isNestSheetQuery != null) {
            // 如果是嵌入查询，将条件中的{$parentId}实时改为引入nest_sheet_view.jsp时url中传递的参数mainId
            // $mainId前需加上转义，否则会因为按照$1$2的分组模式进行匹配，当编译器发现“$”后跟的不是整数的时候，就会抛出“非法的组引用”的异常：Illegal group reference
            conds = conds.replaceAll("\\{\\$parentId\\}", "{\\$mainId}");

            // 当嵌入查询时，条件中如使用cws_id时，不能将模式设置为显示成下拉菜单，而只能显示为输入框
            // 因为cws_id只能用于弹窗选择模式，只有当进入module_list_sel.jsp时才会传入cws_id参数，详见文档
            conds = conds.replaceAll("\\{\\$cws_id\\}", "{\\$mainId}");
        }

        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDbOrInit(sourceFormCode);
        String formCode = msd.getString("form_code");

        String sql = "select distinct t1.id from form_table_" + formCode + " t1";

        boolean isFilterSelectSQL = false;
        if (!"".equals(conds)) {
            String[] ary = ModuleUtil.parseFilter(request, formCode, conds);
            if (ary[0] != null) {
                conds = ary[0];
            }

            if (!conds.startsWith("select ")) {
                if (!sql.contains(" where ")) {
                    sql += " where " + conds;
                } else {
                    sql += " and " + conds;
                }
            }
            else {
                isFilterSelectSQL = true;
                sql = conds;
            }
        }

        if (!isFilterSelectSQL) {
            if (sql.contains(" where ")) {
                // 以便于手工在表单域选择控件中设定cws_status条件，用于任务管理中取流程未走完的项目，但此时项目的任务已开始分配
                if (!sql.toLowerCase().contains("cws_status")) {
                    sql += " and t1.cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
                }
            } else {
                sql += " where t1.cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
            }

            if (!sql.contains(" order by")) {
                sql += " order by " + orderBy + " " + sort;
            }
        }

        DebugUtil.i(getClass(), "getOptions", sql);

        int count = 0;
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate();
            ResultIterator ri = jdbcTemplate.executeQuery(SQLFilter.getCountSql(sql));
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                count = rr.getInt(1);
                if (count == 0) {
                    return "";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 最多显示不超过500条
        if (count > MAX_COUNT) {
            count = MAX_COUNT;
        }

        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        ListResult lr = fdao.listResult(formCode, sql, 1, count);

        StringBuilder stringBuffer = new StringBuilder();
        for (Object o : lr.getResult()) {
            fdao = (FormDAO) o;
            JSONObject json = new JSONObject();
            if ("id".equals(byFieldName)) {
                json.put("id", fdao.getId());
                stringBuffer.append("<option value='" + fdao.getId() + "'>" + fdao.getFieldValue(showFieldName) + "</option>");
            } else {
                stringBuffer.append("<option value='" + fdao.getFieldValue(byFieldName) + "'>" + fdao.getFieldValue(showFieldName) + "</option>");
            }
        }

        return stringBuffer.toString();
    }

    /**
     * 从macro_module_field_select_ctl_js.jsp中映射
     * @param request
     * @param ff
     * @param fd
     * @return
     * @throws ErrMsgException
     * @throws JSONException
     * @throws SQLException
     */
    public static JSONArray getOnSelect(HttpServletRequest request, FormField ff, FormDb fd) throws ErrMsgException, JSONException, SQLException {
        String sourceFormCode = "", byFieldName = "", showFieldName = "", filter = "";
        boolean isSimilar = false;
        JSONArray mapAry = new JSONArray();
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        JSONArray ary = new JSONArray();
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        try {
            strDesc = ModuleFieldSelectCtl.formatJSONStr(strDesc);
            JSONObject json = new JSONObject(strDesc);
            sourceFormCode = json.getString("sourceFormCode");
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(sourceFormCode);
            sourceFormCode = msd.getString("form_code");

            byFieldName = json.getString("idField");
            showFieldName = json.getString("showField");
            mapAry = (JSONArray) json.get("maps");
            filter = com.redmoon.oa.visual.ModuleUtil.decodeFilter(json.getString("filter"));
            if (json.has("isSimilar")) {
                isSimilar = json.getBoolean("isSimilar");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String id = ParamUtil.get(request, "id");
        String pageType = ParamUtil.get(request, "pageType");

        MacroCtlMgr mm = new MacroCtlMgr();
        FormDb sourceFd = new FormDb();
        sourceFd = sourceFd.getFormDb(sourceFormCode);
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        if ("id".equals(byFieldName)) {
            fdao = fdao.getFormDAO(StrUtil.toLong(id), sourceFd);
        } else {
            String sql = "select id from form_table_" + sourceFormCode + " t1 where " + byFieldName + "=" + StrUtil.sqlstr(id);
            // 在sql中加入filter条件，因为byFieldName如果不是id，结果可能会有多条，如果不加filter，则获取可能有误
            IModuleUtil iModuleUtil = (IModuleUtil) SpringUtil.getBean("moduleUtilService");
            if (!"".equals(filter)) {
                String[] r = iModuleUtil.parseFilter(request, ff.getFormCode(), filter);
                sql += " and " + r[0];
            }

            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                long fdaoId = rr.getLong(1);
                fdao = fdao.getFormDAO(fdaoId, sourceFd);
            }
        }
        if (!fdao.isLoaded()) {
            DebugUtil.i(ModuleFieldSelectCtl.class, "getOnSelect", "表单 " + sourceFd.getName() + " 中未找到 " + byFieldName + " 为 " + id + " 的记录");
            return ary;
        }

        for (int i = 0; i < mapAry.length(); i++) {
            JSONObject json = (JSONObject) mapAry.get(i);
            String destF = (String) json.get("destField");
            String sourceF = (String) json.get("sourceField");

            FormField tempFf = null;
            Vector<FormField> vector = fd.getFields();
            for (FormField formField : vector) {
                tempFf = formField;
                if (tempFf.getName().equals(destF)) {
                    break;
                }
            }

            boolean isMacro = false;
            // setValue为module_list_sel.jsp页面中所选择的值
            String val = FuncUtil.renderFieldValue(fdao, fdao.getFormField(sourceF));
            String setValue = val;
            // 如果这个值将被赋值至父页面中的一个宏控件中的时候，则需要将父页面中的宏控件用convertToHTMLCtl重新替换赋值，需要注意的是宏控件传入参数中FormField需要用setValue赋值
            if (tempFf != null && tempFf.getType().equals(FormField.TYPE_MACRO)) {
                tempFf.setValue(setValue);
                isMacro = true;
                request.setAttribute("cwsMapSourceFormField", fdao.getFormField(sourceF));
                if (ConstUtil.PAGE_TYPE_FLOW_SHOW.equals(pageType) || ConstUtil.PAGE_TYPE_SHOW.equals(pageType)) {
                    setValue = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl().converToHtml(request, tempFf, setValue);
                }
                else {
                    setValue = mm.getMacroCtlUnit(tempFf.getMacroType()).getIFormMacroCtl().convertToHTMLCtl(request, tempFf);
                }
            }

            JSONObject jo = new JSONObject();
            jo.put("fieldName", destF);
            jo.put("isMacro", isMacro);
            jo.put("setValue", setValue);
            jo.put("value", val);
            ary.put(jo);
        }

        return ary;
    }
}
