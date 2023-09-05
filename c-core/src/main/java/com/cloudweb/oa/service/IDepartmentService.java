package com.cloudweb.oa.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.entity.Department;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-01-30
 */
public interface IDepartmentService extends IService<Department> {
    JSONObject getAddDepartmentData(String parentCode);

    boolean create(Department department) throws ValidateException;

    boolean createAnySyn(Department department) throws ValidateException;

    Department getDepartment(String code);

    Department getDepartmentByName(String name);

    List<Department> getChildren(String code);

    boolean update(Department department) throws ValidateException;

    List<Department> getAllChild(List<Department> list, String code);

    Department getUnitOfDept(Department department);

    boolean delWithChildren(String code, boolean canDelWhenHasUser) throws ValidateException;

    boolean del(String code, String userName) throws ValidateException;

    void move(String code, String parentCode, int position) throws ValidateException;

    String getJsonString(boolean isOpenAll, boolean isShowNodeHided);

    List<Department> getDepartments(String parentCode);

    List<Department> findChildren(List<Department> treeList);

    List<String> getAllUnit();

    List<Department> getDeptsOfUser(String userName);

    List<String> getBranchDeptCode(String code, List<String> list);

    StringBuffer getUnitAsOptions(StringBuffer outStr, Department department, int rootlayer);

    List<Department> getDeptsWithouRoot();

    List<Department> getDeptsNotHide();

    String getDeptAsOptions(StringBuffer outStr, Department department, int rootLayer);

    String generateNewNodeCode(String parentCode);

    boolean updateByCode(Department department);

    boolean setNewParent(Department department, String newParentCode);

    Department getBrother(Department department, String direction);

    Map<String, String> getFulleNameMap();

    String getFullNameOfDept(Department department);

    List<JSONObject> getUnits(Department department, int rootLayer);

    JSONArray getComboTree(Department department, JSONArray arr);

    List<Department> getUnitTree();
}