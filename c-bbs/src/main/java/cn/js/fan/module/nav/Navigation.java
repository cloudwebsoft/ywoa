package cn.js.fan.module.nav;

import java.io.*;
import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;

public class Navigation implements ITagSupport,Serializable {
    String name;
    String connname;
    String link;
    int orders;
    String newName;

    public static final int STYLE_MULTI_TREE = 0; // 树形
    public static final int STYLE_MULTI_FLAT = 1; // 子项显示在主项下方的div中

    public static final String TYPE_CMS = "0";
    public static final String TYPE_FORUM = "1";
    public static final String TYPE_BLOG = "2";

    public static final String LINK_COLUMN = "column"; // 链接

    transient Logger logger = Logger.getLogger(Navigation.class.getName());

    final String INSERT =
            "insert into cws_cms_nav (name, link, orders, color,target,nav_type,code) values (?,?,?,?,?,?,?)";
    final String STORE =
            "update cws_cms_nav set name=?,link=?,color=?,target=? where code=? and nav_type=?";
    final String LOAD =
            "select name,link,orders,color,target from cws_cms_nav where code=? and nav_type=?";
    final String GETMAXORDERS =
            "select max(orders) from cws_cms_nav where nav_type=?";

    public Navigation() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:connname is empty.");
    }

    public Navigation(String code, String type) {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:connname is empty.");
        this.code = code;
        this.type = type;
        load(code);
    }

    public String getName() {
        return name;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int o) {
        this.orders = o;
    }

    public void setNewName(String n) {
        this.newName = n;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String get(String field) {
        if (field.equals("name"))
            return name;
        else if (field.equals("link"))
            return link;
        else if (field.equals("orders"))
            return "" + orders;
        else
            return "";
    }

    public String getLink() {
        return this.link;
    }

    public String getColor() {
        return color;
    }

    public String getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public void setLink(String lk) {
        this.link = lk;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean insert(String name, String link, String color, String target, String type) {
        code = RandomSecquenceCreator.getId(20);
        orders = getMaxOrders(type) + 1;
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(INSERT);
            pstmt.setString(1, name);
            pstmt.setString(2, link);
            pstmt.setInt(3, orders);
            pstmt.setString(4, color);
            pstmt.setString(5, target);
            pstmt.setString(6, type);
            pstmt.setString(7, code);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error("insert:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean store() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(STORE);
            pstmt.setString(1, newName);
            pstmt.setString(2, link);
            pstmt.setString(3, color);
            pstmt.setString(4, target);
            pstmt.setString(5, code);
            pstmt.setString(6, type);
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

    public void load(String navCode) {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            // 更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(LOAD);
            pstmt.setString(1, navCode);
            pstmt.setString(2, type);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    name = rs.getString(1);
                    link = rs.getString(2);
                    orders = rs.getInt(3);
                    color = rs.getString(4);
                    target = StrUtil.getNullStr(rs.getString(5));
                }
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
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
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            String sql = "delete from cws_cms_nav where code=" + StrUtil.sqlstr(code) + " and nav_type=" + StrUtil.sqlstr(type);
            String sql1 = "update cws_cms_nav set orders=orders-1 where orders>" +
                          orders + " and nav_type=" + StrUtil.sqlstr(type);
            // System.out.println(getClass() + " sql=" + sql);
            conn.beginTrans();
            conn.executeUpdate(sql);
            conn.executeUpdate(sql1);
            conn.commit();
            re = true;
        } catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public int getMaxOrders(String type) {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        int maxorders = -1;
        try {
            PreparedStatement pstmt = conn.prepareStatement(GETMAXORDERS);
            pstmt.setString(1, type);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return maxorders;
    }

    public boolean move(String direction) {
        // logger.info(direction);
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            if (direction.equals("up")) {
                if (orders == 0)
                    return true;
                String sql = "update cws_cms_nav set orders=orders+1 where orders=" +
                             (orders - 1) + " and nav_type=" + StrUtil.sqlstr(type);
                conn.executeUpdate(sql);
                sql = "update cws_cms_nav set orders=orders-1 where code=" +
                        StrUtil.sqlstr(code) + " and nav_type=" + StrUtil.sqlstr(type);
                conn.executeUpdate(sql);
                re = true;
            } else {
                int maxorders = getMaxOrders(type);
                if (orders == maxorders) {
                    return true;
                } else {
                    String sql = "update cws_cms_nav set orders=orders-1 where orders=" +
                                 (orders + 1) + " and nav_type=" + StrUtil.sqlstr(type);
                    conn.executeUpdate(sql);
                    sql = "update cws_cms_nav set orders=orders+1 where code=" +
                            StrUtil.sqlstr(code) + " and nav_type=" + StrUtil.sqlstr(type);
                    conn.executeUpdate(sql);
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
        return re;
    }

    public Vector getAllNavName(String type) {
        String sql = "select code from cws_cms_nav where nav_type=? order by orders";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        Vector v = null;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, type);
            rs = conn.executePreQuery();
            if (rs!=null) {
                v = new Vector();
                while (rs.next()) {
                    v.addElement(rs.getString(1));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return v;
    }

    private String color = "";
    private String target;
    private String type = TYPE_CMS;
    private String code;

}
