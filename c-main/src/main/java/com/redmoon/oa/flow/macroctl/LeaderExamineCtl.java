package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.base.IFormDAO;

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
public class LeaderExamineCtl extends AbstractMacroCtl {
    public LeaderExamineCtl() {
           super();
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
       String defaultValue = ff.getDefaultValue();
       String str = "";
       str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' title='请输入审批内容'>";
       str += "<script src='" + request.getContextPath() + "/flow/macro/macro_leader_examine_ctl.jsp?fieldName=" + ff.getName() + "'></script>";
       return str;
   }

   @Override
   public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request, FormField ff) {
       if (ff.getValue()==null || ff.getValue().equals(ff.getDefaultValue())) {
           ff.setValue("");
       }
       return FormField.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
   }

/*
   public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
       if (ff.getValue().equals(ff.getDefaultValue())) {
           ff.setValue("");
           return "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '');";
       }
       else
           return FormField.getSetCtlValueScript(request, IFormDao, ff, formElementId);
    }
*/

    public Object getValueForCreate(FormField ff) {
        return "";
    }

    @Override
    public String getHideCtlScript(FormField formField, String string) {
        return "";
    }

    public String getControlType() {
        return "";
    }

    public String getControlValue(String userName, FormField ff) {
        return "";
    }

    public String getControlText(String userName, FormField ff) {
        return "";
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}
