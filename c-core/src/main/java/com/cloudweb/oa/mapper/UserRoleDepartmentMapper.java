package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.UserRoleDepartment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudweb.oa.entity.UserRolePost;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 角色关联的部门 Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2022-03-13
 */
public interface UserRoleDepartmentMapper extends BaseMapper<UserRoleDepartment> {

    List<UserRoleDepartment> listBySql(@Param("sql") String sql);

    @Select("select * from user_role_department where dept_code=#{deptCode}")
    List<UserRoleDepartment> listByDeptCode(@Param("deptCode")String deptCode);

    @Select("select * from user_role_department where include=1")
    List<UserRoleDepartment> listAllInclude();

}
