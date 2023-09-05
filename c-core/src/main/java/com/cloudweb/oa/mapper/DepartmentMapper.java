package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-01-30
 */
public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("select MAX(code) from department d where d.parentCode=#{parentCode}")
    String getMaxCode(String parentCode);

    List<Department> getChildren(String code);

    List<Department> getDeptsOfUser(String userName);

    @Select("select code from department where code<>'root' order by layer asc")
    List<Department> getDeptsWithouRoot();

    @Update("update department set orders=orders-1 where parentCode=#{parentCode} and orders>${orders}")
    Integer updateOrdersGreatThan(@Param("parentCode") String parentCode, @Param("orders")int orders);

}
