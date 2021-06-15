package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.UserSetup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-02-10
 */
public interface UserSetupMapper extends BaseMapper<UserSetup> {

    @Select("select user_name from user_setup where myleaders like '%${userName}%'")
    List<String> getMySubordinates(String userName);
}
