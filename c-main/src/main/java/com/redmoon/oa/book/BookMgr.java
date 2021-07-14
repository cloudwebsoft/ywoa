package com.redmoon.oa.book;
import java.util.Calendar;
import cn.js.fan.db.Conn;
import cn.js.fan.util.*;
import java.sql.*;

import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;
import com.redmoon.oa.person.UserDb;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class BookMgr {
    Logger logger = Logger.getLogger(BookMgr.class.getName());

    public BookMgr() {

    }

    /**
     *
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean modify(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "book.all"))
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        boolean re = true;
        String deptCode = "", bookName = "", author = "",
                bookNum = "", pubHouse = "", pubDate = "";
        String keepSite = "", abstracts = "", borrowRange = "",
               brief = "";
        int typeId=0;
        double  price=0;
        int id = ParamUtil.getInt(request,"id");
        deptCode = ParamUtil.get(request, "deptCode");
        typeId = ParamUtil.getInt(request, "typeId",false);
        if (typeId==0)
                 throw new ErrMsgException("请您选择图书类别！\\n");
        bookName = ParamUtil.get(request, "bookName");
        if (bookName.equals(""))
              throw new ErrMsgException("请您输入图书名称！\\n");
        bookNum = ParamUtil.get(request,"bookNum");
        author = ParamUtil.get(request, "author");
        pubHouse = ParamUtil.get(request, "pubHouse");
        pubDate = ParamUtil.get(request, "pubDate");
        price = ParamUtil.getDouble(request, "price");
        if (price == 0) {
            throw new ErrMsgException("请输入正确价格！\\n");
        }
        abstracts = ParamUtil.get(request, "abstracts");
        borrowRange = ParamUtil.get(request, "borowRange");
        brief = ParamUtil.get(request, "brief");
        
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(deptCode);
		String unitCode = dd.getUnitOfDept(dd).getCode();        
        
        BookDb book = getBookDb(id);
        book.setAbstracts(abstracts);
        book.setAuthor(author);
        book.setBookName(bookName);
        book.setBorrowRange(borrowRange);
        book.setBrief(brief);
        book.setDeptCode(deptCode);
        book.setBookNum(bookNum);
        book.setKeepSite(keepSite);
        book.setPrice(price);
        java.util.Date d = null;
        try {
            d = DateUtil.parse(pubDate, "yyyy-MM-dd");
        }
        catch (Exception e) {
            logger.error("modify:" + e.getMessage());
        }
        book.setPubDate(d);
        book.setPubHouse(pubHouse);
        book.setTypeId(typeId);
        book.setUnitCode(unitCode);
        re = book.save();
        return re;
    }

    // 添加借书日期和还书日期
    public boolean borrow(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "book.all"))
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        boolean re = true;
        String  beginDate = "", endDate = "",borrowPerson = "";
        String strids = ParamUtil.get(request, "ids");
        String[] ids = strids.split(",");
        int id;
        for (int i = 0; i < ids.length; i++) {
            id = Integer.parseInt(ids[i]);
            beginDate = ParamUtil.get(request, "beginDate");
            endDate = ParamUtil.get(request, "endDate");
            borrowPerson = ParamUtil.get(request,"borrowPerson");
            UserDb ud = new UserDb();
            ud = ud.getUserDb(borrowPerson);
            if (ud==null || !ud.isLoaded()) {
                throw new ErrMsgException("用户" + borrowPerson + "不存在！");
            }

            BookDb book = getBookDb(id);
            book.setBorrowState(true);

            java.util.Date d = null;
            try {
                d = DateUtil.parse(beginDate, "yyyy-MM-dd");
            } catch (Exception e) {
                logger.error("borrow:" + e.getMessage());
            }
            book.setBeginDate(d);
            try {
                d = DateUtil.parse(endDate, "yyyy-MM-dd");
            } catch (Exception e) {
                logger.error("borrow:" + e.getMessage());
            }
            book.setEndDate(d);
            book.setBorrowPerson(borrowPerson);
            re = book.save();
        }
        return re;
    }

    public boolean returnBook(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "book.all"))
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        boolean re = true;
        String strids = ParamUtil.get(request, "ids");
        String[] ids = strids.split(",");
        String beginDate = "1900-01-01" ;
        String endDate = "1900-01-01" ;
        int id;
        for (int i = 0; i < ids.length; i++) {
            id = Integer.parseInt(ids[i]);
            BookDb book = getBookDb(id);
            book.setBorrowState(false);
            java.util.Date d = null;
            try {
                d = DateUtil.parse(beginDate, "yyyy-MM-dd");
            } catch (Exception e) {
                logger.error("returnBook:" + e.getMessage());
            }
            try {
                book.setBeginDate(d);
                d = DateUtil.parse(endDate, "yyyy-MM-dd");
            } catch (Exception e) {
                logger.error("returnBook:" + e.getMessage());
            }
            book.setEndDate(d);
            re = book.save();
        }
        return re;
    }

    public BookDb getBookDb(int id) {
        BookDb book = new BookDb();
        return book.getBookDb(id);
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "book.all"))
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        boolean re = true;
        int typeId=0;
        String deptCode = "", bookName = "", author = "",
                bookNum = "", pubHouse = "", pubDate = "";
        String keepSite = "", abstracts = "", borrowRange = "",
                borrowState = "", brief = "",borrowPerson="",beginDate ="",endDate="";
        double  price;
        bookName = ParamUtil.get(request, "bookName");
        if (bookName.equals(""))
            throw new ErrMsgException("请您输入图书名称！\\n");
        deptCode = ParamUtil.get(request, "deptCode");
        typeId = ParamUtil.getInt(request,"typeId",false);
         // 判断是否输入图书类别
        if (typeId==0)
                throw new ErrMsgException("请您选择图书类别！\\n");
        author = ParamUtil.get(request, "author");
        bookNum = ParamUtil.get(request, "bookNum");
        //判断是否输入图书编号
        if (bookNum.equals(""))
            throw new ErrMsgException("请您输入图书编号！\\n");
        BookDb bd = new BookDb();
        if (bd.isExist(bookNum))
             throw new ErrMsgException("添加的图书编号已存在!");
        pubHouse = ParamUtil.get(request, "pubHouse");
        pubDate = ParamUtil.get(request, "pubDate");
        keepSite = ParamUtil.get(request, "keepSite");
        price = ParamUtil.getDouble(request, "price",0);
        if (price==0){
                throw new ErrMsgException("请输入正确价格！\\n");
        }
        abstracts = ParamUtil.get(request, "abstracts");
        borrowRange = ParamUtil.get(request, "borrowRange");
        brief = ParamUtil.get(request, "brief");
        borrowPerson = ParamUtil.get(request, "borrowPerson");
        beginDate = ParamUtil.get(request, "beginDate");
        endDate = ParamUtil.get(request, "endDate");
        
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(deptCode);
		String unitCode = dd.getUnitOfDept(dd).getCode();               
        
        BookDb book = new BookDb();
        book.setAuthor(author);
        book.setAbstracts(abstracts);
        book.setBookName(bookName);
        book.setTypeId(typeId);
        book.setBorrowRange(borrowRange);
        book.setBorrowState(false);
        book.setBrief(brief);
        book.setDeptCode(deptCode);
        book.setBookNum(bookNum);
        book.setKeepSite(keepSite);
        book.setPrice(price);
        if (pubDate.equals(""))
            pubDate="1900-01-01" ;
        java.util.Date d = null;
        try {
            d = DateUtil.parse(pubDate, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("create:" + e.getMessage());
        }
        book.setPubDate(d);
        book.setPubHouse(pubHouse);
        book.setBorrowPerson(borrowPerson);
        try {
            d = DateUtil.parse(beginDate, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("create:" + e.getMessage());
        }
        book.setBeginDate(d);
        try {
            d = DateUtil.parse(endDate, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("create:" + e.getMessage());
        }
        book.setEndDate(d);
        book.setUnitCode(unitCode);
        re = book.create();
        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, "book.all"))
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
        int id = ParamUtil.getInt(request, "id");
        BookDb book = getBookDb(id);
        if (book == null || !book.isLoaded())
            throw new ErrMsgException("该计划已不存在！");

        // Privilege privilege = new Privilege();
        // if (!privilege.getUser(request).equals(book.getUserName()))
        //     throw new ErrMsgException("非法操作！");
        return book.del();
    }
}
