package com.redmoon.oa.flow.macroctl;

import com.redmoon.oa.pvg.Privilege;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.fileark.*;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * <p>Title: 角色选择</p>
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
public class FilearkDirSelectCtl extends AbstractMacroCtl {
    public FilearkDirSelectCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String rCode = StrUtil.getNullStr(ff.getDefaultValueRaw());
		if (rCode.equals(""))
			rCode = Leaf.ROOTCODE;
		Leaf lf = new Leaf();
		lf = lf.getLeaf(rCode);
		
        StringBuffer sb = new StringBuffer();
        if (lf==null) {
        	sb.append("节点" + rCode + "不存在！");
        }
		sb.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "' onChange=\"if(this.options[this.selectedIndex].value==''){alert(this.options[this.selectedIndex].text+' 不能被选择！'); return false;}\">");
		sb.append("<option value=''>请选择</option>");
		DirectoryView dv = new DirectoryView(lf);
		try {
			dv.getDirAsOptions(request, sb, lf, lf.getLayer());
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		sb.append("</select>");
        
        return sb.toString();
    }

    public String convertToHTMLCtlForQuery(HttpServletRequest request,
                                           FormField ff) {
		String rCode = StrUtil.getNullStr(ff.getDefaultValueRaw());
		if (rCode.equals(""))
			rCode = Leaf.ROOTCODE;
		Leaf lf = new Leaf();
		lf = lf.getLeaf(rCode);
		
        StringBuffer sb = new StringBuffer();
        if (lf==null) {
        	sb.append("节点" + rCode + "不存在！");
        }
        else {
	        sb.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "'>");
	        sb.append("<option value=''>无</option>");

			DirectoryView dv = new DirectoryView(lf);
			sb.append(dv.getDirAsOption(request, lf, lf.getLayer()));

	        sb.append("</select>");
        }
        return sb.toString();
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     * @return String
     */
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request,
            FormField ff) {
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String desc = "";
        if (ff.getValue() != null) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(ff.getValue());
            if (lf!=null)
            	desc = lf.getName();
        }
        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                     "','" + desc + "','" + ff.getValue() + "');\n";

        return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String desc = "";
        if (ff.getValue() != null) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(ff.getValue());
            if (lf!=null)
            	desc = lf.getName();
        }
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() +
                "','" + desc + "');\n";
    }

    public Object getValueForCreate(FormField ff) {
        return ff.getValue();
    }

    /**
     * 用于模块列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField 表单域的描述，其中的value值为空
     * @param fieldValue String 表单域的值
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String desc = "";
        if (fieldValue!=null && !fieldValue.equals("")) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(ff.getValue());
            if (lf!=null)
            	desc = lf.getName();
        }

        return desc;
    }

    public String getControlType() {
        return "select";
    }

    public String getControlValue(String userName, FormField ff) {
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            return ff.getValue();
        }else{
            String defaultCode = StrUtil.getNullStr(ff.getDefaultValueRaw());
            return defaultCode;
        }
    }

    public String getControlText(String userName, FormField ff) {
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
        	String desc = "";
            Leaf lf = new Leaf();
            lf = lf.getLeaf(ff.getValue());
            if (lf!=null)
            	desc = lf.getName();

            return desc;
        }
        else {
            return "";
        }
    }

    public String getControlOptions(String userName, FormField ff) {
       return "";
    }

}
