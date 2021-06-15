package com.redmoon.forum.plugin.entrance;

import java.sql.*;
import java.util.Date;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.*;
import com.redmoon.forum.Config;

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
public class VIPCardDb extends ObjectDb {
    public String KEY = "bluewindBlueWindBLUEWIND";

    public VIPCardDb() {
        init();
    }

    public VIPCardDb(String id) {
        this.id = id;
        init();
        load();
    }

    public String getKey() {
        return KEY;
    }

    public void init() {
        super.init();
        Config cfg = Config.getInstance();
        KEY = cfg.getKey();
    }

    public synchronized String getNextID() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        String sql = "select id from " + tableName + " order by id desc";
        String strId = "1";
        try {
            conn.setMaxRows(1);
            rs = conn.executeQuery(sql);
            conn.setFetchSize(1);
            if (rs.next()) {
                strId = rs.getString(1);
                int id = Integer.parseInt(strId);
                id++;
                strId = "" + id;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        strId = StrUtil.PadString(strId, '0', 6, true);
        return strId;
    }

    public boolean create() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, userName);
            ps.setString(2, DateUtil.toLongString(beginDate));
            ps.setString(3, DateUtil.toLongString(endDate));
            ps.setDouble(4, fee);
            ps.setString(5, kind);
            ps.setString(6, pwd);
            ps.setString(7, pwdRaw);
            ps.setString(8, id);
            ps.setInt(9, valid?1:0);
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     * @throws ResKeyException
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public boolean del() throws ErrMsgException, ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setString(1, id);
            rowcount = conn.executePreUpdate();
            VIPCardCache uc = new VIPCardCache(this);
            primaryKey.setValue(id);
            uc.refreshDel(primaryKey);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    /**
     *
     * @param pk Object
     * @return Object
     * @todo Implement this cn.js.fan.base.ObjectDb method
     */
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new VIPCardDb(pk.getStrValue());
    }

    public VIPCardDb getVIPCardDb(String id) {
        return (VIPCardDb)getObjectDb(id);
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
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setString(1, id);
            primaryKey.setValue(id);
            rs = conn.executePreQuery();
            if (rs.next()) {
                beginDate = DateUtil.parse(rs.getString(1));
                endDate = DateUtil.parse(rs.getString(2));
                fee = rs.getInt(3);
                userName = rs.getString(4);
                kind = rs.getString(5);
                pwd = rs.getString(6);
                pwdRaw = rs.getString(7);
                fingerPrint = rs.getString(8);
                useFingerPrint = rs.getInt(9)==1?true:false;
                valid = rs.getInt(10)==1?true:false;
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
        finally {
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
    public boolean save() throws ErrMsgException, ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, DateUtil.toLongString(beginDate));
            ps.setString(2, DateUtil.toLongString(endDate));
            ps.setDouble(3, fee);
            ps.setString(4, userName);
            ps.setString(5, kind);
            ps.setString(6, pwd);
            ps.setString(7, pwdRaw);
            ps.setString(8, fingerPrint);
            ps.setInt(9, useFingerPrint?1:0);
            ps.setInt(10, valid?1:0);
            ps.setString(11, id);
            rowcount = conn.executePreUpdate();
            VIPCardCache uc = new VIPCardCache(this);
            primaryKey.setValue(id);
            uc.refreshSave(primaryKey);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        ListResult lr = new ListResult();
        Vector result = new Vector();
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
                conn.setMaxRows(curPage * pageSize); //尽量减少内存的使用

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
                    VIPCardDb cmm = getVIPCardDb(rs.getString(1));
                    result.addElement(cmm);
                } while (rs.next());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("listResult:DataBase error！");
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

    public void initDB() {
        tableName = "plugin_entrance_vip_card";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_STRING);
        objectCache = new VIPCardCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT " + tableName + " (userName, beginDate, endDate, fee, kind, pwd, pwdRaw, id, isValid) VALUES (?,?,?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT beginDate,endDate,fee,userName,kind,pwd,pwdRaw,fingerPrint,isUseFingerPrint,isValid FROM " + tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET beginDate=?, endDate=?, fee=?,userName=?,kind=?,pwd=?,pwdRaw=?,fingerPrint=?,isUseFingerPrint=?,isValid=? WHERE id=?";
        isInitFromConfigDB = false;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPwd(String pwd) {
        try {
            this.pwd = SecurityUtil.MD5(pwd);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        pwdRaw = ThreeDesUtil.encrypt2hex(KEY, pwd);
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public void setUseFingerPrint(boolean useFingerPrint) {
        this.useFingerPrint = useFingerPrint;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUserName() {
        return userName;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public double getFee() {
        return fee;
    }

    public String getKind() {
        return kind;
    }

    public String getId() {
        return id;
    }

    public String getPwd() {
        return pwd;
    }

    public String getPwdRaw() {
        return ThreeDesUtil.decrypthexstr(KEY, pwdRaw);
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public boolean isUseFingerPrint() {
        return useFingerPrint;
    }

    public boolean isValid() {
        return valid;
    }

    public String[] getKinds() {
        if (kind==null)
            return new String[0];
        return kind.split("\\,");
    }

    private String userName;
    private Date beginDate;
    private Date endDate;
    private double fee;
    private String kind;

    private String id;
    private String pwd;
    private String pwdRaw;
    private String fingerPrint;
    private boolean useFingerPrint = false;
    private boolean valid;
}
