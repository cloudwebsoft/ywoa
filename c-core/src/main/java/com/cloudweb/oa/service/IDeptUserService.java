package com.cloudweb.oa.service;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.entity.DeptUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-04
 */
public interface IDeptUserService extends IService<DeptUser> {

    boolean create(String userName, String deptCode);

    boolean delOfUser(String userName);

    DeptUser getDeptUser(String userName, String deptCode);

    DeptUser getPrimary(String userName);

    List<DeptUser> listByUserName(String userName);

    boolean changeDeptOfUser(String userName, String deptCodes, String opUser) throws ErrMsgException;

    boolean isUserOfDept(String userName, String deptCode);

    List<DeptUser> listByDeptCode(String deptCode);

    boolean del(DeptUser deptUser);

    boolean isUserBelongToDept(String userName, String deptCode);

    List<String> listUserNameInDepts(String deptCodes);

    List<Integer> listIdBySql(String sql);

    void syncUnit();

    ListResult listBySearch(String op, String realName, String mobile, String email, String depts, String phone, String orderField, int curPage, int pageSize);

    List<DeptUser> listAllByDeptCode(String deptCode);
}
