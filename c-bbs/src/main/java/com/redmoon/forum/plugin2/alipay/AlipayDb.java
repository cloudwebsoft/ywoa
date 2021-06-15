package com.redmoon.forum.plugin2.alipay;

import java.sql.*;
import cn.js.fan.db.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.base.ObjectDb;
import cn.js.fan.util.StrUtil;
import javax.servlet.http.HttpServletRequest;

public class AlipayDb extends ObjectDb {
    String connname;
    Logger logger = Logger.getLogger(AlipayDb.class.getName());

    public AlipayDb() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("AlipayDb:DB is empty！");
        init();
    }

    public String getTransportDesc(HttpServletRequest request) {
        if (transport==1) {
            String str =SkinUtil.LoadString(request,"res.forum.plugin.auction","buyers"); //买家承担运费
            if (!ordinary.equals(""))
                str += " 平邮：" + ordinary + " 元";
            if (!express.equals(""))
                str += " 快递：" + express + " 元";
            return str;
        }
        else if (transport==2)
            return SkinUtil.LoadString(request,"res.forum.plugin.auction","sellers");//卖家承担运费
        else
            return SkinUtil.LoadString(request,"res.forum.plugin.auction","virtualGoods");//虚拟物品不需邮递
    }

    public AlipayDb(long msgRootId){
        this.msgRootId = msgRootId;
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("AlipayDb:DB is empty！");
        load();
        init();
    }

    public void initDB() {
        objectCache = new AlipayCache(this);
        tableName = "plugin2_alipay";
        primaryKey = new PrimaryKey("msgRootId", PrimaryKey.TYPE_LONG);

        QUERY_LOAD =
            "SELECT alipay_seller,alipay_subject,alipay_price,alipay_transport,alipay_demo,alipay_ww,alipay_qq,alipay_ordinary,alipay_express FROM " + tableName + " WHERE msgRootId=?";
        QUERY_SAVE =
            "update " + tableName + " set alipay_seller=?,alipay_subject=?,alipay_price=?,alipay_transport=?,alipay_demo=?,alipay_ww=?,alipay_qq=?,alipay_ordinary=?,alipay_express=? where msgRootId=?";
        QUERY_DEL = "delete from " + tableName + " where msgRootId=?";
        QUERY_CREATE = "insert into " + tableName + " (alipay_seller,alipay_subject,alipay_price,alipay_transport,alipay_demo,alipay_ww,alipay_qq,alipay_ordinary,alipay_express,msgRootId) values (?,?,?,?,?,?,?,?,?,?)";
        QUERY_LIST = "select msgRootId from " + tableName + " order by msgRootId desc";
        isInitFromConfigDB = false;
    }

    public boolean create() throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, seller);
            ps.setString(2, subject);
            ps.setString(3, price);
            ps.setInt(4, transport);
            ps.setString(5, demo);
            ps.setString(6, ww);
            ps.setString(7, qq);
            ps.setString(8, ordinary);
            ps.setString(9, express);
            ps.setLong(10, msgRootId);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                AlipayCache mc = new AlipayCache(this);
                mc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("error_insert！");//插入时出错
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new AlipayDb(pk.getLongValue());
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        boolean re = false;
        try {
            pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setLong(1, msgRootId);
            re = conn.executePreUpdate() > 0 ? true : false;
            if (re) {
                AlipayCache bc = new AlipayCache(this);
                primaryKey.setValue(new Long(msgRootId));
                bc.refreshDel(primaryKey);
            }
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

    public boolean save() {
        Conn conn = new Conn(connname);
        PreparedStatement ps = null;
        boolean re = false;
        try {
            ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, seller);
            ps.setString(2, subject);
            ps.setString(3, price);
            ps.setInt(4, transport);
            ps.setString(5, demo);
            ps.setString(6, ww);
            ps.setString(7, qq);
            ps.setString(8, ordinary);
            ps.setString(9, express);
            ps.setLong(10, msgRootId);
            re = conn.executePreUpdate() > 0 ? true : false;
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            AlipayCache bc = new AlipayCache(this);
            primaryKey.setValue(new Long(msgRootId));
            bc.refreshSave(primaryKey);
        }
        return re;
    }

    public void load() {
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setLong(1, msgRootId);
            rs = conn.executePreQuery();
            if (rs.next()) {
                seller = rs.getString(1);
                subject = rs.getString(2);
                price = rs.getString(3);
                transport = rs.getInt(4);
                demo = rs.getString(5);
                ww = rs.getString(6);
                qq = rs.getString(7);
                ordinary = StrUtil.getNullString(rs.getString(8));
                express = StrUtil.getNullString(rs.getString(9));
                primaryKey.setValue(new Long(msgRootId));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public AlipayDb getAlipaydDb(long id) {
        return (AlipayDb)getObjectDb(new Long(id));
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setTransport(int transport) {
        this.transport = transport;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    public void setWw(String ww) {
        this.ww = ww;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public void setOrdinary(String ordinary) {
        this.ordinary = ordinary;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    public void setMsgRootId(long msgRootId) {
        this.msgRootId = msgRootId;
    }

    public String getSeller() {
        return seller;
    }

    public String getSubject() {
        return subject;
    }

    public String getPrice() {
        return price;
    }

    public int getTransport() {
        return transport;
    }

    public String getDemo() {
        return demo;
    }

    public String getWw() {
        return ww;
    }

    public String getQq() {
        return qq;
    }

    public String getOrdinary() {
        return ordinary;
    }

    public String getExpress() {
        return express;
    }

    public long getMsgRootId() {
        return msgRootId;
    }

    private String seller;
    private String subject;
    private String price;
    private int transport;
    private String demo;
    private String ww;
    private String qq;
    private String ordinary;
    private String express;
    private long msgRootId;

}
