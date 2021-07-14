package com.redmoon.oa.netdisk;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

public class NetDiskMyShared {
	public static final int isShared = 1; 
	/**
	 * 获得当前用户所有我的分享
	 * @param request
	 * @param username
	 * @return
	 */
	public Vector<Leaf> queryMySharedFile(String username){
		String sql = "SELECT code FROM netdisk_directory n where isShared = ? and root_code = ?";
		Vector<Leaf> vector = new Vector<Leaf>();
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql,new Object[]{isShared,username});
			ResultRecord record  = null;
			while(ri.hasNext()){
				record = (ResultRecord)ri.next();
				String code = record.getString("code");
				Leaf leaf = new Leaf();
				vector.addElement(leaf.getLeaf(code));
			}
			return vector;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	return vector;
	}
	/**
	 * 返回根节点的root
	 * @param username
	 * @return
	 */
	public HashMap<String,Integer> queryRootCode(String username){
		Vector<Leaf> vector = queryMySharedFile(username);
		HashMap<String,Integer> hashMap = new HashMap<String, Integer>();
		Iterator<Leaf> it = vector.iterator();
		while (it.hasNext()) {
			Leaf leaf = it.next();
			hashMap.put(leaf.getCode(),1);
		}
		return hashMap;
	}
	/**
	 * 获得所有文件夹中的附件
	 * @param docId
	 * @return
	 */
	public Vector<Attachment> queryChildAttachByDocId(String docId,String orderBy,String sort){
		if(orderBy.equals("")){
			orderBy = "uploadDate";
		}
		if(sort.equals("")){
			sort = "desc";
		}
		String sql = "SELECT id FROM netdisk_document_attach WHERE doc_id = ? and is_current=1 and is_deleted=0";
		sql += " ORDER BY "+orderBy+" "+sort;
		Vector<Attachment> attachVector = new Vector<Attachment>();
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql,new Object[]{docId});
			ResultRecord record = null;
			while(ri.hasNext()){
				record = (ResultRecord)ri.next();
				int id = record.getInt("id");
				attachVector.addElement(new Attachment(id));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return attachVector;
		
	}
}
