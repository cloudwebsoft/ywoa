package com.redmoon.oa.exam;

import java.util.ArrayList;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.fileark.VideoTag;

/**
 * @Description: htm解析图片视频方法，题库管理
 * @author: sht
 * @Date: 2017-12-26上午10:48:58
 */
public class ExamFileUpdate {
	public boolean updateExamFile(String content,int questionID){
	     // 将exam的id与上传的临时图片文件相关联，当fckwebedit_new.jsp方式上传时，才会可能有临时图片文件
	        ArrayList al = new ArrayList();            
	        Attachment att = new Attachment();
	        // 解析content ,id 为传过来的试卷id
	        try {
	            Parser myParser;
	            NodeList nodeList = null;
	            myParser = Parser.createParser(content, "utf-8");
	            
	            PrototypicalNodeFactory pnf = new PrototypicalNodeFactory(); 
	            pnf.registerTag(new VideoTag()); 
	            myParser.setNodeFactory(pnf); 
	            
	            NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
	            NodeFilter videoFilter = new NodeClassFilter(VideoTag.class);
	            
	            OrFilter lastFilter = new OrFilter();
	            lastFilter.setPredicates(new NodeFilter[] {imgFilter, videoFilter});
	            nodeList = myParser.parse(lastFilter);
	            Node[] nodes = nodeList.toNodeArray();
	            for (int i = 0; i < nodes.length; i++) {
	                Node anode = (Node) nodes[i];
	                if (anode instanceof ImageTag) {
	                    ImageTag imagenode = (ImageTag) anode;
	                    String url = imagenode.getImageURL();
	                    String ext = StrUtil.getFileExt(url).toLowerCase();
	                    // 如果地址完整
	                    if (ext.equals("gif") || ext.equals("png") ||
	                        ext.equals("jpg") || ext.equals("jpeg")) {
	                        int p = url.lastIndexOf("/");
	                        String diskName = url.substring(p+1);
	                        int tmpId = att.getTmpAttId(diskName);
	                        if (tmpId!=-1) {
	                        	al.add(String.valueOf(tmpId));
	                        }
	                    }
	                }
	                else if (anode instanceof VideoTag) {
	                	VideoTag imagenode = (VideoTag) anode;
	                    String url = imagenode.getAttribute("src");
	                    int p = url.lastIndexOf("/");
	                    String diskName = url.substring(p+1);
	                    int tmpId = att.getTmpAttId(diskName);
	                    if (tmpId!=-1) {
	                    	al.add(String.valueOf(tmpId));
	                    }	                    
	                }                    
	            }
	        } catch (ParserException e) {
	            LogUtil.getLog(StrUtil.class.getName()).error("Update:" +
	                    e.getMessage());
	        }
	        
	        Object[] tmpAttachIds = al.toArray();
	        int len = tmpAttachIds.length;
	        for (int k = 0; k < len; k++) {
	            att = new Attachment(Integer.parseInt((String)tmpAttachIds[
	                    k]));
	            att.setDocId(questionID);
	            att.setPageNum(1);
	            att.save();
	            return true;
	        } 
		return false;
		
	}

}
