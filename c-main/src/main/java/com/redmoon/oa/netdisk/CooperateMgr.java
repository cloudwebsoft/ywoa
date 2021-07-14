package com.redmoon.oa.netdisk;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.clouddisk.bean.CooperateBean;
import com.redmoon.clouddisk.db.CooperateDb;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Vector;

public class CooperateMgr {
	transient Logger logger = Logger.getLogger(CooperateMgr.class.getName());
	
	public boolean isRefused(String userName,String sharedName ,String dirVisual){
		boolean re = false;
		String sql ="select id,is_refused from netdisk_cooperate where user_name ="+StrUtil.sqlstr(userName)
					+" and visual_path="+StrUtil.sqlstr(dirVisual)
					+" and share_user="+StrUtil.sqlstr(sharedName);
		JdbcTemplate jt = new JdbcTemplate();
		try{
		ResultIterator ri = jt.executeQuery(sql);
		ResultRecord record = null;
		while(ri.hasNext()){
			re = true;
			record = (ResultRecord) ri.next();
			long id = record.getLong("id");
			int is_refused = record.getInt("is_refused");
			if(is_refused != 2){
				CooperateBean cb = new CooperateBean();
				cb.setId(id);
				cb.setIsRefused(0);
				CooperateDb cd = new CooperateDb(cb);
				cd.update();
			}
		}
		}catch (Exception e) {
			logger.error("queryHistoryLog:" + e.getMessage());
		}
		return re;
	}
	
	/**
	 * 取消文件夹的分享
	 * @param code
	 */
	public boolean noRefused(String code){
		boolean re = false;
		Leaf lf = new Leaf(code);
		if(!lf.isShared()){
			return re;
		}
		lf.setShared(false);
		re = lf.update();
		String sql = "delete from netdisk_cooperate where dir_code ="+StrUtil.sqlstr(code);
		JdbcTemplate jt = new JdbcTemplate() ;
		try{
			if(re){
				re = jt.executeUpdate(sql) >=1? true : false;
			}
			if(re){
				Vector v = new Vector();
				v = lf.getAllChild(v, lf);
				v.add(lf);
				Iterator it = v.iterator();
				while(it.hasNext()){
					Leaf leaf = (Leaf) it.next();
					sql = "delete from netdisk_dir_priv where dir_code ="+StrUtil.sqlstr(leaf.getCode());
					re = jt.executeUpdate(sql) >= 1 ? true : false;
				}
			}
		}catch(Exception e){
			logger.error("noRefused:" + e.getMessage());
		}
		return re;
	}	
	
	
	
	/**取消个别用户的文件夹的分享
	 * @param dirCode
	 * @param name
	 * @return
	 */
	public boolean noRefusedPersons(String code, String name){
		boolean re = false;
		Leaf lf = new Leaf(code);
		lf.setShared(false);
		re = lf.update();
		String sql = "delete from netdisk_cooperate where user_name = " + StrUtil.sqlstr(name) + " dir_code ="+StrUtil.sqlstr(code);
		JdbcTemplate jt = new JdbcTemplate() ;
		try{
			if(re){
				re = jt.executeUpdate(sql) >=1? true : false;
			}
			if(re){
				Vector v = new Vector();
				v = lf.getAllChild(v, lf);
				v.add(lf);
				Iterator it = v.iterator();
				while(it.hasNext()){
					Leaf leaf = (Leaf) it.next();
					sql = "delete from netdisk_dir_priv where name = "+ StrUtil.sqlstr(name)+" dir_code ="+StrUtil.sqlstr(leaf.getCode());
					re = jt.executeUpdate(sql) >= 1 ? true : false;
				}
			}
		}catch(Exception e){
			logger.error("noRefusedPersons:" + e.getMessage());
		}
		return re;
	}	
	//通过dirCode name获取权限列表的ID值，从而拼出clouddisk_network_neighborhood_list.jsp的进退按钮
	//在clouddisk_network_neighborhood_dir.jsp页面调用 并传参
	public int getPrivId(String dirCode , String name ){
		int i = 0;
		String sql ="select id from netdisk_cooperate where dir_code ="+StrUtil.sqlstr(dirCode)
        +" and user_name = "+StrUtil.sqlstr(name) + " and is_refused = 2";
        JdbcTemplate jt = new JdbcTemplate();
        try {
        	ResultIterator ri = jt.executeQuery(sql);
        	ResultRecord rr = null;
        	if(ri.hasNext()){
        		rr = (ResultRecord) ri.next();
        		i = rr.getInt(1);
        	}
        }catch (Exception e) {
        	logger.error("getPrivId:" + e.getMessage());
		}
		return i;
	}
	
