package com.cloudweb.oa.service.impl;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.cache.DepartmentCache;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.mapper.DepartmentMapper;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.mysql.cj.QueryBindings;
import com.redmoon.dingding.service.department.DepartmentService;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sso.SyncUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.mgr.WXDeptMgr;
import org.apache.jcs.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-01-30
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {
    @Autowired
    HttpServletRequest request;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private UserServiceImpl usersService;

    @Autowired
    private DepartmentCache departmentCache;

    private String parentNodeName = "";

    @Autowired
    private IDeptUserService deptUserService;

    private void generateParentNodeName(String code) {
        Department dept = getDepartment(code);
        if (dept == null) {
            return;
        }
        String nodeName = dept.getName();
        this.parentNodeName = nodeName + "\\" + this.parentNodeName;
        String parentNodeCode = dept.getParentCode();
        if ("-1".equals(parentNodeCode)) {
            return;
        } else {
            generateParentNodeName(parentNodeCode);
        }
    }

    @Override
    public String generateNewNodeCode(String parentCode) {
        String newNodeCode = "";

        String maxCode = departmentMapper.getMaxCode(parentCode);

        int num = 0;
        if (ConstUtil.DEPT_ROOT.equals(parentCode)) {
            num = StrUtil.toInt(maxCode) + 1;
        } else {
            if ("".equals(maxCode) || maxCode == null) {
                maxCode = "0";
            } else {
                maxCode = maxCode.substring(parentCode.length());
            }
            num = StrUtil.toInt(maxCode) + 1;
        }

        Department dept = null;
        do {
            if (ConstUtil.DEPT_ROOT.equals(parentCode)) {
                newNodeCode = StrUtil.PadString(String.valueOf(num), '0', 4, true);
            } else {
                newNodeCode = parentCode + StrUtil.PadString(String.valueOf(num), '0', 4, true);
            }
            num++;
            dept = getDepartment(newNodeCode);
        } while (dept != null);
        return newNodeCode;
    }

    @Override
    public JSONObject getAddDepartmentData(String parentCode) {
        String newNodeCode = generateNewNodeCode(parentCode);
        parentNodeName = "";
        generateParentNodeName(parentCode);

        JSONObject json = new JSONObject();
        json.put("newNodeCode", newNodeCode);
        json.put("parentNodeName", parentNodeName);

        return json;
    }

    @Override
    public Department getDepartment(String code) {
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("code", code);
        return departmentMapper.selectOne(qw);
    }

    @Override
    public Department getDepartmentByName(String name) {
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("name", name);
        return departmentMapper.selectOne(qw);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean create(Department department) throws ValidateException {
        //判断名称是否有重复
        List<Department> list = departmentMapper.getChildren(department.getParentCode());
        for (Department dept : list) {
            if (department.getName().equals(dept.getName())) {
                throw new ValidateException("#dept.name.multi");
            }
        }

        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("code", department.getParentCode());
        Department deptParent = departmentMapper.selectOne(qw);

        department.setLayer(deptParent.getLayer() + 1);
        department.setOrders(list.size() + 1);
        if (department.getId() == null) {
            department.setId((int) SequenceManager.nextID(SequenceManager.OA_DEPT));
        }

        departmentMapper.insert(department);

        deptParent.setChildCount(deptParent.getChildCount() + 1);
        return updateByCode(deptParent);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean createAnySyn(Department department) throws ValidateException {
        boolean re = create(department);
        Department deptParent = getDepartment(department.getParentCode());

        // 同步钉钉部门
        com.redmoon.dingding.Config dingDingCfg = com.redmoon.dingding.Config.getInstance();
        if (dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
            DepartmentService departmentService = new DepartmentService();
            int parentId = deptParent.getId();
            if (deptParent.getCode().equals(ConstUtil.DEPT_ROOT)) {
                parentId = com.redmoon.dingding.enums.Enum.ROOT_DEPT_ID;
            }
            int id = departmentService.addDept(department.getName(), parentId, deptParent.getOrders());
            if (id != -1) {
                department.setId(id);
            }
        }

        if (re) {
            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
            if (ssoCfg.getBooleanProperty("isUse")) {
                SyncUtil su = new SyncUtil();
                su.orgSync(department, SyncUtil.CREATE, new Privilege().getUser(request));
            }

            com.redmoon.weixin.Config weixinCfg = Config.getInstance();
            if (weixinCfg.getBooleanProperty("isUse")) {
                if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
                    WXDeptMgr _wxDpetMgr = new WXDeptMgr();
                    _wxDpetMgr.createWxDept(department);
                }
            }
        }

        return re;
    }

    @Override
    public boolean updateByCode(Department department) {
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("code", department.getCode());
        boolean re = departmentMapper.update(department, qw) == 1;
        if (re) {
            departmentCache.refreshSave(department.getCode());
            departmentCache.refreshChildren(department.getParentCode());
        }
        return re;
    }

    @Override
    public boolean update(Department department) throws ValidateException {
        // 判断名称是否有重复
        List<Department> list = departmentMapper.getChildren(department.getParentCode());
        for (Department dept : list) {
            if (dept.getCode().equals(department.getCode())) {
                continue;
            }
            if (department.getName().equals(dept.getName())) {
                throw new ValidateException("#dept.name.multi");
            }
        }

        Department oldDept = getDepartment(department.getCode());

        boolean re = updateByCode(department);
        if (re) {
            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
            if (ssoCfg.getBooleanProperty("isUse")) {
                SyncUtil su = new SyncUtil();
                su.orgSync(department, SyncUtil.MODIFY, new Privilege().getUser(request));
            }

            com.redmoon.weixin.Config weixinCfg = Config.getInstance();
            if (weixinCfg.getBooleanProperty("isUse")) {
                if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
                    WXDeptMgr wxDpetMgr = new WXDeptMgr();
                    wxDpetMgr.updateWxDept(department);
                }
            }
            com.redmoon.dingding.Config dingDingCfg = com.redmoon.dingding.Config.getInstance();
            if (dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
                DepartmentService departmentService = new DepartmentService();
                departmentService.updateDept(department.getCode());
            }

            // 如果类型有变化，则更新属于其及子部门的用户的unitCode
            if (oldDept.getDeptType().intValue() != department.getDeptType().intValue()) {
                List<Department> all = new ArrayList<>();
                getAllChild(all, department.getCode());

                list.add(department);

                for (Department dept : list) {
                    String unitCode = dept.getDeptType() == ConstUtil.TYPE_UNIT ? dept.getCode() : getUnitOfDept(dept).getCode();
                    usersService.updateUserUnitCode(dept.getCode(), unitCode);
                }

                // 清缓存
                RMCache rmcache = RMCache.getInstance();
                try {
                    rmcache.clear();
                } catch (CacheException e) {
                    e.printStackTrace();
                }
            }

        }
        return re;
    }

    @Override
    public List<Department> getChildren(String code) {
        return departmentMapper.getChildren(code);
    }

    public String getFullNameOfDept(Department department) {
        String name = department.getName();
        while (!department.getParentCode().equals("-1")
                && !department.getParentCode().equals(ConstUtil.DEPT_ROOT)) {
            department = getDepartment(department.getParentCode());
            if (department != null && !department.getParentCode().equals("")) {
                name = department.getName() + "-" + name;
            } else {
                return "";
            }
        }
        return name;
    }

    @Override
    public Map<String, String> getFulleNameMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.orderByAsc("code");
        List<Department> list = list(qw);
        for (Department dept : list) {
            map.put(getFullNameOfDept(dept), dept.getCode());
        }
        return map;
    }

    /**
     * 取出code结点的所有孩子结点
     *
     * @param list
     * @param code
     * @return
     */
    @Override
    public List getAllChild(List list, String code) {
        List<Department> children = getChildren(code);
        if (children.isEmpty()) {
            return children;
        }
        list.addAll(children);
        for (Department dept : children) {
            getAllChild(list, dept.getCode());
        }

        return list;
    }

    /**
     * 取得部门所属的单位
     *
     * @param department
     * @return
     */
    @Override
    public Department getUnitOfDept(Department department) {
        if (department.getDeptType() == ConstUtil.TYPE_UNIT) {
            return department;
        }

        String parentCode = department.getParentCode();

        while (!parentCode.equals(ConstUtil.DEPT_ROOT) && !"-1".equals(parentCode)) {
            Department parentDept = getDepartment(parentCode);
            if (parentDept == null) {
                return getDepartment(ConstUtil.DEPT_ROOT);
            }
            if (parentDept.getDeptType() == ConstUtil.TYPE_UNIT) {
                return parentDept;
            }

            parentCode = parentDept.getParentCode();
        }

        return getDepartment(ConstUtil.DEPT_ROOT);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean delWithChildren(String code) throws ValidateException {
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("code", code);
        Department department = departmentMapper.selectOne(qw);

        if (department == null) {
            log.warn("部门:" + code + " 不存在");
            return false;
        }

        if (department.getChildCount() > 0) {
            throw new ValidateException("请删除部门下的子部门后，再删除本部门！");
        }

        List<DeptUser> duList = deptUserService.listByDeptCode(code);

        if (duList.size() > 0) {
            throw new ValidateException("部门：" + department.getName() + " 下有" + duList.size() + "名人员，请先将人员安排至其它部门再删除！");
        }
        // 删除孩子节点
        List<Department> children = getChildren(code);
        for (Department dept : children) {
            delWithChildren(dept.getCode());
        }

        boolean re = departmentMapper.delete(qw) == 1;
        if (re) {
            departmentMapper.updateOrdersGreatThan(department.getParentCode(), department.getOrders());

            Department parentDept = getDepartment(department.getParentCode());
            parentDept.setChildCount(parentDept.getChildCount() - 1);
            updateByCode(parentDept);

            departmentCache.removeAllFromCache();
        }
        return re;
    }

    @Override
    public boolean del(String code, String userName) throws ValidateException {
        Department department = getDepartment(code);
        boolean re = delWithChildren(code);
        if (re) {
            com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
            if (ssoCfg.getBooleanProperty("isUse")) {
                SyncUtil su = new SyncUtil();
                su.orgSync(department, SyncUtil.DEL, userName);
            }

            com.redmoon.weixin.Config weixinCfg = Config.getInstance();
            if (weixinCfg.getBooleanProperty("isUse")) {
                if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
                    WXDeptMgr _wxDpetMgr = new WXDeptMgr();
                    _wxDpetMgr.deleteWxDept(department.getId());
                }
            }
            com.redmoon.dingding.Config dingDingCfg = com.redmoon.dingding.Config.getInstance();
            if (dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
                DepartmentService _departmentService = new DepartmentService();
                _departmentService.delDept(department.getId());
            }
        }
        return re;
    }

    @Override
    public void move(String code, String parentCode, int position) throws ValidateException {
        Department department = getDepartment(code);
        Department parentDept = getDepartment(department.getParentCode());

        // 得到被移动节点原来的位置
        int oldPosition = department.getOrders();
        String oldParentCode = department.getParentCode();

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        if (cfg.getBooleanProperty("isLarkUsed")) {
            if (!parentDept.getCode().equals(oldParentCode)) {
                throw new ValidateException("因启用了lark精灵，所以只能在同级部门之间移动！");
            }
        }

        department.setParentCode(parentCode);
        int newPosition = position + 1; // 新位置为position + 1
        department.setOrders(newPosition);
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("code", code);
        departmentMapper.update(department, qw);

        // 同步至精灵
        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        SyncUtil su = new SyncUtil();
        if (ssoCfg.getBooleanProperty("isUse")) {
            su.orgSync(department, SyncUtil.MODIFY, new Privilege().getUser(request));
        }

        // 同步至微信
        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        if (weixinCfg.getBooleanProperty("isUse")) {
            if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
                WXDeptMgr _wxDpetMgr = new WXDeptMgr();
                _wxDpetMgr.updateWxDept(department);
            }
        }
        // 同步至钉钉
        com.redmoon.dingding.Config dingDingCfg = com.redmoon.dingding.Config.getInstance();
        if (dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
            DepartmentService departmentService = new DepartmentService();
            departmentService.updateDept(code);
        }

        // 新节点
        List<Department> list = getChildren(parentCode);
        for (Department dept : list) {
            qw = new QueryWrapper<>();
            qw.eq("code", dept.getCode());

            if (dept.getCode().equals(code)) {
                continue;
            }
            //上移
            if (newPosition < oldPosition) {
                if (dept.getOrders() >= newPosition) {
                    dept.setOrders(dept.getOrders() + 1);
                    dept.update(qw);
                }
            } else {
                //下移
                if (dept.getOrders() <= newPosition && dept.getOrders() > oldPosition) {
                    dept.setOrders(dept.getOrders() - 1);
                    dept.update(qw);
                }
            }

            // 同步至精灵
            if (ssoCfg.getBooleanProperty("isUse")) {
                su.orgSync(department, SyncUtil.MODIFY, new Privilege().getUser(request));
            }

            if (weixinCfg.getBooleanProperty("isUse")) {
                if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
                    WXDeptMgr wxDpetMgr = new WXDeptMgr();
                    wxDpetMgr.updateWxDept(department);
                }
            }
            if (dingDingCfg.isUseDingDing() && !dingDingCfg.getBooleanProperty("isSyncDingDingToOA")) {
                DepartmentService _departmentService = new DepartmentService();
                _departmentService.updateDept(dept.getCode());
            }
        }

        // 原节点下的孩子节点通过修复repairTree处理
        Department rootDept = getDepartment(ConstUtil.DEPT_ROOT);
        repairTree(rootDept);

        departmentCache.removeAllFromCache();
    }

    /**
     * 修复因为BUG或者误操作致树形结构被破坏的问题
     */
    public void repairLeaf(Department department) {

        List<Department> children = getChildren(department.getCode());
        // 重置孩子节点数
        department.setChildCount(children.size());
        Iterator<Department> ir = children.iterator();
        int orders = 1;
        while (ir.hasNext()) {
            Department dept = ir.next();
            QueryWrapper<Department> qw = new QueryWrapper<>();
            qw.eq("code", dept.getCode());

            // 重置孩子节点的排列顺序
            dept.setOrders(orders);
            dept.update(qw);
            orders++;
        }
        // 重置层数
        int layer = 2;
        String parentCode = department.getParentCode();
        if (department.getCode().equals(ConstUtil.DEPT_ROOT)) {
            layer = 1;
        } else {
            if (parentCode.equals(ConstUtil.DEPT_ROOT)) {
                layer = 2;
            } else {
                while (!parentCode.equals(ConstUtil.DEPT_ROOT)) {
                    Department parentDept = getDepartment(parentCode);
                    if (parentDept == null) {
                        break;
                    } else {
                        parentCode = parentDept.getParentCode();
                    }
                    layer++;
                }
            }
        }
        department.setLayer(layer);
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("code", department.getCode());
        department.update(qw);
    }

    /**
     * 修复根结点为leaf的树
     *
     * @param department
     */
    public void repairTree(Department department) {
        repairLeaf(department);
        List<Department> children = getChildren(department.getCode());
        int size = children.size();
        if (size == 0) {
            return;
        }

        Iterator<Department> ri = children.iterator();
        while (ri.hasNext()) {
            Department dept = ri.next();
            repairTree(dept);
        }

        // 刷新缓存
        departmentCache.removeAllFromCache();
    }

    /**
     * @param isOpenAll       是否打开js-tree的全部节点，如在用户编辑时选择部门的时候
     * @param isShowNodeHided 是否显示隐藏节点
     * @return
     */
    @Override
    public String getJsonString(boolean isOpenAll, boolean isShowNodeHided) {
        String str = "[";
        // 从根开始
        try {
            str = getJson("-1", str, isShowNodeHided, isOpenAll);
            str = str.substring(0, str.length() - 1);
            str += "]";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    /**
     * 递归获得jsTree的json字符串
     *
     * @param parentCode
     * @param str
     * @param isShowNodeHided
     * @param isOpenAll
     * @return
     */
    private String getJson(String parentCode, String str, boolean isShowNodeHided, boolean isOpenAll) {
        String strStateOpened = "";
        if (isOpenAll) {
            strStateOpened = ", state:{opened:true}";
        }
        // 把顶层的查出来
        List<Department> children = getChildren(parentCode);
        Iterator<Department> ri = children.iterator();
        while (ri.hasNext()) {
            Department childlf = ri.next();
            if (childlf.getIsHide() == 1) {
                if (!isShowNodeHided) {
                    continue;
                }
            }
            if ("-1".equals(parentCode)) {
                if (str.indexOf("{id:\"" + childlf.getCode()) == -1) {
                    str += "{id:\"" + childlf.getCode() + "\",parent:\"#\",text:\""
                            + childlf.getName() + "\",state:{opened:true}} ,";
                }
            } else {
                str += "{id:\"" + childlf.getCode() + "\",parent:\""
                        + childlf.getParentCode() + "\",text:\""
                        + childlf.getName() + "\"" + strStateOpened + " },";
            }
            List<Department> childs = getChildren(childlf.getCode());
            // 如果有子节点
            if (!childs.isEmpty()) {
                // 遍历它的子节点
                Iterator<Department> childri = childs.iterator();
                while (childri.hasNext()) {
                    Department child = childri.next();
                    if (child.getIsHide() == 1) {
                        if (!isShowNodeHided) {
                            continue;
                        }
                    }
                    str += "{id:\"" + child.getCode() + "\",parent:\""
                            + child.getParentCode() + "\",text:\""
                            + child.getName() + "\"" + strStateOpened + " },";
                    // 还有子节点(递归调用)
                    List<Department> ch = getChildren(child.getCode());
                    if (!ch.isEmpty()) {
                        str = getJson(child.getCode(), str, isShowNodeHided, isOpenAll);
                    }
                }
            }
        }
        return str;
    }

    @Override
    public List<String> getAllUnit() {
        List<String> list = new ArrayList<String>();
        return getEachUnit("-1", list);
    }

    private List<String> getEachUnit(String parentCode, List<String> list) {
        List<Department> children = getChildren(parentCode);
        Iterator<Department> ri = children.iterator();
        while (ri.hasNext()) {
            Department childlf = ri.next();
            if (childlf.getDeptType() == ConstUtil.TYPE_UNIT) {
                list.add(childlf.getCode());
            }
            List<Department> childs = getChildren(childlf.getCode());
            if (!childs.isEmpty()) {
                Iterator<Department> childri = childs.iterator();
                while (childri.hasNext()) {
                    Department child = childri.next();
                    if (child.getDeptType() == ConstUtil.TYPE_UNIT) {
                        list.add(child.getCode());
                    }
                    List<Department> ch = getChildren(child.getCode());
                    if (!ch.isEmpty()) {
                        getEachUnit(child.getCode(), list);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public List<Department> getDeptsOfUser(String userName) {
        return departmentMapper.getDeptsOfUser(userName);
    }

    /**
     * 获取当前及所有的下级部门code
     *
     * @param code
     * @return
     * @Description:
     */
    @Override
    public List<String> getBranchDeptCode(String code, List<String> list) {
        list.add(code);

        List<Department> childList = getChildren(code);
        for (Department dept : childList) {
            String childCode = dept.getCode();
            getBranchDeptCode(childCode, list);
        }

        return list;
    }

    @Override
    public StringBuffer getUnitAsOptions(StringBuffer outStr, Department department, int rootlayer) {
        // 不是企业版，则返回空
        if (!License.getInstance().isPlatform()) {
            return new StringBuffer();
        }

        outStr.append(getUnitAsOption(department, rootlayer));
        List<Department> children = getChildren(department.getCode());
        int size = children.size();
        if (size == 0) {
            return outStr;
        }

        for (Department childDept : children) {
            getUnitAsOptions(outStr, childDept, rootlayer);
        }
        return outStr;
    }

    public String getUnitAsOption(Department department, int rootlayer) {
        if (department.getIsShow() == 0) {
            return "";
        }

        if (department.getDeptType() != ConstUtil.TYPE_UNIT) {
            return "";
        }

        // 如果其孩子节点数为0，且不是单位，则不显示
        if (department.getChildCount() == 0) {
            if (department.getDeptType() != ConstUtil.TYPE_UNIT) {
                return "";
            }
        }

        String outStr = "";
        String code = department.getCode();
        String name = department.getName();
        int layer = department.getLayer();
        String blank = "";
        int d = layer - rootlayer;
        for (int i = 0; i < d; i++) {
            blank += "　";
        }

        String cls = "", val = "";
        if (department.getDeptType() == ConstUtil.TYPE_UNIT) {
            cls = " class='unit' ";
            val = code;
        } else {
            val = "";
        }

        if (department.getChildCount() > 0) {
            outStr += "<option " + cls + " value='" + val + "'>" + blank + "╋ " + name + "</option>";
        } else {
            outStr += "<option " + cls + " value=\"" + val + "\">" + blank + "├『" + name +
                    "』</option>";
        }
        return outStr;
    }

    @Override
    public List<Department> getDeptsWithouRoot() {
        return departmentMapper.getDeptsWithouRoot();
    }

    @Override
    public List<Department> getDeptsNotHide() {
        QueryWrapper<Department> qw = new QueryWrapper<>();
        qw.eq("is_hide", 0).orderByAsc("orders");
        return departmentMapper.selectList(qw);
    }

    public String getDeptAsOption(Department department, int rootLayer) {
        if (department.getIsShow() == 0) {
            return "";
        }
        String code = department.getCode();
        String name = department.getName();
        int layer = department.getLayer();
        String blank = "";
        int d = layer - rootLayer;
        for (int i = 0; i < d; i++) {
            blank += "　";
        }
        String cls = "";
        if (department.getDeptType() == ConstUtil.TYPE_UNIT) {
            cls = "class='unit_option'";
        }
        if (department.getChildCount() > 0) {
            return "<option " + cls + " value='" + code + "'>" + blank + "╋ " + name + "</option>";
        } else {
            return ("<option " + cls + " value=\"" + code + "\">" + blank + "├『" + name +
                    "』</option>");
        }
    }

    /**
     * 取得根结点为department的树的options
     *
     * @param outStr
     * @param department
     * @param rootLayer
     * @return
     * @throws Exception
     */
    @Override
    public String getDeptAsOptions(StringBuffer outStr, Department department, int rootLayer) {
        outStr.append(getDeptAsOption(department, rootLayer));

        List<Department> children = getChildren(department.getCode());
        int size = children.size();
        if (size == 0) {
            return outStr.toString();
        }

        for (Department dept : children) {
            getDeptAsOptions(outStr, dept, rootLayer);
        }
        return outStr.toString();
    }

    /**
     * 更改父节点
     *
     * @param department
     * @param newParentCode
     * @return
     */
    @Override
    public boolean setNewParent(Department department, String newParentCode) {
        if (newParentCode.equals(department.getParentCode())) {
            return false;
        }
        // 把该结点加至新父结点，作为其最后一个孩子,同设其layer为父结点的layer + 1
        Department newParentDept = getDepartment(newParentCode);
        int oldorders = department.getOrders();
        int neworders = newParentDept.getChildCount() + 1;
        int parentLayer = newParentDept.getLayer();

        String oldParentCode = department.getParentCode();

        department.setParentCode(newParentCode);
        boolean re = updateByCode(department);

        DepartmentCache departmentCache = SpringUtil.getBean(DepartmentCache.class);

        // 更新原来父结点中，位于本leaf之后的orders
        QueryWrapper qw = new QueryWrapper();
        qw.eq("parentCode", oldParentCode);
        qw.gt("orders", oldorders);
        List<Department> list = list(qw);
        for (Department dept : list) {
            dept.setOrders(dept.getOrders() - 1);
            updateByCode(dept);
        }

        List<Department> childList = new ArrayList<>();
        getAllChild(childList, department.getCode());
        for (Department childDept : childList) {
            int layer = parentLayer + 1 + 1;
            String pcode = childDept.getParentCode();
            while (!pcode.equals(department.getCode())) {
                layer++;
                Department lfp = getDepartment(pcode);
                pcode = lfp.getParentCode();
            }

            childDept.setLayer(layer);
            updateByCode(childDept);
        }

        // 将其原来的父结点的孩子数-1
        Department oldParentLeaf = getDepartment(oldParentCode);
        oldParentLeaf.setChildCount(oldParentLeaf.getChildCount() - 1);
        updateByCode(oldParentLeaf);

        // 将其新父结点的孩子数 + 1
        Department newParentLeaf = getDepartment(newParentCode);
        newParentLeaf.setChildCount(newParentLeaf.getChildCount() + 1);
        updateByCode(newParentDept);

        if (re) {
            departmentCache.removeAllFromCache();
        }
        return re;
    }

    public Department getBrother(Department department, String direction) {
        QueryWrapper<Department> qw = new QueryWrapper<>();
        if (direction.equals("down")) {
            qw.eq("parentCode", department.getParentCode())
                    .eq("orders", department.getOrders() + 1);
        } else {
            qw.eq("parentCode", department.getParentCode())
                    .eq("orders", department.getOrders() - 1);
        }
        return getOne(qw, false);
    }

}
