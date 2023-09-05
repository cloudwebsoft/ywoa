package com.redmoon.oa.pvg;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

public class RoleUser {
	String userName;
	String roleCode;
	int orders;
	
	public RoleUser() {
		
	}
	
	public RoleUser(String userName, String roleCode) {
		this.userName = userName;
		this.roleCode = roleCode;
	}

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
        	String sql = "select orders from user_of_role where userName=? and roleCode=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            ps.setString(2, roleCode);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                orders = rs.getInt(1);
            }
        } catch (SQLException e) {
        	LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }	

    public int getMaxUserOrders(String roleCode) {
        String SQL_GETMAXORDERS =
            "select max(orders) from user_of_role where roleCode=?";
        Conn conn = new Conn(Global.getDefaultDB());
        ResultSet rs = null;
        int maxorders = -1;
        try {
            // 更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(SQL_GETMAXORDERS);
            pstmt.setString(1, roleCode);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getMaxUserOrders:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return maxorders;
    }    	
	
    public RoleUser getBrother(String direction) {
        String sql;
        Conn conn = new Conn(Global.getDefaultDB());
        RoleUser ru = null;
        try {
            if (direction.equals("down")) {
                sql = "select userName, roleCode, orders from user_of_role where roleCode=" +
                      StrUtil.sqlstr(roleCode) +
                      " and ORDERS>" + orders + " order by orders asc";
            } else {
                sql = "select userName, roleCode, orders from user_of_role where roleCode=" +
                      StrUtil.sqlstr(roleCode) +
                      " and ORDERS<" + orders + " order by orders desc";
            }

            ResultSet rs = conn.executeQuery(sql);
            if (rs.next()) {
            	ru = new RoleUser(rs.getString(1), rs.getString(2));
            	ru.setOrders(rs.getInt(3));
            }
        } catch (SQLException e) {
        	LogUtil.getLog(getClass()).error("getBrother:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return ru;
    }
    
    public boolean moveTo(String targetUser, int pos) {
    	boolean re = false;
		RoleUser ru = new RoleUser(targetUser, roleCode);
		ru.load();
    	if (pos==0) { // 之前    		
    		String sql = "update user_of_role set orders=orders+1 where roleCode=" + StrUtil.sqlstr(roleCode) + " and orders>=" + ru.getOrders();
    		JdbcTemplate jt = new JdbcTemplate();
    		try {
				jt.executeUpdate(sql);
			} catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e);
			}
    		
    		this.orders = ru.getOrders();
    		re = save();
    	}
    	else {
    		String sql = "select id from user_of_role where roleCode=" + StrUtil.sqlstr(roleCode) + " and orders>=" + (ru.getOrders()+1);
    		JdbcTemplate jt = new JdbcTemplate();
    		try {
				jt.executeUpdate(sql);
			} catch (SQLException e) {
                LogUtil.getLog(getClass()).error(e);
			}    	
    		this.orders = ru.getOrders() + 1;
    		re = save();
    	}
    	
    	// 生新整理顺序
    	Iterator ir = ru.list(roleCode).iterator();
    	int k = 1;
    	while (ir.hasNext()) {
    		ru = (RoleUser)ir.next();
    		ru.setOrders(k);
    		ru.save();
    		k++;
    	}
    	
    	return re;
    }

    public boolean move(String direction) throws ErrMsgException {
        // 根据direction检查是否可以移动
        if (direction.equals("up")) {
            if (orders==0)
                throw new ErrMsgException("该项已处在首位！");
        }
        
        if (direction.equals("down")) {
            if (orders==getMaxUserOrders(roleCode))
                throw new ErrMsgException("该项已处于最后一位！");
        }

        RoleUser ru = getBrother(direction);
        if (ru == null) {
            throw new ErrMsgException("该项不能被移动！");
        }

        int borders = ru.getOrders();

        Conn conn = new Conn(Global.getDefaultDB());
        boolean re = false;
        try {
            if (direction.equals("up")) {
                if (orders == 0)
                    return true;
                String sql = "update user_of_role set orders=" + orders + " where userName=? and roleCode=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, ru.getUserName());
                ps.setString(2, ru.getRoleCode());
                conn.executePreUpdate();
                sql = "update user_of_role set orders=" + borders + " where userName=? and roleCode=?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, userName);
                ps.setString(2, roleCode);
                conn.executePreUpdate();
                re = true;
            } else {
                int maxorders = getMaxUserOrders(roleCode);
                if (orders == maxorders) {
                    return true;
                } else {
                    String sql = "update user_of_role set orders=" + orders + " where userName=? and roleCode=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, ru.getUserName());
                    ps.setString(2, ru.getRoleCode());
                    conn.executePreUpdate();
                    if (ps!=null) {
                        ps.close();
                        ps = null;
                    }
                    sql = "update user_of_role set orders=" + borders + " where userName=? and roleCode=?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, userName);
                    ps.setString(2, roleCode);
                    conn.executePreUpdate();
                }
                re = true;
            }
        } catch (Exception e) {
        	LogUtil.getLog(getClass()).error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        return re;
    }

    /**
	 * 取出全部信息置于result中
	 */
    public Vector list(String roleCode) {
        Vector result = new Vector();
        
		String sql = "select userName, roleCode, orders from user_of_role where roleCode=? order by orders asc";
		ResultSet rs = null;
		Conn conn = new Conn(Global.getDefaultDB());
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, roleCode);
			rs = conn.executePreQuery();
			while (rs.next()) {
				RoleUser ru = new RoleUser(rs.getString(1), rs.getString(2));
				ru.setOrders(rs.getInt(3));
				result.addElement(ru);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error("list:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}

        return result;
    }    

    public ListResult listResult(String roleCode, int curPage, int pageSize) throws
            ErrMsgException {
        String sql = "select userName, roleCode, orders from user_of_role where roleCode=" + StrUtil.sqlstr(roleCode) + " order by orders asc";
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            //取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); //尽量减少内存的使用

            rs = conn.executeQuery(sql);
            if (rs == null) {
                return null;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return null;
                }
                do {
					RoleUser ru = new RoleUser(rs.getString(1), rs.getString(2));
					ru.setOrders(rs.getInt(3));
                    result.addElement(ru);
                } while (rs.next());
            }
        } catch (SQLException e) {
        	LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }    
    
    public synchronized boolean save() {
        boolean re = false;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
        	String sql = "update user_of_role set orders=? where userName=? and roleCode=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, orders);
            ps.setString(2, userName);
            ps.setString(3, roleCode);
            re = conn.executePreUpdate()==1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }    
    
    public String getUserName() {
    	return userName;
    }
    
    public String getRoleCode() {
    	return roleCode;
    }
    
    public int getOrders() {
    	return orders;
    }
    
    public void setOrders(int orders) {
    	this.orders = orders;
    }
}