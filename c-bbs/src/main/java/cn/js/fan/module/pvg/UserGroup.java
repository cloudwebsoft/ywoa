package cn.js.fan.module.pvg;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;

public class UserGroup extends ObjectDbA {
    private String code;
    private String desc;

    // 系统用户组
    public static final String EVERYONE = "Everyone";
    public static final String Administrators = "Administrators";

    final String DELPRIVILEGE = "delete from user_group_priv where group_code=?";

    public UserGroup() {
        init();
    }

    public UserGroup(String code) {
        this.code = code;
        load();
        init();
    }

    public UserGroup(String code, String desc) {
        init();

        this.code = code;
        this.desc = desc;
    }

    public void setQueryAdd() {
        QUERY_ADD = "insert into user_group (code, description) values (?,?)";
    }

    public void setQuerySave() {
        QUERY_SAVE = "update user_group set description=? where code=?";
    }

    public void setQueryList() {
        QUERY_LIST = "select code from user_group order by isSystem desc, description asc";
    }

    public void setQueryDel() {
        QUERY_DEL = "delete from user_group where code=?";
    }

    public void setQueryLoad() {
        QUERY_LOAD = "select code, description, isSystem from user_group where code=?";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String c) {
        code = c;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isSystem() {
        return system;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    private boolean system = false;

    public boolean insert(String code, String desc) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_ADD);
            pstmt.setString(1, code);
            pstmt.setString(2, desc);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            //更新文件内容
            pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setString(1, code);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    code = rs.getString(1);
                    desc = rs.getString(2);
                    system = rs.getInt(3)==1?true:false;
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            /*
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {}
                pstmt = null;
            }
*/
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, desc);
            pstmt.setString(2, code);
            re = conn.executePreUpdate()==1?true:false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean del() {
        if (isSystem())
            return false;
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_DEL);
            logger.info("del:" + QUERY_DEL);
            pstmt.setString(1, code);
            re = conn.executePreUpdate()==1?true:false;
            pstmt.close();
            pstmt = conn.prepareStatement(DELPRIVILEGE);
            pstmt.setString(1, code);
            if (re)
                re = conn.executePreUpdate()>=0?true:false;
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public UserGroup getUserGroup(String code) {
        return new UserGroup(code);
    }

    public String[] getGroupPriv(String groupCode) {
        String sql = "select priv from user_group_priv where group_code=" + StrUtil.sqlstr(groupCode);
        RMConn rmconn = new RMConn(connname);
        ResultIterator ri = null;
        String[] pv = null;
        try {
            ri = rmconn.executeQuery(sql);
            int count = ri.getRows();
            if (count>0)
                pv = new String[count];
            if (ri != null) {
                int i = 0;
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    pv[i] = rr.getString(1);
                    i++;
                }
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return pv;
    }

    public ListResult list(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        Conn conn = new Conn(connname);
        try {
            //取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
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

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return null;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return null;
                }
                do {
                    UserGroup ug = getUserGroup(rs.getString(1));
                    result.addElement(ug);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
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

    /**
     * 取出全部信息置于result中
     */
    public Vector list() throws SQLException {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        Vector result = new Vector();
        try {
            // logger.info(QUERY_LIST);
            rs = conn.executeQuery(QUERY_LIST);
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    UserGroup ug = new UserGroup(rs.getString(1));
                    result.addElement(ug);
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }
}
