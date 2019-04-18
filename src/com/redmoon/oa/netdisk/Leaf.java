package com.redmoon.oa.netdisk;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.*;
import com.redmoon.oa.Config;
import com.redmoon.oa.person.UserDb;

import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.*;
import org.apache.tools.ant.taskdefs.Copyfile;

public class Leaf implements Serializable, ITagSupport {
	public final static String ROOTNAME = "我的文档";

	transient RMCache rmCache = RMCache.getInstance();
	String connname = "";
	transient Logger logger = Logger.getLogger(Leaf.class.getName());

	int docId;

	public static final int TYPE_DOCUMENT = 1; // 每个目录实质上为CMS的一篇文章

	public static String CODE_ROOT = "root";
	public static String CODE_DOCUMENT = "document"; // 公文夹----虚拟目录

	private String code = "", name = "", description = "",
			parent_code = PARENT_CODE_NONE, root_code = "", add_date = "";
	private int orders = 1, layer = 1, child_count = 0, islocked = 0;
	final String LOAD = "select code,name,description,parent_code,root_code,orders,layer,child_count,add_date,islocked,type,isHome,doc_id,template_id,pluginCode,isShared,isDeleted,deleted_date from netdisk_directory where code=?";
	boolean isHome = false;
	final String dirCache = "NETDISKDIR";

	public static String PARENT_CODE_NONE = "-1";

	public String get(String field) {
		if (field.equals("code"))
			return getCode();
		else if (field.equals("name"))
			return getName();
		else if (field.equals("desc"))
			return getDescription();
		else if (field.equals("parent_code"))
			return getParentCode();
		else if (field.equals("root_code"))
			return getRootCode();
		else if (field.equals("layer"))
			return "" + getLayer();
		else
			return "";
	}

	public Leaf() {
		connname = Global.getDefaultDB();
		if (connname.equals(""))
			logger.info("Directory:默认数据库名不能为空");
	}

	public Leaf(String code) {
		removeAllFromCache();// 刷新缓存
		connname = Global.getDefaultDB();
		if (connname.equals(""))
			logger.info("Directory:默认数据库名不能为空");
		this.code = code;
		loadFromDb();
	}

	public void renew() {
		if (logger == null) {
			logger = Logger.getLogger(Leaf.class.getName());
		}
		if (rmCache == null) {
			rmCache = RMCache.getInstance();
		}
	}

