package com.redmoon.oa.account;

import java.sql.*;
import java.util.Vector;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.Account;
import com.cloudweb.oa.service.IAccountService;
import com.cloudweb.oa.utils.SpringUtil;

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
public class AccountDb extends ObjectDb {
    public AccountDb() {
        init();
    }

    public AccountDb(String name) {
        this.name = name;
        init();
        load();
    }

    @Override
    public void initDB() {
        tableName = "account";
        isInitFromConfigDB = false;
    }

    public AccountDb getAccountDb(String name) {
        IAccountService accountService = SpringUtil.getBean(IAccountService.class);
        return getFromAccount(accountService.getAccount(name), new AccountDb());
    }

    public AccountDb getUserAccount(String userName) {
        IAccountService accountService = SpringUtil.getBean(IAccountService.class);
        return getFromAccount(accountService.getAccountByUserName(userName), new AccountDb());
    }

    @Override
    public boolean create() throws ErrMsgException {
        return false;
    }

    /**
     * del
     *
     * @return boolean
     * @throws ErrMsgException
     */
    @Override
    public boolean del() throws ErrMsgException {
        return false;
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return null;
    }

    @Override
    public void load() {
        IAccountService accountService = SpringUtil.getBean(IAccountService.class);
        Account account = accountService.getAccount(name);
        if (account!=null) {
            getFromAccount(account, this);
        }
    }

    public AccountDb getFromAccount(Account account, AccountDb accountDb) {
        if (account==null) {
            return accountDb;
        }
        accountDb.setName(account.getName());
        accountDb.setUserName(account.getUserName());
        accountDb.setUnitCode(account.getUnitCode());
        accountDb.setLoaded(true);
        return accountDb;
    }

    /**
     * save
     *
     * @return boolean
     * @throws ErrMsgException
     */
    @Override
    public boolean save() throws ErrMsgException {
        return false;
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    /**
     * 工号
     */
    private String name;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 单位编码
     */
    private String unitCode;
}
