package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.RandomSecquenceCreator;

/**
 * <p>Title: CKEditor编辑器控件 2010-12-19</p>
 *
 * <p>Description: 目前暂时只支持用于流程表单，不支持智能模块设计</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class CKEditorCtl extends AbstractMacroCtl {
    public CKEditorCtl() {
    }

    @Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String serialNo = RandomSecquenceCreator.getId(20);

        // String str = FormField.toHtml(StrUtil.getNullString(ff.getValue())) + "<BR><textarea name='" + ff.getName() + "' style='cursor:hand;width:95%' readonly onClick='openWinIdea(this)' rows=8 cols=100 title='意见框'></textarea>";
        String str = "";
        
        int flowId = StrUtil.toInt((String)request.getAttribute("cwsId"), -1);

        if (ff.isEditable()) {
            if (request.getAttribute("isCkeditorJS1") == null) {
	            str += "<script src='" + request.getContextPath() + "/inc/upload_ajax.js'></script>";
	            str += "<script>urlUploadProgress='" + request.getContextPath() + "/ajax_upload_progress.jsp';</script>";
	            request.setAttribute("isCkeditorJS1", "y");
            }
            str += "<div id='ck_div_" + ff.getName() + "' style='margin:0px;padding:0px;'><iframe id=\"uploadFrm\" src=\"" + request.getContextPath() + "/flow/upload_media.jsp?flowId=" + flowId + "&fieldCode=" + ff.getName() + "&uploadSerialNo=" + serialNo + "\" width=\"380px\" height=\"28\" frameborder=\"0\" scrolling=\"no\" style=\"float:left\"></iframe>";
            str += "<table width=\"180px\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"float:left; height:5px; margin-left:5px;background-color:#eeeeee;height:10px;margin-top:10px\">";
            str += "  <tr>";
            str += "    <td style='height:5px'><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"uploadStatusProgress_" + serialNo + "\" style=\"height:5px; background-color:#00CC66;width:0px\">";
            str += "        <tr>";
            str += "          <td style='height:5px'></td>";
            str += "        </tr>";
            str += "      </table></td>";
            str += "  </tr>";
            str += "</table><span id='uploadStatus_" + serialNo + "' style='float:left;margin-top:6px'></span></div>";
        }

        str += "<div style='clear:both;margin:0px;padding:0px'><textarea id=\"" + ff.getName() + "\" name=\"" + ff.getName() + "\">" + StrUtil.getNullString(ff.getValue()) + "</textarea></div>";

        if (ff.isEditable()) {
            if (request.getAttribute("isCkeditorJS2") == null) {
                str += "<script type=\"text/javascript\" src=\"" + request.getContextPath() +
                "/ckeditor/ckeditor.js\"></script>";

	            str += "<script src='" + request.getContextPath() + "/flow/macro/macro_ckeditor_ctl_js.jsp?flowId=" + flowId + "&fieldName=" + StrUtil.UrlEncode(ff.getName()) + "'></script>";
                
                request.setAttribute("isCkeditorJS2", "y");
            }
            
            str += "<script>";
            str += "CKEDITOR.replace('" + ff.getName() + "',";
            str += "        {";
            // str += "         skin : 'office2003',";
            str += "         toolbar : 'Flow',";
            str += "        filebrowserImageBrowseUrl:'',";
            str += "        filebrowserFlashBrowseUrl:''";
            str += "        });";
            str += "</script>";

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
