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
import com.redmoon.oa.address.Leaf;

public class AddressListAction {
	private String skey = "";
	private String result = "";
	private String type = "";
	private String op = "";
	private String cond = ""; // 查询列表值
	private String what = "";
	private String is_public = "1";
	private String searchCond = "";
	private String searchWhat = "";
	private String searchOp = "";

	public String getSearchCond() {
		return searchCond;
	}

	public void setSearchCond(String searchCond) {
		this.searchCond = searchCond;
	}

	public String getSearchWhat() {
		return searchWhat;
	}

	public void setSearchWhat(String searchWhat) {
		this.searchWhat = searchWhat;
	}

	public String getSearchOp() {
		return searchOp;
	}

	public void setSearchOp(String searchOp) {
		this.searchOp = searchOp;
	}

	public String getIs_public() {
		return is_public;
	}

	public void setIs_public(String isPublic) {
		is_public = isPublic;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getCond() {
		return cond;
	}

	public void setCond(String cond) {
		this.cond = cond;
	}

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String groupType;
	private int pagenum;
	private int pagesize;

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

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

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

	public String execute() {
		JSONObject json = new JSONObject();
		if (getSkey() == null || "".equals(getSkey())) {
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
		String unitCode = privilege.getUserUnitCode(getSkey());
		if (searchOp != null && !searchOp.equals("")
				&& searchOp.equals("searchOne")) {
			String sql = "";
			if(type!=null){
				if (type.equals("1")) {
					sql = "select id from address where type="
							+ AddressDb.TYPE_PUBLIC; // getType();
				} else {
					sql = "select id from address where type="
							+ AddressDb.TYPE_USER + " and userName="
							+ StrUtil.sqlstr(userName);
				}
				
			}else{
				sql = "select id from address where type="
					+ AddressDb.TYPE_PUBLIC; // getType();
			}
	
			if (getSearchCond().equals("name")) {
				sql += " and person like "
						+ StrUtil.sqlstr("%" + getSearchWhat() + "%");
			} else if (getSearchCond().equals("mobile")) {
				sql += " and mobile like "
						+ StrUtil.sqlstr("%" + getSearchWhat() + "%");
			} else if (getSearchCond().equals("shortMobile")) {
				sql += " and msn like "
						+ StrUtil.sqlstr("%" + getSearchWhat() + "%");
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
					addresser.put("shortMobile", StrUtil.getNullStr(addr
							.getMSN()));
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

			setResult(json.toString());

		} else {
			String sql = " ";
			if(is_public == null) {
				sql = "select id from address where type="
					+ AddressDb.TYPE_PUBLIC;// getType();
			}else{
				if (is_public.equals("1")) {
					sql = "select id from address where type="
							+ AddressDb.TYPE_PUBLIC;// getType();
				}else {
					sql = "select id from address where type="
							+ AddressDb.TYPE_USER + " and userName="
							+ StrUtil.sqlstr(userName);
				}
			}
	
			if (groupType == null) {
				groupType = "";
			}
			if (!groupType.equals("") && !groupType.equals("0"))
				sql += " and typeId = " + StrUtil.sqlstr(groupType);
			// if (getType() != AddressDb.TYPE_PUBLIC)
			// sql += " and userName=" + StrUtil.sqlstr(userName);

			if (getOp().equals("search")) {
				if(is_public!=null) {
					if (is_public.equals("1")) {
						sql = "select id from address where type="
								+ AddressDb.TYPE_PUBLIC; // getType();
					} else {
						sql = "select id from address where type="
								+ AddressDb.TYPE_USER + " and userName="
								+ StrUtil.sqlstr(userName);
					}
					
				}else{
					sql = "select id from address where type="
						+ AddressDb.TYPE_PUBLIC; // getType();
					
				}
				
				
				if (getCond().equals("name")) {
					sql += " and person like "
							+ StrUtil.sqlstr("%" + getWhat() + "%");
				} else if (getCond().equals("mobile")) {
					sql += " and mobile like "
							+ StrUtil.sqlstr("%" + getWhat() + "%");
				} else if (getCond().equals("shortMobile")) {
					sql += " and msn like "
							+ StrUtil.sqlstr("%" + getWhat() + "%");
				}
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
					addresser.put("shortMobile", StrUtil.getNullStr(addr
							.getMSN()));
					addresser.put("operationPhone", addr.getOperationPhone());
					addresser.put("email", addr.getEmail());
					addresser.put("job", addr.getJob()); // 职务
					addressers.put(addresser);
				}
				result.put("addressers", addressers);
				json.put("result", result);

				JSONArray groupTypes = new JSONArray();
				Leaf lf = new Leaf();
				if (groupType.equals("") || groupType.equals("0"))
					groupType = Leaf.USER_NAME_PUBLIC;
				lf = lf.getLeaf(groupType);

				json.put("dirName", lf.getName());

				ir = lf.getChildren().iterator();
				while (ir.hasNext()) {
					lf = (Leaf) ir.next();

					JSONObject groupType = new JSONObject();
					groupType.put("grouptype", lf.getCode());
					groupType.put("name", lf.getName());
					groupType.put("type", String.valueOf(type));
					groupType.put("mode", "show");
					groupTypes.put(groupType);
				}

				result.put("groupTypes", groupTypes);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setResult(json.toString());

		}

		return "SUCCESS";
	}
}
