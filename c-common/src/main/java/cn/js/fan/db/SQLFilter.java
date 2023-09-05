package cn.js.fan.db;

import cn.js.fan.web.Global;
import cn.js.fan.util.StrUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

public class SQLFilter {
    public SQLFilter() {
    }

    /**
     * 将sql语句中多余的空格去掉，但不能去掉单引号括起来部分的空格，因为查询条件有可能为 where name='张    三'
     * @return
     */
    public static String trimMultiBlank(String sql) {
        String regEx = "([^']*)('.*?')*([^']*)"; // 一个或多个空格
        Pattern pp = Pattern.compile(regEx);
        Matcher mm = pp.matcher(sql);

        // DebugUtil.i(SQLFilter.class, "sql", sql);
        StringBuffer sb = new StringBuffer();
        boolean result = mm.find();
        while (result) {
            String s1 = mm.group(1);
            String s3 = mm.group(3);
            s1 = s1.replaceAll("[ ]+", " ");
            s3 = s3.replaceAll("[ ]+", " ");
            String str = s1 + "$2" + s3;

            mm.appendReplacement(sb, str);
            // DebugUtil.i(SQLFilter.class, "sb", sb.toString());

            result = mm.find();
        }
        mm.appendTail(sb);
        return sb.toString();
    }

