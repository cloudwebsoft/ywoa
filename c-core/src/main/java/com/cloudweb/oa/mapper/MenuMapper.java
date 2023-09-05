package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-02-15
 */
public interface MenuMapper extends BaseMapper<Menu> {

    @Update("update oa_menu set orders=orders-1 where parent_code=#{parentCode} and orders>${orders}")
    Integer updateOrders(@Param("parentCode")String parentCode, @Param("orders")int orders);

    @Update("update oa_menu set child_count=child_count-1 where code=#{code}")
    Integer updateChildCount(String code);
}
