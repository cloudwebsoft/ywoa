package com.redmoon.oa.netdisk;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.pvg.Privilege;

/**
 * 
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * ╋ 女性话题 一级目录 ├『花样年华』 二级目录 ├『花样年华』 ╋ 女性话题 二级目录 ├『花样年华』 三级目录
 * 
 * @author not attributable
 * @version 1.0
 */

public class PublicDirectory {
	String connname = "";
	Logger logger = Logger.getLogger(PublicDirectory.class.getName());

	public PublicDirectory() {
		connname = Global.getDefaultDB();
		if (connname.equals(""))
			logger.info("PublicDirectory:默认数据库名不能为空");
	}

	public boolean AddChild(HttpServletRequest request) throws ErrMsgException {
		// 取出被回复的贴子的有关信息
		int child_count = 0, orders = 1, parent_orders = 1, islocked = 0;
		String root_code = "", name = "", code = "", parent_code = "";

		name = ParamUtil.get(request, "name").trim();
		if (name == null || name.equals(""))
			throw new ErrMsgException("名称不能为空！");
//		code = ParamUtil.get(request, "code").trim();
//		if (code.equals(""))
//			throw new ErrMsgException("编码不能为空！");
		code = PublicLeaf.getAutoCode();
		parent_code = ParamUtil.get(request, "parent_code").trim();
		if (parent_code.equals(""))
			throw new ErrMsgException("父结点不能为空！");
		String description = ParamUtil.get(request, "description");
		String pluginCode = ParamUtil.get(request, "pluginCode");
		String mappingAddress = ParamUtil.get(request, "mappingAddress");
		mappingAddress = mappingAddress.replace('\\', '/');

		Privilege privilege = new Privilege();
		PublicLeafPriv lp = new PublicLeafPriv();
		lp.setDirCode(parent_code);
		if (!lp.canUserManage(privilege.getUser(request))) {
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));
		}
		
		PublicLeaf lf = new PublicLeaf();
		lf = lf.getLeaf(code);
		if (lf != null && lf.isLoaded())
			throw new ErrMsgException("已存在相同编码的节点：" + lf.getName());
		//同一节点存在 同名文件夹名
		boolean isExistDir = isExistsDir(parent_code,name);
		if(isExistDir){
			throw new ErrMsgException("已存在" + name+"节点");
		}
		lf = new PublicLeaf();
		lf.setName(name);
		lf.setCode(code);
		lf.setParentCode(parent_code);
		lf.setDescription(description);
		lf.setType(PublicLeaf.TYPE_LIST);
		lf.setPluginCode(pluginCode);
		lf.setMappingAddress(mappingAddress);

		PublicLeaf leaf = getLeaf(parent_code);
		return leaf.AddChild(lf);
	}

	public void del(HttpServletRequest request, String delcode)
			throws ErrMsgException {
		Privilege privilege = new Privilege();
		PublicLeafPriv lp = new PublicLeafPriv();
		lp.setDirCode(delcode);
		if (!lp.canUserManage(privilege.getUser(request))) {
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));
		}
		PublicLeaf lf = getLeaf(delcode);
		//删除物理文件
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String file_netdisk = Global.getRealPath()+"/"+cfg.get("file_netdisk_public");
		try {
			FileUtil.del(file_netdisk+"/"+lf.getFilePath());
			if (lf != null)
				lf.del(lf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IOException"+e.getMessage());
		}
		
	}

	public synchronized boolean update(HttpServletRequest request)
			throws ErrMsgException {
		String code = ParamUtil.get(request, "code", false);
		String name = ParamUtil.get(request, "name", false);
		String parentCode = ParamUtil.get(request, "parentCode");
		String description = ParamUtil.get(request, "description");
		boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true: false;
		if (code == null || name == null) {
			throw new ErrMsgException("code与name项必填！");
		
		}
		if(code.equals(parentCode)){
			throw new ErrMsgException("当前节点与父节点一致！");
		}
		
		String pluginCode = ParamUtil.get(request, "pluginCode");
		String mappingAddress = ParamUtil.get(request, "mappingAddress");
		mappingAddress = mappingAddress.replace('\\', '/');
		//同一节点存在 同名文件夹名
		PublicLeaf leaf = getLeaf(code);
		if(!leaf.getName().equals(name)){
			boolean isExistDir = isExistsDir(parentCode,name);
			if(isExistDir){
				throw new ErrMsgException("已存在" + name+"节点");
			}
		}
		Privilege privilege = new Privilege();
		PublicLeafPriv lp = new PublicLeafPriv();
		lp.setDirCode(code);
		if (!lp.canUserManage(privilege.getUser(request))) {
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));
		}

	
		leaf.setName(name);
		leaf.setDescription(description);
		leaf.setIsHome(isHome);
		leaf.setType(PublicLeaf.TYPE_LIST);
		leaf.setPluginCode(pluginCode);
		leaf.setMappingAddress(mappingAddress);
		boolean re = false;
		if (parentCode.equals(leaf.getParentCode()))
			re = leaf.update();
		else
			re = leaf.update(parentCode);

		return re;
	}

	public synchronized boolean move(HttpServletRequest request)
			throws ErrMsgException {
		String code = ParamUtil.get(request, "code", false);
		String direction = ParamUtil.get(request, "direction", false);
		if (code == null || direction == null) {
			throw new ErrMsgException("编码与方向项必填！");
		}

		Privilege privilege = new Privilege();
		PublicLeafPriv lp = new PublicLeafPriv();
		lp.setDirCode(code);
		if (!lp.canUserManage(privilege.getUser(request))) {
			throw new ErrMsgException(SkinUtil.LoadString(request,
					"pvg_invalid"));
		}

		PublicLeaf lf = new PublicLeaf(code);
		return lf.move(direction);
	}

	public PublicLeaf getLeaf(String code) {
		PublicLeaf leaf = new PublicLeaf();
		return leaf.getLeaf(code);
	}

	public PublicLeaf getBrother(String code, String direction)
			throws ErrMsgException {
		PublicLeaf lf = getLeaf(code);
		return lf.getBrother(direction);
	}

	public Vector getChildren(String code) throws ErrMsgException {
		PublicLeaf leaf = getLeaf(code);
		return leaf.getChildren();
	}

	/**
	 * 取得目录的子目录
	 * 
	 * @param layer
	 *            int 级数 一级或者二级
	 * @return String
	 */
	public String getSubLeaves(String parentCode, int layer) {
		if (layer > 2)
			return "";

		String str = "";
		PublicLeafChildrenCacheMgr lcc = new PublicLeafChildrenCacheMgr(
				parentCode);
		Vector v = lcc.getList();
		Iterator ir = v.iterator();
		// 进入第一级
		while (ir.hasNext()) {
			PublicLeaf lf = (PublicLeaf) ir.next();
			if (layer == 2) {
				// 进入第二级
				lcc = new PublicLeafChildrenCacheMgr(lf.getCode());
				Iterator ir2 = lcc.getList().iterator();
				while (ir2.hasNext()) {
					lf = (PublicLeaf) ir2.next();
					if (str.equals(""))
						str = StrUtil.sqlstr(lf.getCode());
					else
						str += "," + StrUtil.sqlstr(lf.getCode());
				}
			} else {
				if (str.equals(""))
					str = StrUtil.sqlstr(lf.getCode());
				else
					str += "," + StrUtil.sqlstr(lf.getCode());
			}

		}
		return str;
	}

	/**
	 * 公共共享
	 * 
	 * @param dirCode
	 * @return
	 * @throws ErrMsgException
	 */
	public JSONObject publicShareDirectoy(String dirCode,String userName)
			throws ErrMsgException {
		JSONObject obj = new JSONObject();

		Leaf leaf = new Leaf(dirCode);
		if (leaf == null || !leaf.isLoaded()) {
			obj.put("result", -1);// 文件夹不存在
		} else {
			copyDirToPublicDir(leaf);
			boolean flag = copyDirToShareDir(leaf,userName);
			if (flag) {
				obj.put("result", 1);// 文件夹分享成功
			} else {
				obj.put("result", -2);// 文件夹分享失败
			}
		}
		return obj;

	}

	/**
	 * 文件夹 拷贝
	 * 
	 * @param dirCode
	 * @return
	 */
	public void copyDirToPublicDir(Leaf leaf) {
		boolean flag = false;
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		// 被拷贝路径
		String src = Global.getRealPath() + cfg.get("file_netdisk") + "/"
				+ leaf.getParentCode() + "/" + leaf.getName();
		File sysDir = new File(src);
		

		// 拷贝路径
		String dst = Global.getRealPath() + cfg.get("file_netdisk_public")+"/"+leaf.getName();// 拷贝至根目录
		File dstDir = new File(dst);
		if(dstDir.exists()){
			dstDir.delete();
		}
		//物理文件夹拷贝  
		UtilTools.copyDir(sysDir, dstDir);// 物理文件夹的拷贝
	}

	/**
	 * 文件夹 公共共享
	 * 
	 * @param dirCode
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean copyDirToShareDir(Leaf leaf,String userName) throws ErrMsgException {
		boolean flag = true;
		Vector vt = null;
		PublicLeaf plParent = null;
		try {
			//公共共享文件夹 根目录文件夹  如果已经被分享  更新文件夹名称
			plParent = new PublicLeaf(leaf.getCode());
			if(plParent == null || !plParent.isLoaded()){
				plParent.setCode(leaf.getCode());
				plParent.setParentCode(PublicLeaf.ROOTCODE);
				plParent.setName(leaf.getName());
				plParent.setDescription(leaf.getDescription());
				plParent.setType(PublicLeaf.TYPE_LIST);
				plParent.setUserName(userName);
				plParent.setPluginCode(leaf.getPluginCode());
				PublicLeaf plRoot1 = new PublicLeaf(plParent.getParentCode());
				flag &= plRoot1.AddChild(plParent);
			}else{
				//存在公共共享文件夹  
				PublicAttachment patt = new PublicAttachment();
				//删除 公共共享 文件夹下所有附件
				patt.delOfDir(plParent.getCode());
				//公共文件夹更新名称
				plParent.setName(leaf.getName());
				flag &= plParent.update();
				
				
			}
			// 遍历文件夹下的子文件夹 以及附件 一次插入公共共享文件夹 及附件表中
			if (flag) {
				flag &= getAllAttach(leaf.getDocId(),plParent);
				vt = leaf.getAllChild(vt, leaf);
				if(vt.size()>0){
					Iterator ir = vt.iterator();
					while (ir.hasNext()) {
						Leaf leafChild = (Leaf) ir.next();
						PublicLeaf plChild = new PublicLeaf(leafChild.getCode());
						if(plChild == null || !plChild.isLoaded()){
							plChild.setCode(leafChild.getCode());
							plChild.setParentCode(leafChild.getParentCode());
							plChild.setDescription(leafChild.getDescription());
							plChild.setType(PublicLeaf.TYPE_LIST);
							plChild.setPluginCode(leafChild.getPluginCode());
							plChild.setName(leafChild.getName());
							plChild.setUserName(userName);
							PublicLeaf plRoot = new PublicLeaf(leafChild
									.getParentCode());
							flag &= plRoot.AddChild(plChild);
							
						}else{
							//存在公共共享文件夹  
							PublicAttachment patt = new PublicAttachment();
							//删除 公共共享 文件夹下所有附件
							patt.delOfDir(plChild.getCode());
							//公共文件夹更新名称
							plChild.setName(leafChild.getName());
							plChild.update(leafChild.getParentCode());
						}
						if (flag) {
							flag &= getAllAttach(leafChild.getDocId(),plChild);
						}
					}
				}
		
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block

			logger.error("publicShareDirectory:" + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		}
		return flag;
	}
	
	/**
	 * 遍历文件夹 下的所有附件
	 * @param doc_id
	 * @param pl
	 * @return
	 */
	public boolean getAllAttach(int doc_id,PublicLeaf pl){
		boolean flag = true;
		// 遍历文件夹下的附件
		DocContent docContent = new DocContent(doc_id, 1);// 获得所有docConent
		Vector vtAtt = docContent.getAttachments();
		if (vtAtt.size() > 0) {
			Iterator itAtt = vtAtt.iterator();
			while (itAtt.hasNext()) {
				Attachment attachment = (Attachment) itAtt
						.next();
				PublicAttachment publicAtt = new PublicAttachment();
				publicAtt.setAttId(attachment.getId());
				publicAtt.setDiskName(attachment.getDiskName());
				publicAtt.setName(attachment.getName());
				publicAtt.setSize(attachment.getSize());
				String fullPath = pl.getFilePath();
				publicAtt.setVisualPath(fullPath);
				publicAtt.setOrders(attachment.getOrders());
				publicAtt.setExt(attachment.getExt());
				publicAtt.setUserName(attachment.getUserName());
				publicAtt.setPublicDir(pl.getCode());
				flag &= publicAtt.create();
			}
		}
		return flag;

		
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

	/**
	 * 公共文件夹查询
	 * @param request
	 * @return
	 */
	public Iterator queryPublicDirectory(HttpServletRequest request,String dirCode){
		String op = ParamUtil.get(request, "op");
		String nameStr = ParamUtil.get(request, "select_content");
    	String select_sort = ParamUtil.get(request,"select_sort");
    	Iterator it = null;
		if(!op.trim().equals("")){
			if(op.equals("search")){
				if(select_sort.equals("select_one")){
					String sql = "SELECT code FROM  netdisk_public_directory WHERE name like "+StrUtil.sqlstr("%" + nameStr + "%");
					Vector vec = getSearchChildren(sql);
					if( vec!=null && vec.size()>0){
						it = vec.iterator();
					}
				}
			}
		}else{
			PublicLeaf publicLeaf = new PublicLeaf(dirCode);
			it = publicLeaf.getChildren().iterator();
		}
		
	    return it;
		
	}
	/**
	 * 修改附件visualPath
	 * code:公共文件夹code
	 * @return
	 * @throws ErrMsgException 
	 */
	public boolean changeVisualPath(String code) throws ErrMsgException{
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String file_netdisk = Global.getRealPath()+"/"+cfg.get("file_netdisk_public");
		PublicLeaf pl = getLeaf(code);
		Vector vec = new Vector();
		boolean flag = true;
		try {
			vec = pl.getAllChild(vec, pl);//所有子文件夹
			if(vec!=null){
				vec.add(pl);
				Iterator it = vec.iterator();
				while(it.hasNext()){
					//文件夹新名称的路径
					PublicLeaf plChild = (PublicLeaf)it.next();
					String filePath = plChild.getFilePath();
					//文件夹下所有附件
					String sql = "select id from netdisk_public_attach where public_dir="+StrUtil.sqlstr(plChild.getCode())+" order by create_date desc";
					PublicAttachment publicAtt = new PublicAttachment();
			    	Vector attVec = publicAtt.list(sql);
			    	//遍历附件
			    	if(attVec!=null && attVec.size()>0){
			    		Iterator itAtt = attVec.iterator();
			    		while(itAtt.hasNext()){
			    			PublicAttachment pa = (PublicAttachment)itAtt.next();
		    				//修改物理路径
			    			if(pa.getAttId()==0){
			    				File oldFile = new File(file_netdisk
										+ "/" + pa.getVisualPath() + "/"
										+ pa.getDiskName());
								File newFile = new File(file_netdisk
										+ "/" + filePath + "/" + pa.getDiskName());
								newFile.getParentFile().mkdirs();
								flag &= oldFile.renameTo(newFile);
								if(flag){
									pa.setVisualPath(filePath);
					    			flag &= pa.save();
								}
			    			}
		    				
			    		}
			    	}else{
			    		String newClearPath = Global.getRealPath() + file_netdisk
						+ "/" + filePath;
						File newFloder = new File(newClearPath);
						if (!newFloder.exists()) {
							newFloder.mkdir();
						}
			    	}
			    
				}
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			flag = false;
			throw new ErrMsgException("修改失败！");
			
		}
		return flag;
	}
	/**
	 * 修改公共文件夹
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 * @throws IOException
	 */
	public  boolean  modifyPublicLeaf(HttpServletRequest request) throws ErrMsgException{
		String code = ParamUtil.get(request,"code");
		boolean flag = false;
		if(code !=null && !code.trim().equals("")){
			PublicLeaf pl = new PublicLeaf(code);
			Vector vec = new Vector();
			String oldName = pl.getName();
			flag = update(request);
			if(flag){
				flag = changeVisualPath(code);
				if(flag){
					com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
					String file_netdisk = cfg.get("file_netdisk_public");
					PublicLeaf leaf = new PublicLeaf(code);
					leaf.setName(oldName);
					String subOldRoot = leaf.getFilePath();
					try {
						FileUtil.del(Global.getRealPath() + file_netdisk + "/"
								+ subOldRoot);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						logger.error("公共文件夹修改IOEXception:"+e.getMessage());
						throw new ErrMsgException("修改失败！");
					}
				}
			}
		}
		return flag;
		
	}
	/**
	 * 判断存在同名文件夹
	 * @param parent_code
	 * @param name
	 * @return
	 */
	public boolean isExistsDir(String parent_code,String name){
		boolean flag = false;
		   String sql = "select count(code) from netdisk_public_directory where parent_code="+StrUtil.sqlstr(parent_code)+" and name ="+StrUtil.sqlstr(name);
		   JdbcTemplate jt = new JdbcTemplate();
		   try {
			ResultIterator ri =jt.executeQuery(sql);
			while(ri.hasNext()){
				  ResultRecord rr = (ResultRecord) ri.next();
				  int count = rr.getInt(1);
				  if( count > 0){
					  flag = true;
				  }
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("IsExistFloder=="+e.getMessage());
		}
		   return flag;
	   }
	
	
}
