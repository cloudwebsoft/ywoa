package com.redmoon.oa.flow.macroctl;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.Attachment;
import com.redmoon.oa.flow.DocContentCacheMgr;
import com.redmoon.oa.flow.DocTemplateDb;
import com.redmoon.oa.flow.Document;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserDb;

public class NTKOCtl extends AbstractMacroCtl {
	
	@Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String width = "100%";
        String height = "600";
        String wh = ff.getDefaultValue().trim();
        wh = wh.replaceFirst("，", ",");
        String[] ary = StrUtil.split(wh, ",");
		if (ary != null && ary.length == 2) {
			width = ary[0];
			height = ary[1];
		}

        StringBuffer sb = new StringBuffer();
        sb.append("<span style='display:none'><input id='" + ff.getName() + "' name='" + ff.getName() + "' value='ntko' type='hidden' /></span>");
        sb.append("<object id=\"TANGER_OCX\" classid=\"clsid:C9BC4DFF-4248-4a3c-8A49-63A7D317F404\" codebase=\"" + request.getContextPath() + "/activex/OfficeControl.cab#version=5,0,2,1\" width=\"" + width + "\" height=\"" + height + "\" >");
        sb.append("<param name=\"CustomMenuCaption\" value=\"操作\">");
        sb.append("<param name=\"Caption\" value=\"文件\">\n");
        
        sb.append("<param name=\"MakerCaption\" value=\"cloudweb\">");
        sb.append("<param name=\"MakerKey\" value=\"0727BEFE0CCD576DFA15807DA058F1AC691E1904\">");
        sb.append("<param name=\"ProductCaption\" value=\"" + License.getInstance().getCompany() + "\">");
        sb.append("<param name=\"ProductKey\" value=\"" + License.getInstance().getOfficeControlKey() + "\">");

        // sb.append("<param name=\"MakerCaption\" value=\"cloudweb\">\n");
        // sb.append("<param name=\"MakerKey\" value=\"0727BEFE0CCD576DFA15807DA058F1AC691E1904\">\n");
        // sb.append("<param name=\"ProductCaption\" value=\"云网软件\">\n");
        // sb.append("<param name=\"ProductKey\" value=\"BE624E7FE922DB5392AB03D474FF952C5D45A9FB\">\n");

        sb.append("<SPAN STYLE=\"color:red\">该网页需要控件浏览.浏览器无法装载所需要的文档控件.请检查浏览器选项中的安全设置.</SPAN>");
        sb.append("</object>");
        
        if (request.getAttribute("macro_js_ntko")==null) {
            String flowId = (String)request.getAttribute("cwsId");
            
            // 如果是模块
            String pageType = (String)request.getAttribute("pageType");
            if ("show".equals(pageType)) {
                String[] arr = ff.getValue().split(",");
                if (arr.length==2) {
                    flowId = arr[0];
                }
            }
            
            WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(StrUtil.toInt(flowId));
            Document doc = new Document();
            doc = doc.getDocument(wf.getDocId());
            LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() +
                                            " docId=" + wf.getDocId());
            // LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + " doc.isLoaded=" + doc.isLoaded());

            String file_id = "";
            if (doc!=null) {
	            Vector vt = doc.getAttachments(1);
	            Iterator ir = vt.iterator();
	            while (ir.hasNext()) {
	                Attachment att = (Attachment) ir.next();
	                if (att.getFieldName().equals(ff.getName())) {
	                    file_id = "" + att.getId();
	                    break;
	                }
	            }
            }
            
            int workflowActionId = StrUtil.toInt((String)request.getAttribute("workflowActionId"), -1);
            int myActionId = StrUtil.toInt((String)request.getAttribute("myActionId"), -1);
            
            String editable = "true";
            if (!ff.isEditable()) {
            	editable = "false";
            }
            sb.append("<script src='" + request.getContextPath() + "/flow/macro/macro_js_ntko.jsp?flowId="+flowId+"&file_id=" + file_id + "&ntkoFieldName=" + StrUtil.UrlEncode(ff.getName()) + "&actionId=" + workflowActionId + "&myActionId=" + myActionId + "&editable=" + editable + "'></script>");
            
            sb.append("<script language=\"JScript\" for=\"TANGER_OCX\" event=\"OnCustomMenuCmd(menuIndex,menuCaption,menuID)\">\n");
            sb.append("switch(menuID)\n");
            sb.append("{\n");
            sb.append("case 1:\n");
            sb.append("userStamp();\n");
            sb.append("break;\n");
            sb.append("case 2:\n");
            sb.append("useHandSign();\n");
			sb.append("break;\n");
			sb.append("case 3:\n");
			sb.append("replaceText();\n");
			sb.append("break;\n");
			sb.append("case 4:\n");
			sb.append("TANGER_OCX_AcceptAllRevisions();\n");
			sb.append("break;\n");
			sb.append("}\n");
			sb.append("</script>\n");
            