	public void loadFromDb() {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(LOAD);
			ps.setString(1, code);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				this.code = rs.getString(1);
				name = rs.getString(2);
				description = rs.getString(3);
				parent_code = rs.getString(4);
				root_code = rs.getString(5);
				orders = rs.getInt(6);
				layer = rs.getInt(7);
				child_count = rs.getInt(8);
				add_date = rs.getString(9);
				if (add_date.length() >= 19)
					add_date = add_date.substring(0, 19);
				islocked = rs.getInt(10);
				type = rs.getInt(11);
				isHome = rs.getInt(12) > 0 ? true : false;
				docId = rs.getInt(13);
				templateId = rs.getInt(14);
				pluginCode = rs.getString(15);
				shared = rs.getInt(16) == 1 ? true : false;
				deleted = rs.getInt(17) == 1 ? true : false;
				deletedDate = StrUtil.getNullStr(rs.getString(18));
				if (deletedDate.length() >= 16)
					deletedDate = deletedDate.substring(0, 16);
				loaded = true;
			}
		} catch (SQLException e) {
			logger.error("loadFromDb: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}

	/**
	 * 根据某个文件夹doc_id获取它的用户文件夹名称
	 * 
	 * @param
	 * @return
	 */
	public int getCurrentDocId(String userName) {
		int docId = 0;
		Conn conn = new Conn(connname);
		String sql = "select doc_id from netdisk_directory where code ="
				+ StrUtil.sqlstr(userName) + ";";
		ResultSet rs = null;
		try {
			rs = conn.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					docId = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			logger.error("currentCode: " + e.getMessage());
		}
		return docId;
	}

	/**
	 * 根据某个节点的code获取它的完整路径
	 * 
	 * @param dirCode
	 * @return
	 */
	public String getFullPath(String dirCode) {
		String fullPath = "";
		Leaf lf = new Leaf(dirCode);
		if (lf.getParentCode().equals("-1")) {
			fullPath = lf.getCode();
		} else {
			fullPath = lf.getName();
			while (!(lf.getParentCode().equals("-1"))) {
				dirCode = lf.getParentCode();
				lf = new Leaf(dirCode);
				if (lf.getCode().equals(lf.getRootCode())) {
					fullPath = lf.getCode() + "/" + fullPath;
				} else {
					fullPath = lf.getName() + "/" + fullPath;
				}
			}
		}
		return fullPath;
	}
		
	//移动文件

	public boolean moveFile(int attId, String dirCode, String dirName) {
		boolean re = false;
		Attachment att = new Attachment(attId);
		String oldPath = att.getVisualPath();
		long oldDocId = att.getDocId();
		int oldOrders = att.getOrders();

		Leaf lf = new Leaf(dirCode);
		docId = lf.getDocId();

		// 拷贝当前文件至新文件夹
		// 在数据库中新建数据，再将物理文件拷贝至新文件夹
		// 所谓拷贝就是在一个新的visualPath中建一个与原有数据一样的新数据，与原数据docId，visualPath不同
		String newPath = lf.getFullPath(dirCode);

		att.setVisualPath(newPath);
		att.setDocId(docId);

		// 获取新的orders
		Document doc = new Document();
		doc = doc.getDocument((int) docId);
		DocContent dc = doc.getDocContent(1);
		int orders = dc.getAttachmentMaxOrders() + 1;
		att.setOrders(orders);

		re = att.create();

		Config config = new Config();
		String filePath = Global.getRealPath() + config.get("file_netdisk")
				+ "/";
		File newFile = new File(filePath + newPath);
		if (!newFile.exists()) {
			newFile.mkdirs();
		}
		if (re) {
			re = FileUtil.CopyFile(
					filePath + oldPath + "/" + att.getDiskName(), filePath
							+ newPath + "/" + att.getDiskName());

			// 拷贝后需要修改文件的修改时间为该文件移动前的版本时间
			if (re) {
				File file = new File(filePath + newPath + "/"
						+ att.getDiskName());
				if (att.getVersionDate() == null) {
					Date vdate = new Date();
					file.setLastModified(vdate.getTime());
				}
			}
		}

		// 逻辑删除原文件夹下的该文件
		att = att.getAttachment(attId);
		re = att.delAttLogic();

		if (re) {
			Conn conn = new Conn(connname);
			PreparedStatement pstmt = null;
			try {
				String sql = "update netdisk_document_attach set orders=orders-1 where doc_id=? and page_num=? and orders>?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setLong(1, oldDocId);
				pstmt.setInt(2, 1);
				pstmt.setInt(3, oldOrders);
				re = conn.executePreUpdate() >= 1 ? true : false;
				doc = doc.getDocument((int) oldDocId);
				dc = doc.getDocContent(1);
				int maxOrders = dc.getAttachmentMaxOrders();
				if (maxOrders == oldOrders) {
					return true;
				}

			} catch (SQLException e) {
				logger.error("moveFile: " + e.getMessage());
			} finally {
				if (conn != null) {
					conn.close();
				}
			}
		}

		return re;
	}
	
	//移动文件夹
	public boolean moveFolder(String dirCode, String proposeCode) {
		Leaf leaf = new Leaf(dirCode);
		Leaf lf = new Leaf(proposeCode);
		boolean re = false;
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String oldPath = Global.getRealPath() + cfg.get("file_netdisk") +"/"+leaf.getFullPath(dirCode);
		String proposePath = Global.getRealPath() + cfg.get("file_netdisk") +"/"+lf.getFullPath(proposeCode)+"/"+leaf.getName();
		String sql = "update netdisk_directory set layer = "+ (lf.getLayer()+1) +" , parent_code = "+ StrUtil.sqlstr(proposeCode) +" where code = "+StrUtil.sqlstr(dirCode);
		RMConn conn = new RMConn(connname);
		try {
			re = conn.executeUpdate(sql) >=1 ? true : false;
			try {
				if (re) {
					removeFromCache(code);
					// logger.info("cache is removed " + code);
					// DirListCacheMgr更新
					LeafChildrenCacheMgr.remove(parent_code);
				}
			} catch (Exception e) {
				logger.error("update: " + e.getMessage());
			}
			if (re) {
				removeFromCache(code);
			}
		} catch (Exception e) {
			logger.error("update: " + e.getMessage());
		}
		try{
			String newPath = lf.getFullPath(dirCode);
			sql = "update netdisk_document_attach set visualpath = " + StrUtil.sqlstr(newPath) + " where doc_id = "+ leaf.getDocId();
			re = conn.executeUpdate(sql)>=0?true : false;
			
			//物理文件夹移动
			if(re){
				File f = new File(oldPath);
				File file = new File(proposePath);
				if(f.exists()){
					FileUtil.del(proposePath);
					UtilTools.copyDir(f, file);
					if(file.exists()){
						FileUtil.del(oldPath);
					}
				}
			}
			Vector v = leaf.getChildren();
			Iterator ir = v.iterator();
			int layers = lf.getLayer();
			while (ir.hasNext()) {
				Leaf childlf = (Leaf) ir.next();
				sql = "update netdisk_directory set layer = "+ (layers+2) +" where code = "+StrUtil.sqlstr(childlf.getCode());
				re = conn.executeUpdate(sql)>=0? true : false;
				sql = "update netdisk_document_attach set visualpath = " + StrUtil.sqlstr(childlf.getFullPath(childlf.getCode())) + " where doc_id = "+ childlf.getDocId();
				re = conn.executeUpdate(sql)>= 0? true : false;
				layers++;
				v = childlf.getChildren();
				ir = v.iterator();
			}
		}catch (Exception e) {
			logger.error("update: " + e.getMessage());
		}
		leaf.removeAllFromCache();// 刷新缓存
		return re;
	}
	

	public String getAddDate() {
		return add_date;
	}

	public int getDocID() {
		return docId;
	}

	public void setDocID(int d) {
		this.docId = d;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setRootCode(String c) {
		this.root_code = c;
	}

	public void setType(int t) {
		this.type = t;
	}

	public void setName(String n) {
		this.name = n;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public int getOrders() {
		return orders;
	}

	public boolean getIsHome() {
		return isHome;
	}

	public void setParentCode(String p) {
		this.parent_code = p;
	}

	public String getParentCode() {
		return this.parent_code;
	}

	public void setIsHome(boolean b) {
		this.isHome = b;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public String getRootCode() {
		return root_code;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public String getDescription() {
		return description;
	}

	public int getType() {
		return type;
	}

	public int getTemplateId() {
		return templateId;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public String getPluginCode() {
		return pluginCode;
	}

	public boolean isShared() {
		return shared;
	}

	public int getChildCount() {
		return child_count;
	}

	public Vector listSharedDirOfUser(String shareUser) {
		Vector v = new Vector();
		String sql = "select code from netdisk_directory where root_code=? and isShared=1 and isDeleted = 0 order by name";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Conn conn = new Conn(connname);
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, shareUser);
			rs = conn.executePreQuery();
			if (rs != null) {
				while (rs.next()) {
					String c = rs.getString(1);
					// logger.info("child=" + c);
					v.addElement(getLeaf(c));
				}
			}
		} catch (SQLException e) {
			logger.error("listSharedDirOfUser: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}

			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return v;
	}

	/**
	 * Search页面搜索文件夹
	 * 
	 * @return
	 */
	public Vector getSearchChildren(String theSql) {
		Vector v = new Vector();
		String sql = theSql;
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			rs = conn.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					String c = rs.getString(1);
					// logger.info("child=" + c);
					v.addElement(getLeaf(c));
				}
			}
		} catch (SQLException e) {
			logger.error("getChildren: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return v;
	}

	public Vector getChildren() {
		Vector v = new Vector();
		String sql = "select code from netdisk_directory where parent_code=? and isDeleted = 0 order by orders desc";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Conn conn = new Conn(connname);
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, code);
			rs = conn.executePreQuery();
			if (rs != null) {
				while (rs.next()) {
					String c = rs.getString(1);
					if (!c.trim().equals("")) {
						v.addElement(getLeaf(c));
					} else {
						Leaf leaf = getLeaf(c);
						String dirCode = Leaf.getAutoCode();
						int doc_id = leaf.getDocId();
						boolean flag = updateDirCodeByDocId(dirCode, doc_id);
						if( flag ){
							Document doc = new Document(doc_id);
							doc.setClass1(dirCode);
							flag = doc.update();
							if(flag){
								v.addElement(getLeaf(dirCode));
							}
						}
					}
					// logger.info("child=" + c);

				}
			}
		} catch (SQLException e) {
			logger.error("getChildren: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return v;
	}

	public Vector getDelChildren() {
		Vector v = new Vector();
		String sql = "select code from netdisk_directory where parent_code=? and isDeleted = 1 order by orders desc";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Conn conn = new Conn(connname);
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, code);
			rs = conn.executePreQuery();
			if (rs != null) {
				while (rs.next()) {
					String c = rs.getString(1);
					// logger.info("child=" + c);
					v.addElement(getLeaf(c));
				}
			}
		} catch (SQLException e) {
			logger.error("getChildren: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return v;
	}
	
	
	public Vector getDelChildren2() {
		Vector v = new Vector();
		String sql = "select code from netdisk_directory where parent_code=? and isDeleted <> 0 order by orders desc";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Conn conn = new Conn(connname);
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, code);
			rs = conn.executePreQuery();
			if (rs != null) {
				while (rs.next()) {
					String c = rs.getString(1);
					// logger.info("child=" + c);
					v.addElement(getLeaf(c));
				}
			}
		} catch (SQLException e) {
			logger.error("getChildren: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return v;
	}
	
	public Vector getDelChildren3() {
		Vector v = new Vector();
		String sql = "select code from netdisk_directory where parent_code=? and isDeleted = 2 order by orders desc";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Conn conn = new Conn(connname);
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, code);
			rs = conn.executePreQuery();
			if (rs != null) {
				while (rs.next()) {
					String c = rs.getString(1);
					// logger.info("child=" + c);
					v.addElement(getLeaf(c));
				}
			}
		} catch (SQLException e) {
			logger.error("getChildren: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return v;
	}

	/**
	 * 取出回收站code结点的所有孩子结点
	 * 
	 * @param code
	 *            String
	 * @return ResultIterator
	 * @throws ErrMsgException
	 */
	public Vector getRecyclerChildren() {
		Vector v = new Vector();
		String sql = "select code from netdisk_directory where root_code=? and isDeleted = 1 order by deleted_date desc";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Conn conn = new Conn(connname);
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, code);
			rs = conn.executePreQuery();
			if (rs != null) {
				while (rs.next()) {
					String c = rs.getString(1);
					// logger.info("child=" + c);
					v.addElement(getLeaf(c));
				}
			}
		} catch (SQLException e) {
			logger.error("getChildren: " + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				rs = null;
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				pstmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return v;
	}

	/**
	 * 取出code结点的所有孩子结点
	 * 
	 * @param code
	 *            String
	 * @return ResultIterator
	 * @throws ErrMsgException
	 */
	public Vector getAllChild(Vector vt, Leaf leaf) throws ErrMsgException {
		Vector children = leaf.getChildren();
		if (children.isEmpty())
			return children;
		if (vt == null) {
			vt = new Vector();
		}
		vt.addAll(children);
		Iterator ir = children.iterator();
		while (ir.hasNext()) {
			Leaf lf = (Leaf) ir.next();
			getAllChild(vt, lf);
		}
		// return children;
		return vt;
	}

	/**
	 * 取出code结点的所有已删除的孩子结点
	 * 
	 * @param code
	 *            String
	 * @return ResultIterator
	 * @throws ErrMsgException
	 */
	public Vector getAllDelChild(Vector vt, Leaf leaf) throws ErrMsgException {
		Vector children = leaf.getDelChildren();
		if (children.isEmpty())
			return children;
		vt.addAll(children);
		Iterator ir = children.iterator();
		while (ir.hasNext()) {
			Leaf lf = (Leaf) ir.next();
			getAllDelChild(vt, lf);
		}
		// return children;
		return vt;
	}
	
	public Vector getAllDelChild2(Vector vt, Leaf leaf) throws ErrMsgException {
		Vector children = leaf.getDelChildren2();
		if (children.isEmpty())
			return children;
		vt.addAll(children);
		Iterator ir = children.iterator();
		while (ir.hasNext()) {
			Leaf lf = (Leaf) ir.next();
			getAllDelChild2(vt, lf);
		}
		// return children;
		return vt;
	}
	
	public Vector getAllDelChild3(Vector vt, Leaf leaf) throws ErrMsgException {
		Vector children = leaf.getDelChildren3();
		if (children.isEmpty())
			return children;
		vt.addAll(children);
		Iterator ir = children.iterator();
		while (ir.hasNext()) {
			Leaf lf = (Leaf) ir.next();
			getAllDelChild3(vt, lf);
		}
		// return children;
		return vt;
	}

	public String toString() {
		return "netdisk is " + code;
	}

	public boolean updateDirCodeByDocId(String dirCode,int doc_id){
		boolean flag = false;
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("update netdisk_directory set code = ")
				 .append(StrUtil.sqlstr(dirCode)).append(" where doc_id = ").append(doc_id);
		JdbcTemplate jt = null; 
		jt = new JdbcTemplate();
		try {
			int i = jt.executeUpdate(sqlBuffer.toString());
			if( i >= 0){
				flag = true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("updateDirCode="+e.getMessage());
		}finally{
			try {
				RMCache.getInstance().clear();
			} catch (CacheException e) {
				// TODO Auto-generated catch block
				logger.error("updateDirCode clearCache="+e.getMessage());
			}
		}
		return flag;
		
	}

	public synchronized boolean update() {
		String sql = "update netdisk_directory set name="
				+ StrUtil.sqlstr(name) + ",description="
				+ StrUtil.sqlstr(description) + ",type=" + type + ",isHome="
				+ (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id="
				+ templateId + ",orders=" + orders + ",layer=" + layer
				+ ",child_count=" + child_count + ",pluginCode="
				+ StrUtil.sqlstr(pluginCode) + ",isShared=" + (shared ? 1 : 0)
				+ " where code=" + StrUtil.sqlstr(code);
		// logger.info(sql);
		RMConn conn = new RMConn(connname);
		int r = 0;
		try {
			r = conn.executeUpdate(sql);
			try {
				if (r == 1) {
					removeFromCache(code);
					// logger.info("cache is removed " + code);
					// DirListCacheMgr更新
					LeafChildrenCacheMgr.remove(parent_code);
				}
			} catch (Exception e) {
				logger.error("update: " + e.getMessage());
			}
		} catch (SQLException e) {
			logger.error("update: " + e.getMessage());
		}
		boolean re = r >= 1 ? true : false;
		if (re) {
			removeFromCache(code);
		}
		return re;
	}

	/**
	 * 更改了分类
	 * 
	 * @param newDirCode
	 *            String
	 * @return boolean
	 */
	public synchronized boolean update(String newParentCode)
			throws ErrMsgException {
		if (newParentCode.equals(parent_code))
			return false;
		if (newParentCode.equals(code))
			throw new ErrMsgException("不能将本节点设为父节点！");

		// 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
		Leaf lfparent = getLeaf(newParentCode);
		int oldorders = orders;
		int neworders = lfparent.getChildCount() + 1;
		int parentLayer = lfparent.getLayer();
		String sql = "update netdisk_directory set name="
				+ StrUtil.sqlstr(name) + ",description="
				+ StrUtil.sqlstr(description) + ",type=" + type + ",isHome="
				+ (isHome ? "1" : "0") + ",doc_id=" + docId + ",template_id="
				+ templateId + ",parent_code=" + StrUtil.sqlstr(newParentCode)
				+ ",orders=" + neworders + ",layer=" + (parentLayer + 1)
				+ " where code=" + StrUtil.sqlstr(code);

		String oldParentCode = parent_code;
		parent_code = newParentCode;
		RMConn conn = new RMConn(connname);

		int r = 0;
		try {
			r = conn.executeUpdate(sql);
			try {
				if (r == 1) {
					removeFromCache(code);
					removeFromCache(newParentCode);
					removeFromCache(oldParentCode);
					// DirListCacheMgr更新
					LeafChildrenCacheMgr.remove(oldParentCode);
					LeafChildrenCacheMgr.remove(newParentCode);

					// 更新原来父结点中，位于本leaf之后的orders
					sql = "select code from netdisk_directory where parent_code="
							+ StrUtil.sqlstr(oldParentCode)
							+ " and orders>"
							+ oldorders;
					ResultIterator ri = conn.executeQuery(sql);
					while (ri.hasNext()) {
						ResultRecord rr = (ResultRecord) ri.next();
						Leaf clf = getLeaf(rr.getString(1));
						clf.setOrders(clf.getOrders() - 1);
						clf.update();
					}

					// 更新其所有子结点的layer
					Vector vt = new Vector();
					getAllChild(vt, this);
					int childcount = vt.size();
					Iterator ir = vt.iterator();
					while (ir.hasNext()) {
						Leaf childlf = (Leaf) ir.next();
						int layer = parentLayer + 1 + 1;
						String pcode = childlf.getParentCode();
						while (!pcode.equals(code)) {
							layer++;
							Leaf lfp = getLeaf(pcode);
							pcode = lfp.getParentCode();
						}

						childlf.setLayer(layer);
						childlf.update();
					}

					// 将其原来的父结点的孩子数-1
					Leaf oldParentLeaf = getLeaf(oldParentCode);
					oldParentLeaf
							.setChildCount(oldParentLeaf.getChildCount() - 1);
					oldParentLeaf.update();

					// 将其新父结点的孩子数 + 1
					Leaf newParentLeaf = getLeaf(newParentCode);
					newParentLeaf
							.setChildCount(newParentLeaf.getChildCount() + 1);
					newParentLeaf.update();
				}
			} catch (Exception e) {
				logger.error("update: " + e.getMessage());
			}
		} catch (SQLException e) {
			logger.error("update: " + e.getMessage());
		}
		boolean re = r >= 1 ? true : false;
		if (re) {
			removeFromCache(code);
		}
		return re;
	}

	/**
	 * 为用户创建根目录
	 * 
	 * @param userName
	 *            String
	 * @return boolean
	 */
	public boolean initRootOfUser(String userName) throws ErrMsgException {
		Leaf leaf = new Leaf();
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		leaf.setName(cfg.getBooleanProperty("isCloudDiskRoot") ? "我的云盘"
				: Leaf.ROOTNAME);
		leaf.setCode(userName); // 使用用户名作为节点的编码，以保证根节点的唯一性
		leaf.setParentCode(PARENT_CODE_NONE);
		leaf.setDescription("根结点");
		leaf.setRootCode(userName);
		leaf.setType(type);
		leaf.setOrders(1);
		leaf.setLayer(1);
		leaf.setChildCount(0);
		leaf.setPluginCode(pluginCode);
		boolean flag = leaf.create();
		return flag;
	}

	public boolean create() throws ErrMsgException {
		String insertsql = "insert into netdisk_directory (code,name,parent_code,description,orders,root_code,child_count,layer,type,add_date) values (?,?,?,?,?,?,?,?,?,?)";

		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(insertsql);
			ps.setString(1, code);
			ps.setString(2, name);
			ps.setString(3, parent_code);
			ps.setString(4, description);
			ps.setInt(5, orders);
			ps.setString(6, root_code);
			ps.setInt(7, 0);
			ps.setInt(8, layer);
			ps.setInt(9, type);
			ps.setTimestamp(10, new Timestamp(new java.util.Date().getTime()));
			re = conn.executePreUpdate() >= 1 ? true : false;

			// 加入默认权限 everyone
			// LeafPriv lp = new LeafPriv();
			// lp.add(code);
		} catch (SQLException e) {
			logger.error("create: " + e.getMessage());
			return false;
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public String getFilePath() {
		// 取得文件虚拟路径
		String parentcode = getParentCode();
		Leaf plf = new Leaf();
		String filePath = "";
		if (!parentcode.equals("-1")) {
			// 非根目录取节点名称
			filePath = getName();
			Leaf lf = new Leaf(getCode());
			while (!parentcode.equals(lf.getRootCode())) {
				plf = plf.getLeaf(parentcode);
				if (plf == null)
					break;
				parentcode = plf.getParentCode();
				filePath = plf.getName() + "/" + filePath;
			}
			filePath = lf.getRootCode() + "/" + filePath;
		} else {
			// 根目录取节点编码
			filePath = getCode();
		}
		return filePath;
	}

	public boolean rename(String newName) throws ErrMsgException, IOException {
		// 根目录不作处理
		String oldName = "";
		oldName = name;
		if (code.equals(getRootCode())) {
			throw new ErrMsgException("根目录无法重命名！");
		}

		if (parent_code.equals(root_code) && name.equals("我的云盘")) {
			throw new ErrMsgException("云盘文件夹无法重命名！");
		}
		setName(newName);
		boolean flag = update();
		if (flag) {
			Document doc = new Document(docId);
			doc.setTitle(name);
			doc.setClass1(code);
			flag = doc.update();

			if (flag) {
				flag = changeVisualPath();
				if (flag) {

					Leaf leaf = new Leaf(code);
					leaf.setName(oldName);
					String subOldRoot = leaf.getFilePath();
					try {
						JdbcTemplate jt = new JdbcTemplate();
						String sql = "select isDeleted from netdisk_directory where name = " + StrUtil.sqlstr(leaf.getName())+" and parent_code = "
						+StrUtil.sqlstr(leaf.getParentCode())+" and root_code = "+ StrUtil.sqlstr(leaf.getRootCode());
						ResultIterator  ri = jt.executeQuery(sql);
						int k = 0;
						while(ri.hasNext()){
							ResultRecord rr = (ResultRecord) ri.next();
							int i = rr.getInt(1);
							if(i == 2 || i == 1){
								k = 1;
							}
						}
						if (k == 0){
							com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
							String file_netdisk = cfg.get("file_netdisk");

							FileUtil.del(Global.getRealPath() + file_netdisk + "/"
									+ subOldRoot);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("deleteFileByService Failure:"
								+ e.getMessage());
						throw new IOException();
					}
				}
			}
		}
		return flag;
	}

	/**
	 * @return
	 */
	public boolean isNameExists() {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		String sql = "select code from netdisk_directory where parent_code = "
				+ StrUtil.sqlstr(parent_code) + " and name="
				+ StrUtil.sqlstr(name);
		try {
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				code = rs.getString(1);
				return true;
			}
		} catch (Exception e) {
			logger.error("getCode: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return false;
	}

	/**
	 * @Description: 需要先遍历出当前目录下的所有文件夹 再修改每一个文件夹下的文件路径
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean changeVisualPath() {
		boolean re = true;

		Leaf leaf = new Leaf(code);
		Vector vec = new Vector();
		try {
			vec = getAllChild(vec, leaf);
		} catch (ErrMsgException e) {
			logger.error("changeVisualPath: " + e.getMessage());
		}
		vec.add(leaf);
		Iterator it = vec.iterator();
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String file_netdisk = cfg.get("file_netdisk");

		while (it.hasNext()) {
			Leaf lf = (Leaf) it.next();
			ResultSet rs = null;
			Conn conn = new Conn(connname);
			String fullPath = getFullPath(lf.getCode());
			String sql = "select id from netdisk_document_attach where doc_id = "
					+ lf.getDocId();
			try {
				rs = conn.executeQuery(sql);
				while(rs != null && rs.next()) {
						Attachment att = new Attachment(rs.getInt(1));
						File oldFile = new File(Global.getRealPath() + file_netdisk
								+ "/" + att.getVisualPath() + "/"
								+ att.getDiskName());
						File newFile = new File(Global.getRealPath() + file_netdisk
								+ "/" + fullPath + "/" + att.getDiskName());
						newFile.getParentFile().mkdirs();
						oldFile.renameTo(newFile);
				}
				
				rs = conn.executeQuery(sql);
				if(rs != null && rs.next()){
					sql = "update netdisk_document_attach set visualpath = "
						+ StrUtil.sqlstr(fullPath) + " where doc_id = "
						+ lf.getDocId();
					re = conn.executeUpdate(sql) >= 1 ? true : false;
				}	
				String newClearPath = Global.getRealPath() + file_netdisk
						+ "/" + leaf.getFilePath();
				File newFloder = new File(newClearPath);
				if (!newFloder.exists()) {
					newFloder.mkdir();
				}
				

			} catch (SQLException e) {
				logger.error("changeVisualPath: " + e.getMessage());
			}
		}
		return re;
	}

	/**
	 * 加入新用户
	 * 
	 * @param childleaf
	 *            Leaf
	 * @throws ErrMsgException
	 * @throws ErrMsgException
	 */
	public void AddUser(String dir_code) throws ErrMsgException {
		com.redmoon.clouddisk.Config rcfg = com.redmoon.clouddisk.Config
				.getInstance();
		boolean isRoot = rcfg.getBooleanProperty("isCloudDiskRoot");
		if (isRoot) {
			try {
				initRootOfUser(dir_code);
			} catch (ErrMsgException e) {
				logger.error("AddUser: " + e.getMessage());
				throw new ErrMsgException(e.getMessage());
			}
		} else {
			try {
				initRootOfUser(dir_code);
				Directory dir = new Directory();
				dir.setName("我的云盘");
				dir.setParentCode(dir_code);
				dir.setType(TYPE_DOCUMENT);
				dir.AddChild();
			} catch (ErrMsgException e) {
				logger.error("AddUser: " + e.getMessage());
				throw new ErrMsgException(e.getMessage());
			}
		}
	}

	/**
	 * 加入本节点的子节点
	 * 
	 * @param childleaf
	 *            Leaf
	 * @return boolean
	 * @throws ErrMsgException
	 */
	public boolean AddChild(Leaf childleaf) throws ErrMsgException {
		// 检查文件名与同级目录是否有重复，如果有，则自动加尾数(2)，如果仍重复，则自动再加
		Vector v = getChildren();
		String chName = childleaf.getName();
		String newChName = chName;
		Iterator ir;
		int k = 1;
		while (true) {
			ir = v.iterator();
			boolean isFound = false;
			while (ir.hasNext()) {
				Leaf lf = (Leaf) ir.next();
				if (lf.getName().equals(newChName)) {
					isFound = true;
					newChName = chName + "(" + k + ")";
					k++;
				}
			}
			if (!isFound)
				break;
		}
		childleaf.setName(newChName);

		// 计算得出插入结点的orders
		int childorders = child_count + 1;

		String updatesql = "";
		String insertsql = "insert into netdisk_directory (code,name,parent_code,description,orders,root_code,child_count,layer,type,add_date) values (?,?,?,?,?,?,?,?,?,?)";

		if (!SecurityUtil.isValidSql(insertsql))
			throw new ErrMsgException("请勿输入非法字符如;号等！");
		Conn conn = new Conn(connname);
		try {
			// 更改根结点的信息
			updatesql = "Update netdisk_directory set child_count=child_count+1"
					+ " where code=" + StrUtil.sqlstr(code);
			conn.beginTrans();
			PreparedStatement ps = conn.prepareStatement(insertsql);
			ps.setString(1, childleaf.getCode());
			ps.setString(2, childleaf.getName());
			ps.setString(3, code);
			ps.setString(4, childleaf.getDescription());
			ps.setInt(5, childorders);
			ps.setString(6, root_code);
			ps.setInt(7, 0);
			ps.setInt(8, layer + 1);
			ps.setInt(9, childleaf.getType());
			ps.setTimestamp(10, new Timestamp(new java.util.Date().getTime()));
			conn.executePreUpdate();
			ps.close();

			conn.executeUpdate(updatesql);
			conn.commit();

			removeFromCache(code);

			// 同步添加文件夹
			Leaf lf = getLeaf(childleaf.getCode());
			String filePath = lf.getFilePath();
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			filePath = Global.getRealPath() + cfg.get("file_netdisk") + "/"
					+ filePath;

			// System.out.println(getClass() + filePath +
			// " cfg.get(\"file_netdisk\")=" + cfg.get("file_netdisk"));

			File f = new File(filePath);
			if (!f.isDirectory()) {
				f.mkdirs();
			}

			// 同步添加文件，并初始化leaf中的docId
			Document doc = new Document();
			doc.getIDOrCreateByCode(childleaf.getCode(), root_code);

			// 加入默认权限 everyone
			// LeafPriv lp = new LeafPriv();
			// lp.add(childleaf.getCode());
		} catch (SQLException e) {
			conn.rollback();
			logger.error("AddChild: " + e.getMessage());
			return false;
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}

		return true;
	}

	/**
	 * 每个节点有两个Cache，一是本身，另一个是用于存储其孩子结点的cache
	 * 
	 * @param code
	 *            String
	 */
	public void removeFromCache(String code) {
		try {
			rmCache.remove(code, dirCache);
			LeafChildrenCacheMgr.remove(code);
		} catch (Exception e) {
			logger.error("removeFromCache: " + e.getMessage());
		}
	}

	public void removeAllFromCache() {
		try {
			rmCache.invalidateGroup(dirCache);
			LeafChildrenCacheMgr.removeAll();
		} catch (Exception e) {
			logger.error("removeAllFromCache: " + e.getMessage());
		}
	}

	public Leaf getLeaf(String code) {
		Leaf leaf = null;
		try {
			leaf = (Leaf) rmCache.getFromGroup(code, dirCache);
		} catch (Exception e) {
			logger.error("getLeaf: " + e.getMessage());
		}
		if (leaf == null) {
			leaf = new Leaf(code);
			if (leaf != null) {
				if (!leaf.isLoaded())
					leaf = null;
				else {
					try {
						rmCache.putInGroup(code, dirCache, leaf);
					} catch (Exception e) {
						logger.error("getLeaf: " + e.getMessage());
					}
				}
			}
		} else {
			leaf.renew();
		}

		return leaf;
	}

	public boolean delsingle(Leaf leaf) {
		RMConn rmconn = new RMConn(connname);
		try {
			String sql = "delete from netdisk_directory where code="
					+ StrUtil.sqlstr(leaf.getCode());
			boolean r = rmconn.executeUpdate(sql) >= 1 ? true : false;
			sql = "update netdisk_directory set orders=orders-1 where parent_code="
					+ StrUtil.sqlstr(leaf.getParentCode())
					+ " and orders>"
					+ leaf.getOrders();
			rmconn.executeUpdate(sql);
			sql = "update netdisk_directory set child_count=child_count-1 where code="
					+ StrUtil.sqlstr(leaf.getParentCode());
			rmconn.executeUpdate(sql);

			// removeFromCache(leaf.getCode());
			// removeFromCache(leaf.getParentCode());
			removeAllFromCache();

			// 删除该目录下的所有权限
			LeafPriv lp = new LeafPriv(leaf.getCode());
			lp.delPrivsOfDir();

			// 删除该目录下的所有文章
			// logger.info("delsingle: leaf name=" + leaf.getName());
			Document doc = new Document();
			try {
				doc.delDocumentByDirCode(leaf.getCode());
			} catch (ErrMsgException e) {
				logger.error("delsingle:" + e.getMessage());
			}

			// 删除物理目录
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			String file_netdisk = cfg.get("file_netdisk");
			File f = new File(Global.getRealPath() + file_netdisk + "/"
					+ leaf.getFilePath());
			f.delete();
			try {
				// 如果目录中带有文件，则必须如此才能删除物理目录，或者用System.gc();
				Thread.sleep(50);
			} catch (Exception e) {
			}

		} catch (SQLException e) {
			logger.error("delsingle: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean delsingle2(Leaf leaf) {
		RMConn rmconn = new RMConn(connname);
		try {
			String sql = "update netdisk_directory set isDeleted = 3 where code="
					+ StrUtil.sqlstr(leaf.getCode());
			boolean r = rmconn.executeUpdate(sql) >= 1 ? true : false;
			sql = "update netdisk_directory set orders=orders-1 where parent_code="
					+ StrUtil.sqlstr(leaf.getParentCode())
					+ " and orders>"
					+ leaf.getOrders();
			//rmconn.executeUpdate(sql);
			sql = "update netdisk_directory set child_count=child_count-1 where code="
					+ StrUtil.sqlstr(leaf.getParentCode());
			//rmconn.executeUpdate(sql);

			// removeFromCache(leaf.getCode());
			// removeFromCache(leaf.getParentCode());
			removeAllFromCache();

			// 删除该目录下的所有权限
			LeafPriv lp = new LeafPriv(leaf.getCode());
			lp.delPrivsOfDir();

			// 删除该目录下的所有文章
			// logger.info("delsingle: leaf name=" + leaf.getName());
			Document doc = new Document();
			try {
				doc.delDocumentByDirCode(leaf.getCode());
			} catch (ErrMsgException e) {
				logger.error("delsingle:" + e.getMessage());
			}

			// 删除物理目录
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			String file_netdisk = cfg.get("file_netdisk");
			File f = new File(Global.getRealPath() + file_netdisk + "/"
					+ leaf.getFilePath());
			f.delete();
			try {
				// 如果目录中带有文件，则必须如此才能删除物理目录，或者用System.gc();
				Thread.sleep(50);
			} catch (Exception e) {
			}

		} catch (SQLException e) {
			logger.error("delsingle: " + e.getMessage());
			return false;
		}
		return true;
	}

	public void del(Leaf leaf) {
		delsingle(leaf);
		Iterator children = leaf.getChildren().iterator();
		while (children.hasNext()) {
			Leaf lf = (Leaf) children.next();
			del(lf);
		}

	}
	
	public void del2(Leaf leaf) {
		delsingle2(leaf);
		Iterator children = leaf.getChildren().iterator();
		while (children.hasNext()) {
			Leaf lf = (Leaf) children.next();
			del2(lf);
		}

	}

	/**
	 * @Description: 文件夹删除
	 * @param leaf
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean delFolders(Leaf leaf) {
		boolean re = false;
		Conn conn = new Conn(connname);
		Vector vec = new Vector();
		String deletedDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");

		try {
			String sql = "update netdisk_directory set isDeleted = 1,deleted_date="
					+ SQLFilter.getDateStr(deletedDate, "yyyy-MM-dd HH:mm:ss")
					+ " where code = " + StrUtil.sqlstr(leaf.getCode());
			re = conn.executeUpdate(sql) >= 0 ? true : false; 
			
			vec = getAllChild(vec, leaf);
			Iterator it = vec.iterator();
			while (it.hasNext()){
				Leaf l = (Leaf) it.next();
				sql = "update netdisk_directory set isDeleted = 2,deleted_date="
					+ SQLFilter.getDateStr(deletedDate, "yyyy-MM-dd HH:mm:ss")
					+ " where code = " + StrUtil.sqlstr(l.getCode());
				if(re){
					re = conn.executeUpdate(sql) >= 0 ? true : false;
				}
			}
			
			vec.add(leaf);
			it = vec.iterator();
			while (it.hasNext()) {
				Leaf lf = (Leaf) it.next();
				int docId = lf.getDocId();

				// String deletedDate = DateUtil.format(new Date(),
				// "yyyy-MM-dd HH:mm:ss");
				// String sql =
				// "update netdisk_directory set isDeleted = 1,deleted_date="
				// + SQLFilter.getDateStr(deletedDate, "yyyy-MM-dd HH:mm:ss")
				// + " where code = " + StrUtil.sqlstr(dirCode);

				// re = conn.executeUpdate(sql) >= 1 ? true : false;
				if (re) {
					sql = "update netdisk_document_attach set is_deleted = 2,is_current = 0 ,delete_date="
							+ SQLFilter.getDateStr(deletedDate,
									"yyyy-MM-dd HH:mm:ss")
							+ " where doc_id = "+ docId
							+ " and is_deleted = 0";
					re = conn.executeUpdate(sql) >= 0 ? true : false;
					
					sql = "select id from netdisk_document_attach where is_deleted = 2 and is_current = 0 and doc_id = "+ docId;
					ResultSet rs = conn.executeQuery(sql);
					while (rs != null && rs.next()) {
						int delId = rs.getInt(1);
						Attachment att = new Attachment();
						att.delAttLogic(delId);

						// 更新其后的附件的orders
						if (re) {
							PreparedStatement pstmt = null;
							conn = new Conn(connname);
							try {
								sql = "update netdisk_document_attach set orders=orders-1 where doc_id=? and page_num=? and orders>?";
								pstmt = conn.prepareStatement(sql);
								pstmt.setInt(1, docId);
								pstmt.setInt(2, 1);
								pstmt.setInt(3, orders);
								conn.executePreUpdate();

								// 更新用户的磁盘已用空间
								UserDb ud = new UserDb();
								Document doc = new Document();
								doc = doc.getDocument(docId);
								ud = ud.getUserDb(doc.getNick());
								ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() - att.getSize());
								re = ud.save();
							} catch (Exception e) {
								logger.error("delLogical:" + e.getMessage());
							} finally {
								if (conn != null) {
									conn.close();
									conn = null;
								}
							}
						}
					}
					
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			logger.error("delFolder: " + e.getMessage());
		}

		leaf.removeAllFromCache(); // 刷新缓存，否则会出现deleted_date时间显示不变的情况
		return re;
	}

	/**
	 * 获取某文件夹下最新的子文件夹
	 * 
	 * @param rootCode
	 * @return
	 */
	public String getNewestCode() {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		String dirCode = "";
		String sql = "select code from netdisk_directory where root_code = "
				+ StrUtil.sqlstr(root_code)
				+ (Global.db.equalsIgnoreCase(Global.DB_ORACLE) ? " and rownum<=1"
						: "")
				+ " order by add_date desc"
				+ (Global.db.equalsIgnoreCase(Global.DB_MYSQL) ? " limit 1"
						: "");
		try {
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				dirCode = rs.getString(1);
			}
		} catch (Exception e) {
			logger.error("getCode: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return dirCode;
	}

	/**
	 * 根据名称和父节点获取当前节点的code
	 * 
	 * @return 返回是否存在
	 */
	public boolean getCodeByName() {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		String sql = "select code from netdisk_directory where parent_code = "
				+ StrUtil.sqlstr(parent_code) + " and name="
				+ StrUtil.sqlstr(name);
		try {
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				code = rs.getString(1);
				return true;
			}
		} catch (Exception e) {
			logger.error("getCode: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return false;
	}

	/**
	 * 判断文件夹恢复之前是否重名，如果重名，不许恢复
	 * 
	 * @param dirCode
	 * @return
	 */
	public boolean isSameName() {
		boolean re = false;
		ResultSet rs = null;
		String sql = "select code from netdisk_directory where isDeleted = 0 and name = "
				+ StrUtil.sqlstr(name)
				+ " and parent_code = "
				+ StrUtil.sqlstr(parent_code);
		Conn conn = new Conn(connname);
		try {
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				re = true;
			} else {
				return re;
			}
		} catch (SQLException e) {
			logger.error("isSameName: " + e.getMessage());
		}
		return re;
	}

	/**
	 * @Description: 文件夹恢复
	 * @param dirCode
	 * @return
	 */

	public boolean restore(String dirCode) {
		boolean re = false;
		Conn conn = new Conn(connname);
		Leaf lf = new Leaf(dirCode);
		Vector vec = new Vector();
		try {
			vec = getAllDelChild3(vec, lf);
		} catch (ErrMsgException e) {
			logger.error("delFolders: " + e.getMessage());
		}
		vec.add(lf);
		Iterator it = vec.iterator();
		String thisDate = DateUtil.format(new Date(), "yyyyMMddHHmmss");
		while (it.hasNext()) {
			Leaf leaf = (Leaf) it.next();
			String dcode = leaf.getCode();
			int ddocid = leaf.getDocId();

			ResultSet rs = null;
			String sql = "update netdisk_directory set isDeleted = 0, add_date = "+thisDate+" where code = "
					+ StrUtil.sqlstr(dcode);
			try {
				re = conn.executeUpdate(sql) >= 1 ? true : false;
				Leaf parentLeaf = new Leaf(dcode);
				while (!parentLeaf.getParentCode().equals(leaf.getRootCode())) {
					dcode = parentLeaf.getParentCode();
					sql = "update netdisk_directory set isDeleted = 0, add_date = "+thisDate+"  where code = "
							+ StrUtil.sqlstr(parentLeaf.getParentCode());
					re = conn.executeUpdate(sql) >= 1 ? true : false;
					parentLeaf = new Leaf(parentLeaf.getParentCode());
				}

				sql = "select id from netdisk_document_attach where doc_id = "
						+ ddocid + " and is_deleted = 2";
				rs = conn.executeQuery(sql);
				while (rs != null && rs.next()) {
					int theDocId = rs.getInt(1);
					Attachment att = new Attachment(theDocId);
					//String theName = att.getName();
					//String theDisName = att.getDiskName();
					//if (!theName.equals(theDisName)) {
						re = att.restore();
					//}
				}
				//sql = "update netdisk_document_attach set is_deleted = 0, is_current = 1 where doc_id = "
					//	+ ddocid + " and is_deleted = 2";
				//re = conn.executeUpdate(sql) >= 0 ? true : false;
			} catch (Exception e) {
				logger.error("restore: " + e.getMessage());
			}
		}
		removeAllFromCache();
		return re;
	}

	/**
	 * @Description: 文件夹彻底删除
	 * @param dirCode
	 * @return
	 */
	public boolean remove(String dirCode) {
		boolean re = false;
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		Vector vec = new Vector();
		Leaf leaf = new Leaf(dirCode);

		try {
			vec = getAllChild(vec, leaf);
			vec = getAllDelChild(vec, leaf);
			vec.add(leaf);
			Iterator it = vec.iterator();
			while (it.hasNext()) {
				Leaf lf = (Leaf) it.next();
				int docId = lf.getDocId();
				del2(lf);
//				String sql = "delete from netdisk_directory where code = "
//						+ StrUtil.sqlstr(lf.getCode());
//				re = conn.executeUpdate(sql) >= 1 ? true : false;
				String sql = "update netdisk_document_attach set is_Deleted = 3 where doc_id = "
						+ docId + " and is_deleted = 2";
				re = conn.executeUpdate(sql) >= 0 ? true : false;

			}
			if (re) {
				Config config = new Config();
				String filePath = Global.getRealPath()
						+ config.get("file_netdisk") + "/" + leaf.getFilePath();
				File f = new File(filePath);
				if (f.exists()) {
					FileUtil.del(filePath);
				}
			}

		} catch (Exception e) {
			logger.error("remove: " + e.getMessage());
		}
		removeAllFromCache();
		return re;
	}

	public Leaf getBrother(String direction) {
		String sql;
		RMConn rmconn = new RMConn(connname);
		Leaf bleaf = null;
		try {
			if (direction.equals("down")) {
				sql = "select code from netdisk_directory where parent_code="
						+ StrUtil.sqlstr(parent_code) + " and orders="
						+ (orders + 1);
			} else {
				sql = "select code from netdisk_directory where parent_code="
						+ StrUtil.sqlstr(parent_code) + " and orders="
						+ (orders - 1);
			}

			ResultIterator ri = rmconn.executeQuery(sql);
			if (ri != null && ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				bleaf = getLeaf(rr.getString(1));
			}
		} catch (SQLException e) {
			logger.error("getBrother: " + e.getMessage());
		}
		return bleaf;
	}

	public boolean move(String direction) {
		String sql = "";

		// 取出该结点的移动方向上的下一个兄弟结点的orders
		boolean isexist = false;

		Leaf bleaf = getBrother(direction);
		if (bleaf != null) {
			isexist = true;
		}

		// 如果移动方向上的兄弟结点存在则移动，否则不移动
		if (isexist) {
			Conn conn = new Conn(connname);
			try {
				conn.beginTrans();
				if (direction.equals("down")) {
					sql = "update netdisk_directory set orders=orders+1"
							+ " where code=" + StrUtil.sqlstr(code);
					conn.executeUpdate(sql);
					sql = "update netdisk_directory set orders=orders-1"
							+ " where code=" + StrUtil.sqlstr(bleaf.getCode());
					conn.executeUpdate(sql);
				}

				if (direction.equals("up")) {
					sql = "update netdisk_directory set orders=orders-1"
							+ " where code=" + StrUtil.sqlstr(code);
					conn.executeUpdate(sql);
					sql = "update netdisk_directory set orders=orders+1"
							+ " where code=" + StrUtil.sqlstr(bleaf.getCode());
					conn.executeUpdate(sql);
				}
				conn.commit();
				removeFromCache(code);
				removeFromCache(bleaf.getCode());
			} catch (Exception e) {
				conn.rollback();
				logger.error("move: " + e.getMessage());
				return false;
			} finally {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			}
		}

		return true;
	}
	
	/**
	 * @param name
	 * @param docId
	 *            判断数据库里是否有重名文件数据存在
	 * @return
	 */
	public boolean isExist(String name, String code) {
		String sql = "select code from netdisk_directory where parent_code=? and name=?";
		try {
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql, new Object[] {
					code, name });
			if (ri.hasNext())
				return true;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return false;
	}

	public ArrayList<String> listFilesForZip(String basePath, String zipPath) {
		ArrayList<String> list = new ArrayList<String>();
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		String sql = "select name,visualPath from netdisk_document_attach where user_name="
				+ StrUtil.sqlstr(root_code)
				+ " and (visualPath like "
				+ StrUtil.sqlstr(basePath + "/" + zipPath + "/%")
				+ " or visualPath="
				+ StrUtil.sqlstr(basePath + "/" + zipPath)
				+ ") and is_current=1 and is_deleted=0";
		try {
			rs = conn.executeQuery(sql);

			while (rs.next()) {
				String visualPath = rs.getString("visualPath");
				visualPath = visualPath.substring(basePath.length() + 1);
				if (!visualPath.endsWith("/")) {
					visualPath += "/";
				}
				visualPath += rs.getString("name");
				list.add(visualPath);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return list;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}

	public void setPluginCode(String pluginCode) {
		this.pluginCode = pluginCode;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public void setChildCount(int childCount) {
		this.child_count = childCount;
	}

	public static String getAutoCode() {
		return FileUpload.getRandName();
	}

	public int getDocId() {
		return docId;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getDeletedDate() {
		return deletedDate;
	}

	public void setDeletedDate(String deletedDate) {
		this.deletedDate = deletedDate;
	}

	private int templateId = -1;
	private boolean loaded = false;
	private String pluginCode;
	private int type = TYPE_DOCUMENT;
	private boolean shared = false;
	private boolean deleted = false;
	private String deletedDate;
}
