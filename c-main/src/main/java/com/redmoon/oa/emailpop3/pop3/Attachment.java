package com.redmoon.oa.emailpop3.pop3;

import javax.mail.*;
import javax.mail.internet.*;
import cn.js.fan.util.*;
import java.io.InputStream;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class Attachment {
    String name = "";
    int size = 0;
    int num = 0;
    Part part;
    String ext = "";

    public Attachment(Part part, int num) {
        this.part = part;
        try {
            name = StrUtil.UnicodeToGB(part.getFileName());
            if (name != null)
                ext = name.substring(name.length() - 3, name.length()).
                      toLowerCase(); //取得扩展名
            else
                ext = "";
            size = part.getSize();
        } catch (Exception e) {
            System.out.println("Attachment:" + e.getMessage());
        }
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public String getExt() {
        return ext;
    }

    public int getSize() {
        return size;
    }

    /**
     * 取得附件在邮件中的序号，即第几个附件
     * @return
     */
    public int getNum() {
        return num;
    }

    public InputStream getInputStream() throws Exception {
        return part.getInputStream();
    }
}
