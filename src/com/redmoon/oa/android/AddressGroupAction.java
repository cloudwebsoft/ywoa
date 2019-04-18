package com.redmoon.oa.android;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.address.AddressDb;
import com.redmoon.oa.address.AddressTypeDb;
import com.redmoon.oa.address.Leaf;

public class AddressGroupAction {
	private String skey = "";
	private String result = "";
	private String dircode = "";
	private String is_public;
	
	
	

	public String getIs_public() {
		return is_public;
	}


	public void setIs_public(String isPublic) {
		is_public = isPublic;
	}


	public int getPagenum() {
		return pagenum;
	}

	private int pagenum;
	private int pagesize;
	
	public String getSkey() {
		return skey;
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
	
	public String getDircode() {
		return dircode;
	}
	public void setDircode(String dircode) {
		this.dircode = dircode;
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

		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			
			JSONArray groupTypes = new JSONArray();
			Leaf lf = new Leaf();
			if (dircode.equals("")){
				if(getIs_public() != null){
					if(getIs_public().equals("1")){
						dircode = Leaf.USER_NAME_PUBLIC;
					}else if(getIs_public().equals("0")){
						dircode = privilege.getUserName(getSkey());
					}
				}else{
					dircode = Leaf.USER_NAME_PUBLIC;
				}
				
			}
			lf = lf.getLeaf(dircode);
			if(lf != null) {
				json.put("dirName", lf.getName());
				Iterator ir = lf.getChildren().iterator();
				while (ir.hasNext()) {
					lf = (Leaf) ir.next();

					JSONObject groupType = new JSONObject();
					groupType.put("grouptype", lf.getCode());
					groupType.put("name", lf.getName());
					groupType.put("type",getIs_public());  //判断是个人通讯还是公共通讯录列别
					groupType.put("mode", "show");
					groupTypes.put(groupType);
				}

				json.put("groupTypes", groupTypes);
				
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String userName = privilege.getUserName(getSkey());
		String unitCode = privilege.getUserUnitCode(getSkey());

		if (!dircode.equals("")) {
			String sql = "";
			if(getIs_public()!=null) {
				if(getIs_public().equals("1")){
					sql = "select id from address where type=" + AddressDb.TYPE_PUBLIC; // getType();
					sql += " and typeId = " + StrUtil.sqlstr(dircode);
				}else if(getIs_public().equals("0")){
					sql = "select id from address where type="+AddressDb.TYPE_USER+" and userName="+StrUtil.sqlstr(userName);
					sql += " and typeId = " + StrUtil.sqlstr(dircode);
				}
				
			}else{
				sql = "select id from address where type=" + AddressDb.TYPE_PUBLIC; // getType();
				sql += " and typeId = " + StrUtil.sqlstr(dircode);
			}
			
			int curpage = getPagenum(); // 第几页
			int pagesize = getPagesize(); // 每页显示多少条
			AddressDb addr = new AddressDb();
			try {
				ListResult lr = addr.listResult(sql, curpage, pagesize);
				int total = lr.getTotal();
				json.put("res", "0");
				json.put("msg", "操作成功");
				json.put("total", String.valueOf(total));
				Vector v = lr.getResult();
				Iterator ir = null;
				if (v != null)
					ir = v.iterator();
	
				JSONObject result = new JSONObject();
				result.put("count", String.valueOf(pagesize));
	
				JSONArray addressers = new JSONArray();
				while (ir != null && ir.hasNext()) {
					addr = (AddressDb) ir.next();
					JSONObject addresser = new JSONObject();
					addresser.put("id", String.valueOf(addr.getId()));
					addresser.put("name", addr.getPerson());
					addresser.put("mobile", addr.getMobile());
					addresser.put("shortMobile", StrUtil.getNullStr(addr.getMSN()));
					addresser.put("operationPhone", addr.getOperationPhone());
					addresser.put("email", addr.getEmail());
					addresser.put("job", addr.getJob()); // 职务
					addressers.put(addresser);
				}
				result.put("addressers", addressers);
				json.put("result", result);
	
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	    setResult(json.toString());
		return "SUCCESS";
	}
}
