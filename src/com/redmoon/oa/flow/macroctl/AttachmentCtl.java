package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.kit.util.FileUpload;

import java.util.Vector;
import java.util.Iterator;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.oa.flow.FormDAO;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.flow.Document;
import com.redmoon.oa.flow.Attachment;
import com.redmoon.oa.flow.DocContentCacheMgr;
import com.cloudwebsoft.framework.util.LogUtil;
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
public class AttachmentCtl extends AbstractMacroCtl {

    public AttachmentCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        // 如果附件中已存在赋值，则显示图片
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            // str += "<img src='" + Global.getRootPath() + "/" + ff.getValue() +
            //        "'><BR>";
            String[] ary = ff.getValue().split(",");
            // 如果有两个元素，则说明是流程中所存
            if (ary.length==2) {
                Attachment att = new Attachment(Integer.valueOf(ary[1]).intValue());
                if (att != null && att.isLoaded()) {
	                str += "<a href='" + request.getContextPath() + "/flow_getfile.jsp?attachId=" + ary[1] +
	                        "&flowId=" + ary[0] + "' target='_blank'>" + att.getName() + "</a><br />";
                }
            }
            // 如果只有一个元素，则说明是visual模块所存，字段中存的是diskName
            else if (ary.length==1) {
            	com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment();
            	att = att.getAttachment(ary[0]);
            	if (att != null && att.isLoaded()) {
            		str += "<a href='" + request.getContextPath() + "/visual_getfile.jsp?attachId=" + att.getId() + "' target='_blank'>" + att.getName() + "</a><br />";
            	}
            	/*
                Conn conn = new Conn(Global.getDefaultDB());
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                try {
                    String sql = "SELECT id FROM visual_attach WHERE diskname=?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, ary[0]);
                    rs = conn.executePreQuery();
                    if (rs != null && rs.next()) {
                        int attId = rs.getInt(1);
                    	com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(attId);
                    	str += "<a href=\"" + request.getContextPath() + "/visual_getfile.jsp?attachId=" + att.getId() + "\" target=\"_blank\">" + att.getName() + "</a><br />";
                    }
                } catch (SQLException e) {
                    // logger.error("loadFromDbByOrders:" + e.getMessage());
                	e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.close();
                        conn = null;
                    }
                }
                */
            }
        }
        if (ff.isEditable()) {
        	str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='file' size=15>";
        } else {
        	str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' size=15>";
        }
        return str;
    }
    
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
    	String str = "";
        //System.out.println(AttachmentsCtl.class+":::convertToHTMLCtl::"+StrUtil.getNullStr(ff.getValue())+","+StrUtil.getNullStr(ff.getValue()).equals(""));
        // 如果附件中已存在赋值，则显示图片
        if (!StrUtil.getNullStr(fieldValue).equals("")) {
        	String[] ary = fieldValue.split(",");
            // 如果有两个元素，则说明是流程中所存
            if (ary.length==2) {
                Attachment att = new Attachment(Integer.valueOf(ary[1]).intValue());
                if (att != null && att.isLoaded()) {
	                str += "<a href=\"" + Global.getRootPath() + "/flow_getfile.jsp?attachId=" + ary[1] +
	                        "&flowId=" + ary[0] + "\" target=\"_blank\">" + att.getName() + "</a><br />";
                }
            }
            // 如果只有一个元素，则说明是visual模块所存，字段中存的是visual + diskName
            else if (ary.length==1) {
            	com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment();
            	att = att.getAttachment(ary[0]);
            	if (att != null && att.isLoaded()) {
            		str += "<a href=\"" + Global.getRootPath() + "/visual_getfile.jsp?attachId=" + att.getId() + "\" target=\"_blank\">" + att.getName() + "</a><br />";
            	}
            }
        }
        return str;
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     * @return String
     */
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
    	/*
    	String str = "";
    	String value = StrUtil.getNullStr(ff.getValue());
        if(!value.equals("")){
        	String[] ary = value.split(",");
            // 如果有两个元素，则说明是流程中所存
            if (ary.length==2) {
                Attachment att = new Attachment(Integer.valueOf(ary[1]).intValue());
                str += "var " + ff.getName() + "temp = \"<a href='" + "flow_getfile.jsp?attachId=" + ary[1] +
                        "&flowId=" + ary[0] + "' target='_blank'>" + att.getName() + "</a>\";\n";
                str += "document.getElementById('"+ff.getName() + "File" + "').innerHTML = " + ff.getName() + "temp;\n";
            }
            // 如果只有一个元素，则说明是visual模块所存，字段中存的是diskName
            else if (ary.length==1) {
            	com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment();
            	att = att.getAttachment(ary[0]);
            	str += "var "+ff.getName() + "temp = \"<a href='" + Global.getFullRootPath() + "/visual_getfile.jsp?attachId=" + att.getId() + "' target='_blank'>" + att.getName() + "</a>\";\n";
                str += "document.getElementById('"+ff.getName() + "File" + "').innerHTML = " + ff.getName() + "temp;\n";
            }
        }
        return str;
        */
    	// 有可能在老版本的表单中，没有这个字段
    	return "if (o('" + ff.getName() + "')) {o('" + ff.getName() + "').style.display = 'none';}\n";
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
    	/*
    	String str = "";
    	String value = StrUtil.getNullStr(ff.getValue());
        if(!value.equals("")){
        	//String strTemp = "";
        	String[] items = value.split(";");
        	//System.out.println(AttachmentsCtl.class+":::"+items);
        	for(int i = 0; i < items.length; i ++){
        		//System.out.println(AttachmentsCtl.class+":::"+items[i]);
	            String[] ary = items[i].split(",");
	            if (ary.length==2) {
	                Attachment att = new Attachment(Integer.valueOf(ary[1]).intValue());
	                str += "var "+ff.getName()+i+"temp = \"<a href='" + "flow_getfile.jsp?attachId=" + ary[1] +"&flowId=" + ary[0] + "' target='_blank'>" + att.getName() + "</a>\";\n";
	                str += "document.getElementById(\'"+ff.getName()+"File"+i+"\').innerHTML = "+ff.getName()+i+"temp;\n";
	            }
	        }
        }
        return str;
        */
    	return "o('" + ff.getName() + "').style.display = 'none';";
    }

    /**
     * 在验证前获取表单域的值，用于附件、图片宏控件不能为空的检查
     * @param request
     * @param fu
     * @param ff
     */
    public void setValueForValidate(HttpServletRequest request, FileUpload fu, FormField ff) {
        Vector v = fu.getFiles();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            LogUtil.getLog(getClass()).info("setValueForValidate: ff.getName()=" +
                                            ff.getName() + " fi.fieldName=" +
                                            fi.getFieldName());
            if (fi.getFieldName().equals(ff.getName())) {
                fu.setFieldValue(ff.getName(), fi.getName());
                break;
            }
        }    	
    }    

    public Object getValueForSave(FormField ff, int flowId, FormDb fd,
                                  FileUpload fu) {
        // ff参数来自于FormDAO中的doUpload，并不是来自于数据库，所以需从数据库中提取(已更改为取自数据库)
        // 在数据库中存储其附件ID|文件名
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

        String re = ff.getValue();
        if (dbff != null)
            re = dbff.getValue();

        Vector v = fu.getFiles();
        ir = v.iterator();
        boolean isUploaded = false;
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            LogUtil.getLog(getClass()).info("getValueForSave: ff.getName()=" +
                                            ff.getName() + " fi.fieldName=" +
                                            fi.getFieldName());
            if (fi.getFieldName().equals(ff.getName())) {
                // re = fdao.getVisualPath() + "/" + fi.getDiskName();
                isUploaded = true;
            }
        }

        LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + "=" +
                                        ff.getValue());
        if (isUploaded && dbff != null) {
            // 如果formfield原来的值不为空，且上传的文件中存在有对应fieldName的，则获取对应的图片附件，将其删除
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Document doc = new Document();
            doc = doc.getDocument(wf.getDocId());
            LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() +
                                            " docId=" + wf.getDocId());
            // LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + " doc.isLoaded=" + doc.isLoaded());

            Vector vt = doc.getAttachments(1);
            ir = vt.iterator();
            // 取得ID为最小的对应的attach
            int count = 0;
            int minId = Integer.MAX_VALUE;
            Attachment lastAtt = null;
            while (ir.hasNext()) {
                Attachment att = (Attachment) ir.next();
                // LogUtil.getLog(getClass()).info("getValueForSave:" + att.getFieldName() + " ff.getName()=" + ff.getName());

                if (att.getFieldName().equals(ff.getName())) {
                    if (minId==Integer.MAX_VALUE) {
                        re = flowId + "," +att.getId();
                    }
                    if (minId > att.getId()) {
                        minId = att.getId();
                        lastAtt = att;
                    }
                    else
                        re = flowId + "," + att.getId();
                    count++;
                }
            }

            // LogUtil.getLog(getClass()).info("getValueForSave:" + lastAtt + " count=" + count);

            // 如果相同fieldName的存在两个附件，则删除较早的一个
            if (lastAtt != null && count > 1) {
                lastAtt.del();
                DocContentCacheMgr dcm = new DocContentCacheMgr();
                dcm.refreshUpdate(doc.getID(), 1);
            }
        }
        return re;
    }

    public Object getValueForCreate(FormField ff, FileUpload fu, FormDb fd) {
        // 当在流程中时，fu的值为null，而当在visual模块中时，fu参数才可用
        if (fu == null)
            return ff.getDefaultValue();
        Vector v = fu.getFiles();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            if (fi.getFieldName().equals(ff.getName())) {
                //com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.
                //        FormDAO(fd);
                // return fdao.getVisualPath() + "/" + fi.getDiskName();
                return fi.getDiskName();
            }
        }
        return ff.getDefaultValue();
    }

    public Object getValueForSave(FormField ff, FormDb fd, long formDAOId, FileUpload fu) {
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

        String re = ff.getValue();
        if (dbff != null)
            re = dbff.getValue();

        Vector v = fu.getFiles();
        ir = v.iterator();
        boolean isUploaded = false;
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            LogUtil.getLog(getClass()).info("getValueForSave: ff.getName()=" +
                                            ff.getName() + " fi.fieldName=" +
                                            fi.getFieldName());
            if (fi.getFieldName().equals(ff.getName())) {
                // re = fdao.getVisualPath() + "/" + fi.getDiskName();
                re = fi.getDiskName();
                isUploaded = true;
            }
        }

        LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + "=" + ff.getValue());
        if (isUploaded && dbff != null &&
            !StrUtil.getNullStr(dbff.getValue()).equals("")) {
            // 如果formfield原来的值不为空，且上传的文件中存在有对应fieldName的，则获取对应的附件，将其删除
            Vector vt = fdao.getAttachments();
            ir = vt.iterator();
            // 取得ID为最小的attach，并将其删除
            int count = 0;
            int minId = Integer.MAX_VALUE;
            com.redmoon.oa.visual.Attachment lastAtt = null;
            while (ir.hasNext()) {
                com.redmoon.oa.visual.Attachment att = (com.redmoon.oa.visual.Attachment) ir.next();
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
