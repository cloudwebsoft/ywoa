package com.cloudweb.oa.service.impl;

import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.Account;
import com.cloudweb.oa.mapper.AccountMapper;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.IAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author bw
 * @since 2020-02-09
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements IAccountService {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    AuthUtil authUtil;

    @Override
    public Account getAccount(String account) {
        QueryWrapper<Account> qw = new QueryWrapper<>();
        qw.eq("name", account);
        return getOne(qw, false);
    }

    @Override
    public Account getAccountByUserName(String userName) {
        QueryWrapper<Account> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        return getOne(qw, false);
    }

    @Override
    public boolean delOfUser(String userName) {
        QueryWrapper<Account> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        return remove(qw);
    }

    @Override
    public boolean del(String name) {
        QueryWrapper<Account> qw = new QueryWrapper<>();
        qw.eq("name", name);
        return remove(qw);
    }

    @Override
    public boolean update(Account account) {
        QueryWrapper<Account> qw = new QueryWrapper<>();
        qw.eq("name", account.getName());
        return update(account, qw);
    }

    @Override
    public List<Account> list(String userName, String op, String by, String what, String searchUnitCode, String unitCode, int pageNum, int pageSize) {
        String sql = "select * from account a where 1=1";
        if ("search".equals(op)) {
            if ("userName".equals(by)) {
                sql = "select a.* from account a, users u where a.userName=u.name and u.realName like " + StrUtil.sqlstr("%" + what + "%");
            }
            else if ("account".equals(by)) {
                sql = "select * from account a where name like " + StrUtil.sqlstr("%" + what + "%");
            }
        }

        if (!"".equals(searchUnitCode)) {
            sql += " and a.unit_code=" + StrUtil.sqlstr(searchUnitCode);
        }
        else {
            boolean isAdmin = authUtil.isUserPrivValid(userName, "admin.user") && unitCode.equals(ConstUtil.DEPT_ROOT);
            if (!isAdmin) {
                sql += " and a.unit_code=" + StrUtil.sqlstr(searchUnitCode);
            }
        }

        sql += " order by a.name asc";

        log.debug(sql);

        return accountMapper.listBySql(sql);
    }
}
