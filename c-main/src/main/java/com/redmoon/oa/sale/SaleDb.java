package com.redmoon.oa.sale;

import java.sql.*;
import java.util.*;
import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
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
public class SaleDb extends ObjectDb {
//customer,ifShare,customerName,customerCode,customerShort,phone
//faxNo,customerWWW,email,customerArea,postalcode,constomerAdd
//source,kind,sellMode,attribute,enterType,enterMemo,demo
    private int id;
    private String customer;
    private String ifShare;
    private String customerName;
    private String customerCode;
    private String customerShort;
    private String phone;
    private String faxNo;
    private String customerWWW;
    private String email;
    private String customerArea;
    private String postalcode;
    private String constomerAdd;
    private String source;
    private String kind;
    private String sellMode;
    private String attribute;
    private String enterType;
    private String enterMemo;
    private String demo;

    public SaleDb() {
        init();
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public SaleDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public String getCustomer() {
        return customer;
    }

    public String isIfShare() {
        return ifShare;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public String getCustomerShort() {
        return customerShort;
    }

    public String getPhone() {
        return phone;
    }

    public String getFaxNo() {
        return faxNo;
    }

    public String getCustomerWWW() {
        return customerWWW;
    }

    public String getEmail() {
        return email;
    }

    public String getCustomerArea() {
        return customerArea;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public String getConstomerAdd() {
        return constomerAdd;
    }

    public String getSource() {
        return source;
    }

    public String getKind() {
        return kind;
    }

    public String getSellMode() {
        return sellMode;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getEnterType() {
        return enterType;
    }

    public String getEnterMemo() {
        return enterMemo;
    }

    public String getDemo() {
        return demo;
    }

    public void initDB() {
        tableName = "sale";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new SaleCache(this);
        isInitFromConfigDB = false;
        //customer,ifShare,customerName,customerCode,customerShort,phone
        //faxNo,customerWWW,email,customerArea,postalcode,constomerAdd
        //source,kind,sellMode,attribute,enterType,enterMemo,demo

        QUERY_CREATE =
                "insert into " + tableName + " (customer, ifShare,  customerName, customerCode , customerShort ,phone ,faxNo ,customerWWW ,email ,customerArea ,postalcode ,constomerAdd ,source ,kind ,sellMode ,attribute ,enterType ,enterMemo ,demo) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set customer=?, ifShare=?,  customerName=?, customerCode=? , customerShort=? ,phone=? ,faxNo=? ,customerWWW=? ,email=? ,customerArea=? ,postalcode=? ,constomerAdd=? ,source=? ,kind=? ,sellMode=? ,attribute=? ,enterType=? ,enterMemo=? ,demo=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select customer, ifShare,  customerName, customerCode , customerShort ,phone ,faxNo ,customerWWW ,email ,customerArea ,postalcode ,constomerAdd ,source ,kind ,sellMode ,attribute ,enterType ,enterMemo ,demo  from " +
                     tableName + " where id=?";
    }

    public SaleDb getSaleDb(int id) {
        return (SaleDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            //customer, ifShare,  customerName, customerCode , customerShort
            //phone ,faxNo ,customerWWW ,email ,customerArea ,postalcode ,constomerAdd ,
            //source ,kind ,sellMode ,attribute ,enterType ,enterMemo ,demo
            ps.setString(1, customer);
            ps.setString(2, ifShare);
            ps.setString(3, customerName);
            ps.setString(4, customerCode);
            ps.setString(5, customerShort);
            ps.setString(6, phone);
            ps.setString(7, faxNo);
            ps.setString(8, customerWWW);
            ps.setString(9, email);
            ps.setString(10, customerArea);
            ps.setString(11, postalcode);
            ps.setString(12, constomerAdd);
            ps.setString(13, source);
            ps.setString(14, kind);
            ps.setString(15, sellMode);
            ps.setString(16, attribute);
            ps.setString(17, enterType);
            ps.setString(18, enterMemo);
            ps.setString(19, demo);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                SaleCache rc = new SaleCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("数据库操作失败！");
        } finally {
            if (conn != null) {
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
                SaleCache rc = new SaleCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("del: " + e.getMessage());
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
        return new SaleDb(pk.getIntValue());
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
         //customer, ifShare,  customerName, customerCode , customerShort
          //phone ,faxNo ,customerWWW ,email ,customerArea ,postalcode ,constomerAdd ,
           //source ,kind ,sellMode ,attribute ,enterType ,enterMemo ,demo
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                customer = rs.getString(1);
                ifShare = rs.getString(2);
                customerName = rs.getString(3);
                customerCode = rs.getString(4);
                customerShort = rs.getString(5);
                phone = rs.getString(6);
                faxNo = rs.getString(7);
                customerWWW = rs.getString(8);
                email = rs.getString(9);
                customerArea = rs.getString(10);
                postalcode = rs.getString(11);
                constomerAdd = rs.getString(12);
                source = rs.getString(13);
                kind = rs.getString(14) ;
                sellMode = rs.getString(15);
                attribute = rs.getString(16);
                enterType = rs.getString(17);
                enterMemo = rs.getString(18);
                demo = rs.getString(19) ;
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
                rs = null;
            }
            if (conn != null) {
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
         //customer, ifShare,  customerName, customerCode , customerShort
         //phone ,faxNo ,customerWWW ,email ,customerArea ,postalcode ,constomerAdd ,
         //source ,kind ,sellMode ,attribute ,enterType ,enterMemo ,demo
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, customer);
            ps.setString(2, ifShare);
            ps.setString(3, customerName);
            ps.setString(4, customerCode);
            ps.setString(5, customerShort);
            ps.setString(6, phone);
            ps.setString(7, faxNo);
            ps.setString(8, customerWWW);
            ps.setString(9, email);
            ps.setString(10, customerArea);
            ps.setString(11, postalcode);
            ps.setString(12, constomerAdd);
            ps.setString(13, source);
            ps.setString(14, kind);
            ps.setString(15, sellMode);
            ps.setString(16, attribute);
            ps.setString(17, enterType);
            ps.setString(18, enterMemo);
            ps.setString(19, demo);
            ps.setInt(20, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                SaleCache rc = new SaleCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            logger.error("save: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }
    /**
        * @param typeId int
        * @return boolean
        */
       public boolean hasSaleOfType(int typeId) {
           ResultSet rs = null;
           Conn conn = new Conn(connname);
           try {
               String sql = "select id from sale where typeId=?";
               PreparedStatement ps = conn.prepareStatement(sql);
               ps.setInt(1, typeId);
               rs = conn.executePreQuery();
               if (rs != null && rs.next()) {
                   return true;
               }
           } catch (SQLException e) {
               logger.error("hasSaleOfType: " + e.getMessage());
           } finally {
               if (rs != null) {
                   try {
                       rs.close();
                   } catch (SQLException e) {}
                   rs = null;
               }
               if (conn != null) {
                   conn.close();
                   conn = null;
               }
           }
           return false;
       }

    public boolean isExist(String tableName) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            rs = conn.executeQuery(
                    "select id from sale where number='" + tableName +
                    "'");
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            logger.error("Salelist:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
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
                    result.addElement(getSaleDb(rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return result;
    }

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

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

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
                    SaleDb ug = getSaleDb(rs.getInt(1));
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
                } catch (SQLException e) {e.printStackTrace();}
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

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setIfShare(String ifShare) {
        this.ifShare = ifShare;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public void setCustomerShort(String customerShort) {
        this.customerShort = customerShort;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setFaxNo(String faxNo) {
        this.faxNo = faxNo;
    }

    public void setCustomerWWW(String customerWWW) {
        this.customerWWW = customerWWW;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCustomerArea(String customerArea) {
        this.customerArea = customerArea;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public void setConstomerAdd(String constomerAdd) {
        this.constomerAdd = constomerAdd;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setSellMode(String sellMode) {
        this.sellMode = sellMode;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setEnterType(String enterType) {
        this.enterType = enterType;
    }

    public void setEnterMemo(String enterMemo) {
        this.enterMemo = enterMemo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    private void jbInit() throws Exception {
    }
}
