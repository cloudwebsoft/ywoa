package com.cloudweb.oa.mapper;

import com.cloudweb.oa.entity.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fgf
 * @since 2020-02-09
 */
public interface AccountMapper extends BaseMapper<Account> {

    List<Account> listBySql(@Param("sql") String sql);

}
