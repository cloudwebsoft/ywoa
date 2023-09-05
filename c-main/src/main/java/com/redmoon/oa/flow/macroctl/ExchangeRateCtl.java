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
public class ExchangeRateCtl extends AbstractMacroCtl {
    public ExchangeRateCtl() {
           super();
    }

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
       String defaultValue = ff.getDefaultValue();
       String[] ary = StrUtil.split(defaultValue, ",");
       String str = "";
       String moneyTypeField = "",  moneyValueField= "";
       if (ary.length!=2) {
           str += "默认值必须为逗号分隔";
       }
       else {
           moneyTypeField = ary[0];
           moneyValueField = ary[1];
       }
       str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' moneyValueField ='"+moneyValueField+"' moneyTypeField='" +
                    moneyTypeField + "' value='' title='汇率转换" + moneyValueField +
                    "' readonly>";

       // 如果有任意的动作，都重新从小写框获取数据，并转换汇率
        String isRateToCtlJSWrited = (String) request.getAttribute(
                "isRateToCtlJSWrited");
        if (isRateToCtlJSWrited == null) {
            str += "<script src='" + request.getContextPath() + "/flow/macro/macro_exchange_rate_ctl.jsp?fieldName=" + ff.getName() + "'></script>";
            request.setAttribute("isRateToCtlJSWrited", "y");
        }

       return str;
   }

   @Override
   public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request, FormField ff) {
       if (ff.getValue().equals(ff.getDefaultValue())) {
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
        return "0.0";
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