    /**
     * 分析query查询语句，生成
     * 当sql语句中有group by时，不适用此方法
     * @param sql String
     * @return String
     */
    public static String getCountSql(String sql) {
        if (sql.contains(" union ") || sql.contains(" join ")) {
            // 必须加上tmp别名，否则会报： Every derived table must have its own alias
            return "select count(*) from (" + sql + ") as myTmpTable";
        }

        // 我的流程，存在子查询
        // select distinct id from (select distinct f.id,m.id as mid from flow_my_action m force index (user_checked), flow f where m.flow_id=f.id and m.user_name='zs' and f.status>-10 and m.is_checked<9 order by m.id desc) tmp;
    	if (sql.contains(" from (") || sql.contains(" from  (")) {
            return "select count(*) from (" + sql + ") as myTmpTable";
        }

        // query = "select distinct id from table where id=1";
    	// 注意sql语句中对于数据的处理会区分大小写，所以不能转换为小写
        String query = sql.toLowerCase();
    	query = trimMultiBlank(query);

    	sql = trimMultiBlank(sql); // 替换为一个空格

        int begin = query.indexOf(" from ");
        if (begin==-1) {
            // DebugUtil.i(SQLFilter.class, "getCountSql", "sql语句中缺少from：" + sql);
            return "select count(*) from (" + sql + ") as mytable";
        }
        String queryPart = query.substring(begin, query.length()).trim();
        String queryPartRaw = sql.substring(begin, query.length()).trim();

        // 去除query_part中的order by 部分
        // 2010-8-15 不能直接去除order by，因为：
        // select * from (select user_name,change_date,row_number() over (partition by user_name order by change_date Desc) rank_no from archive_user_change) t where t.rank_no=1
        // @task:需要找到最后一个where之后的order by，或者留给数据库自己去解决
        // 2011-1-22还是该去除，因为有一些统计语句，如：select user_name, sum(score) as c from cms_wiki_doc_update where check_status=" + WikiDocUpdateDb.CHECK_STATUS_PASSED + " and edit_date>=? and edit_date<? group by user_name order by 2 desc";
        // 其中含有的order by项，在转换中，必须去除
        // 因此通过判断order by后是否含有 where 或 from (含空格)判断能否去除order by

        int d = -1;

        d = queryPart.lastIndexOf(" order by");
        if (d != -1) {
            String temp = queryPart.substring(d + " order by".length());
            boolean canRemove = true;
            if (temp.contains(" from ")) {
                canRemove = false;
            } else if (temp.contains(" where ")) {
                canRemove = false;
            }
            if (canRemove) {
                queryPart = queryPart.substring(0, d);
                queryPartRaw = queryPartRaw.substring(0, d);
            }
        }

        // 2011-3-9 group by问题
        // select count(*) from ft_sales_order o, ft_sales_ord_product p where o.id=p.cws_id group by p.cws_creator order by 1 desc
        // 上句会取出多条记录，每条记录的值为group by 分组的count计数
        // 因此需滤除group by
        
        // 2015-10-3 fgf
        // select sum(score_value) as s, user_name from sq_score_log group by user_name order by s desc
        // 上句中的group by不能被过滤，否则会报Invalid use of group function
        
        d = queryPart.indexOf(" group by");
        boolean isSimpleGroup = false;
        if (d != -1) {
            String temp = queryPart.substring(d + " group by".length());
            String selectPart = query.substring(0, begin);
            boolean canRemove = true;
            if (selectPart.contains(" sum(")) {
            	canRemove = false;
            }
            else {
	            if (temp.contains(" from ")) {
                    canRemove = false;
                } else if (temp.contains(" where ")) {
                    canRemove = false;
                }
	            // 2011-11-17 select id from archive_user_info_instant where user_name in ((select user_name from archive_user_info_instant) intersect (select user_name from archive_studyinfo where end_date is not null and STUDY_TYPE=0 group by user_name having max(degree) = 1.0)) and working_state = 0 and substr(department,1,10) = '2320200016' order by department
	            else if (temp.contains(" and ")) {
                    canRemove = false;
                } else {
                    // select id from ft_zs_xm_dupl t1 where t1.is_dupl=1 and 1=1 group by xm; // 如果去掉group by 数量可能会减少
                    canRemove = false;
                    isSimpleGroup = true;
                }
            }
            if (canRemove) {
                queryPart = queryPart.substring(0, d);
                queryPartRaw = queryPartRaw.substring(0, d);
            }
        }

        // 分析当query中存在有distinct的情况
        d = query.indexOf("distinct ");
        int n = query.indexOf("(");
        String distinct = ""; //存放distinct中的域
        // 判断distinct前有没有(号，如果有，则说明distinct在子句中
        if (d != -1 && n > d) {
            int nextspace = query.indexOf(" ", d + 9);
            if (nextspace > d) {
                distinct = query.substring(d + 9, nextspace).trim();
                // 假如distinct后有多个字段，则去除末尾有逗号的情况
                if (distinct.lastIndexOf(",")==distinct.length()-1) {
                    distinct = distinct.substring(0, distinct.length()-1);
                }
                // 如果多个字段之间无空格，如select distinct name,birthday from users...
                int p = distinct.indexOf(",");
                if (p!=-1) {
                    distinct = distinct.substring(0, p);
                }
                
                // select id, count(DISTINCT flow) from flow_paper_distribut       
                p = distinct.indexOf(")");
                if (p!=-1) {
                    distinct = distinct.substring(0, p);
                }
            }
        }

        // task:当SQL语句为select id from plugin_exam_user_answer group by user_name having count(*)>=10 order by answer_time desc
        // 时，再用select count(*)或者select count(id)就不正确了

        if ("".equals(distinct)) {
        	// 下句当两个表联合查询，在sqlserver中时，结果始终为0，需要count select语句中的第一个字段
        	// query = "select count(*) " + query_part;
        	
            int p = query.indexOf("select");
            p = p + 6;
            
            String str = query.substring(p, begin).trim(); // 取出select的字段

            String[] ary = StrUtil.split(str, ",");
            
            boolean isSelectOnlySum = false;
            String f = ary[0].trim();
            if (f.contains("sum(")) {
            	if (ary.length>1) {
            		f = ary[1].trim();
            	}
            	else {
            		query = "select count(*) " + queryPartRaw;
            		isSelectOnlySum = true;
            	}
            }
            
            if (!isSelectOnlySum) {
	            int q = f.indexOf(" as ");
	            if (q!=-1) {
	            	f = ary[0].substring(0, q).trim();
	            }
	            if (f.contains("count(")) {
	            	query = "select " + f + queryPartRaw;
	            }
	            else {
                    if (!isSimpleGroup) {
                        // 如果f为p.*，则select count(p.*) from ...会报错
                        if (f.contains(".*")) {
                            f = "*";
                        }
                        query = "select count(" + f + ") " + queryPartRaw;
                    }
	                else {
                        query = "select count(*) from (select " + f + " " + queryPartRaw + ") a";
                    }
	            }
            }
        }
        else {
            if (!isSimpleGroup) {
                query = "select count(distinct " + distinct + ") " + queryPartRaw;
            }
            else {
                // select distinct t1.id from ft_zs_xm_dupl t1 where t1.unit_code='root' and 1=1 and t1.is_dupl=1 and t1.cws_status=1 group by xm
                query = "select count(*) from (" + sql + ") as mytable";
            }
        }
        return query;
    }

