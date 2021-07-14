package com.redmoon.oa.exam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.address.AddressDb;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.basic.TreeSelectMgr;
import com.redmoon.oa.fileark.Directory;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RoleDb;

/**
 * @Description:考试专业展开树基础类
 * @author:sht
 * @Date: 2018-4-25下午06:24:24
 */
public class MajorView {
	
	public MajorView(){
		
	}

	TreeSelectDb rootLeaf;
	public static final String ROOT_CODE = "exam_major";
	public MajorView(TreeSelectDb rootLeaf) {
		this.rootLeaf = rootLeaf;
	}

	/**
	 * 
	 * @Description: 获取jsTree的json字符串
	 * @return
	 * @throws Exception
	 */
	public String getJsonString() throws Exception {
		TreeSelectMgr dir = new TreeSelectMgr();
		String str = "[";
		// 从根开始
		str = this.getJson(dir, "-1", str);
		str = str.substring(0, str.length() - 1);
		str += "]";
		return str;
	}

	/**
	 * 递归获得jsTree的json字符串只遍历第一个节点
	 * 
	 * @param parentCode
	 *            父节点parentCode
	 * @return str
	 */
	private String getJson(TreeSelectMgr dir, String parentCode, String str)
			throws Exception {

		int i = 0;
		int j = 0;
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
		int size = children.size();
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			if (!childlf.getRootCode().equals(rootLeaf.getCode())) {
				continue;
			}
			i++;
			if ("-1".equals(parentCode)) {
				str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
						+ childlf.getName().replaceAll("\"", "\\\\\"")
						+ "\",state:{opened:true}} ,";
			} else {
				str += "{id:\"" + childlf.getCode() + "\",parent:\""
						+ childlf.getParentCode() + "\",text:\""
						+ childlf.getName().replaceAll("\"", "\\\\\"")
						+ "\" },";
			}
			Vector childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				int size2 = childs.size();
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					j++;
					TreeSelectDb child = (TreeSelectDb) childri.next();
					str += "{id:\"" + child.getCode() + "\",parent:\""
							+ child.getParentCode() + "\",text:\""
							+ child.getName().replaceAll("\"", "\\\\\"")
							+ "\" },";
					// 还有子节点(递归调用)
					Vector ch = dir.getChildren(child.getCode());
					if (!ch.isEmpty()) {
						str = this.getJson(dir, child.getCode(), str);
					}
				}
			}
		}
		return str;
	}

	public String getJsonStringByUser(String userName) throws Exception {
		TreeSelectMgr dir = new TreeSelectMgr();
		String str = "[";
		// 从根开始
		str = this.getJsonStringByUser(dir, "-1", str, userName);
		str = str.substring(0, str.length() - 1);
		str += "]";
		return str;
	}

	public String getJsonStringByUser(TreeSelectMgr dir, String parentCode,
			String str, String userName) throws Exception {
		int i = 0;
		int j = 0;
		// 把顶层的查出来
		Vector children = dir.getChildren(parentCode);
		int size = children.size();
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			if (!childlf.getRootCode().equals(rootLeaf.getCode())) {
				continue;
			}
			i++;
			if ("-1".equals(parentCode)) {
				str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
						+ childlf.getName().replaceAll("\"", "\\\\\"")
						+ "\",state:{opened:true}} ,";

			} else {
				str += "{id:\"" + childlf.getCode() + "\",parent:\""
						+ childlf.getParentCode() + "\",text:\""
						+ childlf.getName().replaceAll("\"", "\\\\\"")
						+ "\" },";
			}
			Vector childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				int size2 = childs.size();
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					j++;
					TreeSelectDb child = (TreeSelectDb) childri.next();
					if (userName.equals("admin")) {
						str += "{id:\"" + child.getCode() + "\",parent:\""
								+ child.getParentCode() + "\",text:\""
								+ child.getName().replaceAll("\"", "\\\\\"")
								+ "\" },";
						// 还有子节点(递归调用)
						Vector ch = dir.getChildren(child.getCode());
						if (!ch.isEmpty()) {
							str = this.getJson(dir, child.getCode(), str);
						}
					} else {
						StringBuffer sb = new StringBuffer();
						sb.append("'" + userName + "'");
						UserDb user = new UserDb();
						if (userName != null){
							user = user.getUserDb(userName);
							RoleDb[] roles = user.getRoles();
							int len = roles.length;
							for (int k = 0; k < len; k++) {
								sb.append(",'" + roles[k].getCode() + "'");
							}
						}
						String sql = "select major_code from oa_exam_major_priv where can_manage = 1 and name in("
								+ sb.toString() + ")";
						JdbcTemplate jt = new JdbcTemplate();
						ResultIterator rit = jt.executeQuery(sql);
						String chiCodeString = "";
						while (rit.hasNext()) {
							ResultRecord rd = (ResultRecord) rit.next();
							chiCodeString = rd.getString("major_code");
							if (chiCodeString.equals(child.getCode())) {
								str += "{id:\""
										+ child.getCode()
										+ "\",parent:\""
										+ child.getParentCode()
										+ "\",text:\""
										+ child.getName().replaceAll("\"",
												"\\\\\"") + "\" },";
								// 还有子节点(递归调用)
								Vector ch = dir.getChildren(child.getCode());
								if (!ch.isEmpty()) {
									str = this.getJson(dir, child.getCode(),
											str);
								}
							}
						}
					}
				}
			}
		}
		return str;
	}

	public String getJsonStringByUser(TreeSelectDb tsd, String userName)
			throws Exception {
		TreeSelectMgr tsm = new TreeSelectMgr();
		String str = "[{id:\"" + tsd.getCode() + "\",parent:\"#\",text:\""
				+ tsd.getName().replaceAll("\"", "\\\\\"")
				+ "\",state:{opened:true}} ,";
		// 从根开始
		ArrayList<String> list = new ArrayList<String>();
		getJsonByUser(tsm, tsd.getCode(), userName, list);
		for (String node : list) {
			if (node.equals("exam_major")) {
				continue;
			}
			if (node.equals(tsd.getCode())) {
				continue;
			}
			TreeSelectDb childlf = new TreeSelectDb(node);
			str += "{id:\"" + childlf.getCode() + "\",parent:\""
					+ childlf.getParentCode() + "\",text:\""
					+ childlf.getName().replaceAll("\"", "\\\\\"") + "\" },";
		}
		str = str.substring(0, str.length() - 1);
		str += "]";
		return str;
	}

	public void getJsonByUser(TreeSelectMgr tsd, String parentCode,
			String userName, ArrayList<String> list) throws Exception {
		MajorPriv mp = new MajorPriv(parentCode);
		if (!parentCode.equals("exam_major")) {
			if (mp.canUserSeeWithAncestorNode(userName)) {
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
		}
		// 把顶层的查出来
		Vector children = tsd.getChildren(parentCode);
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			if (childlf.isShow()) {
				continue;
			}
			mp = new MajorPriv(childlf.getCode());
			if (mp.canUserSeeWithAncestorNode(userName)) {
				if (!list.contains(childlf.getCode())) {
					list.add(childlf.getCode());
				}
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
			if (childlf.getChildCount() > 0) {
				getJsonByUser(tsd, childlf.getCode(), userName, list);
			}
			if (list.contains(childlf.getCode())) {
				if (!list.contains(parentCode)) {
					list.add(parentCode);
				}
			}
		}
	}
	/**
	 * 
	 * @Description: 根据用户权限展示考试专业分类树
	 * @param outStr
	 * @param leaf
	 * @param rootlayer
	 * @return
	 * @throws ErrMsgException
	 * @throws SQLException 
	 */
	public StringBuffer getTreeSelectByUserAsOptions(StringBuffer outStr,
			TreeSelectDb leaf, int rootlayer,String userName,String isAllShow) throws ErrMsgException, SQLException {
		outStr.append(getTreeSelectAsOption(leaf, rootlayer,userName,isAllShow));
		TreeSelectMgr dm = new TreeSelectMgr();
		Vector children = dm.getChildren(leaf.getCode());
		int size = children.size();
		if (size == 0)
			return outStr;
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			getTreeSelectByUserAsOptions(outStr,childlf,rootlayer,userName,isAllShow);
		}
		return outStr;
	}
	
	public String getTreeSelectAsOption(TreeSelectDb leaf, int rootlayer,String userName,String isAllShow) throws SQLException, ErrMsgException {
		String outStr = "";
		String code = leaf.getCode();
		String name = leaf.getName();

		String clr = "";
		if (!leaf.getColor().equals(""))
			clr = " style='color:" + leaf.getColor() + "' ";

		int layer = leaf.getLayer();
		String blank = "";
		int d = layer - rootlayer;
		for (int i = 0; i < d; i++) {
			blank += "　";
		}
		if(isShow(code, userName,isAllShow)){
			if (leaf.getChildCount() > 0) {
				outStr += "<option value='" + code + "'" + clr + ">" + blank + "╋ "
						+ name + "</option>";
			} else {
				outStr += "<option value=\"" + code + "\"" + clr + ">" + blank
						+ "├『" + name + "』</option>";
			}
		}
		return outStr;
	}
	
	public boolean isShow(String majorCode,String userName, String isAllShow) throws SQLException, ErrMsgException{

		boolean re = false;
		String selMajorSql = "";
		if("admin".equals(userName)){
			if("1".equals(isAllShow)){
				// admin用户登陆 如果不需要全部显示 从基础数据表中取出所有的节点的layer 如果是第三层就返回false 不显示
				selMajorSql = "select layer from oa_tree_select where code = " + StrUtil.sqlstr(majorCode) + " and rootCode = 'exam_major'";
				JdbcTemplate jTemplate = new JdbcTemplate();
				ResultIterator ri= jTemplate.executeQuery(selMajorSql);
				while(ri.hasNext()){
					ResultRecord rd = (ResultRecord)ri.next();
					if(rd.getInt(1)>2){
						return false;
					}else{
						return true;
					}
				}
			}else{
				return true;
			}
		}else{
			UserDb user = new UserDb();
			user = user.getUserDb(userName);
			RoleDb [] roles =  user.getRoles();
			int len = roles.length;
			String sqlName = StrUtil.sqlstr(userName);
			for (int i = 0; i < len; i++) {
				sqlName += ","+StrUtil.sqlstr(roles[i].getCode());
			}
			// 一般用户登陆 从考试权限表中 取出该userName具有权限的code
			selMajorSql = "select major_code from oa_exam_major_priv where can_manage = 1 and name in("+sqlName+")";
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator mri = jt.executeQuery(selMajorSql);
			if(mri.size()==0){
				return re;
			}
			List list = new ArrayList();
			while(mri.hasNext()){
				ResultRecord rd = (ResultRecord)mri.next();
				if("1".equals(isAllShow)){
					TreeSelectDb tsd = new TreeSelectDb();
					tsd = tsd.getTreeSelectDb(rd.getString("major_code"));
					if(tsd.isLoaded()){
						if(tsd.getLayer()>2){
							return false;
						}
					}
				}else{
					list.add(rd.getString("major_code"));
					List list1 = new ArrayList();
					list1 = getMajorCode(rd.getString("major_code"));
					for(int i = 0 ; i < list1.size() ; i++) {
						list.add(list1.get(i));
					}
				}
			}
			for(int j = 0 ; j < list.size() ; j++) {
				 if(majorCode.equals(list.get(j))){
					 re = true;
					 break;
				 }else{
					 continue;
				 }
			}
		}
		
		return re;
	}
	/**
	 * 
	 * @Description: 根据majorCode递归法遍历找出所有的子节点majorCode返回list集合
	 * @param code
	 * @return
	 */
	public List getMajorCode(String code){
		List list = new ArrayList();
		TreeSelectDb tsd = new TreeSelectDb();
		tsd = tsd.getTreeSelectDb(code);
		list.add(tsd.getCode());
		Vector v = tsd.getChildren();
		if(v.size()!=0){
			Iterator ri = v.iterator();
			while (ri.hasNext()) {
				TreeSelectDb childlf = (TreeSelectDb) ri.next();
				list.addAll(getMajorCode(childlf.getCode()));
			}
		}
		
		return list;
	}
	
	public List getMajorCodeByUser(String userName) throws SQLException{
		List list = new ArrayList();
		UserDb user = new UserDb();
		user = user.getUserDb(userName);
		RoleDb [] roles =  user.getRoles();
		int len = roles.length;
		String sqlName = StrUtil.sqlstr(userName);
		for (int i = 0; i < len; i++) {
			sqlName += ","+StrUtil.sqlstr(roles[i].getCode());
		}
		String selMajorSql = "select major_code from oa_exam_major_priv where invigilate =1 and name in("+sqlName+")";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator mri = jt.executeQuery(selMajorSql);
		if(mri.size()==0){
			
			return null;
		}
		while(mri.hasNext()){
			ResultRecord rd = (ResultRecord)mri.next();
			list.add(rd.getString("major_code"));
			List list1 = new ArrayList();
			list1 = getMajorCode(rd.getString("major_code"));
			for(int i = 0 ; i < list1.size() ; i++) {
				list.add(list1.get(i));
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @Description: 根据专业树的Code得到以其为根节点的json字符串
	 * @param code
	 * @return
	 * @throws Exception
	 */
	public String getJsonByCode(String code) throws Exception{
		TreeSelectMgr dir = new TreeSelectMgr();
		TreeSelectDb tsd = new TreeSelectDb();
		tsd = tsd.getTreeSelectDb(code);
		// 判断该节点是否为第二级节点 如果是 直接遍历 ，如果不是 找到其所有的父级节点直至第二层父级节点
		
		String str = "[";
		// 从根开始
		str += "{id:\"" + code + "\",parent:\"#\",text:\""
		+ tsd.getName().replaceAll("\"", "\\\\\"")
		+ "\" },";
		str = this.getJsonStringByCode(dir, code, str);
		str = str.substring(0, str.length() - 1);
		str += "]";
		return str;
		
	}
	/**
	 * 
	 * @Description: 
	 * @param dir
	 * @param code
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public String getJsonStringByCode(TreeSelectMgr dir,String code, String str) throws Exception{
		int i = 0;
		int j = 0;
		// 把顶层的查出来
		Vector children = dir.getChildren(code);
		Iterator ri = children.iterator();
		while (ri.hasNext()) {
			TreeSelectDb childlf = (TreeSelectDb) ri.next();
			if (!childlf.getRootCode().equals(rootLeaf.getCode())) {
				continue;
			}
			i++;
			if ("-1".equals(code)) {
				str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
						+ childlf.getName().replaceAll("\"", "\\\\\"")
						+ "\",state:{opened:true}} ,";
			} else {
				str += "{id:\"" + childlf.getCode() + "\",parent:\""
					+ childlf.getParentCode() + "\",text:\""
					+ childlf.getName().replaceAll("\"", "\\\\\"")
					+ "\" },";
			}
			Vector childs = dir.getChildren(childlf.getCode());
			// 如果有子节点
			if (!childs.isEmpty()) {
				// 遍历它的子节点
				int size2 = childs.size();
				Iterator childri = childs.iterator();
				while (childri.hasNext()) {
					j++;
					TreeSelectDb child = (TreeSelectDb) childri.next();
					str += "{id:\"" + child.getCode() + "\",parent:\""
							+ child.getParentCode() + "\",text:\""
							+ child.getName().replaceAll("\"", "\\\\\"")
							+ "\" },";
					// 还有子节点(递归调用)
					Vector ch = dir.getChildren(child.getCode());
					if (!ch.isEmpty()) {
						str = this.getJson(dir, child.getCode(), str);
					}
				}
			}
		}
		return str;
	}
	
}
