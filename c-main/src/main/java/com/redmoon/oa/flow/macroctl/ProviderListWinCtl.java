package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.base.IFormDAO;

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
public class ProviderListWinCtl extends AbstractMacroCtl {
    public ProviderListWinCtl() {
    }
    
    public FormDAO getFormDAOOfProvider(int id) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb("sales_provider_info");
        FormDAO fdao = new FormDAO(id, fd);
        return fdao;
    }    

    /**
     * convertToHTMLCtl
     *
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     * @todo Implement this com.redmoon.oa.base.IFormMacroCtl method
     */
     public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		String v = "";
		if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
			// LogUtil.getLog(getClass()).info("StrUtil.toInt(ff.getValue())=" +
			// StrUtil.toInt(ff.getValue()));
			FormDAO fdao = getFormDAOOfProvider(StrUtil.toInt(ff.getValue()));
			// LogUtil.getLog(getClass()).info("mobile=" +
			// fdao.getFieldValue("mobile"));
			v = fdao.getFieldValue("provide_name");
		}

		str += "<input id='" + ff.getName() + "_realshow' name='"
				+ ff.getName() + "_realshow' value='" + v
				+ "' size=15 readonly>";
		str += "<input id='" + ff.getName() + "' name='" + ff.getName()
				+ "' value='' type='hidden'>";

         str +=
                 "&nbsp;<input id=\"" + ff.getName() + "_btn\" type=button class=btn value=\"选择\" onClick=\"openWinProviderList(document.getElementById('" +
                 ff.getName() + "'))\">";
         return str;
    }
     
     public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
         String v = StrUtil.getNullStr(fieldValue);
         if (!v.equals("")) {
             // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
             FormDAO fdao = getFormDAOOfProvider(StrUtil.toInt(v));
             String str = fdao.getFieldValue("provide_name");
             return str;
         }
         else
             return "";
     }

     public String getDisableCtlScript(FormField ff, String formElementId) {
         String str = super.getDisableCtlScript(ff, formElementId) + "\n";
         str += "o('" + ff.getName() + "_btn').style.display='none';\n";
         // str += "o('" + ff.getName() + "_realshow').readonly='true';\n";
         return str;
     }    
    
    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
            FormDAO fdao = getFormDAOOfProvider(StrUtil.toInt(ff.getValue()));
            v = fdao.getFieldValue("provide_name");
        }
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
    }


    public String getControlType() {
        return "buttonSelect";
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

    public String getControlValue(String userName, FormField ff) {
         if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
             return ff.getValue();
         }
         return "";
    }

    public String getControlText(String userName, FormField ff) {
        String v = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            FormDAO fdao = getFormDAOOfProvider(StrUtil.toInt(ff.getValue()));
            v = fdao.getFieldValue("provide_name");
        }
        return v;
    }
}
