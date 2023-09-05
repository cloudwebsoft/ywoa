package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.api.IBasicSelectCtl;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.basic.*;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.visual.SQLBuilder;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Vector;

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
public class BasicTreeSelectCtl extends AbstractMacroCtl implements IBasicSelectCtl {

	private final static String OFFICE_EQUIPMENT = "office_equipment";
    public BasicTreeSelectCtl() {
    }

    @Override
    public String getCode(FormField ff) {
        return getBasicCode(ff);
    }

    public static String getBasicCode(FormField ff) {
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        if (strDesc.startsWith("{")) {
            com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(strDesc);
            strDesc = json.getString("code");
        }
        return strDesc;
    }

    public static com.alibaba.fastjson.JSONObject getDesc(FormField ff) {
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
            strDesc = ff.getDefaultValueRaw();
        }
        if (strDesc.startsWith("{")) {
            com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(strDesc);
            return json;
        }
        else {
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("code", strDesc);
            json.put("layer", "");
            return json;
        }
    }

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
	@Override
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
	    String optName = "";

	    String code = getCode(ff);
	    SelectMgr sm = new SelectMgr();
	    SelectDb sd = sm.getSelect(code);
	    if (sd.getType() == SelectDb.TYPE_LIST) {
	        SelectOptionDb sod = new SelectOptionDb();
	        optName = sod.getOptionName(code, fieldValue);
	    } else {
	        TreeSelectDb tsd = new TreeSelectDb();
	        tsd = tsd.getTreeSelectDb(fieldValue);
	        optName = tsd.getName();
	    }

	    return optName;
	}

	@Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		request.setAttribute("filed_" + ff.getName(), ff);
		return convertToHTMLCtl(request, ff.getName(), getCode(ff));
	}

   /**
    * 用于流程处理
    * @param ff FormField
    * @return Object
    */
	@Override
    public Object getValueForCreate(int flowId, FormField ff) {
		// MacroCtlMgr mm = new MacroCtlMgr();
		// MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		SelectMgr sm = new SelectMgr();
		SelectDb sd = sm.getSelect(getCode(ff));
		if (sd.getType() == SelectDb.TYPE_LIST) {
            return sd.getDefaultValue();
        } else {
            return ff.getDefaultValue();
        }
   	}

    @Override
    public String convertToHtmlCtl(HttpServletRequest request, String fieldName, String code) {
	    return convertToHTMLCtl(request, fieldName, code);
    }

    public static String convertToHTMLCtl(HttpServletRequest request, String fieldName, String code) {
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect(code);
        FormField ff = (FormField)request.getAttribute("filed_" + fieldName);

        com.alibaba.fastjson.JSONArray arr = new com.alibaba.fastjson.JSONArray();
        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(sd.getCode());
        getComboTree(tsd, arr);

        String style = "";
        if (!"".equals(ff.getCssWidth())) {
            style = "style='width:" + ff.getCssWidth() + "'";
        } else {
            style = "style='width:150px'";
        }

        String pageType = StrUtil.getNullStr((String) request.getAttribute("pageType"));

        String str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' style='position:absolute; top:-100000px' />";
        if (ff.isEditable() && !pageType.toLowerCase().contains("show")) {
            str += "<input id='" + ff.getName() + "_realshow' title='" + ff.getTitle() + "' " + style + " />";
            str += "<script>\n";
            str += "var " + ff.getName() + "_data=" + arr.toString() + "\n";
            str += "var comboTree" + ff.getName() + " = $(findObj('" + ff.getName() + "_realshow')).comboTree({\n";
            str += "        source : " + ff.getName() + "_data,\n";
            // 如果置isMultiple为true，则会出现checkbox，暂不支持
            str += "        isMultiple: false,\n";
            str += "        cascadeSelect: false,\n";
            str += "        collapse: false,\n";
            str += "        selected: ['" + StrUtil.getNullStr(ff.getValue()) + "'],\n";
            str += "});\n";

            // str += "console.log('comboTree " + ff.getName() + "');\n";

            // 当comboTree为空的时候，失去焦点，并不会触发onChange事件
            str += "comboTree" + ff.getName() + ".onChange(function() {\n" +
                    "findObj('" + ff.getName() + "').value=comboTree" + ff.getName() + ".getSelectedIds()[0];" +
                    "});\n";
            str += "</script>\n";
        }
        return str;
    }

    public static com.alibaba.fastjson.JSONArray getComboTree(TreeSelectDb tsd, com.alibaba.fastjson.JSONArray arr) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        json.put("id", tsd.getCode());
        json.put("title", tsd.getName());
        arr.add(json);

        Vector<TreeSelectDb> v = tsd.getChildren();
        if (v.size() >  0) {
            com.alibaba.fastjson.JSONArray arrChild = new com.alibaba.fastjson.JSONArray();
            json.put("subs", arrChild);
            for (TreeSelectDb child : v) {
                getComboTree(child, arrChild);
            }
        }
        return arr;
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     * @return String
     */
    @Override
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request,
                                                                 FormField ff) {
        // LogUtil.getLog(getClass()).info(getClass() + " ff.getValue()=" + ff.getValue());
        // 因为默认值为code，所以当在流程中创建时，已自动填写了默认值(等于基础数据的编码)，而当在智能模块设计中，则当创建记录时，数据库中还没有值，所以为空
        if (StrUtil.getNullStr(ff.getValue()).equals(getCode(ff)) || ff.getValue()==null) {
            String code = getCode(ff);
            SelectMgr sm = new SelectMgr();
            SelectDb sd = sm.getSelect(code);
            if (sd.getType()==SelectDb.TYPE_LIST) {
                boolean isDefault = false;
                Vector<SelectOptionDb> v = sd.getOptions(new JdbcTemplate());
                for (SelectOptionDb sod : v) {
                    if (sod.isDefault()) {
                        isDefault = true;
                        ff.setValue(sod.getValue());
                        break;
                    }
                }
                if (!isDefault) {
                    ff.setValue(ff.getDefaultValue());
                }
            }
        }
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
    	if (ff.getCondType().equals(SQLBuilder.COND_TYPE_FUZZY)) {
    		return super.convertToHTMLCtlForQuery(request, ff);
    	}

    	boolean isMultiple = ff.getCondType().equals(SQLBuilder.COND_TYPE_MULTI);
        String code = getCode(ff);
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect(code);

        com.alibaba.fastjson.JSONArray arr = new com.alibaba.fastjson.JSONArray();
        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(sd.getCode());
        getComboTree(tsd, arr);

        String str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' type='hidden' />";
        str += "<input id='" + ff.getName() + "_realshow' title='" + ff.getTitle() + "' />\n";
        str += "<script>\n";
        str += "var " + ff.getName() + "_data=" + arr.toString() + "\n";
        str += "var comboTree" + ff.getName() + " = $('#" + ff.getName() + "_realshow').comboTree({\n";
        str += "        source : " + ff.getName() + "_data,\n";
        str += "        isMultiple: " + isMultiple + ",\n";
        str += "        cascadeSelect: false,\n";
        str += "        collapse: false,\n";
        str += "        selected: ['" + StrUtil.getNullStr(ff.getValue()) + "'],\n";
        str += "});\n";

        str += "console.log('convertToHTMLCtlForQuery comboTree " + ff.getName() + "');\n";

        // 使支持多选
        if (isMultiple) {
            str += "comboTree" + ff.getName() + ".onChange(function(){\n";
            str += "    console.log(comboTree" + ff.getName() + ".getSelectedIds());\n";
            str += "    findObj('" + ff.getName() + "').value = comboTree" + ff.getName() + ".getSelectedIds();\n";
            str += "});\n";
        } else {
            // 当comboTree为空的时候，失去焦点，并不会触发onChange事件
            str += "comboTree" + ff.getName() + ".onChange(function() {\n" +
                    "findObj('" + ff.getName() + "').value=comboTree" + ff.getName() + ".getSelectedIds()[0];" +
                    "});\n";
        }
        str += "</script>\n";
        return str;
    }

	@Override
    public String getSetCtlValueScript(HttpServletRequest request,
                                       IFormDAO IFormDao, FormField ff, String formElementId) {
		String str = super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
		// 20220113 有时只读时仍需能够传入参数，如公告中的项目阶段，自动带入但不可更改
		if (false && ff.isReadonly()) {
			// 20170825 fgf 尽管这样设置也能有效（仅编辑时），但如果本控件被表单域映射宏控件动态生新生成后会失效
			// 另外，也不太友好，故放弃
			// str += "$(o('" + ff.getName() + "')).click(function() {alert('请勿重新选择！'); return false});\n";
		}
		else {
            String strDesc = StrUtil.getNullStr(ff.getDescription());
            // 向下兼容
            if ("".equals(strDesc)) {
                strDesc = ff.getDefaultValueRaw();
            }
            if (strDesc.startsWith("{")) {
                com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(strDesc);
                String value = StrUtil.getNullStr(ff.getValue());
                // 如果未赋予值，则从request参数中取值
                if ("".equals(value)) {
                    if (json.containsKey("requestParam") && !"".equals(json.getString("requestParam"))) {
                        // 来自于指定的参数名称
                        value = ParamUtil.get(request, json.getString("requestParam"));
                    } else {
                        // 默认以字段名作为参数从request中获取
                        value = ParamUtil.get(request, ff.getName());
                    }
                    return "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '" + value + "');\n";
                }
            }
        }
		return str;
	}

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 参数ff来自于数据库
        String text = "";

        String code = getCode(ff);
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect(code);

        if (StrUtil.getNullStr(ff.getValue()).equals(code)) {
            if (sd.getType() == SelectDb.TYPE_LIST) {
                boolean isFound = false;
                Vector<SelectOptionDb> v = sd.getOptions(new JdbcTemplate());
                for (SelectOptionDb sod : v) {
                    if (sod.isDefault()) {
                        ff.setValue(sod.getValue());
                        text = sod.getName();
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    return "DisableCtl('" + ff.getName() + "', '" +
                                 ff.getType() +
                                 "','" + "" + "','" + "" + "');\n";
                }
            }
        }
        else {
            if (sd.getType()==SelectDb.TYPE_LIST) {
                SelectOptionDb sod = new SelectOptionDb();
                text = sod.getOptionName(code, ff.getValue());
            }
            else {
                TreeSelectDb tsd = new TreeSelectDb();
                tsd = tsd.getTreeSelectDb(ff.getValue());
                text = tsd.getName();
            }
        }

        return "DisableCtl('" + ff.getName() + "', '" +
             ff.getType() +
             "','" + text + "','" + ff.getValue() + "');\n";
        // return super.getDisableCtlScript(ff, formElementId);
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        String optName = "";
        if (ff.getValue()!=null) {
            String code = getCode(ff);
            SelectMgr sm = new SelectMgr();
            SelectDb sd = sm.getSelect(code);
            if (sd.getType()==SelectDb.TYPE_LIST) {
                SelectOptionDb sod = new SelectOptionDb();
                optName = sod.getOptionName(code, ff.getValue());
            }
            else {
                TreeSelectDb tsd = new TreeSelectDb();
                tsd = tsd.getTreeSelectDb(ff.getValue());
                optName = tsd.getName();
            }
        }
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + optName + "');\n";
     }

     @Override
     public String ajaxOnNestTableCellDBClick(HttpServletRequest request, String formCode, String fieldName, String oldValue, String oldShowValue, String objId) {

         FormDb fd = new FormDb();
         fd = fd.getFormDb(formCode);
         FormField ff = fd.getFormField(fieldName);

         StringBuilder str = new StringBuilder();
         SelectMgr sm = new SelectMgr();
         SelectDb sd = sm.getSelect(getCode(ff));
         str.append("<select id = '" + objId +
                    "' onchange='onBasciCtlChange(this)'>");
         if (sd.getType() == SelectDb.TYPE_LIST) {
             Vector<SelectOptionDb> v = sd.getOptions(new JdbcTemplate());
             for (SelectOptionDb sod : v) {
                 String selected = "";
                 if (oldValue.equals("")) {
                     if (sod.isDefault()) {
                         selected = "selected";
                     }
                 } else {
                     if (sod.getValue().equals(oldValue)) {
                         selected = "selected";
                     }
                 }
                 String clr = "";
                 if (!sod.getColor().equals("")) {
                     clr = " style='color:" + sod.getColor() + "' ";
                 }
                 str.append("<option value='" + sod.getValue() + "' " + selected + clr +
                         ">" +
                         sod.getName() +
                         "</option>");
             }
         } else {
             TreeSelectDb tsd = new TreeSelectDb();
             tsd = tsd.getTreeSelectDb(sd.getCode());
             TreeSelectView tsv = new TreeSelectView(tsd);
             StringBuffer sb = new StringBuffer();
             try {
                 str.append(tsv.getTreeSelectAsOptions(sb, tsd, 1));
             } catch (ErrMsgException e) {
                 LogUtil.getLog(getClass()).error(e);
             }
         }
         str.append("</select>");

         if (!"".equals(oldValue)) {
             if (sd.getType() == SelectDb.TYPE_TREE) {
                 str.append("<script>");
                 str.append("document.getElementById('" + objId + "').value='" + oldValue + "'");
                 str.append("<script>");
             }
         }

         return str.toString();

    }

    @Override
    public String getControlType() {
        return FormField.TYPE_SELECT;
    }

    @Override
    public String getControlOptions(String userName, FormField ff) {
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect(getCode(ff));
        JSONArray selects = new JSONArray();
        if (sd.getType() == SelectDb.TYPE_LIST) {
            JSONObject select = new JSONObject();
            try {
                select.put("name", ConstUtil.NONE);
                select.put("value", "");
                selects.put(select);
            } catch (JSONException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }

        	Vector<SelectOptionDb> v = sd.getOptions(new JdbcTemplate());
            for (SelectOptionDb sod : v) {
                select = new JSONObject();
                try {
                    select.put("name", sod.getName());
                    select.put("value", sod.getValue());
                    selects.put(select);
                } catch (JSONException ex) {
                    LogUtil.getLog(getClass()).error(ex);
                }
            }
        }else{//添加明细表中树状基础数据显示 jfy 2015-05-22
        	String is_office_equipment = ff.getDescription();

        	TreeSelectDb tsd = new TreeSelectDb();
            tsd = tsd.getTreeSelectDb(sd.getCode());

            Vector<TreeSelectDb> v = null;
            Iterator<TreeSelectDb> ir = null;
			try {
				v = new Vector<>();
				v = tsd.getAllChild(v, tsd);
				if (v != null){
					ir = v.iterator();
				}
			} catch (ErrMsgException e1) {
				e1.printStackTrace();
			}

            JSONObject select = new JSONObject();
            try {
				select.put("name", tsd.getName());
				select.put("value", tsd.getCode());
	            selects.put(select);
			} catch (JSONException e) {
				LogUtil.getLog(getClass()).error(e);
			}
			if (ir != null){
	            while (ir.hasNext()) {
	            	TreeSelectDb tsdTemp = (TreeSelectDb)ir.next();
	            	JSONObject selectTemp = new JSONObject();
	                try {
	                	selectTemp.put("name", tsdTemp.getName());
	                	selectTemp.put("value", tsdTemp.getCode());
	                    selects.put(selectTemp);
	                } catch (JSONException ex) {
	                    LogUtil.getLog(getClass()).error(ex);
	                }
	            }
			}
            
            /*TreeSelectView tsv = new TreeSelectView(tsd);
            StringBuffer sb = new StringBuffer();
            try {
            	selects.put(tsv.getTreeSelectAsOptions(sb, tsd, 1));
                //str.append(tsv.getTreeSelectAsOptions(sb, tsd, 1));
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
            }*/
        }
        return selects.toString();
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
         if (ff.getValue()!=null) {
            return ff.getValue();
         }else{
              SelectMgr sm = new SelectMgr();
              SelectDb sd = sm.getSelect(getCode(ff));
              if (sd.getType() == SelectDb.TYPE_LIST) {
                  return sd.getDefaultValue();
              } else {
                  return ff.getDefaultValue();
              }
         }
    }

    @Override
    public String getControlText(String userName, FormField ff) {
        String optName = "";
         if (ff.getValue()!=null && !"".equals(ff.getName())) {
             String code = getCode(ff);
             SelectMgr sm = new SelectMgr();
             SelectDb sd = sm.getSelect(code);
             if (sd.getType()==SelectDb.TYPE_LIST) {
                 SelectOptionDb sod = new SelectOptionDb();
                 optName = sod.getOptionName(code, ff.getValue());
             }
             else {
                 TreeSelectDb tsd = new TreeSelectDb();
                 tsd = tsd.getTreeSelectDb(ff.getValue());
                 optName = tsd.getName();
             }
         }
         return optName;
    }

    /**
     * 根据名称取值，用于导入Excel数据
     * @return
     */
	@Override
    public String getValueByName(FormField ff, String name) {
	    if (StringUtils.isEmpty(name)) {
	        return "";
        }
		SelectMgr sm = new SelectMgr();
		String val = null;
		String code = getCode(ff);
		SelectDb sd = sm.getSelect(code);
		if (sd.getType() == SelectDb.TYPE_LIST) {
			SelectOptionDb sod = new SelectOptionDb();
			val = sod.getOptionValue(code, name);
		} else {
			TreeSelectDb tsd = new TreeSelectDb();
			tsd = tsd.getTreeSelectDbByName(code, name);
			if (tsd != null) {
                val = tsd.getCode();
            }
			else {
			    DebugUtil.e(getClass(), "", "树形基础数据，编码：" + code + " 名称：" + name + " 对应的值不存在");
            }
		}
		return val;
	}

    @Override
    public boolean validate(HttpServletRequest request, IFormDAO fdao, FormField ff, FileUpload fu) throws ErrMsgException {
        com.alibaba.fastjson.JSONObject desc = getDesc(ff);
        if (desc.containsKey("layer")) {
            if ("0".equals(desc.getString("layer"))) {
                String val = fu.getFieldValue(ff.getName());
                // String code = desc.getString("code");

                TreeSelectDb tsd = new TreeSelectDb();
                tsd = tsd.getTreeSelectDb(val);
                if (tsd.getChildCount() > 0) {
                    throw new ErrMsgException("请选择最末级的叶子节点，当前选的是：" + tsd.getName());
                }
            }
        }
        return true;
    }

    @Override
    public String getSqlForQuery(HttpServletRequest request, FormField ff, String value, boolean isBlur) {
        if ("".equals(value)){
            return "";
        }
        String nameCond = StrUtil.getNullStr((String)request.getAttribute("name_cond"));
        if (SQLBuilder.COND_TYPE_MULTI.equals(nameCond)) {
            String[] ary = value.split(",");
            StringBuilder sb = new StringBuilder();
            for (String val : ary) {
                StrUtil.concat(sb, ",", StrUtil.sqlstr(val));
            }
            return sb.toString();
        } else {
            return "";
        }
    }
}
