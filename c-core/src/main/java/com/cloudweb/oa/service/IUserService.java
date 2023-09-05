package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import com.cloudweb.oa.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.vo.UserVO;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-01-09
 */
public interface IUserService extends IService<User> {
    User getUser(String userName);

    void updateUserUnitCode(String code, String unitCode);

    List<User> getRecentSelected(String userName);

    List<User> getRecentSelectedOfUnit(String userName, String unitCode);

    List<User> getDeptUsers(String deptCode, boolean isIncludeChildren, String[] limitDeptArr, String unitCode, boolean includeRootDept);

    List<User> searchUser(String userName, String[] limitDeptArr, boolean includeRootDept);

    List<User> getUserOfRole(String roleCode, String unitCode);

    List<User> getGroupUsers(String groupCode, String unitCode);

    boolean leaveOffice(int id, String operator);

    String getNextPersonNo();

    User getUserById(int id);

    User getUserByEmail(String email);

    User getUserByMobile(String mobile);

    User getUserByRealName(String realName);

    boolean create(UserVO userVO) throws IOException, ErrMsgException;

    boolean isPersonNoExist(String personNo);

    boolean update(UserVO userVO) throws IOException;

    boolean delUsers(String[] ary) throws ResKeyException, ErrMsgException;

    List<String> listNameBySql(String sql);

    List<User> listBySql(String sql);

    boolean reEmploryment(int id, String operator);

    List<User> listAll();

    boolean refreshOrders(String userName);

    boolean modifyPwd(String userName, String newPwd);

    boolean updateByUserName(User user);

    boolean updateMyInfo(UserVO userVO) throws IOException;

    List<User> listByUnitCode(String unitCode);

    int getValidUserCount();

    boolean isPersonNoUsedByOther(String userName, String personNo);

    boolean importUser(com.alibaba.fastjson.JSONArray arr) throws ValidateException, IOException, ErrMsgException;

    int updateUserUnitInDept(String unitCode, String deptCode);

    List<User> listByOnlineTime();

    User getUserByOpenId(String openId);

    User getUserByUnionId(String openId);

    User getUserByUin(String uin);

    User getUserByLoginName(String loginName);

    List<User> listForAddress(String what);

    String encryptPwd(String pwd);

    String aesEncryptPwdRaw(String pwdRaw);
}
