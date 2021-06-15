package com.cloudweb.oa.service;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.bean.Address;
import com.cloudweb.oa.dao.AddressDao;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.pvg.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Vector;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2018/12/28 14:34
 */
@Service
public class AddressService {

	public static final int TYPE_PUBLIC = 1;
	public static final int TYPE_USER = 0;

	@Autowired
	private AddressDao addressDao;

	@Autowired
	private HttpServletRequest request;

	public AddressService() {
	}

	private static AddressService addrService;

	// 被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器调用一次，类似于Servlet的init()方法。被@PostConstruct修饰的方法会在构造函数之后，init()方法之前运行
	// 该注解的方法在整个Bean初始化中的执行顺序：
	// Constructor(构造方法) -> @Autowired(依赖注入) -> @PostConstruct(注释的方法)
	@PostConstruct //通过@PostConstruct实现初始化bean之前进行的操作，本处是为了支持非spring扫描的包中调用
	public void init() {
		addrService = this;
	}

	public AddressDao getAddressDao() {
		addressDao = addrService.addressDao;
		// 也可以通过SpringHelper.getBean获取
		/*if (addressDao == null) {
			addressDao = SpringHelper.getBean(AddressDao.class);
		}*/
		return addressDao;
	}

	public Address getAddress(int id){
		return getAddressDao().getAddress(id);
	}

	public Address getAddressByMobile(String mobile) {
		return getAddressDao().getAddressByMobile(mobile);
	}

	public boolean create(Address address) throws ErrMsgException{
		String errmsg = "";
		Privilege privilege = new Privilege();
/*		if (address.getTypeId().equals("")) {
			errmsg += "请选择类别！";
		}*/
		if (address.getPerson() == null || address.getPerson().equals("")) {
			errmsg += "姓名不能为空！";
		}
		if (address.getPostalcode().length() > 10) {
			errmsg += "邮政编码长度不能超过10位！\\n";
		}
		if (!errmsg.equals("")) {
			throw new ErrMsgException(errmsg);
		}

		if (address.getType() == TYPE_PUBLIC) {
			if (!privilege.isUserPrivValid(request, "admin.address.public")) {
				throw new ErrMsgException(Privilege.MSG_INVALID);
			}
		}
		// 生成id
		int id = (int) SequenceManager.nextID(SequenceManager.OA_ADDRESS);
		address.setId(id);
		address.setUserName(privilege.getUser(request));
		address.setUnitCode(privilege.getUserUnitCode(request));
		return getAddressDao().create(address);
	}

	public boolean del(int id){
		return getAddressDao().del(id);
	}

	public boolean save(Address address) throws ErrMsgException{
		String errmsg = "";
		Privilege privilege = new Privilege();

/*		if (address.getTypeId()==null || address.getTypeId().equals("")) {
			errmsg += "请选择类别！";
		}*/
		String person = address.getPerson();
		if (person == null || person.equals("")) {
			errmsg += "姓名不能为空！";
		}
		if (address.getPostalcode().length() > 10) {
			errmsg += "邮政编码长度不能超过10位！\\n";
		}
		if (!errmsg.equals("")) {
			throw new ErrMsgException(errmsg);
		}
		return getAddressDao().save(address);
	}

	public ListResult listSql(String sql){
		List<Address> list = getAddressDao().selectList(sql);
		Vector vector = new Vector();
		vector.addAll(list);
		ListResult listResult = new ListResult();
		listResult.setTotal(list.size());
		listResult.setResult(vector);
		return listResult;
	}

	public ListResult listResult(String sql, int curPage, int pageSize) {
		PageHelper.startPage(curPage, pageSize); // 分页查询
		List<Address> list = addressDao.selectList(sql);
		PageInfo<Address> pageInfo = new PageInfo<>(list);

		ListResult lr = new ListResult();
		Vector v = new Vector();
		v.addAll(list);
		lr.setResult(v);
		lr.setTotal(pageInfo.getTotal());
		return lr;
	}

	public void delBatch(String ids) throws ErrMsgException{
		String[] idTemp = ids.split(",");
		Privilege privilege = new Privilege();
		String userUnitCode = privilege.getUserUnitCode(request);
		// 首先判断此数据是否存在
		for(String id : idTemp){
			Address address = getAddress(StrUtil.toInt(id));
			if (address == null){
				throw new ErrMsgException("该项已不存在！");
			}
			if (address.getType() == TYPE_PUBLIC) {
				if (!privilege.isUserPrivValid(request, "admin.address.public") || !address.getUnitCode().equals(userUnitCode)) {
					throw new ErrMsgException(Privilege.MSG_INVALID);
				}
			} else {
				if (!privilege.getUser(request).equals(address.getUserName())) {
					throw new ErrMsgException("非法操作！");
				}
			}
			del(StrUtil.toInt(id));
		}
	}

	/**
	 * 用于获取sql语句
	 * @param op
	 * @param userName
	 * @param type
	 * @param typeId
	 * @param person
	 * @param company
	 * @param mobile
	 * @param orderBy
	 * @param sort
	 * @return
	 */
	public String getSql(String op, String userName, int type, String typeId, String person, String company, String mobile, String orderBy, String sort,String unit_code){
		String sql = "select * from address where type=" + type;
		if (type == TYPE_PUBLIC) {
			if (typeId.equals("public")) {
				op = "search";
				typeId = "";
			}
		} else {
			if (typeId.equals(userName)) {
				op = "search";
				typeId = "";
			}
		}

		if (op.equals("search")) {
			if (type == TYPE_USER) {
				sql = "select * from address where userName=" + StrUtil.sqlstr(userName) + " and type=" + TYPE_USER;
			} else {
				sql = "select * from address where type=" + type;
			}
			if (!person.equals("")) {
				sql += " and person like " + StrUtil.sqlstr("%" + person + "%");
			}

			if (!company.equals("")) {
				sql += " and company like " + StrUtil.sqlstr("%" + company + "%");
			}

			if (!typeId.equals("")) {
				sql += " and typeId = " + StrUtil.sqlstr(typeId);
			}
			if (!mobile.equals("")) {
				sql += " and mobile like " + StrUtil.sqlstr("%" + mobile + "%");
			}
		} else {
			if (!typeId.equals("")) {
				sql += " and typeId = " + StrUtil.sqlstr(typeId);
			}
			if (type != TYPE_PUBLIC) {
				sql += " and userName=" + StrUtil.sqlstr(userName);
			}
		}

		if (type == TYPE_PUBLIC) {
			sql += " and unit_code=" + StrUtil.sqlstr(unit_code);
		}

		sql += " order by " + orderBy;
		sql += " " + sort;
		return sql;
	}
}
