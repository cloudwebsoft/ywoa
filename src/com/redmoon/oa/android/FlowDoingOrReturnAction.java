package com.redmoon.oa.android;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class FlowDoingOrReturnAction {
	private String skey = "";
	private String result = "";
	private String op = "";
	private String title = "";
    private String showyear = "";
    private String showmonth = "";
    
    
	public String getShowyear() {
		return showyear;
	}

	public void setShowyear(String showyear) {
		this.showyear = showyear;
	}

	public String getShowmonth() {
		return showmonth;
	}

	public void setShowmonth(String showmonth) {
		this.showmonth = showmonth;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

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
		String beginDate = "",endDate="";
		if (!getShowyear().equals("") && !getShowmonth().equals("")) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, StrUtil.toInt(showyear));
			cal.set(Calendar.MONTH, StrUtil.toInt(showmonth)-1);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			beginDate = DateUtil.format(cal, "yyyy-MM-dd");
			cal.add(Calendar.MONTH, 1);   
			endDate = DateUtil.format(cal, "yyyy-MM-dd");
		}
		String myname = privilege.getUserName(getSkey());
		MyActionDb mad = new MyActionDb();
		WorkflowDb wfd = new WorkflowDb();
		
		String sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name="
				+ StrUtil.sqlstr(myname) + " or m.proxy="
				+ StrUtil.sqlstr(myname) + ") and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<> " +WorkflowDb.STATUS_DELETED +" and (is_checked=0 or is_checked=2) and sub_my_action_id=0";
		if (getOp().equals("search")) {
			sql = "select m.id from flow_my_action m, flow f where m.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE + " and f.status<> " +WorkflowDb.STATUS_DELETED + " and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and (is_checked=0 or is_checked=2) and sub_my_action_id=0";
			if (!getTitle().equals("")) {
				sql += " and f.title like " + StrUtil.sqlstr("%" + getTitle() + "%");
			}
			if (!beginDate.equals("")) {
				sql += " and f.mydate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
			}
			if (!endDate.equals("")) {
				sql += " and f.mydate<" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
			}	
		}
		
		sql += " order by receive_date desc";
		//System.out.println("sql = " + sql);
		int curpage = getPagenum(); // 第几页
		int pagesize = getPagesize(); // 每页显示多少条

		ListResult lr;
		try {
			lr = mad.listResult(sql, curpage, pagesize);
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
			
			Leaf lf = new Leaf();
			
			JSONArray flows = new JSONArray();
			while (ir.hasNext()) {
				mad = (MyActionDb) ir.next();

				wfd = wfd.getWorkflowDb((int) mad.getFlowId());
				
				lf = lf.getLeaf(wfd.getTypeCode());
				if (lf==null) {
					lf = new Leaf();
					continue;
				}
				
				JSONObject flow = new JSONObject();
				flow.put("myActionId", String.valueOf(mad.getId()));
				flow.put("flowId", String.valueOf(mad.getFlowId()));
				flow.put("name",StringEscapeUtils.unescapeHtml(wfd.getTitle()) );
				flow.put("status", WorkflowActionDb.getStatusName(mad
						.getActionStatus()));
				flow.put("beginDate", DateUtil.format(wfd.getBeginDate(),
						"yyyy-MM-dd HH:mm:ss"));
				flow.put("type", String.valueOf(lf.getType()));
				flow.put("typeName", lf.getName());
				
				flows.put(flow);
			}
			result.put("flows", flows);
			json.put("result", result);
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

}
