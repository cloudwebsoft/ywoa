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

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.flow.Document;
import com.redmoon.oa.flow.Attachment;
import com.redmoon.oa.pvg.Privilege;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
/**
 * 附件组
 * @author Administrator
 *
 */
public class AttachmentsCtl extends AbstractMacroCtl {
	
	public AttachmentsCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        Privilege pvg = new Privilege();
        String myname = pvg.getUser(request);
    	String str = "";
       	String pageType = (String)request.getAttribute("pageType");
    	
        //System.out.println(AttachmentsCtl.class+":::convertToHTMLCtl::"+StrUtil.getNullStr(ff.getValue())+","+StrUtil.getNullStr(ff.getValue()).equals(""));
    	if (request.getAttribute("myActionId")!=null) {
	        // 如果附件中已存在赋值，则显示图片
	        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
	        	str = "";
	        	String[] items = ff.getValue().split(";");
	        	for(int i = 0; i < items.length; i ++) {
		            String[] ary = items[i].split(",");
		            if (ary.length==2) {
		                Attachment att = new Attachment(Integer.valueOf(ary[1]).intValue());
		                String fName = StrUtil.getNullStr(att.getFieldName());
					 	if (!fName.startsWith(ff.getName())) {
					 		continue;
					 	}		                
		                if (att != null && att.isLoaded()) {
			                String diskName = StrUtil.getNullStr(att.getDiskName());
			                String ext = diskName.substring(diskName.lastIndexOf(".")+1,diskName.length());
			                if(ext.equals("doc")||ext.equals("docx")){
			                	str += "<div id='"+ff.getName()+"File"+i+"'><a href=\"javascript:;\" onClick=\"javascript:ReviseByUserColor('',1," + ary[0] + "," + ary[1] + ")\">" + att.getName() + "</a>";
			                	if(att.getCreator().equals(myname) && ff.isEditable()){
			                		str += "&nbsp;&nbsp;<a href='javascript:;' onclick='if (confirm(\"您确定要删除么？\")) "+ff.getName()+"DelFile("+i+","+ary[1]+")'>[删除]</a>";
			                	}
			                	str += "</div>";
			                }else{
			                	str += "<div id='"+ff.getName()+"File"+i+"'><a href=\"" + "flow_getfile.jsp?attachId=" + ary[1] +
			                    "&flowId=" + ary[0] + "\" target=\"_blank\">" + att.getName() + "</a>";
			                	if(att.getCreator().equals(myname) && ff.isEditable()){
			                		str += "&nbsp;&nbsp;<a href='javascript:;' onclick='if (confirm(\"您确定要删除么？\")) "+ff.getName()+"DelFile("+i+","+ary[1]+")'>[删除]</a>";
			                	}
			                	str += "</div>";
			                }
		            	}
		            }
		        }
	        }
    	}
    	else if (!"flowShow".equals(pageType)) {
           	long fdaoId = StrUtil.toLong((String)request.getAttribute("cwsId"));
           	String formCode = (String)request.getAttribute("formCode");
           	
           	FormDb fd = new FormDb();
           	fd = fd.getFormDb(formCode);
           	
           	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
           	fdao = fdao.getFormDAO(fdaoId, fd);
           	
           	boolean isReport = "show".equals(pageType);
           	
           	Iterator ir = fdao.getAttachments().iterator();
			while (ir.hasNext()) {
			 	com.redmoon.oa.visual.Attachment att = (com.redmoon.oa.visual.Attachment) ir.next();
                String fName = StrUtil.getNullStr(att.getFieldName());
                
			 	if (!fName.startsWith(ff.getName())) {
			 		continue;
			 	}
			 	// String diskName = StrUtil.getNullStr(att.getDiskName());
                // String ext = diskName.substring(diskName.lastIndexOf(".")+1,diskName.length());
               	str += "<div id='"+ff.getName()+"File'><img src='" + Global.getRootPath() + "/images/attach.gif" + "' />&nbsp;&nbsp;<a href=\"" + Global.getRootPath() + "/visual_getfile.jsp?attachId=" + att.getId() +
                   "\" target=\"_blank\">" + att.getName() + "</a>";
               	if (!isReport) {
               		str += "&nbsp;&nbsp;<a href='javascript:;' onclick='delAtt(" + att.getId() + ")'>[删除]</a>";
               	}
               	str += "</div>";
			}
    	}
        
        if(!str.equals("")){
        	//str += "<br/>";
        }
        
        if (ff.isEditable()) {
	        str += "<input name='" + ff.getName() + "' type='file' size=15><div id='"+ff.getName()+"Div'></div>";
	        str += "<input type='hidden' name='"+ff.getName()+"Hid' id='"+ff.getName()+"Hid' value='1'/>";
	        str += "<input type='hidden' name='"+ff.getName()+"DelHid' id='"+ff.getName()+"DelHid' value=''/>";
	        str += "<input type='button' name='"+ff.getName()+"btn1' value='增加' onclick='add"+ff.getName()+"Files()' />";
	        //str += "<input type='button' name='"+ff.getName()+"btn2' value='减少' onclick='del"+ff.getName()+"Files()' />";
	        str += "<script>";
	        str += "function add"+ff.getName()+"Files(){";
	        str += "var i = document.getElementById('"+ff.getName()+"Hid').value;";
	        str += "if(i==0){i = 1;}";
	        str += "var temp = parseInt(i)+1;";
	        str += "var str = \"<div id='"+ff.getName()+"Div\"+i+\"'><input type='file' name='"+ff.getName()+"\"+i+\"' size=15 /><input  type='button' name='"+ff.getName()+"btn\"+i+\"' value='删除' onclick='del"+ff.getName()+"Files(\"+i+\")'/></div>\";";
	        str += "var fjdiv = document.getElementById('"+ff.getName()+"Div');";
	        str += "var div = document.createElement('span');";
	        str += "div.innerHTML=str;";
	        str += "fjdiv.appendChild(div);";
	        /*str += "if(i==1){";
	        str += "document.getElementById('"+ff.getName()+"Div').innerHTML = str;";
	        str += "}else{";
	        str += "document.getElementById('"+ff.getName()+"Div'+(parseInt(i)-1)).innerHTML = str;";
	        str += "}";*/
	        str += "document.getElementById('"+ff.getName()+"Hid').value = temp;";
	        str +="}";
	        str += "function del"+ff.getName()+"Files(i){";
	        //str += "var i = document.getElementById('"+ff.getName()+"Hid').value;";
	        str += "if(i<1){";
	        str += "return;";
	        str += "}else{";
	        str += "var temp = document.getElementById('"+ff.getName()+"Div'+i);";
	        str += "if(temp!=null){";
	        str += "temp.parentNode.removeChild(temp);";
	        str += "}";
	        str += "}";
	        /*str += "}else if(i==1){";
	        str += "document.getElementById('"+ff.getName()+"Div').innerHTML = '';";
	        str += "}else{";
	        str += "var temp = parseInt(i)-2;";
	        str += "if(temp == 0){";
	        str += "document.getElementById('"+ff.getName()+"Div').innerHTML = '';";
	        str += "}else{";
	        str += "document.getElementById('"+ff.getName()+"Div'+temp).innerHTML = '';";
	        str += "}";
	        str += "}";
	        str += "document.getElementById('"+ff.getName()+"Hid').value = parseInt(i)-1;";*/
	        str += "}";
	        str += "function "+ff.getName()+"DelFile(i,aid){";
	        str += "document.getElementById('"+ff.getName()+"File'+i).innerHTML = '';";
	        str += "var toDel = document.getElementById('"+ff.getName()+"DelHid').value;";
	        str += "if(toDel!=''){";
	        str += "toDel = toDel + ','+aid;";
	        str += "}else{";
	        str += "toDel = aid;";
	        str += "}";
	        str += "document.getElementById('"+ff.getName()+"DelHid').value=toDel;";
	        str += "}";
	        str += "function ReviseByUserColor"+ff.getName()+"(a,b,c){";
	        str += "ReviseByUserColor('',a,b,c);";
	        str += "}";
	        str += "</script>";
        } else {
        	str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' type='hidden' size=15><div id='" + ff.getName() + "Box'></div>";
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
    
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
    	String str = "";
        //System.out.println(AttachmentsCtl.class+":::convertToHTMLCtl::"+StrUtil.getNullStr(ff.getValue())+","+StrUtil.getNullStr(ff.getValue()).equals(""));
        // 如果附件中已存在赋值，则显示图片
        if (!StrUtil.getNullStr(fieldValue).equals("")) {
            // str += "<img src='" + Global.getRootPath() + "/" + ff.getValue() +
            //        "'><BR>";
        	str = "";
        	String[] items = fieldValue.split(";");
        	//System.out.println(AttachmentsCtl.class+":::"+items);
        	for(int i = 0; i < items.length; i ++){
        		//System.out.println(AttachmentsCtl.class+":::"+items[i]);
	            String[] ary = items[i].split(",");
	            if (ary.length==2) {
	                Attachment att = new Attachment(Integer.valueOf(ary[1]).intValue());
	                if (att != null && att.isLoaded()) {
		                str += "<div id='"+ff.getName()+"File"+i+"'><a href=\"" + "flow_getfile.jsp?attachId=" + ary[1] +
			                "&flowId=" + ary[0] + "\" target=\"_blank\">" + att.getName() + "</a>";
			            str += "</div>";
	                }
	            }
	        }
        }
        return str;
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 不disable序列
        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                     "', '', " + "o('cws_textarea_" + ff.getName() +
                     "').value);\n";
        str += "DisableCtl('" + ff.getName() + "btn1', '" + ff.getType() +"','', '');\n";
        str += "DisableCtl('" + ff.getName() + "btn2', '" + ff.getType() +"','', '');\n";
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
	                if (att != null && att.isLoaded()) {
	                	str += "var "+ff.getName()+i+"temp = \"<a href='" + "flow_getfile.jsp?attachId=" + ary[1] +"&flowId=" + ary[0] + "' target='_blank'>" + att.getName() + "</a>\";\n";
	                	str += "document.getElementById(\'"+ff.getName()+"File"+i+"\').innerHTML = "+ff.getName()+i+"temp;\n";
	                }
	            }
	        }
        }
        
        return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(IFormDAO ifdao, FormField ff) {
//    	String str = "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() +"','');\n";
//    	str += "ReplaceCtlWithValue('" + ff.getName() + "btn1', '" + ff.getType() +"','');\n";
//    	str += "ReplaceCtlWithValue('" + ff.getName() + "btn2', '" + ff.getType() +"','');\n";
//        return str;
    	//return super.getReplaceCtlWithValueScript(ff);
    
    	String str = "";
    	
    	String value = StrUtil.getNullStr(ff.getValue());
        if(!value.equals("")){
        	//String strTemp = "";
        	String[] items = value.split(";");
        	//System.out.println(AttachmentsCtl.class+":::"+items);
        	for(int i = 0; i < items.length; i ++) {
        		// System.out.println(AttachmentsCtl.class+":::"+items[i]);
	            String[] ary = items[i].split(",");
	            if (ary.length==2) {
	            	// if (ifdao.getFlowId()!=com.redmoon.oa.visual.FormDAO.NONEFLOWID) {
	            	if (ifdao instanceof com.redmoon.oa.flow.FormDAO) { 
		                Attachment att = new Attachment(Integer.valueOf(ary[1]).intValue());
		                if (att != null && att.isLoaded()) {
		                	str += "var "+ff.getName()+i+"temp = \"<a href='" + "flow_getfile.jsp?attachId=" + ary[1] +"&flowId=" + ary[0] + "' target='_blank'>" + att.getName() + "</a><br/>\";\n";
		                	// str += "document.getElementById(\'"+ff.getName()+"File"+i+"\').innerHTML = "+ff.getName()+i+"temp;\n";
		                	str += "o('" + ff.getName() + "Box').innerHTML += "+ff.getName()+i+"temp;\n";
		                }
	            	}
	            	else {
		                com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(Integer.valueOf(ary[1]).intValue());
		                if (att != null && att.isLoaded()) {
		                	// str += "o('" + ff.getName() + "Div').innerHTML += \"<a href='" + Global.getRootPath() + "/visual_getfile.jsp?attachId=" + ary[1] + "' target='_blank'>" + att.getName() + "</a><BR>\";\n";
		                	str += "o('" + ff.getName() + "Box').innerHTML += \"<a href='" + Global.getRootPath() + "/visual_getfile.jsp?attachId=" + ary[1] + "' target='_blank'>" + att.getName() + "</a><BR>\";\n";		                	
		                }	            		
	            	}
	            }
	        }
        }
        return str;
    }

    public Object getValueForSave(FormField ff, int flowId, FormDb fd,
                                  FileUpload fu) {
        // ff参数来自于FormDAO中的doUpload，并不是来自于数据库，所以需从数据库中提取
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

        String re = "";//ff.getValue();
        if (dbff != null)
            re = dbff.getValue();
      //以上找到数据库中存储的附件的值

        String filesCountStr = StrUtil.getNullStr(fu.getFieldValue(ff.getName()+"Hid"));
        int filesCount = 0;
        if(!filesCountStr.equals("")){
        	filesCount = Integer.valueOf(filesCountStr);
        }
        Vector v = fu.getFiles();
        ir = v.iterator();
        boolean isUploaded = false;
        boolean isToDel = false;
        String idsToDel = StrUtil.getNullStr(fu.getFieldValue(ff.getName()+"DelHid"));
        String[] idsToDels = null;
        if(!idsToDel.equals("")){
        	isToDel = true;
        	isUploaded = true;
        	idsToDels = idsToDel.split(",");
        }
        while (!isUploaded&&ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            LogUtil.getLog(getClass()).info("getValueForSave: ff.getName()=" +
                                            ff.getName() + " fi.fieldName=" +
                                            fi.getFieldName());
            if (fi.getFieldName().equals(ff.getName())) {
                // re = fdao.getVisualPath() + "/" + fi.getDiskName();
                isUploaded = true;
            }else{
            	for(int i = filesCount; i >1; i --){
            		if(fi.getFieldName().equals(ff.getName()+(i-1))){
            			isUploaded = true;
            			break;
            		}
            	}
            }
        }
        if(isUploaded){
        	WorkflowDb wf = new WorkflowDb();
            wf = wf.getWorkflowDb(flowId);
            Document doc = new Document();
            doc = doc.getDocument(wf.getDocId());
            Vector vt = doc.getAttachments(1);
            ir = vt.iterator();
            //Attachment lastAtt = null;
            while (ir.hasNext()) {
            	Attachment att = (Attachment) ir.next();
            	if(isToDel){
            		if(isToDel(idsToDels, att.getId()+"")){
            			if(!re.equals("")){
            				re = rebuildRe(re,att.getId()+"");
            			}
            			att.del();
            			continue;
            		}
            	}
            	if(filesCount==1){
            		if (att.getFieldName().equals(ff.getName())) {
                		if(re.equals("")){
                			re = flowId + "," +att.getId();
                		}else{
                			if(re.indexOf(flowId + "," +att.getId())==-1){
                				re += ";" + flowId + "," +att.getId();
                			}
                		}
                	}
            	}else{
            		if (att.getFieldName().equals(ff.getName())) {
            			if(re.equals("")){
                			re = flowId + "," +att.getId();
                		}else{
                			if(re.indexOf(flowId + "," +att.getId())==-1){
                				re += ";" + flowId + "," +att.getId();
                			}
                		}
            			//System.out.println(AttachmentsCtl.class+"::aa::"+re);
            		}else{
		            	for(int i = filesCount; i > 1; i --){
		            		if(att.getFieldName().equals(ff.getName()+(i-1))){
		            			if(re.equals("")){
		                			re = flowId + "," +att.getId();
		                		}else{
		                			if(re.indexOf(flowId + "," +att.getId())==-1){
		                				re += ";" + flowId + "," +att.getId();
		                			}
		                		}
		            			//System.out.println(AttachmentsCtl.class+"::aa::"+re);
		            		}
		        		}
            		}
            	}
            	
            }
            //System.out.println(AttachmentsCtl.class+"::aabb::"+re);
        	/*for(int i = filesCount; i >= 0; i --){
        		if(i)
        	}*/
        }
        /*
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
        }*/
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
                com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.
                        FormDAO(fd);
                return fdao.getVisualPath() + "/" + fi.getDiskName();
            }
        }
        //System.out.println(getClass()+"::getValueForCreate::");
        return ff.getDefaultValue();
    }
