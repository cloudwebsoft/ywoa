package com.redmoon.oa.flow;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudweb.oa.cache.FlowShowRuleCache;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.oa.db.*;
import com.redmoon.oa.person.*;
import com.redmoon.oa.pvg.*;
import com.redmoon.oa.sys.DebugUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkflowPredefineDb extends ObjectDb {
    private int id;

    /**
     * 当节点上设置了“删除流程”标志位时，被退回时能否删除
     */
    private boolean canDelOnReturn = false;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;
    
    /**
     * 按流程图流转
     */
    public static final int RETURN_MODE_NORMAL = 0;
    
    /**
     * 返回给打回者
     */
    public static final int RETURN_MODE_TO_RETURNER = 1;
    
    /**
     * 按流程图返回至设定的人员
     */
    public static final int RETURN_STYLE_NORMAL = 0;
    
    /**
     * 可返回至任一已处理过的人员
     */
    public static final int RETURN_STYLE_FREE = 1;
    
    /**
     * 不判断下一节点
     */
    public static final int ROLE_RANK_MODE_NONE = 0;
    
    /**
     * 如果下一节点小于则跳过
     */
    public static final int ROLE_RANK_NEXT_LOWER_JUMP = 2;
    // public static final int ROLE_RANK_NEXT_LOWERANDEQUAL_JUMP = 2;
    
    public static final String COMB_COND_TYPE_FIELD = "comb_field";
    public static final String COMB_COND_TYPE_PRIV_ROLE = "comb_priv_role";
    public static final String COMB_COND_TYPE_PRIV_DEPT = "comb_priv_dept";
    
    public static final int WRITE_BACK_UPDATE = 0;
    public static final int WRITE_BACK_INSERT = 1;
    /**
     * 更新并插入新数据
     */
    public static final int WRITE_BACK_UPDATE_INSERT = 2;

    public WorkflowPredefineDb() {
        init();
    }

    public WorkflowPredefineDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void initDB() {
        tableName = "flow_predefined";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new WorkflowPredefineCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (flowString,typeCode,title,return_back,IS_DEFAULT_FLOW,id,dir_code,examine,is_reactive,is_recall,return_mode,return_style,role_rank_mode,props,views,scripts,is_light,link_prop,write_prop,is_distribute,write_db_prop,msg_prop,is_plus,is_transfer,is_reply,download_count,flow_json) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set flowString=?,typeCode=?,title=?,return_back=?,IS_DEFAULT_FLOW=?,dir_code=?,examine=?,is_reactive=?,is_recall=?,return_mode=?,return_style=?,role_rank_mode=?,props=?,views=?,scripts=?,is_light=?,link_prop=?,write_prop=?,is_distribute=?,write_db_prop=?,msg_prop=?,is_plus=?,is_transfer=?,is_reply=?,download_count=?,can_del_on_return=?,flow_json=?,is_module_filter=?,is_use_form_view_rule=? where id=?";
        QUERY_LIST = "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select flowString,typeCode,title,return_back,IS_DEFAULT_FLOW,dir_code,examine,is_reactive,is_recall,return_mode,return_style,role_rank_mode,props,views,scripts,is_light,link_prop,write_prop,is_distribute,write_db_prop,msg_prop,is_plus,is_transfer,is_reply,download_count,can_del_on_return,flow_json,is_module_filter,is_use_form_view_rule from " + tableName + " where id=?";
    }

    public WorkflowPredefineDb getWorkflowPredefineDb(int id) {
        return (WorkflowPredefineDb)getObjectDb(id);
    }
    
    public int getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}

	/**
     * 取得自由流程开始角色
     * @return
     */
    public String[] getStarterRoles() {
        if (flowString.trim().equals(""))
            return new String[0];
        int p = flowString.indexOf("[starter]");
        int q = flowString.indexOf("[/starter]");
        if (p == -1 || q==-1 || q==p+7)
            return new String[0];
        String str = flowString.substring(p + "[starter]".length(), q);
        String[] ary = StrUtil.split(str, ":");
        if (ary!=null && ary.length>0) {
        	return StrUtil.split(ary[0], ",");
        }

        return new String[0];
    }

    /**
     * 取得自由流程开始角色可写表单域
     * 已作废
     * @return
     */
    public String[] getFieldsWriteOfStarter() {
        if (flowString.trim().equals(""))
            return new String[0];
        int p = flowString.indexOf("[starter]");
        int q = flowString.indexOf("[/starter]");
        if (p == -1 || q==-1 || q==p+7)
            return new String[0];
        String str = flowString.substring(p + "[starter]".length(), q);
        String[] ary = StrUtil.split(str, ":");
        if (ary!=null && ary.length>1) {
        	return StrUtil.split(ary[1], ",");
        }

        return new String[0];
    }
    
    /**
     * 取得自由流程中角色的权限
     * @return String[][]
     * @throws ErrMsgException
     */
    public String[][] getRolePrivsOfFree() {
        if (flowString.trim().equals("")) {
            return new String[0][0];
        }
        int p = flowString.indexOf("[roles]");
        int q = flowString.indexOf("[/roles]");
        if (p == -1 || q==-1 || q==p+7) {
            return new String[0][0];
        }
        String str = flowString.substring(p + "[roles]".length(), q);
        String[] ary = str.split(";");
        int len = ary.length;
        String[][] rolePrivs = new String[len][9];
        for (int i=0; i<len; i++) {
            String roleCode = ary[i].substring(0, ary[i].indexOf(":"));
            str = ary[i].substring(ary[i].indexOf(":") + 1);
            String[] privs = StrUtil.split(str, ",");
            int n = privs.length;
            // LogUtil.getLog(getClass()).info("getRolePrivOfFree " + str + "n=" + n);
            rolePrivs[i][0] = roleCode;
            System.arraycopy(privs, 0, rolePrivs[i], 1, n);
        }
        return rolePrivs;
    }

    /**
     * 取得自由流程的预定义流程
     * @param typeCode String
     * @return WorkflowPredefineDb
     * @throws ErrMsgException
     */
    public WorkflowPredefineDb getPredefineFlowOfFree(String typeCode) {
        WorkflowPredefineDb wp = getDefaultPredefineFlow(typeCode);
        if (wp==null) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(typeCode);
            
            if (lf==null) {
            	return null;
            }
            
            try {
                wp = new WorkflowPredefineDb();
                int id = (int) SequenceManager.nextID(SequenceManager.OA_WORKFLOW_PREDEFINED);
                wp.setTypeCode(typeCode);
                wp.setTitle(lf.getName());
                wp.setDefaultFlow(true);
                wp.setId(id);
                wp.setFlowString("");
                wp.create();
            }
            catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("getPredefineFlowOfFree:" + e.getMessage());
            }
        }
        return wp;
    }

    /**
     * 取得自由流程中角色可写的表单域
     * @param roleCode String
     * @return String[]
     * @throws ErrMsgException
     */
    public String[] getFieldsWriteOfRole(String roleCode) {
        String[][] rolePrivs = getRolePrivsOfFree();
        // 根据用户角色判定权限
        for (String[] rolePriv : rolePrivs) {
            if (roleCode.equals(rolePriv[0])) {
                String fieldStr = rolePriv[7];
                LogUtil.getLog(getClass()).info("getFieldsWriteOfRole:" + rolePriv[7]);
                return StrUtil.split(fieldStr, "\\|");
            }
        }
        return new String[0];
    }

    /**
     * 取得自由流程中用户可写的表单域，取得结果为用户所有角色拥有权限的并集
     * @param userName String
     * @return String[]
     * @throws ErrMsgException
     */
    public String[] getFieldsWriteOfUser(WorkflowDb wf, String userName) {
        Vector v = new Vector();
    	/*
    	// 如果是发起者
    	if (userName.equals(wf.getUserName())) {
    		return getFieldsWriteOfStarter();
    	} 
    	*/   	
        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        RoleDb[] roles = user.getRoles();
        if (roles!=null) {
            // 遍历用户所属的角色
            int len = roles.length;
            for (int i=0; i<len; i++) {
				// 获取角色能写的表单域
				String[] fields = getFieldsWriteOfRole(roles[i].getCode());

				if (fields == null) {
                    continue;
                }
				int flen = fields.length;
				for (int j = 0; j < flen; j++) {
					boolean isAdded = false;
					Iterator ir = v.iterator();
					while (ir.hasNext()) {
						// 遍历已记录的表单域，检查是否已被加入
						String f = (String) ir.next();
						if (f.equals(fields[j])) {
							isAdded = true;
							break;
						}
					}
					if (!isAdded) {
						v.addElement(fields[j]);
					}
				}
            }
        }
        String[] re = new String[v.size()];
        int len = v.size();
        for (int i=0; i<len; i++) {
            re[i] = (String)v.elementAt(i);
        }

        return re;
    }

    /**
     * 判定用户是否具有自由流程中的权限
     * @param user UserDb
     * @param typeCode String 流程类型
     * @param priv String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean canUserDo(UserDb user, String typeCode, String priv) throws ErrMsgException {
        WorkflowPredefineDb wfp = getPredefineFlowOfFree(typeCode);
        
        /*
        if (priv.equalsIgnoreCase("start")) {
        	String[] starterRoleAry = wfp.getStarterRoles();
        	int len = 0;
        	if (starterRoleAry!=null)
        		len = starterRoleAry.length;
        	for (int i=0; i<len; i++) {
                if (user.isUserOfRole(starterRoleAry[i])) {
                	return true;
                }
        	}
        	
        	return false;
        }
        */
        
        String[][] rolePrivs = wfp.getRolePrivsOfFree();
        // 根据用户角色判定权限
        int privLen = rolePrivs.length;
        boolean isValid = false;
        for (int i=0; i<privLen; i++) {
            if (user.isUserOfRole(rolePrivs[i][0])) {
            	if (priv.equalsIgnoreCase("start")) {
                    isValid = StrUtil.getNullStr(rolePrivs[i][1]).equals("1");
                }
                else if (priv.equalsIgnoreCase("stop")) {
                    isValid = StrUtil.getNullStr(rolePrivs[i][2]).equals("1");
                }
                else if (priv.equalsIgnoreCase("archive")) {
                    isValid = StrUtil.getNullStr(rolePrivs[i][3]).equals("1");
                }
                else if (priv.equalsIgnoreCase("discard")) {
                    isValid = StrUtil.getNullStr(rolePrivs[i][4]).equals("1");
                }
                else if (priv.equalsIgnoreCase("del")) {
                    isValid = StrUtil.getNullStr(rolePrivs[i][5]).equals("1");
                }
                else if (priv.equalsIgnoreCase("delAttach")) {
                    isValid = StrUtil.getNullStr(rolePrivs[i][6]).equals("1");
                }
                else if (priv.equalsIgnoreCase("editAttach")) {
                	if (rolePrivs[i].length>=9) {
                		isValid = StrUtil.getNullStr(rolePrivs[i][8]).equals("1");
                	}
                }
                if (isValid) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判定用户是否具有自由流程中的权限
     * @param user UserDb
     * @param wf WorkflowDb
     * @param priv String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean canUserDo(UserDb user, WorkflowDb wf, String priv) throws ErrMsgException {
        return canUserDo(user, wf.getTypeCode(), priv);
    }

    /**
     * 取得预定义流程中的默认项（只有默认项才能用于流程的流转）
     * @param typeCode String
     * @return WorkflowPredefineDb
     */
    public WorkflowPredefineDb getDefaultPredefineFlow(String typeCode) {
         ResultSet rs = null;
         PreparedStatement ps = null;
         String sql = "select id from " + tableName + " where typeCode=? and IS_DEFAULT_FLOW=1";
         Conn conn = new Conn(connname);
         try {
             ps = conn.prepareStatement(sql);
             ps.setString(1, typeCode);
             rs = conn.executePreQuery();
             if (rs != null && rs.next()) {
                 int id = rs.getInt(1);
                 return getWorkflowPredefineDb(id);
             }
         } catch (SQLException e) {
             LogUtil.getLog(getClass()).error("getDefaultFlow: " + e.getMessage());
             LogUtil.getLog(getClass()).error(e);
         } finally {
             conn.close();
         }
         return null;
    }

    @Override
    public boolean create() throws ErrMsgException {
        boolean re = false;
        PreparedStatement ps = null;
        defaultFlow = getDefaultPredefineFlow(typeCode)==null;
        id = (int)SequenceManager.nextID(SequenceManager.OA_WORKFLOW_PREDEFINED);
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, flowString);
            ps.setString(2, typeCode);
            ps.setString(3, title);
            ps.setInt(4, returnBack?1:0);
            ps.setInt(5, defaultFlow?1:0);
            ps.setInt(6, id);
            ps.setString(7, dirCode);
            ps.setInt(8, examine);
            ps.setInt(9, reactive?1:0);
            ps.setInt(10, recall?1:0);
            ps.setInt(11, returnMode);
            ps.setInt(12, returnStyle);
            ps.setInt(13, roleRankMode);
            ps.setString(14, props);
            ps.setString(15, views);
            ps.setString(16, scripts);
            ps.setInt(17, light?1:0);
            ps.setString(18, linkProp);
            ps.setString(19, writeProp);
            ps.setInt(20, distribute?1:0);
            ps.setString(21, writeDbProp);
            ps.setString(22, msgProp);
            ps.setInt(23, plus?1:0);
            ps.setInt(24, transfer?1:0);
            ps.setInt(25, reply?1:0);
            ps.setInt(26, downloadCount);
            ps.setString(27, flowJson);
            re = conn.executePreUpdate()==1;
            if (re) {
                WorkflowPredefineCache rc = new WorkflowPredefineCache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage() + " " + StrUtil.trace(e));
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException("数据库操作失败！");
        }
        finally {
            conn.close();
        }
        return re;
    }

    public int delWorkflowPredefineDbOfType(String typeCode) {
        String sql = "select id from " + tableName + " where typeCode=?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int i = 0;
        Conn conn = new Conn(connname);
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, typeCode);
            rs = conn.executePreQuery();
            while (rs.next()) {
                WorkflowPredefineDb wd = getWorkflowPredefineDb(rs.getInt(1));
                try {
                    wd.del();
                }
                catch (ErrMsgException e) {
                    LogUtil.getLog(getClass()).error("delWorkflowPredefineDbOfType:" + e.getMessage());
                }
                i++;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("delWorkflowPredefineDbOfType:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return i;
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     */
    @Override
    public boolean del() throws ErrMsgException {
        boolean re = false;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                WorkflowPredefineCache rc = new WorkflowPredefineCache(this);
                primaryKey.setValue(id);
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del: " + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    /**
     *
     * @param pk Object
     * @return Object
     */
    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new WorkflowPredefineDb(pk.getIntValue());
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     */
    @Override
    public void load() {
        ResultSet rs = null;
        PreparedStatement ps = null;
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                flowString = StrUtil.getNullStr(rs.getString(1));
                typeCode = rs.getString(2);
                title = rs.getString(3);
                returnBack = rs.getInt(4)==1;
                defaultFlow = rs.getInt(5)==1;
                dirCode = StrUtil.getNullString(rs.getString(6));
                examine = rs.getInt(7);
                reactive = rs.getInt(8)==1;
                recall = rs.getInt(9)==1;
                returnMode = rs.getInt(10);
                returnStyle = rs.getInt(11);
                roleRankMode = rs.getInt(12);
                props = StrUtil.getNullStr(rs.getString(13));
                views = StrUtil.getNullStr(rs.getString(14));
                scripts = StrUtil.getNullStr(rs.getString(15));
                light = rs.getInt(16)==1;
                linkProp = StrUtil.getNullStr(rs.getString(17));
                writeProp = StrUtil.getNullStr(rs.getString(18));
                distribute = rs.getInt(19)==1;
                writeDbProp = StrUtil.getNullStr(rs.getString(20));
                msgProp = StrUtil.getNullStr(rs.getString(21));
                plus = rs.getInt(22)==1;
                transfer = rs.getInt(23)==1;
                reply = rs.getInt(24)==1;
                downloadCount = rs.getInt(25);
                canDelOnReturn = rs.getInt(26)==1;
                flowJson = StrUtil.getNullStr(rs.getString(27));
                moduleFilter = rs.getInt(28) == 1;
                useFormViewRule = rs.getInt(29) == 1;
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     */
    @Override
    public boolean save() throws ErrMsgException {
        PreparedStatement ps = null;
        boolean re = false;
        if (defaultFlow) {
            // 清除原来的默认预定义流程
            WorkflowPredefineDb wpf = getDefaultPredefineFlow(typeCode);
            if (wpf != null && wpf.isLoaded() && wpf.getId()!=id) {
                wpf.setDefaultFlow(false);
                wpf.save();
            }
        }
        Conn conn = new Conn(connname);
        try {
            ps = conn.prepareStatement(QUERY_SAVE);
            if ("".equals(flowString)) {
                flowString = " "; // 适应SQLSERVER
            }
            ps.setString(1, flowString); //适应Oracle，LONG类型需放在第一个
            ps.setString(2, typeCode);
            ps.setString(3, title);
            ps.setInt(4, returnBack ? 1 : 0);
            ps.setInt(5, defaultFlow ? 1 : 0);
            ps.setString(6, dirCode);
            ps.setInt(7, examine);
            ps.setInt(8, reactive ? 1 : 0);
            ps.setInt(9, recall ? 1 : 0);
            ps.setInt(10, returnMode);
            ps.setInt(11, returnStyle);
            ps.setInt(12, roleRankMode);
            ps.setString(13, props);
            ps.setString(14, views);
            ps.setString(15, scripts);
            ps.setInt(16, light ? 1 : 0);
            ps.setString(17, linkProp);
            ps.setString(18, writeProp);
            ps.setInt(19, distribute ? 1 : 0);
            ps.setString(20, writeDbProp);
            ps.setString(21, msgProp);
            ps.setInt(22, plus ? 1 : 0);
            ps.setInt(23, transfer ? 1 : 0);
            ps.setInt(24, reply ? 1 : 0);
            ps.setInt(25, downloadCount);
            ps.setInt(26, canDelOnReturn ? 1 : 0);
            ps.setString(27, flowJson);
            ps.setInt(28, moduleFilter ? 1 : 0);
            ps.setInt(29, useFormViewRule ? 1 : 0);
            ps.setInt(30, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                WorkflowPredefineCache rc = new WorkflowPredefineCache(this);
                primaryKey.setValue(id);
                rc.refreshSave(primaryKey);

                FlowShowRuleCache flowShowRuleCache = SpringUtil.getBean(FlowShowRuleCache.class);
                flowShowRuleCache.refresh(typeCode);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            LogUtil.getLog(getClass()).error("save: " + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        } finally {
            conn.close();
        }
        return re;
    }

    /**
     * 取出全部信息置于result中
     */
    @Override
    public Vector list(String sql) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    result.addElement(getWorkflowPredefineDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list:" + e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return result;
    }

    @Override
    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();

        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (!rs.absolute(absoluteLocation)) {
                    return lr;
                }
                do {
                    WorkflowPredefineDb ug = getWorkflowPredefineDb(rs.getInt(1));
                    result.addElement(ug);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {LogUtil.getLog(getClass()).error(e);}
            }
            conn.close();
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    /**
     * 取得预设流程中的发起者，用于调度发起流程
     * @return String[] 如果是$self或者角色中无用户或者用户为空，则返回空的Vector
     */
    public Vector getStarters() {
        String predefinedStr = getFlowString();
        Vector v = new Vector();

        if (!predefinedStr.startsWith("paper")) {
            return v;
        }
        String[] ary = predefinedStr.split("\\r\\n");
        int len = ary.length;
        for (int i = 1; i < len; i++) {
            WorkflowActionDb wa = new WorkflowActionDb();
            boolean re = false;
            try {
                re = wa.fromString(ary[i], false);
            }
            catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("getStartersOfPredefinedFlow:" + e.getMessage());
            }
            if (re) { // 如果是action，而不是link
                if (wa.isStart == 1) {
                    String jobCode = wa.getJobCode();
                    if ("$self".equals(jobCode)) {
                        return v;
                    }

                    // 检查发起人是否合法
                    if (wa.getNodeMode() == WorkflowActionDb.NODE_MODE_ROLE) {
                        String[] prearyrole = StrUtil.split(wa.getJobCode(), ",");
                        int prelen = 0;
                        if (prearyrole != null) {
                            prelen = prearyrole.length;
                        }
                        RoleMgr rm = new RoleMgr();
                        for (int k=0; k<prelen; k++) {
                            RoleDb rd = rm.getRoleDb(prearyrole[k]);
                            if (!rd.isLoaded()) {
                                DebugUtil.w(getClass(), "getStarters", "角色: " + prearyrole[k] + " 不存在");
                                continue;
                            }
                            Vector vt = rd.getAllUserOfRole();
                            int vsize = v.size();
                            int vtsize = vt.size();
                            // 检查是否已被加入，避免重复加入
                            boolean isFound = false;
                            for (int m=0; m<vtsize; m++) {
                                UserDb ud = (UserDb)vt.elementAt(m);
                                for (int n=0; n<vsize; n++) {
                                	UserDb userName = (UserDb)v.elementAt(n);
                                    if (ud.getName().equals(userName.getName())) {
                                        isFound = true;
                                        break;
                                    }
                                }
                                if (!isFound) {
                                    v.addElement(ud);
                                }
                            }
                        }
                    } else {
                        String[] aryuser = StrUtil.split(wa.getJobCode(), ",");
                        UserMgr um = new UserMgr();
                        for (String s : aryuser) {
                            UserDb ud = um.getUserDb(s);
                            v.addElement(ud);
                        }
                    }
                    return v;
                }
            }
        }
        return v;
    }

    public String getFlowString() {
        return flowString;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getTitle() {
        return title;
    }

    public boolean isReturnBack() {
        return returnBack;
    }

    public boolean isDefaultFlow() {
        return defaultFlow;
    }

    public String getDirCode() {
        return dirCode;
    }

    public int getExamine() {
        return examine;
    }

    public boolean isReactive() {
        return reactive;
    }

    public boolean isRecall() {
        return recall;
    }

    public void setFlowString(String flowString) {
        this.flowString = flowString;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReturnBack(boolean returnBack) {
        this.returnBack = returnBack;
    }

    public void setDefaultFlow(boolean defaultFlow) {
        this.defaultFlow = defaultFlow;
    }

    public void setDirCode(String dirCode) {
        this.dirCode = dirCode;
    }

    public void setExamine(int examine) {
        this.examine = examine;
    }

    public void setReactive(boolean reactive) {
        this.reactive = reactive;
    }

    public void setRecall(boolean recall) {
        this.recall = recall;
    }

    private String flowString;
    private String typeCode;
    private String title;
    private boolean returnBack;
    private boolean defaultFlow;
    private String dirCode;
    private int examine = 0;

    /**
     * 节点是否能重新激活，激活后，可以重新修改表单，并重新分配流转至下一节点
     */
    private boolean reactive = false;

    /**
     * 是否能撤销已激活的下一动作
     */
    private boolean recall = false;

    private int returnMode = RETURN_MODE_NORMAL;
    
    /**
     * 返回方式，20170227 fgf 改为默认为RETURN_STYLE_FREE
     */
    private int returnStyle = RETURN_STYLE_FREE; // RETURN_STYLE_NORMAL;
    
    public int getRoleRankMode() {
		return roleRankMode;
	}

	public void setRoleRankMode(int roleRankMode) {
		this.roleRankMode = roleRankMode;
	}

	private int roleRankMode = ROLE_RANK_MODE_NONE;

	public int getReturnMode() {
		return returnMode;
	}

	public void setReturnMode(int returnMode) {
		this.returnMode = returnMode;
	}

	public void setReturnStyle(int returnStyle) {
		this.returnStyle = returnStyle;
	}

	public int getReturnStyle() {
		return returnStyle;
	}
	
	public void setProps(String props) {
		this.props = props;
	} 
  
	public String getProps() {
		return props;
	}

	public void setViews(String views) {
		this.views = views;
	}

	public String getViews() {
		return views;
	}

	public void setScripts(String scripts) {
		this.scripts = scripts;
	}

	public String getScripts() {
		return scripts;
	}

	public void setLight(boolean light) {
		this.light = light;
	}

	public boolean isLight() {
		return light;
	}

	private String props;
	
	private String views;
	
	private String scripts;
	
	private boolean light = false;
	
	private String linkProp;
	
	/**
	 * 回写模块配置
	 */
	private String writeProp;
	
	/**
	 * 回写数据库配置
	 */
	private String writeDbProp;
	
	/**
	 * 是否允许在每个节点上分发或知会
	 */
	private boolean distribute;
	
	/**
	 * 消息发送配置
	 */
	private String msgProp;
	
	/**
	 * 是否允许加签
	 */
	boolean plus;
	
	/**
	 * 能否回复
	 */
	boolean reply = true;
	
	public boolean isReply() {
		return reply;
	}

	public void setReply(boolean reply) {
		this.reply = reply;
	}

	/**
	 * @return the plus
	 */
	public boolean isPlus() {
		return plus;
	}

	/**
	 * @param plus the plus to set
	 */
	public void setPlus(boolean plus) {
		this.plus = plus;
	}

	/**
	 * @return the transfer
	 */
	public boolean isTransfer() {
		return transfer;
	}

	/**
	 * @param transfer the transfer to set
	 */
	public void setTransfer(boolean transfer) {
		this.transfer = transfer;
	}

	/**
	 * 是否允许指派
	 */
	boolean transfer;

    public boolean isUseFormViewRule() {
        return useFormViewRule;
    }

    public void setUseFormViewRule(boolean useFormViewRule) {
        this.useFormViewRule = useFormViewRule;
    }

    boolean useFormViewRule = true;
	
	/**
	 * 下载次数限制，-1表示不限
	 */
	private int downloadCount = -1;

    public boolean isModuleFilter() {
        return moduleFilter;
    }

    public void setModuleFilter(boolean moduleFilter) {
        this.moduleFilter = moduleFilter;
    }

    private boolean moduleFilter = true;

    public String getFlowJson() {
        return flowJson;
    }

    public void setFlowJson(String flowJson) {
        this.flowJson = flowJson;
    }

    private String flowJson = "";
	
	public String getWriteProp() {
		return writeProp;
	}

	public void setWriteProp(String writeProp) {
		this.writeProp = writeProp;
	}

	public String getLinkProp() {
		return linkProp;
	}

	public void setLinkProp(String linkProp) {
		this.linkProp = linkProp;
	}

	public void setDistribute(boolean distribute) {
		this.distribute = distribute;
	}

	public boolean isDistribute() {
		return distribute;
	}

	public void setWriteDbProp(String writeDbProp) {
		this.writeDbProp = writeDbProp;
	}

	public String getWriteDbProp() {
		return writeDbProp;
	}

	public void setMsgProp(String msgProp) {
		this.msgProp = msgProp;
	}

	public String getMsgProp() {
		return msgProp;
	}

    public boolean isCanDelOnReturn() {
        return canDelOnReturn;
    }

    public void setCanDelOnReturn(boolean canDelOnReturn) {
        this.canDelOnReturn = canDelOnReturn;
    }
}