    /**
     * 防注入
     * @param str String
     * @return boolean
     */
    public static boolean sql_inj(String str) {
        // String inj_str = "'|and|exec|insert|select|delete|update|count|*|%|chr|mid|master|truncate|char|declare|;|or|-|+|,";
        // 允许or，如LinkDb中的url中可能含有.org
        // String inj_str = "'|and|exec|insert|select|delete|update|count|*|%|chr|mid|master|truncate|char|declare|;|-|+|,";
        // 允许+ - ,
    	if (str.startsWith(";") || str.startsWith("'")) {
    		return true;
    	}

        /*
        1) Set parameter 'orderBy's value to 'id and sleep(0)'
        2) Set parameter 'orderBy's value to 'id and 1=2 or sleep(11)=0 limit 1 -- '
        3) orderBy=id and sleep(0)
        */
        Pattern p = Pattern.compile("( * (and)? *(or)? *sleep.*?)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        String strReplaced = m.replaceAll("");
        if (!strReplaced.equals(str)) {
            return true;
        }

        // * 会出现在cron表达式中
        // String inj_str = " having |'| and |exec|insert|select |delete|update| count| *| %| chr| mid|master|truncate|char|declare";
        String inj_str = " having |' |\'| and |exec|insert |select |delete |update | count| %| chr| mid| master|truncate | char| declare";//前台传sql语句到后台，字符型数据会含有单引号，所以单引号屏蔽删除 20141211 jfy
        String inj_stra[] = inj_str.split("\\|"); 
        for (int i = 0; i < inj_stra.length; i++) {
            if (str.indexOf(inj_stra[i]) > 0) { // 原来为>=0，但是这样会过滤掉像ajax_online.jsp?op=count的参数，所以改为>0 fgf 20141026
            	if (inj_stra[i].equals(" and ")) {
            		if (str.indexOf(" where ") > 0) {
            			return true;
            		}
            	} else if (inj_stra[i].equals(" %")) {
            		if (str.indexOf(" like ") > 0) {
            			return true;
            		}
            	} else {
                    return true;
            	}
            }
        }
        return false;
    }

    /**
     * 防止非法植入攻击
     * @param param String
     * @return boolean
     */
    public static boolean isValidSqlParam(String param) {
        if (param==null) {
            return true;
        }
        return !sql_inj(param.toLowerCase());
    }

    public static boolean isValidSql(String sql) {
        //防止非法删除
        return sql.toLowerCase().indexOf(";delete") == -1;
    }

    public static String sqlstr(String str) {
        if (str == null || (str.trim()).equals("")) {
            str = "\'\'";
            return str;
        }
        str = "\'" + replace(str, "\'", "\'\'") + "\'";
        return str;
    }

    /**
     * 转换日期字符串为相应的数据库中对应的日期格式
     * @param dateStr String
     * @param format String
     * @return String
     */
    public static String getDateStr(String dateStr, String format) {
        String str = dateStr;
        if (Global.db.equalsIgnoreCase(Global.DB_MYSQL)) {
            if (format.equals("yyyy-MM-dd") || format.equals("yyyy-MM-dd HH:mm:ss")) {
                return StrUtil.sqlstr(dateStr);
            }
        } else if (Global.db.equalsIgnoreCase(Global.DB_SQLSERVER)) {
            if (format.equals("yyyy-MM-dd") || format.equals("yyyy-MM-dd HH:mm:ss")) {
                return StrUtil.sqlstr(dateStr);
            }
        } else if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
            if (format.equals("yyyy-MM-dd HH:mm:ss")) {
                return "to_date('" + dateStr + "','yyyy-mm-dd hh24:mi:ss')";
            } else if (format.equals("yyyy-MM-dd")) {
                return "to_date('" + dateStr + "','yyyy-mm-dd')";
            }
        }else if(Global.db.equalsIgnoreCase(Global.DB_POSTGRESQL)){
        	 if (format.equals("yyyy-MM-dd HH:mm:ss")) {
                 return "to_timestamp('" + dateStr + "','yyyy-mm-dd hh24:mi:ss')";
             } else if (format.equals("yyyy-MM-dd")) {
                 return "to_date('" + dateStr + "','yyyy-mm-dd')";
             }
        }
       return str;
    }
    
