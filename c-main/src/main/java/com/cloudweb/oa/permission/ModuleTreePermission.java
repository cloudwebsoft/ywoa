package com.cloudweb.oa.permission;

import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.constants.ModuleConst;
import com.cloudweb.oa.entity.Group;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.VisualModuleTreePriv;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.service.IVisualModuleTreePrivService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.PrivDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.UserGroupDb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Component
public class ModuleTreePermission {

    @Autowired
    HttpServletRequest request;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    UserCache userCache;

    @Autowired
    IVisualModuleTreePrivService visualModuleTreePrivService;

    public static final int TYPE_USERGROUP = 0;
    public static final int TYPE_USER = 1;
    public static final int TYPE_ROLE = 2;
    public static final int TYPE_DEPT = 3;

    public static final int PRIV_SEE = 0;
    public static final int PRIV_ADD = 1;
    public static final int PRIV_EDIT = 2;
    public static final int PRIV_DEL = 3;
    public static final int PRIV_MANAGE = 4;
    public static final int PRIV_EXPORT = 5;
    public static final int PRIV_IMPORT = 6;
    public static final int PRIV_EXPORT_WORD = 7;

    public boolean canDo(VisualModuleTreePriv treePriv, int privType) {
        if (privType == PRIV_ADD) {
            return treePriv.getPrivAdd() == 1;
        } else if (privType == PRIV_DEL) {
            return treePriv.getPrivDel() == 1;
        } else if (privType == PRIV_EDIT) {
            return treePriv.getPrivEdit() == 1;
        } else if (privType == PRIV_SEE) {
            return treePriv.getPrivSee() == 1;
        } else if (privType == PRIV_MANAGE) {
            return treePriv.getPrivManage() == 1;
        } else if (privType == PRIV_EXPORT) {
            return treePriv.getPrivExport() == 1;
        } else if (privType == PRIV_IMPORT) {
            return treePriv.getPrivImport() == 1;
        } else if (privType == PRIV_EXPORT_WORD) {
            return treePriv.getPrivExportWord() == 1;
        }
        return false;
    }

    /**
     * 判断对于单个结点leaf（并不往上查其父节点），用户是否有某权限
     *
     * @return boolean
     */
    public boolean canUserDo(TreeSelectDb tsd, String userName, List<Group> groups, List<Role> roles, int privType) {
        boolean re = false;
        // list该节点的所有拥有权限的用户
        List<VisualModuleTreePriv> list = visualModuleTreePrivService.list(tsd.getRootCode(), tsd.getCode());
        for (VisualModuleTreePriv treePriv : list) {
            // 遍历每个权限项
            //　权限项对应的是组用户
            if (treePriv.getPrivType() == TYPE_USERGROUP) {
                // 组为everyone
                if (treePriv.getName().equals(UserGroupDb.EVERYONE)) {
                    re = canDo(treePriv, privType);
                } else {
                    if (groups != null) {
                        // 判断该用户所在的组是否有权限
                        for (Group group : groups) {
                            if (group.getCode().equals(treePriv.getName())) {
                                re = canDo(treePriv, privType);
                                break;
                            }
                        }
                    }
                }
            } else if (treePriv.getPrivType() == TYPE_ROLE) {
                if (treePriv.getName().equals(RoleDb.CODE_MEMBER)) {
                    re = canDo(treePriv, privType);
                } else {
                    if (roles != null) {
                        // 判断该用户所属的角色是否有权限
                        for (Role role : roles) {
                            if (role.getCode().equals(treePriv.getName())) {
                                re = canDo(treePriv, privType);
                                break;
                            }
                        }
                    }
                }
            } else if (treePriv.getPrivType() == TYPE_DEPT) {
                IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
                if (deptUserService.isUserBelongToDept(userName, treePriv.getName())) {
                    re = canDo(treePriv, privType);
                }
            } else if (treePriv.getPrivType() == TYPE_USER) { //　个人用户
                if (treePriv.getName().equals(userName)) {
                    re = canDo(treePriv, privType);
                }
            }
        }

        // 如果是判断可见权限
        if (PRIV_SEE == privType) {
            // 如果节点上没有任何权限，则默认可见
            if (list.size() == 0) {
                return true;
            }
        }

        return re;
    }

