package com.redmoon.forum.util;

import cn.js.fan.db.*;
import java.sql.*;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import cn.js.fan.util.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.*;
import java.util.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;


/**
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
public class ForumStatisticDb {
    public ForumStatisticDb() {
         connname = Global.getDefaultDB();
    }

    public int getMemberTotal() {
        String sql = "select count(*) from sq_user";
        int memberTotal = 0;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = conn.executePreQuery();
            if (rs.next()) {
                memberTotal = rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.getLogger("load:" + e.getMessage());
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
        return memberTotal;
    }

    public int getAllCount() {
        String sql = "select count(*) from sq_message";
        int addCount = 0;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = conn.executePreQuery();
            if (rs.next()) {
                addCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.getLogger("load:" + e.getMessage());
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
        return addCount;
    }

    public int getAddCount() {
        String sql = "select count(*) from sq_message where replyid = -1";
        int addCount = 0;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = conn.executePreQuery();
            if (rs.next()) {
                addCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.getLogger("load:" + e.getMessage());
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
        return addCount;
    }

    /**
     * 重新统计各版的主题贴数、总贴数，重置最后发贴或回复
     * @throws ErrMsgException
     */
    public void initBoardPost() throws ErrMsgException {
        Directory dir = new Directory();
        Leaf rootLeaf = dir.getLeaf(Leaf.CODE_ROOT);
        initBoardTree(rootLeaf);
    }

    // 遍历根结点为leaf的树
    void initBoardTree(Leaf leaf) throws ErrMsgException {
        initBoardLeaf(leaf);

        Directory dir = new Directory();
        Vector children = dir.getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        Iterator ri = children.iterator();
        while (ri.hasNext()) {
            Leaf childlf = (Leaf) ri.next();
            initBoardTree(childlf);
        }
    }

    /**
     * 生新统计版块的主题贴数、总贴数，重置最后发贴
     **/
    void initBoardLeaf(Leaf leaf) throws ErrMsgException {
        String sql = "select count(*) from sq_message where boardcode=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {leaf.getCode()});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                int count = rr.getInt(1);
                leaf.setPostCount(count);
            }
            sql = "select count(*) from sq_thread where boardcode=?";
            ri = jt.executeQuery(sql, new Object[] {leaf.getCode()});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                int count = rr.getInt(1);
                leaf.setTopicCount(count);
            }

            sql = "select id from sq_message where boardcode=? order by id desc";
            ri = jt.executeQuery(sql, new Object[] {leaf.getCode()}, 1, 1);
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                long id = rr.getLong(1);
                leaf.setAddId(id);
            }
            leaf.update();
        }
        catch (SQLException e) {
            throw new ErrMsgException(e.getMessage());
        }
    }

    public int getAddCount(String name){
        String sql = "select count(*) from sq_message where name = ? and replyid = -1";
        int addCount = 0;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,name);
            rs = conn.executePreQuery();
            if (rs.next()) {
                addCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.getLogger("load:" + e.getMessage());
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
        return addCount;
    }

    public int getEliteCount() {
        String sql = "select count(*) from sq_message where replyid = -1 and iselite = 1";
        int eliteCount = 0;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = conn.executePreQuery();
            if (rs.next()) {
                eliteCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.getLogger("load:" + e.getMessage());
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
        return eliteCount;
    }

    public int getEliteCount(String name) {
        String sql =
                "select count(*) from sq_message where name=? and replyid = -1 and iselite = 1";
        int eliteCount = 0;
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,name);
            rs = conn.executePreQuery();
            if (rs.next()) {
                eliteCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.getLogger("load:" + e.getMessage());
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
        return eliteCount;
    }

    public boolean save() throws ErrMsgException {
        boolean re = false;
        int addCount = 0,eliteCount = 0;

        ForumDb fd = new ForumDb();
        fd = fd.getForumDb();
        fd.setUserCount(getMemberTotal());
        fd.setTopicCount(getAddCount());
        fd.setPostCount(getAllCount());
        re = fd.save();
        if (!re)
            return re;

        UserDb ud = new UserDb();
        Vector vt = ud.list();
        Iterator ir = vt.iterator();
        while(ir.hasNext()){
            ud = (UserDb)ir.next();
            addCount = getAddCount(ud.getName());
            eliteCount = getEliteCount(ud.getName());
            ud.setAddCount(addCount);
            ud.setEliteCount(eliteCount);
            re = ud.save();
            if (!re)
                break;
        }

        initBoardPost();

        return re;
    }


    public boolean save(String name){
        int addCount = 0, eliteCount = 0;
        boolean re = false;
        addCount = getAddCount(name);
        eliteCount = getEliteCount(name);
        UserDb ud = new UserDb();
        ud = ud.getUser(name);
        ud.setAddCount(addCount);
        ud.setEliteCount(eliteCount);
        re = ud.save();
        return re;
    }


    private int id;
    private String connname;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


}
