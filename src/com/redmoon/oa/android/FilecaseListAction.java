package com.redmoon.oa.android;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.fileark.DirView;
import com.redmoon.oa.fileark.Directory;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.LeafPriv;

public class FilecaseListAction {
	private String skey = "";
	private String result = "";
	private String dircode = "";
	private String op = "";
	private String cond = ""; //查询列表值
	private String what = "";

	public String getDircode() {
		return dircode;
	}
	public void setDircode(String dircode) {
		this.dircode = dircode;
	}

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
		
		String userName = privilege.getUserName(getSkey());
		
		    String unitCode = "";
			if(getDircode().equals("")){
				unitCode = privilege.getUserUnitCode(getSkey());
				setDircode(Leaf.ROOTCODE);
			}
			
			Leaf lf = new Leaf();
			lf = lf.getLeaf(getDircode());
			LeafPriv lps = new LeafPriv(getDircode());
		
			try {
				String sql = "select distinct d.id,class1,title,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level,createDate,keywords from document as d, doc_content as c";
				sql += " where d.id=c.doc_id and d.examine<>" + Document.EXAMINE_DUSTBIN;
				if(lps.canUserSee(userName) && !lps.canUserExamine(userName)){
					sql+=" and d.id not in (";
				 					sql+="select doc_id from doc_priv ";
				 					sql+="where see= 0 and (";
				 						//当个用户
				 						sql+="name="+StrUtil.sqlstr(userName);
				 						//角色
				 						sql+=" or name in(select roleCode from user_of_role where userName ="+StrUtil.sqlstr(userName)+")";
				 						//用户角色组
				 						sql +=" or name in(select code from user_group g,user_of_group ug where g.code = ug.group_code and  user_name ="+StrUtil.sqlstr(userName)+")";
				 						sql +=" or name in( select code from user_group_of_role rg,user_group g,user_of_role r   where rg.userGroupCode = g.code and r.roleCode = rg.roleCode and r.userName ="+StrUtil.sqlstr(userName)+")";
				 					sql+=")";
				 	sql +=")";

				}
				if(!lps.canUserModify(userName)){
					sql += " and examine=" + Document.EXAMINE_PASS;
				}

				if(getOp().equals("search")){
					if (cond.equals("title")) {
						sql += " and title like "+StrUtil.sqlstr("%"+what+"%");
					}
					else if (cond.equals("content")) {
						//sql = "select distinct id, class1,title,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level,createDate from document as d, doc_content as c where d.id=c.doc_id and d.examine<>" + Document.EXAMINE_DUSTBIN;
					 	sql += " and c.content like " + StrUtil.sqlstr("%" + what + "%");
					}
					else {
						sql += " and keywords like " + StrUtil.sqlstr("%" + what + "%");
					}
					
				}
				sql += " and class1=" + StrUtil.sqlstr(dircode);
				sql += " order by doc_level desc, examine asc, createDate desc";
							
				int curpage = getPagenum();   //第几页
				int pagesize = getPagesize(); //每页显示多少条
				JdbcTemplate jt = new JdbcTemplate();
				ResultIterator ri = jt.executeQuery(sql, curpage, pagesize);
				ResultRecord rr =  null;
									
				json.put("res","0");
				json.put("msg","操作成功");
				json.put("total",String.valueOf(ri.getTotal()));
				
				json.put("dirName", lf.getName());
				
				JSONObject result = new JSONObject(); 
				result.put("count",String.valueOf(pagesize));
				
				JSONArray filecases = new JSONArray(); 		
				
				while (ri.hasNext()) {
					rr = (ResultRecord)ri.next();			
					JSONObject filecase = new JSONObject();
					filecase.put("id",String.valueOf(rr.getInt("id")));
					filecase.put("title",rr.getString("title"));
					filecase.put("createdate",StrUtil.getNullStr(DateUtil.format(rr.getDate("modifiedDate"),"yyyy-MM-dd HH:mm")));
					filecases.put(filecase);
				}	
				result.put("filecases",filecases);	
				
				JSONArray childrens  = new JSONArray(); 	
				Directory dir = new Directory();
				if(lf!=null){
					HttpServletRequest request = ServletActionContext.getRequest();
					DirView dirView = new DirView(request, lf);
					ArrayList<String> list = new ArrayList<String>();
					try {
						dirView.getJsonByUser(dir, lf.getCode(), userName, list);
						if(list!= null && list.size()>0){
							for(String dirCode:list){
								Leaf leaf_c = new Leaf(dirCode);
								if(leaf_c.isLoaded()){
									if(leaf_c.getParentCode().equals(lf.getCode())){
										JSONObject children = new JSONObject();
										children.put("dircode",leaf_c.getCode());
										children.put("name",leaf_c.getName());
										childrens.put(children);
									}
								}
								
							}
							result.put("childrens",childrens);
						}
					
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logger.getLogger(FilecaseListAction.class).error(e.getMessage());
					}
					/*Vector vector = lf.getChildren();
					Iterator ir = vector.iterator();
					while(ir.hasNext()){
						Leaf lf_c = (Leaf)ir.next();
						
						LeafPriv lp = new LeafPriv(lf_c.getCode());
						if (lp.canUserSee(userName)) {						
							JSONObject children = new JSONObject();
							children.put("dircode",lf_c.getCode());
							children.put("name",lf_c.getName());
							childrens.put(children);
						}
					}	*/
				}
				
		
				json.put("result",result);		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(FilecaseListAction.class).error(e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(FilecaseListAction.class).error(e.getMessage());
		}		
		setResult(json.toString());
		return "SUCCESS";
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
}
