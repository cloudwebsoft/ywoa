package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
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
public class WhitePadCtl extends AbstractMacroCtl {
    public static int INT_HELPER = 1;

    public WhitePadCtl() {
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     * @return String
     */
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        String s = super.getSetCtlValueScript(request, IFormDao, ff, formElementId);

        int width = 340;
        int height = 200;
        String wh = ff.getDefaultValue().trim();
        wh = wh.replaceFirst("，", ",");
        String[] ary = StrUtil.split(wh, ",");
        if (ary!=null && ary.length==2) {
            if (StrUtil.isNumeric(ary[0])) {
                width = StrUtil.toInt(ary[0]);
                height = StrUtil.toInt(ary[1]);
            }
        }

        // 手写板
        String patternStr = "\\[whitepad\\](.[^\\[]*)\\[\\/whitepad\\]";
        Pattern pattern = Pattern.compile(patternStr,
                                          Pattern.DOTALL |
                                          Pattern.CASE_INSENSITIVE);
        String content = StrUtil.getNullStr(ff.getValue());
        // 如果等于默认值，则说明尚未手写，默认值中存储的是宽度和高度
        if (content.length()>0 && content.equals(ff.getDefaultValue()))
            content = "";
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result = matcher.find();
        while (result) {
            INT_HELPER++;
            if (INT_HELPER >= 50000) {
                INT_HELPER = 0;
            }
            String str =
                    "<span id='span_pad_" + ff.getName() + "'><textarea style='display:none' id='value_spwhitepad_" +
                    INT_HELPER +
                    "'>$1</textarea><iframe src='" + Global.getRootPath() + "/spwhitepad/show.htm' name='spwhitepad_" +
                    INT_HELPER + "' frameborder='0' style='width:" + width + "px;height:" + height + "px;margin:5px;border:1px dashed #CCCCCC;' scrolling='no'></iframe></span>";
            matcher.appendReplacement(sb, str);
            result = matcher.find();
        }
        matcher.appendTail(sb);
        content = sb.toString();

        s += formElementId + "." + ff.getName() + ".insertAdjacentHTML(\"AfterEnd\", \"" + content + "\")\n";
        return s;
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        int width = 340;
        int height = 200;
        String wh = ff.getDefaultValue().trim();
        wh = wh.replaceFirst("，", ",");
        String[] ary = StrUtil.split(wh, ",");
        if (ary!=null && ary.length==2) {
            if (StrUtil.isNumeric(ary[0])) {
                width = StrUtil.toInt(ary[0]);
                height = StrUtil.toInt(ary[1]);
            }
        }
        String str = "<textarea style='display:none' name='" + ff.getName() + "'></textarea>";
        str += "<input title='打开手写板窗口' onClick=\"openWhitePadWin('" + ff.getName() +
                "'," + width + "," + height + ")\" name='" + ff.getName() +
                "_btn' type='button' class=btn value='手写'>";
        return str;
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 手写板
        /*
        String patternStr = "\\[whitepad\\](.[^\\[]*)\\[\\/whitepad\\]";
        Pattern pattern = Pattern.compile(patternStr,
                                          Pattern.DOTALL |
                                          Pattern.CASE_INSENSITIVE);
        String content = ff.getValue();
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result = matcher.find();
        while (result) {
            INT_HELPER++;
            if (INT_HELPER >= 50000) {
                INT_HELPER = 0;
            }
            String str =
                    "<textarea style='display:none' id='value_spwhitepad_" +
                    INT_HELPER +
                    "'>$1</textarea><iframe src='spwhitepad/show.htm' name='spwhitepad_" +
                    INT_HELPER + "' frameborder='0' style='width:400px;height:200px;margin:5px;border:1px dashed #CCCCCC;' scrolling='no'></iframe>";
            matcher.appendReplacement(sb, str);
            result = matcher.find();
        }
        matcher.appendTail(sb);
        content = sb.toString();
        */
        String content = "";
        // LogUtil.getLog(getClass()).info(" content=" + sb.toString());

        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                     "',\"" + content + "\", " +
                     formElementId + "o('cws_textarea_" + ff.getName() +
                     "').value);\n";
        // 把紧跟控件后的按钮也消除掉
        str += "DisableCtl('" + ff.getName() + "_btn', '" + ff.getType() +
                     "','', '');\n";
        return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        int width = 340;
        int height = 200;
        String wh = ff.getDefaultValue();
        String[] ary = StrUtil.split(wh, ",");
        if (ary!=null && ary.length==2) {
            if (StrUtil.isNumeric(ary[0])) {
                width = StrUtil.toInt(ary[0]);
                height = StrUtil.toInt(ary[1]);
            }
        }
        // 手写板
        String patternStr = "\\[whitepad\\](.[^\\[]*)\\[\\/whitepad\\]";
        Pattern pattern = Pattern.compile(patternStr,
                                          Pattern.DOTALL |
                                          Pattern.CASE_INSENSITIVE);
        String content = StrUtil.getNullStr(ff.getValue());

        // 如果等于默认值，则说明尚未手写，默认值中存储的是宽度和高度
        if (content.length()>0 && content.equals(StrUtil.getNullStr(ff.getDefaultValue())))
            content = "";

        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        boolean result = matcher.find();
        while (result) {
            INT_HELPER++;
            if (INT_HELPER >= 50000) {
                INT_HELPER = 0;
            }
            String str =
                    "<textarea style='display:none' id='value_spwhitepad_" +
                    INT_HELPER +
                    "'>$1</textarea><iframe src='" + Global.getRootPath() + "/spwhitepad/show.htm' name='spwhitepad_" +
                    INT_HELPER + "' frameborder='0' style='width:" + width + "px;height:" + height + "px;margin:5px;border:1px dashed #CCCCCC;' scrolling='no'></iframe>";
            matcher.appendReplacement(sb, str);
            result = matcher.find();
        }
        matcher.appendTail(sb);
        content = sb.toString();

        String str = "ReplaceCtlWithValue('" + ff.getName() + "', '" +
                     ff.getType() + "',\"" + content + "\");\n";
        return str;
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
