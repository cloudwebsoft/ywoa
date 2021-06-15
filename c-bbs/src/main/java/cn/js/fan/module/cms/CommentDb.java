package cn.js.fan.module.cms;

import java.sql.*;
import java.util.Date;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.*;
import com.redmoon.forum.*;

public class CommentDb extends ObjectDb {
    String content,ip,nick,link;
    int id;
    int docId;
    Date addDate;

    public CommentDb() {
        super();
    }

    public CommentDb(int id) {
        this.id = id;
        init();
        load(new JdbcTemplate());
    }

    public void initDB() {
        this.tableName = "cms_comment";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new CommentCache(this);

        QUERY_CREATE =
                "insert into cms_comment (nick, link, content, ip, doc_id, add_date,id) values (?,?,?,?,?,?,?)";
        QUERY_LOAD =
                "select nick,content,link,ip,add_date,doc_id from cms_comment where id=?";
        QUERY_DEL =
                "delete from cms_comment where id=?";

        isInitFromConfigDB = false;
    }

    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new CommentDb(pk.getIntValue());
    }

    public static String getVisualGroupName(int docId) {
        return "" + docId;
    }

    public boolean create(JdbcTemplate jt) {
        boolean re = false;
        id = (int)SequenceMgr.nextID(SequenceMgr.SQ_LINK);
        try {
            re = jt.executeUpdate(this.QUERY_CREATE, new Object[] {nick, link,
                                  content, ip, new Integer(docId), "" + System.currentTimeMillis(),
                                  new Integer((int)SequenceMgr.nextID(SequenceMgr.COMMENT))}) == 1;
            if (re) {
                CommentCache mc = new CommentCache(this);
                mc.refreshCreate(getVisualGroupName(docId));
            }
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        }
        return re;
    }

    public String getContent() {
        return content;
    }

    public String getIp() {
        return ip;
    }

    public Date getAddDate() {
        return addDate;
    }

    public int getId() {
        return id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public void load(JdbcTemplate jt) {
        // Based on the id in the object, get the message data from the database.
        try {
            ResultIterator ri = jt.executeQuery(QUERY_LOAD, new Object[] {new Integer(id)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                nick = rr.getString(1);
                content = rr.getString(2);
                link = rr.getString(3);
                ip = rr.getString(4);
                addDate = DateUtil.parse(rr.getString(5));
                docId = rr.getInt(6);
                primaryKey.setValue(new Integer(id));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
    }

    public boolean del(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(this.QUERY_DEL, new Object[] {new Integer(id)})==1;
            if (re) {
                CommentCache cc = new CommentCache(this);
                cc.refreshDel(primaryKey, getVisualGroupName(docId));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return re;
    }

    public CommentDb getCommentDb(int id) {
        return (CommentDb)getObjectDb(new Integer(id));
    }

    public boolean save(JdbcTemplate jt) {
        return true;
    }
}
