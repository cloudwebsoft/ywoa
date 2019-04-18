package com.redmoon.oa.dept;

import java.sql.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.dingding.enums.Enum;
import com.redmoon.dingding.service.department.DepartmentService;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.mgr.WXDeptMgr;
import org.apache.log4j.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.general.GetAllPersonAction;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sso.GetSyncXml;
import com.redmoon.oa.sso.SyncUtil;
import com.redmoon.oa.tigase.TigaseConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import rtx.RTXUtil;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 * ╋ 女性话题      一级目录
 *   ├『花样年华』  二级目录
 *   ├『花样年华』
 *   ╋ 女性话题     二级目录
 *     ├『花样年华』 三级目录
 * @author not attributable
 * @version 1.0
 */

public class DeptMgr {
    String connname = "";
    Logger logger = Logger.getLogger(DeptMgr.class.getName());

    public DeptMgr() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:默认数据库名不能为空");
    }

    /**
     * 已弃用
     * @param request
     * @return
     */
    public boolean AddRootChild(HttpServletRequest request) throws
            ErrMsgException {
        int child_count = 0, orders = 1;
        String root_code = "", name = "", code = "", parent_code = "-1";

        name = ParamUtil.get(request, "name", false);
        if (name == null)
            throw new ErrMsgException("名称不能为空！");
        code = ParamUtil.get(request, "code", false);
        if (code == null)
            throw new ErrMsgException("编码不能为空！");
        String description = ParamUtil.get(request, "description");

        root_code = code;

        String insertsql = "insert into department (code,name,parent_code,description,orders,root_code,child_count,layer) values (";
        insertsql += StrUtil.sqlstr(code) + "," + StrUtil.sqlstr(name) +
                "," + StrUtil.sqlstr(parent_code) +
                "," + StrUtil.sqlstr(description) + "," +
                orders + "," + StrUtil.sqlstr(root_code) + "," +
                child_count + ",1)";

        logger.info(insertsql);
        if (!SecurityUtil.isValidSql(insertsql))
            throw new ErrMsgException("请勿输入非法字符如;号等！");
        int r = 0;
        RMConn conn = new RMConn(connname);
        try {
            r = conn.executeUpdate(insertsql);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("请检查编码" + code + "是否重复！");
        }
        return r == 1 ? true : false;
    }

    public boolean AddChild(HttpServletRequest request) throws
            ErrMsgException {
        String name = "", code = "", parent_code = "", shortName = "";
        name = ParamUtil.get(request, "name", false);
        if (name == null)
            throw new ErrMsgException("名称不能为空！");
        if (!checkNameChar(name))
        	throw new ErrMsgException("名称中不能含有\"字符！");
        code = ParamUtil.get(request, "code").trim();
        if (code.equals(""))
            throw new ErrMsgException("编码不能为空！");
        if(code.length()>50){
        	throw new ErrMsgException("现只支持12级部门！");
        }
        if (!StrUtil.isSimpleCode(code))
            throw new ErrMsgException("编码请使用字母、数字、-或_！");

        parent_code = ParamUtil.get(request, "parent_code").trim();
        if (parent_code.equals(""))
            throw new ErrMsgException("父结点不能为空！");
        String description = ParamUtil.get(request, "description");
        int type = ParamUtil.getInt(request, "type");
        
        boolean show = ParamUtil.getInt(request, "show", 0)==1;
        
        shortName = ParamUtil.get(request, "shortName");
        if(shortName.length() > 45){
        	 throw new ErrMsgException("\"简称\"不能超过45位！");
        }
        if (!checkNameChar(shortName))
        	throw new ErrMsgException("简称中不能含有\"字符！");
        
        boolean isGroup = ParamUtil.getInt(request, "isGroup", 0)==1;
        boolean isHide = ParamUtil.getInt(request, "isHide", 0)==1;
        
        DeptDb lf = new DeptDb();
        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        lf.setShow(show);
        lf.setShortName(shortName);
        lf.setGroup(isGroup);
        lf.setHide(isHide);
        // 同步钉钉部门
        DeptDb dd = getDeptDb(parent_code);
        com.redmoon.dingding.Config dingDingCfg = com.redmoon.dingding.Config.getInstance();
        if(dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
            DepartmentService _departmentService = new DepartmentService();
            int _parentId = dd.getId();
            if (dd.getCode().equals(DeptDb.ROOTCODE)) {
                _parentId = Enum.ROOT_DEPT_ID;
            }
            int _id = _departmentService.addDept(name, _parentId, dd.getOrders());
            if (_id != -1) {
                lf.setId(_id);
            }
        }
        boolean re = dd.AddChild(lf);
        if (re) {
        	com.redmoon.oa.tigase.Config tigaseCfg = new com.redmoon.oa.tigase.Config();
        	if (tigaseCfg.getBooleanProperty("isUse")) {
        		TigaseConnection tc = new TigaseConnection();
        		tc.syncDept(lf.getCode(), new Privilege().getUser(request));
        	}
        	
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
            if (isRTXUsed) {
                RTXUtil.addDept(lf);
            }

            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
            if (ssoCfg.getBooleanProperty("isUse")) {
            	SyncUtil su = new SyncUtil();
            	su.orgSync(lf, SyncUtil.CREATE, new Privilege().getUser(request));
            }

            com.redmoon.weixin.Config weixinCfg = Config.getInstance();
            if (weixinCfg.getBooleanProperty("isUse")) {
                if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
                    WXDeptMgr _wxDpetMgr = new WXDeptMgr();
                    _wxDpetMgr.createWxDept(lf);
                }
            }

        }
        return re;
    }

    public void del(String delcode, String opUser) throws ErrMsgException {
        // 检查部门中是否含有用户，如果是则不允许删除
    	/*
        DeptUserDb du = new DeptUserDb();
        int count = du.list(delcode).size();
        if (count>0)
            throw new ErrMsgException("该部门下有" + count + "名人员，请先将人员安排至其它部门再删除！");
        */
        DeptDb lf = getDeptDb(delcode);
        lf.del(lf);
        
        com.redmoon.oa.tigase.Config tigaseCfg = new com.redmoon.oa.tigase.Config();
    	if (tigaseCfg.getBooleanProperty("isUse")) {
    		TigaseConnection tc = new TigaseConnection();
    		tc.delDept(lf.getCode(), opUser);
    	}

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
        if (isRTXUsed) {
            RTXUtil.deleteDept("" + lf.getId());
        }
        
        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        if (ssoCfg.getBooleanProperty("isUse")) {
        	SyncUtil su = new SyncUtil();
        	su.orgSync(lf, SyncUtil.DEL, opUser);
        }

        com.redmoon.weixin.Config weixinCfg = Config.getInstance();
        if (weixinCfg.getBooleanProperty("isUse")) {
            if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
                WXDeptMgr _wxDpetMgr = new WXDeptMgr();
                _wxDpetMgr.deleteWxDept(lf.getId());
            }
        }
        com.redmoon.dingding.Config dingDingCfg = com.redmoon.dingding.Config.getInstance();
        if(dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
            DepartmentService _departmentService = new DepartmentService();
            _departmentService.delDept(lf.getId());
        }
    }

    public synchronized boolean update(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String name = ParamUtil.get(request, "name", false);
        String description = ParamUtil.get(request, "description");
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;
        int type = ParamUtil.getInt(request, "type");
        if (code.equals(""))
            throw new ErrMsgException("编码不能为空！");
        if (name == null)
            throw new ErrMsgException("名称不能为空！");
        if (!checkNameChar(name))
        	throw new ErrMsgException("名称中不能含有\"字符！");
        String parentCode = ParamUtil.get(request, "parentCode");
        boolean isGroup = ParamUtil.getInt(request, "isGroup", 0)==1;
        boolean isHide = ParamUtil.getInt(request, "isHide", 0)==1;

        DeptDb leaf = getDeptDb(code);
        if (code.equals(parentCode)) {
            throw new ErrMsgException("请选择正确的父节点！");
        }
        if (!parentCode.equals(leaf.getParentCode())) {
            // 节点不能改变其父节点为其子节点
            DeptDb lf = getDeptDb(parentCode); // 取得新的父节点
            while (lf!=null && !lf.getCode().equals(DeptDb.ROOTCODE)) {
                // 从parentCode节点往上遍历，如果找到leaf.getParentCode()则证明不合法
                String pCode = lf.getParentCode();
                if (pCode.equals(leaf.getCode()))
                    throw new ErrMsgException("不能将其子节点更改为父节点");
                lf = getDeptDb(pCode);
            }
        }
        
        boolean show = ParamUtil.getInt(request, "show", 0)==1;        
        String shortName = ParamUtil.get(request, "shortName");
        if(shortName.length() > 45){
        	throw new ErrMsgException("\"简称\"不能超过45位！");
        }
        if (!checkNameChar(shortName))
        	throw new ErrMsgException("简称中不能含有\"字符！");
        leaf.setName(name);
        leaf.setDescription(description);
        leaf.setIsHome(isHome);
        leaf.setType(type);
        leaf.setShow(show);
        leaf.setShortName(shortName);
        leaf.setGroup(isGroup);
        leaf.setHide(isHide);
        boolean re = false;
        if (parentCode.equals(leaf.getParentCode())) {
            logger.info("update:name=" + name);
            re = leaf.save();
        }
        else
            re = leaf.save(parentCode);
        if (re) {
        	com.redmoon.oa.tigase.Config tigaseCfg = new com.redmoon.oa.tigase.Config();
        	if (tigaseCfg.getBooleanProperty("isUse")) {
        		TigaseConnection tc = new TigaseConnection();
        		tc.syncDept(leaf.getCode(), new Privilege().getUser(request));
        	}
        	
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
            if (isRTXUsed) {
                RTXUtil.setDept(leaf);
            }
            
            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
            if (ssoCfg.getBooleanProperty("isUse")) {
            	SyncUtil su = new SyncUtil();
            	su.orgSync(leaf, SyncUtil.MODIFY, new Privilege().getUser(request));
            }

            com.redmoon.weixin.Config weixinCfg = Config.getInstance();
            if (weixinCfg.getBooleanProperty("isUse")) {
                if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
                    WXDeptMgr _wxDpetMgr = new WXDeptMgr();
                    _wxDpetMgr.updateWxDept(leaf);
                }
            }
            com.redmoon.dingding.Config dingDingCfg = com.redmoon.dingding.Config.getInstance();
            if(dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
                DepartmentService _departmentService = new DepartmentService();
                _departmentService.updateDept(code);
            }
        }
        return re;
    }

    private boolean checkNameChar(String name) {
    	int len = name.length();
        for(int i = 0; i < len; i++){
        	char ch = name.charAt(i);
        	if(ch == 34){
        		return false;
        	}
        }
		return true;
	}

	public synchronized boolean move(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String direction = ParamUtil.get(request, "direction", false);
        if (code == null || direction == null) {
            throw new ErrMsgException("编码与方向项必填！");
        }

        DeptDb dd = getDeptDb(code);
        boolean re = dd.move(direction);
        if (re) {
        	DeptDb leaf = new DeptDb();
        	leaf = leaf.getDeptDb(code);
        	
        	com.redmoon.oa.tigase.Config cfg = new com.redmoon.oa.tigase.Config();
        	TigaseConnection tc = new TigaseConnection();
            if (cfg.getBooleanProperty("isUse")) {
            	tc.syncDept(code, new Privilege().getUser(request));
            	tc.syncDept(dd.getBcode(), new Privilege().getUser(request));
            }
            com.redmoon.dingding.Config dingDingCfg = com.redmoon.dingding.Config.getInstance();
            if(dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
                DepartmentService _departmentService = new DepartmentService();
                _departmentService.updateDept(code);
            }
            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
            if (ssoCfg.getBooleanProperty("isUse")) {
            	SyncUtil su = new SyncUtil();
            	if (direction.equals("up")) {
            		su.orgSync(leaf, SyncUtil.MOVE_UP, new Privilege().getUser(request));
            	}
            	else {
            		su.orgSync(leaf, SyncUtil.MOVE_DOWN, new Privilege().getUser(request));            		
            	}
            }          	
        }
        return re;
    }

    public DeptDb getDeptDb(String code) {
        DeptDb dd = new DeptDb();
        return dd.getDeptDb(code);
    }

    public DeptDb getBrother(String code, String direction) throws
            ErrMsgException {
        DeptDb dd = getDeptDb(code);
        return dd.getBrother(direction);
    }

    public Vector getChildren(String code) throws ErrMsgException {
        DeptDb dd = getDeptDb(code);
        return dd.getChildren();
    }


    /**
     * 修复因为BUG或者误操作致树形结构被破坏的问题
     */
    public void repairLeaf(DeptDb lf) {
        Vector children = lf.getChildren();
        // 重置孩子节点数
        lf.setChildCount(children.size());
        Iterator ir = children.iterator();
        int orders = 1;
        while (ir.hasNext()) {
            DeptDb lfch = (DeptDb)ir.next();
            // 重置孩子节点的排列顺序
            lfch.setOrders(orders);
            // System.out.println(getClass() + " leaf name=" + lfch.getName() + " orders=" + orders);

            lfch.save();
            orders ++;
        }
        // 重置层数
        int layer = 2;
        String parentCode = lf.getParentCode();
        if (lf.getCode().equals(DeptDb.ROOTCODE)) {
            layer = 1;
        }
        else {
            if (parentCode.equals(DeptDb.ROOTCODE))
                layer = 2;
            else {
                while (!parentCode.equals(DeptDb.ROOTCODE)) {
                    // System.out.println(getClass() + "leaf parentCode=" + parentCode);
                    DeptDb parentLeaf = getDeptDb(parentCode);
                    if (parentLeaf == null || !parentLeaf.isLoaded())
                        break;
                    else {
                        parentCode = parentLeaf.getParentCode();
                    }
                    layer++;
                }
            }
        }
        lf.setLayer(layer);
        lf.save();
    }

    // 修复根结点为leaf的树
    public void repairTree(DeptDb leaf) throws Exception {
        // System.out.println(getClass() + "leaf name=" + leaf.getName());
        repairLeaf(leaf);
        Vector children = getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            DeptDb childlf = (DeptDb) ri.next();
            repairTree(childlf);
        }
        // 刷新缓存
        DeptCache dc = new DeptCache();
        dc.removeAllFromCache();
    }
    /**
     * 获取当前及所有的下级部门code
     * @Description: 
     * @param code
     * @return
     */
    public List<String> getBranchDeptCode(String code,List<String> list){
    	list.add(code);
    	try {
			Vector v = this.getChildren(code);
			Iterator ir = v.iterator();
			while (ir.hasNext()) {
				DeptDb dept = (DeptDb)ir.next();
				String childCode = dept.getCode();
				this.getBranchDeptCode(childCode, list);
			}
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		}
    	return list;
    }
    
    /**
     * 获得 用户 可管理的每个部门下的用户
     * LZM
     * @Description: 
     * @param request
     * @return
     */
    public ArrayList<String> getUserAdminDeptsUser(HttpServletRequest request) {
		DeptDb dd = new DeptDb(DeptDb.ROOTCODE);
		DeptUserDb deptUserDb = new DeptUserDb();
		Vector vec = new Vector();
		ArrayList<String> list = new ArrayList<String>();
		Privilege priv = new Privilege();
		try {
			vec = dd.getAllChild(vec, dd);
		} catch (ErrMsgException e) {
		}
		
		Iterator it = vec.iterator();
		while (it.hasNext()) {
			DeptDb dept = (DeptDb) it.next();
			if (priv.canAdminDept(request, dept.getCode())) {
				//部门下的所有用户
				
				Vector deptUsersVec = deptUserDb.list(dept.getCode());
				if(deptUsersVec != null && deptUsersVec.size()>0){
					Iterator deptUsersIt = deptUsersVec.iterator();
					while(deptUsersIt.hasNext()){
						DeptUserDb dUserDb = (DeptUserDb)deptUsersIt.next();
						list.add(dUserDb.getUserName());
						
					}
				}
		
			}
		}
		return list;
	}
    /**
     * 根据部门code 获得 管理该部门的用户
     * @Description: 
     * @param dCode
     * @return
     */
  public List<UserDb> getDeptManagersBydCode(String dCode){
	  List<UserDb> list = new ArrayList<UserDb>();
	  String sql = "select userName from user_admin_dept where deptCode = ?";
	  JdbcTemplate jt = null;
	  jt = new JdbcTemplate();
	  try {
		ResultIterator ri = jt.executeQuery(sql,new Object[]{dCode});
		while(ri.hasNext()){
			ResultRecord rr = (ResultRecord)ri.next();
			String userName = rr.getString("userName");
			UserDb ud = new UserDb(userName);
			if(ud.isLoaded() &&  ud.isValid()){
				list.add(ud);
			}
		}
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		logger.error("SqlException:"+e.getMessage());
	}
	return list;
	  
  }

}

