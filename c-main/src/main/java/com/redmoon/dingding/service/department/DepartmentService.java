package com.redmoon.dingding.service.department;

import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.dingding.domain.BaseDdObj;
import com.redmoon.dingding.domain.CreateResDto;
import com.redmoon.dingding.domain.DdDepartment;
import com.redmoon.dingding.enums.Enum;
import com.redmoon.dingding.service.BaseService;
import com.redmoon.dingding.service.department.dto.DdDeptDetailDto;
import com.redmoon.dingding.service.department.dto.DdDeptDto;
import com.redmoon.dingding.service.user.UserService;
import com.redmoon.dingding.util.DdException;
import com.redmoon.dingding.util.HttpHelper;
import com.redmoon.oa.dept.DeptDb;

import java.util.List;

public class DepartmentService extends BaseService {
    /**
     * 获取所有部门
     *
     * @return
     */
    public List<DdDepartment> allDepartments() {
        List<DdDepartment> depts = null;
        try {
            HttpHelper http = new HttpHelper(URL_DEPT_LIST + "id=1&fetch_child=true&");
            DdDeptDto deptDto = http.httpGet(DdDeptDto.class);
            if (deptDto != null) {
                depts = deptDto.department;
            }
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            LogUtil.getLog(getClass()).error(e);
        }
        return depts;
    }

    /**
     * 删除部门
     *
     * @param _id
     * @return
     */
    public boolean delDept(int _id) {
        boolean flag = false;
        HttpHelper http = new HttpHelper(URL_DEPT_DELETE + "id=" + _id + "&");
        try {
            http.httpGet(BaseDdObj.class);
            flag = true;
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException(e.getMessage());
        }
        return flag;
    }

    /**
     * 创建部门
     *
     * @param name
     * @param parentId
     * @return
     */
    public int addDept(String name, Integer parentId, int order) {
        int id = -1;
        DdDepartment dd = new DdDepartment();
        dd.name = name;
        dd.parentid = parentId;
        dd.autoAddUser = true;
        dd.createDeptGroup = true;
        dd.order = order;
        HttpHelper http = new HttpHelper(URL_DEPT_CREATE);
        try {
            CreateResDto dto = http.httpPost(CreateResDto.class, dd);
            if (dto != null) {
                id = dto.id;
            }
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException(e.getMessage());
        }
        return id;
    }

    /**
     * 部門獲得詳情
     *
     * @param id
     * @return
     */
    public DdDeptDetailDto getDept(int id) {
        HttpHelper http = new HttpHelper(URL_DEPT_GET + "id=" + id + "&");
        DdDeptDetailDto dd = null;
        try {
            dd = http.httpGet(DdDeptDetailDto.class);
        } catch (DdException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return dd;
    }

    /**
     * 更新部門
     *
     * @param code
     * @return
     */
    public int updateDept(String code) {
        int res = -1;
        DeptDb deptDb = new DeptDb(code);
        DeptDb pDeptDb = new DeptDb(deptDb.getParentCode());
        String parentCode = pDeptDb.getCode();
        int parentId = pDeptDb.getId();
        if (parentCode.equals(DeptDb.ROOTCODE)) {
            parentId = Enum.ROOT_DEPT_ID;
        }
        if (deptDb != null && deptDb.isLoaded()) {
            DdDepartment _dd = new DdDepartment();
            _dd.id = deptDb.getId();
            _dd.name = deptDb.getName();
            _dd.parentid = parentId;
            _dd.autoAddUser = true;
            _dd.createDeptGroup = true;
            _dd.order = deptDb.getOrders();
            HttpHelper _http = new HttpHelper(URL_DEPT_UPDATE);
            try {
                CreateResDto _dto = _http.httpPost(CreateResDto.class, _dd);
                if (_dto != null) {
                    res = _dto.id;
                }
            } catch (DdException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        return res;
    }
}
