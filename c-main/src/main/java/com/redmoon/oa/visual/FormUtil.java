package com.redmoon.oa.visual;

import cn.js.fan.util.*;
import com.cloudweb.oa.api.IAttachmentCtl;
import com.cloudweb.oa.api.IAttachmentsCtl;
import com.cloudweb.oa.api.IFormulaCtl;
import com.cloudweb.oa.api.IImageCtl;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.query.QueryScriptUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormQueryDb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFormMacroCtl;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 * 
 * <p>Company: </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FormUtil {
	public static final String ERROR_PARSE = "ERR_PARSE";
	
    public FormUtil() {
    }

    /**
     * PC端生成检测字段是否唯一的脚本
     * @param id
     * @param fields
     * @return
     */
    public static String doGetCheckJSUnique(long id, Vector<FormField> fields) {
        StringBuffer sbFields = new StringBuffer();
        Iterator<FormField> ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = ir.next();
            if (ff.isUnique()) {
                StrUtil.concat(sbFields, ",", ff.getName());
            }
        }

        StringBuffer sbFieldsNest = new StringBuffer();
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = ir.next();
            if (ff.isUniqueNest()) {
                StrUtil.concat(sbFieldsNest, ",", ff.getName());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<script>\n");
        sb.append("$(function() {\n");
        ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = ir.next();
            if (ff.isUnique()) {
                sb.append("checkFieldIsUnique(" + id + ", '" + ff.getFormCode() + "', '" + ff.getName() + "', '" + sbFields.toString() + "');\n"); // 如果是添加页面，则id为-1
            }
            else if (ff.isUniqueNest()) {
                sb.append("checkFieldIsUniqueNest(" + id + ", '" + ff.getFormCode() + "', '" + ff.getName() + "', '" + sbFieldsNest.toString() + "');\n"); // 如果是添加页面，则id为-1
            }
        }
        sb.append("})\n");
        sb.append("</script>\n");
        return sb.toString();
    }

    public static String doGetCheckJSFormat(Vector<FormField> fields) {
        StringBuffer sb = new StringBuffer();
        sb.append("<script>\n");
        sb.append("$(function() {\n");
        for (FormField ff : fields) {
            if (ff.getFormat().equals(FormField.FORMAT_THOUSANDTH)) {
                sb.append("doFormat('" + ff.getName() + "');\n");
            }
        }
        sb.append("})\n");
        sb.append("</script>\n");
        return sb.toString();
    }

   /**
     * 生成前台验证JS脚本
     * @param request HttpServletRequest
     * @param fields Vector
     * @return String
     */
    public static String doGetCheckJS(HttpServletRequest request, Vector fields) {
        String pageType = ParamUtil.get(request, "pageType");
        MacroCtlMgr mm = new MacroCtlMgr();
        ParamChecker pck = new ParamChecker(request);
        StringBuffer sb = new StringBuffer();
        String someNotNullFieldName = "";
        sb.append("<script>\n");

        sb.append(FormUtil.getFormNotFoundThenReturnJs(request, true));

        FormField ffSubmit = null;
        Iterator ir = fields.iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField) ir.next();
            if (ff==null) {
            	continue;
            }
            if (!ff.isEditable()) {
                continue;
            }

            if (ff.getHide()!=FormField.HIDE_NONE) {
                continue;
            }

            // 附件宏控件及富本文编辑宏控件不作前台是否为空的验证
            if ("macro_attachment".equals(ff.getMacroType()) || "macro_ueditor".equals(ff.getMacroType())) {
                continue;
            }

            // 20230319 加入对于hidden的判断
            if (ff.isHidden()) {
                continue;
            }
            // 20230319 如果为编辑页面，则不检查文件型的控件是否为空
            // 图片、文件宏控件在编辑时，应该允许为空
            if (pageType.contains("edit")) {
                if (ff.getType().equals(FormField.TYPE_MACRO)) {
                    if ("macro_image".equals(ff.getMacroType()) || "macro_attachment".equals(ff.getMacroType())
                        || "macro_attachments".equals(ff.getMacroType())
                    ) {
                        continue;
                    }
                }
            }

            // 如果是函数型的，则不进行前台验证，20181201 fgf 仍改为前台进验证
            if (ff.isFunc()) {
            	sb.append("$('input[name=" + ff.getName() + "]').attr(\"readonly\",\"readonly\");\n");
            }
            
			String js = getCheckFieldJS(pck, ff);
			// 不用宏控件作为livevalidation的onsubmit元素
			if (someNotNullFieldName.equals("")) {
				// 如果没有add过，则当设置 var automaticOn***Submit =
				// f_***.form.onsubmit时，会出错
				if (js.contains(".add(")) {
					if (ff.getType().equals(FormField.TYPE_MACRO)) {	
		            	MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());						
			            if (mu!=null && mu.getNestType() != MacroCtlUnit.NEST_TYPE_NORMAIL) {            
							// 不能用嵌套表格作为onsubmit的元素
			            	ffSubmit = ff;
			            }
					}
					else {
						ffSubmit = ff;						
					}
					if (!ff.getType().equals(FormField.TYPE_MACRO)) {
						someNotNullFieldName = ff.getName();
					}
				}
			}
            sb.append(js);
        }
        
        // 如果未找到不是宏控件的适用于livevalidation检查的域
        if ("".equals(someNotNullFieldName)) {
        	if (ffSubmit!=null) {
        		someNotNullFieldName = ffSubmit.getName();
        	}
        }

        // 可能会出现someNotNullFieldName为空的情况，如：没有必填项，所以增加fieldNameFirst
        if (!"".equals(someNotNullFieldName)) {
            sb.append("if (f_" + someNotNullFieldName + ".form) {\n");
            sb.append("var automaticOn" + someNotNullFieldName + "Submit = " +
                      "f_" + someNotNullFieldName + ".form.onsubmit;\n");
            sb.append("f_" + someNotNullFieldName + ".form.onsubmit = function() {\n");
            sb.append("var valid = automaticOn" + someNotNullFieldName + "Submit();\n");
            sb.append("if(valid)\n");
            sb.append("        return true;\n");
            sb.append("else\n");
            sb.append("        return false;\n");
            sb.append("}\n");
            sb.append("} else { console.warn('f_" + someNotNullFieldName + ".form 不存在'); }\n");
        }

        sb.append(FormUtil.getFormNotFoundThenReturnJs(request, false));

        sb.append("</script>\n");
        return sb.toString();
    }

    /**
     * 根据表单的属性成生成规则字符串，用以进行有效性验证
     * @param ff FormField
     * @return String
     */
    public static String getCheckFieldJS(ParamChecker pck, FormField ff) {
        String fieldName = ff.getName();

        String ruleStr = "";
        String cNull = "not";
        if (ff.isCanNull()) {
            cNull = "empty";
        }

        // 置宏控件的数据类型fieldType，实际上2.2及之前版本均为varchar
        // 由于宏控件的值有些是通过程序动态获得的，所以不宜通过ParamChecker来进行有效性验证，因此在表单设计的时候，只设置其是否必填，而不设置最大和最小值
        // 在仅设置必填项的时候也有问题，如：意见框，用户可以先保存意见，然后再转交下一步，但是因为转交时设置了必填，就要重复填写意见，这就需要管理员设置的时候要仔细
        if (ff.getType().equals(FormField.TYPE_MACRO)) {
            // LogUtil.getLog(getClass()).info("saveDAO1 " + fieldName() + " getType()=" + getType() + " getMacroType=" + this.getMacroType());
            MacroCtlMgr mm = new MacroCtlMgr();
            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
            if (mu!=null) {
                IFormMacroCtl ifmctl = mu.getIFormMacroCtl();
                if (ifmctl != null) {
                    ff.setFieldType(mu.getIFormMacroCtl().getFieldType(ff));
                } else {
                    LogUtil.getLog(FormUtil.class).error("getCheckFieldJS: 宏控件" +
                            ff.getMacroType() + "不存在！");
                }
            }
            else {
                LogUtil.getLog(FormUtil.class).error("getCheckFieldJS2: 宏控件" +
                            ff.getMacroType() + "不存在！");
            }
        }

        StringBuilder sb = new StringBuilder();

        if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR) {
            ruleStr = ParamChecker.TYPE_STRING;
            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }
            // LogUtil.getLog(FormUtil.class).info("ruleStr=" + ruleStr);
            sb.append(pck.getCheckJSOfFieldString(ruleStr));
        } else if (ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
            ruleStr = ParamChecker.TYPE_STRING;

            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }

            sb.append(pck.getCheckJSOfFieldString(ruleStr));
        }
        else if (ff.getFieldType() == FormField.FIELD_TYPE_INT) {
            if (ff.isCanNull()) {
                cNull = "allow";
            }
            ruleStr = ParamChecker.TYPE_INT;

            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }
            sb.append(pck.getCheckJSOfFieldInt(ruleStr));
        }
        else if (ff.getFieldType() == FormField.FIELD_TYPE_LONG) {
            if (ff.isCanNull()) {
                cNull = "allow";
            }
            ruleStr = ParamChecker.TYPE_LONG;

            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }

            sb.append(pck.getCheckJSOfFieldLong(ruleStr));
        }
        else if (ff.getFieldType() == FormField.FIELD_TYPE_BOOLEAN) {
            ruleStr = ParamChecker.TYPE_BOOLEAN;

            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }

            sb.append(pck.getCheckJSOfFieldBoolean(ruleStr));
        }
        else if (ff.getFieldType() == FormField.FIELD_TYPE_FLOAT) {
            if (ff.isCanNull()) {
                cNull = "allow";
            }
            ruleStr = ParamChecker.TYPE_FLOAT;

            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }
            sb.append(pck.getCheckJSOfFieldFloat(ruleStr));
        }
        else if (ff.getFieldType() == FormField.FIELD_TYPE_DOUBLE) {
            if (ff.isCanNull()) {
                cNull = "allow";
            }
            ruleStr = ParamChecker.TYPE_DOUBLE;

            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }
            sb.append(pck.getCheckJSOfFieldDouble(ruleStr));
        }
        else if (ff.getFieldType() == FormField.FIELD_TYPE_DATE) {
            if (ff.isCanNull()) {
                cNull = "allow";
            }
            ruleStr = ParamChecker.TYPE_DATE;

            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }
            ruleStr += ",format=yyyy-MM-dd";
            sb.append(pck.getCheckJSOfFieldDate(ruleStr));
        }
        else if (ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
            if (ff.isCanNull()) {
                cNull = "allow";
            }
            ruleStr = ParamChecker.TYPE_DATE;

            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }
            // 因为pck是根据规则从fu从取出ff的值，故只能取出日期部分，而不能取出时间部分，所以不能用format=yyyy-MM-dd HH:mm:ss，而应用format=yyyy-MM-dd
            // ruleStr += ",format=yyyy-MM-dd HH:mm:ss";
            ruleStr += ",format=yyyy-MM-dd";

            sb.append(pck.getCheckJSOfFieldDate(ruleStr));

            // 时间部分不再使用,也不再做判断
            /*
            if (cNull.equals("not")) {
                ruleStr = ParamChecker.TYPE_STRING;
                ruleStr += "," + fieldName + "_time," +
                        ff.getTitle() + "时间," + cNull;
                LogUtil.getLog(FormUtil.class).info("ruleStr=" + ruleStr);
                sb.append(pck.getCheckJSOfFieldString(ruleStr));
            }*/
        }
        else if (ff.getFieldType() == FormField.FIELD_TYPE_PRICE) {
            if (ff.isCanNull()) {
                cNull = "allow";
            }
            ruleStr = ParamChecker.TYPE_DOUBLE;

            ruleStr += "," + fieldName + "," +
                    ff.getTitle() + "," + cNull;
            if (!"".equals(ff.getRule())) {
                ruleStr += "," + ff.getRule();
            }

            sb.append(pck.getCheckJSOfFieldDouble(ruleStr));
        }

        return sb.toString();
    }
    
    /**
     * 解析字符串，将其中的表单域替换成表单值
     * {$表单域的编码或者名称}
     * 先根据编码替换，不行再找名称，防止名称重复
     * @param strWithFields 含有表单域的字符串
     * @param ifdao
     * @return
     */
    public static String parseAndSetFieldValue(String strWithFields, IFormDAO ifdao) {
        if (!strWithFields.contains("$")) {
            return strWithFields;
        }
    	FormDb fd = ifdao.getFormDb();
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(strWithFields);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fieldTitle = m.group(1);
            // 制作大亚表单时发现，差旅费报销单中字段名称会有重复，所以这里先找编码，不行再找名称，防止名称重复
            FormField field = fd.getFormField(fieldTitle);
            if (field == null) {
                field = fd.getFormFieldByTitle(fieldTitle);
                if (field == null) {
                    LogUtil.getLog(FormUtil.class).error("表单：" + fd.getName() + "，脚本：" + strWithFields + "中，字段：" + fieldTitle + " 不存在！");
                }
            }
            
            if (field==null) {
                if ("cws_id".equals(fieldTitle)) {
                    m.appendReplacement(sb, ifdao.getCwsId());
                }
                else if ("id".equals(fieldTitle)) {
                    m.appendReplacement(sb, String.valueOf(ifdao.getId()));
                }
                else if ("flowId".equalsIgnoreCase(fieldTitle)) {
                    m.appendReplacement(sb, String.valueOf(ifdao.getFlowId()));
                }
                else if ("cws_status".equals(fieldTitle)) {
                    m.appendReplacement(sb, String.valueOf(ifdao.getCwsStatus()));
                }
                else {
                    LogUtil.getLog(FormUtil.class).error("parseAndSetFieldValue:" + fieldTitle + " 不存在");
                    // m.appendReplacement(sb, fieldTitle + " 不存在");
                    return ERROR_PARSE;
                }
            }
            else {
            	// LogUtil.getLog(getClass()).info(FormUtil.class + " ifdao.getFieldValue(field.getName())=" + ifdao.getFieldValue(field.getName()) + " fieldTitle=" + fieldTitle);
            	String val = StrUtil.getNullStr(ifdao.getFieldValue(field.getName()));
            	if (field.getType().equals(FormField.TYPE_MACRO)) {
	            	// 如果是宏控件，跳过默认值（如：SQL宏控件）
	            	if (val.equals(field.getDefaultValueRaw())) {
	            		val = "";
	            	}
            	}
            	m.appendReplacement(sb, val);
            }
        }
        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * 取出嵌套表中需要sum的字段，用于传至手机端
     *
     * @param fdRelated
     * @param fdParent
     * @param cwsId
     * @return
     */
    public static JSONObject getSums(FormDb fdRelated, FormDb fdParent, String cwsId) {
        JSONObject sums = new JSONObject();
        MacroCtlMgr mm = new MacroCtlMgr();
        // 遍历主表中的函数，取出所有用到的sum(nest.***)字段
        for (FormField ff : fdParent.getFields()) {
            if (ff.getType().equals(FormField.TYPE_CALCULATOR)) {
                String formula = ff.getDefaultValueRaw();
                if ("".equals(formula)) {
                    formula = ff.getDescription();
                }
                String calcFieldName = ff.getName();
                StringBuffer sb = new StringBuffer();

                boolean isFound = false;
                // 解析其中的sum
                Pattern p = Pattern.compile(
                        "sum\\((.*?)\\)", // 前为utf8中文范围，后为gb2312中文范围
                        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(formula);
                while (m.find()) {
                    String nestFieldName = m.group(1);
                    if (nestFieldName.startsWith("nest.")) {
                        nestFieldName = nestFieldName.substring(5);

                        // 如果关联表单中有此字段
                        FormField nestField = fdRelated.getFormField(nestFieldName);
                        if (nestField == null) {
                            continue;
                        }
                        isFound = true;
                        // 20210511 present为计算控件相关联的嵌套表单的编码，如果与fdRelated不相等，则说明尽管中有与sum(field)中同名的field，但却不是计算控件相关的嵌套表
                        if (!StringUtils.isEmpty(nestField.getPresent())) {
                            String nestFormCode = nestField.getPresent();
                            if (!nestFormCode.equals(fdRelated.getCode())) {
                                continue;
                            }
                        }

                        boolean isFormulaCtl = false;
                        boolean isQuoteCtl = false;
                        IFormMacroCtl ifmctl = null;
                        // 判断nestField是否为函数宏控件，如果是，则需取得函数宏控件的值进行累加
                        if (nestField.getType().equals(FormField.TYPE_MACRO)) {
                            MacroCtlUnit mu = mm.getMacroCtlUnit(nestField.getMacroType());
                            ifmctl = mu.getIFormMacroCtl();
                            DebugUtil.i(FormUtil.class, "getSums", ifmctl.getClass().getName());
                            if (ifmctl instanceof IFormulaCtl) {
                                String desc = nestField.getDescription();
                                try {
                                    JSONObject json = new JSONObject(desc);
                                    if (json.has("isAutoWhenList")) {
                                        if (json.getBoolean("isAutoWhenList")) {
                                            isFormulaCtl = true;
                                        }
                                    }
                                } catch (JSONException e) {
                                    LogUtil.getLog(FormUtil.class).error(e);
                                }
                            } else if (ifmctl.getClass().getName().contains("QuoteFieldCtl") || ifmctl.getClass().getName().contains("QuoteDoubleFieldCtl")) {
                                isQuoteCtl = true;
                            }
                        }
                        if (isFormulaCtl || isQuoteCtl) {
                            double val = 0;
                            String sql = "select id from ft_" + fdRelated.getCode() + " where cws_parent_form=" + StrUtil.sqlstr(fdParent.getCode()) + " and cws_id='" + cwsId + "'";
                            FormDAO fao = new FormDAO();
                            try {
                                Vector<FormDAO> v = fao.list(fdRelated.getCode(), sql);
                                for (FormDAO fdao : v) {
                                    FormField ffNestField = fdao.getFormField(nestFieldName);

                                    IFormDAO ifdao = RequestUtil.getFormDAO(SpringUtil.getRequest());
                                    RequestUtil.setFormDAO(SpringUtil.getRequest(), fdao);
                                    double value = StrUtil.toDouble(ifmctl.converToHtml(SpringUtil.getRequest(), ffNestField, fdao.getFieldValue(nestFieldName)), 0);
                                    val += value;
                                    if (ifdao != null) {
                                        // 恢复request中原来的fdao，以免其它地方要用到的fdao，被改为本方法的fdao
                                        RequestUtil.setFormDAO(SpringUtil.getRequest(), ifdao);
                                    }
                                }
                            } catch (ErrMsgException e) {
                                LogUtil.getLog(FormUtil.class).error(e);
                            }
                            m.appendReplacement(sb, String.valueOf(val));
                            /*try {
                                sums.put(nestFieldName, String.valueOf(ff.convertToHtmlForCalculate(fdParent, String.valueOf(val))));
                            } catch (JSONException e) {
                                LogUtil.getLog(getClass()).error(e);
                            }*/
                        } else {
                            String sql = "select sum(" + nestFieldName + ") from ft_" + fdRelated.getCode() + " where cws_parent_form=" + StrUtil.sqlstr(fdParent.getCode()) + " and cws_id='" + cwsId + "'";
                            JdbcTemplate jt = new JdbcTemplate();
                            ResultIterator ri;
                            try {
                                ri = jt.executeQuery(sql);
                                if (ri.hasNext()) {
                                    ResultRecord rr = ri.next();
                                    double val = rr.getDouble(1);
                                    m.appendReplacement(sb, String.valueOf(val));
                                    /*try {
                                        sums.put(nestFieldName, String.valueOf(ff.convertToHtmlForCalculate(fdParent, NumberUtil.toString(val))));
                                    } catch (JSONException e) {
                                        LogUtil.getLog(getClass()).error(e);
                                    }*/
                                } else {
                                    /*try {
                                        sums.put(nestFieldName, "0");
                                    } catch (JSONException e) {
                                        LogUtil.getLog(getClass()).error(e);
                                    }*/
                                    m.appendReplacement(sb, "0");
                                }
                            } catch (SQLException e) {
                                LogUtil.getLog(FormUtil.class).error(e);
                            }
                        }
                    }
                }
                if (isFound) {
                    m.appendTail(sb);
                    ScriptEngineManager manager = new ScriptEngineManager();
                    ScriptEngine engine = manager.getEngineByName("javascript");
                    try {
                        double val = StrUtil.toDouble(engine.eval(sb.toString()).toString(), -65536);
                        // 20220730 将o由原来的sum(nest.je)中的je改为计算控件的字段名
                        sums.put(calcFieldName, String.valueOf(ff.convertToHtmlForCalculate(fdParent, NumberUtil.toString(val))));
                    } catch (ScriptException ex) {
                        LogUtil.getLog(FormUtil.class).error("formula=" + formula);
                        LogUtil.getLog(FormUtil.class).error(StrUtil.trace(ex));
                    } catch (JSONException e) {
                        LogUtil.getLog(FormUtil.class).error(e);
                    }
                }
            }
        }

        return sums;
    }

    public static String getFormNotFoundThenReturnJs(HttpServletRequest request, boolean isStart) {
        return "";
    }
}
