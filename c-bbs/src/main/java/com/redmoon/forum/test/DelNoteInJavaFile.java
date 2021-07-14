package com.redmoon.forum.test;

import java.io.*;


/**
 * <p>Title: 清除注释</p>
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
public class DelNoteInJavaFile {
    static String srcStr = "";
    static String desStr = "";

    public DelNoteInJavaFile() {
    }

    //处理文件
    private void profile(File src, File des) throws Exception {

        StringBuffer content = new StringBuffer();
        StringBuffer output1 = new StringBuffer();
        StringBuffer output2 = new StringBuffer();
        String temp;
        int start = 0;
        int end = 0;
        int from = 0;
        BufferedReader br = new BufferedReader(new FileReader(src));
        BufferedWriter bw = new BufferedWriter(new FileWriter(des));

        while ((temp = br.readLine()) != null) {
            content.append(temp);
            content.append("\n");
        }

        //弃掉/*   ...   */注释，未考虑以//*开头的注释,
        //且未考虑/*、*/等包含在""内的情形
        //指定from以提高速度
        while ((start = content.indexOf("/*", start)) != -1) {
            output1.append(content.substring(from, start));
            from = content.indexOf("*/", start) + 2;
            start = from;
        }
        output1.append(content.substring(from));

        //弃掉//   ...   注释
        start = 0;
        from = 0;
        while ((start = output1.indexOf("//", start)) != -1) {
            //用来判断"://"和""//"
            if(output1.substring(start - 1, start).equals(":") || output1.substring(start - 1, start).equals("\"")){
                start = start + 2;
            }else{
                output2.append(output1.substring(from, start));
                from = output1.indexOf("\n", start); //保留回车换行
                if (from == -1) { //不以"\n"结束
                    from = output1.length();
                    break;
                }
                start = from;
            }
        }
        output2.append(output1.substring(from));

        bw.write(output2.toString());
        br.close();
        bw.close();
    }

    //处理目录
    private void prodir(File src, File des) throws Exception {

        des.mkdir(); //创建目录
        File[] files = src.listFiles();

        for (int i = 0; i < files.length; i++) {
            //下一级目录/文件
            File nf = new File(des, files[i].getName());
            System.out.println("prodir=" + files[i].getName());
            if (files[i].isDirectory()) {
                //是目录，递归
                prodir(files[i], nf);
            } else { //文件
                profile(files[i], nf);
            }
        }
    }


    //输入目录还是文件
    public void dispReq(File src, File des) throws Exception {
        if (src.isDirectory()) {
            prodir(src, des);
        } else {
            profile(src, des);
        }
    }
}
