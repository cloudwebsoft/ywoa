package com.redmoon.oa.book;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.oa.address.AddressDb;

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
public class BookDb extends ObjectDb {
    private int id;

    public static final int TYPE_PUBLIC = 1;
    public static final int TYPE_USER = 0;
    
    private String unitCode;

    public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public BookDb() {
        init();
    }

    public BookDb(int id) {
        this.id = id;
        init();
        load();
    }

    public int getId() {
        return id;
    }

    public void initDB() {
        tableName = "book";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new BookCache(this);
        isInitFromConfigDB = false;

        QUERY_CREATE =
                "insert into " + tableName + " (deptCode, bookName,typeId,author,bookNum,pubHouse,pubDate,keepSite,price,abstracts,borrowRange,borrowState,brief,borrowPerson,beginDate,endDate,unit_code) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set deptCode=?, bookName=?,typeId=?,author=?,bookNum=?,pubHouse=?,pubDate=?,keepSite=?,price=?,abstracts=?,borrowRange=?,borrowState=?,brief=?,borrowPerson=?,beginDate=?,endDate=?,unit_code=? where id=?";
        QUERY_LIST =
                "select id from " + tableName;
        QUERY_DEL = "delete from " + tableName + " where id=?";
        QUERY_LOAD = "select deptCode, bookName,typeId,author,bookNum,pubHouse,pubDate,keepSite,price,abstracts,borrowRange,borrowState,brief,borrowPerson,beginDate,endDate,unit_code from " +
                     tableName + " where id=?";
    }

    public BookDb getBookDb(int id) {
        return (BookDb) getObjectDb(new Integer(id));
    }

    public boolean create() throws ErrMsgException {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //  deptCode, bookName,typeId,author,bookNum,pubHouse,pubDate,
            //  keepSite,price,abstracts,borrowRange,borrowState,brief,borrowPerson,beginDate,endDate
            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            ps.setString(1, deptCode);
            ps.setString(2, bookName);
            ps.setInt(3, typeId);
            ps.setString(4, author);
            ps.setString(5, bookNum);
            ps.setString(6, pubHouse);
            if (pubDate != null)
                ps.setDate(7, new java.sql.Date(pubDate.getTime()));
            else
                ps.setDate(7, null);
            ps.setString(8, keepSite);
            ps.setDouble(9, price);
            ps.setString(10, abstracts);
            ps.setString(11, borrowRange);
            ps.setInt(12, borrowState?1:0);
            ps.setString(13, brief);
            ps.setString(14,borrowPerson);
            if (beginDate != null)
                ps.setDate(15, new java.sql.Date(beginDate.getTime()));
            else
                ps.setDate(15, null);
            if (endDate != null)
               ps.setDate(16, new java.sql.Date(endDate.getTime()));
            else
               ps.setDate(16, null);
            
            ps.setString(17, unitCode);
            
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                BookCache rc = new BookCache(this);
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
                BookCache rc = new BookCache(this);
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
        return new BookDb(pk.getIntValue());
    }

    /**
     * 判断类型为typeId的书是否有被借出的
     * @param typeId int
     * @return boolean
     */
    public boolean hasBookOfType(int typeId) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            String sql = "select id from book where typeId=?";
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
        // deptCode, bookName,typeId,author,bookNum,pubHouse,pubDate,keepSite,price,
        // abstracts,borrowRange,borrowState,brief,borrowPerson,beginDate,endDate
            PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
            ps.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                deptCode = rs.getString(1);
                bookName = rs.getString(2);
                logger.info("deptCode=" + deptCode + " bookName=" + bookName);

