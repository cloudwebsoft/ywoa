package com.redmoon.oa.dept;

import java.io.*;
import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.util.*;
import com.redmoon.oa.db.*;
import com.redmoon.oa.person.*;
import com.redmoon.oa.pvg.Privilege;

import rtx.*;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DeptUserDb extends ObjectDb implements Serializable {
    private String deptCode = "", userName = "";

    public DeptUserDb() {
        init();
    }

    public DeptUserDb(int id) {
        this.id = id;
        load();
        init();
    }
    
    public DeptUserDb(String userName) {
        this.userName = userName;
        loadOfName();
        init();
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new DeptUserDb(pk.getIntValue());
    }

    public void setQueryCreate() {
        QUERY_CREATE = "insert into dept_user (DEPT_CODE,USER_NAME,ORDERS,RANK,ID) values(?,?,?,?,?)";
    }

    public void setQuerySave() {
        this.QUERY_SAVE = "update dept_user set USER_NAME=?,DEPT_CODE=?,ORDERS=?,RANK=? where ID=?";
    }

    public void setQueryDel() {
        QUERY_DEL = "delete from dept_user where ID=?";
    }

    public void setQueryLoad() {
        this.QUERY_LOAD = "select DEPT_CODE,USER_NAME,ORDERS,RANK from dept_user where ID=?";
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                deptCode = rs.getString(1);
                userName = StrUtil.getNullStr(rs.getString(2));
                orders = rs.getInt(3);
                rank = rs.getString(4);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }
    
    public void loadOfName() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement("select ID,DEPT_CODE,ORDERS,RANK from dept_user where USER_NAME=?");
            ps.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
            	id = rs.getInt(1);
                deptCode = rs.getString(2);
                orders = rs.getInt(3);
                rank = rs.getString(4);
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {e.printStackTrace();}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     * 用于兼容 5.0版
     * @param deptCode
     * @param userName
     * @param rank
     * @return
     * @throws ErrMsgException
     */
    public boolean create(String deptCode, String userName, String rank) throws ErrMsgException {
        orders = getMaxOrders(deptCode) + 1;
        return create(deptCode, userName, orders, rank);
    }
    
    /**
     * 用于导入 6.0版
     * @param deptCode
     * @param userName
     * @param orders
     * @param rank
     * @return
     * @throws ErrMsgException
     */
    public boolean create(String deptCode, String userName, int orders, String rank) throws ErrMsgException {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, deptCode);
            ps.setString(2, userName);
            ps.setInt(3, orders);
            ps.setString(4, rank);
            id = (int)SequenceManager.nextID(SequenceManager.OA_DEPT_USER);
            ps.setInt(5, id);
            re = conn.executePreUpdate()==1;

            if (re) {
            	DeptDb dd = new DeptDb();
            	dd = dd.getDeptDb(deptCode);
            	String unitCode = dd.getUnitOfDept(dd).getCode();
            	
            	// 置单位编码
            	UserDb user = new UserDb();
            	user = user.getUserDb(userName);
            	user.setUnitCode(unitCode);
            	// 注意这里需刷新user缓存, 因为所属部门用户组在user实例中
            	user.save();
            	
                DeptUserCache dcm = new DeptUserCache();
                dcm.refreshCreate();
                
                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
                if (isRTXUsed) {
                    RTXUtil.addUserToDept(user, "", dd.getName(), true);
                }
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作错误，可能是编码重复造成的！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }    

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
    
    public String getUserNameUrlEncoded() {
    	return StrUtil.UrlEncode(userName);
    }

    public String getUserName() {
        return userName;
    }
    
    public String getDuty() {
        if (userName==null || userName.equals(""))
            return "";
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        if (ud!=null &&  ud.isLoaded())
            return ud.getDuty();
        else
            return "";
    }

    public String getParty() {
        if (userName==null || userName.equals(""))
            return "";
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        if (ud!=null &&  ud.isLoaded())
            return ud.getParty();
        else
            return "";
    }
    
    public int getId() {
        return id;
    }

    public int getOrders() {
        return orders;
    }

    public String getRank() {
        return rank;
    }

    public String getAdminDepts() {
    	Vector v = Privilege.getUserAdminDepts(userName);
        String str = "";    	
    	Iterator ir = v.iterator();
    	while (ir.hasNext()) {
    		DeptDb dd = (DeptDb)ir.next();
            if (str.equals(""))
                str = dd.getCode();
            else
                str += "," + dd.getCode();    		
    	}
        return str;
    }

    public void setUserName(String n) {
        this.userName = n;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    private int id;

    public synchronized boolean save() {
        int r = 0;
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, userName);
            ps.setString(2, deptCode);
            ps.setInt(3, orders);
            ps.setString(4, rank);
            ps.setInt(5, id);
            r = conn.executePreUpdate();
            try {
                if (r == 1) {
                    re = true;
                    DeptUserCache dcm = new DeptUserCache();
                    dcm.refreshSave(id);
                    
                	DeptDb dd = new DeptDb();
                	dd = dd.getDeptDb(deptCode);
                	String unitCode = dd.getUnitOfDept(dd).getCode();
                	
                	// 置单位编码
                	UserDb user = new UserDb();
                	user = user.getUserDb(userName);
                	user.setUnitCode(unitCode);
                	user.save();                    
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } catch (SQLException e) {
            logger.error("修改出错！");
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        int r = 0;
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            r = conn.executePreUpdate();
            if (r == 1) {
                re = true;
                DeptUserCache dcm = new DeptUserCache();
                dcm.refreshDel(id);

                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");
                if (isRTXUsed) {
                    DeptDb dd = new DeptDb();
                    dd = dd.getDeptDb(deptCode);
                    UserDb user = new UserDb();
                    RTXUtil.delUserFromDept(user.getUserDb(userName), dd.getName());
                }
            }
            if (ps!=null) {
                ps.close();
                ps = null;
            }
            // 将位于其后的同一部门下的用户的orders下降一位
            String sql = "select ID from dept_user where DEPT_CODE=? and ORDERS>?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, deptCode);
            ps.setInt(2, orders);
            ResultSet rs = conn.executePreQuery();
            while (rs.next()) {
                DeptUserDb du = getDeptUserDb(rs.getInt(1));
                du.setOrders(du.getOrders() - 1);
                du.save();
            }
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     * 用户是否属于某个部门或其子部门
     * @param userName String
     * @param deptCode String
     * @return boolean
     */
    public boolean isUserBelongToDept(String userName, String deptCode) {
        Vector v = getDeptsOfUser(userName);
        int size = v.size();
        for (int i = 0; i < size; i++) {
            DeptDb dd = (DeptDb) v.elementAt(i);
            // LogUtil.getLog(getClass()).info("isUserBelongToDept1 deptCode=" + deptCode + " dd.getCode()=" + dd.getCode() + " " + dd.getName());
            if (dd.getCode().equals(deptCode))
                return true;
        }
        
        // 部门单位是否就为deptCode
        if (size==1) {
            DeptDb dd = (DeptDb)v.elementAt(0);
            // 如果dd的单位是deptCode
            if (dd.getUnitOfDept(dd).getCode().equals(deptCode))
                return true;
        }        

        // 判断用户是否属于该deptCode部门的子部门

        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            DeptDb dd = (DeptDb)ir.next();
            // 向上遍历
            String parentCode = dd.getParentCode();
            while (!parentCode.equals("-1")) {
                if (parentCode.equals(deptCode))
                    return true;
                dd = dd.getDeptDb(parentCode);
                if (dd==null || !dd.isLoaded())
                    break;
                parentCode = dd.getParentCode();
            }
        }        
        
        /*
        // 效率严重有问题 
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(deptCode);
        try {
            Vector vt = new Vector();
            dd.getAllChild(vt, dd);
            Iterator ir = vt.iterator();
            while (ir.hasNext()) {
                DeptDb childdd = (DeptDb) ir.next();
                for (int i = 0; i < size; i++) {
                    dd = (DeptDb) v.elementAt(i);
                    // LogUtil.getLog(getClass()).info("isUserBelongToDept2 dd.getCode=" + dd.getCode() + " childdd.getCode()=" + childdd.getCode() + " " + dd.getName());
                    if (dd.getCode().equals(childdd.getCode()))
                        return true;
                }
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        */
        return false;
    }

    /**
     * 用户是否属于某部门
     * @param userName String 用户名
     * @param deptCode String 部门编码
     * @return boolean
     */
    public boolean isUserOfDept(String userName, String deptCode) {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            String sql = "select ID from dept_user where DEPT_CODE=? and USER_NAME=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, deptCode);
            ps.setString(2, userName);
            ResultSet rs = conn.executePreQuery();
            while (rs.next()) {
                re = true;
            }
        } catch (SQLException e) {
            logger.error("isUserOfDept:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public DeptUserDb getDeptUserDb(int id) {
        DeptUserCache dcm = new DeptUserCache();
        return dcm.getDeptUserDb(id);
    }

    public String getDeptName() {
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(deptCode);
        return dd.getName();
    }

    public void setQueryList() {
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
        String orderField = showByDeptSort ? "du.orders" : "u.orders";
        this.QUERY_LIST = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE=? order by " + orderField + " asc";
    }
    
    /**
     * 取得用户所在的单位，用户如果属于多个部门，则取其所在第一个部门所在的单位
     * @param userName
     * @return
     */
    public DeptDb getUnitOfUser(String userName) {
    	Iterator ir = getDeptsOfUser(userName).iterator();
    	// System.out.println(getClass() + "  getDeptsOfUser().size=" + getDeptsOfUser(userName).size());
    	if (ir.hasNext()) {
    		DeptDb dd = (DeptDb)ir.next();
        	// System.out.println(getClass() + " dd.getName()=" + dd.getName());
    		
    		return dd.getUnitOfDept(dd);
    	}
    	
    	DeptDb dd = new DeptDb();
    	return dd.getDeptDb(DeptDb.ROOTCODE);
    }
    
    /**
     * 取得用户所在部门的DeptUserDb
     * @param userName
     * @return
     */
    public DeptUserDb getDeptUserDb(String userName, String deptCode) {
        ResultSet rs = null;
        String sql =
                "select id from dept_user where USER_NAME=? and DEPT_CODE=? order by ORDERS";
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            ps.setString(2, deptCode);
            rs = conn.executePreQuery();
            if (rs == null) {
                return null;
            } else {
            	if (rs.next()) {
                    return getDeptUserDb(rs.getInt(1));
            	}
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;    	
    }
    
    /**
     * 取得用户所在的全部单位
     * @param userName
     * @return
     */
    public String[] getUnitsOfUser(String userName) {
    	if (userName == null) {
    		return null;
    	}
    	Iterator ir = getDeptsOfUser(userName).iterator();
    	Map map = new HashMap();
    	UserDb user = new UserDb();
    	user = user.getUserDb(userName);
    	map.put(user.getUnitCode(), "");
    	while (ir.hasNext()) {
    		DeptDb dd = (DeptDb)ir.next();
        	String unitCode = dd.getUnitOfDept(dd).getCode();
        	map.put(unitCode, "");
    	}

    	Set set = map.keySet();
    	String[] ary = new String[set.size()];
    	ir = set.iterator();
    	int i = 0;
    	while (ir.hasNext()) {
    		String key = (String)ir.next();
    		ary[i] = key;
    		i++;
    	}
    	return ary;
    }

    /**
     * 取得用户所在的部门
     * @param userName String
     * @return Vector
     */
    public Vector getDeptsOfUser(String userName) {
        ResultSet rs = null;
        String sql =
                "select DEPT_CODE from dept_user where USER_NAME=? order by id asc";
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs == null) {
                return null;
            } else {
                DeptMgr dm = new DeptMgr();
                while (rs.next()) {
                	DeptDb dd = dm.getDeptDb(rs.getString(1));
                	if (dd.isLoaded()) {
                		result.addElement(dd);
                	}
                }
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

    /**
     * 因为DWR不推荐重载，所以增加此方法，用于列出部门下的职位
     * @param deptCode String
     * @return Vector
     */
    public Vector list2DWR(String deptCode) {
        return list(deptCode);
    }
    
    public Vector listBySQL(String sql) {
        Vector result = new Vector();
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			rs = conn.executePreQuery();
			while (rs.next()) {
				result.addElement(getDeptUserDb(rs.getInt(1)));
			}
		} catch (SQLException e) {
			logger.error("list:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
        return result;    	
    }

    /**
	 * 取出全部信息置于result中
	 */
    public Vector list(String deptCode) {
        Vector result = new Vector();
        if (false && deptCode.equals(com.redmoon.oa.dept.DeptDb.ROOTCODE)) {
        	String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 order by du.DEPT_CODE asc, u.orders asc";
	        ResultSet rs = null;
	        Conn conn = new Conn(connname);
	        try {
				rs = conn.executeQuery(sql);
				while (rs.next()) {
					result.addElement(getDeptUserDb(rs.getInt(1)));
				}
	        } catch (SQLException e) {
	            logger.error("list:" + e.getMessage());
	        } finally {
	            if (conn != null) {
	                conn.close();
	                conn = null;
	            }
	        }
        }
    	else {
	        ResultSet rs = null;
	        Conn conn = new Conn(connname);
	        try {
	            PreparedStatement ps = conn.prepareStatement(this.QUERY_LIST);
	            ps.setString(1, deptCode);
	            rs = conn.executePreQuery();
				while (rs.next()) {
					result.addElement(getDeptUserDb(rs.getInt(1)));
				}
	        } catch (SQLException e) {
	            logger.error("list:" + e.getMessage());
	        } finally {
	            if (conn != null) {
	                conn.close();
	                conn = null;
	            }
	        }
    	}
        return result;
    }

    /**
     * 当删除用户时调用，删除用户部门后，部门人员中的顺序关系要整理
     * @param userName
     * @return
     */
    public boolean delUser(String userName) {
        String sql = "select ID from dept_user where USER_NAME=?";
        ResultSet rs = null;
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs == null) {
                return false;
            } else {
                while (rs.next()) {
                    getDeptUserDb(rs.getInt(1)).del();
                    re = true;
                }
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
    }

    public ObjectDb getObjectDb(Object objKey) {
        return getDeptUserDb(((Integer)objKey).intValue());
    }

    public int getObjectCount(String sql) {
        return getDeptUserCount(sql);
    }

    public int getDeptUserCount(String sql) {
        DeptUserCache uc = new DeptUserCache();
        return uc.getDeptUserCount(sql);
    }
    
    public Vector getAllUsersOfUnit(String unitCode) {
    	DeptDb dd = new DeptDb();
    	dd = dd.getDeptDb(unitCode);
    	try {
        	Vector vt = new Vector();
			dd.getAllChild(vt, dd);
			// if (vt.size()>0) {
				String depts = StrUtil.sqlstr(unitCode);
				Iterator ir = vt.iterator();
				while (ir.hasNext()) {
					dd = (DeptDb)ir.next();
					depts += "," + StrUtil.sqlstr(dd.getCode());
				}
				String sql = "select distinct USER_NAME from dept_user where DEPT_CODE in (" + depts + ")";
				UserDb user = new UserDb();
				return user.list(sql);
			// }
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Vector();
    }

    public String getUserRealName() {
        if (userName==null || userName.equals(""))
            return "";
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        if (ud!=null &&  ud.isLoaded())
            return ud.getRealName();
        else
            return "";
    }

    public String getUserId() {
        if (userName==null || userName.equals(""))
            return "";
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        if (ud!=null &&  ud.isLoaded())
            return "" + ud.getId();
        else
            return "";
    }

    public int getMaxOrders(String deptCode) {
        String SQL_GETMAXORDERS =
            "select max(orders) from dept_user where DEPT_CODE=?";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        int maxorders = -1;
        try {
            // 更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(SQL_GETMAXORDERS);
            pstmt.setString(1, deptCode);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("getMaxOrders:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return maxorders;
    }

    public boolean move(String direction) throws ErrMsgException {
        // 根据direction检查是否可以移动
        if (direction.equals("up")) {
            if (orders==0)
                throw new ErrMsgException("该项已处在首位！");
        }
        if (direction.equals("down")) {
            if (orders==getMaxOrders(deptCode))
                throw new ErrMsgException("该项已处于最后一位！");
        }

        DeptUserDb bdud = getBrother(direction);
        if (bdud == null) {
            throw new ErrMsgException("该项不能被移动！");
        }

        int borders = bdud.getOrders();

        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            if (direction.equals("up")) {
                if (orders == 0)
                    return true;
                String sql = "update dept_user set orders=" + orders + " where id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, bdud.getId());
                conn.executePreUpdate();
                sql = "update dept_user set orders=" + borders + " where id=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                conn.executePreUpdate();
                re = true;
            } else {
                int maxorders = getMaxOrders(deptCode);
                if (orders == maxorders) {
                    return true;
                } else {
                    String sql = "update dept_user set orders=" + orders + " where id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, bdud.getId());
                    conn.executePreUpdate();
                    if (ps!=null) {
                        ps.close();
                        ps = null;
                    }
                    sql = "update dept_user set orders=" + borders + " where id=?";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, id);
                    conn.executePreUpdate();
                }
                re = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            DeptUserCache jc = new DeptUserCache();
            jc.refreshMove(id, bdud.getId());
        }
        return re;
    }

    public DeptUserDb getBrother(String direction) {
        String sql;
        Conn conn = new Conn(connname);
        DeptUserDb bleaf = null;
        try {
            if (direction.equals("down")) {
                sql = "select ID from dept_user where DEPT_CODE=" +
                      StrUtil.sqlstr(deptCode) +
                      " and ORDERS>" + orders + " order by orders asc";
            } else {
                sql = "select ID from dept_user where DEPT_CODE=" +
                      StrUtil.sqlstr(deptCode) +
                      " and ORDERS<" + orders + " order by orders desc";
            }

            ResultSet rs = conn.executeQuery(sql);
            if (rs.next()) {
                bleaf = getDeptUserDb(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("getBrother:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return bleaf;
    }

    private int orders; // 起始值为0

    private String rank;
    private String adminDept;

}
