package com.redmoon.oa.dept;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.cache.DepartmentCache;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.LocalUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class DeptDb extends ObjectDb implements Serializable {
    private String code = "", name = "", description = "", parentCode = "-1",
            rootCode = "", addDate = "";
    private int orders = 1, layer = 1, childCount = 0;
    boolean isHome = false;
    private boolean loaded = false;

    private String shortName;

    public static final int TYPE_DEPT = 1;
    public static final int TYPE_UNIT = 0;

    private int type = TYPE_DEPT;

    /**
     * 是否为班组
     */
    private boolean group = false;

    /**
     * 是否隐藏
     */
    private boolean hide = false;

    public static final String ROOTCODE = "root";

    public DeptDb() {
        isInitFromConfigDB = false;
        init();
    }

    public DeptDb(String code) {
        this.code = code;
        isInitFromConfigDB = false;
        init();
        load();
    }

    public DeptDb(int id) {
        this.id = id;
        isInitFromConfigDB = false;
        init();
        loadById();
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return null;
    }

    public DeptDb getFromDepartment(Department dept, DeptDb dd) {
        if (dept == null) {
            return dd;
        }
        dd.setCode(dept.getCode());
        dd.setName(dept.getName());
        dd.setDescription(dept.getDescription());
        dd.setParentCode(dept.getParentCode());
        dd.setRootCode(dept.getRootCode());
        // DebugUtil.i(getClass(), "getFromDepartment", dd.getCode() + "--" + dd.getName());
        dd.setOrders(dept.getOrders());
        dd.setLayer(dept.getLayer());
        dd.setChildCount(dept.getChildCount());
        dd.setAddDate(DateUtil.format(dept.getAddDate(), "yyyy-MM-dd"));
        dd.setType(dept.getDeptType());
        dd.setId(dept.getId());
        dd.setShow(dept.getIsShow() == 1);
        dd.setShortName(dept.getShortName());
        dd.setGroup(dept.getIsGroup() == 1);
        dd.setHide(dept.getIsHide() == 1);
        dd.setLoaded(true);
        return dd;
    }

    @Override
    public ListResult listResult(String listsql, int curPage, int pageSize) throws ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();

        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
            }

            // 防止受到攻击时，curPage被置为很大，或者很小
            int totalpages = (int) Math.ceil((double) total / pageSize);
            if (curPage > totalpages) {
                curPage = totalpages;
            }
            if (curPage <= 0) {
                curPage = 1;
            }

            conn.prepareStatement(listsql);

            if (total != 0) {
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用
            }

            // rs = conn.executeQuery(listsql); // MySQL中效率很低，70万行的数据，原本30毫秒的数据，需要2秒多才能查出
            rs = conn.executePreQuery();

            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    result.addElement(getDeptDb(rs.getString(1)));
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listResult:" + e.getMessage());
            throw new ErrMsgException("Db error.");
        } finally {
            conn.close();
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    @Override
    public void load() {
        com.cloudweb.oa.cache.DepartmentCache departmentCache = SpringUtil.getBean(com.cloudweb.oa.cache.DepartmentCache.class);
        Department department = departmentCache.getDepartment(code);
        if (department != null) {
            getFromDepartment(department, this);
        }
    }

    public void loadById() {
        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
        Department department = departmentService.getById(id);
        if (department != null) {
            getFromDepartment(department, this);
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setRootCode(String c) {
        this.rootCode = c;
    }

    public void setType(int t) {
        this.type = t;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public int getOrders() {
        return orders;
    }

    public boolean getIsHome() {
        return isHome;
    }

    public void setParentCode(String p) {
        this.parentCode = p;
    }

    public String getParentCode() {
        return this.parentCode;
    }

    public void setIsHome(boolean b) {
        this.isHome = b;
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getRootCode() {
        return rootCode;
    }

    public int getLayer() {
        return layer;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public int getChildCount() {
        return childCount;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    public int getId() {
        return id;
    }

    public Vector<DeptDb> getChildren() {
        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
        List<Department> list = departmentService.getChildren(code);
        Vector<DeptDb> v = new Vector<>();
        for (Department department : list) {
            v.addElement(getFromDepartment(department, new DeptDb()));
        }
        return v;
    }

    /**
     * 取出code结点的所有孩子结点
     *
     * @return ResultIterator
     * @throws ErrMsgException
     */
    public Vector<DeptDb> getAllChild(Vector<DeptDb> vt, DeptDb dd) throws ErrMsgException {
        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
        List<Department> list = new ArrayList<>();
        departmentService.getAllChild(list, dd.getCode());
        for (Department department : list) {
            vt.addElement(getFromDepartment(department, new DeptDb()));
        }
        return vt;
    }

    @Override
    public String toString() {
        return "Dept is " + code;
    }

    @Override
    public synchronized boolean save() {
        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setParentCode(parentCode);
        department.setDeptType(type);
        department.setOrders(orders);
        department.setChildCount(childCount);
        department.setLayer(layer);
        department.setIsShow(show ? 1 : 0);
        department.setShortName(shortName);
        department.setIsGroup(group ? 1 : 0);
        department.setIsHide(hide ? 1 : 0);
        department.setId(id);
        department.setCode(code);
        return departmentService.updateByCode(department);
    }

    @Override
    public boolean del() throws ErrMsgException, ResKeyException {
        return false;
    }

    public DeptDb getDeptDb(String code) {
        // 注意不能赋值给code，否则会替代原来的code，造成混乱
        // this.code = code;
        com.cloudweb.oa.cache.DepartmentCache departmentCache = SpringUtil.getBean(com.cloudweb.oa.cache.DepartmentCache.class);
        Department user = departmentCache.getDepartment(code);
        return getFromDepartment(user, new DeptDb());
    }

    public DeptDb getBrother(String direction) {
        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
        Department department = departmentService.getDepartment(code);
        department = departmentService.getBrother(department, direction);
        if (department != null) {
            return getFromDepartment(department, new DeptDb());
        } else {
            return null;
        }
    }

    /**
     * 取得部门所在的单位
     *
     * @param dd
     * @return
     */
    public DeptDb getUnitOfDept(DeptDb dd) {
        if (dd.getType() == TYPE_UNIT) {
            return dd;
        }

        String parentCode = dd.getParentCode();
        while (!parentCode.equals(ROOTCODE) && !parentCode.equals("-1")) {
            DeptDb deptDb = getDeptDb(parentCode);
            if (deptDb == null || !deptDb.isLoaded()) {
                return getDeptDb(ROOTCODE);
            }
            if (deptDb.getType() == DeptDb.TYPE_UNIT) {
                return deptDb;
            }
            parentCode = deptDb.getParentCode();
        }

        return getDeptDb(ROOTCODE);
    }

    /**
     * 中英文切换
     *
     * @param request
     * @return
     */
    public String getName(HttpServletRequest request) {
        if (name.startsWith("#")) {
            return LocalUtil.LoadString(request, "res.ui.department", code);
        } else {
            return name;
        }
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public boolean isShow() {
        return show;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public String getAddDate() {
        return addDate;
    }

    private boolean show = true;
    private String bcode;

    /**
     * @return the bcode
     */
    public String getBcode() {
        return bcode;
    }

    /**
     * @param bcode the bcode to set
     */
    public void setBcode(String bcode) {
        this.bcode = bcode;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public boolean isGroup() {
        return group;
    }

    /**
     * @param hide the hide to set
     */
    public void setHide(boolean hide) {
        this.hide = hide;
    }

    /**
     * @return the hide
     */
    public boolean isHide() {
        return hide;
    }

    private int id = -1;
}
