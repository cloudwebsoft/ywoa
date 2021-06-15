package com.cloudweb.oa.mapper;

import com.cloudweb.oa.annotation.SysLog;
import com.cloudweb.oa.entity.UserOfRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
public interface UserOfRoleMapper extends BaseMapper<UserOfRole> {

    @Select("select max(orders) from user_of_role where roleCode=#{roleCode}")
    Integer getMaxOrders(@Param("roleCode") String roleCode);

    @Update("update user_of_role set orders=orders+1 where roleCode=#{roleCode} and orders>=#{orders}")
    Integer updateOrders(String roleCode, int orders);
}
