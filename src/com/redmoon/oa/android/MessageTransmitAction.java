package com.redmoon.oa.android;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.oa.message.Attachment;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MessageMgr;
import com.redmoon.oa.person.UserDb;

public class MessageTransmitAction {
	private String skey = "";
	private String result = "";
	private int id;
	private String receiver = "";
	private String title = "";
	private String content = "";

	private File[] upload; // 封装文件属性
	private String[] uploadContentType;// 文件类型
	private String[] uploadFileName;// 文件名称
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}


	public File[] getUpload() {
		return upload;
	}

	public void setUpload(File[] upload) {
		this.upload = upload;
	}

	public String[] getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String[] uploadContentType) {
		this.uploadContentType = uploadContentType;
	}

	public String[] getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String[] uploadFileName) {
		this.uploadFileName = uploadFileName;
	}
	
	public String execute() {
		JSONObject json = new JSONObject();

		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		if (re) {
			try {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {			
			String receivers = getReceiver();
			Attachment att = new Attachment();
			MessageDb md_transmit = new MessageDb();
			try {
				re = md_transmit.AddMsgByMobile(privilege.getUserName(getSkey()), receivers,
				 		getTitle(), getContent(), getUpload(), uploadFileName);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (re) {
				MessageDb msg = new MessageDb();
				msg = (MessageDb)msg.getMessageDb(getId());
				Iterator ir = msg.getAttachments().iterator();
				while (ir.hasNext()) {
					Attachment att_o = (Attachment) ir.next();
					att.setMsgId(md_transmit.getId());
					att.setName(att_o.getName());
					att.setDiskName(att_o.getDiskName());
					att.setVisualPath(att_o.getVisualPath());
					att.setSize(att_o.getSize());
					att.create();
				}
			}
			
			try {
				re = md_transmit.AddMsgByMobile(privilege.getUserName(getSkey()),getTitle(),getContent(), getUpload(), uploadFileName);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (re) {
				MessageDb msg = new MessageDb();
				msg = (MessageDb)msg.getMessageDb(getId());
				Iterator ir = msg.getAttachments().iterator();
				while (ir.hasNext()) {
					Attachment att_o = (Attachment) ir.next();
					att.setMsgId(md_transmit.getId());
					att.setName(att_o.getName());
					att.setDiskName(att_o.getDiskName());
					att.setVisualPath(att_o.getVisualPath());
					att.setSize(att_o.getSize());
					att.create();
				}
			}	
			
			json.put("res", "0");
			json.put("msg", "操作成功");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
