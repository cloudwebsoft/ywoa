package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import com.redmoon.oa.netdisk.Directory;
import com.redmoon.oa.netdisk.Leaf;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;

import com.redmoon.oa.netdisk.Attachment;
import com.redmoon.oa.netdisk.Document;

public class NetdiskListAction {
	private String skey = "";
	private String result = "";
	private String dircode = "";
		
	private int pagenum;
	private int pagesize;
	public int getPagenum() {
		return pagenum;
	}
	public void setPagenum(int pagenum) {
		this.pagenum = pagenum;
	}
	public int getPagesize() {
		return pagesize;
	}
	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}
	public String getSkey() {
		return skey;
	}
	public void setSkey(String skey) {
		this.skey = skey;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
	public String getDircode() {
		return dircode;
	}
	public void setDircode(String dircode) {
		this.dircode = dircode;
	}
	public String execute() {
		JSONObject json = new JSONObject(); 
		
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		if(re){
			try {
				json.put("res","-2");
				json.put("msg","时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Document doc = new Document();
		
		if(getDircode().equals("")){
			setDircode(privilege.getUserName(getSkey()));
			
			String root_code = privilege.getUserName(getSkey());
			Leaf leaf = new Leaf();
			leaf = leaf.getLeaf(root_code);
			if (leaf==null || !leaf.isLoaded()) {
				// 为用户初始化网盘
				leaf = new Leaf();
				try {
					leaf.initRootOfUser(root_code);
					doc.getIDOrCreateByCode(getDircode(), privilege.getUserName(getSkey()));
				} catch (ErrMsgException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}
		
		
		doc = doc.getDocumentByDirCode(getDircode());
		
		if(doc==null){
			try {
				json.put("res","-1");
				json.put("msg","目录不存在");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String sql = "SELECT id FROM netdisk_document_attach WHERE doc_id=" + doc.getID() + " and page_num=1  and is_current=1 and is_deleted=0  order by ";
		sql += " uploadDate desc ";
   
		
		Attachment am = new Attachment();
		int curpage = getPagenum();   //第几页
		int pagesize = getPagesize(); //每页显示多少条

		try {
			ListResult lr = am.listResult(sql, curpage, pagesize);
			int total = lr.getTotal();
			Vector attachments = lr.getResult();
			Iterator ir = attachments.iterator();
			json.put("res","0");
			json.put("msg","操作成功");
			json.put("total",String.valueOf(total));
			
			JSONObject result = new JSONObject(); 
			result.put("count",String.valueOf(pagesize));
			JSONArray files = new JSONArray(); 		
			long fileLength = -1;
			String downPath = "";
			while (ir.hasNext()) {
				am = (Attachment) ir.next(); 
				fileLength = (long)am.getSize()/1024;
				if(fileLength == 0 && (long)am.getSize() > 0)
				   fileLength = 1; 		
				JSONObject file = new JSONObject();
				file.put("id",String.valueOf(am.getId()));
				file.put("doc_id",String.valueOf(am.getDocId()));
				file.put("title",am.getName());			
				file.put("size",fileLength+"KB");				
				file.put("createdate", DateUtil.format(am.getUploadDate(), "yyyy-MM-dd HH:mm:ss"));
				downPath = "public/android/netdisk_getfile.jsp?"+"id="+am.getDocId()+"&attachId="+am.getId();
				file.put("url",downPath);	
				files.put(file);
			}	
			result.put("files",files);	
			
			Directory dir = new Directory();
			Leaf leaf = dir.getLeaf(getDircode());
			
			JSONArray categorys = new JSONArray(); 	
			Iterator irch = leaf.getChildren().iterator();
			while (irch.hasNext()) {
				Leaf clf = (Leaf)irch.next();
				JSONObject category = new JSONObject();
				category.put("id",clf.getCode());
				category.put("title",clf.getName());
				category.put("dircode",clf.getCode());
				category.put("createdate",clf.getAddDate());
				categorys.put(category);				
			}
			result.put("categorys",categorys);	
			json.put("result",result);				
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		setResult(json.toString());
		return "SUCCESS";
	}		
}