    public boolean canUserDo(String userName, String nodeCode, int privType) {
        if (userName == null) {
            return false;
        }
        Privilege pvg = new Privilege();
        if (pvg.isUserPrivValid(userName, Privilege.ADMIN)) {
            return true;
        }

        //　判断用户是否具有管理员的权限
        String[] privs = userCache.getPrivs(userName);
        if (privs != null) {
            for (String priv : privs) {
                // 拥有管理员权限
                if (priv.equals(PrivDb.PRIV_ADMIN)) {
                    return true;
                }
            }
        }

        // 如果属于管理员组,则拥有全部权
        // LogUtil.getLog(getClass()).info("groups[i].code=" + groups[i].getCode());
        // for (int i = 0; i < groups.length; i++)
        //    if (groups[i].getCode().equals(groups[i].ADMINISTRATORS))
        //        return true;

        List<Role> roles = userCache.getRoles(userName);
        List<Group> groups = userCache.getGroups(userName);

        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(nodeCode);
        if (canUserDo(tsd, userName, groups, roles, privType)) {
            return true;
        }

        // 回溯其父节点，判别用户对其父节点是否有权限，回溯可到达根root节点
        String parentCode = tsd.getParentCode();
        // while (!parentCode.equals("-1") && !parentCode.equals("root")) {
        while (!"-1".equals(parentCode)) {
            // LogUtil.getLog(getClass()).info("dirCode=" + dirCode + " parentCode=" + parentCode);
            TreeSelectDb tsdParent = tsd.getTreeSelectDb(parentCode);
            if (tsdParent == null || !tsdParent.isLoaded()) {
                return false;
            }
            if (canUserDo(tsdParent, userName, groups, roles, privType)) {
                return true;
            }
            parentCode = tsdParent.getParentCode();
        }

        return false;
    }

    public boolean canAdd(String userName, String nodeCode) {
        if (userName == null) {
            return false;
        }

        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(nodeCode);
        // 根节点上不能添加
        if (tsd.getRootCode().equals(nodeCode)) {
            return false;
        }

        if (userName.equals(UserDb.ADMIN)) {
            return true;
        }

        List<Role> roles = userCache.getRoles(userName);
        List<Group> groups = userCache.getGroups(userName);

        return canUserDo(tsd, userName, groups, roles, PRIV_ADD) || canUserDo(userName, nodeCode, PRIV_MANAGE);
    }

    public boolean canDel(String userName, String nodeCode) {
        if (userName == null)
            return false;
        if (userName.equals(UserDb.ADMIN)) {
            return true;
        }

        List<Role> roles = userCache.getRoles(userName);
        List<Group> groups = userCache.getGroups(userName);

        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(nodeCode);

        return canUserDo(tsd, userName, groups, roles, PRIV_DEL) || canUserDo(userName, nodeCode, PRIV_MANAGE);
    }

    public boolean canExport(String userName, String nodeCode) {
        if (userName == null)
            return false;

        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(nodeCode);
        // 根节点上不能操作
        if (tsd.getRootCode().equals(nodeCode)) {
            return false;
        }

        if (userName.equals(UserDb.ADMIN)) {
            return true;
        }

        List<Role> roles = userCache.getRoles(userName);
        List<Group> groups = userCache.getGroups(userName);

        return canUserDo(tsd, userName, groups, roles, PRIV_EXPORT) || canUserDo(userName, nodeCode, PRIV_MANAGE);
    }

    public boolean canImport(String userName, String nodeCode) {
        if (userName == null)
            return false;

        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(nodeCode);
        // 根节点上不能操作
        if (tsd.getRootCode().equals(nodeCode)) {
            return false;
        }

        if (userName.equals(UserDb.ADMIN)) {
            return true;
        }

        List<Role> roles = userCache.getRoles(userName);
        List<Group> groups = userCache.getGroups(userName);

        return canUserDo(tsd, userName, groups, roles, PRIV_IMPORT) || canUserDo(userName, nodeCode, PRIV_MANAGE);
    }

    public boolean canExportWord(String userName, String nodeCode) {
        if (userName == null)
            return false;
        if (userName.equals(UserDb.ADMIN)) {
            return true;
        }

        List<Role> roles = userCache.getRoles(userName);
        List<Group> groups = userCache.getGroups(userName);

        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(nodeCode);

        return canUserDo(tsd, userName, groups, roles, PRIV_EXPORT_WORD) || canUserDo(userName, nodeCode, PRIV_MANAGE);
    }

    public boolean canSee(String userName, String nodeCode) {
        if (userName == null) {
            return false;
        }
        if (userName.equals(UserDb.ADMIN)) {
            return true;
        }
        List<Role> roles = userCache.getRoles(userName);
        List<Group> groups = userCache.getGroups(userName);

        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(nodeCode);
        return canUserDo(tsd, userName, groups, roles, PRIV_SEE) || canUserDo(userName, nodeCode, PRIV_MANAGE);
    }

    public boolean canEdit(String userName, String nodeCode) {
        if (userName == null) {
            return false;
        }
        if (userName.equals(UserDb.ADMIN)) {
            return true;
        }
        List<Role> roles = userCache.getRoles(userName);
        List<Group> groups = userCache.getGroups(userName);

        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(nodeCode);
        return canUserDo(tsd, userName, groups, roles, PRIV_EDIT) || canUserDo(userName, nodeCode, PRIV_MANAGE);
    }

    public boolean canManage(String userName, String nodeCode) {
        return canUserDo(userName, nodeCode, PRIV_MANAGE);
    }
}
