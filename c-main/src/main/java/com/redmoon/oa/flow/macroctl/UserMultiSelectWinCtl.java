package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.person.UserDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class UserMultiSelectWinCtl extends AbstractMacroCtl {
    public UserMultiSelectWinCtl() {
    }

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        String realName = "";
        if (!"".equals(StrUtil.getNullStr(ff.getValue()))) {
        	String[] ary = StrUtil.split(ff.getValue(), ",");
            UserDb user = new UserDb();
            for (String s : ary) {
                user = user.getUserDb(s);
                if ("".equals(realName)) {
                    realName = user.getRealName();
                } else {
                    realName += "," + user.getRealName();
                }
            }
        }

        str += "<div class='user_group_box'>";
        str += "<input id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow" +
                "' title=" + ff.getTitle() + " readonly style='float:left; width:" + ff.getCssWidth() + "' value='" + realName + "' />";
        str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' type='hidden'>";
        str += "<div id='" + ff.getName() + "_btn' class='user_group_btn' onclick='openWinUserMultiSelect(findObj(\"" + ff.getName() + "\"))'></div>";
        str += "</div>";
        
        str += "<script>";
        str += "$('#" + ff.getName() + "_btn').hover(\n";
        str += "function() {$('#" + ff.getName() + "_btn').toggleClass('user_group_btn_hover');},\n";
        str += "function() {$('#" + ff.getName() + "_btn').toggleClass('user_group_btn_hover');}\n";
        str += ");\n";
        str += "</script>";
        // str += "&nbsp;<input id='" + ff.getName() + "_btn' type=button class=btn value='选择' onclick='openWinUserMultiSelect(" + ff.getName() + ")'>";
        return str;
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
        String v = StrUtil.getNullStr(fieldValue);
        if (!"".equals(v)) {
            UserDb user = new UserDb();
        	String[] ary = StrUtil.split(v, ",");
        	String realNames = "";
            for (String s : ary) {
                user = user.getUserDb(s);
                if ("".equals(realNames)) {
                    realNames = user.getRealName();
                } else {
                    realNames += "," + user.getRealName();
                }
            }
            return realNames;
        }
        else {
            return "";
        }
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !"".equals(ff.getValue())) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
            UserDb user = new UserDb();
        	String[] ary = StrUtil.split(ff.getValue(), ",");
            for (String s : ary) {
                user = user.getUserDb(s);
                if ("".equals(v)) {
                    v = user.getRealName();
                } else {
                    v += "," + user.getRealName();
                }
            }
        }
        String str = "$('#" + ff.getName() + "_btn').hide();\n";
        return str + "ReplaceCtlWithValue('" + ff.getName() + "_realshow', '" + ff.getType() + "','" + v + "');\n";
     }

     /**
      * 用于nesttable双击单元格编辑时ajax调用
      * @param request HttpServletRequest
      * @param oldValue String 单元格原来的真实值 （如product的ID）
      * @param oldShowValue String 单元格原来的显示值（如product的名称）
      * @param objId String 单元格原来的显示值的input输入框的ID
      * @return String
      */
     @Override
     public String ajaxOnNestTableCellDBClick(HttpServletRequest request, String formCode, String fieldName,
                                              String oldValue,
                                              String oldShowValue, String objId) {
         String str = "";
         // 注意下面三行的顺序不能变
         str += "<input id=\"" + objId + "_realshow\" size=\"10\" readonly name=\"" + objId + "_realshow\" value=\"" + oldShowValue + "\">";
         str += "<input type=\"hidden\" id=\"" + objId + "\" name=\"" + objId + "\" value=\"" + oldValue + "\">";
         str += "<input type=\"button\" class=btn value=\"...\" onclick=\"openWinUserMultiSelect(" + objId + ")\">";
         return str;
     }

     @Override
     public String getDisableCtlScript(FormField ff, String formElementId) {
         String realName = "";
         if (ff.getValue() != null && !"".equals(ff.getValue())) {
         	String[] ary = StrUtil.split(ff.getValue(), ",");
            UserDb user = new UserDb();
             for (String s : ary) {
                 user = user.getUserDb(s);
                 if ("".equals(realName)) {
                     realName = user.getRealName();
                 } else {
                     realName += "," + user.getRealName();
                 }
             }
         }

         String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                      "','" + realName + "','" + ff.getValue() + "');\n";
         str += "DisableCtl('" + ff.getName() + "_realshow', '" + ff.getType() +
                 "','" + "" + "','" + ff.getValue() + "');\n";
         str += "if (o('" + ff.getName() + "_btn')) o('" + ff.getName() + "_btn').outerHTML='';";
         return str;
     }

    @Override
    public String getControlType() {
	    return "userSelect";
	}
	
	@Override
    public String getControlValue(String userName, FormField ff) {
		return StrUtil.getNullStr(ff.getValue());
	}
	
	@Override
    public String getControlText(String userName, FormField ff) {
		if (ff.getValue() == null || "".equals(ff.getValue())) {
			return "";
		} else {
			String str = "";
			UserDb user = new UserDb();
			String users = ff.getValue();
			String[] ary = StrUtil.split(users, ",");
            for (String s : ary) {
                user = user.getUserDb(s);
                if ("".equals(str)) {
                    str = user.getRealName();
                } else {
                    str += "，" + user.getRealName();
                }
            }
			return str;
		}
	}

    @Override
    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

    /**
     * 根据名称取值，用于导入Excel数据
     *
     * @return
     */
    @Override
    public String getValueByName(FormField ff, String name) {
        StringBuffer sb = new StringBuffer();
        String[] realNames = StrUtil.split(name, ",");
        if (realNames!=null) {
            UserDb user = new UserDb();
            for (String realName : realNames) {
                user = user.getUserDbByRealName(realName);
                StrUtil.concat(sb, ",", user.getName());
            }
        }
        return sb.toString();
    }
}
