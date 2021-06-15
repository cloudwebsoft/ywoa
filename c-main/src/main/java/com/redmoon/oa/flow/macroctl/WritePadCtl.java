package com.redmoon.oa.flow.macroctl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.CheckErrException;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.*;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONException;
import org.json.JSONObject;
import sun.misc.BASE64Decoder;

/**
 * <p>Title: </p>
 *
 * <p>Description: 
 * 如果默认值为w,h，则图片以宽w和高h自适应，如果仅有w，则固定宽度为w，如果无默认值，则为原始尺寸
 * 默认值也可能为w,h;300或w;300，300表示限制文件大小，单位为KB
 * </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WritePadCtl extends AbstractMacroCtl {

    public WritePadCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        String w = "200", h = "100";
        String val = StrUtil.getNullStr(ff.getValue());
        // 如果附件中已存在赋值，则显示图片
        if (!val.equals("")) {
            String desc = ff.getDescription();
            if (desc.startsWith("{")) {
                try {
                    JSONObject json = new JSONObject(desc);
                    w = json.getString("width");
                    h = json.getString("height");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                String defaultStr = ff.getDescription().replaceAll("，", ",");
                defaultStr = defaultStr.replaceAll("；", ";");

                String[] strAry = StrUtil.split(defaultStr, ";");
                String strStyle = "";
                if (strAry != null)
                    strStyle = strAry[0];
                String[] ary = StrUtil.split(strStyle, ",");
                if (ary!=null) {
                    if (ary.length==2) {
                        w = ary[0];
                        h = ary[1];
                    }
                    else {
                        w = ary[0];
                    }
                }
            }

            str += "<div id='pad_" + ff.getName() + "' img='" + Global.getFullRootPath(request) + "/" + ff.getValue() + "'>";
            // 只根据宽度，以使得成比例缩放
            if (!"".equals(w)) {
                str += "<a title='点击在新窗口中打开' href='" + request.getContextPath() + "/public/img_show.jsp?path=" + ff.getValue() + "' target='_blank'><img src='" + request.getContextPath() + "/public/img_show.jsp?path=" + ff.getValue() +
                        "' style=\"width:" + w + "px\"></a><BR>";
            } else {
                str += "<a title='点击在新窗口中打开' href='" + request.getContextPath() + "/public/img_show.jsp?path=" + ff.getValue() + "' target='_blank'><img src='" + request.getContextPath() + "/public/img_show.jsp?path=" + ff.getValue() + "'></a><BR>";
            }
            str += "</div>";
        }
        if (ff.isEditable()) {
            if (val.equals("")) {
                str += "<div id='pad_" + ff.getName() + "' style='width:" + w + "px;height:" + h + "px;'></div>";
            }
            str += "<input id='btn_" + ff.getName() + "' type='button' value='手写' onclick=\"openWin('" + request.getContextPath() + "/flow/writepad.jsp?fieldName=" + ff.getName() + "&w=" + w + "&h=" + h + "', 640, 260)\"/>";
        }
        str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' value='" + val + "'/>";
        return str;
    }
    
    @Override
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
    	String str = "";
        // 如果附件中已存在赋值，则显示图片
        if (!StrUtil.getNullStr(fieldValue).equals("")) {
        	if (fieldValue!=null && !fieldValue.equals("")) {
        	    String rootPath;
        	    if (request==null) {
        	        rootPath = Global.getRootPath();
                }
        	    else {
        	        rootPath = request.getContextPath();
                }
        		str += "<a title='点击在新窗口中打开' href='" + rootPath +"/public/img_show.jsp?path="+ fieldValue + "' target='_blank'>图片</a>";
        	}
        }
        return str;
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                     "', '', " + "o('cws_textarea_" + ff.getName() + "').value);\n";
        return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() + "','');\n";
    }

    public String getControlType() {
        return "writePad";
    }

    public String getControlValue(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }

    public String getControlText(String userName, FormField ff) {
        return StrUtil.getNullStr(ff.getValue());
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

    public boolean convertBase64ToImgFile(String imgUrl, IFormDAO ifdao, FormField field) {
        int p = imgUrl.indexOf(",");
        if (p != -1) {
            String base64img = imgUrl.substring(p + 1);
            int a = imgUrl.indexOf("/");
            int b = imgUrl.indexOf(";");
            String ext = imgUrl.substring(a + 1, b);

            String diskName = FileUpload.getRandName() + "." + ext;
            String filePath = ifdao.getVisualPath();
            File file = new File(Global.getRealPath() + filePath);
            if (!file.isDirectory()) {
                file.mkdirs();
            }

            boolean re = com.redmoon.oa.fileark.DocContent.generateImage(base64img, Global.getRealPath() + filePath + "/" + diskName);
            if (re) {
                File f = new File(Global.getRealPath() + filePath + "/" + diskName);
                long size = f.length();

                if (ifdao instanceof com.redmoon.oa.flow.FormDAO) {
                    // 有则替换，无则创建
                    com.redmoon.oa.flow.Attachment att = new com.redmoon.oa.flow.Attachment();
                    WorkflowDb wf = new WorkflowDb();
                    wf = wf.getWorkflowDb(ifdao.getFlowId());

                    att = att.getAttachment(wf.getDocId(), field.getName());
                    if (att==null) {
                        att = new com.redmoon.oa.flow.Attachment();
                        att.setDocId(wf.getDocId());
                        att.setName(diskName);
                        att.setDiskName(diskName);
                        att.setVisualPath(filePath);
                        att.setSize(size);
                        att.setOrders(0);
                        att.setCreator(((FormDAO) ifdao).getCreator());
                        att.setFieldName(field.getName());
                        att.create();
                    }
                    else {
                        att.setName(diskName);
                        att.setDiskName(diskName);
                        att.setVisualPath(filePath);
                        att.setSize(size);
                        att.save();
                    }
                }/* else {
                    com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment();
                    att.setVisualId(ifdao.getId());
                    att.setName(diskName);
                    att.setDiskName(diskName);
                    att.setVisualPath(filePath);
                    att.setFileSize(size);
                    att.setOrders(0);
                    att.setFieldName(field.getName());
                    att.setCreator(((com.redmoon.oa.visual.FormDAO)ifdao).getCreator());
                    att.create();
                }*/
                ifdao.setFieldValue(field.getName(), filePath + "/" + diskName);
                try {
                    ifdao.save();
                } catch (ErrMsgException e) {
                    e.printStackTrace();
                }
            }
            return re;
        }
        return false;
    }

    @Override
    public void onFormDAOSave(HttpServletRequest request, IFormDAO ifdao, FormField field,
                              FileUpload fu) throws ErrMsgException {
        // String content = ifdao.getFieldValue(field.getName());
        // String content = fu.getFieldValue(field.getName());
        String content = field.getValue();
        convertBase64ToImgFile(content, ifdao, field);
    }
}
