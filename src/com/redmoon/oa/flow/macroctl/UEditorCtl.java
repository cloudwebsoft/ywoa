package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormField;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by fgf on 2018/12/4.
 */
public class UEditorCtl extends AbstractMacroCtl {

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String serialNo = RandomSecquenceCreator.getId(20);

        String str = "";

        int flowId = StrUtil.toInt((String)request.getAttribute("cwsId"), -1);

        str += "<div style='clear:both;margin:0px;padding:0px'><textarea id=\"" + ff.getName() + "\" name=\"" + ff.getName() + "\">" + StrUtil.getNullString(ff.getValue()) + "</textarea></div>";

        String pageType = StrUtil.getNullStr((String)request.getAttribute("pageType"));
        if (!"show".equals(pageType) && ff.isEditable()) {
            if (request.getAttribute("isUEditorJS") == null) {
                str += "<script type=\"text/javascript\" charset=\"utf-8\" src=\"" + request.getContextPath() + "/ueditor/js/ueditor/ueditor.config.js?2023\"></script>";
                str += "<script type=\"text/javascript\" charset=\"utf-8\" src=\"" + request.getContextPath() + "/ueditor/js/ueditor/ueditor.all.js?2023\"> </script>";
                str += "<script type=\"text/javascript\" charset=\"utf-8\" src=\"" + request.getContextPath() + "/ueditor/js/ueditor/lang/zh-cn/zh-cn.js?2023\"></script>";
                request.setAttribute("isUEditorJS", "y");
            }
            str += "<script src='" + request.getContextPath() + "/flow/macro/macro_ueditor_ctl_js.jsp?flowId=" + flowId + "&fieldName=" + StrUtil.UrlEncode(ff.getName()) + "'></script>";
        }
        return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        // 用下行效果是一样的，但是因为考虑到原来为textarea，后来又在表单中直接改为ckeditor宏控件，为保证兼容性所以不采用下行的方式
        // return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "', o('" + ff.getName() + "').value);\n";

        // 如果value为空，则表示可能原来是textarea，后来改为ckeditor控件
        String str = "if (o('" + ff.getName() + "').value=='') ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "', o('cws_span_" + ff.getName() + "').innerHTML);\n";
        str += "else ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "', o('" + ff.getName() + "').value);\n";
        str += "try{o('ck_div_" + ff.getName() + "').style.display='none';}catch(e){}";
        return str;
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                "', o('" + ff.getName() + "').value, o('" + ff.getName() + "').value);\n";
        return str;
    }

    public String getControlType() {
        return "text";
    }

    public String getControlValue(String userName, FormField ff) {
        String str = StrUtil.getAbstract(null, ff.getValue(), 2000, "\r\n");
        return str;
    }

    public String getControlText(String userName, FormField ff) {
        String str = StrUtil.getAbstract(null, ff.getValue(), 2000, "\r\n");
        return str;
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}