/*
    public Object getValueForSave(FormField ff, FormDb fd, long formDAOId,
                                  FileUpload fu) {
        // ff参数来自于FormDAO中的doUpload，并不是来自于数据库，所以需从数据库中提取
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(formDAOId, fd);
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
                re = fdao.getVisualPath() + "/" + fi.getDiskName();
                isUploaded = true;
            }
        }

        LogUtil.getLog(getClass()).info("getValueForSave:" + ff.getName() + "=" +
                                        ff.getValue());
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
*/
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
    
    public boolean isToDel(String[] ids,String aid){
    	if(ids==null||ids.length==0){
    		return false;
    	}else{
    		boolean res = false;
    		for(int i = 0; i < ids.length; i ++){
    			if(ids[i].equals(aid)){
    				res = true;
    				break;
    			}
    		}
    		return res;
    	}
    }
    
    public String rebuildRe(String re,String aid){
    	if(re.equals("")){
    		return "";
    	}else{
    		//System.out.println(getClass()+"::aabbcc::"+re);
    		String res = "";
    		String[] temp = re.split(";");
    		for(int i = 0; i < temp.length; i ++){
    			String[] temp2 = temp[i].split(",");
    			//System.out.println(getClass()+"::aabbcc::"+temp[i]);
    			if(!aid.equals(temp2[1])){
    				if(res.equals("")){
    					res = temp[i];
    				}else{
    					res += ";"+temp[i];
    				}
    			}
    		}
    		return res;
    	}
    }
    

    /**
     * 在验证前获取表单域的值，用于附件、图片宏控件不能为空的检查
     * @param request
     * @param fu
     * @param flowId
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
    
    
    /**
     * 当智能模块创建记录后调用，如将文件的ID存入件组控件对应的字段中
     * @param request
     * @param fu
     * @param fdaoId
     */
/*    public void doAfterCreate(HttpServletRequest request, FileUpload fu, FormField ff, com.redmoon.oa.visual.FormDAO fdao) {
		StringBuffer val = new StringBuffer();
    	Iterator ir = fdao.getAttachments().iterator();
		while (ir.hasNext()) {
		  	com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) ir.next();
		  	if (am.getFieldName().startsWith(ff.getName())) {
		  		StrUtil.concat(val, ";", fdao.getId() + "," + am.getId());
		  	}
		}
		if (val.length() > 0) {
			fdao.setFieldValue(ff.getName(), val.toString());
			try {
				fdao.save();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(getClass()).error("doAfterCreate:" + e.getMessage());
				e.printStackTrace();
			}
		}
    }  */ 
}
