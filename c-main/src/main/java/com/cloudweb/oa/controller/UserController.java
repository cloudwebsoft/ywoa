package com.cloudweb.oa.controller;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.web.Global;
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
import com.cloudweb.oa.vo.UserVO;
import com.cloudwebsoft.framework.db.JdbcTemplate;
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
import com.redmoon.oa.post.PostDb;
import com.redmoon.oa.post.PostUserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.sso.SyncUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.weixin.mgr.WeixinDo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
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

    @RequestMapping(value = "/userMultiSel")
    public String userMultiSel(Model model) {
        String skinPath = SkinMgr.getSkinPath(request, false);
        model.addAttribute("skinPath", skinPath);

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

        model.addAttribute("mode", mode);
        model.addAttribute("isIncludeChildren", isIncludeChildren);
        model.addAttribute("isForm", isForm);
        model.addAttribute("parameterNum", parameterNum);
        model.addAttribute("from", from);
        model.addAttribute("windowSize", windowSize);

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
        model.addAttribute("recentSelectedList", recentSelectedList);

        List<Role> roleList = roleService.getAll();
        model.addAttribute("roleList", roleList);

        QueryWrapper<Group> qw = new QueryWrapper<>();
        qw.orderByDesc("isSystem").orderByAsc("code");
        List<Group> groupList = userGroupService.list(qw);
        model.addAttribute("groupList", groupList);

        boolean isOpenAll = true; // 展开所有节点
        String treeJsonData = departmentService.getJsonString(isOpenAll, false);
        model.addAttribute("treeJsonData", treeJsonData);

        model.addAttribute("unitList", departmentService.getAllUnit());

        boolean canSaveUserGroupShow = false;
        if (!privilege.isUserPrivValid(request, "admin.user")) {
            canSaveUserGroupShow = true;
        }
        model.addAttribute("canSaveUserGroupShow", canSaveUserGroupShow);

        return "th/user_multi_sel";
    }

    @ApiOperation(value = "为userMultiSel初始化用户", notes = "根据传入的用户，初始化用户选择框")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userNames", value = "用户名，如果有多个的话，以逗号分隔", required = true, dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/user/initUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String initUsers(String userNames) {
        String result = "<select name='websites' id='websites' style='width:300px;' size='24' multiple='true' ondblclick='hasSelected();'>";
        String[] userNameArr = StrUtil.split(userNames, ",");
        // 取出用户信息，拼select
        int i = 0;
        for (String userName : userNameArr) {
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.eq("name", userName);
            User user = userService.getOne(qw, false);
            if (i == 0) {
                if (!user.getGender()) {
                    result += "<option value='" + user.getName() + "' title='" + request.getContextPath() + "/images/man.png' selected='selected'>" + user.getRealName() + "</option>";
                } else {
                    result += "<option value='" + user.getName() + "' title='" + request.getContextPath() + "/images/woman.png' selected='selected'>" + user.getRealName() + "</option>";
                }
            } else {
                if (!user.getGender()) {
                    result += "<option value='" + user.getName() + "' title='" + request.getContextPath() + "/images/man.png' >" + user.getRealName() + "</option>";
                } else {
                    result += "<option value='" + user.getName() + "' title='" + request.getContextPath() + "/images/woman.png' >" + user.getRealName() + "</option>";
                }
            }
            i++;
        }
        result += "</select>";
        JSONObject json = new JSONObject();
        json.put("ret", "1");
        json.put("result", result);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/getDeptUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String getDeptUsers(String deptCode, boolean isIncludeChildren, String limitDepts) {
        JSONObject json = new JSONObject();
        StringBuffer result = new StringBuffer();

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

        if (list == null) {
            result.append("<select name='webmenu' id='webmenu' style='width:298px;' size='11' multiple='true' ondblclick='notSelected(this);'></select>");
            json.put("ret", "1");
            json.put("result", result);
            return json.toString();
        }

        long total = 0;
        int maxCount = 200;

        String deptName = "";

        if (deptCode != null && deptCode.indexOf(",") == -1) {
            deptName = departmentService.getDepartment(deptCode).getName();
        }

        result.append("<select name='webmenu' id='webmenu' style='width:298px;' size='11' multiple='true' ondblclick='notSelected(this);'>");
        result.append(getOptions(list, deptName, !includeRootDept));
        result.append("</select>");
        String tip = "";
        if (total > maxCount) {
            tip = "人数超出" + maxCount + "，未全部显示";
        }
        json.put("ret", "1");
        json.put("tip", tip);
        json.put("result", result);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/searchUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String searchUsers(String userName, String limitDepts) {
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

        result = "<select name='webmenu5' id='webmenu5' style='width:300px;' size='24' multiple='true' ondblclick='notSelected(this);'>";
        result += getOptions(list, "", false);
        result += "</select>";

        JSONObject json = new JSONObject();
        json.put("ret", "1");
        json.put("result", result);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/getRoleUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String getRoleUsers(String roleCode) {
        String result = "";
        Privilege privilege = new Privilege();
        String unitCode = privilege.getUserUnitCode(request);

        List<User> list = userService.getUserOfRole(roleCode, unitCode);
        result = "<select name='webmenu3' id='webmenu3' style='width:298px;' size='11' multiple='true' ondblclick='notSelected(this);'>";
        result += getOptions(list, "", false);
        result += "</select>";
        JSONObject json = new JSONObject();
        json.put("ret", "1");
        json.put("result", result);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/getGroupUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String getGroupUsers(String groupCode) {
        String result = "";
        Privilege privilege = new Privilege();
        String unitCode = privilege.getUserUnitCode(request);

        List<User> list = userService.getGroupUsers(groupCode, unitCode);
        result = "<select name='webmenu4' id='webmenu4' style='width:298px;' size='11' multiple='true' ondblclick='notSelected(this);'>";
        result += getOptions(list, "", false);
        result += "</select>";
        JSONObject json = new JSONObject();
        json.put("ret", "1");
        json.put("result", result);
        return json.toString();
    }

    /**
     * 取得options
     *
     * @param list
     * @param deptName
     * @param isSingleDept 是否仅显示deptName中的用户
     * @return
     */
    public String getOptions(List<User> list, String deptName, boolean isSingleDept) {
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for (User user : list) {
            String spaceSize = "";

            if (!isSingleDept) {
                StringBuffer sbDept = new StringBuffer();
                List<Department> deptList = departmentService.getDeptsOfUser(user.getName());
                for (Department dept : deptList) {
                    StrUtil.concat(sbDept, ",", dept.getName());
                }
                deptName = sbDept.toString();
            }

            String userRealName = user.getRealName();

            if (userRealName.length() >= 24) {
                userRealName = userRealName.substring(0, 10) + "...";
            }
            for (int i = 0; i < 24 - user.getRealName().length(); i++) {
                spaceSize += "&nbsp;";
            }
            userRealName = userRealName + spaceSize + deptName;
            if (isFirst) {
                if (!user.getGender()) {
                    sb.append("<option value='" + user.getName() + "' title='" + request.getContextPath() + "/images/man.png' myattr='" + user.getRealName() + "' selected='selected'>" + userRealName + "</option>");
                } else {
                    sb.append("<option value='" + user.getName() + "' title='" + request.getContextPath() + "/images/woman.png' myattr='" + user.getRealName() + "' selected='selected'>" + userRealName + "</option>");
                }
            } else {
                if (!user.getGender()) {
                    sb.append("<option value='" + user.getName() + "' title='" + request.getContextPath() + "/images/man.png' myattr='" + user.getRealName() + "'>" + userRealName + "</option>");
                } else {
                    sb.append("<option value='" + user.getName() + "' title='" + request.getContextPath() + "/images/woman.png' myattr='" + user.getRealName() + "'>" + userRealName + "</option>");
                }
            }
            isFirst = false;
        }
        return sb.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/leaveOffBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String leaveOffBatch(@NotEmpty(message = "{user.select.none}") String userNames) throws ValidateException {
        boolean re = false;

        String[] ary = StrUtil.split(userNames, ",");
        if (ary == null) {
            return responseUtil.getFailJson("请选择用户").toString();
        }

        String opUser = SpringUtil.getUserName();
        for (int i = 0; i < ary.length; i++) {
            if (ary[i].equals(ConstUtil.USER_ADMIN)) {
                throw new ValidateException("管理员不能离职！");
            }

            re = userService.leaveOffice(StrUtil.toInt(ary[i], -1), opUser);
        }

        return responseUtil.getResultJson(re).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/checkUserName", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String checkUserName(@NotEmpty(message = "{user.name.notempty}") @Length(max = 20, message = "{user.name.length}") String userName) {
        try {
            chkUserName(userName);
        } catch (ValidateException e) {
            JSONObject json = responseUtil.getResultJson(false);
            json.put("msg", e.getMessage());
            return json.toString();
        }

        JSONObject json = responseUtil.getResultJson(true, "");
        return json.toString();
    }

    public void chkUserName(String userName) throws ValidateException {
        String[] chars = {";", "'", ",", "\""};
        int chlen = chars.length;
        for (int k = 0; k < chlen; k++) {
            if (userName.indexOf(chars[k]) != -1) {
                throw new ValidateException("#user.name.invalid");
            }
        }
        User user = userService.getUser(userName);
        if (user != null) {
            throw new ValidateException("#user.name.exist");
        }
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/checkMobile", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String checkMobile(String mobile) {
        if (mobile == null || "".equals(mobile)) {
            return responseUtil.getResultJson(true).toString();
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("mobile", mobile);
        User user = userService.getOne(qw, false);

        if (user != null) {
            JSONObject json = responseUtil.getResultJson(false);
            json.put("msg", validMessageSource.getMessage("user.mobile.exist", null, null));
            return json.toString();
        }
        return responseUtil.getResultJson(true).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/checkAccount", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String checkAccount(String account) {
        if (account == null || "".equals(account)) {
            return responseUtil.getResultJson(true).toString();
        }

        QueryWrapper<Account> qw = new QueryWrapper<>();
        qw.eq("name", account);
        Account acc = accountService.getOne(qw, false);
        if (acc != null) {
            JSONObject json = responseUtil.getResultJson(false);
            json.put("msg", validMessageSource.getMessage("user.account.used", null, null));
            return json.toString();
        }

        return responseUtil.getResultJson(true).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/checkPwd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String checkPwd(String pwd) {
        if (pwd == null || "".equals(pwd)) {
            return responseUtil.getResultJson(true, "").toString();
        }

        boolean re = false;
        String msg = "";
        try {
            re = chkPwd(pwd);
        } catch (ErrMsgException e) {
            msg = e.getMessage();
        }

        return responseUtil.getResultJson(re, msg).toString();
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

    @ResponseBody
    @RequestMapping(value = "/user/checkPersonNo", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String checkPersonNo(String personNo) {
        if (userService.isPersonNoExist(personNo)) {
            return responseUtil.getResultJson(true, i18nUtil.getValid("user.personNo.exist")).toString();
        } else {
            return responseUtil.getResultJson(true, "").toString();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/user/create", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String create(UserVO userVO, @RequestParam(value = "photo", required = false) MultipartFile file) throws ErrMsgException, IOException, ValidateException {
        chkUserName(userVO.getName());

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
                return responseUtil.getResultJson(false, validMessageSource.getMessage("user.account.used", null, null)).toString();
            }
        }

        return responseUtil.getResultJson(userService.create(userVO)).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/update", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String update(UserVO userVO, @RequestParam(value = "photo", required = false) MultipartFile file) throws ErrMsgException, IOException, ValidateException {
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

        return responseUtil.getResultJson(userService.update(userVO)).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/setPrivs", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String setPrivs(String userName, String[] priv) throws ErrMsgException, IOException, ValidateException {
        // 许可证验证
        License.getInstance().validate(request);

        boolean re = userPrivService.setPrivs(userName, priv);
        return responseUtil.getResultJson(re).toString();
    }

    @ApiOperation(value = "选择用户", notes = "可选择部门、角色中的用户，以及最近使用的用户", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleCodes", value = "角色编码，如果有多个的话，以逗号分隔", required = true, dataType = "String"),
            @ApiImplicitParam(name = "unitCode", value = "单位编码", required = true, dataType = "String")
    })
    @RequestMapping(value = "/roleMultilSel")
    public String roleMultilSel(@RequestParam(required = false) String roleCodes, @RequestParam(required = false) String unitCode, Model model) {
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
    public String setRoleOfUser(@RequestParam(required = true) String userName, String roleCodes) throws ErrMsgException, IOException, ValidateException {
        // 许可证验证
        License.getInstance().validate(request);

        String[] roles = StrUtil.split(roleCodes, ",");

        return responseUtil.getResultJson(userOfRoleService.setRoleOfUser(userName, roles)).toString();
    }

    @ApiOperation(value = "删除用户", notes = "删除用户", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "用户ID，如果有多个的话，以逗号分隔", required = true, dataType = "String")
    })
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @PostMapping(value = "/user/delUsers", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String delUsers(@RequestParam(value = "ids", required = true) String ids) throws ResKeyException, ErrMsgException {
        String[] ary = StrUtil.split(ids, ",");
        return responseUtil.getResultJson(userService.delUsers(ary)).toString();
    }

    @ApiOperation(value = "用户列表", notes = "用户列表", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchType", value = "realName: 姓名, userName: 帐户, account: 工号, mobile: 手机, Email: 邮件", required = false, dataType = "String"),
            @ApiImplicitParam(name = "isValid", value = "1:在职  0：离职", required = false, dataType = "String")
    })

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/list", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String list(String searchType, @RequestParam(defaultValue = "") String op, String condition, @RequestParam(defaultValue = "1") int isValid,
                       @RequestParam(defaultValue = "regDate") String orderBy,
                       @RequestParam(defaultValue = "desc") String sort, String deptCode,
                       @RequestParam(value = "rp", defaultValue = "20") int pageSize, @RequestParam(value = "page", defaultValue = "1") int curPage) {
        Privilege privilege = new Privilege();
        JSONObject jobject = new JSONObject();
        String unitCode = privilege.getUserUnitCode();

        if (searchType == null || "".equals(searchType)) {
            searchType = "realName";
        }

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        boolean is_bind_mobile = cfg.getBooleanProperty("is_bind_mobile");
        boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");

        deptCode = StrUtil.getNullStr(deptCode);

        String sql = "";
        if (ConstUtil.DEPT_ROOT.equals(deptCode)) {
            sql = "select name from users where name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM) + " and name<>" + StrUtil.sqlstr(ConstUtil.USER_ADMIN);
            if (op.equals("search")) {
                if ("".equals(condition)) {
                    sql += " and isValid = " + isValid;
                } else {
                    sql += " and isValid = " + isValid;

                    if ("realName".equals(searchType)) {
                        sql += " and realName like " + StrUtil.sqlstr("%" + condition + "%");
                    } else if ("userName".equals(searchType)) {
                        sql += " and name like " + StrUtil.sqlstr("%" + condition + "%");
                    } else if ("mobile".equals(searchType)) {
                        sql += " and mobile like " + StrUtil.sqlstr("%" + condition + "%");
                    } else if ("account".equals(searchType)) {
                        sql = "select DISTINCT u.name from users u, account a where u.name=a.username and u.name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM);
                        sql += " and isValid = " + isValid + " and a.name like " + StrUtil.sqlstr("%" + condition + "%");
                    } else if ("email".equals(searchType)) {
                        sql += " and email like " + StrUtil.sqlstr("%" + condition + "%");
                    }
                }
            } else {
                sql += " and isValid=" + isValid;
            }
            if (!unitCode.equals(ConstUtil.DEPT_ROOT)) {
                sql += " and unit_code=" + StrUtil.sqlstr(unitCode);
            }
            sql += " order by orders desc," + orderBy + " " + sort;
        } else {
            if (op.equals("search")) {
                sql = "select DISTINCT u.name from users u,department d,dept_user du where u.name = du.user_name and d.code = du.dept_code and u.name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM);
                if ("".equals(condition)) {
                    sql += " and u.isValid = " + isValid;
                } else {
                    sql += " and u.isValid = " + isValid;
                    if ("realName".equals(searchType)) {
                        sql += " and realName like " + StrUtil.sqlstr("%" + condition + "%");
                    } else if ("userName".equals(searchType)) {
                        sql += " and name like " + StrUtil.sqlstr("%" + condition + "%");
                    } else if ("mobile".equals(searchType)) {
                        sql += " and mobile like " + StrUtil.sqlstr("%" + condition + "%");
                    } else if ("account".equals(searchType)) {
                        sql = "select DISTINCT u.name from users u, account a where u.name=a.username and u.name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM);
                        sql += " and isValid = " + isValid + " and a.name like " + StrUtil.sqlstr("%" + condition + "%");
                    } else if ("email".equals(searchType)) {
                        sql += " and email like " + StrUtil.sqlstr("%" + condition + "%");
                    }
                }
            } else {
                sql = "select DISTINCT u.name from users u,department d,dept_user du  where u.name = du.user_name and d.code = du.dept_code and isValid = 1 and u.name<>" + StrUtil.sqlstr(ConstUtil.USER_SYSTEM);
            }
            String cond = "";
            if (!ConstUtil.DEPT_ROOT.equals(deptCode) && !"".equals(deptCode)) {
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
            }
            if (!unitCode.equals(ConstUtil.DEPT_ROOT)) {
                sql += " and u.unit_code=" + unitCode;
            }
            if (showByDeptSort) {
                orderBy = "du.orders";
                sort = "desc";
                sql += cond + " order by " + orderBy + " " + sort;
            } else {
                sql += cond + " order by u.orders desc," + orderBy + " " + sort;
            }
        }

        // System.out.println(getClass() + " " + sql);

        PageHelper.startPage(curPage, pageSize); // 分页查询

        JSONArray rows = new JSONArray();

        List<String> list = userService.listNameBySql(sql);
        PageInfo<String> pageInfo = new PageInfo<>(list);

        jobject.put("rows", rows);
        jobject.put("page", curPage);
        jobject.put("total", pageInfo.getTotal());

        for (String userName : list) {
            User user = userService.getUser(userName);
            Account acc = accountService.getAccountByUserName(user.getName());
            UserSetup userSetup = userSetupService.getUserSetup(user.getName());
            JSONObject jo = new JSONObject();

            jo.put("id", String.valueOf(user.getId()));

            if (showByDeptSort && !"".equals(deptCode)) {
                DeptUser du = deptUserService.getDeptUser(user.getName(), deptCode);
                if (du != null) {
                    jo.put("deptOrder", String.valueOf(du.getOrders()));
                } else {
                    jo.put("deptOrder", "");
                }
            }
            jo.put("name", "<a href='javascript:;' onclick=\"addTab('" + user.getRealName() + "', '" + request.getContextPath() + "/admin/organize/editUser.do?userName=" + StrUtil.UrlEncode(user.getName()) + "')\">" + user.getName() + "</a>");
            jo.put("realName", user.getRealName());
            if (acc != null) {
                jo.put("account", acc.getName());
            } else {
                jo.put("account", "");
            }
            jo.put("sex", user.getGender() != null ? (!user.getGender() ? "男" : "女") : "");
            jo.put("mobile", user.getMobile());

            StringBuffer deptNames = new StringBuffer();
            List<DeptUser> duList = deptUserService.listByUserName(user.getName());
            for (DeptUser du : duList) {
                Department dept = departmentService.getDepartment(du.getDeptCode());
                String deptName = "不存在";
                if (dept!=null) {
                    if (!ConstUtil.DEPT_ROOT.equals(dept.getParentCode()) && !ConstUtil.DEPT_ROOT.equals(dept.getCode())) {
                        Department parentDept = departmentService.getDepartment(dept.getParentCode());
                        if (parentDept!=null) {
                            deptName = parentDept.getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dept.getName();
                        }
                    } else {
                        deptName = dept.getName();
                    }
                }

                StrUtil.concat(deptNames, "，", deptName);
            }
            jo.put("deptNames", deptNames.toString());

            List<UserOfRole> uorList = userOfRoleService.listByUserName(user.getName());
            String roleDescs = "";
            for (UserOfRole uor : uorList) {
                Role role = roleService.getRole(uor.getRoleCode());
                if (role == null) {
                    continue;
                }
                if (role.getCode().equals(ConstUtil.ROLE_MEMBER)) {
                    continue;
                }

                if (roleDescs.equals("")) {
                    roleDescs = "<a href=\"javascript:;\" onclick=\"addTab('" + role.getDescription() + "', '" + request.getContextPath() + "/admin/user_role_priv.jsp?roleCode=" + role.getCode() + "')\">" + StrUtil.getNullStr(role.getDescription()) + "</a>";
                } else {
                    roleDescs += "，" + "<a href=\"javascript:;\" onclick=\"addTab('" + role.getDescription() + "', '" + request.getContextPath() + "/admin/user_role_priv.jsp?roleCode=" + role.getCode() + "')\">" + StrUtil.getNullStr(role.getDescription()) + "</a>";
                }
            }
            jo.put("roleName", roleDescs);

            if (user.getIsValid() == 1) {
                //out.print("已启用");
                jo.put("status", "<img title='已启用' width=16 src='" + request.getContextPath() + "/skin/images/organize/icon-finish.png'/>");
            } else {
                //out.print("未启用");
                jo.put("status", "<img title='未启用' width=16 src='" + request.getContextPath() + "/skin/images/organize/stop.png'/>");
            }

            if (is_bind_mobile) {
                if (userSetup.getIsBindMobile() == 1) {
                    jo.put("isBindMobile", "已绑定");
                } else {
                    jo.put("isBindMobile", "未绑定");
                }
            }
            jo.put("op", "<a href=\"javascript:;\" onclick=\"addTab('" + user.getRealName() + "', '" + request.getContextPath() + "/admin/organize/editUser.do?userName=" + StrUtil.UrlEncode(user.getName()) + "')\">编辑</a>");
            rows.add(jo);
        }
        return jobject.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/changeOrder", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String changeOrder(@RequestParam(value = "id", required = true) int id, String colName, String original_value, String update_value, String deptCode) {
        if (update_value.equals(original_value)) {
            return responseUtil.getResultJson(false, "值未更改").toString();
        }
        boolean re = false;

        User user = userService.getById(id);
        DeptUser deptUser = deptUserService.getDeptUser(user.getName(), deptCode);
        if (deptUser != null) {
            deptUser.setOrders(StrUtil.toInt(update_value, 0));
            re = deptUser.updateById();
        }

        return responseUtil.getResultJson(re).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/enableBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String enableBatch(@NotEmpty(message = "{user.id.empty}") @RequestParam(value = "ids", required = true) String ids) {
        boolean re = true;
        String[] ary = StrUtil.split(ids, ",");
        for (String id : ary) {
            re = userService.reEmploryment(StrUtil.toInt(id), SpringUtil.getUserName());
        }
        return responseUtil.getResultJson(re).toString();
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
    public String changeDepts(@NotEmpty(message = "{user.id.empty}") @RequestParam(value = "ids", required = true) String ids, String deptCodes) throws ErrMsgException {
        boolean re = false;
        String[] ary = StrUtil.split(ids, ",");
        String curUserName = SpringUtil.getUserName();
        for (int i = 0; i < ary.length; i++) {
            User user = userService.getById(StrUtil.toInt(ary[i]));
            re = deptUserService.changeDeptOfUser(user.getName(), deptCodes, curUserName);
        }
        return responseUtil.getResultJson(re).toString();
    }

    /**
     * 调入
     *
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/user/transferUsers", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String transferUsers(String userNames, String deptCode) throws ErrMsgException, ValidateException {
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

        return responseUtil.getResultJson(re).toString();
    }

    @ApiOperation(value = "完善用户的邮件信息", notes = "完善用户的邮件信息", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "邮箱", required = true, dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/user/setUserInfo", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String setUserInfo(@RequestParam(value = "email", required = true) String email) {
        boolean re = true;
        String userName = SpringUtil.getUserName();
        User user = userService.getUser(userName);
        user.setEmail(email);
        re = user.updateById();
        return responseUtil.getResultJson(re).toString();
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
    public String bindBatch(@NotEmpty(message = "{user.id.empty}") @RequestParam(value = "ids", required = true) String ids) {
        boolean re = true;
        String[] ary = StrUtil.split(ids, ",");
        for (int i = 0; i < ary.length; i++) {
            User user = userService.getById(StrUtil.toInt(ary[i]));
            UserSetup userSetup = userSetupService.getUserSetup(user.getName());
            userSetup.setIsBindMobile(1);
            re = userSetupService.updateByUserName(userSetup);
        }

        return responseUtil.getResultJson(re).toString();
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
    public String unbindBatch(@NotEmpty(message = "{user.id.empty}") @RequestParam(value = "ids", required = true) String ids) {
        boolean re = true;
        String[] ary = StrUtil.split(ids, ",");
        for (int i = 0; i < ary.length; i++) {
            User user = userService.getById(StrUtil.toInt(ary[i]));
            UserSetup userSetup = userSetupService.getUserSetup(user.getName());
            userSetup.setIsBindMobile(1);
            re = userSetupService.updateByUserName(userSetup);
            userMobileService.delByUserName(user.getName());
        }
        return responseUtil.getResultJson(re).toString();
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
    public String synAll(String op) {
        String userName = SpringUtil.getUserName();
        if (op.equals("syncWeixin")) { // 一键同步OA部门人员至微信端
            WeixinDo weixinDo = new WeixinDo();
            weixinDo.syncDeptUsers();
        } else if (op.equals("syncWeixinToOA")) {
            WeixinDo weixinDo = new WeixinDo();
            weixinDo.syncWxDeptUserToOA();
        } else if (op.equals("syncOAToDingding")) {
            DingDingClient client = new DingDingClient();
            client.syncOAtoDingDing();
        } else if (op.equals("syncDingdingToOA")) {
            DingDingClient client = new DingDingClient();
            client.syncDingDingToOA();
        } else if (op.equals("syncDing")) {
            //同步钉钉账户所有人员信息至user表中dingding字段
            DingDingClient.batchUserAddDingDing();
        } else if (op.equals("sync")) {
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

        return responseUtil.getResultJson(true, "同步完成").toString();
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

        if (deptCodes.equals("")) {
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
    public String setUserAdminDept(@NotEmpty(message = "{user.name.notempty}") String userName, String deptCodes) {
        return responseUtil.getResultJson(userAdminDeptService.setUserAdminDept(userName, deptCodes)).toString();
    }

    @RequestMapping(value = "/user/controlPanel")
    public String controlPanel(Model model) {
        String skinPath = SkinMgr.getSkinPath(request, false);
        model.addAttribute("skinPath", skinPath);

        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        boolean isNetdiskUsed = cfg.getBooleanProperty("isNetdiskUsed");
        boolean isLarkUsed = cfg.getBooleanProperty("isLarkUsed");
        boolean isForumOpen = cfg.getBooleanProperty("isForumOpen");
        boolean isIntegrateEmail = cfg.getBooleanProperty("isIntegrateEmail");
        model.addAttribute("isNetdiskUsed", isNetdiskUsed);
        model.addAttribute("isLarkUsed", isLarkUsed);
        model.addAttribute("isForumOpen", isForumOpen);
        model.addAttribute("isIntegrateEmail", isIntegrateEmail);

        String oldSkinCode = UserSet.getSkin(request);
        model.addAttribute("oldSkinCode", oldSkinCode);

        StringBuilder skinOpts = new StringBuilder();
        com.redmoon.oa.ui.SkinMgr sm = new com.redmoon.oa.ui.SkinMgr();
        Iterator irskin = sm.getAllSkin().iterator();
        String defaultSkinCode = "";
        while (irskin.hasNext()) {
            com.redmoon.oa.ui.Skin sk = (com.redmoon.oa.ui.Skin) irskin.next();
            String d = "";
            if (sk.isDefaultSkin()) {
                d = "selected";
                defaultSkinCode = sk.getCode();
            }
            skinOpts.append("<option value=" + sk.getCode() + " " + d + ">" + sk.getName() + "</option>");
        }
        model.addAttribute("skinOpts", skinOpts.toString());

        String userName = SpringUtil.getUserName();
        User user = userService.getUser(userName);
        UserSetup userSetup = userSetupService.getUserSetup(userName);
        model.addAttribute("user", user);
        model.addAttribute("userSetup", userSetup);

        boolean isSpecified = "specified".equals(cfg.get("styleMode")) || "2".equals(cfg.get("styleMode"));
        int uiMode = 0;
        if (isSpecified) {
            uiMode = StrUtil.toInt(cfg.get("styleSpecified"), -1);
        } else {
            uiMode = userSetup.getUiMode();
        }
        model.addAttribute("isSpecified", isSpecified);
        model.addAttribute("uiMode", uiMode);
        model.addAttribute("menuMode", userSetup.getMenuMode());

        String skinCode = userSetup.getSkinCode();
        if ("".equals(skinCode)) {
            skinCode = defaultSkinCode;
        }
        model.addAttribute("skinCode", skinCode);

        boolean isDisplaySlideMenu = true;
        if (uiMode == ConstUtil.UI_MODE_LTE || uiMode == ConstUtil.UI_MODE_PROFESSION || uiMode == ConstUtil.UI_MODE_NONE) {
            isDisplaySlideMenu = false;
        }
        model.addAttribute("isDisplaySlideMenu", isDisplaySlideMenu);

        String diskSpaceUsed = UtilTools.getFileSize(user.getDiskSpaceUsed());
        String diskSpaceAllowed = UtilTools.getFileSize(user.getDiskSpaceAllowed());
        model.addAttribute("diskSpaceUsed", diskSpaceUsed);
        model.addAttribute("diskSpaceAllowed", diskSpaceAllowed);
        String msgSpaceAllowed = UtilTools.getFileSize(userSetup.getMsgSpaceAllowed());
        model.addAttribute("msgSpaceAllowed", msgSpaceAllowed);
        String msgSpaceUsed = UtilTools.getFileSize(userSetup.getMsgSpaceUsed());
        model.addAttribute("msgSpaceUsed", msgSpaceUsed);

        model.addAttribute("UI_MODE_NONE", ConstUtil.UI_MODE_NONE);
        model.addAttribute("UI_MODE_PROFESSION", ConstUtil.UI_MODE_PROFESSION);
        model.addAttribute("UI_MODE_FASHION", ConstUtil.UI_MODE_FASHION);
        model.addAttribute("UI_MODE_FLOWERINESS", ConstUtil.UI_MODE_FLOWERINESS);
        model.addAttribute("UI_MODE_PROFESSION_NORMAL", ConstUtil.UI_MODE_PROFESSION_NORMAL);
        model.addAttribute("UI_MODE_LTE", ConstUtil.UI_MODE_LTE);

        model.addAttribute("MENU_MODE_NEW", ConstUtil.MENU_MODE_NEW);
        model.addAttribute("MENU_MODE_NORMAL", ConstUtil.MENU_MODE_NORMAL);

        return "th/user/control_panel";
    }

    /**
     * 更改密码
     *
     * @param userName
     * @param pwd
     * @param pwd2
     * @param pwd3
     * @return
     * @throws ValidateException
     */
    @ResponseBody
    @RequestMapping(value = "/user/changePwd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String changePwd(String userName, @RequestParam(required = true) String pwd, @RequestParam(required = true) String pwd2, @RequestParam(required = true) String pwd3) throws ValidateException {
        User user = userService.getUser(userName);
        String pwdMd5Old = user.getPwd();

        String pwdMD5 = "";
        try {
            pwdMD5 = SecurityUtil.MD5(pwd3);
        } catch (Exception e) {
            e.printStackTrace();
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

        return responseUtil.getResultJson(userService.modifyPwd(userName, pwd)).toString();
    }

    /**
     * 语方设置
     *
     * @param local
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/changeLang", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String changeLang(@RequestParam(required = true) String local) {

        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setLocal(local);

        return responseUtil.getResultJson(userSetupService.updateByUserName(userSetup)).toString();
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
    public String setEmail(String emailName, String emailPwd) {

        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setEmailName(emailName);
        userSetup.setEmailPwd(emailPwd);

        return responseUtil.getResultJson(userSetupService.updateByUserName(userSetup)).toString();
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
    public String setIndividuality(Integer isMsgWinPopup, Integer isMessageSoundPlay, Integer isChatIconShow, Integer isChatSoundPlay, Integer isWebedit, Integer isShowSidebar) {

        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setIsMsgWinPopup(isMsgWinPopup == 1);
        userSetup.setIsMessageSoundPlay(isMessageSoundPlay);
        userSetup.setIsChatIconShow(isChatIconShow == 1);
        userSetup.setIsChatSoundPlay(isChatSoundPlay == 1);
        userSetup.setIsWebedit(isWebedit);
        userSetup.setIsShowSidebar(isShowSidebar);

        return responseUtil.getResultJson(userSetupService.updateByUserName(userSetup)).toString();
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
    public JSONObject setStyleMode(HttpServletResponse response, String skinCode, Integer uiMode, Integer menuMode) {

        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setSkinCode(skinCode);
        userSetup.setUiMode(uiMode);
        userSetup.setMenuMode(menuMode);

        JSONObject json = responseUtil.getResultJson(userSetupService.updateByUserName(userSetup));

        if (uiMode== UserSetupDb.UI_MODE_LTE) {
            UserSet.setSkin(request, response, SkinMgr.SKIN_CODE_LTE);
        }
        else {
            UserSet.setSkin(request, response, skinCode);
        }

        String page = loginService.getUIModePage("");
        json.put("page", page);

        return json;
    }

    @RequestMapping(value = "/user/editUser")
    public String editUser(Model model) {
        String userName = SpringUtil.getUserName();
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        User user = userService.getUser(userName);
        model.addAttribute("user", user);

        String selectDeptCode = ParamUtil.get(request, "selectDeptCode");
        model.addAttribute("selectDeptCode", selectDeptCode);

        StringBuffer sbDeptName = new StringBuffer();
        List<Department> list = departmentService.getDeptsOfUser(userName);
        for (Department department : list) {
            StrUtil.concat(sbDeptName, ",", department.getName());
        }
        model.addAttribute("deptName", sbDeptName.toString());

        List<Role> roleList = roleService.getRolesOfUser(user.getName(), true);
        StringBuffer roleDescs = new StringBuffer();
        for (Role role : roleList) {
            StrUtil.concat(roleDescs, "，", role.getDescription());
        }
        model.addAttribute("roleName", roleDescs.toString());

        model.addAttribute("isPlatformSrc", com.redmoon.oa.kernel.License.getInstance().isPlatformSrc());
        model.addAttribute("isGov", License.getInstance().isGov());

        com.redmoon.oa.Config cfg = Config.getInstance();
        boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");
        String accountName = "";
        if (isUseAccount) {
            Account account = accountService.getAccountByUserName(userName);
            if (account != null) {
                accountName = account.getName();
            }
        }
        model.addAttribute("isUseAccount", isUseAccount);
        model.addAttribute("account", accountName);

        boolean isMobileNotEmpty = false;
        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        if (weixinCfg.getBooleanProperty("isUse") || dingdingCfg.isUseDingDing()) {
            isMobileNotEmpty = true;
        }
        model.addAttribute("isMobileNotEmpty", isMobileNotEmpty);

        StringBuffer typeOpts = new StringBuffer();
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("user_type");
        Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        Iterator irType = vType.iterator();
        while (irType.hasNext()) {
            SelectOptionDb sod = (SelectOptionDb) irType.next();
            String selected = "";
            if (sod.isDefault()) {
                selected = "selected";
            }
            String clr = "";
            if (!sod.getColor().equals("")) {
                clr = " style='color:" + sod.getColor() + "' ";
            }
            typeOpts.append("<option value='" + sod.getValue() + "'" + selected + clr + ">" + sod.getName() + "</option>");
        }
        model.addAttribute("userTypeOpts", typeOpts);

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
        model.addAttribute("leaders", leaders);
        model.addAttribute("leaderNames", leaderNames);

        model.addAttribute("isMyLeaderUsed", cfg.getBooleanProperty("isMyLeaderUsed"));

        return "th/user/user_edit";
    }

    /**
     * 更新用户的信息
     * @param userVO
     * @param file
     * @return
     * @throws ErrMsgException
     * @throws IOException
     * @throws ValidateException
     */
    @ResponseBody
    @RequestMapping(value = "/user/updateMyInfo", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String updateMyInfo(UserVO userVO, @RequestParam(value = "photo", required = false) MultipartFile file) throws ErrMsgException, IOException, ValidateException {
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
            userVO.setPwdRaw((userVO.getPassword()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return responseUtil.getResultJson(userService.updateMyInfo(userVO)).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/restoreIcon", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public JSONObject restoreIcon(String userName) throws ValidateException {
        if (!SpringUtil.getUserName().equals(userName)) {
            Privilege pvg = new Privilege();
            if (!pvg.isUserPrivValid(SpringUtil.getUserName(), "admin.user")) {
                throw new ValidateException("权限非法");
            }
        }
        User user = userService.getUser(userName);
        user.setPhoto("");
        return responseUtil.getResultJson(userService.updateByUserName(user));
    }

    /**
     * 进入更改初始密码页面
     * @param model
     * @return
     */
    @RequestMapping(value = "/user/changeInitPwd")
    public String changeInitPwd(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        LoginService loginService = SpringUtil.getBean(LoginService.class);
        String url = loginService.getUIModePage("");
        model.addAttribute("url", url);
        model.addAttribute("isDebug", Global.getInstance().isDebug());

        return "th/user/change_initpwd";
    }

    /**
     * 进入更改初始密码页面
     * @param model
     * @return
     */
    @RequestMapping(value = "/user/changeMyPwd")
    public String changeMyPwd(Model model) {
        String userName = SpringUtil.getUserName();
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        LoginService loginService = SpringUtil.getBean(LoginService.class);
        String url = loginService.getUIModePage("");
        model.addAttribute("userName", userName);
        model.addAttribute("url", url);
        model.addAttribute("isDebug", Global.getInstance().isDebug());

        return "th/user/change_pwd";
    }

    @ResponseBody
    @RequestMapping(value = "/user/updateInitPwd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public JSONObject updateInitPwd(String pwd, String confirmPwd) throws ValidateException {
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

        return responseUtil.getResultJson(userService.modifyPwd(userName, pwd));
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @RequestMapping(value = "/admin/organize/userImport")
    public String userImport(Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        return "th/admin/organize/user_import";
    }

    // 设置错误信息
    public String setPrompt(String prompt,String msg){
        if (prompt.contains(msg)) {
            return prompt;
        }
        if ("".equals(prompt)) {
            prompt = msg;
        } else {
            prompt += ","+msg;
        }
        return prompt;
    }

    @RequestMapping(value = "/admin/organize/userImportConfirm", method = RequestMethod.POST)
    public String userImportConfirm(@RequestParam(value = "att1", required = false) MultipartFile file, Model model) throws ValidateException {
        if (file==null) {
            throw new ValidateException("请上传excel文件");
        }

        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        JSONObject json = new JSONObject();
        if (file.getSize()==0) {
            model.addAttribute("isShowBack", true);
            model.addAttribute("msg", "请上传文件");
            return "th/error/error";
        }

        json.put("ret", 1);

        InputStream in = null;
        try {
            in = file.getInputStream();
        } catch (IOException e) {
            json.put("ret", 0);
            json.put("msg", "读取文件异常");
            e.printStackTrace();
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
                                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
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
                                    cell.setCellType(XSSFCell.CELL_TYPE_STRING);
                                    info[k - 1][i] = StrUtil.getNullStr(cell.getStringCellValue()).trim();
                                }
                            }
                        }
                    }
                }
            }
            else {
                json.put("ret", 0);
                json.put("msg", "文件格式错误");
            }
        } catch (IOException e) {
            json.put("ret", 0);
            json.put("msg", "导入文件出现异常");
            log.error("导入文件出现异常");
        } catch (InvalidFormatException e) {
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

        if (json.getIntValue("ret")==0) {
            model.addAttribute("msg", json.getString("msg"));
            return "th/error/error";
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

        for(int i=0;i<info.length;i++) {
            name = info[i][0];
            realName = info[i][1];
            accountName = info[i][2];

            // System.out.println("name=" + name + " realName=" + realName + " len=" + info.length);
            // excel中可能会出现行为null或空，用格式刷后有可能出现这种情况
            if ((name==null && realName==null) || ("".equals(name) && "".equals(realName))) {
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
            String personNOCol="";
            String deptCol = "";
            String accountNameCol = "";
            //帐号校验
            String sql = "select name from users where name="+StrUtil.sqlstr(name);
            if ("".equals(name)){
                prompt = setPrompt(prompt, "必填项内容为空");
                nameCol = "1";
            } else {
                if (name.length()>20){
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
                if (!accountNameCol.equals("1")) {
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
            if ("".equals(realName)){
                prompt = setPrompt(prompt,"必填项内容为空");
                realNameCol = "1";
            } else {
                if (realName.length()>20){
                    prompt = setPrompt(prompt,"姓名不能大于20个字符");
                    realNameCol = "1";
                }
            }
            //手机校验
            if (!"".equals(mobile)){
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("^\\d{11}$");
                java.util.regex.Matcher m = p.matcher(mobile);
                if (!m.matches()){
                    prompt = setPrompt(prompt,"手机号非法");
                    mobileCol = "1";
                }
                if (!mobileCol.equals("1")) {
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
            if (!"".equals(personNO)){
                if (personNO.length()>20){
                    prompt = setPrompt(prompt,"员工编号不能大于20个字符");
                    personNOCol = "1";
                } else {
                    if (userService.isPersonNoUsedByOther(name, personNO)) {
                        prompt = setPrompt(prompt,"员工编号已存在");
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
            if("".equals(dept)){
                prompt = setPrompt(prompt,"必填项内容为空");
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
            if (!"".equals(prompt)){
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

        model.addAttribute("infoLen", info.length);
        model.addAttribute("successArr", successArr);
        model.addAttribute("failArr", failArr);

        return "th/admin/organize/user_import_confirm";
    }

    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/admin/organize/userImportFinish", produces = {"text/html;charset=UTF-8;", "application/json;"})
    public JSONObject userImportFinish(String info, Model model) {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        String errMsg = "";
        JSONArray arr = JSONArray.parseArray(info);
        boolean re = false;
        try {
            re = userService.importUser(arr);
        } catch (ValidateException e) {
            errMsg = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            errMsg = e.getMessage();
            e.printStackTrace();
        } catch (ErrMsgException e) {
            errMsg = e.getMessage();
            e.printStackTrace();
        }

        JSONObject json = responseUtil.getResultJson(re);
        if (!"".equals(errMsg)) {
            json.put("msg", errMsg);
        }
        return json;
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
            String[] preinfo = {"帐号", "姓名","工号","手机","性别","员工编号"};
            String[] depts = {"部门一级", "部门二级", "部门三级", "部门四级", "部门五级", "部门六级", "部门七级", "部门八级", "部门九级", "部门十级", "部门十一级", "部门十二级"};
            String[] sufinfo = {"角色", "岗位", "身份证", "婚否", "出生日期", "邮箱", "QQ", "电话", "入职日期", "短号", "兴趣爱好", "地址"};

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
            if (deptLevels==0) {
                depts[0] = "部门";
            }
            for (int i = 0; i < (deptLevels==0?1:deptLevels); i++) {
                a = new Label(preinfo.length + i, 0, depts[i]);
                ws.addCell(a);
            }
            // 部门后面的信息
            for (int i = 0; i < sufinfo.length; i++) {
                a = new Label(preinfo.length + (deptLevels==0?1:deptLevels) + i, 0, sufinfo[i]);
                ws.addCell(a);
            }

            String account = "", realName = "", mobile = "", gender = "", personNo = "", idCard="", married = "", email="", QQ="", phone="", entryDate="", shortMobile="", hobbies="", address="";
            String postName = "";
            // 内容
            String sql = "select users.id,name,dept_code,realname,case gender when 0 then '男' when 1 then'女' end g,date_format(birthday,'%Y-%m-%d') d,'' s,mobile,idcard,ISMARRIAGED,person_no,phone,'' fax,address,mobile,email,qq,msn,hobbies,address,entryDate from users,dept_user where name=dept_user.user_name and isvalid=" + isValid + " order by dept_code asc,dept_user.orders asc";
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = null;
            ri = jt.executeQuery(sql);
            int row = 1;
            HashMap<String, java.lang.Boolean> map = new HashMap<String, java.lang.Boolean>();
            while (ri.hasNext()) {
                int col = 0;
                ResultRecord rr = (ResultRecord)ri.next();
                String name = rr.getString(2);
                if (map.containsKey(name)) {
                    continue;
                } else {
                    map.put(name, true);
                }

                a = new Label(col++, row, rr.getString("name"));
                ws.addCell(a);

                a = new Label(col++, row, rr.getString("realname"));
                ws.addCell(a);

                Account acc = accountService.getAccountByUserName(name);
                if (acc!=null) {
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

                //String deptCode = rr.getString(3);
                // 获取各级部门名称
                //增加多部门导出
                String[] deptNames = new String[maxLevel];
                String depts_ = "";
                String sqlDepts = "select * from dept_user where user_name = ?";
                ResultIterator resultIterator = jt.executeQuery(sqlDepts,new Object[]{rr.getString(2)});
                while (resultIterator.hasNext()){
                    ResultRecord resultRecord = (ResultRecord)resultIterator.next();
                    String deptCode = resultRecord.getString("dept_code");
                    DeptDb dd = new DeptDb(deptCode);
                    deptNames[0] = dd.getName();
                    int j = 0;
                    while (!dd.getParentCode().equals("-1")
                            && !dd.getParentCode().equals(DeptDb.ROOTCODE)
                            && j < maxLevel - 1) {
                        dd = new DeptDb(dd.getParentCode());
                        // System.out.println(getClass() + " dd.getParentCode()=" + dd.getParentCode());
                        if (dd != null && !dd.getParentCode().equals("")) {
                            deptNames[++j] = dd.getName();
                        } else {
                            break;
                        }
                    }
                    if (deptLevels==0) {
                        String dps = "";
                        // System.out.println(getClass() + " j=" + j);
                        for (int i = j; i >= 0; i--) {
                            if (dps.equals("")) {
                                dps = deptNames[i];
                            }
                            else {
                                dps += "\\" + deptNames[i];
                            }
                        }
                        if ("".equals(depts_)){
                            depts_ = dps;
                        }else {
                            depts_ += "," + dps;
                        }

                    }
                }
                a = new Label(col++, row, depts_);
                ws.addCell(a);


                // 用户角色
                UserDb ud = new UserDb(name);
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

                PostUserDb pud = new PostUserDb();
                pud = pud.getPostUserDb(name);
                if (pud!=null) {
                    PostDb pd = new PostDb();
                    pd = pd.getPostDb(pud.getInt("post_id"));
                    if (pd!=null) {
                        postName = pd.getString("name");
                    }
                }
                a = new Label(col++, row, postName);
                ws.addCell(a);

                a = new Label(col++, row, rr.getString("idCard"));
                ws.addCell(a);

                married = rr.getInt("ISMARRIAGED")==1?"已婚":"未婚";
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
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (wwb != null) {
                    try {
                        wwb.close();
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
    public String syncUnit() {
        deptUserService.syncUnit();
        return responseUtil.getResultJson(true).toString();
    }

    @ResponseBody
    @RequestMapping(value = "/user/changeSkin", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String changeSkin(String skinCode, HttpServletResponse response) {
        IUserSetupService userSetupService = SpringUtil.getBean(IUserSetupService.class);
        UserSetup userSetup = userSetupService.getUserSetup(SpringUtil.getUserName());
        userSetup.setSkinCode(skinCode);
        boolean re = userSetupService.updateByUserName(userSetup);
        if (re) {
            UserSet.setSkin(request, response, skinCode);
        }
        return responseUtil.getResultJson(re).toString();
    }

    @RequestMapping(value = "/session/invalid")
    public String sessionValid(Model model) {
        return "logout";
    }
}
