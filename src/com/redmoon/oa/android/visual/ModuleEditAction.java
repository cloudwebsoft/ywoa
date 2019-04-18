package com.redmoon.oa.android.visual;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.android.Privilege;
public class ModuleEditAction {
	private String skey = "";
	private String result = "";
	private String formCode = "";
	private long id = 0;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	private HashMap<String, String> fieldsMap;

	public HashMap<String, String> getFieldsMap() {
		return fieldsMap;
	}

	public void setFieldsMap(HashMap<String, String> fieldsMap) {
		this.fieldsMap = fieldsMap;
	}

	public String getFormCode() {
		return formCode;
	}

	public void setFormCode(String formCode) {
		this.formCode = formCode;
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
		try {
			StringBuilder valSb = new StringBuilder();
			if(fieldsMap!=null && fieldsMap.size()>0){
				Set<Entry<String, String>> set=	(Set<Entry<String, String>>)fieldsMap.entrySet();
				Iterator<Entry<String, String>> it = set.iterator();
				while(it.hasNext()){
					Entry<String,String> entry = it.next();
					String key = entry.getKey();
					String value = entry.getValue();
					if(valSb!= null && valSb.toString().equals("")){
						valSb.append(key).append("=").append("'").append(value).append("'");
					}else{
						valSb.append(",").append(key).append("=").append("'").append(value).append("'");
					}
				}
				StringBuilder sqlSb = new StringBuilder();
				if(valSb!=null && !valSb.equals("") && id !=0){
					sqlSb.append("update ").append("form_table_").append(formCode).append(" set ")
					.append(valSb).append( " where id = ").append(id);
					boolean flag = updateFields(sqlSb.toString());
					if(flag){
						json.put("res", "0");
						json.put("msg", "修改成功！");
					}else{
						json.put("res", "-1");
						json.put("msg", "修改失败！");
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ModuleEditAction.class).error("JSONException updateFields=="+e.getMessage());
		} 
		setResult(json.toString());
		return "SUCCESS";
	}

	public boolean updateFields(String sql) {
		boolean result = false;
		JdbcTemplate jt = null;
		jt = new JdbcTemplate();
		try {
			int res = jt.executeUpdate(sql);
			if (res > 0) {
				result = true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(ModuleEditAction.class).error(
					"updateFields==" + e.getMessage());
		}
		return result;
	}
}
