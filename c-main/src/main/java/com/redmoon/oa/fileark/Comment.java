package com.redmoon.oa.fileark;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;

import java.sql.Timestamp;
import java.util.Vector;

public class Comment implements java.io.Serializable {
    String content,ip,nick,link;
    int id, doc_id;
    String add_date;

    String connname = "";

    private static final String INSERT =
            "INSERT into cms_comment (nick, link, content, ip, doc_id, add_date) VALUES (?,?,?,?,?,?)";
    private static final String LOAD =
            "SELECT id,nick,content,link,ip,add_date,doc_id from cms_comment WHERE id=?";
    private static final String DEL =
            "delete FROM cms_comment WHERE id=?";

    public Comment() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Directory:默认数据库名为空！");
    }

    public Comment(int id) {
        this.id = id;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            LogUtil.getLog(getClass()).info("Directory:默认数据库名为空！");
        load(id);
    }

    public void renew() {
    }

    public boolean insert(int doc_id, String nick, String link, String content, String ip) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(INSERT);
            pstmt.setString(1, nick);
            pstmt.setString(2, link);
            pstmt.setString(3, content);
            pstmt.setString(4, ip);
            pstmt.setInt(5, doc_id);
            pstmt.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }

    public String getContent() {
        return content;
    }

    public String getIp() {
        return ip;
    }

    public String getAddDate() {
        return add_date;
    }

    public int getId() {
        return id;
    }

    public String getNick() {
        return nick;
    }

    public String getLink() {
        return link;
    }

    public int getDocId() {
        return doc_id;
    }

    public void load(int cmtId) {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(LOAD);
            pstmt.setInt(1, cmtId);
            rs = conn.executePreQuery();
            if (rs!=null) {
                if (rs.next()) {
                    id = rs.getInt(1);
                    nick = rs.getString(2);
                    content = rs.getString(3);
                    link = rs.getString(4);
                    ip = rs.getString(5);
                    add_date = rs.getString(6).substring(0,16);
                    doc_id = rs.getInt(7);
                }
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        finally {
            if (rs!=null) {
                try { rs.close(); } catch (SQLException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
                rs = null;
            }
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
    }

    public boolean del(int id) {
        Conn conn = new Conn(connname);
        boolean re = true;
        try {
            PreparedStatement pstmt = conn.prepareStatement(DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate()==1?true:false;
        }
        catch (SQLException e) {
            re = false;
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close(); conn = null;
            }
        }
        return re;
    }
    
	
    public ListResult listResult(String sql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();
        ListResult lr = new ListResult();
        lr.setResult(result);
        lr.setTotal(total);
        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(sql);
            // 取得总记录条数
            PreparedStatement ps = conn.prepareStatement(countsql);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (ps!=null) {
                ps.close();
                ps = null;
            }

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages)
                curPage = totalpages;
            if (curPage <= 0)
                curPage = 1;

            ps = conn.prepareStatement(sql);

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

            rs = conn.executePreQuery();
            if (rs == null) {
                return lr;
            } else {
                Comment cmt;
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                	cmt = new Comment(rs.getInt(1));
                    result.addElement(cmt);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listResult:" + StrUtil.trace(e));
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }    
}
