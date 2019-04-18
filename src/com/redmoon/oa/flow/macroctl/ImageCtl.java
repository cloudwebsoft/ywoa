package com.redmoon.oa.flow.macroctl;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.CheckErrException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.*;
import org.json.JSONException;
import org.json.JSONObject;

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
public class ImageCtl extends AbstractMacroCtl {

    public ImageCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        // 如果附件中已存在赋值，则显示图片
        if (ff.getValue() != null && !ff.getValue().equals("") && !ff.getValue().equals(ff.getDefaultValueRaw())) {
            String w = "", h = "";
            String desc = ff.getDescription();
            if (desc.startsWith("{")) {
                try {
                    JSONObject json = new JSONObject(desc);
                    w = json.getString("w");
                    h = json.getString("h");
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

            if (!"".equals(w) && !"".equals(h)) {
                str += "<img title='点击在新窗口中打开' onclick='window.open(\"" + request.getContextPath() + "/img_show.jsp?path=" + ff.getValue() + "\")' src='" + request.getContextPath() + "/img_show.jsp?path=" + ff.getValue() +
                        "' style='cursor:pointer; width:" + w + "px; height:" + h + "px'></a><BR>";
            } else if (!"".equals(w)) {
                str += "<a title='点击在新窗口中打开' href='" + request.getContextPath() + "/img_show.jsp?path=" + ff.getValue() + "' target='_blank'><img src='" + request.getContextPath() + "/img_show.jsp?path=" + ff.getValue() +
                        "' style=\"width:" + w + "px\"></a><BR>";

            } else {
                str += "<a title='点击在新窗口中打开' href='" + request.getContextPath() + "/img_show.jsp?path=" + ff.getValue() + "' target='_blank'><img src='" + request.getContextPath() + "/img_show.jsp?path=" + ff.getValue() + "'></a><BR>";
            }
        }
        str += "<input name='" + ff.getName() + "' type='file' accept='image/gif,image/jpeg,image/jpg,image/png,image/svg' size=15>";
        return str;
    }
    
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
    	String str = "";
        // 如果附件中已存在赋值，则显示图片
        if (!StrUtil.getNullStr(fieldValue).equals("")) {
        	if (fieldValue!=null && !fieldValue.equals("")) {
/*	        	String defaultStr = ff.getDefaultValue().replaceAll("，", ",");
	        	String[] ary = StrUtil.split(defaultStr, ",");
	        	if (ary!=null) {
	        		if (ary.length==2) {
	        			str += "<img src='" + request.getContextPath() +"/img_show.jsp?path="+ fieldValue +
	                		"' width='" + ary[0] + "' height='" + ary[1] + "'><BR>";
	        		}
	        		else if (ary.length==1) {
	        			str += "<img src='" + request.getContextPath() +"/img_show.jsp?path="+ fieldValue +
                		"' width='" + ary[0] + "'><BR>";	        			
	        		}
	        	}
	        	else {
		            str += "<img src='" + request.getContextPath() +"/img_show.jsp?path="+ fieldValue +
		                    "'><BR>";
	        	}*/
        		
        		str += "<a title='点击在新窗口中打开' href='" + request.getContextPath() +"/img_show.jsp?path="+ fieldValue + "' target='_blank'>图片</a>";
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
                     "', '', " + "o('cws_textarea_" + ff.getName() +
                     "').value);\n";
        return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() +
                "','');\n";
    }
    
    /**
     * 在验证前获取表单域的值，用于附件、图片宏控件不能为空的检查
     * @param request
     * @param fu
     * @param ff
     * @throws CheckErrException 
     */
    public void setValueForValidate(HttpServletRequest request, FileUpload fu, FormField ff) throws CheckErrException {
        Vector<FileInfo> v = fu.getFiles();
        Iterator<FileInfo> ir = v.iterator();
        while (ir.hasNext()) {
            FileInfo fi = ir.next();
            LogUtil.getLog(getClass()).info("setValueForValidate: ff.getName()=" +
                                            ff.getName() + " fi.fieldName=" +
                                            fi.getFieldName());
            if (fi.getFieldName().equals(ff.getName())) {
                fu.setFieldValue(ff.getName(), fi.getName());

                String defaultStr = ff.getDescription();
                defaultStr = defaultStr.replaceAll("；", ";");
	        	String[] strAry = StrUtil.split(defaultStr, ";");
	        	if (strAry!=null && strAry.length==2) {
	        		long maxSize = StrUtil.toLong(strAry[1], -1);
	        		if (maxSize!=-1) {
	        			File f = new File(fi.getTmpFilePath());
	        			if (f.length() > maxSize*1024) {
	        	    		String msg = "图片不能大于" + maxSize + "KB";
	        	    		Vector<String> vt = new Vector<String>();
	        	    		vt.addElement(msg);
	        	    		throw new CheckErrException(vt);     			
	        	    	}
	        		}
	        	}                
                break;
            }
        }    	
    }        

    public Object getValueForSave(FormField ff, int flowId, FormDb fd,
                                  FileUpload fu) {
        // ff参数来自于FormDAO中的doUpload，并不是来自于数据库，所以需从数据库中提取
        // System.out.println(getClass() + " flowId=" + flowId + " ff.getName()=" + ff.getName());
        FormDAO fdao = new FormDAO(flowId, fd);
        fdao.load();
        Vector vts = fdao.getFields();
        Iterator ir = vts.iterator();
        FormField dbff = null;
        while (ir.hasNext()) {
            dbff = (FormField) ir.next();
            LogUtil.getLog(getClass()).info("getValueForSave1:" + dbff.getName() +
                                            ":" + ff.getName());
            if (dbff.getName().equals(ff.getName()))
                break;
        }

        String re = StrUtil.getNullStr(ff.getValue());
        // 加getNullStr是为了防止修改表单新增图像控件字段，致历史记录中该字段值为null，如果不处理，保存后在数据库中就会变null字符串
        if (dbff != null)
            re = StrUtil.getNullStr(dbff.getValue());

        Vector v = fu.getFiles();
        ir = v.iterator();
        boolean isUploaded = false;
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            LogUtil.getLog(getClass()).info("getValueForSave: ff.getName()=" +
                                            ff.getName() + " fi.fieldName=" +
                                            fi.getFieldName());
            if (fi.getFieldName().equals(ff.getName())) {
                re = fdao.getVisualPath() + "/" + fi.getDiskName();
                isUploaded = true;
            }
        }

        LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + "=" +
                                        ff.getValue());
        if (isUploaded && dbff != null &&
            !StrUtil.getNullStr(dbff.getValue()).equals("")) {
            // 如果formfield原来的值不为空，且上传的文件中存在有对应fieldName的，则获取对应的图片附件，将其删除
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Document doc = new Document();
            doc = doc.getDocument(wf.getDocId());
            LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() +
                                            " docId=" + wf.getDocId());
            // LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + " doc.isLoaded=" + doc.isLoaded());

            int pageNum = 1;
            Vector vt = doc.getAttachments(pageNum);
            ir = vt.iterator();
            // 取得ID为最小的attach，并将其删除
            int count = 0;
            int minId = Integer.MAX_VALUE;
            Attachment lastAtt = null;
            while (ir.hasNext()) {
                Attachment att = (Attachment) ir.next();
                // LogUtil.getLog(getClass()).info("getValueForSave:" + att.getFieldName() + " ff.getName()=" + ff.getName());

                if (att.getFieldName().equals(ff.getName())) {
                    if (minId > att.getId()) {
                        minId = att.getId();
                        lastAtt = att;
                    }
                    count++;
                }
            }
            // LogUtil.getLog(getClass()).info("getValueForSave:" + lastAtt + " count=" + count);

            // 如果相同fieldName的存在两个附件，则删除较早的一个
            if (lastAtt != null && count > 1) {
                lastAtt.del();
                DocContentCacheMgr dcm = new DocContentCacheMgr();
                dcm.refreshUpdate(doc.getID(), pageNum);
            }
        }
        return re;
    }

    public Object getValueForCreate(FormField ff, FileUpload fu, FormDb fd) {
        // 当在流程中创建初始表单时，fu的值为null
        if (fu == null)
            return ff.getDefaultValue();
        Vector v = fu.getFiles();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            if (fi.getFieldName().equals(ff.getName())) {
                com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.
                        FormDAO(fd);
                return fdao.getVisualPath() + "/" + fi.getDiskName();
            }
        }
        return ff.getDefaultValue();
    }

    public Object getValueForSave(FormField ff, FormDb fd, long formDAOId,
                                  FileUpload fu) {
        // ff参数来自于FormDAO中的doUpload，并不是来自于数据库，所以需从数据库中提取
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(
                formDAOId, fd);
        Vector vts = fdao.getFields();
        Iterator ir = vts.iterator();
        FormField dbff = null;
        while (ir.hasNext()) {
            dbff = (FormField) ir.next();
            LogUtil.getLog(getClass()).info("getValueForSave1:" + dbff.getName() +
                                            ":" + ff.getName());
            if (dbff.getName().equals(ff.getName()))
                break;
        }

        String re = StrUtil.getNullStr(ff.getValue());
        // 加getNullStr是为了防止修改表单新增图像控件字段，致历史记录中该字段值为null，如果不处理，保存后在数据库中就会变null字符串        
        if (dbff != null)
            re = StrUtil.getNullStr(dbff.getValue());

        Vector v = fu.getFiles();
        ir = v.iterator();
        boolean isUploaded = false;
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            LogUtil.getLog(getClass()).info("getValueForSave: ff.getName()=" +
                                            ff.getName() + " fi.fieldName=" +
                                            fi.getFieldName());
            if (fi.getFieldName().equals(ff.getName())) {
                re = fdao.getVisualPath() + "/" + fi.getDiskName();
                isUploaded = true;
            }
        }

        LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + "=" +
                                        ff.getValue());
        if (isUploaded && dbff != null &&
            !StrUtil.getNullStr(dbff.getValue()).equals("")) {
            // 如果formfield原来的值不为空，且上传的文件中存在有对应fieldName的，则获取对应的图片附件，将其删除
            Vector vt = fdao.getAttachments();
            ir = vt.iterator();
            // 取得ID为最小的attach，并将其删除
            int count = 0;
            int minId = Integer.MAX_VALUE;
            com.redmoon.oa.visual.Attachment lastAtt = null;
            while (ir.hasNext()) {
                com.redmoon.oa.visual.Attachment att = (com.redmoon.oa.
                        visual.Attachment) ir.next();
                LogUtil.getLog(getClass()).info("getValueForSave:att.getFieldName()=" + att.getFieldName() + " ff.getName()=" + ff.getName());
                if (att.getFieldName().equals(ff.getName())) {
                    if (minId > att.getId()) {
                        minId = att.getId();
                        lastAtt = att;
                    }
                    count++;
                }
            }
            LogUtil.getLog(getClass()).info("getValueForSave:" + lastAtt + " count=" + count);
            // 如果相同fieldName的存在两个附件，则删除较早的一个
            if (lastAtt != null && count > 1) {
                lastAtt.del();
            }
        }
        return re;
    }

    public String getControlType() {
        return "img";
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

}
