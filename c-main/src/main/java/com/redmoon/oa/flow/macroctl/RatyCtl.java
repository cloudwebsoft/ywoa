package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import com.github.pagehelper.util.StringUtil;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.base.IFormDAO;
import cn.js.fan.util.RandomSecquenceCreator;
import com.redmoon.kit.util.FileUpload;
import cn.js.fan.web.Global;

/**
 * <p>Title: 利用JQuery raty实现的标值控件</p>
 *
 * <p>Description:flexigrid如果后执行，则raty控件会在customer_list.jsp页面中显示为两个
 * 因此，需先通过cwAddLoadEvent，在onload事件中初始化flexigrid，而RactCtl则需改造为也在cwAddLoadEvent中去显示。
 * 经测试，将flexigrid放在jquery的documenty.ready事件中无效。
 * 20180714 fgf 已改为通过is_" + rand + "_launched判断，避免二次执行，出现两个星标控件了
 * </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RatyCtl extends AbstractMacroCtl {
    public RatyCtl() {
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
        if (StringUtil.isNotEmpty(fieldValue)) {
            return "<img src='" + request.getContextPath() + "/images/rate/star" + fieldValue + ".png'/>";
        }
        // 下面这段在flexigrid中会出现两组重复的星标
        // 控件默认值：0,5（0表示默认不选，5表示5级）
        /*String props = ff.getDefaultValueRaw();
        String[] ary = StrUtil.split(props, ",");
        int number = 5;
        if (ary!=null) {
            if (ary.length>1) {
                number = StrUtil.toInt(ary[1], 5);
            }
        }

        String rand = RandomSecquenceCreator.getId(5);
        String str = "<span id='" + ff.getName() + "_raty" + rand + "'></span>";

        str += "<script>\n";
        str += "var is_" + rand + "_launched = false;\n"; // 防止当flexigrid时被多次运行
        str += "function raty_" + rand + "_func() {\n";
        str += "if (is_" + rand + "_launched) return;\n";
        str += "$('#" + ff.getName() + "_raty" + rand + "').raty({\n";
        str += "readOnly:'true',\n";
        // str += "scoreName:'" + ff.getName() + "',";
        str += "number:'" + number + "',\n";
        str += "path: '" + request.getContextPath() + "/images/rate'";

        UserSetupDb usd = new UserSetupDb();
        usd = usd.getUserSetupDb(new Privilege().getUser(request));
		if (usd != null && usd.isLoaded()) {
			if ("zh-CN".equals(usd.getLocal())) {
				str += ",cancelHint: '取消'";
				str += ",hintList:['一星级','二星级','三星级','四星级','五星级']";
			} else {
				str += ",cancelHint: 'cancel'";
				str += ",hintList:['one-star','two-star','three-star','fore-star','five-star']";
			}
		}
        
        if (!"".equals(v)) {
            str += ",start:'" + v + "'\n";
        }

        str += "});\n";
        
        str += "is_" + rand + "_launched = true;\n";
        str += "}\n";
        // str += "cwAddLoadEvent(raty_" + rand + "_func);";
        str += "$(function() {raty_" + rand + "_func();});\n";
        str += "</script>";*/
        return "";
    }

    public static String render(HttpServletRequest request, FormField ff) {
        return render(request, ff, false);
    }

    public static String render(HttpServletRequest request, FormField ff, boolean readonly) {
        String v = StrUtil.getNullStr(ff.getValue());
        // 控件默认值：0,5（0表示默认不选，5表示5级）
        String props = ff.getDefaultValueRaw();
        String[] ary = StrUtil.split(props, ",");
        int number = 5;
        int start = 0;
        if (ary!=null) {
            start = StrUtil.toInt(ary[0], 0);
            if (ary.length>1) {
                number = StrUtil.toInt(ary[1], 5);
            }
        }

        String rand = RandomSecquenceCreator.getId(5);

        String str = "<span id='" + ff.getName() + "_raty" + rand + "'></span>";

        str += "<script>";
        str += "$('#" + ff.getName() + "_raty" + rand + "').raty({";
        if (readonly) {
            str += "readOnly:'true',";
        }
        str += "number:'" + number + "',";
        str += "path: '" + Global.getRootPath() + "/images/rate'";
        
        UserSetupDb usd = new UserSetupDb();
        usd = usd.getUserSetupDb(new Privilege().getUser(request));
		if (usd != null && usd.isLoaded()) {
			if ("zh-CN".equals(usd.getLocal())) {
				str += ",cancelHint: '取消'";
				str += ",hintList:['一星级','二星级','三星级','四星级','五星级']";
			} else {
				str += ",cancelHint: 'cancel'";
				str += ",hintList:['one-star','two-star','three-star','fore-star','five-star']";
			}
		}
		
        if (!"".equals(v)) {
            str += ",start:'" + v + "'";
        }
        /*
        , onClick:function(score) {
            alert('score : ' +score);
        }
        */
        
        str += "});";
        str += "</script>";
        return str;
    }

    /**
     *
     *
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     */
    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String v = StrUtil.getNullStr(ff.getValue());
        // 控件默认值：0,5（0表示默认不选，5表示5级）
        String props = ff.getDefaultValueRaw();
        String[] ary = StrUtil.split(props, ",");
        int number = 5;
        int start = 0;
        if (ary!=null) {
            start = StrUtil.toInt(ary[0], -1);
            if (ary.length>1)
                number = StrUtil.toInt(ary[1], 5);
        }

        String str = "<span id='" + ff.getName() + "_raty'></span>";

        str += "<script>";
        str += "$('#" + ff.getName() + "_raty').raty({";
        str += "cancel     : true,";
        str += "scoreName:'" + ff.getName() + "',";
        str += "number:'" + number + "',";
        str += "path:'" + request.getContextPath() + "/images/rate'";

        UserSetupDb usd = new UserSetupDb(new Privilege().getUser(request));
		if (usd != null && usd.isLoaded()) {
			if (usd.getLocal().equals("zh-CN")) {
				str += ",cancelHint: '取消'";
				str += ",hintList:['一星级','二星级','三星级','四星级','五星级']";
			} else {
				str += ",cancelHint: 'cancel'";
				str += ",hintList:['one-star','two-star','three-star','fore-star','five-star']";
			}
		}
		
        if (!v.equals("")) {
            str += ",start:'" + v + "'";
        }
        else {
            str += ",start:'" + start + "'";
        }
        str += "});";

        str += "</script>";
        return str;
    }

    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        if (ff.getValue()==null) {
            return "$.fn.raty.start(0, '#" + ff.getName() + "_raty');$.fn.raty.readOnly(true, '#" + ff.getName() + "_raty');";
        }
        return "$.fn.raty.readOnly(true, '#" + ff.getName() + "_raty');";
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     * @return String
     */
    @Override
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        String pageType = StrUtil.getNullStr((String)request.getAttribute("pageType"));
        if (pageType.equals("edit")) {
            if (ff.getValue() == null || ff.getValue().equals("")) {
                return "$.fn.raty.start(0, '#" + ff.getName() + "_raty');";
            }
            else {
                return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
            }
        }
        else {
            return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
        }
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
        FormField ff = new FormField();
        ff.setName(objId);
        ff.setValue(oldValue);
        String str = render(request, ff);
        return str;
    }

    /**
     * 用于visual可视化模块处理，当用于智能模块设计时，需继承此方法
     * @param ff FormField
     * @param fu FileUpload
     * @param fd FormDb
     * @return Object
     */
    @Override
    public Object getValueForCreate(FormField ff, FileUpload fu, FormDb fd) {
		String v = StrUtil.getNullStr(ff.getValue());
		// 控件默认值：0,5（0表示默认不选，5表示5级）
		String props = ff.getDefaultValueRaw();
		String[] ary = StrUtil.split(props, ",");
		String start = "0";
		if (ary != null) {
			start = ary[0];
		}
		if (v == null || v.equals("")) {
			v = start;
    	}
		return v;
    }
    
    public Object getValueForSave(FormField ff, FileUpload fu, FormDb fd) {
    	String v = StrUtil.getNullStr(ff.getValue());
		// 控件默认值：0,5（0表示默认不选，5表示5级）
		String props = ff.getDefaultValueRaw();
		String[] ary = StrUtil.split(props, ",");
		String start = "0";
		if (ary != null) {
			start = ary[0];
		}
		if (v == null || v.equals("")) {
			v = start;
    	}
		return v;
    }

    @Override
    public String getControlType() {
        return "img";
    }

    @Override
    public String getControlValue(String userName, FormField ff) {
        return ff.getValue();
    }

    @Override
    public String getControlText(String userName, FormField ff) {
        if (StringUtil.isNotEmpty(ff.getValue())) {
            return "images/rate/star" + ff.getValue() + ".png";
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