            request.setAttribute("macro_js_ntko", "macro_js_ntko");
        }
        
        return sb.toString();
	}
	
    public Object getValueForCreate(int flowId, FormField ff) {
        // 根据流程的模板拷贝文件，如果没有模板，则生成一份空文件
    	WorkflowDb wf = new WorkflowDb();
    	wf = wf.getWorkflowDb(flowId);
    	String typeCode = wf.getTypeCode();
    	Leaf lf = new Leaf();
    	lf = lf.getLeaf(typeCode);
    	int templateId = lf.getTemplateId();
    	
    	String fileName = "", fullPath = "", diskName = "";
        FormDAO fdao = new FormDAO();

    	if (templateId!=-1) {
			DocTemplateDb dtd = new DocTemplateDb();
			dtd = dtd.getDocTemplateDb(templateId);
			if (!dtd.isLoaded()) {
				LogUtil.getLog(getClass()).error("流程：" + wf.getTitle() + " 的模板文件已不存在！");
			}
			else {
				fileName = dtd.getFileName();
				
		        String templatePath = Global.getRealPath() + "/upfile/file_flow_doc_template/" + fileName;
		        
	            diskName = FileUpload.getRandName() + ".doc";
	            // 判断路径是否存在，不存在则创建，因CopyFile并不会自动创建目录，会报：系统找不到指定文件
	            File f = new File(Global.getRealPath() + fdao.getVisualPath());
	            if (!f.exists()) {
	            	f.mkdir();
	            }
	            fullPath = Global.getRealPath() + fdao.getVisualPath() + "/" + diskName;	    		
	    		FileUtil.CopyFile(templatePath, fullPath);

			}
    	}
    	else {
            diskName = FileUpload.getRandName() + ".doc";
            fullPath = Global.getRealPath() + fdao.getVisualPath() + "/" + diskName;
            fileName = wf.getTitle() + ".doc";

    		String templatePath = Global.getRealPath() + "/flow/empty_word.doc";
    		
    		FileUtil.CopyFile(templatePath, fullPath);
    	}
    	
    	if (!fileName.equals("")) {
    		// 写入attachment表中
            Attachment att = new Attachment();
            att.setDocId(wf.getDocId());
            // att.setName(fi.getName()); // WebOffice控件不支持utf-8
            att.setName(fileName);
            att.setFullPath(fullPath);
            att.setDiskName(diskName);
            att.setVisualPath(fdao.getVisualPath());
            att.setPageNum(1);
            att.setCreator(UserDb.SYSTEM);
            att.setLockUser(UserDb.SYSTEM);

            Document doc = new Document();
            doc = doc.getDocument(wf.getDocId());
            int orders = doc.getAttachments(1).size() + 1;

            att.setOrders(orders);
            att.setFieldName(ff.getName());

            att.create();    	

    	}
    	
        return ff.getDefaultValue();
    }
	
	@Override
	public String getControlOptions(String arg0, FormField arg1) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getControlText(String arg0, FormField arg1) {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getControlType() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getControlValue(String arg0, FormField arg1) {
		// TODO Auto-generated method stub
		return "";
	}

	/**
	 * 取得保存表单时，控件应保存的值，用于流程中表单的处理，被调用前，流程中的附件已被保存
	 */
	public Object getValueForSave(FormField ff, int flowId, FormDb fd, FileUpload fu) {
		String re = ff.getValue();
		Vector v = fu.getFiles();
		Iterator ir = v.iterator();
		boolean isUploaded = false;
		while (ir.hasNext()) {
			FileInfo fi = (FileInfo) ir.next();
			LogUtil.getLog(getClass()).info("getValueForSave: ff.getName()=" + ff.getName() + " fi.fieldName=" + fi.getFieldName());
			if (fi.getFieldName().equals(ff.getName())) {
				// re = fdao.getVisualPath() + "/" + fi.getDiskName();
				isUploaded = true;
			}
		}

		LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + "=" + ff.getValue());
		if (isUploaded) {
			// 如果formfield原来的值不为空，且上传的文件中存在有对应fieldName的，则获取对应的图片附件，将其删除
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb(flowId);
			Document doc = new Document();
			doc = doc.getDocument(wf.getDocId());
			LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + " docId=" + wf.getDocId());
			// LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName()
			// + " doc.isLoaded=" + doc.isLoaded());

			Vector vt = doc.getAttachments(1);
			ir = vt.iterator();
			// lastAtt， 取得ID为最小的对应的attach
			int count = 0;
			int minId = Integer.MAX_VALUE;
			Attachment lastAtt = null;
			while (ir.hasNext()) {
				Attachment att = (Attachment) ir.next();
				// System.out.println("getValueForSave:" + att.getFieldName() + " ff.getName()=" + ff.getName());
				if (att.getFieldName().equals(ff.getName())) {
					if (minId == Integer.MAX_VALUE) {
						re = flowId + "," + att.getId();
					}
					if (minId > att.getId()) {
						minId = att.getId();
						lastAtt = att;
					} else
						re = flowId + "," + att.getId();
					count++;
				}
			}

			// System.out.println("getValueForSave:" + lastAtt.getId() + " count=" + count + " re=" + re);

			// 如果相同fieldName的存在两个附件，则删除较早的一个
			if (lastAtt != null && count > 1) {
				lastAtt.del();
				DocContentCacheMgr dcm = new DocContentCacheMgr();
				dcm.refreshUpdate(doc.getID(), 1);
			}
		}
		return re;
	}
    
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
		String str = "setCtlValue('" + ff.getName() + "', '"
		+ ff.getType() + "', 'ntko');\n";
		return str;
    }

}
