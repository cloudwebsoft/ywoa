package com.cloudweb.oa.permission;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.basic.SelectKindPriv;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.LeafPriv;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.visual.ModulePrivDb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Vector;

import static com.redmoon.oa.ui.menu.Leaf.TYPE_FLOW;

@Slf4j
@Component
public class MenuPermission {

    public boolean canRoleSee(String roleCode, Menu childlf, String[] rolePrivs) {
        if (childlf.getType() == ConstUtil.MENU_TYPE_MODULE) {
            String moduleCode = childlf.getFormCode();
            ModulePrivDb mpd = new ModulePrivDb();
            Vector v = mpd.getModulePrivsOfModule(moduleCode);
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
                ModulePrivDb lp = (ModulePrivDb) ir.next();
                if (lp.getType() == ModulePrivDb.TYPE_ROLE) {
                    if (lp.getName().equals(ConstUtil.ROLE_MEMBER)) {
                        if (lp.getSee() == 1 || lp.getManage() == 1) {
                            // LogUtil.getLog(getClass()).info(getClass() + " " + lp.getName() + " " + moduleCode);
                            return true;
                        }
                    } else if (roleCode.equals(lp.getName())) {
                        if (lp.getSee() == 1 || lp.getManage() == 1) {
                            return true;
                        }
                    }
                }
            }
        } else if (childlf.getType() == ConstUtil.MENU_TYPE_FLOW) {
            LeafPriv leafPriv = new LeafPriv(childlf.getFormCode());
            // list该节点的所有拥有权限的用户
            Vector v = leafPriv.listPriv(LeafPriv.PRIV_SEE);
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
                LeafPriv lp = (LeafPriv) ir.next();
                if (lp.getType() == LeafPriv.TYPE_ROLE) {
                    if (lp.getName().equals(ConstUtil.ROLE_MEMBER)) {
                        if (lp.getSee() == 1) {
                            return true;
                        }
                    } else if (roleCode.equals(lp.getName())) {
                        if (lp.getSee() == 1) {
                            return true;
                        }
                    }
                }
            }
        } else if (childlf.getType() == ConstUtil.MENU_TYPE_LINK) {
            if (StrUtil.isEmpty(childlf.getPvg())) {
                return true;
            }
            if (rolePrivs != null) {
                for (String pv : rolePrivs) {
                    String[] ary = StrUtil.split(childlf.getPvg(), ",");
                    if (ary != null) {
                        for (int k = 0; k < ary.length; k++) {
                            if (pv.equals(ary[k])) {
                                return true;
                            }
                        }
                    }
                }
            }
        } else if (childlf.getType() == ConstUtil.MENU_TYPE_BASICDATA) { // 如果是基礎數據管理
            int kindId = StrUtil.toInt(childlf.getFormCode(), -1);
            SelectKindPriv skp = new SelectKindPriv();
            skp.setKindId(kindId);
            // list该节点的所有拥有权限的用户?
            Vector r = skp.listPriv(SelectKindPriv.PRIV_APPEND);
            Iterator ir = r.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
                SelectKindPriv lp = (SelectKindPriv) ir.next();
                if (lp.getType() == SelectKindPriv.TYPE_ROLE) {
                    if (lp.getName().equals(ConstUtil.ROLE_MEMBER)) {
                        return true;
                    } else {
                        if (roleCode.equals(lp.getName())) {
                            return true;
                        }
                    }
                }
            }
            r = skp.listPriv(SelectKindPriv.PRIV_MODIFY);
            ir = r.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
                SelectKindPriv lp = (SelectKindPriv) ir.next();
                if (lp.getType() == SelectKindPriv.TYPE_ROLE) {
                    if (lp.getName().equals(ConstUtil.ROLE_MEMBER)) {
                        return true;
                    } else {
                        if (roleCode.equals(lp.getName())) {
                            return true;
                        }
                    }
                }
            }
            r = skp.listPriv(SelectKindPriv.PRIV_DEL);
            ir = r.iterator();
            while (ir.hasNext()) {
                // 遍历每个权限项
                SelectKindPriv lp = (SelectKindPriv) ir.next();
                if (lp.getType() == SelectKindPriv.TYPE_ROLE) {
                    if (lp.getName().equals(ConstUtil.ROLE_MEMBER)) {
                        return true;
                    } else {
                        if (roleCode.equals(lp.getName())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * 判断用户能否看到菜单项
     * @param menu Menu
     * @return boolean
     */
    public boolean canUserSee(HttpServletRequest request, Menu menu) {
        if (menu.getIsUse()!=1) {
            return false;
        }

        long t = System.currentTimeMillis();

        boolean re = false;
        // 如果指定的可见权限不为空，则按权限判断（优先级最高）
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String pvg = menu.getPvg();
        if (!StrUtil.isEmpty(pvg)) {
            // 替换全角逗号
            pvg = pvg.replaceAll("，", ",");
            String[] ary = StrUtil.split(pvg, ",");
            for (String value : ary) {
                // 如果禁止管理员看到
                if ("!admin".equalsIgnoreCase(value.trim())) {
                    if (privilege.isUserPrivValid(request, "admin")) {
                        re = false;
                    }
                }
            }
            // layer=2，对应于菜单上的一级大类项
            if (menu.getLayer() == 2) {
                if (privilege.isUserPrivValid(request, "admin")) {
                    re = true;
                }
            }

            if (!re) {
                for (String s : ary) {
                    if (menu.getLayer() == 2) {
                        // 20161115 qcg发现BUG，如果菜单项置为admin可见，则admin.flow即具有流程查询权限的也可见
                        if (privilege.isUserPrivValid(request, s.trim())) {
                            // 202004 为提高效率不再支持以权限编码打头的权限的判断
                            // || (!ary[i].trim().equals(com.redmoon.oa.pvg.Privilege.ADMIN) && privilege.isUserHasPrivStartWith(request, ary[i].trim()))) {
                            re = true;
                        }
                    } else {
                        if (privilege.isUserPrivValid(request, s.trim())) {
                            re = true;
                        }
                    }
                }
            }
            return re;
        }

        // 在微信端会用到，而weixin不处于spring security的扫描目录下，无法通过其获取userName
        // String userName = SpringUtil.getUserName();
        String userName = privilege.getUser(request);
        if (menu.getType()==TYPE_FLOW) {
            com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
            lf = lf.getLeaf(menu.getFormCode());
            if (lf==null) {
                return false;
            }
            com.redmoon.oa.flow.DirectoryView dv = new com.redmoon.oa.flow.DirectoryView(lf);
            re = dv.canUserSeeWhenInitFlow(request, lf);
        } else if (menu.getType()==ConstUtil.MENU_TYPE_MODULE) {
            ModulePrivDb mpd = new ModulePrivDb(menu.getFormCode());
            re = mpd.canUserSee(userName);
        }
        else if (menu.getType()==ConstUtil.MENU_TYPE_BASICDATA) {
            SelectKindPriv skp = new SelectKindPriv();
            int kindId = StrUtil.toInt(menu.getFormCode(), -1);
            re = skp.canUserAppend(userName, kindId) || skp.canUserModify(userName, kindId) || skp.canUserDel(userName, kindId);
        } else {
            // 特殊菜单项
            re = canUserSeeSpecial(userName, menu);
        }

        /*if (Global.getInstance().isDebug()) {
            long s = System.currentTimeMillis() - t;
            log.info(menu.getName() + " type=" + menu.getType() + " take " + s + " ms");
        }*/
        return re;
    }

    /**
     * 判断特殊菜单项是否可见，如：督办（或称部门工作）
     * @param menu
     * @return
     */
    public boolean canUserSeeSpecial(String userName, Menu menu) {
        boolean re = false;
        if (menu.getCode().equals(ConstUtil.MENU_SUPERVIS) || (menu.getParentCode().equals(ConstUtil.MENU_SUPERVIS_PARENT_CODE)
                && (menu.getName().equals(ConstUtil.MENU_SUPERVIS_NAME) ||menu.getName().equals(ConstUtil.MENU_SUPERVIS_NAME2)))){
            if (userName.equals(com.redmoon.oa.pvg.Privilege.ADMIN)) {
                re = true;
            }
            else {
                Vector<DeptDb> v = com.redmoon.oa.pvg.Privilege.getUserAdminDepts(userName);
                if (v.size() > 0) {
                    re = true;
                }
            }
        }
        else {
            re = true;
        }
        return re;
    }
}
