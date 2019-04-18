package com.redmoon.dingding.service.department;

import cn.js.fan.util.ErrMsgException;
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
import org.apache.log4j.Logger;

import java.util.List;

public class DepartmentService extends BaseService {
    /**
     * 获取所有部门
     *
     * @return
     */
    public List<DdDepartment> allDepartments() {
        List<DdDepartment> _depts = null;
        try {
            HttpHelper _http = new HttpHelper(URL_DEPT_LIST + "id=1&fetch_child=true&");
            DdDeptDto deptDto = _http.httpGet(DdDeptDto.class);
            if (deptDto != null) {
                _depts = deptDto.department;
            }
        } catch (DdException e) {
            Logger.getLogger(UserService.class).error(e.getMessage());
        }
        return _depts;
    }

    /**
     * 删除部门
     *
     * @param _id
     * @return
     */
    public boolean delDept(int _id) {
        boolean _flag = false;
        HttpHelper _http = new HttpHelper(URL_DEPT_DELETE + "id=" + _id + "&");
        try {
            _http.httpGet(BaseDdObj.class);
            _flag = true;
        } catch (DdException e) {
            e.printStackTrace();
        }
        return _flag;
    }

    /**
     * 创建部门
     *
     * @param name
     * @param parentId
     * @return
     */
    public int addDept(String name, Integer parentId, int order) {
        int _id = -1;
        DdDepartment _dd = new DdDepartment();
        _dd.name = name;
        _dd.parentid = parentId;
        _dd.autoAddUser = true;
        _dd.createDeptGroup = true;
        _dd.order = order;
        HttpHelper _http = new HttpHelper(URL_DEPT_CREATE);
        try {
            CreateResDto _dto = _http.httpPost(CreateResDto.class, _dd);
            if (_dto != null) {
                _id = _dto.id;
            }
        } catch (DdException e) {
            e.printStackTrace();
        }
        return _id;
    }

    /**
     * 部門獲得詳情
     *
     * @param id
     * @return
     */
    public DdDeptDetailDto getDept(int id) {
        HttpHelper _http = new HttpHelper(URL_DEPT_GET + "id=" + id + "&");
        DdDeptDetailDto dd = null;
        try {
            dd = _http.httpGet(DdDeptDetailDto.class);
        } catch (DdException e) {
            e.printStackTrace();
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
        int _res = -1;
        DeptDb _deptDb = new DeptDb(code);
        DeptDb _pDeptDb = new DeptDb(_deptDb.getParentCode());
        String _parentCode = _pDeptDb.getCode();
        int _parentId = _pDeptDb.getId();
        if (_parentCode.equals(DeptDb.ROOTCODE)) {
            _parentId = Enum.ROOT_DEPT_ID;
        }
        if (_deptDb != null && _deptDb.isLoaded()) {
            DdDepartment _dd = new DdDepartment();
            _dd.id = _deptDb.getId();
            _dd.name = _deptDb.getName();
            _dd.parentid = _parentId;
            _dd.autoAddUser = true;
            _dd.createDeptGroup = true;
            _dd.order = _deptDb.getOrders();
            HttpHelper _http = new HttpHelper(URL_DEPT_UPDATE);
            try {
                CreateResDto _dto = _http.httpPost(CreateResDto.class, _dd);
                if (_dto != null) {
                    _res = _dto.id;
                }
            } catch (DdException e) {
                e.printStackTrace();
            }
        }
        return _res;
    }
}
