package com.redmoon.oa.exam;
import java.sql.SQLException;
import java.util.Iterator;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger;

import com.cloudwebsoft.framework.db.JdbcTemplate;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class SubjectMgr {
  Logger logger = Logger.getLogger( SubjectMgr.class.getName() );

  public SubjectMgr() {

  }

  public boolean modify(HttpServletRequest request) throws ErrMsgException, Exception {
      boolean re = true;
      String errmsg = "";
      int id = ParamUtil.getInt(request, "hidId");
      String subject = ParamUtil.get(request, "hidSubject");
      int orders =ParamUtil.getInt(request, "hidOrders");
      // 判断order是否与其他科目重复
      String judgeSql = "select count(*) num from oa_exam_subject where orders = "+orders+" and id not in("+id+");";
      JdbcTemplate jt = new JdbcTemplate();
      ResultIterator ri = jt.executeQuery(judgeSql);
      int num=0;
      if(ri.hasNext()){
			ResultRecord rd = (ResultRecord) ri.next();
			num=rd.getInt("num");
      }
      if(num!=0){
    	  errmsg += "该顺序号已存在！\\n";
      }
      if (subject.equals(""))
          errmsg += "名称不能为空！\\n";
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);
      SubjectDb wptd =  new SubjectDb();
      wptd.setId(id);
      wptd.setName(subject);
      wptd.setOrders(orders);
      re = wptd.save();
      return re;
  }

  public SubjectDb getSubjectDb(int id) {
      SubjectDb sb = new SubjectDb();
      return sb.getSubjectDb(id);
  }

  public boolean create(HttpServletRequest request) throws ErrMsgException {
      boolean re = true;
      String errmsg = "";
      String subject = ParamUtil.get(request, "subject");
      // 2017-10-26 增加顺序号orders的判定 添加科目默认给orders赋值为当前最大；
      int orders;
      SubjectDb std = new SubjectDb();
	  	String sql = "select id from oa_exam_subject order by orders desc limit 1";
	  	Iterator ir1 = std.list(sql).iterator();
		if (ir1.hasNext()) {
			std = (SubjectDb)ir1.next();
			orders = std.getOrders()+1;
		}else{
			orders = 0;
		}
		
      if (subject.equals(""))
          errmsg += "名称不能为空！\\n";
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);
      SubjectDb wptd = new SubjectDb();
      if (wptd.isExist(subject))
          throw new ErrMsgException("该科目已存在!");
      else{
    	  wptd.setName(subject);
    	  wptd.setOrders(orders);
              re = wptd.create();
          }
      return re;
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
      int id = ParamUtil.getInt(request, "hidId");
      SubjectDb subjectDb = getSubjectDb(id);
      if (subjectDb==null || !subjectDb.isLoaded())
          throw new ErrMsgException("该项已不存在！");
      return subjectDb.del();
  }
}
