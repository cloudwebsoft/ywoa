package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.ui.menu.DirectoryView;
import com.redmoon.oa.ui.menu.Leaf;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: 系统菜单选择</p>
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
public class MenuSelectCtl extends AbstractMacroCtl {
    public MenuSelectCtl() {
    }

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String rCode = StrUtil.getNullStr(ff.getDefaultValueRaw());
		if ("".equals(rCode)) {
            rCode = ConstUtil.MENU_ROOT;
        }

		StringBuffer sb = new StringBuffer();
        Leaf lf = new Leaf();
        lf = lf.getLeaf(rCode);
        if (lf==null) {
            sb.append("节点" + rCode + "不存在！");
        }
        sb.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "'>");
        com.redmoon.oa.ui.menu.DirectoryView directoryView = new com.redmoon.oa.ui.menu.DirectoryView(request, lf);
		directoryView.ShowDirectoryAsOptionsToString(sb, lf, lf.getLayer());
		sb.append("</select>");
        return sb.toString();
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request,
                                           FormField ff) {
		String rCode = StrUtil.getNullStr(ff.getDefaultValueRaw());
		if ("".equals(rCode)) {
            rCode = ConstUtil.MENU_ROOT;
        }
		Leaf lf = new Leaf();
		lf = lf.getLeaf(rCode);
		
        StringBuffer sb = new StringBuffer();
        if (lf==null) {
        	sb.append("节点" + rCode + "不存在！");
        }
        else {
	        sb.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "'>");
	        sb.append("<option value=''>无</option>");

	        StringBuffer opts = new StringBuffer();
			DirectoryView dv = new DirectoryView(request, lf);
            dv.ShowDirectoryAsOptionsToString(opts, lf, lf.getLayer());
            sb.append(opts.toString());

	        sb.append("</select>");
        }
        return sb.toString();
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素，通常为textarea
     * @return String
     */
    @Override
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request,
            FormField ff) {
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String desc = "";
        if (ff.getValue() != null) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(ff.getValue());
            if (lf!=null) {
                desc = lf.getName();
            }
        }

        return "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                     "','" + desc + "','" + ff.getValue() + "');\n";
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        String desc = "";
        if (ff.getValue() != null) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(ff.getValue());
            if (lf!=null) {
                desc = lf.getName();
            }
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
    @Override
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String desc = "";
        if (fieldValue!=null && !"".equals(fieldValue)) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(ff.getValue());
            if (lf!=null) {
                desc = lf.getName();
            }
        }

        return desc;
    }

    @Override
    public String getControlType() {
        return "select";
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
        if (!"".equals(StrUtil.getNullStr(ff.getValue()))) {
            return ff.getValue();
        }else{
            return StrUtil.getNullStr(ff.getDefaultValueRaw());
        }
    }

    @Override
    public String getControlText(String userName, FormField ff) {
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
        	String desc = "";
            Leaf lf = new Leaf();
            lf = lf.getLeaf(ff.getValue());
            if (lf!=null) {
                desc = lf.getName();
            }

            return desc;
        }
        else {
            return "";
        }
    }

    @Override
    public String getControlOptions(String userName, FormField ff) {
       return "";
    }

}
