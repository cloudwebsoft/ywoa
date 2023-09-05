package com.cloudweb.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudweb.oa.entity.DeptUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-02-04
 */
public interface DeptUserMapper extends BaseMapper<DeptUser> {

    // @Select("select IFNULL(MAX(orders), -1) AS orders from dept_user where dept_code=#{deptCode}")
    @Select("select MAX(orders) AS orders from dept_user where dept_code=#{deptCode}")
    Integer getMaxOrder(String deptCode);

    @Select("select du.* from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE=#{deptCode} order by #{orderField} asc")
    List<DeptUser> listByDeptCode(@Param("deptCode") String deptCode, @Param("orderField") String orderField);

    /**
     * 将位于其后的同一部门下的用户的orders下降一位
     * @param deptCode
     * @param orders
     * @return
     */
    @Update("update dept_user set orders=orders-1 where DEPT_CODE=#{deptCode} and ORDERS>${orders}")
    Integer updateOrdersGreatThan(@Param("deptCode") String deptCode, @Param("orders") int orders);

    /**
     * 取出属于部门deptCodes的用户的user_name
     * 注意deptCodes需以逗号分隔，且每个deptCode需加上单引号
     * @param deptCodes
     * @return
     */
    @Select("select distinct USER_NAME from dept_user where DEPT_CODE in (${deptCodes})")
    List<String> listUserNameInDepts(@Param("deptCodes") String deptCodes);

    List<Integer> listIdBySql(@Param("sql") String sql);

    List<DeptUser> listBySql(@Param("sql") String sql);
}