    /**
     * 取得最后一个自增长的ID
     * @param jt
     * @return
     */
    public static long getLastId(JdbcTemplate jt) {
    	/*
    	Oracle      SELECT sequence.currval FROM DUAL
    	MySQL       SELECT LAST_INSERT_ID()
    	SqlServer   SELECT SCOPE_IDENTITY()或SELECT @@IDENTITY

    	PostgreSQL  SELECT nextval('<TABLE>_SEQ')
    	DB2         IDENTITY_VAL_LOCAL()
    	Informix    SELECT dbinfo('sqlca.sqlerrd1') FROM <TABLE>
    	Sybase      SELECT @@IDENTITY
    	HsqlDB      CALL IDENTITY()
    	Cloudscape  IDENTITY_VAL_LOCAL()
    	Derby       IDENTITY_VAL_LOCAL()
    	*/
    	String sql;
        if (Global.db.equalsIgnoreCase(Global.DB_SQLSERVER)) {
            sql = "SELECT SCOPE_IDENTITY()";
        } else if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
            sql = "SELECT sequence.currval FROM DUAL";
        }
        else {
        	sql = "SELECT LAST_INSERT_ID()";
        }
        try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getLong(1);
			}
		} catch (SQLException e) {
            LogUtil.getLog(SQLFilter.class).error(e);
		}
    	return -1;
    }

    public static long getLastId(Conn conn, String tableName) throws SQLException {
        long visualId = -1;
        String sql;

        if (Global.db.equalsIgnoreCase(Global.DB_SQLSERVER)) {
            sql = "select SCOPE_IDENTITY()";
        }
        else if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
            sql = "SELECT " + tableName + "_id.currval FROM DUAL";
        }
        else { // if (Global.db.equalsIgnoreCase(Global.DB_MYSQL)) {
            sql = "select last_insert_id() from " + tableName + " limit 1";
        }
        ResultSet rs = conn.executeQuery(sql);
        if (rs.next()) {
            visualId = rs.getLong(1);
        }
        return visualId;
    }

    /**
     * 取得字段field为相应的数据库中对应的取得年份的函数
     * @param field String 字段名，注意字段为date类型
     * @return String
     */
    public static String year(String field) {
        if (Global.db.equalsIgnoreCase(Global.DB_SQLSERVER)) {
            return "year(" + field + ")";
        } else if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
            return "to_char(" + field +  ",'yyyy')";
        }else if (Global.db.equalsIgnoreCase(Global.DB_POSTGRESQL)) {
            return "to_char(" + field +  ",'yyyy')";
        } else {
            return "year(" + field + ")";
        }
    }

    public static String now() {
        if (Global.db.equalsIgnoreCase(Global.DB_SQLSERVER)) {
            return "getDate()";
        } else if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
            return "sysdate";
        } else {
            return "NOW()";
        }
    }

    /**
     * 取得字段field为相应的数据库中对应的取得月份的函数
     * @param field String 字段名，注意字段为date类型
     * @return String
     */
    public static String month(String field) {
        if (Global.db.equalsIgnoreCase(Global.DB_SQLSERVER)) {
            return "month(" + field + ")";
        } else if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
            return "to_char(" + field +  ",'mm')";
        } else if (Global.db.equalsIgnoreCase(Global.DB_POSTGRESQL)) {
            return "to_char(" + field +  ",'mm')";
        }else {
            return "month(" + field + ")";
        }
    }

    public static String day(String field) {
        if (Global.db.equalsIgnoreCase(Global.DB_SQLSERVER)) {
            return "day(" + field + ")";
        } else if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
            return "to_char(" + field +  ",'dd')";
        } else if (Global.db.equalsIgnoreCase(Global.DB_POSTGRESQL)) {
            return "to_char(" + field +  ",'dd')";
        } else {
            return "day(" + field + ")";
        }
    }

    /**
     * 取得sql语句中from（包括在内）后面的部分
     * @param query String
     * @return String
     */
    public static String getFromSql(String query) {
        query = query.toLowerCase();
        int begin = query.indexOf(" from ");
        String query_part = query.substring(begin, query.length()).trim();
        return query_part;
    }

    public static String replace(String strSource, String strFrom, String strTo) {
        if (strSource.equals("") || strSource == null)
            return strSource;
        String strDest = "";
        int intFromLen = strFrom.length();
        int intPos;
        if (strSource == null || (strSource.trim()).equals(""))
            return strSource;
        while ((intPos = strSource.indexOf(strFrom)) != -1) {
            strDest = strDest + strSource.substring(0, intPos);
            strDest = strDest + strTo;
            strSource = strSource.substring(intPos + intFromLen);
        }
        strDest = strDest + strSource;

        return strDest;
    }

    public static String left(String field, int len) {
        if (Global.db.equalsIgnoreCase(Global.DB_SQLSERVER)) {
            return "Left(" + field + "," + len + ")";
        } else if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
            return "substr(" + field + ",1," + len + ")";
        } else if (Global.db.equalsIgnoreCase(Global.DB_POSTGRESQL)) {
            return "substr(" + field + ",1," + len + ")";
        } else {
            return "Left(" + field + "," + len + ")";
        }
    }

    /**
     * 连接条件，如concat(..., "and", "name='cws'")
     * @param cond String 原来的条件
     * @param opToken String 逻辑运算符 and or
     * @param newCond String 新加的条件
     * @return String
     */
    public static String concat(String cond, String opToken, String newCond) {
        if ("".equals(cond)) {
            cond = newCond;
        }
        else {
            cond += " " + opToken + " " + newCond;
        }
        return cond;
    }

    public static void main(String[] args) {
        String sql = "select t1.id from ft_prj_worker t1 where t1.id not in (select distinct worker from ft_prj_org_worker t100 where t100.status=1 and end_date>=now()) and end_time>=now() and 1=1 order by t1.id desc";
        sql = getCountSql(sql);
        // LogUtil.getLog(getClass()).info(sql);
    }
}
