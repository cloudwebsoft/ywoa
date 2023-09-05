package com.redmoon.oa.dept;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.cache.DeptUserCache;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.entity.DeptUser;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.service.IDeptUserService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DeptUserDb extends ObjectDb implements Serializable {
    private String deptCode = "", userName = "";

    public DeptUserDb() {
        init();
    }

    public DeptUserDb(int id) {
        this.id = id;
        load();
        init();
    }
    
    public DeptUserDb(String userName) {
        this.userName = userName;
        loadOfName();
        init();
    }

    @Override
    public void initDB() {
        tableName = "dept_user";
        isInitFromConfigDB = false;
    }

    @Override
    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return null;
    }

    @Override
    public void load() {
        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        DeptUser deptUser = deptUserService.getById(id);
        getFromDeptUser(deptUser, this);
    }

    public DeptUserDb getFromDeptUser(DeptUser deptUser, DeptUserDb dud) {
        if (deptUser == null) {
            return dud;
        }

        dud.setDeptCode(deptUser.getDeptCode());
        dud.setUserName(deptUser.getUserName());
        dud.setOrders(deptUser.getOrders());
        dud.setLoaded(true);
        return dud;
    }
    
    public void loadOfName() {
        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        DeptUser deptUser = deptUserService.getPrimary(userName);
        getFromDeptUser(deptUser, new DeptUserDb());
    }

    /**
     * 用于兼容 5.0版
     * @param deptCode
     * @param userName
     * @param rank
     * @return
     * @throws ErrMsgException
     */
    public boolean create(String deptCode, String userName, String rank) throws ErrMsgException {
        return insert(deptCode, userName);
    }
    
    /**
     * 用于导入 6.0版
     * @param deptCode
     * @param userName
     * @return
     * @throws ErrMsgException
     */
    public boolean insert(String deptCode, String userName) throws ErrMsgException {
        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        return deptUserService.create(userName, deptCode);
    }    

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getUserName() {
        return userName;
    }

    public int getId() {
        return id;
    }

    public int getOrders() {
        return orders;
    }

    public String getRank() {
        return rank;
    }

    public void setUserName(String n) {
        this.userName = n;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    private int id;

    @Override
    public boolean save() {
        return false;
    }

    @Override
    public boolean del() {
        return false;
    }

    /**
     * 用户是否属于某部门
     * @param userName String 用户名
     * @param deptCode String 部门编码
     * @return boolean
     */
    public boolean isUserOfDept(String userName, String deptCode) {
        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        return deptUserService.isUserOfDept(userName, deptCode);
    }

    public DeptUserDb getDeptUserDb(int id) {
        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        DeptUser deptUser = deptUserService.getById(id);
        return getFromDeptUser(deptUser, new DeptUserDb());
    }

    public String getDeptName() {
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(deptCode);
        return dd.getName();
    }
    
    /**
     * 取得用户所在的单位，用户如果属于多个部门，则取其所在第一个部门所在的单位
     * @param userName
     * @return
     */
    public DeptDb getUnitOfUser(String userName) {
    	Iterator ir = getDeptsOfUser(userName).iterator();
    	if (ir.hasNext()) {
    		DeptDb dd = (DeptDb)ir.next();
    		return dd.getUnitOfDept(dd);
    	}
    	
    	DeptDb dd = new DeptDb();
    	return dd.getDeptDb(DeptDb.ROOTCODE);
    }
    
    /**
     * 取得用户所在的全部单位
     * @param userName
     * @return
     */
    public String[] getUnitsOfUser(String userName) {
    	if (userName == null) {
    		return null;
    	}
    	Iterator ir = getDeptsOfUser(userName).iterator();
    	Map map = new HashMap();
    	UserDb user = new UserDb();
    	user = user.getUserDb(userName);
    	map.put(user.getUnitCode(), "");
    	while (ir.hasNext()) {
    		DeptDb dd = (DeptDb)ir.next();
        	String unitCode = dd.getUnitOfDept(dd).getCode();
        	map.put(unitCode, "");
    	}

    	Set set = map.keySet();
    	String[] ary = new String[set.size()];
    	ir = set.iterator();
    	int i = 0;
    	while (ir.hasNext()) {
    		String key = (String)ir.next();
    		ary[i] = key;
    		i++;
    	}
    	return ary;
    }

    /**
     * 取得用户所在的部门
     * @param userName String
     * @return Vector
     */
    public Vector<DeptDb> getDeptsOfUser(String userName) {
        /*IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        List<DeptUser> list = deptUserService.listByUserName(userName);*/
        DeptUserCache deptUserCache = SpringUtil.getBean(DeptUserCache.class);
        List<DeptUser> list = deptUserCache.listByUserName(userName);
        Vector<DeptDb> v = new Vector<>();
        DeptDb dd = new DeptDb();
        for (DeptUser deptUser : list) {
            v.addElement(dd.getDeptDb(deptUser.getDeptCode()));
        }
        return v;
    }
    
    public Vector listBySQL(String sql) {
        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        List<Integer> list = deptUserService.listIdBySql(sql);
        Vector<DeptUserDb> v = new Vector<>();
        for (Integer id : list) {
            DeptUser deptUser = deptUserService.getById(id);
            v.addElement(getFromDeptUser(deptUser, new DeptUserDb()));
        }
        return v;
    }

    /**
     * 仅用于网盘中获取用户姓名
     * @return
     */
    public String getUserRealName() {
        if (userName==null || userName.equals("")) {
            return "";
        }
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        if (ud!=null &&  ud.isLoaded()) {
            return ud.getRealName();
        } else {
            return "";
        }
    }

    /**
	 * 取出全部信息置于result中
	 */
    @Override
    public Vector<DeptUserDb> list(String deptCode) {
        Vector<DeptUserDb> result = new Vector<>();
        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        List<DeptUser> list = deptUserService.listByDeptCode(deptCode);
        for (DeptUser deptUser : list) {
            result.addElement(getFromDeptUser(deptUser, new DeptUserDb()));
        }

        return result;
    }

    public Vector<DeptUserDb> listAll(String deptCode) {
        Vector<DeptUserDb> result = new Vector<>();
        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        List<DeptUser> list = deptUserService.listAllByDeptCode(deptCode);
        for (DeptUser deptUser : list) {
            result.addElement(getFromDeptUser(deptUser, new DeptUserDb()));
        }
        return result;
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
                    result.addElement(getDeptUserDb(rs.getInt(1)));
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
    
    public Vector<UserDb> getAllUsersOfUnit(String unitCode) {
        IDepartmentService departmentService = SpringUtil.getBean(IDepartmentService.class);
        List<Department> deptList = new ArrayList<>();
        departmentService.getAllChild(deptList, unitCode);

        String depts = StrUtil.sqlstr(unitCode);
        for (Department dept : deptList) {
            depts += "," + StrUtil.sqlstr(dept.getCode());
        }

        IDeptUserService deptUserService = SpringUtil.getBean(IDeptUserService.class);
        List<String> list = deptUserService.listUserNameInDepts(depts);
        Vector<UserDb> v = new Vector();
        UserDb userDb = new UserDb();
        for (String userName : list) {
            v.addElement(userDb.getUserDb(userName));
        }
        return v;
    }

    private int orders; // 起始值为0

    private String rank;
    private String adminDept;

}
