package com.redmoon.oa.emailpop3;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;

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
public class EmailPop3Db extends ObjectDb {
    private int id;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;
    
    private boolean ssl = false;
    private boolean isDefault = false;

    public EmailPop3Db() {
        init();
    }

    public EmailPop3Db(int id) {
        this.id = id;
        init();
        load();
    }

    public EmailPop3Db getEmailPop3Db(String userName, String emailAddress) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            String sql = "select id from " + tableName +
                         " where userName=? and email=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            ps.setString(2, emailAddress);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                id = rs.getInt(1);
                return getEmailPop3Db(id);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("loadByUserEmail: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "email_pop3";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new EmailPop3Cache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (userName,email,emailUser,emailPwd,server,port,smtpPort,server_pop3,is_delete,id,is_ssl,is_default) values (?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set email=?,emailUser=?,emailPwd=?,server=?,port=?,smtpPort=?,server_pop3=?,is_delete=?,is_ssl=?,is_default=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select userName,email,emailUser,emailPwd,server,port,smtpPort,server_pop3,is_delete,is_ssl,is_default from " + tableName + " where id=?";
    }

    public EmailPop3Db getEmailPop3Db(int id) {
        return (EmailPop3Db)getObjectDb(new Integer(id));
    }

    public Vector getEmailPop3DbOfUser(String userName) {
        String sql = "select id from email_pop3 where userName=" + StrUtil.sqlstr(userName)+" order by is_default desc";
        return list(sql);
    }

    /**
     * 根据Email取得EmailPop3Db
     * @param emailAddr String
     * @return EmailPop3Db
     */
    public EmailPop3Db getEmailPop3DbByEmail(String emailAddr) {
        String sql = "select id from email_pop3 where email=" + StrUtil.sqlstr(emailAddr);
        Iterator ir = null;
        try {
            ir = listResult(sql, 1, 1).getResult().iterator();
            if (ir.hasNext()) {
                return (EmailPop3Db)ir.next();
            }
        } catch (ErrMsgException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }

        return null;
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, userName);
            ps.setString(2, email);
            ps.setString(3, emailUser);
            ps.setString(4, emailPwd);
            ps.setString(5, server);
            ps.setInt(6, port);
            ps.setInt(7, smtpPort);
            ps.setString(8, serverPop3);
            ps.setInt(9, delete?1:0);
            id = (int)SequenceManager.nextID(SequenceManager.EMAIL_POP3);
            ps.setInt(10, id);
            ps.setInt(11, ssl?1:0);
            ps.setInt(12, isDefault?1:0);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                EmailPop3Cache rc = new EmailPop3Cache(this);
                rc.refreshCreate();
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
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
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                EmailPop3Cache rc = new EmailPop3Cache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);

                // 删除其email表中所有的草稿或邮件
                MailMsgDb mmd = new MailMsgDb();
                mmd.delOfSender(email);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("del: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new EmailPop3Db(pk.getIntValue());
    }

    /**
     * load
     *
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
        // QUERY_LOAD = "select name,reason,direction,type,myDate from " + tableName + " where id=?";
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                // QUERY_LOAD = "select userName,email,emailUser,emailPwd,server,port from " + tableName + " where id=?";
                userName = rs.getString(1);
                email = rs.getString(2);
                emailUser = rs.getString(3);
                emailPwd = rs.getString(4);
                server = rs.getString(5);
                port = rs.getInt(6);
                smtpPort = rs.getInt(7);
                serverPop3 = rs.getString(8);
                delete = rs.getInt(9)==1;
                
                ssl = rs.getInt(10)==1;
                isDefault = rs.getInt(11)==1;

                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
                rs = null;
            }
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean save() throws ErrMsgException {
        Conn conn = new Conn(connname);
         boolean re = false;
         try {
             // QUERY_SAVE = "update " + tableName + " set email=?,emailUser=?,emailPwd=?,server=?,port=? where id=?";
             PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
             ps.setString(1, email);
             ps.setString(2, emailUser);
             ps.setString(3, emailPwd);
             ps.setString(4, server);
             ps.setInt(5, port);
             ps.setInt(6, smtpPort);
             ps.setString(7, serverPop3);
             ps.setInt(8, delete?1:0);
             ps.setInt(9, ssl?1:0);
             ps.setInt(10, isDefault?1:0);
             ps.setInt(11, id);
             re = conn.executePreUpdate()==1?true:false;

             if (re) {
                 EmailPop3Cache rc = new EmailPop3Cache(this);
                 primaryKey.setValue(new Integer(id));
                 rc.refreshSave(primaryKey);
             }
         } catch (SQLException e) {
             LogUtil.getLog(getClass()).error("save: " + e.getMessage());
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
                    result.addElement(getEmailPop3Db(rs.getInt(1)));
                }
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

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    EmailPop3Db ug = getEmailPop3Db(rs.getInt(1));
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
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public String getEmailPwd() {
        return emailPwd;
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getServerPop3() {
        return serverPop3;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public void setEmailPwd(String emailPwd) {
        this.emailPwd = emailPwd;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public void setServerPop3(String serverPop3) {
        this.serverPop3 = serverPop3;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public boolean isSsl() {
		return ssl;
	}

	private String userName;
    private String email;
    private String emailUser;
    private String emailPwd;
    private String server;
    private int port;
    private int smtpPort;
    private String serverPop3;
    private boolean delete = true;

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
}
