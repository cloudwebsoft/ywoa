package com.cloudweb.oa.service.impl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.MenuCache;
import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.entity.Privilege;
import com.cloudweb.oa.entity.RolePriv;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.mapper.MenuMapper;
import com.cloudweb.oa.permission.MenuPermission;
import com.cloudweb.oa.service.IMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.service.IPrivilegeService;
import com.cloudweb.oa.service.IRolePrivService;
import com.cloudweb.oa.service.IUserSetupService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.basic.SelectKindDb;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-15
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {
    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    IRolePrivService rolePrivService;

    @Autowired
    IPrivilegeService privilegeService;

    @Autowired
    MenuCache menuCache;

    @Autowired
    MenuMapper menuMapper;

    @Autowired
    MenuPermission menuPermission;

    @Override
    public Menu getMenu(String code) {
        QueryWrapper<Menu> qw = new QueryWrapper<>();
        qw.eq("code", code);
        return getOne(qw);
    }

    @Override
    public String getFullName(Menu menu) {
        String name = menu.getName();
        if (menu.getLayer() == 3) {
            Menu pMenu = getMenu(menu.getParentCode());
            name = pMenu.getName() + "\\" + name;
        }
        if (menu.getLayer() == 4) {
            Menu pMenu = getMenu(menu.getParentCode());
            name = pMenu.getName() + "\\" + name;
            pMenu = getMenu(pMenu.getParentCode());
            name = pMenu.getName() + "\\" + name;
        }
        return name;
    }

    @Override
    public String getRealLink(Menu menu) {
        if (menu.getType() == ConstUtil.MENU_TYPE_MODULE) {
            return "visual/module_list.jsp?code=" + StrUtil.UrlEncode(menu.getFormCode());
        } else if (menu.getType() == ConstUtil.MENU_TYPE_FLOW) {
            return "flow_initiate1.jsp?op=" + StrUtil.UrlEncode(menu.getFormCode());
        } else if (menu.getType() == ConstUtil.MENU_TYPE_BASICDATA) {
            return "admin/basic_select_list.jsp?kind=" + menu.getFormCode();
        } else if (menu.getType() == ConstUtil.MENU_TYPE_LINK) {
            if (menu.getLink().indexOf("$") != -1) {
                String userName = SpringUtil.getUserName();
                UserSetup userSetup = userSetupService.getUserSetup(userName);
                String lk = menu.getLink();
                lk = lk.replaceFirst("\\$emailName", StrUtil.UrlEncode(userSetup.getEmailName()));
                lk = lk.replaceFirst("\\$emailPwd", StrUtil.UrlEncode(userSetup.getEmailPwd()));
                // 替换为当前用户
                try {
                    // 先转成gbk的编码，用于润乾报表，对应的在showReport.jsp中接收参数时要加下行
                    // paramValue = new String(paramValue.getBytes("iso-8859-1"), "GB2312");
                    lk = lk.replaceFirst("\\$userName", java.net.URLEncoder.encode(userName, "GBK"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return lk;
            }

            // 链接需替换路径变量$u
            if (menu.getIsHasPath() == 1) {
                return menu.getLink().replaceFirst("\\$u", "/" + Global.getRootPath());
            } else {
                return menu.getLink();
            }
        } else {
            String lk = getPresetLink(menu);
            if (lk.equals("")) {
                return menu.getLink();
            } else {
                return lk;
            }
        }
    }

    @Override
    public List<Menu> getChildren(String parentCode) {
        QueryWrapper<Menu> qw = new QueryWrapper<>();
        qw.eq("parent_code", parentCode);
        qw.orderByAsc("orders");
        return list(qw);
    }

    /**
     * 取出code结点的所有孩子结点
     *
     * @return List
     */
    @Override
    public List getAllChild(List<Menu> list, Menu leaf) {
        List<Menu> children = menuCache.getChildren(leaf.getCode());
        if (children.isEmpty()) {
            return children;
        }
        list.addAll(children);
        for (Menu childMenu : children) {
            getAllChild(list, childMenu);
        }
        return list;
    }


    /**
     * 递归获得jsTree的json字符串
     *
     * @param parentCode 父节点parentCode
     * @return str
     */
    private String getJson(String parentCode, String str) {
        // 把顶层的查出来
        List<Menu> children = menuCache.getChildren(parentCode);
        License lic = License.getInstance();
        for (Menu childlf : children) {
            String desc = childlf.getDescription();
            if (desc!=null && !"".equals(desc)) {
                desc = "(" + desc + ")";
            }

            if ("-1".equals(parentCode)) {
                if (str.indexOf("{id:\"" + childlf.getCode()) == -1) {
                    str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
                            + childlf.getName() + desc + "\",state:{opened:true}} ,";
                }
            } else {
                str += "{id:\"" + childlf.getCode() + "\",parent:\""
                        + childlf.getParentCode() + "\",text:\""
                        + childlf.getName() + desc + "\", isUse:\"" + (childlf.getIsUse() == 1) + "\" },";
            }
            List<Menu> childs = menuCache.getChildren(childlf.getCode());
            // 如果有子节点
            if (!childs.isEmpty()) {
                // 遍历它的子节点
                for (Menu child : childs) {
                    desc = child.getDescription();
                    if (!"".equals(desc)) {
                        desc = "(" + desc + ")";
                    }
                    // System.out.println("getJson: code=" + child.getCode() + " name=" + child.getName() + "  lic.isPlatformSrc()=" + lic.isPlatformSrc());

                    if (child.getCode().equals(ConstUtil.MENU_ITEM_SALES)) {
                        if (!lic.isPlatformSrc()) {
                            continue;
                        }
                        // 平台版才可以用CRM模块，如果许可证中的解决方案中未勾选CRM模块
                        if (lic.isSolutionVer() && !lic.canUseSolution(License.SOLUTION_CRM)) {
                            continue;
                        }
                    }

                    str += "{id:\"" + child.getCode() + "\",parent:\""
                            + child.getParentCode() + "\",text:\""
                            + child.getName() + desc + "\" },";
                    // 还有子节点(递归调用)
                    List<Menu> ch = menuCache.getChildren(child.getCode());
                    if (!ch.isEmpty()) {
                        str = getJson(child.getCode(), str);
                    }
                }
            }
        }
        return str;
    }

    @Override
    public String getJsonString() {
        String str = "[";
        // 从根开始
        str = getJson("-1", str);
        str = str.substring(0, str.length() - 1);
        str += "]";
        return str;
    }

    @Override
    public String getJsonTreeString(String roleCode) {
        List<RolePriv> rolePrivlist = rolePrivService.listByRoleCode(roleCode);
        String[] rolePrivs = new String[rolePrivlist.size()];
        int i = 0;
        for (RolePriv rolePriv : rolePrivlist) {
            rolePrivs[i] = rolePriv.getPriv();
            i++;
        }

        String str = "[";
        // 从根开始
        str = getJsonTree(roleCode, rolePrivs, "-1", str);
        str += "]";
        return str;
    }

    /**
     * 递归获得TreeGrid的json字符串
     *
     * @param parentCode 父节点parentCode
     * @return str
     */
    private String getJsonTree(String roleCode, String[] rolePrivs, String parentCode, String str) {
        // 把顶层的查出来
        int i = 0;
        List<Menu> children = getChildren(parentCode);
        int size = children.size();
        for (Menu childlf : children) {
            if (childlf.getIsUse() == 0) {
                i++;
                continue;
            }

            boolean canSee = false;
            String link = getRealLink(childlf);

            String priv = childlf.getPvg();
            String privName = "";

            String[] ary = StrUtil.split(priv, ",");
            if (ary != null) {
                for (int k = 0; k < ary.length; k++) {
                    if (ary[k].equals("!admin")) {
                        if ("".equals(privName)) {
                            privName = "非管理员";
                        } else {
                            privName += "，" + "非管理员";
                        }
                    } else {
                        Privilege privilege = privilegeService.getByPriv(ary[k]);
                        if (privilege != null) {
                            if ("".equals(privName)) {
                                privName = privilege.getDescription();
                            } else {
                                privName += ", " + privilege.getDescription();
                            }
                        }
                    }
                }
            }

            canSee = menuPermission.canRoleSee(roleCode, childlf, rolePrivs);

            String moduleCode = "", moduleName = "", formCode = "";
            String aliasCode = "", aliasName = "";
            if (childlf.getType() == ConstUtil.MENU_TYPE_MODULE) {
                ModuleSetupDb msd = new ModuleSetupDb();
                moduleCode = childlf.getFormCode();
                msd = msd.getModuleSetupDb(moduleCode);
                if (msd != null) {
                    moduleName = msd.getString("name");
                    formCode = msd.getString("form_code");
                    aliasName = moduleName;
                    aliasCode = formCode;
                    String desc = StrUtil.getNullStr(msd.getString("description"));
                    if (!"".equals(desc)) {
                        privName = "(" + desc + ")";
                    }
                }
            } else if (childlf.getType() == ConstUtil.MENU_TYPE_FLOW) {
                com.redmoon.oa.flow.Leaf lfFlow = new com.redmoon.oa.flow.Leaf();
                lfFlow = lfFlow.getLeaf(childlf.getFormCode());
                if (lfFlow != null) {
                    aliasName = lfFlow.getName();
                    aliasCode = childlf.getFormCode();
                }
            } else if (childlf.getType() == ConstUtil.MENU_TYPE_BASICDATA) {
                SelectKindDb skd = new SelectKindDb();
                skd = skd.getSelectKindDb(StrUtil.toInt(childlf.getFormCode(), -1));
                if (skd.isLoaded()) {
                    aliasName = skd.getName();
                    aliasCode = childlf.getFormCode();
                }
            }

            if ("-1".equals(parentCode)) {
                if (str.indexOf("{id:\"" + childlf.getCode()) == -1) {
                    str += "{id:\"" + childlf.getCode() + "\", link:\"" + link + "\", aliasCode:\"" + aliasCode + "\", aliasName:\"" + aliasName + "\", type:\"" + childlf.getType() + "\", canSee:true, parent:\"#\",name:\""
                            + childlf.getName() + "\", formCode:\"" + formCode + "\", moduleCode:\"" + moduleCode + "\", moduleName:\"" + moduleName + "\", priv:\"" + priv + "\", privName:\"" + privName + "\", state:{opened:true}";
                }
            } else {
                str += "{id:\"" + childlf.getCode() + "\", link:\"" + link + "\", aliasCode:\"" + aliasCode + "\", aliasName:\"" + aliasName + "\", type:\"" + childlf.getType() + "\", canSee:" + canSee + ", parent:\""
                        + childlf.getParentCode() + "\",name:\""
                        + childlf.getName() + "\", formCode:\"" + formCode + "\", moduleCode:\"" + moduleCode + "\", moduleName:\"" + moduleName + "\", priv:\"" + priv + "\", privName:\"" + privName + "\", isUse:\"" + (childlf.getIsUse() == 1) + "\"";
            }

            List<Menu> childs = getChildren(childlf.getCode());
            // 如果有子节点
            if (!childs.isEmpty()) {
                str += ", children:[";
                int size2 = childs.size() - 1;
                // 遍历它的子节点
                int j = 0;
                for (Menu child : childs) {
                    if (child.getIsUse() == 0) {
                        j++;
                        continue;
                    }

                    link = getRealLink(child);

                    priv = child.getPvg();
                    privName = "";
                    ary = StrUtil.split(priv, ",");
                    if (ary != null) {
                        for (int k = 0; k < ary.length; k++) {
                            if (ary[k].equals("!admin")) {
                                if ("".equals(privName)) {
                                    privName = "非管理员";
                                } else {
                                    privName += "，" + "非管理员";
                                }
                            } else {
                                Privilege privilege = privilegeService.getByPriv(ary[k]);
                                if (privilege != null) {
                                    if ("".equals(privName)) {
                                        privName = privilege.getDescription();
                                    } else {
                                        privName += ", " + privilege.getDescription();
                                    }
                                }
                            }
                        }
                    }

                    if (child.getType() == ConstUtil.MENU_TYPE_MODULE) {
                        ModuleSetupDb msd = new ModuleSetupDb();
                        moduleCode = child.getFormCode();
                        msd = msd.getModuleSetupDb(moduleCode);
                        if (msd != null) {
                            moduleName = msd.getString("name");
                            formCode = msd.getString("form_code");
                            aliasName = moduleName;
                            aliasCode = formCode;
                        }
                    } else if (child.getType() == ConstUtil.MENU_TYPE_FLOW) {
                        com.redmoon.oa.flow.Leaf lfFlow = new com.redmoon.oa.flow.Leaf();
                        lfFlow = lfFlow.getLeaf(child.getFormCode());
                        if (lfFlow != null) {
                            aliasName = lfFlow.getName();
                            aliasCode = child.getFormCode();
                        }
                    } else if (child.getType() == ConstUtil.MENU_TYPE_BASICDATA) {
                        SelectKindDb skd = new SelectKindDb();
                        skd = skd.getSelectKindDb(StrUtil.toInt(child.getFormCode(), -1));
                        if (skd.isLoaded()) {
                            aliasName = skd.getName();
                            aliasCode = child.getFormCode();
                        }
                    }

                    canSee = menuPermission.canRoleSee(roleCode, child, rolePrivs);

                    str += "{id:\"" + child.getCode() + "\", link:\"" + link + "\", aliasCode:\"" + aliasCode + "\", aliasName:\"" + aliasName + "\", type:\"" + child.getType() + "\", canSee:" + canSee + ", parent:\""
                            + child.getParentCode() + "\", formCode:\"" + formCode + "\", moduleCode:\"" + moduleCode + "\", moduleName:\"" + moduleName + "\", priv:\"" + priv + "\", privName:\"" + privName + "\", name:\""
                            + child.getName() + "\"";

                    // 还有子节点(递归调用)
                    List<Menu> ch = getChildren(child.getCode());
                    if (!ch.isEmpty()) {
                        str += ", children:[";
                        str = getJsonTree(roleCode, rolePrivs, child.getCode(), str);
                        str += "]";
                    }
                    str += "}";
                    if (j != size2) {
                        str += ",";
                    }
                    j++;
                }
                str += "]";
            }
            str += "}";
            if (i != size - 1) {
                str += ",";
            }
            i++;
        }
        return str;
    }

    @Override
    public boolean updateByCode(Menu menu) {
        QueryWrapper<Menu> qw = new QueryWrapper<>();
        qw.eq("code", menu.getCode());

        boolean re = menu.update(qw);
        if (re) {
            menuCache.removeAllFromCache();
        }
        return re;
    }

    /**
     * 修复因为BUG或者误操作致树形结构被破坏的问题
     */
    public void repairLeaf(Menu menu) {
        List<Menu> children = getChildren(menu.getCode());
        menu.setChildCount(children.size());
        // 重置孩子节点数
        menu.setChildCount(children.size());
        int orders = 1;
        for (Menu childMenu : children) {
            // 重置孩子节点的排列顺序
            childMenu.setOrders(orders);
            updateByCode(childMenu);
            orders++;
        }
        // 重置层数
        int layer = 2;
        String parentCode = menu.getParentCode();
        if (menu.getCode().equals(ConstUtil.MENU_ROOT)) {
            layer = 1;
        } else {
            if (parentCode.equals(ConstUtil.MENU_ROOT)) {
                layer = 2;
            } else {
                while (!parentCode.equals(ConstUtil.MENU_ROOT)) {
                    Menu parentLeaf = getMenu(parentCode);
                    if (parentLeaf == null) {
                        break;
                    } else {
                        parentCode = parentLeaf.getParentCode();
                    }
                    layer++;
                }
            }
        }
        menu.setLayer(layer);
        updateByCode(menu);
    }

    @Override
    public void repairTree(Menu menu) {
        // System.out.println(getClass() + "leaf name=" + leaf.getName());
        repairLeaf(menu);
        List<Menu> children = getChildren(menu.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        for (Menu childMenu : children) {
            repairTree(childMenu);
        }

        // 刷新缓存
        menuCache.removeAllFromCache();
    }

    public void ShowLeafAsOptionToString(StringBuffer sb, Menu leaf, int rootlayer) {
        String code = leaf.getCode();
        String name = leaf.getName();
        int layer = leaf.getLayer();
        String blank = "";
        int d = layer - rootlayer;
        for (int i = 0; i < d; i++) {
            blank += "　";
        }
        if (leaf.getChildCount() > 0) {
            sb.append("<option value='" + code + "' style='COLOR: #0005ff'>" + blank + "╋ " + name + "</option>");
        } else {
            sb.append("<option value=\"" + code + "\" style='COLOR: #0005ff'>" + blank + "├『" + name +
                    "』</option>");
        }
    }

    // 显示根结点为leaf的树
    @Override
    public void ShowDirectoryAsOptionsToString(StringBuffer sb, Menu leaf, int rootlayer) {
        ShowLeafAsOptionToString(sb, leaf, rootlayer);
        List<Menu> children = getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        for (Menu child : children) {
            ShowDirectoryAsOptionsToString(sb, child, rootlayer);
        }
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean create(Menu menu) {
        Menu parentMenu = getMenu(menu.getParentCode());
        int orders = parentMenu.getChildCount() + 1;
        menu.setOrders(orders);
        boolean re = menu.insert();
        if (re) {
            parentMenu.setChildCount(parentMenu.getChildCount() + 1);
            updateByCode(parentMenu);

            menuCache.removeAllFromCache();
        }
        return re;
    }

    public boolean delSingle(Menu menu) {
        QueryWrapper<Menu> qw = new QueryWrapper<>();
        qw.eq("code", menu.getCode());
        menu.delete(qw);

        menuMapper.updateOrders(menu.getParentCode(), menu.getOrders());
        menuMapper.updateChildCount(menu.getParentCode());

        menuCache.removeAllFromCache();

        return true;
    }

    @Override
    public boolean del(Menu menu) {
        delSingle(menu);

        List<Menu> list = getChildren(menu.getCode());
        for (Menu child : list) {
            del(child);
        }

        menuCache.removeAllFromCache();

        return true;
    }

    public String getPresetLink(Menu menu) {
        if (menu.getPreCode().equals("chat")) {
            return "";
        } else {
            return "";
        }
    }

    @Override
    public String getName(Menu menu) {
        if (menu.getName().startsWith("#")) {
            return LocalUtil.LoadString(SpringUtil.getRequest(), "res.ui.menu", menu.getCode());
        } else {
            return menu.getName();
        }
    }

    public boolean move(String code, String parentCode, int position) throws ErrMsgException {
        if (ConstUtil.MENU_ROOT.equals(code)) {
            throw new ErrMsgException("根节点不能移动！");
        }
        if ("#".equals(parentCode)) {
            throw new ErrMsgException("不能与根节点平级！");
        }

        Menu moveleaf = getMenu(code);
        int oldPosition = moveleaf.getOrders(); // 得到被移动节点原来的位置
        String oldParentCode = moveleaf.getParentCode();
        Menu newParentLeaf = getMenu(parentCode);

        moveleaf.setParentCode(parentCode);
        int p = position + 1;
        moveleaf.setOrders(p);
        updateByCode(moveleaf);

        boolean isSameParent = oldParentCode.equals(parentCode);

        // 重新梳理orders
        List<Menu> children = getChildren(parentCode);
        for (Menu lf : children) {
            // 跳过自己
            if (lf.getCode().equals(code)) {
                continue;
            }
            // 如果移动后父节点变了
            if (!isSameParent) {
                if (lf.getOrders() >= p) {
                    lf.setOrders(lf.getOrders() + 1);
                    updateByCode(lf);
                }
            }
            else {
                if (p < oldPosition) {//上移
                    if (lf.getOrders() >= p) {
                        lf.setOrders(lf.getOrders() + 1);
                        updateByCode(lf);
                    }
                } else {//下移
                    if (lf.getOrders() <= p && lf.getOrders() > oldPosition) {
                        lf.setOrders(lf.getOrders() - 1);
                        updateByCode(lf);
                    }
                }
            }
        }

        // 原节点下的孩子节点通过修复repairTree处理
        Menu rootLeaf = getMenu(ConstUtil.MENU_ROOT);
        repairTree(rootLeaf);
        return true;
    }

}
