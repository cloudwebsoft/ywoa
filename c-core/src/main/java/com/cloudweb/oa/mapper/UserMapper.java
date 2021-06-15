package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-01-09
 */
public interface UserMapper extends BaseMapper<User> {
    User getUserByName(@Param("userName") String userName);

    User getUserByNameWithRole(@Param("userName") String userName);

    @Update("update users set unit_code=#{unitCode} where exists (select id from dept_user where user_name=name and dept_code=#{code})")
    void updateUserUnitCode(String code, String unitCode);

    List<User> getRecentSelected(@Param("userName") String userName);

    List<User> getRecentSelectedOfUnit(@Param("userName") String userName, @Param("unitCode") String unitCode);

    List<String> listNameBySql(@Param("sql") String sql);

    List<User> listBySql(@Param("sql") String sql);

    @Select("select person_no from users where person_no is not null and person_no<>''")
    List<User> getUsersHasPersonNo();

    @Update("update users set orders=${orders} where name=#{userName}")
    Integer updateUserOrder(@Param("userName") String userName, @Param("orders") int order);

    @Update("update users set unit_code=#{unitCode} where name in (select user_name from dept_user d where d.dept_code=#{deptCode}")
    Integer updateUserUnitInDept(String unitCode, String deptCode);

    @Select("select * from users where isValid=1 and (name <> 'system' and name <> 'admin') order by online_time desc")
    List<User> listByOnlineTime();
}
