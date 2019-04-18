package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.db.SQLUtil;
import com.redmoon.oa.worklog.Config;
import com.redmoon.oa.worklog.WorkLogDb;

public class MyworkAddAction {
	private String skey = "";
	private String result = "";
	private String content = "";

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
		String userName = privilege.getUserName(getSkey());
		WorkLogDb wld = new WorkLogDb();

		if (wld.isWorkLogTodayWritten(userName)) {
			try {
				json.put("res", "-1");
				json.put("msg", "您当天的工作日志已记录，请不要重复提交！");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		wld.setContent(getContent());
		wld.setUserName(userName);
		String itemType = getWorkLogItemType(getContent());
		wld.setItemType(itemType);
		try {
			re = wld.create();
			if (re) {
				json.put("res", "0");
				json.put("msg", "添加成功");
			} else {
				json.put("res", "-1");
				json.put("msg", "添加失败");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}

	/**
	 * 获取工作日报:itemType值
	 * 
	 * @param itemContent
	 * @return
	 */
	private String getWorkLogItemType(String itemContent) {
		Config config = Config.getInstance();
		String itemType = "";

		List list = config.getRoot().getChild("items").getChildren();

		if (list != null) {
			Iterator ir = list.iterator();
			int i = 0;
			while (ir.hasNext()) {
				Element element = (Element) ir.next();
				String title = element.getChildText("title");
				boolean canNull = element.getChildText("canNull")
						.equals("true");
				int wordCount = StrUtil.toInt(
						element.getChildText("wordCount"), -1);
				if (ir.hasNext()) {
					if (itemContent.equals("")) {
						itemContent = "NULL";
					}
					itemType += title + ":#" + itemContent + ":#" + canNull
							+ ":#" + wordCount + "a#a";
				} else {
					if (itemContent.equals("")) {
						itemContent = "NULL";
					}
					itemType += title + ":#" + itemContent + ":#" + canNull
							+ ":#" + wordCount;
				}
				i++;
			}
		}
		return itemType;
	}
}
