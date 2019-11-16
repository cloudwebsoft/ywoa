package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.flow.FormParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.basic.TreeSelectView;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.officeequip.OfficeStocktakingDb;
import com.redmoon.oa.visual.SQLBuilder;

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
public class BasicSelectCtl extends AbstractMacroCtl  {
    public static final String NONE = "    "; // 无

	private final static String OFFICE_EQUIPMENT = "office_equipment";
    public BasicSelectCtl() {
    }

    public String getDesc(FormField ff) {
        String strDesc = StrUtil.getNullStr(ff.getDescription());
        // 向下兼容
        if ("".equals(strDesc)) {
        	strDesc = ff.getDefaultValueRaw();
        }
        return strDesc;
    }
    
    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
	public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
	    String optName = "";
	
	    String code = getDesc(ff);
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

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		request.setAttribute("filed_" + ff.getName(), ff);
		return convertToHTMLCtl(request, ff.getName(), getDesc(ff));
	}

   /**
    * 用于流程处理
    * @param ff FormField
    * @return Object
    */
	public Object getValueForCreate(int flowId, FormField ff) {
		MacroCtlMgr mm = new MacroCtlMgr();
		// MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		SelectMgr sm = new SelectMgr();
		SelectDb sd = sm.getSelect(ff.getDefaultValue());
		if (sd.getType() == SelectDb.TYPE_LIST)
			return sd.getDefaultValue();
		else
			return ff.getDefaultValue();
   	}

    public static String convertToHTMLCtl(HttpServletRequest request, String fieldName, String code) {
        StringBuffer str = new StringBuffer();
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect(code);
        // onBasciCtlChange默认调用方法在inc/flow_js.jsp中
        // 当需要调用该方法时，应在flow/form_js_....jsp中重新定义，覆盖默认定义
        FormField ff = (FormField)request.getAttribute("filed_" + fieldName);
        if (ff!=null && ff.isReadonly()) {
	        str.append("<select name='" + fieldName + "' id='" + fieldName + "' title='" + ff.getTitle() + "'" +
            " style='background-color:#eeeeee' onfocus='this.defaultIndex=this.selectedIndex;' onchange='this.selectedIndex=this.defaultIndex;'>");
        }
        else {
	        str.append("<select name='" + fieldName + "' id='" + fieldName + "' title='" + ff.getTitle() + "'" +
	                   " onchange='onBasciCtlChange(this)'>");
        }
        if (sd.getType() == SelectDb.TYPE_LIST) {
        	str.append("<option value=''>" + NONE + "</option>");
            Vector v = sd.getOptions(new JdbcTemplate());
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                SelectOptionDb sod = (SelectOptionDb) ir.next();
                if (!sod.isOpen())
                	continue;
                String selected = "";
                if (sod.isDefault())
                    selected = "selected";
                String clr = "";
                if (!sod.getColor().equals(""))
                    clr = " style='color:" + sod.getColor() + "' ";
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
                e.printStackTrace();
            }
        }
        str.append("</select>");
        return str.toString();
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     * @return String
     */
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request,
            FormField ff) {
        // LogUtil.getLog(getClass()).info(getClass() + " ff.getValue()=" + ff.getValue());
        // 因为默认值为code，所以当在流程中创建时，已自动填写了默认值(等于基础数据的编码)，而当在智能模块设计中，则当创建记录时，数据库中还没有值，所以为空
        if (StrUtil.getNullStr(ff.getValue()).equals(getDesc(ff)) || StrUtil.getNullStr(ff.getValue()).equals("")) {
            String code = getDesc(ff);
            SelectMgr sm = new SelectMgr();
            SelectDb sd = sm.getSelect(code);
            if (sd.getType()==SelectDb.TYPE_LIST) {
                Vector v = sd.getOptions(new JdbcTemplate());
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    SelectOptionDb sod = (SelectOptionDb) ir.next();
                    if (sod.isDefault()) {
                        ff.setValue(sod.getValue());
                        break;
                    }
                }
            }
        }
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
    	if (ff.getCondType().equals(SQLBuilder.COND_TYPE_FUZZY)) {
    		return super.convertToHTMLCtlForQuery(request, ff);
    	}
        String code = getDesc(ff);
        StringBuffer str = new StringBuffer();
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect(code);
        if (sd.getType()==SelectDb.TYPE_LIST) {
            Vector v = sd.getOptions(new JdbcTemplate());
            if (ff.getCondType().equals(SQLBuilder.COND_TYPE_MULTI)) {
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    SelectOptionDb sod = (SelectOptionDb) ir.next();
                    str.append("<input name=\"" + ff.getName() + "\" type=\"checkbox\" value=\"" + sod.getValue() + "\" style=\"width:20px\"/>" + sod.getName());
                }
            }
            else {
                str.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "'>");
                str.append("<option value=''>" + NONE + "</option>");
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    SelectOptionDb sod = (SelectOptionDb) ir.next();
                    str.append("<option value='" + sod.getValue() + "' " + ">" + sod.getName() + "</option>");
                }
                str.append("</select>");
            }
        }
        else {
            str.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "'>");
            TreeSelectDb tsd = new TreeSelectDb();
            tsd = tsd.getTreeSelectDb(sd.getCode());
            TreeSelectView tsv = new TreeSelectView(tsd);
            StringBuffer sb = new StringBuffer();
            try {
                str.append(tsv.getTreeSelectAsOptions(sb, tsd, 1));
            }
            catch (ErrMsgException e) {
                e.printStackTrace();
            }
            str.append("</select>");
        }
        return str.toString();
    }
    
	public String getSetCtlValueScript(HttpServletRequest request,
			IFormDAO IFormDao, FormField ff, String formElementId) {
		String str = super.getSetCtlValueScript(request, IFormDao, ff,
					formElementId);
		if (ff.isReadonly()) {
			// 20170825 fgf 尽管这样设置也能有效（仅编辑时），但如果本控件被表单域映射宏控件动态生新生成后会失效
			// 另外，也不太友好，故放弃
			// str += "$(o('" + ff.getName() + "')).click(function() {alert('请勿重新选择！'); return false});\n";
		}
		return str;
	}    

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 参数ff来自于数据库
        String text = "";

        String code = getDesc(ff);
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect(code);

        if (StrUtil.getNullStr(ff.getValue()).equals(getDesc(ff))) {
            if (sd.getType() == SelectDb.TYPE_LIST) {
                Vector v = sd.getOptions(new JdbcTemplate());
                Iterator ir = v.iterator();
                boolean isFound = false;
                while (ir.hasNext()) {
                    SelectOptionDb sod = (SelectOptionDb) ir.next();
                    if (sod.isDefault()) {
                        ff.setValue(sod.getValue());
                        text = sod.getName();
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    String str = "DisableCtl('" + ff.getName() + "', '" +
                                 ff.getType() +
                                 "','" + "" + "','" + "" + "');\n";
                    return str;
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

        String str = "DisableCtl('" + ff.getName() + "', '" +
             ff.getType() +
             "','" + text + "','" + ff.getValue() + "');\n";
        return str;


        // return super.getDisableCtlScript(ff, formElementId);
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String optName = "";
        if (ff.getValue()!=null) {
            String code = getDesc(ff);
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

     public String ajaxOnNestTableCellDBClick(HttpServletRequest request, String formCode, String fieldName, String oldValue, String oldShowValue, String objId) {

         FormDb fd = new FormDb();
         fd = fd.getFormDb(formCode);
         FormField ff = fd.getFormField(fieldName);

         StringBuffer str = new StringBuffer();
         SelectMgr sm = new SelectMgr();
         SelectDb sd = sm.getSelect(getDesc(ff));
         str.append("<select id = '" + objId +
                    "' onchange='onBasciCtlChange(this)'>");
         if (sd.getType() == SelectDb.TYPE_LIST) {
             Vector v = sd.getOptions(new JdbcTemplate());
             Iterator ir = v.iterator();
             while (ir.hasNext()) {
                 SelectOptionDb sod = (SelectOptionDb) ir.next();
                 String selected = "";
                 if (oldValue.equals("")) {
                     if (sod.isDefault())
                         selected = "selected";
                 }
                 else {
                     if (sod.getValue().equals(oldValue))
                         selected = "selected";
                 }
                 String clr = "";
                 if (!sod.getColor().equals(""))
                     clr = " style='color:" + sod.getColor() + "' ";
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
                 e.printStackTrace();
             }
         }
         str.append("</select>");

         if (!oldValue.equals("")) {
             if (sd.getType() == SelectDb.TYPE_TREE) {
                 str.append("<script>");
                 str.append("document.getElementById('" + objId + "').value='" + oldValue + "'");
                 str.append("<script>");
             }
         }

         return str.toString();

    }

    public String getControlType() {
        return FormField.TYPE_SELECT;
    }

    public String getControlOptions(String userName, FormField ff) {
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect(getDesc(ff));
        JSONArray selects = new JSONArray();
        if (sd.getType() == SelectDb.TYPE_LIST) {
            JSONObject select = new JSONObject();
            try {
                select.put("name", NONE);
                select.put("value", "");
                selects.put(select);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }        	
        	
        	Vector v = sd.getOptions(new JdbcTemplate());
            Iterator ir = v.iterator();
           
            while (ir.hasNext()) {
                SelectOptionDb sod = (SelectOptionDb) ir.next();
                select = new JSONObject();
                try {
                    select.put("name", sod.getName());
                    select.put("value", sod.getValue());
                    selects.put(select);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }else{//添加明细表中树状基础数据显示 jfy 2015-05-22
        	String is_office_equipment = ff.getDescription();
        	
        	TreeSelectDb tsd = new TreeSelectDb();
            tsd = tsd.getTreeSelectDb(sd.getCode());
            
            Vector v = null;
            Iterator ir = null;
			try {
				v = new Vector();
				v = tsd.getAllChild(v, tsd);
				if (v != null){
					ir = v.iterator();
				}
			} catch (ErrMsgException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
           
            JSONObject select = new JSONObject();
            try {
				select.put("name", tsd.getName());
				select.put("value", tsd.getCode());
				if(is_office_equipment.equals(OFFICE_EQUIPMENT)){
					//如果是选择办公用品
					if(tsd.getChildCount()>0){
						select.put("relate_value", -1);
					}else{
						OfficeStocktakingDb osd = new OfficeStocktakingDb();
						select.put("relate_value", osd.queryNumByCode(tsd.getCode()));
					}
					
				}
	            selects.put(select);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (ir != null){
	            while (ir.hasNext()) {
	            	TreeSelectDb tsdTemp = (TreeSelectDb)ir.next();
	            	JSONObject selectTemp = new JSONObject();
	                try {
	                	selectTemp.put("name", tsdTemp.getName());
	                	selectTemp.put("value", tsdTemp.getCode());
	                	if(is_office_equipment.equals(OFFICE_EQUIPMENT)){
	    					//如果是选择办公用品
	    					if(tsdTemp.getChildCount()>0){
	    						selectTemp.put("relate_value", -1);
	    					}else{
	    						OfficeStocktakingDb osd = new OfficeStocktakingDb();
	    						selectTemp.put("relate_value", osd.queryNumByCode(tsdTemp.getCode()));
	    					}
	    				}
	                    selects.put(selectTemp);
	                } catch (JSONException ex) {
	                    ex.printStackTrace();
	                }
	            }
			}
            
            
            /*TreeSelectView tsv = new TreeSelectView(tsd);
            StringBuffer sb = new StringBuffer();
            try {
            	selects.put(tsv.getTreeSelectAsOptions(sb, tsd, 1));
                //str.append(tsv.getTreeSelectAsOptions(sb, tsd, 1));
            } catch (ErrMsgException e) {
                e.printStackTrace();
            }*/
        }
        return selects.toString();
    }

    public String getControlValue(String userName, FormField ff) {
         if (ff.getValue()!=null) {
            return ff.getValue();
         }else{
              SelectMgr sm = new SelectMgr();
              SelectDb sd = sm.getSelect(ff.getDefaultValue());
              if (sd.getType() == SelectDb.TYPE_LIST)
                  return sd.getDefaultValue();
              else
                  return ff.getDefaultValue();
         }
    }

    public String getControlText(String userName, FormField ff) {
        String optName = "";
         if (ff.getValue()!=null && !ff.getName().equals("")) {
             String code = getDesc(ff);
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
	public String getValueByName(FormField ff, String name) {
		SelectMgr sm = new SelectMgr();
		String val;
		String code = getDesc(ff);
		SelectDb sd = sm.getSelect(code);
		if (sd.getType() == SelectDb.TYPE_LIST) {
			SelectOptionDb sod = new SelectOptionDb();
			val = sod.getOptionValue(code, name);
		} else {
			TreeSelectDb tsd = new TreeSelectDb();
			tsd = tsd.getTreeSelectDbByName(code, name);
			val = tsd.getCode();
		}

		return val;
	}
}
