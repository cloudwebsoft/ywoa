package com.cloudweb.oa.dao;

import com.cloudweb.oa.bean.Address;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AddressDao{
	// 根据id查询
	Address getAddress(@Param("id") int id);

	// 根据号码查询
	Address getAddressByMobile(@Param("mobile") String mobile);

	// 创建
	boolean create(Address address);

	// 根据id删除
	boolean del(@Param("id") int id);

	// 保存
	boolean save(Address address);

	// 查询
//	List<Address> listResult(@Param("userName") String userName, @Param("type") int type, @Param("typeId") String typeId, @Param("person") String person, @Param("company") String company, @Param("mobile") String mobile, @Param("orderBy") String orderBy, @Param("sort") String sort, @Param("pageSize") int pageSize, @Param("start") int start);

	// 查出所有记录数量
	int count(@Param("sql") String sql);
	List<Integer> listSql(@Param("sql") String sql);
}
