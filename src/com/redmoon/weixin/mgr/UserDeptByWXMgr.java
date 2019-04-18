package com.redmoon.weixin.mgr;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.organization.DeptTreeAction;
import com.redmoon.oa.person.UserDb;

import java.util.HashMap;

/**
 * 用于OA中 用户 部门 新增的一系列操作
 */
public  class UserDeptByWXMgr {
    public interface Callback{
        void createDeptAfter();
    }

    /**
     * 获取微信数据 创建用户至OA
     * @param hashMap
     * @throws ErrMsgException
     */
    public void createUser(HashMap<String,String> hashMap) throws ErrMsgException {
        String _userId = hashMap.get("UserID");
        UserDb _userDb = new UserDb(_userId);
        if(_userDb!=null && _userDb.isLoaded()){
            saveUser(hashMap);
            if(hashMap.containsKey("Department")){
                //先删除
                DeptUserDb _du = new DeptUserDb(_userId);
                _du.delUser(_userId);
                String _deptIds = hashMap.get("Department");
                createDeptUser(_userId,_deptIds);
            }
        }else{
            String _realName = hashMap.get("Name");
            String _mobile = hashMap.get("Mobile");
            String deptIds = hashMap.get("Department");
            com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
            // 默认密码
            String defaultPwd = scfg.getInitPassword();
            _userDb.create(_userId,_realName,defaultPwd,_mobile, DeptDb.ROOTCODE);//新增用户
            saveUser(hashMap);//修改用户
            createDeptUser(_userId,deptIds);//创建用户部门
        }
    }

    public void saveUser(HashMap<String,String> hashMap) throws ErrMsgException{
        String _userId = hashMap.get("UserID");
        UserDb _userDb = new UserDb(_userId);
        if(_userDb!=null && _userDb.isLoaded()){
            if(hashMap.containsKey("Email"))
                _userDb.setEmail(hashMap.get("Email"));
            if(hashMap.containsKey("Gender"))
                _userDb.setGender(StrUtil.toInt(hashMap.get("Gender")) ==1?0:1);
            if(hashMap.containsKey("Telephone"))
                _userDb.setPhone(hashMap.get("Telephone"));
            if(hashMap.containsKey("Name"))
                _userDb.setRealName(hashMap.get("Name"));
            _userDb.save();
        }
    }


    /**
     * 创建部门用户
     * @param deptIds
     */
    public void  createDeptUser(String userId,String deptIds) throws ErrMsgException {
        String[] _Departments = deptIds.split(",");
        if(_Departments!=null &&  _Departments.length>0){
            for(String deptIdStr:_Departments ){
                DeptDb deptDb = null;
                int _deptId = StrUtil.toInt(deptIdStr);
                deptDb = _deptId == 1?new DeptDb(DeptDb.ROOTCODE):new DeptDb(_deptId);
                if(deptDb!=null && deptDb.isLoaded()){
                    DeptUserDb _deptUserDb = new DeptUserDb();
                    _deptUserDb.create(deptDb.getCode(),userId,"");
                }
            }
        }
    }

    /**
     * 创建部门
     * @param hashMap
     */
    public boolean createDept(HashMap<String,String> hashMap) throws ErrMsgException {
        boolean _flag = true;
        Integer _Id = StrUtil.toInt(hashMap.get("Id"),0);
        Integer _parentId = StrUtil.toInt(hashMap.get("ParentId"),0);
        if(_Id == 1){
            return  true;
        }
        String _Name = hashMap.get("Name");
        DeptDb _parentDept = null;
        _parentDept = _parentId == 1?new DeptDb(DeptDb.ROOTCODE):new DeptDb(_parentId);
        if(_parentDept!=null && _parentDept.isLoaded()){
            DeptDb _cDept = new DeptDb(_Id);
            if(_cDept!=null && _cDept.isLoaded()){//修改
                _cDept.setParentCode(_parentDept.getCode());
                _cDept.setName(_Name);
                _flag = _cDept.save();
            }else{ //新增
                DeptTreeAction _deptTreeAction = new DeptTreeAction();
                _deptTreeAction.generateNewNodeCode(_parentDept.getCode());
                String _newCode = _deptTreeAction.getNewNodeCode();
                DeptDb lf = new DeptDb();
                lf.setId(_Id);
                lf.setName(_Name);
                lf.setCode(_newCode);
                lf.setParentCode(_parentDept.getCode());
                lf.setDescription("");
                lf.setType(1);
                lf.setShow(true);
                lf.setShortName(_Name);
                lf.setGroup(false);
                lf.setHide(false);
                _flag = _parentDept.AddChild(lf);
            }
        }
        return  _flag;
    }


}
