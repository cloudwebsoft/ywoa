package com.cloudweb.oa.controller;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.web.Global;
import com.cloudweb.oa.cache.DepartmentCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.mapper.UserMapper;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.utils.PasswordUtil;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.visual.PersonBasicService;
import com.cloudweb.oa.vo.Result;
import com.cloudweb.oa.vo.UserVO;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.dingding.service.client.DingDingClient;
import com.redmoon.oa.Config;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.netdisk.UtilTools;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserSet;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.sso.SyncUtil;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.weixin.mgr.WeixinDo;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.io.*;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-01-09
 */
@Api(tags = "用户管理模块")
@Slf4j
@Controller
@Validated
@RequestMapping
public class UserController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    IUserService userService;

    @Autowired
    IRoleService roleService;

    @Autowired
    IGroupService userGroupService;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    IAccountService accountService;

    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    private IUserOfGroupService userOfGroupService;

    @Autowired
    private IGroupOfRoleService userGroupOfRoleService;

    @Autowired
    private UserCache userCache;

    @Autowired
    @Qualifier(value = "validMessageSource")
    private MessageSource validMessageSource;

    @Autowired
    private IUserSetupService userSetupService;

    @Autowired
    private IUserPrivService userPrivService;

    @Autowired
    private IUserOfRoleService userOfRoleService;

    @Autowired
    private IUserMobileService userMobileService;

    @Autowired
    private IUserAdminDeptService userAdminDeptService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private PersonBasicService personBasicService;

    @Autowired
    private IPostService postService;

    @Autowired
    private IPostUserService postUserService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private DepartmentCache departmentCache;

    @ApiOperation(value = "弹出框用户列表", notes = "弹出框用户列表")
    @ResponseBody
    @RequestMapping(value = "/userMultiSel")
    public Result<Object> userMultiSel() {
//        String skinPath = SkinMgr.getSkinPath(request, false);
//        model.addAttribute("skinPath", skinPath);
        JSONObject object = new JSONObject();

        Privilege privilege = new Privilege();

        String mode = ParamUtil.get(request, "mode");//此参数用来判断是否单选模式,single表示单选模式
        String parameterNum = ParamUtil.get(request, "parameterNum");//此参数用来设置选择用户之后，传递的参数个数
        String windowSize = ParamUtil.get(request, "windowSize");
        // 从表单中UserSelectWinCtl调用
        String isForm = ParamUtil.get(request, "isForm");
        String userName = privilege.getUser(request);
        String unitCode = ParamUtil.get(request, "unitCode");
        // 来自于流程则为flow
        String from = ParamUtil.get(request, "from");
        // 选择时是否包含子节点，启用checkbox;
        boolean isIncludeChildren = ParamUtil.getBoolean(request, "isIncludeChildren", false);

        object.put("mode", mode);
        object.put("isIncludeChildren", isIncludeChildren);
        object.put("isForm", isForm);
        object.put("parameterNum", parameterNum);
        object.put("from", from);
        object.put("windowSize", windowSize);

        if ("".equals(unitCode)) {
            unitCode = ConstUtil.DEPT_ROOT;
        }

        List<User> recentSelectedList = null;
        if (unitCode.equals(ConstUtil.DEPT_ROOT)) {
            recentSelectedList = userService.getRecentSelected(userName);
        } else {
            recentSelectedList = userService.getRecentSelectedOfUnit(userName, unitCode);
        }
        if (recentSelectedList == null) {
            recentSelectedList = new ArrayList<>();
        }
        object.put("recentSelectedList", recentSelectedList);

        List<Role> roleList = roleService.getAll();
        object.put("roleList", roleList);

        QueryWrapper<Group> qw = new QueryWrapper<>();
        qw.orderByDesc("isSystem").orderByAsc("code");
        List<Group> groupList = userGroupService.list(qw);
        object.put("groupList", groupList);

        String parentCode = ParamUtil.get(request, "parentCode");
        List<Department> list = departmentService.getDepartments(parentCode);
        object.put("departmentList", list);

        object.put("unitList", departmentService.getAllUnit());

        boolean canSaveUserGroupShow = false;
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            canSaveUserGroupShow = true;
        }
        object.put("canSaveUserGroupShow", canSaveUserGroupShow);

        return new Result<>(object);
    }

    @ApiOperation(value = "为userMultiSel初始化用户", notes = "根据传入的用户，初始化用户选择框")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userNames", value = "用户名，如果有多个的话，以逗号分隔", required = true, dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/user/initUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> initUsers(String userNames) {
        String[] userNameArr = StrUtil.split(userNames, ",");
        JSONArray array = new JSONArray();
        // 取出用户信息，拼select
        for (String userName : userNameArr) {
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.eq("name", userName);
            User user = userService.getOne(qw, false);
            array.add(user);
        }

        return new Result<>(array);
    }

    @ApiOperation(value = "获取部门用户", notes = "根据传入的用户，初始化用户选择框")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deptCode", value = "部门编码", dataType = "String"),
            @ApiImplicitParam(name = "isIncludeChildren", value = "是否包含子集", dataType = "boolean"),
            @ApiImplicitParam(name = "limitDepts", value = "最少部门", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/user/getDeptUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> getDeptUsers(String deptCode, boolean isIncludeChildren, String limitDepts) {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        String unitCode = privilege.getUserUnitCode(request);

        // 仅发通知时可以勾选checkbox选择多个部门，否则一般没有没有复选框，即deptCode只能是一个
        if (isIncludeChildren) {
            String[] ary = StrUtil.split(deptCode, ",");
            if (ary != null && ary.length >= 1) {
                limitDepts = deptCode;
            }
        }

        boolean includeRootDept = false;
        String[] limitDeptArr = StrUtil.split(limitDepts, ",");
        if (limitDeptArr != null) {
            for (String dept : limitDeptArr) {
                if (dept.equals(ConstUtil.DEPT_ROOT)) {
                    includeRootDept = true;
                    break;
                }
            }
        }

        List<User> list = userService.getDeptUsers(deptCode, isIncludeChildren, limitDeptArr, unitCode, includeRootDept);

        if (authUtil.getUserName().equals(ConstUtil.USER_ADMIN)) {
            list.add(userCache.getUser(authUtil.getUserName()));
        }
        json.put("list", list);
        return new Result<>(json);
    }

    @ApiOperation(value = "搜索用户", notes = "根据传入的用户，初始化用户选择框")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名称", dataType = "String"),
            @ApiImplicitParam(name = "limitDepts", value = "最少部门", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/user/searchUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> searchUsers(String userName, String limitDepts) {
        String result = "";

        boolean includeRootDept = false;
        String[] limitDeptArr = StrUtil.split(limitDepts, ",");
        if (limitDeptArr != null) {
            for (String dept : limitDeptArr) {
                if (dept.equals(ConstUtil.DEPT_ROOT)) {
                    includeRootDept = true;
                    break;
                }
            }
        }

        List<User> list = userService.searchUser(userName, limitDeptArr, includeRootDept);

        result += getOptions(list, "", false);

        return new Result<>(result);
    }

    @ApiOperation(value = "获取角色用户", notes = "获取权限用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCode", value = "角色编码", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/user/getRoleUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> getRoleUsers(String roleCode) {
        String result = "";
        Privilege privilege = new Privilege();
        String unitCode = privilege.getUserUnitCode(request);

        List<User> list = userService.getUserOfRole(roleCode, unitCode);
        return new Result<>(list);
    }

    @ApiOperation(value = "获取组用户", notes = "获取组用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupCode", value = "组编码", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/user/getGroupUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> getGroupUsers(String groupCode) {
        String result = "";
        Privilege privilege = new Privilege();
        String unitCode = privilege.getUserUnitCode(request);

        List<User> list = userService.getGroupUsers(groupCode, unitCode);

        return new Result<>(list);
    }

    /**
     * 取得options
     *
     * @param list
     * @param deptName
     * @param isSingleDept 是否仅显示deptName中的用户
     * @return
     */
    @ApiOperation(value = "取得options", notes = "取得options")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "list", value = "用户列表", dataType = "List"),
            @ApiImplicitParam(name = "deptName", value = "部门名称", dataType = "String"),
            @ApiImplicitParam(name = "isSingleDept", value = "是否仅显示deptName中的用户", dataType = "boolean"),
    })
    public Result<Object> getOptions(List<User> list, String deptName, boolean isSingleDept) {
        boolean isFirst = true;
        JSONObject object = new JSONObject();
        for (User user : list) {
            if (!isSingleDept) {
                StringBuffer sbDept = new StringBuffer();
                List<Department> deptList = departmentService.getDeptsOfUser(user.getName());
                for (Department dept : deptList) {
                    StrUtil.concat(sbDept, ",", dept.getName());
                }
                deptName = sbDept.toString();
            }
            object.put("isFirst", isFirst);
            object.put("deptName", deptName);
        }
        return new Result<>(object);
    }

    @ApiOperation(value = "批量离开用户", notes = "批量离开用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userNames", value = "用户名称们", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/leaveOffBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> leaveOffBatch(@NotEmpty(message = "{user.select.none}") String userNames) throws ValidateException {
        boolean re = false;

        String[] ary = StrUtil.split(userNames, ",");
        if (ary == null) {
            return new Result<>("请选择用户");
        }

        String opUser = SpringUtil.getUserName();
        for (int i = 0; i < ary.length; i++) {
            if (ary[i].equals(ConstUtil.USER_ADMIN)) {
                throw new ValidateException("管理员不能离职！");
            }

            re = userService.leaveOffice(StrUtil.toInt(ary[i], -1), opUser);
        }

        return new Result<>(re);
    }

    @ApiOperation(value = "检查用户名称", notes = "检查用户名称")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名称", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/checkUserName", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> checkUserName(@NotEmpty(message = "{user.name.notempty}") @Length(max = 20, message = "{user.name.length}") String userName, @RequestParam(defaultValue="-1")Integer uId) {
        try {
            chkUserName(userName, uId);
        } catch (ValidateException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(0);
    }

    public void chkUserName(String userName, int uId) throws ValidateException {
        String[] chars = {";", "'", ",", "\""};
        int chlen = chars.length;
        for (int k = 0; k < chlen; k++) {
            if (userName.indexOf(chars[k]) != -1) {
                throw new ValidateException("#user.name.invalid");
            }
        }
        User user = userService.getUser(userName);
        if (user != null) {
            if (user.getId() != uId) {
                throw new ValidateException("#user.name.exist");
            }
        }
    }

    @ApiOperation(value = "检查手机号码", notes = "检查手机号码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mobile", value = "手机号码", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/checkMobile", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> checkMobile(String mobile) {
        if (mobile == null || "".equals(mobile)) {
            return new Result<>(true);
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("mobile", mobile);
        User user = userService.getOne(qw, false);

        return new Result<>(user);
    }

    @ApiOperation(value = "检查角色", notes = "检查角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account", value = "号码", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/user/checkAccount", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> checkAccount(String account) {
        if (account == null || "".equals(account)) {
            return new Result<>(true);
        }

        QueryWrapper<Account> qw = new QueryWrapper<>();
        qw.eq("name", account);
        Account acc = accountService.getOne(qw, false);
        if (acc != null) {
            JSONObject json = responseUtil.getResultJson(false);
            json.put("msg", validMessageSource.getMessage("user.account.used", null, null));
            return new Result<>(json);
        }

        return new Result<>(true);
    }

    @ApiOperation(value = "检查密码", notes = "检查密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pwd", value = "密码", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/user/checkPwd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> checkPwd(String pwd) throws ErrMsgException {
        if (pwd == null || "".equals(pwd)) {
            throw new ErrMsgException("请输入密码");
        }

        if (chkPwd(pwd)) {
            return new Result<>(0);
        } else {
            return new Result<>(1);
        }
    }

    public boolean chkPwd(String pwd) throws ErrMsgException {
        com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
        int minLen = scfg.getIntProperty("password.minLen");
        int maxLen = scfg.getIntProperty("password.maxLen");
        int strenth = scfg.getIntProperty("password.strenth");

        PasswordUtil pu = new PasswordUtil();
        if (pu.check(pwd, minLen, maxLen, strenth) == 1) {
            return true;
        } else {
            throw new ErrMsgException(pu.getResultDesc(request));
        }
    }

    @ApiOperation(value = "检查人员编码", notes = "检查人员编码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "personNo", value = "人员编码", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/user/checkPersonNo", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> checkPersonNo(String personNo) {
        if (userService.isPersonNoExist(personNo)) {
            return new Result<>(1);
        } else {
            return new Result<>(0);
        }
    }

    @ApiOperation(value = "创建用户", notes = "创建用户")
    @ResponseBody
    @RequestMapping(value = "/user/create", produces = {"application/json;charset=UTF-8;"})
    public Result<Object> create(UserVO userVO, @RequestParam(value = "photo", required = false) MultipartFile file) throws ErrMsgException, IOException, ValidateException {
        chkUserName(userVO.getName(), -1);

        // 许可证验证
        License.getInstance().validate(request);

        // 检查密码与确认密码是否一致
        if (!userVO.getPassword().equals(userVO.getPassword2())) {
            throw new ValidateException("#user.pwd.confirm");
        }

        // 检查密码是否合法
        try {
            chkPwd(userVO.getPassword());
        } catch (ErrMsgException e) {
            throw new ValidateException(e.getMessage());
        }

        try {
            String pwdM55 = SecurityUtil.MD5(userVO.getPassword());
            userVO.setPwd(pwdM55);
            userVO.setPwdRaw((userVO.getPassword()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        String account = userVO.getAccount();
        if (account != null && !"".equals(account)) {
            QueryWrapper<Account> qw = new QueryWrapper<>();
            qw.eq("name", account);
            Account acc = accountService.getOne(qw, false);
            if (acc != null) {
                return new Result<>(false);
            }
        }

        return new Result<>(userService.create(userVO));
    }

    @ApiOperation(value = "修改用户", notes = "修改用户")
    @ResponseBody
    @RequestMapping(value = "/user/update", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> update(UserVO userVO, @RequestParam(value = "photo", required = false) MultipartFile file) throws ErrMsgException, IOException, ValidateException {
        // 许可证验证
        License.getInstance().validate(request);

        if (!userVO.getPassword().equals(userVO.getPassword2())) {
            throw new ValidateException("#user.pwd.confirm");
        }

        // 检查密码是否合法
        if (!"".equals(userVO.getPassword())) {
            try {
                chkPwd(userVO.getPassword());
            } catch (ErrMsgException e) {
                throw new ValidateException(e.getMessage());
            }
        }

        try {
            String pwdM55 = SecurityUtil.MD5(userVO.getPassword());
            userVO.setPwd(pwdM55);
            userVO.setPwdRaw(userVO.getPassword());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return new Result<>(userService.update(userVO));
    }

    @ApiOperation(value = "人员资料编辑 -- 权限保存", notes = "人员资料编辑 -- 权限保存")
    @ResponseBody
    @RequestMapping(value = "/user/setPrivs", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setPrivs(String userName, String privs) throws ErrMsgException, IOException, ValidateException {
        // 许可证验证
        License.getInstance().validate(request);
        String[] priv = privs.split(",");

        boolean re = userPrivService.setPrivs(userName, priv);
        return new Result<>(re);
    }

    @ApiOperation(value = "选择用户", notes = "可选择部门、角色中的用户，以及最近使用的用户", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCodes", value = "角色编码，如果有多个的话，以逗号分隔", required = true, dataType = "String"),
            @ApiImplicitParam(name = "unitCode", value = "单位编码", required = true, dataType = "String")
    })
    @RequestMapping(value = "/roleMultilSel")
    @ResponseBody
    public Result<Object> roleMultilSel(@RequestParam(required = false) String unitCode) {
        JSONObject object = new JSONObject();
        Privilege pvg = new Privilege();
        List<Role> list;
        if (unitCode == null || ("".equals(unitCode)) || pvg.isUserPrivValid(request, "admin")) {
            list = roleService.getAll();
        } else {
            list = roleService.getRolesOfUnit(unitCode, true);
        }

        return new Result<>(list);
    }

    @ApiOperation(value = "选择用户", notes = "可选择部门、角色中的用户，以及最近使用的用户", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCodes", value = "角色编码，如果有多个的话，以逗号分隔", required = true, dataType = "String"),
            @ApiImplicitParam(name = "unitCode", value = "单位编码", required = true, dataType = "String")
    })
    @RequestMapping(value = "/roleMultilSelBack")
    public String roleMultilSelBack(@RequestParam(required = false) String roleCodes, @RequestParam(required = false) String unitCode, Model model) {
        String skinPath = SkinMgr.getSkinPath(request, false);
        model.addAttribute("skinPath", skinPath);

        Privilege pvg = new Privilege();
        List<Role> list;
        if (unitCode == null || ("".equals(unitCode)) || pvg.isUserPrivValid(request, "admin")) {
            list = roleService.getAll();
        } else {
            list = roleService.getRolesOfUnit(unitCode, true);
        }

        StringBuffer optNotSelected = new StringBuffer();
        StringBuffer optSelected = new StringBuffer();

        if (roleCodes != null) {
            String[] aryCodes = StrUtil.split(roleCodes, ",");
            if (aryCodes != null) {
                int len = aryCodes.length;
                for (Role role : list) {
                    boolean isFinded = false;
                    for (int i = 0; i < len; i++) {
                        if (role.getCode().equals(aryCodes[i])) {
                            isFinded = true;
                            break;
                        }
                    }
                    if (isFinded) {
                        optSelected.append("<option value='" + role.getCode() + "'>" + role.getDescription() + "</option>");
                    } else {
                        optNotSelected.append("<option value='" + role.getCode() + "'>" + role.getDescription() + "</option>");
                    }
                }
            } else {
                for (Role role : list) {
                    optNotSelected.append("<option value='" + role.getCode() + "'>" + role.getDescription() + "</option>");
                }
            }
        }

        model.addAttribute("optNotSelected", optNotSelected);
        model.addAttribute("optSelected", optSelected);

        return "th/role_multi_sel.html";
    }

    @ApiOperation(value = "置用户的角色", notes = "置用户的角色", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "roleCode", value = "角色编码，如果有多个的话，以逗号分隔", required = true, dataType = "String")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @PostMapping(value = "/user/setRoleOfUser", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setRoleOfUser(@RequestParam(required = true) String userName, String roleCodes) throws ErrMsgException, IOException, ValidateException {
        // 许可证验证
        License.getInstance().validate(request);

        String[] roles = StrUtil.split(roleCodes, ",");

        return new Result<>(userOfRoleService.setRoleOfUser(userName, roles));
    }

    @ApiOperation(value = "删除用户", notes = "删除用户", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "用户ID，如果有多个的话，以逗号分隔", required = true, dataType = "String")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @PostMapping(value = "/user/delUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> delUsers(@RequestParam(value = "ids", required = true) String ids) throws ResKeyException, ErrMsgException {
        String[] ary = StrUtil.split(ids, ",");
        return new Result<>(userService.delUsers(ary));
    }

    @ApiOperation(value = "用户列表", notes = "用户列表", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchType", value = "realName: 姓名, userName: 帐户, account: 工号, mobile: 手机, Email: 邮件", required = false, dataType = "String"),
            @ApiImplicitParam(name = "isValid", value = "1:在职  0：离职", required = false, dataType = "String")
    })

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/list", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> list(String searchType, @RequestParam(defaultValue = "") String op, @RequestParam(defaultValue = "") String condition, @RequestParam(defaultValue = "isValid") String isValid,
                               @RequestParam(defaultValue = "regDate") String orderBy,
                               @RequestParam(defaultValue = "desc") String sort, String deptCode,
                               @RequestParam(value = "pageSize", defaultValue = "20") int pageSize, @RequestParam(value = "page", defaultValue = "1") int curPage) {
        Privilege privilege = new Privilege();
        JSONObject jobject = new JSONObject();
        String unitCode = privilege.getUserUnitCode();

        if (searchType == null || "".equals(searchType)) {
            searchType = "realName";
        }

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");

        deptCode = StrUtil.getNullStr(deptCode);

        String sql;
        if (ConstUtil.DEPT_ROOT.equals(deptCode)) {
            if ("orders".equals(orderBy)) {
                sql = "select name,orders from users u where name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM) + " and name<>" + StrUtil.sqlstr(ConstUtil.USER_ADMIN);
            }
            else {
                sql = "select name,orders," + orderBy + " from users u where name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM) + " and name<>" + StrUtil.sqlstr(ConstUtil.USER_ADMIN);
            }
            if ("search".equals(op)) {
                if ("".equals(condition)) {
                    sql += " and isValid = " + isValid;
                } else {
                    sql += " and isValid = " + isValid;
                    switch (searchType) {
                        case "realName":
                            sql += " and realName like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                        case "userName":
                            sql += " and login_name like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                        case "mobile":
                            sql += " and mobile like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                        case "account":
                            sql = "select DISTINCT u.name from users u, account a where u.name=a.username and u.name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM);
                            sql += " and isValid = " + isValid + " and a.name like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                        case "email":
                            sql += " and email like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                    }
                }
            } else {
                sql += " and isValid=" + isValid;
            }
            if (License.getInstance().isPlatformGroup()) {
                if (!unitCode.equals(ConstUtil.DEPT_ROOT)) {
                    sql += " and unit_code=" + StrUtil.sqlstr(unitCode);
                }
            }
            sql += " order by orders desc," + orderBy + " " + sort;
        } else {
            // 范围为属于某个部门
            boolean isBelongToDept = !"".equals(deptCode);
            if ("search".equals(op)) {
                // 其实当search时，deptCode始终为空，无论是否选择了部门
                /*if (isBelongToDept) {
                    // 该SQL查不到未安排部门的人
                    sql = "select DISTINCT u.name from users u,department d,dept_user du where u.name = du.user_name and d.code = du.dept_code and u.name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM);
                }
                else {*/
                if (isBelongToDept) {
                    sql = "select DISTINCT u.name,u.orders from users u,department d,dept_user du where u.name = du.user_name and d.code = du.dept_code and isValid = 1 and u.name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM);
                }
                else {
                    sql = "select u.name,u.orders from users u where name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM) + " and name<>" + StrUtil.sqlstr(ConstUtil.USER_ADMIN);
                }
                // }

                if ("".equals(condition)) {
                    sql += " and u.isValid = " + isValid;
                } else {
                    sql += " and u.isValid = " + isValid;
                    switch (searchType) {
                        case "realName":
                            sql += " and realName like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                        case "userName":
                            sql += " and u.name like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                        case "mobile":
                            sql += " and mobile like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                        case "account":
                            sql = "select DISTINCT u.name from users u, account a where u.name=a.username and u.name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM);
                            sql += " and isValid = " + isValid + " and a.name like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                        case "email":
                            sql += " and email like " + StrUtil.sqlstr("%" + condition + "%");
                            break;
                    }
                }
            } else {
                sql = "select DISTINCT u.name,u.orders from users u,department d,dept_user du where u.name = du.user_name and d.code = du.dept_code and isValid = 1 and u.name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM);
            }

            String cond = "";
            if (isBelongToDept) {
                sql += " and du.dept_code=" + StrUtil.sqlstr(deptCode);
                // 20220218 注释，不再取下级部门中的人员，因为会导致混淆，另外排序号在设置时，子部门的人员将会无效
                /*
                List<String> list = new ArrayList<String>();
                list = departmentService.getBranchDeptCode(deptCode, list);
                cond = " and ( ";
                for (int i = 0; i < list.size(); i++) {
                    String code = list.get(i);
                    if (i == 0) {
                        cond += " du.dept_code = " + StrUtil.sqlstr(code);
                    } else {
                        cond += " or du.dept_code = " + StrUtil.sqlstr(code);
                    }
                    if (i == list.size() - 1) {
                        cond += " ) ";
                    }
                }
                */
            }
            if (License.getInstance().isPlatformGroup()) {
                if (!unitCode.equals(ConstUtil.DEPT_ROOT)) {
                    sql += " and u.unit_code=" + StrUtil.sqlstr(unitCode);
                }
            }
            if (isBelongToDept && showByDeptSort) {
                orderBy = "du.orders";
                sort = "desc";
                sql += cond + " order by " + orderBy + " " + sort;
            } else {
                sql += cond + " order by u.orders desc," + orderBy + " " + sort;
            }
        }

        DebugUtil.i(getClass(), "list", sql);

        PageHelper.startPage(curPage, pageSize); // 分页查询

        JSONArray rows = new JSONArray();

        List<String> list = userService.listNameBySql(sql);
        PageInfo<String> pageInfo = new PageInfo<>(list);

        for (String userName : list) {
            User user = userService.getUser(userName);
            Account acc = accountService.getAccountByUserName(user.getName());
            UserSetup userSetup = userSetupService.getUserSetup(user.getName());
            JSONObject jo = new JSONObject();

            jo.put("user", user);
            jo.put("id", user.getId());
            jo.put("userSetup", userSetup);

            if (showByDeptSort && !"".equals(deptCode)) {
                DeptUser du = deptUserService.getDeptUser(user.getName(), deptCode);
                if (du != null) {
                    jo.put("deptOrder", String.valueOf(du.getOrders()));
                } else {
                    jo.put("deptOrder", "");
                }
            }
            jo.put("realName", user.getRealName());
            if (acc != null) {
                jo.put("account", acc.getName());
            } else {
                jo.put("account", "");
            }
            // jo.put("sex", user.getGender() != null ? (!user.getGender() ? "男" : "女") : "");
            jo.put("gender", user.getGender() != null ? user.getGender() : true);
            jo.put("mobile", user.getMobile());

            StringBuffer deptNames = new StringBuffer();
            List<DeptUser> duList = deptUserService.listByUserName(user.getName());
            for (DeptUser du : duList) {
                Department dept = departmentCache.getDepartment(du.getDeptCode());
                String deptName;
                if (dept != null) {
                    if (!ConstUtil.DEPT_ROOT.equals(dept.getParentCode()) && !ConstUtil.DEPT_ROOT.equals(dept.getCode())) {
                        Department parentDept = departmentCache.getDepartment(dept.getParentCode());
                        if (parentDept != null) {
                            deptName = parentDept.getName() + dept.getName();
                        } else {
                            deptName = dept.getName();
                        }
                    } else {
                        deptName = dept.getName();
                    }
                    StrUtil.concat(deptNames, "，", deptName);
                } else {
                    // deptName = du.getDeptCode() + " 不存在";
                    deptUserService.del(du);
                }
            }
            jo.put("deptNames", deptNames.toString());

            List<UserOfRole> uorList = userOfRoleService.listByUserName(user.getName());
            String roleDescs = "";
            List<String> roleList = new ArrayList<>();
            for (UserOfRole uor : uorList) {
                Role role = roleService.getRole(uor.getRoleCode());
                if (role == null) {
                    continue;
                }
                if (role.getCode().equals(ConstUtil.ROLE_MEMBER)) {
                    continue;
                }
                roleList.add(role.getCode());
                if ("".equals(roleDescs)) {
                    roleDescs = role.getDescription();
                } else {
                    roleDescs += "," + role.getDescription();
                }
            }
            jo.put("roleCodes", roleList.toArray());
            jo.put("roleNames", roleDescs);

            /*StringBuffer sbGroupRoleDesc = new StringBuffer();
            // 取得用户所有的组
            List<UserOfGroup> ugList = userOfGroupService.listByUserName(userName);
            for (UserOfGroup userOfGroup : ugList) {
                // 取得组信息
                Group group = userGroupService.getGroup(userOfGroup.getGroupCode());
                if (group == null) {
                    DebugUtil.w(getClass(), "list", "用户: " + userName + " 所属的组 " + userOfGroup.getGroupCode() + " 已不存在");
                }
                else {
                    List<GroupOfRole> grList = userGroupOfRoleService.listByGroupCode(group.getCode());
                    for (GroupOfRole groupOfRole : grList) {
                        // 取得组所属的角色
                        Role role = roleService.getRole(groupOfRole.getRoleCode());
                        StrUtil.concat(sbGroupRoleDesc, ",", group.getDescription() + ":" + role.getDescription());
                    }
                }
            }
            jo.put("groupRoleDesc", sbGroupRoleDesc);*/

            rows.add(jo);
        }

        jobject.put("list", rows);
        jobject.put("page", curPage);
        jobject.put("total", pageInfo.getTotal());
        return new Result<>(jobject);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/changeOrder", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> changeOrder(@RequestParam(value = "id", required = true) int id, String colName, String original_value, String update_value, String deptCode) {
        if (update_value.equals(original_value)) {
            return new Result<>(false);
        }
        boolean re = false;

        User user = userService.getById(id);
        DeptUser deptUser = deptUserService.getDeptUser(user.getName(), deptCode);
        if (deptUser != null) {
            deptUser.setOrders(StrUtil.toInt(update_value, 0));
            re = deptUser.updateById();
        }

        return new Result<>(re);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/enableBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> enableBatch(@NotEmpty(message = "{user.id.empty}") @RequestParam(value = "ids", required = true) String ids) {
        boolean re = true;
        String[] ary = StrUtil.split(ids, ",");
        for (String id : ary) {
            re = userService.reEmploryment(StrUtil.toInt(id), SpringUtil.getUserName());
        }
        return new Result<>(re);
    }

    /**
     * 调出
     *
     * @param ids
     * @param deptCodes
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/changeDepts", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> changeDepts(@NotEmpty(message = "{user.id.empty}") @RequestParam(value = "ids", required = true) String ids, String deptCodes) throws ErrMsgException {
        boolean re = false;
        String[] ary = StrUtil.split(ids, ",");
        String curUserName = SpringUtil.getUserName();
        for (int i = 0; i < ary.length; i++) {
            User user = userService.getById(StrUtil.toInt(ary[i]));
            re = deptUserService.changeDeptOfUser(user.getName(), deptCodes, curUserName);
        }
        return new Result<>(re);
    }

    /**
     * 调入
     *
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/transferUsers", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> transferUsers(String userNames, String deptCode) throws ErrMsgException, ValidateException {
        boolean re = false;

        String[] ary = StrUtil.split(userNames, ",");
        // 当只选了单个用户时检查，如果是批量调动则不检查
        if (ary.length == 1) {
            User user = userService.getUser(userNames);
            if (user == null) {
                throw new ValidateException("用户 " + userNames + " 不存在！");
            } else {
                // 检查用户是否已处于该部门
                if (deptUserService.isUserOfDept(userNames, deptCode)) {
                    throw new ValidateException("用户 " + user.getRealName() + " 已在该部门中！");
                }
            }
        }

        for (int i = 0; i < ary.length; i++) {
            re = deptUserService.changeDeptOfUser(ary[i], deptCode, new Privilege().getUser(request));
        }

        return new Result<>(re);
    }

    @ApiOperation(value = "完善用户的邮件信息", notes = "完善用户的邮件信息", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "邮箱", required = true, dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/user/setUserInfo", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setUserInfo(@RequestParam(value = "email", required = true) String email) {
        boolean re = true;
        String userName = SpringUtil.getUserName();
        User user = userService.getUser(userName);
        user.setEmail(email);
        re = user.updateById();
        return new Result<>(re);
    }

    /**
     * 批量绑定手机
     *
     * @param ids
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/bindBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> bindBatch(@NotEmpty(message = "{user.id.empty}") @RequestParam(value = "ids", required = true) String ids) {
        boolean re = true;
        String[] ary = StrUtil.split(ids, ",");
        for (int i = 0; i < ary.length; i++) {
            User user = userService.getById(StrUtil.toInt(ary[i]));
            UserSetup userSetup = userSetupService.getUserSetup(user.getName());
            userSetup.setIsBindMobile(1);
            re = userSetupService.updateByUserName(userSetup);
        }

        return new Result<>(re);
    }

    /**
     * 批量解绑手机
     *
     * @param ids
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/unbindBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> unbindBatch(@NotEmpty(message = "{user.id.empty}") @RequestParam(value = "ids", required = true) String ids) {
        boolean re = true;
        String[] ary = StrUtil.split(ids, ",");
        for (int i = 0; i < ary.length; i++) {
            User user = userService.getById(StrUtil.toInt(ary[i]));
            UserSetup userSetup = userSetupService.getUserSetup(user.getName());
            userSetup.setIsBindMobile(1);
            re = userSetupService.updateByUserName(userSetup);
            userMobileService.delByUserName(user.getName());
        }
        return new Result<>(re);
    }

    /**
     * 同步用户
     *
     * @param op
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/synAll", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> synAll(String op) {
        String userName = SpringUtil.getUserName();
        if ("syncWeixin".equals(op)) { // 一键同步OA部门人员至微信端
            WeixinDo weixinDo = new WeixinDo();
            weixinDo.syncDeptUsers();
        } else if ("syncWeixinToOA".equals(op)) {
            WeixinDo weixinDo = new WeixinDo();
            weixinDo.syncWxDeptUserToOA();
        } else if ("syncOAToDingding".equals(op)) {
            DingDingClient client = new DingDingClient();
            client.syncOAtoDingDing();
        } else if ("syncDingdingToOA".equals(op)) {
            DingDingClient client = new DingDingClient();
            client.syncDingDingToOA();
        } else if ("syncDing".equals(op)) {
            //同步钉钉账户所有人员信息至user表中dingding字段
            DingDingClient.batchUserAddDingDing();
        } else if ("sync".equals(op)) {
            SyncUtil su = new SyncUtil();
            // 先删除
            su.allDelete();

            List<Department> list = departmentService.getDeptsWithouRoot();
            for (Department dept : list) {
                // 先加部门
                su.orgSync(dept, SyncUtil.CREATE, new Privilege().getUser(request));

                String deptCode = dept.getCode();

                List<DeptUser> duList = deptUserService.listByDeptCode(deptCode);
                for (DeptUser deptUser : duList) {
                    if (deptUser.getUserName().equals(ConstUtil.USER_ADMIN)) {
                        continue;
                    }
                    su.userSync(deptUser.getUserName(), deptCode, SyncUtil.CREATE, new Privilege().getUser(request));
                }
            }
        }

        return new Result<>(true);
    }

    /**
     * 多部门选择
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/deptMultiSel")
    public String deptMultiSel(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        Privilege privilege = new Privilege();
        String deptCodes = ParamUtil.get(request, "deptCodes");

        String[] deptSelected = StrUtil.split(deptCodes, ",");
        if (deptSelected == null) {
            deptSelected = new String[]{};
        }
        ArrayList<String> deptSelectedList = new ArrayList<String>(Arrays.asList(deptSelected));
        model.addAttribute("deptSelectedList", deptSelectedList);

        boolean isOpenAll = true; // 展开所有节点
        String treeJsonData = departmentService.getJsonString(isOpenAll, true);

        if ("".equals(deptCodes)) {
            deptCodes = privilege.getUserUnitCode(request);
        }
        model.addAttribute("deptCode", deptCodes);
        model.addAttribute("treeJsonData", treeJsonData);

        return "th/dept_multi_sel";
    }

    /**
     * 设置用户管理的部门
     *
     * @param userName
     * @param deptCodes
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/setUserAdminDept", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setUserAdminDept(@NotEmpty(message = "{user.name.notempty}") String userName, String deptCodes) {
        return new Result<>(userAdminDeptService.setUserAdminDept(userName, deptCodes));
    }

    @RequestMapping(value = "/user/controlPanel")
    @ResponseBody
    public Result<Object> controlPanel() {
        JSONObject object = new JSONObject();

        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        boolean isLarkUsed = cfg.getBooleanProperty("isLarkUsed");
        boolean isIntegrateEmail = cfg.getBooleanProperty("isIntegrateEmail");
        object.put("isLarkUsed", isLarkUsed);
        object.put("isIntegrateEmail", isIntegrateEmail);

        String oldSkinCode = UserSet.getSkin(request);
        object.put("oldSkinCode", oldSkinCode);

        StringBuilder skinOpts = new StringBuilder();
        com.redmoon.oa.ui.SkinMgr sm = new com.redmoon.oa.ui.SkinMgr();
        Iterator irskin = sm.getAllSkin().iterator();

        object.put("skinOpts", irskin);

        String userName = SpringUtil.getUserName();
        User user = userService.getUser(userName);
        UserSetup userSetup = userSetupService.getUserSetup(userName);
        object.put("user", user);
        object.put("userSetup", userSetup);

        boolean isSpecified = "specified".equals(cfg.get("styleMode")) || "2".equals(cfg.get("styleMode"));
        int uiMode = 0;
        if (isSpecified) {
            uiMode = StrUtil.toInt(cfg.get("styleSpecified"), -1);
        } else {
            uiMode = userSetup.getUiMode();
        }
        object.put("isSpecified", isSpecified);
        object.put("uiMode", uiMode);
        object.put("menuMode", userSetup.getMenuMode());

        String skinCode = userSetup.getSkinCode();
        object.put("skinCode", skinCode);

        boolean isDisplaySlideMenu = true;
        if (uiMode == ConstUtil.UI_MODE_LTE || uiMode == ConstUtil.UI_MODE_PROFESSION || uiMode == ConstUtil.UI_MODE_NONE) {
            isDisplaySlideMenu = false;
        }
        object.put("isDisplaySlideMenu", isDisplaySlideMenu);

        String diskSpaceUsed = UtilTools.getFileSize(user.getDiskSpaceUsed());
        String diskSpaceAllowed = UtilTools.getFileSize(user.getDiskSpaceAllowed());
        object.put("diskSpaceUsed", diskSpaceUsed);
        object.put("diskSpaceAllowed", diskSpaceAllowed);
        String msgSpaceAllowed = UtilTools.getFileSize(userSetup.getMsgSpaceAllowed());
        object.put("msgSpaceAllowed", msgSpaceAllowed);
        String msgSpaceUsed = UtilTools.getFileSize(userSetup.getMsgSpaceUsed());
        object.put("msgSpaceUsed", msgSpaceUsed);

        object.put("UI_MODE_NONE", ConstUtil.UI_MODE_NONE);
        object.put("UI_MODE_PROFESSION", ConstUtil.UI_MODE_PROFESSION);
        object.put("UI_MODE_FASHION", ConstUtil.UI_MODE_FASHION);
        object.put("UI_MODE_FLOWERINESS", ConstUtil.UI_MODE_FLOWERINESS);
        object.put("UI_MODE_PROFESSION_NORMAL", ConstUtil.UI_MODE_PROFESSION_NORMAL);
        object.put("UI_MODE_LTE", ConstUtil.UI_MODE_LTE);

        object.put("MENU_MODE_NEW", ConstUtil.MENU_MODE_NEW);
        object.put("MENU_MODE_NORMAL", ConstUtil.MENU_MODE_NORMAL);

        boolean isArchiveShowAsUserInfo = cfg.getBooleanProperty("isArchiveShowAsUserInfo");
        if (isArchiveShowAsUserInfo) {
            long personId = personBasicService.getIdByUserName(SpringUtil.getUserName());
            if (personId == -1) {
                isArchiveShowAsUserInfo = false;
                DebugUtil.d(getClass(), "", SpringUtil.getUserName() + "'s personbasic info is not exist.");
            } else {
                object.put("personId", personId);
                String visitKey = com.redmoon.oa.security.SecurityUtil.makeVisitKey(personId);
                object.put("userInfoUrl", request.getContextPath() + "/visual/moduleShowPage.do?moduleCode=personbasic&id=" + personId + "&visitKey=" + visitKey);
            }
        }
        if (!isArchiveShowAsUserInfo) {
            object.put("userInfoUrl", request.getContextPath() + "/user/editUser.do");
        }
        return new Result<>(object);
    }

    /**
     * 更改密码
     *
     * @param pwd
     * @param pwd2
     * @param pwd3
     * @return
     * @throws ValidateException
     */
    @ResponseBody
    @RequestMapping(value = "/user/changePwd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> changePwd(@RequestParam(required = true) String pwd, @RequestParam(required = true) String pwd2, @RequestParam(required = true) String pwd3) throws ValidateException {
        User user = userService.getUser(authUtil.getUserName());
        String pwdMd5Old = user.getPwd();

        String pwdMD5 = "";
        try {
            pwdMD5 = SecurityUtil.MD5(pwd3);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }

        if (!pwdMd5Old.equals(pwdMD5)) {
            throw new ValidateException("您输入的旧密码有误，请重新输入！");
        }
        if (!pwd2.equals(pwd)) {
            throw new ValidateException("您输入的两次密码不匹配，请重新输入！");
        }
        if (pwd.length() < 1 || pwd.length() > 20) {
            throw new ValidateException("密码长度必须在1~20之间，请重新输入！");
        }

        try {
            chkPwd(pwd);
        } catch (ErrMsgException e) {
            throw new ValidateException(e.getMessage());
        }

        return new Result<>(userService.modifyPwd(authUtil.getUserName(), pwd));
    }

    /**
     * 语方设置
     *
     * @param local
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/changeLang", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> changeLang(@RequestParam(required = true) String local) {

        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setLocal(local);

        return new Result<>(userSetupService.updateByUserName(userSetup));
    }

    /**
     * 邮箱设置
     *
     * @param emailName
     * @param emailPwd
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/setEmail", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setEmail(String emailName, String emailPwd) {

        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setEmailName(emailName);
        userSetup.setEmailPwd(emailPwd);

        return new Result<>(userSetupService.updateByUserName(userSetup));
    }

    /**
     * 个性化设置
     *
     * @param isMsgWinPopup
     * @param isMessageSoundPlay
     * @param isChatIconShow
     * @param isChatSoundPlay
     * @param isWebedit
     * @param isShowSidebar
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/setIndividuality", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> setIndividuality(Integer isMsgWinPopup, Integer isMessageSoundPlay, Integer isChatIconShow, Integer isChatSoundPlay, Integer isWebedit, Integer isShowSidebar) {

        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setIsMsgWinPopup(isMsgWinPopup == 1);
        userSetup.setIsMessageSoundPlay(isMessageSoundPlay);
        userSetup.setIsChatIconShow(isChatIconShow == 1);
        userSetup.setIsChatSoundPlay(isChatSoundPlay == 1);
        userSetup.setIsWebedit(isWebedit);
        userSetup.setIsShowSidebar(isShowSidebar);

        return new Result<>(userSetupService.updateByUserName(userSetup));
    }

    /**
     * 界面设置
     *
     * @param skinCode
     * @param uiMode
     * @param menuMode
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/setStyleMode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> setStyleMode(HttpServletResponse response, String skinCode, Integer uiMode, Integer menuMode) {

        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setSkinCode(skinCode);
        userSetup.setUiMode(uiMode);
        userSetup.setMenuMode(menuMode);

        JSONObject json = responseUtil.getResultJson(userSetupService.updateByUserName(userSetup));

        if (uiMode == UserSetupDb.UI_MODE_LTE) {
            UserSet.setSkin(request, response, SkinMgr.SKIN_CODE_LTE);
        } else {
            UserSet.setSkin(request, response, skinCode);
        }

        String page = loginService.getUIModePage("");
        json.put("page", page);

        return new Result<>(json);
    }

    @ApiOperation(value = "修改用户", notes = "修改用户", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/user/editUser")
    public Result<Object> editUser() {
        JSONObject object = new JSONObject();
        String userName = authUtil.getUserName();

        User user = userCache.getUser(userName);
        object.put("user", user);

        String selectDeptCode = ParamUtil.get(request, "selectDeptCode");
        object.put("selectDeptCode", selectDeptCode);

        StringBuffer sbDeptName = new StringBuffer();
        List<Department> list = departmentService.getDeptsOfUser(userName);
        for (Department department : list) {
            StrUtil.concat(sbDeptName, ",", department.getName());
        }
        object.put("deptName", sbDeptName.toString());

        List<Role> roleList = roleService.getAllRolesOfUser(user.getName(), false);
        StringBuffer roleDescs = new StringBuffer();
        for (Role role : roleList) {
            StrUtil.concat(roleDescs, "，", role.getDescription());
        }
        object.put("roleName", roleDescs.toString());

        object.put("isPlatformSrc", com.redmoon.oa.kernel.License.getInstance().isPlatformSrc());
        object.put("isGov", License.getInstance().isGov());

        com.redmoon.oa.Config cfg = Config.getInstance();
        boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");
        String accountName = "";
        if (isUseAccount) {
            Account account = accountService.getAccountByUserName(userName);
            if (account != null) {
                accountName = account.getName();
            }
        }
        object.put("isUseAccount", isUseAccount);
        object.put("account", accountName);

        boolean isMobileNotEmpty = false;
        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        if (weixinCfg.getBooleanProperty("isUse") || dingdingCfg.isUseDingDing()) {
            isMobileNotEmpty = true;
        }
        object.put("isMobileNotEmpty", isMobileNotEmpty);

        StringBuffer typeOpts = new StringBuffer();
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("user_type");
        Vector<SelectOptionDb> vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        for (SelectOptionDb sod : vType) {
            String selected = "";
            if (sod.isDefault()) {
                selected = "selected";
            }
            String clr = "";
            if (!"".equals(sod.getColor())) {
                clr = " style='color:" + sod.getColor() + "' ";
            }
            typeOpts.append("<option value='" + sod.getValue() + "'" + selected + clr + ">" + sod.getName() + "</option>");
        }
        object.put("userTypeOpts", typeOpts);

        String leaders = "";
        String leaderNames = "";
        UserSetup userSetup = userSetupService.getUserSetup(user.getName());
        if (userSetup != null) {
            leaders = StrUtil.getNullStr(userSetup.getMyleaders());
            if (!leaders.equals("")) {
                String[] leadersAry = StrUtil.split(leaders, ",");
                for (int i = 0; i < leadersAry.length; i++) {
                    User myUser = userService.getUser(leadersAry[i]);
                    if (myUser == null) {
                        continue;
                    }
                    leaderNames += (leaderNames.equals("") ? "" : ",") + myUser.getRealName();
                }
            }
        }
        object.put("leaders", leaders);
        object.put("leaderNames", leaderNames);

        object.put("isMyLeaderUsed", cfg.getBooleanProperty("isMyLeaderUsed"));

        return new Result<>(object);
    }

    /**
     * 更新用户的信息
     *
     * @param userVO
     * @param file
     * @return
     * @throws ErrMsgException
     * @throws IOException
     * @throws ValidateException
     */
    @ApiOperation(value = "修改用户的信息", notes = "修改用户的信息", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/user/updateMyInfo", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> updateMyInfo(UserVO userVO, @RequestParam(value = "photo", required = false) MultipartFile file) throws ErrMsgException, IOException, ValidateException {
        // 许可证验证
        License.getInstance().validate(request);

        // 防止垂直越权漏洞，用户只能改自己的
        userVO.setName(authUtil.getUserName());

        return new Result<>(userService.updateMyInfo(userVO));
    }

    @ApiOperation(value = "重置头像", notes = "重置头像", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名称", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/user/resetPortrait", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> resetPortrait(String userName) throws ValidateException {
        if (!SpringUtil.getUserName().equals(userName)) {
            Privilege pvg = new Privilege();
            if (!pvg.isUserPrivValid(SpringUtil.getUserName(), "admin.user")) {
                throw new ValidateException("权限非法");
            }
        }
        User user = userService.getUser(userName);
        user.setPhoto("");
        return new Result<>(userService.updateByUserName(user));
    }

    @ApiOperation(value = "修改初始密码", notes = "修改初始密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pwd", value = "密码", dataType = "String"),
            @ApiImplicitParam(name = "confirmPwd", value = "确认密码", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/user/updateInitPwd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> updateInitPwd(String pwd, String confirmPwd) throws ValidateException {
        String userName = SpringUtil.getUserName();
        if (!pwd.equals(confirmPwd)) {
            throw new ValidateException("密码与确认密码不一致！");
        }

        if (pwd.length() < 1 || pwd.length() > 20) {
            throw new ValidateException("密码长度必须在1~20之间，请重新输入！");
        }

        try {
            chkPwd(pwd);
        } catch (ErrMsgException e) {
            throw new ValidateException(e.getMessage());
        }

        return new Result<>(userService.modifyPwd(userName, pwd));
    }

//    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
//    @RequestMapping(value = "/admin/organize/userImport")
//    public String userImport(Model model) {
//        object.put("skinPath", SkinMgr.getSkinPath(request, false));
//
//        return "th/admin/organize/user_import";
//    }

    // 设置错误信息
    public String setPrompt(String prompt, String msg) {
        if (prompt.contains(msg)) {
            return prompt;
        }
        if ("".equals(prompt)) {
            prompt = msg;
        } else {
            prompt += "," + msg;
        }
        return prompt;
    }

    @RequestMapping(value = "/admin/organize/userImportConfirm", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> userImportConfirm(@RequestParam(value = "att1", required = false) MultipartFile file, Model model) throws ValidateException {
        if (file == null) {
            throw new ValidateException("请上传excel文件");
        }

        JSONObject json = new JSONObject();
        if (file.getSize() == 0) {
            json.put("isShowBack", true);
            json.put("msg", "请上传文件");
            return new Result<>(json);
        }

        json.put("ret", 1);

        InputStream in = null;
        try {
            in = file.getInputStream();
        } catch (IOException e) {
            json.put("ret", 0);
            json.put("msg", "读取文件异常");
            LogUtil.getLog(getClass()).error(e);
        }
        String[][] info = null;
        int cols = 19;
        try {
            if (file.getOriginalFilename().endsWith("xls")) {
                HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
                HSSFSheet sheet = w.getSheetAt(0);
                if (sheet != null) {
                    // 获取行数
                    int rowcount = sheet.getLastRowNum();
                    info = new String[rowcount][cols];
                    // 获取每一行
                    for (int k = 1; k <= rowcount; k++) {
                        HSSFRow row = sheet.getRow(k);
                        if (row != null) {
                            for (int i = 0; i < cols; i++) {
                                HSSFCell cell = row.getCell(i);
                                if (cell == null) {
                                    info[k - 1][i] = "";
                                } else {
                                    cell.setCellType(CellType.STRING);
                                    info[k - 1][i] = StrUtil.getNullStr(cell.getStringCellValue()).trim();
                                }
                            }
                        }
                    }
                }
            } else if (file.getOriginalFilename().endsWith("xlsx")) {
                XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
                XSSFSheet sheet = w.getSheetAt(0);
                if (sheet != null) {
                    int rowcount = sheet.getLastRowNum();
                    info = new String[rowcount][cols];
                    for (int k = 1; k <= rowcount; k++) {
                        XSSFRow row = sheet.getRow(k);
                        if (row != null) {
                            for (int i = 0; i < cols; i++) {
                                XSSFCell cell = row.getCell(i);
                                if (cell == null) {
                                    info[k - 1][i] = "";
                                } else {
                                    cell.setCellType(CellType.STRING);
                                    info[k - 1][i] = StrUtil.getNullStr(cell.getStringCellValue()).trim();
                                }
                            }
                        }
                    }
                }
            } else {
                json.put("ret", 0);
                json.put("msg", "文件格式错误");
            }
        } catch (IOException e) {
            json.put("ret", 0);
            json.put("msg", "导入文件出现异常");
            log.error("导入文件出现异常");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("导入文件出现异常");
                }
            }
        }

        if (json.getIntValue("ret") == 0) {
            json.put("msg", json.getString("msg"));
            return new Result<>(json);
        }

        String name = "";
        String realName = "";
        String mobile = "";
        String gender = "";
        String personNO = "";
        String dept = "";
        String prompt = "";
        String roleName = "";

        String accountName = "";
        String postName = "";
        String idCard = "";
        String marriaged = "";
        String email = "";
        String birthday = "";
        String QQ = "";
        String phone = "";
        String entryDate = "";
        String shortMobile = "";
        String hobbies = "";
        String address = "";

        JSONArray successArr = new JSONArray();
        JSONArray failArr = new JSONArray();
        HashMap<String, String> userMap = new HashMap<String, String>();
        HashMap<String, String> cellMap = new HashMap<String, String>();
        HashMap<String, String> noMap = new HashMap<String, String>();

        HashMap<String, String> accountNameMap = new HashMap<String, String>();

        //判断工号是否启用
        Config cf = new Config();
        boolean isUseAccount = cf.getBooleanProperty("isUseAccount");

        StringBuilder errMsg = new StringBuilder();
        for (int i = 0; i < info.length; i++) {
            name = info[i][0];
            realName = info[i][1];
            accountName = info[i][2];

            // excel中可能会出现行为null或空，用格式刷后有可能出现这种情况
            if ((name == null && realName == null) || ("".equals(name) && "".equals(realName))) {
                continue;
            }

            mobile = info[i][3];
            gender = info[i][4];
            personNO = info[i][5];
            dept = info[i][6];
            roleName = info[i][7];

            postName = info[i][8];
            idCard = info[i][9];
            marriaged = info[i][10];
            birthday = info[i][11];
            email = info[i][12];
            QQ = info[i][13];
            phone = info[i][14];
            entryDate = info[i][15];
            shortMobile = info[i][16];
            hobbies = info[i][17];
            address = info[i][18];

            String nameCol = "";
            String realNameCol = "";
            String mobileCol = "";
            String personNOCol = "";
            String deptCol = "";
            String accountNameCol = "";
            //帐号校验
            String sql = "select name from users where name=" + StrUtil.sqlstr(name);
            if ("".equals(name)) {
                prompt = setPrompt(prompt, "帐户不能为空");
                nameCol = "1";
            } else {
                if (name.length() > 20) {
                    prompt = setPrompt(prompt, "帐号不能大于20个字符");
                    nameCol = "1";
                }
            }
            if (!nameCol.equals("1")) {
                if (userMap.containsKey(name)) {
                    // 判断是否为同一个人
                    if (!userMap.get(name).equals(realName)) {
                        prompt = setPrompt(prompt, "导入文件中存在相同帐号");
                        nameCol = "1";
                    }
                } else {
                    userMap.put(name, realName);
                }
            }
            //判断工号是否启用，如果启用进入判断，如果不启用不判断
            if (isUseAccount) {
                if (StrUtil.isEmpty(accountName)) {
                    prompt = setPrompt(prompt, "工号为空");
                    accountNameCol = "1";
                } else {
                    if (accountNameMap.containsKey(accountName)) {
                        // 判断是否为同一个人
                        if (!accountNameMap.get(accountName).equals(name)) {
                            prompt = setPrompt(prompt, "导入文件中存在相同工号");
                            accountNameCol = "1";
                        }
                    } else {
                        accountNameMap.put(accountName, name);
                    }
                }

            }

            //姓名校验
            if ("".equals(realName)) {
                prompt = setPrompt(prompt, "姓名不能为空");
                realNameCol = "1";
            } else {
                if (realName.length() > 20) {
                    prompt = setPrompt(prompt, "姓名不能大于20个字符");
                    realNameCol = "1";
                }
            }
            //手机校验
            if (!"".equals(mobile)) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("^\\d{11}$");
                java.util.regex.Matcher m = p.matcher(mobile);
                if (!m.matches()) {
                    prompt = setPrompt(prompt, "手机号非法");
                    mobileCol = "1";
                }
                if (!"1".equals(mobileCol)) {
                    if (cellMap.containsKey(mobile)) {
                        // 判断是否为同一个人
                        if (!cellMap.get(mobile).equals(name)) {
                            prompt = setPrompt(prompt, "导入文件中存在相同手机号");
                            mobileCol = "1";
                        }
                    } else {
                        cellMap.put(mobile, name);
                    }
                }
            }

            // 员工编号校验
            if (!"".equals(personNO)) {
                if (personNO.length() > 20) {
                    prompt = setPrompt(prompt, "员工编号不能大于20个字符");
                    personNOCol = "1";
                } else {
                    if (userService.isPersonNoUsedByOther(name, personNO)) {
                        prompt = setPrompt(prompt, "员工编号已存在");
                        personNOCol = "1";
                    }
                }
                if (!personNOCol.equals("1")) {
                    if (noMap.containsKey(personNO)) {
                        // 判断是否为同一个人
                        if (!noMap.get(personNO).equals(name)) {
                            prompt = setPrompt(prompt, "导入文件中存在相同的人员编号");
                            personNOCol = "1";
                        }
                    } else {
                        noMap.put(personNO, name);
                    }
                }
            }

            //部门校验
            if ("".equals(dept)) {
                prompt = setPrompt(prompt, "部门不能为空");
                deptCol = "1";
            }

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("name", name);
            jsonObj.put("realName", realName);
            jsonObj.put("accountName", accountName);
            jsonObj.put("mobile", mobile);
            jsonObj.put("personNO", personNO);
            jsonObj.put("dept", dept);
            jsonObj.put("gender", gender);
            jsonObj.put("roleName", roleName);
            jsonObj.put("postName", postName);
            jsonObj.put("idCard", idCard);
            jsonObj.put("marriaged", marriaged);
            jsonObj.put("birthday", birthday);
            jsonObj.put("email", email);
            jsonObj.put("QQ", QQ);
            jsonObj.put("phone", phone);
            jsonObj.put("entryDate", entryDate);
            jsonObj.put("shortMobile", shortMobile);
            jsonObj.put("hobbies", hobbies);
            jsonObj.put("address", address);
            jsonObj.put("prompt", prompt);
            if (!"".equals(prompt)) {
                StrUtil.concat(errMsg, "\n", realName + ": " + prompt);

                jsonObj.put("name_err", "1".equals(nameCol));
                jsonObj.put("realName_err", "1".equals(realNameCol));
                jsonObj.put("accountName_err", "1".equals(accountNameCol));
                jsonObj.put("mobile_err", "1".equals(mobileCol));
                jsonObj.put("personNO_err", "1".equals(personNOCol));
                jsonObj.put("dept_err", "1".equals(deptCol));

                failArr.add(jsonObj);
            } else {
                successArr.add(jsonObj);
            }
            prompt = "";
        }

        json.put("infoLen", info.length);
        json.put("successArr", successArr);
        json.put("failArr", failArr);
        if (errMsg.length() > 0) {
            json.put("res", 1);
        } else {
            json.put("res", 0);
        }
        json.put("msg", errMsg.toString());

        return new Result<>(json);
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/admin/organize/userImportFinish", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> userImportFinish(String info) {
        String errMsg = "";
        JSONArray arr = JSONArray.parseArray(info);
        boolean re = false;
        try {
            re = userService.importUser(arr);
        } catch (ValidateException | ErrMsgException | IOException e) {
            errMsg = e.getMessage();
            LogUtil.getLog(getClass()).error(e);
        }

        JSONObject json = responseUtil.getResultJson(re);
        if (!"".equals(errMsg)) {
            json.put("msg", errMsg);
        }
        return new Result<>(json);
    }

    @RequestMapping("/admin/exportUser")
    public void exportUser(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel");
        String fileName = "全部人员.xls";
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);

        OutputStream os = response.getOutputStream();
        WritableWorkbook wwb = null;
        WritableSheet ws = null;

        int isValid = ParamUtil.getInt(request, "isValid", 1);
        try {
            String[] preinfo = {"帐号", "姓名", "工号", "手机", "性别", "员工编号"};
            String[] depts = {"部门一级", "部门二级", "部门三级", "部门四级", "部门五级", "部门六级", "部门七级", "部门八级", "部门九级", "部门十级", "部门十一级", "部门十二级"};
            String[] sufinfo = {"角色", "职位", "身份证", "婚否", "出生日期", "邮箱", "QQ", "电话", "入职日期", "短号", "兴趣爱好", "地址"};

            // 创建excel
            wwb = jxl.Workbook.createWorkbook(os);
            ws = wwb.createSheet("全体人员", 0);

            Config cfg = new Config();
            int maxLevel = 12;
            int deptLevels = cfg.getInt("export_dept_levels");
            if (deptLevels > maxLevel) {
                deptLevels = maxLevel;
            } else if (deptLevels < 0) {
                deptLevels = 4;
            }

            // 表头
            String title = "";
            Label a = null;
            // 部门前面的信息
            for (int i = 0; i < preinfo.length; i++) {
                a = new Label(i, 0, preinfo[i]);
                ws.addCell(a);
            }
            // 部门
            if (deptLevels == 0) {
                depts[0] = "部门";
            }
            for (int i = 0; i < (deptLevels == 0 ? 1 : deptLevels); i++) {
                a = new Label(preinfo.length + i, 0, depts[i]);
                ws.addCell(a);
            }
            // 部门后面的信息
            for (int i = 0; i < sufinfo.length; i++) {
                a = new Label(preinfo.length + (deptLevels == 0 ? 1 : deptLevels) + i, 0, sufinfo[i]);
                ws.addCell(a);
            }

            String account = "", realName = "", mobile = "", gender = "", personNo = "", idCard = "", married = "", email = "", QQ = "", phone = "", entryDate = "", shortMobile = "", hobbies = "", address = "";
            String postName = "";
            // 内容
            String sql = "select users.id,login_name,dept_code,realname,case gender when 0 then '男' when 1 then'女' end g,date_format(birthday,'%Y-%m-%d') d,'' s,mobile,idcard,ISMARRIAGED,person_no,phone,'' fax,address,mobile,email,qq,msn,hobbies,address,entryDate,name from users,dept_user where name=dept_user.user_name and isvalid=" + isValid + " order by dept_code asc,dept_user.orders asc";
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = null;
            ri = jt.executeQuery(sql);
            int row = 1;
            HashMap<String, java.lang.Boolean> map = new HashMap<String, java.lang.Boolean>();
            while (ri.hasNext()) {
                int col = 0;
                ResultRecord rr = ri.next();
                String userName = rr.getString("name");
                String loginName = rr.getString(2);
                if (map.containsKey(loginName)) {
                    continue;
                } else {
                    map.put(loginName, true);
                }

                a = new Label(col++, row, rr.getString("login_name"));
                ws.addCell(a);

                a = new Label(col++, row, rr.getString("realname"));
                ws.addCell(a);

                Account acc = accountService.getAccountByUserName(userName);
                if (acc != null) {
                    account = acc.getName();
                }

                a = new Label(col++, row, account);
                ws.addCell(a);

                a = new Label(col++, row, StrUtil.getNullStr(rr.getString("mobile")));
                ws.addCell(a);

                a = new Label(col++, row, StrUtil.getNullStr(rr.getString("g")));
                ws.addCell(a);

                a = new Label(col++, row, StrUtil.getNullStr(rr.getString("person_no")));
                ws.addCell(a);

                // String deptCode = rr.getString(3);
                // 获取各级部门名称
                // 增加多部门导出
                String[] deptNames = new String[maxLevel];
                String depts_ = "";
                String sqlDepts = "select * from dept_user where user_name = ?";
                ResultIterator resultIterator = jt.executeQuery(sqlDepts, new Object[]{userName});
                while (resultIterator.hasNext()) {
                    ResultRecord resultRecord = resultIterator.next();
                    String deptCode = resultRecord.getString("dept_code");
                    DeptDb dd = new DeptDb(deptCode);
                    deptNames[0] = dd.getName();
                    int j = 0;
                    while (!dd.getParentCode().equals("-1")
                            && !dd.getParentCode().equals(DeptDb.ROOTCODE)
                            && j < maxLevel - 1) {
                        dd = new DeptDb(dd.getParentCode());
                        if (!dd.getParentCode().equals("")) {
                            deptNames[++j] = dd.getName();
                        } else {
                            break;
                        }
                    }
                    LogUtil.getLog(getClass()).info("deptNames=" + StringUtils.join(deptNames, ","));
                    if (deptLevels == 0) {
                        String dps = "";
                        for (int i = j; i >= 0; i--) {
                            if (dps.equals("")) {
                                dps = deptNames[i];
                            } else {
                                dps += "\\" + deptNames[i];
                            }
                        }
                        if ("".equals(depts_)) {
                            depts_ = dps;
                        } else {
                            depts_ += "," + dps;
                        }

                    }
                }
                a = new Label(col++, row, depts_);
                ws.addCell(a);

                // 用户角色
                UserDb ud = new UserDb(userName);
                RoleDb[] roleary = ud.getRoles();
                String roles = "";
                if (roleary != null) {
                    for (int i = 0; i < roleary.length; i++) {
                        if (roleary[i].getCode().equals(RoleDb.CODE_MEMBER)) {
                            continue;
                        }
                        roles += (i == 0 ? "" : "，") + roleary[i].getDesc();
                    }
                }
                a = new Label(col++, row, roles);
                ws.addCell(a);

                PostUser postUser = postUserService.getPostUserByUserName(userName);
                if (postUser != null) {
                    Post post = postService.getById(postUser.getPostId());
                    if (post != null) {
                        postName = post.getName();
                    }
                }
                a = new Label(col++, row, postName);
                ws.addCell(a);

                a = new Label(col++, row, rr.getString("idCard"));
                ws.addCell(a);

                married = rr.getInt("ISMARRIAGED") == 1 ? "已婚" : "未婚";
                a = new Label(col++, row, married);
                ws.addCell(a);

                a = new Label(col++, row, rr.getString("d"));
                ws.addCell(a);

                a = new Label(col++, row, StrUtil.getNullStr(rr.getString("email")));
                ws.addCell(a);

                // 帐号 姓名 工号	手机	性别	员工编号	部门 角色岗位	身份证	婚否	 出生日期 	E-mail	QQ	电话	入职日期	短号	兴趣爱好	地址
                a = new Label(col++, row, StrUtil.getNullStr(rr.getString("qq")));
                ws.addCell(a);
                a = new Label(col++, row, StrUtil.getNullStr(rr.getString("phone")));
                ws.addCell(a);
                a = new Label(col++, row, DateUtil.format(rr.getDate("entryDate"), "yyyy-MM-dd"));
                ws.addCell(a);
                a = new Label(col++, row, StrUtil.getNullStr(rr.getString("MSN")));
                ws.addCell(a);
                a = new Label(col++, row, StrUtil.getNullStr(rr.getString("hobbies")));
                ws.addCell(a);
                a = new Label(col++, row, StrUtil.getNullStr(rr.getString("address")));
                ws.addCell(a);

                row++;
            }

            wwb.write();
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            try {
                if (wwb != null) {
                    try {
                        wwb.close();
                    } catch (WriteException e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            os.close();
        }
    }

    /**
     * 用于导入后同步用户的单位
     *
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/admin/organize/syncUnit", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> syncUnit() {
        deptUserService.syncUnit();
        return new Result<>(true);
    }

    @ResponseBody
    @RequestMapping(value = "/user/changeSkin", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> changeSkin(String skinCode, HttpServletResponse response) {
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setSkinCode(skinCode);
        boolean re = userSetupService.updateByUserName(userSetup);
        if (re) {
            UserSet.setSkin(request, response, skinCode);
        }
        return new Result<>(re);
    }

    @RequestMapping(value = "/session/invalid")
    public String sessionValid(Model model) {
        return "logout";
    }

    /**
     * 较验手机号
     * 0:校验通过 1：不通过
     */
    @ApiOperation(value = "较验手机号", notes = "较验手机号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "名称", dataType = "String"),
            @ApiImplicitParam(name = "mobile", value = "手机号码", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/checkMobileRepeat", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> checkMobileRepeat(String mobile, String name) {
        int re = 0;
        if (cn.hutool.core.util.StrUtil.isEmpty(name)) {
            if (cn.hutool.core.util.StrUtil.isNotEmpty(mobile)) {
                QueryWrapper<User> qw = new QueryWrapper<>();
                qw.eq("mobile", mobile);
                List<User> userList = userService.list(qw);
                if (userList.size()>0) {
                    re = 1;
                }
            }
        } else {
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.eq("mobile", mobile);
            List<User> userList = userService.list(qw);
            if (userList.size()>0 ) {
                if(!userList.get(0).getName().equals(name)){
                    re = 1;
                }
            }
        }
        return new Result<>(re);
    }

    /**
     * 较验人员编号
     * 0:校验通过 1：不通过
     */
    @ApiOperation(value = "较验人员编号", notes = "较验人员编号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "名称", dataType = "String"),
            @ApiImplicitParam(name = "personNo", value = "人员编码", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/checkPersonNoRepeat", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> checkPersonNoRepeat(String personNo, String name) {
        int re = 0;
        if (cn.hutool.core.util.StrUtil.isEmpty(name)) {
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.eq("person_no", personNo);
            List<User> userList = userService.list(qw);
            if (userList.size()>0) {
                re = 1;
            }
        } else {
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.eq("person_no", personNo);
            List<User> userList = userService.list(qw);
            if (userList.size()>0 ) {
                if(!userList.get(0).getName().equals(name)){
                    re = 1;
                }
            }
        }
        return new Result<>(re);
    }

    /**
     * 启用停用
     */
    @ApiOperation(value = "启用停用", notes = "启用停用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "isValid", value = "是否允许", dataType = "String"),
            @ApiImplicitParam(name = "name", value = "名称", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/stopStartIsValid", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> stopStartIsValid(String name, String isValid) {
        User user = userCache.getUser(name);
        user.setIsValid(Integer.parseInt(isValid));
        userService.updateByUserName(user);
        return new Result<>();
    }

    @ApiOperation(value = "取得经Aes加密的密码", notes = "启用停用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", dataType = "String"),
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/getMmaRaw", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> getMmRaw(String userName) {
        User user = userCache.getUser(userName);
        String pwdRaw = user.getPwdRaw();
        return new Result<>(userService.aesEncryptPwdRaw(pwdRaw));
    }
}
