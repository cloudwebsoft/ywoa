package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.Account;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-09
 */
public interface IAccountService extends IService<Account> {
    Account getAccount(String account);

    Account getAccountByUserName(String userName);

    boolean delOfUser(String userName);

    List<Account> list(String userName, String op, String by, String what, String searchUnitCode, String unitCode, int pageNum, int pageSize);

    boolean del(String name);

    boolean update(Account account);
}