	/**
	 * 分享文件list页面的类型分类方法
	 */
	public Vector getShareAttByType(String code,String exts){
		Leaf leaf = new Leaf(code);
		Vector vec = new Vector();
		Vector v = new Vector();
		try {
			vec = leaf.getAllChild(vec, leaf);
		} catch (ErrMsgException e) {
			logger.error("getShareAttByType: " + e.getMessage());
		}
		vec.add(leaf);
		Iterator it = vec.iterator();
		JdbcTemplate jt = new JdbcTemplate();
		
		while(it.hasNext()){
			Leaf lf = (Leaf) it.next();
			int docId = lf.getDocId();
			String sql = "select id from netdisk_document_attach where doc_id = "+ docId + " and is_current=1 and is_deleted=0 and ext in"
						+ (exts.equals("") ? "" : "(" + exts + ")") + " order by version_date desc" ;
			try{
				ResultIterator ri = jt.executeQuery(sql);
				ri = jt.executeQuery(sql);
				ResultRecord rr = null;
				while(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					Attachment att = new Attachment(rr.getInt(1));
					v.add(att);
				}
			} catch (Exception e1){
				logger.error("getShareAttByType: " + e1.getMessage());
			}
		}
		return v;
	}
	
	/**
	 * 网络硬盘文件list页面的搜索方法（文件版）
	 */
	public Vector getShareAttBySearch(String code,String text_content){
		Leaf leaf = new Leaf(code);
		Vector vec = new Vector();
		Vector v = new Vector();
		try {
			vec = leaf.getAllChild(vec, leaf);
		} catch (ErrMsgException e) {
			logger.error("getShareAttByType: " + e.getMessage());
		}
		vec.add(leaf);
		Iterator it = vec.iterator();
		JdbcTemplate jt = new JdbcTemplate();
		
		while(it.hasNext()){
			Leaf lf = (Leaf) it.next();
			String sql = "select id from netdisk_document_attach WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")
				+" and doc_id = " + lf.getDocId() + " and is_current=1 and is_deleted=0 order by version_date desc";
			try{
				ResultIterator ri = jt.executeQuery(sql);
				ri = jt.executeQuery(sql);
				ResultRecord rr = null;
				while(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					Attachment att = new Attachment(rr.getInt(1));
					v.add(att);
				}
			} catch (Exception e1){
				logger.error("getShareAttByType: " + e1.getMessage());
			}
		}
		return v;
	}
	
	/**
	 * 网络硬盘文件list页面的搜索方法（文件夹版）
	 */
	public Vector getShareDirBySearch(String code,String text_content){
		Leaf leaf = new Leaf(code);
		Vector vec = new Vector();
		Vector v = new Vector();
		try {
			vec = leaf.getAllChild(vec, leaf);
		} catch (ErrMsgException e) {
			logger.error("getShareAttByType: " + e.getMessage());
		}
		Iterator it = vec.iterator();
		JdbcTemplate jt = new JdbcTemplate();
		
		while(it.hasNext()){
			Leaf lf = (Leaf) it.next();
			String sql = "select code from netdisk_directory WHERE name like "+StrUtil.sqlstr("%" + text_content + "%")
			+" and doc_id = " + lf.getDocId() + " and isDeleted=0 order by add_date desc";
			try{
				ResultIterator ri = jt.executeQuery(sql);
				ri = jt.executeQuery(sql);
				ResultRecord rr = null;
				while(ri.hasNext()){
					rr = (ResultRecord)ri.next();
					lf = new Leaf(rr.getString(1));
					v.add(lf);
				}
			} catch (Exception e1){
				logger.error("getShareAttByType: " + e1.getMessage());
			}
		}
		return v;
	}
	
	/**
	 * 刷新页面时判断该用户是否存在未接受的分享文件，用alert弹出提醒
	 */
	public String isSharedDir(String userName){
		JdbcTemplate jt = new JdbcTemplate();
		String info = "";
		String sql = "select dir_code from netdisk_cooperate where user_name = "
			+ StrUtil.sqlstr(userName)+" and is_refused = 0";
		try{
			ResultIterator ri = jt.executeQuery(sql);
			ri = jt.executeQuery(sql);
			ResultRecord rr = null;
			if(ri.hasNext()){
				rr = (ResultRecord)ri.next();
				Leaf lf = new Leaf(rr.getString(1));
				com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb(lf.getRootCode());
				String realName = ud.getRealName();
				if("".equals(info)){
					info = lf.getName()+",";
					info += realName+",";
					info += rr.getString(1);
				}
			}
		}catch(Exception e){
			logger.error("isSharedDir: " + e.getMessage());
		}
		return info;
	}
	
	/**
	 * 刷新页面时判断该用户是否存在未接受的分享文件，并且点击  "同意" 之后的操作
	 */
	public boolean agreeShared(String userName , String code){
		JdbcTemplate jt = new JdbcTemplate();
		Leaf lf = new Leaf(code);;
		Vector v = new Vector();
		
		boolean re = false;
		String sql = "update netdisk_cooperate set is_refused = 2 where dir_code = "
			+StrUtil.sqlstr(code) +" and user_name = "+ StrUtil.sqlstr(userName);
		try{
			re = jt.executeUpdate(sql)>=1 ? true : false;
			v = lf.getAllChild(v, lf);
			v.add(lf);
			Iterator it = v.iterator();
			while (it.hasNext()){
				Leaf clf = (Leaf)it.next();
				sql = "insert into netdisk_dir_priv (dir_code,see,append,del,priv_modify,name,type,examine) values ("+StrUtil.sqlstr(clf.getCode())+",1,1,1,1,"+StrUtil.sqlstr(userName)+",1,1)";
				re = jt.executeUpdate(sql) >=1 ? true : false;
			}
			RMCache.getInstance().clear();
		}catch(Exception e){
			logger.error("agreeShared: " + e.getMessage());
		}
		
		return re;
	}
	
	/**
	 * 刷新页面时判断该用户是否存在未接受的分享文件，并且点击 "拒绝" 之后的操作
	 */
	public boolean refusedShared(String userName , String code){
		JdbcTemplate jt = new JdbcTemplate();
		boolean re = false;
		String sql = "update netdisk_cooperate set is_refused = 1 where dir_code = "
			+StrUtil.sqlstr(code) +" and user_name = "+ StrUtil.sqlstr(userName);
		try{
			re = jt.executeUpdate(sql)>=1 ? true : false;
		}catch(Exception e){
			logger.error("agreeShared: " + e.getMessage());
		}
		return re;
	}
	
	/**
	 * 在文件夹移动或者添加时 检测当前文件夹的父文件夹是否有被其他用户访问的权限（即 父文件夹是否是分享中的文件）
	 * 在 移动文件 或 添加新文件夹 时使用
	 */
	public void isParentFolderRefused(String code){
		JdbcTemplate jt = new JdbcTemplate();
		Leaf lf = new Leaf(code);
		boolean re = false;
		String sql = "select code from netdisk_directory where code = "+StrUtil.sqlstr(lf.getParentCode())+" and isShared = 1";
		try{
			ResultIterator ri = jt.executeQuery(sql);
			if(ri.hasNext()){
				sql = "select name from netdisk_dir_priv where dir_code = "+StrUtil.sqlstr(lf.getParentCode());
				ri = jt.executeQuery(sql);
				ResultRecord rr = null;
				while(ri.hasNext()){
					rr = (ResultRecord) ri.next();
					String users = rr.getString(1);
					Vector v = new Vector();
					v = lf.getAllChild(v, lf);
					v.add(lf);
					Iterator it = v.iterator();
					while(it.hasNext()){
						lf = (Leaf)it.next();
						sql = "insert into netdisk_dir_priv (dir_code,see,append,del,priv_modify,name,type,examine) values ("+StrUtil.sqlstr(lf.getCode())+",1,1,1,1,"+StrUtil.sqlstr(users)+",1,1)";
						jt.executeUpdate(sql);
					}
				}
			}
		}catch(Exception e){
			logger.error("agreeShared: " + e.getMessage());
		}
	}
	
	
}