                typeId = rs.getInt(3);
                author = StrUtil.getNullStr(rs.getString(4));
                bookNum = StrUtil.getNullStr(rs.getString(5));
                pubHouse = StrUtil.getNullStr(rs.getString(6));
                try{
                    pubDate = rs.getDate(7);
                }catch(Exception e){
                    logger.error("load1:" + e.getMessage());
                }
                keepSite = StrUtil.getNullStr(rs.getString(8));
                price = rs.getDouble(9);
                abstracts = StrUtil.getNullStr(rs.getString(10));
                borrowRange = rs.getString(11);
                borrowState = rs.getInt(12)==1?true:false;
                brief = StrUtil.getNullStr(rs.getString(13));
                borrowPerson = rs.getString(14);
                beginDate = rs.getDate(15);
                endDate = rs.getDate(16);
                unitCode = rs.getString(17);
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
           // deptCode, bookName,typeId,author,bookNum,pubHouse,pubDate,
           // keepSite,price,abstracts,borrowRange,borrowState,brief,borrowPerson,beginDate,endDate
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, deptCode);
            ps.setString(2, bookName);
            ps.setInt(3, typeId);
            ps.setString(4, author);
            ps.setString(5, bookNum);
            ps.setString(6, pubHouse);
            if (pubDate==null)
                ps.setDate(7, null);
            else
                ps.setDate(7, new java.sql.Date(pubDate.getTime()));
            ps.setString(8, keepSite);
            ps.setDouble(9, price);
            ps.setString(10, abstracts);
            ps.setString(11, borrowRange);
            ps.setInt(12, borrowState?1:0);
            ps.setString(13, brief);
            ps.setString(14,borrowPerson);
            if (beginDate==null)
                ps.setDate(15, null);
            else
                ps.setDate(15, new java.sql.Date(beginDate.getTime()));
            if (endDate==null)
                ps.setDate(16, null);
            else
                ps.setDate(16, new java.sql.Date(endDate.getTime()));
            ps.setString(17, unitCode);
            ps.setInt(18,id);
            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                BookCache rc = new BookCache(this);
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
                    BookDb ug = getBookDb(rs.getInt(1));
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
    /**
     *  判断是否存在该字段
     */

    public boolean isExist(String tableName){
         ResultSet rs = null;
         Conn conn = new Conn(connname);
         try {
             rs = conn.executeQuery("select id from book where bookNum='"+tableName+"'" );
             if(rs.next()) return true;
         } catch (SQLException e) {
             logger.error("list:" + e.getMessage());
         } finally {
             if (conn != null) {
                 conn.close();
                 conn = null;
             }
         }
         return false ;
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
                    result.addElement(getBookDb(rs.getInt(1)));
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


    public String getDeptCode() {
        return deptCode;
    }

    public String getBookName() {
        return bookName;
    }


    public String getAuthor() {
        return author;
    }



    public String getPubHouse() {
        return pubHouse;
    }

    public java.util.Date getPubDate() {
        return pubDate;
    }

    public String getKeepSite() {
        return keepSite;
    }




    public String getAbstracts() {
        return abstracts;
    }



    public String getBorrowRange() {
        return borrowRange;
    }

    public String getBrief() {
        return brief;
    }



    public boolean getBorrowState() {
        return borrowState;
    }

    public int getTypeId() {
        return typeId;
    }


    public String getBookNum() {
        return bookNum;
    }

    public double getPrice() {
        return price;
    }

    public String getBorrowPerson() {
        return borrowPerson;
    }

    public java.util.Date  getBeginDate() {
        return beginDate;
    }

    public java.util.Date  getEndDate() {
        return endDate;
    }


    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public void setPubHouse(String pubHouse) {
        this.pubHouse = pubHouse;
    }

    public void setPubDate(java.util.Date pubDate) {
        this.pubDate = pubDate;
    }

    public void setKeepSite(String keepSite) {
        this.keepSite = keepSite;
    }




    public void setAbstracts(String abstracts) {
        this.abstracts = abstracts;
    }



    public void setBorrowRange(String borrowRange) {
        this.borrowRange = borrowRange;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public void setBorrowState(boolean borrowState) {
        this.borrowState = borrowState;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public void setBookNum(String bookNum) {
        this.bookNum = bookNum;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setBorrowPerson(String borrowPerson) {
        this.borrowPerson = borrowPerson;
    }

    public void setBeginDate(java.util.Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
    }

    // deptCode, bookName,typeId,author,bookNum,pubHouse,pubDate,keepSite,price,
    // abstracts,borrowRange,borrowState,brief,borrowPerson,beginDate,endDate
    private String deptCode;
    private String bookName;
    private int typeId;
    private String author;
    private String bookNum;
    private String pubHouse;
    private java.util.Date pubDate;
    private String keepSite;
    private double price;
    private String abstracts;
    private String borrowRange;
    private String brief;
    private boolean borrowState;
    private String borrowPerson;
    private java.util.Date beginDate;
    private java.util.Date endDate;

}
