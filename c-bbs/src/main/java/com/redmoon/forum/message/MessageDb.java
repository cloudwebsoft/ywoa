package com.redmoon.forum.message;

/**
 * <p>Title: 社区</p>
 * <p>Description: 社区</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 红月亮工作室</p>
 * @author bluewind
 * @version 1.0
 */

import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.*;
import com.redmoon.forum.Config;
import com.redmoon.forum.person.*;

public class MessageDb extends ObjectDb {
    int id;
    public String title, content, receiver, sender, ip;
    java.util.Date rq;
    boolean readed = false;

    public static String USER_SYSTEM = "system"; // Global.AppName;

    static {
        Config cfg = Config.getInstance();
        USER_SYSTEM = cfg.getProperty("forum.message_sender");
    }

    public static final int TYPE_SYSTEM = 10;
    public static final int TYPE_USER = 0;
    public int type = TYPE_USER;

    public MessageDb() {
        init();
    }

    public MessageDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getNewMsgCount(String receiver) {
        MessageCache mc = new MessageCache(this);
        return mc.getNewMsgCount(receiver);
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void clear() { // throws Throwable
        try {
            finalize();
        } catch (java.lang.Throwable e) {
            logger.error("clear: " + e.getMessage());
        }
    }

    public boolean AddMsg(HttpServletRequest request) throws
            ErrMsgException {
        MessageForm mf = new MessageForm(request, this);
        mf.checkCreate();
        this.sender = USER_SYSTEM;
        // 群发
        String[] receivers = ParamUtil.getParameters(request, "receivers");
        for (int i = 0; i < receivers.length; i++) {
            if (receivers[i].equals("isToAll")){
                UserDb ud = new UserDb();
                Iterator ir = ud.list().iterator();
                while (ir.hasNext()) {
                    ud = (UserDb)ir.next();
                    this.receiver = ud.getName();
                    create();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                    }
                }
                return true;
            }else {
                if(receivers[i].equals("boardManager")){
                    String sql = "select distinct name from sq_boardmanager";
                    UserDb ud = new UserDb();
                    Iterator ir = ud.list(sql).iterator();
                    while (ir.hasNext()) {
                        ud = (UserDb) ir.next();
                        this.receiver = ud.getName();
                        create();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ex) {
                        }
                    }
                }else{
                    String sql = "select name from sq_user where group_code=" +StrUtil.sqlstr(receivers[i]);
                    UserDb ud = new UserDb();
                    Iterator ir = ud.list(sql).iterator();
                    while (ir.hasNext()) {
                        ud = (UserDb) ir.next();
                        this.receiver = ud.getName();
                        create();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
        }

        String receiver = ParamUtil.get(request, "receiver");
        String[] r = StrUtil.split(receiver, ",");
        if( r != null){
            int len = r.length;
            for (int i = 0; i < len; i++) {
                UserDb user = new UserDb();
                user = user.getUserDbByNick(r[i]);
                if (user == null || !user.isLoaded()) {
                    String s = SkinUtil.LoadString(request,
                            "res.forum.message.MessageDb", "err_receiver_none");
                    s = s.replaceFirst("\\$u", r[i]);
                    throw new ErrMsgException(s);
                } else {
                    this.receiver = user.getName();
                    create();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {

                    }

                }
            }
        }
        return true;
    }

    public boolean AddMsg(HttpServletRequest request,
                          String sender) throws
            ErrMsgException {
        MessageForm mf = new MessageForm(request, this);
        mf.checkCreate();
        this.sender = sender;
        // 群发
        boolean isToAll = ParamUtil.getBoolean(request, "isToAll", false);
        if (isToAll) {
            UserDb ud = new UserDb();
            Iterator ir = ud.list().iterator();
            while (ir.hasNext()) {
                ud = (UserDb)ir.next();
                receiver = ud.getName();
                create();
            }
            return true;
        }
        String[] r = StrUtil.split(receiver, ",");
        int len = r.length;
        for (int i=0; i<len; i++) {
            UserDb user = new UserDb();
            user = user.getUserDbByNick(r[i]);
            if (user==null || !user.isLoaded()) {
                String s = SkinUtil.LoadString(request, "res.forum.message.MessageDb", "err_receiver_none");
                s = s.replaceFirst("\\$u", r[i]);
                throw new ErrMsgException(s);
            }
            else {
                receiver = user.getName();
                create();
            }
        }
        return true;
    }

    public boolean delMsg(String[] ids) throws ResKeyException {
        int len = ids.length;
        String str = "";
        for (int i = 0; i < len; i++)
            if (str.equals(""))
                str += ids[i];
            else
                str += "," + ids[i];
        str = "(" + str + ")";
        String sql = "select id from message where id in " + str;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            ResultSet rs = conn.executeQuery(sql);
            if (rs!=null) {
                while (rs.next()) {
                    getMessageDb(rs.getInt(1)).del();
                }
            }
        } catch (Exception e) {
            logger.error("delMsg:" + e.getMessage());
            throw new ResKeyException(SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public boolean isReaded() {
        return readed;
    }

    public MessageDb getMessageDb(int id) throws ErrMsgException {
        return (MessageDb)getObjectDb(new Integer(id));
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public java.util.Date getRq() {
        return rq;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int)SequenceMgr.nextID(SequenceMgr.SQ_SHORT_MESSAGE);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, sender);
            ps.setString(4, receiver);
            ps.setInt(5, type);
            ps.setString(6, ip);
            ps.setString(7, "" + System.currentTimeMillis());
            ps.setInt(8, id);
            if (!(conn.executePreUpdate() == 1 ? true : false))
                return false;
            MessageCache mc = new MessageCache(this);
            mc.refreshNewCountOfReceiver(receiver);
            mc.refreshCreate();
        } catch (Exception e) {
            logger.error("create: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        MessageCache uc = new MessageCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return (MessageDb) uc.getObjectDb(pk);
    }

    public synchronized boolean del() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;

            if (re) {
                MessageCache mc = new MessageCache(this);
                mc.refreshDel(primaryKey);
            }
        } catch (Exception e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            MessageCache uc = new MessageCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshDel(primaryKey);
        }
        return re;
    }

    public int getObjectCount(String sql) {
        MessageCache uc = new MessageCache(this);
        return uc.getObjectCount(sql);
    }

    public int getMessageCount(String sql) {
        return getObjectCount(sql);
    }

    public Object[] getObjectBlock(String query, int startIndex) {
        MessageCache dcm = new MessageCache(this);
        return dcm.getObjectBlock(query, startIndex);
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new MessageDb(pk.getIntValue());
    }

    public void setQueryCreate() {
        this.QUERY_CREATE =
                "insert into message (title,content,sender,receiver,type,ip,rq,id) values (?,?,?,?,?,?,?,?)";
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
                "update message set isreaded=? where id=?";
    }

    public void setQueryDel() {
        this.QUERY_DEL = "delete from message where id=?";
    }

    public void setQueryLoad() {
        QUERY_LOAD = "select title,content,sender,receiver,rq,ip,type,isreaded from message where id=?";
    }

    public void setQueryList() {
        QUERY_LIST = "select id from message order by isreaded asc,rq desc";
    }

    public synchronized boolean save() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setInt(1, readed?1:0);
            ps.setInt(2, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            MessageCache uc = new MessageCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshSave(primaryKey);
            uc.refreshNewCountOfReceiver(receiver);
        }
        return re;
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
    }

    public void clearMessageExpired(int expireDay) {
        long d = expireDay * 24 * 60 * 60 * 1000;
        long dt = System.currentTimeMillis() - d;
        String sql = "select id from message where rq < ?";
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "" + dt);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    MessageDb md = getMessageDb(rs.getInt(1));
                    md.del();
                }
            }
        } catch (Exception e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public synchronized void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                title = rs.getString(1);
                content = StrUtil.getNullString(rs.getString(2));
                sender = StrUtil.getNullString(rs.getString(3));
                receiver = StrUtil.getNullString(rs.getString(4));
                rq = DateUtil.parse(rs.getString(5));
                ip = rs.getString(6);
                type = rs.getInt(7);
                readed = rs.getInt(8) == 1 ? true : false;
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (Exception e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void setReaded(boolean readed) {
        this.readed = readed;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setType(int type) {
        this.type = type;
    }
}
