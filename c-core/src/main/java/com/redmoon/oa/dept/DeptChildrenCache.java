package com.redmoon.oa.dept;

import com.cloudweb.oa.cache.DepartmentCache;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.utils.SpringUtil;

import java.util.List;
import java.util.Vector;

public class DeptChildrenCache {
    String parentCode;

    public DeptChildrenCache(String parentCode) {
        this.parentCode = parentCode;
    }

    public Vector<DeptDb> getDirList() {
        DepartmentCache departmentCache = SpringUtil.getBean(DepartmentCache.class);
        List<Department> list = departmentCache.getChildren(parentCode);
        Vector<DeptDb> v = new Vector<>();
        for (Department dept : list) {
            DeptDb dd = new DeptDb();
            v.addElement(dd.getFromDepartment(dept, dd));
        }
        return v;
    }
}
