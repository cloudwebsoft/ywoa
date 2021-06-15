package com.redmoon.oa.asset;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
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
public class AssetDb extends ObjectDb {
    private int id;

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_USER = 0;

    public AssetDb() {
        init();
    }

    public AssetDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "asset_info";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new AssetCache(this);
        isInitFromConfigDB = false;
        //name type  number  abstracts typeId  addId department buyMan buyDate keeper startDate abstracts inputMan regDate price

        QUERY_CREATE =
                "insert into " + tableName + " (name, asset_type,  asset_number, typeId , addId, department, buyMan,  keeper, inputMan,startDate, buyDate, regDate,abstracts,price,id, unit_code, asset_amount) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set name=?, asset_type=?,  asset_number=?, typeId=? , addId=?, department=?, buyMan=?,  keeper=?, inputMan=?,startDate=?, buyDate=?, regDate=?,abstracts=?, price=?, unit_code=?, asset_amount=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select name, asset_type,  asset_number, typeId , addId, department, buyMan,  keeper, inputMan,startDate, buyDate, regDate,abstracts, price, unit_code, asset_amount from " +
                     tableName + " where id=?";
    }

    public AssetDb getAssetDb(int id) {
        return (AssetDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            //name, type,  number, typeId , addId, department, buyMan,
            //keeper, inputMan,startDate, buyDate, regDate,abstracts
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, number);
            ps.setInt(4, typeId);
            ps.setString(5, addId);
            ps.setString(6, department);
            ps.setString(7, buyMan);
            ps.setString(8, keeper);
            ps.setString(9, inputMan);
            if (startDate != null) {
                ps.setDate(10, new java.sql.Date(startDate.getTime()));
            } else {
                ps.setDate(10, null);
            }
            if (buyDate != null) {
                ps.setDate(11, new java.sql.Date(buyDate.getTime()));
            } else {
                ps.setDate(11, null);
            }
            if (regDate != null) {
                ps.setDate(12, new java.sql.Date(regDate.getTime()));
            } else {
                ps.setDate(12, null);
            }
            ps.setString(13, abstracts);
            ps.setDouble(14, price);

            id = (int)SequenceManager.nextID(SequenceManager.ASSET_INFO);
            ps.setInt(15, id);
            ps.setString(16, unitCode);
            ps.setLong(17, amount);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                AssetCache rc = new AssetCache(this);
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
                AssetCache rc = new AssetCache(this);
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
        return new AssetDb(pk.getIntValue());
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
            //name, type,  number, typeId , addId, department, buyMan,
            //keeper, inputMan,startDate, buyDate, regDate,abstracts
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                name = rs.getString(1);
                type = rs.getString(2);
                number = rs.getString(3);
                typeId = rs.getInt(4);
                addId = rs.getString(5);
                department = rs.getString(6);
                buyMan = StrUtil.getNullStr(rs.getString(7));
                keeper = StrUtil.getNullStr(rs.getString(8));
                inputMan = StrUtil.getNullStr(rs.getString(9));
                startDate = rs.getDate(10);
                buyDate = rs.getDate(11);
                regDate = rs.getDate(12);
                abstracts = StrUtil.getNullStr(rs.getString(13));
                price = rs.getDouble(14);
                unitCode = rs.getString(15);
                amount = rs.getLong(16);
                loaded = true;
                primaryKey.setValue(new Integer(id));
            }
        } catch (SQLException e) {
            logger.error("load: " + e.getMessage());
        } finally {
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
            //name, type,  number, typeId , addId, department, buyMan,
            //keeper, inputMan,startDate, buyDate, regDate,abstracts
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, number);
            ps.setInt(4, typeId);
            ps.setString(5, addId);
            ps.setString(6, department);
            ps.setString(7, buyMan);
            ps.setString(8, keeper);
            ps.setString(9, inputMan);
            if (startDate != null) {
                ps.setDate(10, new java.sql.Date(startDate.getTime()));
            } else {
                ps.setDate(10, null);
            }
            if (buyDate != null) {
                ps.setDate(11, new java.sql.Date(buyDate.getTime()));
            } else {
                ps.setDate(11, null);
            }
            if (regDate != null) {
                ps.setDate(12, new java.sql.Date(regDate.getTime()));
            } else {
                ps.setDate(12, null);
            }
            ps.setString(13, abstracts);
            ps.setDouble(14, price);
            ps.setString(15, unitCode);
            ps.setLong(16, amount);
            ps.setInt(17, id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                AssetCache rc = new AssetCache(this);
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
       public boolean hasAssetOfType(int typeId) {
           ResultSet rs = null;
           Conn conn = new Conn(connname);
           try {
               String sql = "select id from asset_info where typeId=?";
               PreparedStatement ps = conn.prepareStatement(sql);
               ps.setInt(1, typeId);
               rs = conn.executePreQuery();
               if (rs != null && rs.next()) {
                   return true;
               }
           } catch (SQLException e) {
               logger.error("hasBookOfType: " + e.getMessage());
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
                    "select id from asset_info where asset_number='" + tableName +
                    "'");
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            logger.error("list:" + e.getMessage());
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
                    result.addElement(getAssetDb(rs.getInt(1)));
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
                    AssetDb ug = getAssetDb(rs.getInt(1));
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
                } catch (SQLException e) {
                	e.printStackTrace();
                }
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

    public String getName() {
        return name;
    }
    public void setType(String type) {
        this.type = type;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setAbstracts(String abstracts) {
        this.abstracts = abstracts;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public void setAddId(String addId) {
        this.addId = addId;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setBuyMan(String buyMan) {
        this.buyMan = buyMan;
    }

    public void setBuyDate(java.util.Date buyDate) {
        this.buyDate = buyDate;
    }

    public void setKeeper(String keeper) {
        this.keeper = keeper;
    }

    public void setStartDate(java.util.Date startDate) {
        this.startDate = startDate;
    }

    public void setInputMan(String inputMan) {
        this.inputMan = inputMan;
    }

    public void setRegDate(java.util.Date regDate) {
        this.regDate = regDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public String getNumber() {
        return number;
    }

    public String getAbstracts() {
        return abstracts;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getAddId() {
        return addId;
    }

    public String getDepartment() {
        return department;
    }

    public String getBuyMan() {
        return buyMan;
    }

    public java.util.Date getBuyDate() {
        return buyDate;
    }

    public String getKeeper() {
        return keeper;
    }

    public java.util.Date getStartDate() {
        return startDate;
    }

    public String getInputMan() {
        return inputMan;
    }

    public java.util.Date getRegDate() {
        return regDate;
    }

    public double getPrice() {
        return price;
    }

    private String name;


    //name type  number  typeId  addId department buyMan buyDate keeper startDate abstracts inputMan regDate

    private String type;
    private String number;
    private String abstracts;
    private int typeId;
    private String addId;
    private String department;
    private String buyMan;
    private String keeper;
    private java.util.Date buyDate;
    private java.util.Date startDate;
    private java.util.Date regDate;
    private String inputMan;
    private double price;
    
    private String unitCode;
    
    private long amount;

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public long getAmount() {
		return amount;
	}


}
