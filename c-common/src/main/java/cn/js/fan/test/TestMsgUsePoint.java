package cn.js.fan.test;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TestMsgUsePoint {
    public TestMsgUsePoint() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println(SQLFilter.getCountSql("select id from sms_send_record group by batch order by sendtime desc"));
        if (true) {
            return;
        }

        java.util.Date d = new java.util.Date(108, 0, 1);
        System.out.println(DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateUtil.toLong(d));
        if (true)
            return;

        TestMsgUsePoint twb = new TestMsgUsePoint();
        // 以下内容需要支付1分积分方可查看
        String patternStr =
                "(\\[points=([a-z|A-Z]*),\\s*([0-9]*)\\])(.[^\\[]*)(\\[\\/points\\])";
        Pattern pattern;
        Matcher matcher;
        String content = "[points=good, 200]sdafsafsadfsadfsadfsafd[/msg_use]dsfasfasaaaaaaaaaaaaaa[points=Food,500]xxxxxxxxxxxxxx[/points]";
        pattern = Pattern.compile(patternStr,
                                  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            System.out.println(matcher.group(3));
            System.out.println(matcher.group(4));
            System.out.println("-----------");
        }
    }
}
