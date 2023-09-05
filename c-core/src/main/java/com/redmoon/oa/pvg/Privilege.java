package com.redmoon.oa.pvg;

import cn.js.fan.base.IPrivilege;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.cache.RoleCache;
import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.listener.SessionListener;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.*;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.security.AesUtil;
import com.cloudwebsoft.framework.util.IPUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.LogDb;
import com.redmoon.oa.LogUtil;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.InvalidNameException;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.WrongPasswordException;
import com.redmoon.oa.sys.DebugUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Privilege implements IPrivilege {
    /**
     * session中用于保存用户名的键
     */
    public static final String NAME = ConstUtil.SESSION_NAME;
    /**
     * session中用于保存密码MD5值的键
     */
    public static final String PWDMD5 = ConstUtil.SESSION_PWDMD5;

    /**
     * 超级管理员权限名称
     */
    public static final String ADMIN = ConstUtil.PRIV_ADMIN;

    /**
     * 权限非法时的提示
     */
    public static final String MSG_INVALID = "对不起，您无权访问！";

    /**
     * 帐号被停用时的提示
     */
    public static final String MSG_ACCOUNT_INVALID = "对不起，您的帐号已被停用！";

    /**
     * session中的Authoration的键
     */
    public static final String SESSION_OA_AUTH = "oa.auth";

    /**
     * session中保存unitCode的键
     */
    public static final String UNITCODE = ConstUtil.SESSION_UNITCODE;
    
    /**
     * session中保存流程测试员的用户名
     */
    public static final String SESSION_OA_FLOW_TESTER = "oa.flowTester";

    public Privilege() {
    }

    /**
     * 用以判别用户是否具有某权限的子项，如是否拥有管理员的某些权限，这些权限名称以admin为前缀，如管理用户的权限admin.user
     * @return boolean
     */
    public boolean isUserHasPrivStartWith(HttpServletRequest request,
                                          String privPrefix) {
        if (!isUserLogin(request)) {
            return false;
        }
        // if (getUser(request).equals(UserDb.ADMIN)) // admin 享有所有权限
        //     return true;
        // 查用户本人是否拥有权限
        UserDb user = new UserDb(getUser(request));
        String[] privs = user.getPrivs();
        if (privs != null) {
            int len = privs.length;
            for (int i = 0; i < len; i++) {
                // 拥有某些管理员权限
                if (privs[i].startsWith(privPrefix)) {
                    return true;
                }
            }
        }
        // 根据其所属角色来判断是否具有相应的权限
        RoleDb[] rgs = user.getRoles();
        if (rgs != null) {
            int k = rgs.length;
            RolePrivDb rpd = new RolePrivDb();
            for (int i = 0; i < k; i++) {
                if (rpd.isRoleHasPrivStartWith(rgs[i].getCode(), privPrefix)) {
                    return true;
                }
            }
        }

        UserGroupDb[] ug = user.getGroups();
        // 根据组权限来判断是否有相应权限
        if (ug != null) {
            int k = ug.length;
            UserGroupPrivDb ugpd = new UserGroupPrivDb();
            for (int i = 0; i < k; i++) {
                if (ugpd.isUserGroupHasPrivStartWith(ug[i].getCode(),
                        privPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 用户是否具有管理员的一些权限，用以在进入菜单项超级管理时判别
     * @return boolean
     */
    public boolean isUserHasAnyAdminPriv(HttpServletRequest request) {
        if (!isUserLogin(request)) {
            return false;
        }
        // admin 享有所有权限
        if (getUser(request).equals(UserDb.ADMIN)) {
            return true;
        }
        if (isUserPrivValid(request, "admin")) {
            return true;
        }
        return isUserHasPrivStartWith(request, PrivDb.PRIV_ADMIN + ".");
    }

    /**
     * 判断请求是否来自于本服务器
     * @param request HttpServletRequest
     * @return boolean
     * @throws SQLException
     */
    public boolean isRequestValid(HttpServletRequest request) throws
            SQLException {
        return request.getRequestURL().indexOf(request.getServerName()) != -1;
    }

    /**
     * 验证用户名和密码是否有效
     * @param name String
     * @param pwdMD5 String 经MD5加密后的密码
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean Authenticate(String name, String pwdMD5) throws
            ErrMsgException {
        UserDb ud;
        UserMgr um = new UserMgr();
        ud = um.getUserDb(name);
        if (ud == null || !ud.isLoaded()) {
            return false;
        }
        // 检查大小写是否有问题，已在login方法中将name置为小写
        // com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).info("Authenticate:" + name + " " + ud.getName());
        // if (!ud.getName().equals(name)) {
        //    return false;
        // }
        return ud.getPwdMD5().equals(pwdMD5);
    }

    /**
     * 跳转时用以填充登录信息
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @param name String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean JumpToOA(HttpServletRequest req, HttpServletResponse res,
                            String name) throws ErrMsgException {
        com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
        String pwdMD5 = ud.getPwdMD5();
        doLogin(req, name, pwdMD5);
        return true;
    }

    /**
     * 判断用户是否已登录
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean isUserLogin(HttpServletRequest request) {
        AuthUtil authUtil = SpringUtil.getBean(AuthUtil.class);
        return authUtil.isUserLogin(request);
    }

    /**
     * 取得用户名
     * @param request HttpServletRequest
     * @return String
     */
    @Override
    public String getUser(HttpServletRequest request) {
        String userName = null;
        HttpSession session = request.getSession(true);
        if (session != null) {
            userName = (String) session.getAttribute(NAME);
            if (userName != null) {
                return userName;
            }
        }

        userName = SpringUtil.getUserName();
        // 当对Spring Security permitAll的路径进行访问时，SprintUtil.getUserName取得的用户名为 ANONYMOUS_USER
        if (ConstUtil.ANONYMOUS_USER.equals(userName)) {
            return null;
        }

        return userName;
    }

    public String getUser() {
        return SpringUtil.getUserName();
    }

    public static String getCurRoleCode() {
        HttpSession session = SpringUtil.getRequest().getSession(true);
        return (String)session.getAttribute(ConstUtil.SESSION_CUR_ROLE);
    }

    public static String getCurDeptCode() {
        HttpSession session = SpringUtil.getRequest().getSession(true);
        return (String)session.getAttribute(ConstUtil.SESSION_CUR_DEPT);
    }

    public static void setCurRoleCode(String curRoleCode) {
        HttpSession session = SpringUtil.getRequest().getSession(true);
        session.setAttribute(ConstUtil.SESSION_CUR_ROLE, curRoleCode);
    }

    public static void setCurDeptCode(String curDeptCode) {
        HttpSession session = SpringUtil.getRequest().getSession(true);
        session.setAttribute(ConstUtil.SESSION_CUR_DEPT, curDeptCode);
    }
    
    public static Object getAttribute(HttpServletRequest request, String attName) {
        HttpSession session = request.getSession(true);
        return session.getAttribute(attName);    	
    }

    public static void setAttribute(HttpServletRequest request, String attName, Object attValue) {
        HttpSession session = request.getSession(true);
        session.setAttribute(attName, attValue);    	
    }    
    
    /**
     * 取得用户所在单位的编码
     * @param request HttpServletRequest
     * @return String
     */
    public String getUserUnitCode(HttpServletRequest request) {
        // 从session中取得的Authorization有时会为null，看似丢失了该属性，但是此时登录的NAME及PWDMD5依然有效，致取不到unitCode
        // 经测试是reload导致了丢失
        /*
			Authorization auth = getAuthorization(request);
            if (auth!=null)
            	return auth.getUnitCode();
            else
            	return "";
         */
        String unitCode = null;
        HttpSession session = request.getSession(true);
        if (session != null) {
            unitCode = (String) session.getAttribute(UNITCODE);
        }
        if (unitCode == null) {
            String userName = getUser(request);
            if (userName != null) {
                DeptUserDb dud = new DeptUserDb();
                unitCode = dud.getUnitOfUser(userName).getCode();
                session.setAttribute(UNITCODE, unitCode);
            }
        }
        return unitCode;
    }

    public String getUserUnitCode() {
        AuthUtil authUtil = SpringUtil.getBean(AuthUtil.class);
        return authUtil.getUserUnitCode();
    }

    /**
     * 取得用户经MD5加密的密码
     * @param request HttpServletRequest
     * @return String
     */
    public static String getPwd(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return (String) session.getAttribute(PWDMD5);
    }

    /**
     * 取得用户有管理权的单位
     * @param request HttpServletRequest
     * @return Vector
     * @throws ErrMsgException
     */
    public Vector getUserAdminUnits(HttpServletRequest request) throws ErrMsgException {
        Vector vv = new Vector();
        if (isUserPrivValid(request, ADMIN)) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(DeptDb.ROOTCODE);
            vv.addElement(dd);
            Vector v = dd.getChildren();
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                dd = (DeptDb) ir.next();
                if (dd.getType() == DeptDb.TYPE_UNIT) {
                    vv.addElement(dd);
                }
            }
        } else if (isUserPrivValid(request, "admin.unit")) {
            DeptUserDb dud = new DeptUserDb();
            vv.addElement(dud.getUnitOfUser(getUser(request)));
        }
        return vv;
    }

    /**
     * 判断用户是否有管理某单位的权限
     * @param request HttpServletRequest
     * @param unitCode String
     * @return boolean
     */
    public boolean canUserAdminUnit(HttpServletRequest request, String unitCode) {
        if (isUserPrivValid(request, "admin")) {
            return true;
        }

        if (isUserPrivValid(request, "admin.unit")) {
            String userName = getUser(request);
            DeptUserDb dud = new DeptUserDb();
            return dud.getUnitOfUser(userName).getCode().equals(unitCode);
        }
        return false;
    }

    /**
     * 取得用户管理的部门
     * @param userName
     * @return
     */
    public static Vector<DeptDb> getUserAdminDepts(String userName) {
        Vector v = new Vector();
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        Map map = new HashMap();
        // 取得用户所属的角色
        RoleDb[] roles = ud.getRoles();
        for (int i=0; i<roles.length; i++) {
            String[] depts = roles[i].getAdminDepts();
            if (depts != null) {
                int len = depts.length;
                for (int j = 0; j < len; j++) {
                    map.put(depts[j], depts[j]);
                }
            }
            
            String[] ary = roles[i].getDeptsOfManager(userName);
            if (ary!=null) {
            	for (int j=0; j<ary.length; j++) {
            		map.put(ary[j], ary[j]);
            	}
            }
        }

        String[] depts = ud.getAdminDepts();
        if (depts != null) {
            int len = depts.length;
            for (int i = 0; i < len; i++) {
                map.put(depts[i], depts[i]);
            }
        }

        DeptMgr dm = new DeptMgr();
        Iterator ir = map.keySet().iterator();
        while (ir.hasNext()) {
            DeptDb dd = dm.getDeptDb((String)ir.next());
            v.addElement(dd);
        } 

        return v;
    }

    /**
     * 取得用户拥有管理权的部门，包括用户自身能管理的部门及用户所属角色能够管理的部门
     * @return Vector 存储部门对象DeptDb
     */
    public Vector<DeptDb> getUserAdminDepts(HttpServletRequest request) throws ErrMsgException {
        if (isUserPrivValid(request, ADMIN)) {
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(DeptDb.ROOTCODE);
            Vector<DeptDb> v = new Vector<>();
            dd.getAllChild(v, dd);
            return v;
        }

        String userName = getUser(request);
        return getUserAdminDepts(userName);
    }

    /**
     * 用户能否管理部门
     * @param request
     * @param deptCode 部门编码
     * @return
     */
    public static boolean canUserAdminDept(HttpServletRequest request, String deptCode) {
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(request, "admin")) {
            return true;
        }
        if (pvg.isUserPrivValid(request, "admin.unit")) {
            String unitCode = pvg.getUserUnitCode(request);
            if (unitCode.equals(deptCode)) {
                return true;
            }

            // 检查deptCode所属的单位是否为unitCode
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            dd = dd.getUnitOfDept(dd);
            if (dd.getCode().equals(unitCode)) {
                return true;
            }

            // 检查dd.getCode是否为unitCode的下属单位
            while (!"-1".equals(dd.getParentCode())) {
                dd = dd.getDeptDb(dd.getParentCode());
                if (dd==null) {
                    break;
                }
                if (dd.getCode().equals(unitCode)) {
                    return true;
                }
                if (dd.getCode().equals(DeptDb.ROOTCODE)) {
                    break;
                }
            }
        }

        return canUserAdminDept(pvg.getUser(request), deptCode);
    }


    /**
     * 用户能否管理部门
     * @param userName String 用户名
     * @param deptCode String 部门编码
     * @return boolean
     */
    public static boolean canUserAdminDept(String userName, String deptCode) {
        if (!canUserAdminDeptSingle(userName, deptCode)) {
            return canUserAdminDeptAncestor(userName, deptCode);
        }
        else {
            return true;
        }
    }

    /**
     * 用户能否管理某个部门
     * @param userName
     * @param deptCode
     * @return
     */
    public static boolean canUserAdminDeptSingle(String userName, String deptCode) {
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);

        // 判断用户所属的角色能管理的部门
        RoleDb[] roles = ud.getRoles();
        for (int i = 0; i < roles.length; i++) {
        	// 根据角色能否管理本部门判断
        	if (roles[i].isDeptManager()) {
        		// 判断deptCode是否为自己所管理的部门
        		DeptUserDb dud = new DeptUserDb();
                for (DeptDb dd : dud.getDeptsOfUser(userName)) {
                    if (dd.getCode().equals(deptCode)) {
                        return true;
                    }
                }
        	}
        	
            String[] depts = roles[i].getAdminDepts();
            if (depts != null) {
                for (String dept : depts) {
                    if (dept.equals(deptCode)) {
                        return true;
                    }
                }
            }
        }
        
        // 判断用户能管理的部门
        String[] depts = ud.getAdminDepts();
        if (depts != null) {
            for (String dept : depts) {
                if (dept.equals(deptCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断对于deptCode的祖先节点是否具有管理权限
     * @param userName String
     * @param deptCode String
     * @return boolean
     */
    public static boolean canUserAdminDeptAncestor(String userName, String deptCode) {
        // 检测其祖先节点，如果有权限，则继承权限
        DeptMgr dm = new DeptMgr();
        DeptDb dd = dm.getDeptDb(deptCode);

        String parentCode = dd.getParentCode();
        boolean isAncestorPrivValid = false;
        while (!"-1".equals(parentCode)) {
            dd = dm.getDeptDb(parentCode);
            if (dd == null) {
                break;
            }
            isAncestorPrivValid = canUserAdminDeptSingle(userName, dd.getCode());
            if (isAncestorPrivValid) {
                break;
            }

            parentCode = dd.getParentCode();
        }
        return isAncestorPrivValid;
    }

    /**
     * 判别当前用户能否管理某部门
     * @param request HttpServletRequest
     * @param deptCode String 被管理的部门
     * @return boolean
     */
    public boolean canAdminDept(HttpServletRequest request, String deptCode) {
        return canUserAdminDept(request, deptCode);
    }

    /**
     * 判别当前用户能否管理某用户
     * @param request HttpServletRequest
     * @param userName String 被管理的用户名
     * @return boolean
     */
    public boolean canAdminUser(HttpServletRequest request, String userName) {
        if (isUserPrivValid(request, ADMIN)) {
            return true;
        }
        // 取得用户所属的部门
        DeptUserDb dud = new DeptUserDb();
        Iterator ir = dud.getDeptsOfUser(userName).iterator();
        while (ir.hasNext()) {
            DeptDb dd = (DeptDb) ir.next();
            if (canAdminDept(request, dd.getCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否有admin的权限
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean isValid(HttpServletRequest request) {
        return isUserPrivValid(request, "admin");
    }

    /**
     * 取得session中的Authorization
     * @param request HttpServletRequest
     * @return Authorization
     */
    public static Authorization getAuthorization(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return (Authorization) session.getAttribute(SESSION_OA_AUTH);
    }

    /**
     * 设置session中的Authorization
     * @param request HttpServletRequest
     * @param auth Authorization
     */
    public static void setAuthorization(HttpServletRequest request,
                                        Authorization auth) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_OA_AUTH, auth);
    }

    /**
     * 登录时在session中置登录相关的信息
     * @param req HttpServletRequest
     * @param name String
     * @param pwdMD5 String
     */
    public void doLogin(HttpServletRequest req, String name, String pwdMD5) {
        // 检查是否已有该在线用户在不同地点登录，如果是，则将该用户强制退出
        OnlineUserDb oud = new OnlineUserDb();
        oud = oud.getOnlineUserDb(name);
        String sessionId = req.getSession().getId();

        // 如果该用户已处于在线记录中
        if (oud.isLoaded()) {
            // sessionId当手机端登录时，为null
        	// 如果是pc端
        	if (oud.getClient().equals(OnlineUserDb.CLIENT_PC)) {
	            if (!oud.getSessionId().equals(sessionId)) {
	                /*HttpSession session = SessionListener.getSession(oud.getSessionId());
	                if (session != null) {
	                    // 一个帐户多重登录
                        // 前后端分离后，有可能会碰到session无效的情况，setAttribute: Session [*****] has already been invalidated
	                    session.setAttribute("loginOnOtherPlace", "y");
	                } else {
	                    com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).
	                            info("doLogin:Session with sessionId=" +
	                                  oud.getSessionId() +
	                                  " is not found in session filter's map.");
	                }*/
	                oud.setSessionId(sessionId);
	                oud.setClient(OnlineUserDb.CLIENT_PC);
                    oud.setIp(StrUtil.getIp(req));
                    oud.save();
	            }
        	}
        	else {
                oud.setSessionId(sessionId);
                oud.setClient(OnlineUserDb.CLIENT_PC);
                oud.setIp(StrUtil.getIp(req));
                oud.save();
        	}
        } else {
            // 如果在线记录中没有该用户，则创建在线记录
            oud.setName(name);
            oud.setIp(StrUtil.getIp(req));
            oud.setGuest(false);
            oud.setSessionId(sessionId);
            oud.setClient(OnlineUserDb.CLIENT_PC);
            oud.create();
        }

        doLoginSession(req, name, pwdMD5);
    }

    /**
     * 只对登录session处理，不处理在线列表，可用于手机端或者spark处理
     * @param req HttpServletRequest
     * @param name String
     * @param pwdMD5 String
     */
    public static void doLoginSession(HttpServletRequest req, String name, String pwdMD5) {
        HttpSession session = req.getSession(true);
        session.setAttribute(NAME, name);
        session.setAttribute(PWDMD5, pwdMD5);

        DeptUserDb dud = new DeptUserDb();
        String unitCode = dud.getUnitOfUser(name).getCode();

        Authorization auth = new Authorization(name);
        // 置用户所在的单位
        // auth.setUnitCode(unitCode);
        // 放在Authorization中，会致测试时，因reload而被从session中清除掉，但是reload却不会致NAME及PWDMD5被清除
        session.setAttribute(SESSION_OA_AUTH, auth);
        session.setAttribute(UNITCODE, unitCode);

        Config cfg = Config.getInstance();
        boolean isRoleSwitchable = cfg.getBooleanProperty("isRoleSwitchable");
        if (isRoleSwitchable) {
            String curRoleCode = getDefaultCurRoleCode(name);
            if (curRoleCode != null) {
                setCurRoleCode(curRoleCode);

                // 根据当前切换的角色，赋予相应的权限以便于spring security控制权限
                UserCache userCache = SpringUtil.getBean(UserCache.class);
                com.cloudweb.oa.entity.User user = userCache.getUser(name);

                List<UserOfRole> userOfRoleList = new ArrayList<>();
                IUserOfRoleService userOfRoleService = SpringUtil.getBean(IUserOfRoleService.class);
                userOfRoleList.add(userOfRoleService.getUserOfRole(name, curRoleCode));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(name, user.getPwd(), getRolesAndAuthorities(name, userOfRoleList));
                // 存放authentication到SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        if (cfg.getBooleanProperty("isDeptSwitchable")) {
            String curDeptCode = getDefaultCurDeptCode(name);
            if (curDeptCode!=null) {
                setCurDeptCode(curDeptCode);
            }
        }
    }

    public static Collection<GrantedAuthority> getRolesAndAuthorities(String userName, List<UserOfRole> roles) {
        if (Privilege.ADMIN.equals(userName)) {
            // 给admin加入ROLE_ADMIN角色
            return AuthorityUtils.commaSeparatedStringToAuthorityList("admin,ROLE_ADMIN");
        }

        List<GrantedAuthority> list = new ArrayList<>();

        // 加入ROLE_LOGIN，表示已登录
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_LOGIN");
        list.add(grantedAuthority);

        // 1. 放入角色时需要加前缀ROLE_，而在controller使用时不需要加ROLE_前缀
        // 2. 放入的是权限时，不能加ROLE_前缀，hasAuthority与放入的权限名称对应即可
        if (roles!=null) {
            for (UserOfRole userOfRole : roles) {
                grantedAuthority = new SimpleGrantedAuthority("ROLE_" + userOfRole.getRoleCode());
                list.add(grantedAuthority);
            }
        }

        UserAuthorityCache userAuthorityCache = SpringUtil.getBean(UserAuthorityCache.class);
        List<String> listAuthority = userAuthorityCache.getUserAuthorities(userName);
        for (String authority : listAuthority) {
            grantedAuthority = new SimpleGrantedAuthority(authority);
            list.add(grantedAuthority);
        }

        return list;
    }

    /**
     * 取得默认角色
     * @param name
     * @return
     */
    public static String getDefaultCurRoleCode(String name) {
        // 取最大的角色为默认角色
        UserCache userCache = SpringUtil.getBean(UserCache.class);
        List<Role> list = userCache.getRoles(name);
        if (list.size() > 1) {
            Comparator<Role> comp = (o1, o2) -> {
                //相当于从大到小排序，大值返回负值，往前放
                return -(o1.getOrders() - o2.getOrders());
            };
            list.sort(comp);
        }
        if (list.size() >= 1) {
            return list.get(0).getCode();
        }
        else {
            DebugUtil.w(Privilege.class, "doLoginSession", name + " has no role");
            return null;
        }
    }

    public static String getDefaultCurDeptCode(String userName) {
        Config cfg = Config.getInstance();
        boolean isRoleSwitchable = cfg.getBooleanProperty("isRoleSwitchable");
        // 如果角色可切换
        if (isRoleSwitchable) {
            String curRoleCode = getCurRoleCode();
            if (curRoleCode != null) {
                IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
                IUserOfRoleService userOfRoleService = SpringUtil.getBean(IUserOfRoleService.class);
                List<Department> list = departmentService.getDeptsOfUser(userName);
                for (Department department : list) {
                    // 取得第一个角色与当前部门均对应的记录作为默认的当前部门
                    if (userOfRoleService.isRoleOfDept(userName, curRoleCode, department.getCode())) {
                        return department.getCode();
                    }
                }
            }
            else {
                if (!ConstUtil.USER_ADMIN.equals(userName)) {
                    DebugUtil.e(Privilege.class, "getDefaultCurDeptCode", userName + " has none role");
                }
            }
        }
        else {
            IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
            List<Department> list = departmentService.getDeptsOfUser(userName);
            if (list.size() > 0) {
                return list.get(0).getCode();
            }
            else {
                if (!ConstUtil.USER_ADMIN.equals(userName)) {
                    DebugUtil.w(Privilege.class, "getDefaultCurDeptCode", userName + " has no dept");
                }
            }
        }
        return null;
    }

    /**
     * 登录
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @return boolean
     * @throws WrongPasswordException
     * @throws InvalidNameException
     * @throws ErrMsgException
     */
    public boolean login(HttpServletRequest req, HttpServletResponse res, Authentication authentication) throws
            WrongPasswordException, ErrMsgException {
        String name = (String)authentication.getPrincipal();

        // 许可证验证
        License.getInstance().validate(req);

        com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
        String pwdName = myconfig.getProperty("pwdName");
        String pwd = req.getParameter(pwdName);
        String pwdAesKey = myconfig.getProperty("pwdAesKey");
        String pwdAesIV = myconfig.getProperty("pwdAesIV");
        try {
            pwd = AesUtil.aesDecrypt(pwd, pwdAesKey, pwdAesIV);
        } catch (Exception e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(e);
        }

        // 通过key登录
        String pwdMD5 = "";
        if (pwd == null) {
            throw new WrongPasswordException();
        }
        try {
            pwdMD5 = SecurityUtil.MD5(pwd);
        } catch (Exception e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("login MD5 exception: " + e.getMessage());
        }

        UserDb user = new UserDb();
        user = user.getUserDb(name);
        if (user.isValid()) {
            name = user.getName();
            user.setLastLogin(new java.util.Date());
            user.save();
        } else {
            LogUtil.log(name, IPUtil.getRemoteAddr(req), LogDb.TYPE_LOGIN, "非法用户");
            throw new ErrMsgException("非法用户");
        }

        // 记录日志
        // LogUtil.log(name, IPUtil.getRemoteAddr(req), LogDb.TYPE_LOGIN, LogUtil.get(req, "action_login"));
        LogUtil.log(name, IPUtil.getRemoteAddr(req), LogDb.TYPE_LOGIN, SpringUtil.getBean(I18nUtil.class).get("action_login"));

        doLogin(req, name, pwdMD5);
        return true;
    }

    /**
     * 登录，用于手机端
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @return boolean
     * @throws WrongPasswordException
     * @throws InvalidNameException
     * @throws ErrMsgException
     */
    public boolean login(HttpServletRequest req, HttpServletResponse res) throws
            WrongPasswordException, ErrMsgException {
        String name = "";
        try {
            name = URLDecoder.decode(ParamUtil.get(req, "name", true),"utf-8");
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }

        // 许可性验证
        License.getInstance().validate(req);
        String type = License.getInstance().getType();
/*        if (License.TYPE_BIZ.equals(type) || License.TYPE_OEM.equals(type) || License.TYPE_SRC.equals(type)){
            Config cfg = new Config();
            boolean isUse = cfg.getBooleanProperty("systemIsOpen");
            if (!isUse){
                if (!name.equals(ADMIN)) {
                    throw new ErrMsgException("系统已停用");
                }
            }
        }*/

        com.redmoon.oa.security.Config myconfig = com.redmoon.oa.security.Config.getInstance();
        String pwdName = myconfig.getProperty("pwdName");
        String pwd = req.getParameter(pwdName);
        String pwdAesKey = myconfig.getProperty("pwdAesKey");
        String pwdAesIV = myconfig.getProperty("pwdAesIV");
        try {
            pwd = AesUtil.aesDecrypt(pwd, pwdAesKey, pwdAesIV);
        } catch (Exception e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(e);
        }

        // 通过key登录
        String keyId = ParamUtil.get(req, "keyId", true);
        String pwdMD5 = "";
        if (!"".equals(keyId)){
            String sql = "select name,pwd from user_setup t, users u where t.USER_NAME = u.name and t.key_id = " + StrUtil.sqlstr(keyId);
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = null;
            ResultRecord rr = null;
            try {
                ri = jt.executeQuery(sql);
                if(ri.hasNext()){
                    rr = ri.next();
                    name = rr.getString("name");
                    pwdMD5 = rr.getString("pwd");
                }
            } catch (SQLException e) {
                com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(e);
            }
        }else{
            IUserService userService = SpringUtil.getBean(IUserService.class);
            User user = userService.getUserByLoginName(name);
            if (user == null) {
                throw new ErrMsgException("非法用户!");
            }
            name = user.getName();

            if (pwd == null) {
                throw new WrongPasswordException();
            }
            try {
                pwdMD5 = SecurityUtil.MD5(pwd);
            } catch (Exception e) {
                com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("login MD5 exception: " + e.getMessage());
            }
        }

        boolean isAuthValid = false;

        if (Authenticate(name, pwdMD5)) {
            UserDb user = new UserDb();
            user = user.getUserDb(name);
            if (user.isValid()) {
                isAuthValid = true;
                name = user.getName();
                user.setLastLogin(new java.util.Date());
                user.save();
            } else {
                throw new ErrMsgException("非法用户");
            }
        } else {
            Config cfg = Config.getInstance();
            boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");
            if (isUseAccount) {
                // 检查是否使用了工号登录
                AccountDb ad = new AccountDb();
                ad = ad.getAccountDb(name);
                if (ad.isLoaded()) {
                    name = ad.getUserName();
                    if (!"".equals(name) && Authenticate(name, pwdMD5)) {
                        UserDb user = new UserDb();
                        user = user.getUserDb(name);
                        if (user.isValid()) {
                            isAuthValid = true;
                        } else {
                            throw new ErrMsgException("非法用户");
                        }
                    }
                }
            }
        }
        if (isAuthValid) {
            // 记录日志
            LogUtil.log(name, IPUtil.getRemoteAddr(req), LogDb.TYPE_LOGIN, SpringUtil.getBean(I18nUtil.class).get("action_login"));

            doLogin(req, name, pwdMD5);
            return true;
        } else {
            LogUtil.log(name, IPUtil.getRemoteAddr(req), LogDb.TYPE_LOGIN, SpringUtil.getBean(I18nUtil.class).get("warn_login_fail"));
            return false;
        }
    }

    /**
     * 退出登录
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean logout(HttpServletRequest req, HttpServletResponse res) throws
            ErrMsgException {
        HttpSession session = req.getSession(true);
        // LogUtil.log(getUser(req), IPUtil.getRemoteAddr(req), LogDb.TYPE_LOGIN, SpringUtil.getBean(I18nUtil.class).get("action_logout"));
        session.invalidate();
        return true;
    }

    /**
     * 判断角色是否拥有权限
     * @param roleCode String 角色编码
     * @param priv String 权限名称
     * @return boolean
     */
    public boolean isRolePrivValid(String roleCode, String priv) {
        RoleCache roleCache = SpringUtil.getBean(RoleCache.class);
        // 如果角色未启用，则无权限
        if (!roleCache.getRole(roleCode).getStatus()) {
            return false;
        }

        IRolePrivService rolePrivService = SpringUtil.getBean(IRolePrivService.class);
        boolean re = rolePrivService.isRolePrivValid(roleCode, ConstUtil.PRIV_ADMIN);
        if (re) {
            return true;
        }

        return rolePrivService.isRolePrivValid(roleCode, priv);
    }

    /**
     * 判断用户组是否拥有权限
     * @param groupCode String 用户组编码
     * @param priv String 权限名称
     * @return boolean
     */
    public boolean isGroupPrivValid(String groupCode, String priv) {
        // 管理员组拥有全部权限
        if (groupCode.equals(ConstUtil.GROUP_ADMINISTRATORS)) {
            return true;
        }

        // 检查用户组是否拥有权限
        IGroupPrivService groupPrivService = SpringUtil.getBean(IGroupPrivService.class);
        if (groupPrivService.isGroupPrivValid(groupCode, priv)) {
            return true;
        }

        // 检查用户组所属的角色是否拥有权限
        // 20220313 用户组与部门及角色不再关联
        /*IGroupOfRoleService groupOfRoleService = SpringUtil.getBean(IGroupOfRoleService.class);
        List<GroupOfRole> list = groupOfRoleService.listByGroupCode(groupCode);
        for (GroupOfRole groupOfRole : list) {
            if (isRolePrivValid(groupOfRole.getRoleCode(), priv)) {
                return true;
            }
        }*/

        return false;
    }

    /**
     * 设置角色权限
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean setRolePriv(HttpServletRequest request) throws ErrMsgException {
        String[] privs = request.getParameterValues("priv");
        String roleCode = ParamUtil.get(request, "roleCode");
        if ("".equals(roleCode)) {
            throw new ErrMsgException("用户组编码不能为空！");
        }
        int len = 0;
        if (privs != null) {
            len = privs.length;
        }
        String insertSql = "";

        ResultSet rs = null;
        PreparedStatement ps = null;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            String sql = "select roleCode,priv from user_role_priv where roleCode=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, roleCode);
            rs = conn.executePreQuery();
            if (rs != null) {
                RolePrivDb rp = new RolePrivDb();
                while (rs.next()) {
                    rp = rp.getRolePrivDb(rs.getString(1), rs.getString(2));
                    rp.del();
                }
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }

            PrivDb pd = new PrivDb();
            String desc = "";

            conn.beginTrans();
            for (int i = 0; i < len; i++) {
                insertSql =
                        "insert into user_role_priv (roleCode,priv) values (" +
                        StrUtil.sqlstr(roleCode) +
                        ", " + StrUtil.sqlstr(StrUtil.UnicodeToUTF8(privs[i])) +
                        ")";
                conn.executeUpdate(insertSql);

                pd = pd.getPrivDb(privs[i]);
                if ("".equals(desc)) {
                    desc = pd.getDesc();
                } else {
                    desc += "," + pd.getDesc();
                }
            }
            conn.commit();

            // 刷新属于该角色的用户的权限
            RoleDb rd = new RoleDb();
            rd = rd.getRoleDb(roleCode);
            IUserAuthorityService userAuthorityService = SpringUtil.getBean(IUserAuthorityService.class);
            Vector<UserDb> v = rd.getAllUserOfRole();
            Iterator<UserDb> ir = v.iterator();
            while (ir.hasNext()) {
                UserDb user = ir.next();
                userAuthorityService.refreshUserAuthority(user.getName());
            }

            // 操作日志
            // com.redmoon.oa.LogUtil.log(new Privilege().getUser(request), IPUtil.getRemoteAddr(request), LogDb.TYPE_PRIVILEGE, com.redmoon.oa.LogUtil.format(request, "action_role_grant", new Object[]{rd.getDesc(), desc}));
            LogUtil.log(getUser(request), IPUtil.getRemoteAddr(request), LogDb.TYPE_PRIVILEGE, StrUtil.format(SpringUtil.getBean(I18nUtil.class).get("action_role_grant"), new Object[]{rd.getDesc(), desc}));
        } catch (SQLException e) {
            conn.rollback();
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("setRolePriv:" + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    /**
     * 设置用户组的权限
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean setGroupPriv(HttpServletRequest request) throws
            ErrMsgException {
        String[] privs = request.getParameterValues("priv");
        String groupCode = ParamUtil.get(request, "group_code");

        if ("".equals(groupCode)) {
            throw new ErrMsgException("用户组编码不能为空！");
        }
        int len = 0;
        if (privs != null) {
            len = privs.length; // 一个权限都没有时会为null
        }

        String insertSql = "";

        Conn conn = new Conn(Global.getDefaultDB());
        try {
            String listsql =
                    "select groupCode, priv from user_group_priv where groupCode=?";
            PreparedStatement ps = conn.prepareStatement(listsql);
            ps.setString(1, groupCode);

            ResultSet rs = conn.executePreQuery();
            if (rs != null) {
                UserGroupPrivDb ugp = new UserGroupPrivDb();
                while (rs.next()) {
                    ugp = ugp.getUserGroupPrivDb(rs.getString(1),
                                                 rs.getString(2));
                    ugp.del();
                }
            }

            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (len > 0) {
                PrivDb pd = new PrivDb();
                String desc = "";

                conn.beginTrans();
                for (int i = 0; i < len; i++) {
                    insertSql =
                            "insert into user_group_priv (groupCode,priv) values (" +
                            StrUtil.sqlstr(groupCode) +
                            ", " + StrUtil.sqlstr(StrUtil.UnicodeToUTF8(privs[i])) +
                            ")";
                    // logger.info("setGroupPriv: insertSql=" + insertSql);

                    conn.executeUpdate(insertSql);

                    pd = pd.getPrivDb(privs[i]);
                    if ("".equals(desc)) {
                        desc = pd.getDesc();
                    } else {
                        desc += "," + pd.getDesc();
                    }
                }
                conn.commit();

                UserGroupDb ug = new UserGroupDb();
                ug = ug.getUserGroupDb(groupCode);

                // 刷新用户的权限
                IUserAuthorityService userAuthorityService = SpringUtil.getBean(IUserAuthorityService.class);
                Vector<UserDb> v = ug.getAllUserOfGroup();
                Iterator<UserDb> ir = v.iterator();
                while (ir.hasNext()) {
                    UserDb user = ir.next();
                    userAuthorityService.refreshUserAuthority(user.getName());
                }

                // com.redmoon.oa.LogUtil.log(new Privilege().getUser(request), StrUtil.getIp(request), LogDb.TYPE_PRIVILEGE, com.redmoon.oa.LogUtil.format(request, "action_group_grant", new Object[]{ug.getDesc(), desc}));
                LogUtil.log(getUser(request), IPUtil.getRemoteAddr(request), LogDb.TYPE_PRIVILEGE, StrUtil.format(SpringUtil.getBean(I18nUtil.class).get("action_group_grant"), new Object[]{ug.getDesc(), desc}));
            }
        } catch (SQLException e) {
            if (len > 0) {
                conn.rollback();
            }
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("setGroupPriv:" + e.getMessage());
            return false;
        } finally {
            conn.close();
        }
        return true;
    }

    public boolean isUserPrivValid(String userName, String priv) {
        Config cfg = Config.getInstance();
        if (cfg.getBooleanProperty("isRoleSwitchable")) {
            return isUserPrivValidByDb(userName, priv);
        }
        else {
            AuthUtil authUtil = SpringUtil.getBean(AuthUtil.class);
            return authUtil.isUserPrivValid(userName, priv);
        }
    }

    /**
     * 判断用户是否拥有权限
     * @param userName String 用户名
     * @param priv String 权限名称
     * @return boolean
     */
    public boolean isUserPrivValidByDb(String userName, String priv) {
        if (userName==null) {
        	return false;
        }
        
        // PRIV_READ表示是否登录
        if (priv.equals(PrivDb.PRIV_READ)) {
            return true;
        }

        // admin 享有所有权限
        if (userName.equals(ConstUtil.USER_ADMIN)) {
            return true;
        }

        // IUserService userService = SpringUtil.getBean(IUserService.class);
        // User user = userService.getUser(userName);

        /*
        判断全部用户角色是否拥有权限，因为user.getRoles()已经包含了全部用户，因此不需要在此判断
        RoleDb rd = new RoleDb();
        rd = rd.getRoleDb(RoleDb.CODE_MEMBER);
        if (isRolePrivValid(rd, priv))
            return true;
        */

        // 根据其所属角色来判断是否具有相应的权限
        IRolePrivService rolePrivService = SpringUtil.getBean(IRolePrivService.class);
        if (rolePrivService.isRolePrivValid(ConstUtil.ROLE_MEMBER, priv)) {
            return true;
        }
        else {
            Config cfg = Config.getInstance();
            if (cfg.getBooleanProperty("isRoleSwitchable")) {
                String curRoleCode = getCurRoleCode();
                if (curRoleCode!=null) {
                    if (isRolePrivValid(getCurRoleCode(), priv)) {
                        return true;
                    }
                }
                else {
                    DebugUtil.e(getClass(), "isUserPrivValidByDb", "curRoleCode is null");
                }
            }
            else {
                // 取用户所属的全部角色
                IRoleService roleService = SpringUtil.getBean(IRoleService.class);
                List<Role> roleList = roleService.getAllRolesOfUser(userName, false);
                for (Role role : roleList) {
                    if (isRolePrivValid(role.getCode(), priv)) {
                        return true;
                    }
                }
            }
        }

        IUserPrivService userPrivService = SpringUtil.getBean(IUserPrivService.class);
        if (userPrivService.isUserPrivValid(userName, ConstUtil.PRIV_ADMIN)) {
            return true;
        }
        else {
            if (userPrivService.isUserPrivValid(userName, priv)) {
                return true;
            }
        }

        IGroupPrivService groupPrivService = SpringUtil.getBean(IGroupPrivService.class);
        if (groupPrivService.isGroupPrivValid(ConstUtil.GROUP_EVERYONE, priv)) {
            return true;
        }
        else {
            IGroupService groupService = SpringUtil.getBean(IGroupService.class);
            List<Group> groupList = groupService.getAllGroupsOfUser(userName);
            for (Group group : groupList) {
                if (isGroupPrivValid(group.getCode(), priv)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断用户是否拥有权限
     * @param request HttpServletRequest
     * @param priv String 权限名称
     * @return boolean
     */
    public boolean isUserPrivValid(HttpServletRequest request, String priv) {
        if (!isUserLogin(request)) {
            return false;
        }
        return isUserPrivValid(getUser(request), priv);
    }

    /**
     * 判断用户是否拥有权限，功能同isUserPrivValid方法
     * @param request HttpServletRequest
     * @param priv String 权限名称
     * @return boolean
     */
    @Override
    public boolean isValid(HttpServletRequest request, String priv) {
        return isUserPrivValid(request, priv);
    }

    /**
     * 取得拥有某权限的全部用户，如：用于合同到期时发送短消息
     * @param priv String
     * @return Vector
     */
    public static Vector getUsersHavePriv(String priv) {
        Vector v = new Vector();
        String sql =
                "select username from user_priv where priv=? order by username asc";
        ResultIterator ri = null;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ri = jt.executeQuery(sql, new Object[] {priv});

            ResultRecord rr = null;
            UserDb user;
            UserMgr um = new UserMgr();
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String userName = rr.getString(1);
                user = um.getUserDb(userName);
                v.addElement(user);
            }

            UserGroupDb ug;
            UserGroupMgr ugm = new UserGroupMgr();
            sql =
                    "select groupcode from user_group_priv where priv=? order by groupcode asc";
            ri = jt.executeQuery(sql, new Object[] {priv});
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String groupCode = rr.getString(1);
                ug = ugm.getUserGroupDb(groupCode);
                // 检查是否有重复，不重复则加入v
                Vector v2 = ug.getAllUserOfGroup();
                Iterator ir2 = v2.iterator();
                while (ir2.hasNext()) {
                    UserDb ud2 = (UserDb)ir2.next();
                    boolean isRepeat = false;
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        if (ud2.getName().equals(ud.getName())) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat) {
                        v.addElement(ud2);
                    }
                }
            }

            // 2010-8-25修复此bug，原来是将所有的role加入了返回值
            RoleMgr rm = new RoleMgr();
            RoleDb role;
            sql =
                    "select rolecode from user_role_priv where priv=? order by rolecode asc";
            ri = jt.executeQuery(sql, new Object[] {priv});
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String roleCode = rr.getString(1);
                role = rm.getRoleDb(roleCode);

                // 检查是否有重复，不重复则加入v
                Vector v2 = role.getAllUserOfRole();
                Iterator ir2 = v2.iterator();
                while (ir2.hasNext()) {
                    UserDb ud2 = (UserDb)ir2.next();
                    boolean isRepeat = false;
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        if (ud2.getName().equals(ud.getName())) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat) {
                        v.addElement(ud2);
                    }
                }
            }
        }
        catch (SQLException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(Privilege.class).error(e);
        }
        return v;
    }

    /**
     * 取得某单位内拥有某权限的全部用户，如：用于合同到期提醒
     * @param priv String
     * @param unitCode String
     * @return Vector
     */
    public static Vector getUsersHavePriv(String priv, String unitCode) {
        Vector v = new Vector();
        String sql =
                "select p.username from user_priv p, users u where p.priv=? and u.unit_code=? and u.isValid=1 and p.username=u.name order by p.username asc";
        ResultIterator ri = null;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ri = jt.executeQuery(sql, new Object[] {priv, unitCode});

            ResultRecord rr = null;
            UserDb user;
            UserMgr um = new UserMgr();
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String userName = rr.getString(1);
                user = um.getUserDb(userName);
                v.addElement(user);
            }

            UserGroupDb ug;
            UserGroupMgr ugm = new UserGroupMgr();
            sql =
                    "select gp.groupcode from user_group_priv gp, user_group g where gp.priv=? and gp.groupCode=g.code and g.unit_code=? order by gp.groupcode asc";
            ri = jt.executeQuery(sql, new Object[] {priv, unitCode});
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String groupCode = rr.getString(1);
                ug = ugm.getUserGroupDb(groupCode);
                // 检查是否有重复，不重复则加入v
                Vector v2 = ug.getAllUserOfGroup();
                Iterator ir2 = v2.iterator();
                while (ir2.hasNext()) {
                    UserDb ud2 = (UserDb)ir2.next();
                    boolean isRepeat = false;
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        if (ud2.getName().equals(ud.getName())) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat) {
                        v.addElement(ud2);
                    }
                }
            }

            // 2010-8-25修复此bug，原来是将所有的role加入了返回值
            RoleMgr rm = new RoleMgr();
            RoleDb role;
            sql =
                    "select p.rolecode from user_role_priv p, user_role r where p.priv=? and p.rolecode=r.code and r.unit_code=? order by p.rolecode asc";
            ri = jt.executeQuery(sql, new Object[] {priv, unitCode});
            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                String roleCode = rr.getString(1);
                role = rm.getRoleDb(roleCode);

                // 检查是否有重复，不重复则加入v
                Vector v2 = role.getAllUserOfRole();
                Iterator ir2 = v2.iterator();
                while (ir2.hasNext()) {
                    UserDb ud2 = (UserDb)ir2.next();
                    boolean isRepeat = false;
                    Iterator ir = v.iterator();
                    while (ir.hasNext()) {
                        UserDb ud = (UserDb) ir.next();
                        if (ud2.getName().equals(ud.getName())) {
                            isRepeat = true;
                            break;
                        }
                    }
                    if (!isRepeat) {
                        v.addElement(ud2);
                    }
                }
            }
        }
        catch (SQLException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(Privilege.class).error(e);
        }
        return v;
    }

}
