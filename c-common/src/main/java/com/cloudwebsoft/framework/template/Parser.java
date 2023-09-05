package com.cloudwebsoft.framework.template;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.plugin.*;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: 模板解析器</p>
 *
 * <p>Description: </p>
 * 文章列表
 * <!-- begin.list.doc dirCode=xxb-->
 * <table><tr><td>#title(20)</td><td>#modifiedDate(yyyy-MM-dd)</td></tr></table>
 * <!-- end.list.doc-->
 * $表示表示域变量
 * @表示子域变量
 * {$doc.dirCode(code).summary} 提取文章的摘要
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Parser {

    public Parser() {
    }

    static final Pattern varPat = Pattern.compile("\\$(\\S+)",
                                                  Pattern.DOTALL |
                                                  Pattern.CASE_INSENSITIVE);
    static final Pattern beginPat = Pattern.compile(
            "<!--\\s*begin:(\\S+)\\s*(.*?)-->",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    static final Pattern fieldVarPat = Pattern.compile("@(\\S+)",
            Pattern.DOTALL |
            Pattern.CASE_INSENSITIVE);
    static final Pattern endPat = Pattern.compile(
            "<!--\\s*?end:(\\S+)(.*?)-->",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    static final Pattern patArray[] = {
                                      varPat,
                                      beginPat,
                                      fieldVarPat,
                                      endPat
    };

    static final int tokenTypeArray[] = {Token.VAR, Token.BEGIN, Token.FIELD,
                                        Token.END};

    /**
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public ITemplate parse(String fileName) throws IOException,ErrMsgException {
        // LogUtil.getLog(getClass()).info("parse:fileName=" + fileName);

        return parse(fileName, "utf-8");
    }

    /**
     * parse a file to template
     *
     * @param fileName String
     * @param charsetName String
     * @throws IOException
     * @return ITemplate
     */
    public ITemplate parse(String fileName, String charsetName) throws
            IOException,ErrMsgException {
        FileInputStream fileStream = new FileInputStream(fileName);
        // LogUtil.getLog(getClass()).info("parse:charsetName=" + charsetName);

        ITemplate template = null;
        try {
            template = parse(fileStream, charsetName);
        }
        finally {
            fileStream.close();
        }
        return template;
    }

    /**
     *
     * @param stream
     * @param charsetName
     * @return
     * @throws IOException
     */
    public ITemplate parse(InputStream stream, String charsetName) throws
            IOException,ErrMsgException {
        if (charsetName == null) charsetName = "utf-8";
        InputStreamReader streamReader = new InputStreamReader(stream,
                charsetName);
        BufferedReader reader = new BufferedReader(streamReader);
        ITemplate template = null;
        try {
            template = parse(reader);
        }
        finally {
            streamReader.close();
            reader.close();
        }
        return template;
    }

    public ITemplate parse(Reader reader) throws
            IOException,ErrMsgException {
        BufferedReader bufreader = new BufferedReader(reader);
        ITemplate template = null;
        try {
            template = parse(bufreader);
        }
        finally {
            reader.close();
            bufreader.close();
        }
        return template;
    }

    public ListPart getListPartByName(String line) {
        // LogUtil.getLog(getClass()).info("getListPartByName: line=" + line);
        Matcher m = beginPat.matcher(line);
        boolean result = m.find();
        String name = "";
        String propStr = "";
        // 解析属性
        if (result) {
            name = m.group(1);
            if (m.groupCount() >= 2)
                propStr = m.group(2);
        }
        PluginMgr pm = new PluginMgr();
        ListPart lp = (ListPart) pm.getPluginUnit(name).getITemplate();
        // LogUtil.getLog(getClass()).info("getListPartByName: propStr=" + propStr);
        if (!propStr.equals(""))
            lp.parseProps(propStr);
        return lp;
    }

    public VarPart getVarPartByNameString(String nameStr) throws ErrMsgException {
        // $doc.id(id).title 提取文章的标题
        // nameStr = "aaa$doc.id(33).title(len=20,date=yyyy-MM-dd HH:mm:ss,urlEncode=true) ddddddddddf $doc.id(66).title";
        // nameStr = "aaa$doc.id(request.id).title(len=20,date=yyyy-MM-dd HH:mm:ss,urlEncode=true) ddddddddddf $doc.id(66).title";
        // nameStr = "aaa$doc.id(request.id).title(page=1,len=20,date=yyyy-MM-dd HH:mm:ss,urlEncode=true) ddddddddddf $doc.id(66).title";
        // Pattern varNamePat = Pattern.compile("\\$(\\S+)\\.(\\S+)\\((\\S+)\\)\\.(\\S+)",  Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Pattern varNamePat = Pattern.compile(
                "\\$(\\S+)\\.(\\S+)\\((\\S+)\\)\\.([^\\(]+)?(\\((.*?)\\))?",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = varNamePat.matcher(nameStr);
        if (m.find()) {
            if (m.groupCount() < 5) {
                throw new IllegalArgumentException(nameStr +
                        " is invalid! Regexp match group count is " +
                        m.groupCount());
            }
            String name = m.group(1);
            String keyName = m.group(2);
            String keyValue = m.group(3);
            String field = m.group(4);
            String props = "";
            if (m.groupCount() >= 6) {
                props = m.group(6);
            }
            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnit(name);
            if (pu == null)
                throw new IllegalArgumentException(name +
                        " is not found in plugin units. Name string is " +
                        nameStr);
            VarPart vp = (VarPart) pu.getITemplate();
            if (vp==null)
                throw new IllegalArgumentException(
                        " ITemplate is not found. Name string is " +
                        nameStr);
            vp.setName(name);
            vp.setKeyName(keyName);
            vp.setKeyValue(keyValue);
            vp.setField(field);
            if (props != null && !props.equals(""))
                vp.parseProps(props);
            return vp;
        } else {
            // {$Global.AppName}
            Pattern varNamePat2 = Pattern.compile(
                    // "\\$(\\S+)\\.([^\\(]+)(\\((.*?)\\))?", // 这样会因为贪婪模式，致{$cms.include(template/column.htm)}中，得到的name=cms.include(template/column
                    "\\$([A-Z0-9a-z]+)\\.([^\\(]+)(\\((.*?)\\))?",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            m = varNamePat2.matcher(nameStr);
            if (m.find()) {
                if (m.groupCount() < 2) {
                    throw new IllegalArgumentException(nameStr +
                            " is invalid! varNamePat2 match group count is " +
                            m.groupCount());
                }
                String name = m.group(1);
                String field = m.group(2);
                String props = "";
                if (m.groupCount() >= 4) {
                    props = m.group(4);
                }
                PluginMgr pm = new PluginMgr();

                /*
                LogUtil.getLog(getClass()).info(
                        "getVarPartByNameString: nameStr=" +
                        nameStr + " name=" + name + " props=" + props);
                */
               PluginUnit pu = pm.getPluginUnit(name);
               if (pu == null)
                   throw new ErrMsgException("Plugin unit of " + name +
                                             " is not found!");
               VarPart vp = (VarPart) pu.getITemplate();
               if (vp == null)
                   throw new ErrMsgException("ITemplate of " + name +
                                             " is not found!");
               vp.setName(name);
               vp.setField(field);
               if (props != null && !props.equals(""))
                   vp.parseProps(props);
               return vp;
            } else
                throw new IllegalArgumentException(nameStr + " is invalid!");
        }
    }

    public Token parseLine(String line) {
        Token token = new Token();

        // if (line.indexOf("--") != -1)
        //    LogUtil.getLog(getClass()).info("parseLine:" + line);

        if (line.startsWith("<!--") && line.endsWith("-->")) {
            int commentBeginPos = line.indexOf("<!--") + "<!--".length();
            int commentEndPos = line.indexOf("-->", commentBeginPos);

            String tag = line.substring(commentBeginPos, commentEndPos).trim();
            tag = line;
            // LogUtil.getLog(getClass()).info("parseLine:" + tag);
            int nPatterns = patArray.length;
            Matcher m = null;
            for (int i = 0; i < nPatterns; i++) {
                Pattern pattern = patArray[i];
                m = pattern.matcher(tag);
                if (m.find()) {
                    token.setType(tokenTypeArray[i]);
                    break;
                }
            }

            if (token.getType() == Token.BEGIN) { // valid
                token.setName(m.group(1));
            } else if (token.getType() == Token.END) {
                token.setName(m.group(1));
            }

            return token;
        }

        int begin = 0;
        List posPairs = null;

        // 解析如下格式
        // ........{....{.........}....}...{..........}....... etc
        //              ^         ^         ^          ^
        //              begin     end       begin      end
        while (true) {
            int lBracketPos = line.indexOf("{", begin);
            if (lBracketPos < 0)break;

            int rBracketPos = line.indexOf("}", lBracketPos);
            if (rBracketPos < 0)break;

            // pass the nested { { {, use the last one.
            int nestedLeftBracketPos = line.indexOf("{", lBracketPos + 1);
            while (nestedLeftBracketPos >= 0 &&
                   nestedLeftBracketPos < rBracketPos) {
                lBracketPos = nestedLeftBracketPos;
                nestedLeftBracketPos = line.indexOf("{", lBracketPos + 1);
            }

            if (posPairs == null) {
                posPairs = new ArrayList();
            }

            PosPair posPair = new PosPair();
            posPair.begin = lBracketPos;
            posPair.end = rBracketPos;

            posPairs.add(posPair);

            begin = rBracketPos + 1;
        }

        if (posPairs != null) {
            token.type = Token.hasVar;
            token.posPairs = posPairs;
        }
        return token;
    }

    public ITemplate parse(BufferedReader reader) throws IOException,ErrMsgException {
        // LogUtil.getLog(getClass()).info("parse:" + reader);

        StringBuffer staticLines = new StringBuffer();
        Stack stack = new Stack(); // 用于ListPart的嵌套解析
        ListPart top = new ListPart(ListPart.TOP, ListPart.ROOT); // 根节点

        StaticPart staticPart = null;

        int lineNo = 0; // 记录行号
        String commentPart = null;

        String line = null;
        while ((line = reader.readLine()) != null) {
            lineNo++;

            // LogUtil.getLog(getClass()).info("parse:" + line);

            // 注释部分
            int commentBegin = line.indexOf("<!--");
            // 找到注释的起始部分
            if (commentBegin >= 0) {
                boolean isComment = false;
                if (commentBegin > 0) {
                    String lineBefore = null;
                    lineBefore = line.substring(0, commentBegin);
                    staticLines.append(lineBefore + "\n");
                }

                StringBuffer commentBuf = new StringBuffer();
                String commentLine = line;
                int commentEnd = line.indexOf("-->");
                // 该行不包含注释的结尾
                if (commentEnd < 0) {
                    commentBuf.append(line.substring(commentBegin) + "\n");
                    commentBegin = 0;
                }

                // 找到注释的结尾
                while (commentEnd < 0) {
                    commentLine = reader.readLine();
                    lineNo++;
                    if (commentLine == null)
                        break; // 没找到结尾
                    commentEnd = commentLine.indexOf("-->");
                    // 找到，退出循环
                    if (commentEnd >= 0) {
                        isComment = true;
                        commentBuf.append(commentLine.substring(0, commentEnd + "-->".length()));
                        break;
                    }
                    commentBuf.append(commentLine + "\n");
                }

                // LogUtil.getLog(getClass()).info("commentPart:" + commentBuf);

                staticLines.append(commentBuf);

                if (isComment) {
                    if (commentEnd < commentLine.length()-1) {
                        line = commentLine.substring(commentEnd + "-->".length()) + "\n";
                    }
                    else
                        continue;
                }
            }

            // parse a line
            Token token = parseLine(line);

            // LogUtil.getLog(getClass()).info("name=" + token.getName() +
            //                                " type=" + token.getType());

            // normal line, put it to static lines buffer
            if (token.getType() == Token.NONE) {
                staticLines.append(line + "\n");
                continue;
            }

            // line中含有变量
            if (staticLines.length() > 0) {
                staticPart = new StaticPart(staticLines.toString());
                top.addStep(staticPart);
                staticLines.setLength(0); // clean the static lines buf.
            }

            switch (token.getType()) {
            case Token.BEGIN:
                ListPart listPart = (ListPart) getListPartByName(line); // token.getName());
                // LogUtil.getLog(getClass()).info("listPart:" + listPart);
                listPart.setName(token.getName());
                listPart.setParentName(top.getName());
                top.addStep(listPart);
                stack.push(top);
                top = listPart;
                break;
            case Token.END:
                if (!top.getName().equals(token.getName())) {
                    throw new IOException("line " + lineNo + ": End : " +
                                          top.getName() + " instead of " +
                                          token.getName() + " is expected.");
                }
                top = (ListPart) stack.pop();
                if (top == null) {
                    throw new IOException("line " + lineNo +
                                          ": End Dynamic: top = null, why?");
                }
                break;
            case Token.hasVar: // this line contains {..}
                List posPairs = token.posPairs;

                // ........{....{.........}....}.....{..........}....... etc
                //              ^         ^         ^          ^
                // StaticPart   VariablePart  Static   Variable   Static etc
                if (posPairs != null) {
                    // 判断是否为分页符所在的行
                    PaginatorPart pp = null;
                    if (line.indexOf("paginator") != -1) {
                        pp = new PaginatorPart();
                        pp.setParentName(top.getName());
                        top.addStep(pp);
                    }

                    int nPairs = posPairs.size();

                    int begin = 0;
                    int end = line.length() - 1;

                    for (int k = 0; k < nPairs; k++) {
                        PosPair posPair = (PosPair) posPairs.get(k);

                        if (begin < posPair.begin) {
                            staticPart =
                                    new StaticPart(line.substring(begin,
                                    posPair.begin));
                            if (pp == null)
                                top.addStep(staticPart);
                            else
                                pp.addStep(staticPart);
                        }

                        String varStr = line.substring(posPair.begin + 1,
                                posPair.end);

                        if (varPat.matcher(varStr).find()) {
                            token.setType(Token.VAR);
                            VarPart varPart = getVarPartByNameString(varStr);
                            if (pp == null)
                                top.addStep(varPart);
                            else
                                pp.addStep(varPart);
                        } else if (fieldVarPat.matcher(line).find()) {
                            token.setType(Token.FIELD);

                            FieldPart fieldPart =
                                    new FieldPart(varStr);
                            // LogUtil.getLog(getClass()).info("fieldPart name=" +
                            //        fieldPart.getName());
                            if (pp == null)
                                top.addStep(fieldPart);
                            else
                                pp.addStep(fieldPart);
                        } else {
                            if (pp == null) {
                                top.addStep(new StaticPart(line.substring(
                                        posPair.
                                        begin,
                                        posPair.end + 1)));
                            } else {
                                pp.addStep(new StaticPart(line.substring(
                                        posPair.
                                        begin,
                                        posPair.end + 1)));
                            }
                        }
                        begin = posPair.end + 1;
                    }

                    String tail = "\n";
                    if (begin <= end) {
                        tail = line.substring(begin, end + 1) + "\n";
                    }
                    staticPart = new StaticPart(tail);
                    if (pp == null)
                        top.addStep(staticPart);
                    else
                        pp.addStep(staticPart);
                }
                break;
            }
        }

        if (stack.size() > 0) {
            ListPart left = (ListPart) stack.pop();
            throw new IOException("line " + lineNo + ": END " +
                                  left.getName() +
                                  " is expected but not found.");
        }

        if (staticLines.length() > 0) {
            staticPart = new StaticPart(staticLines.toString());
            top.addStep(staticPart);
        }

        return top;
    }

    public static void main(String[] args) {
        Parser p = new Parser();
        String html =
                "bbbb<!-- begin:list.doc dirCode=first start=0 end=3-->a@abc aa<!--begin:doccc_list id=9-->";
        Matcher m = beginPat.matcher(html);

        /*
               html = "dd$doc.id(request.id).content xx";
               html = "$doc.id(1).content";

               if (varPat.matcher(html).find()) {
                   LogUtil.getLog(getClass()).info("matched");
               }
               Matcher m = varPat.matcher(html);


                 Pattern bgeinPat = Pattern.compile(
                "<!--\\s*begin:(\\S+)\\s*(.*?)-->", Pattern.DOTALL |
                Pattern.CASE_INSENSITIVE);

                 Pattern fieldPat = Pattern.compile("@(\\S+)", Pattern.DOTALL |
                                           Pattern.CASE_INSENSITIVE);
                 Matcher m = fieldPat.matcher(html);
         */
        Pattern varNamePat2 = Pattern.compile(
                "\\@([^\\(\\.]+)(\\.([^\\(]+))?(\\((.*?)\\))?",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        String fieldString = "@paginator.total(len=2)";
        fieldString = "@total";
        m = varNamePat2.matcher(fieldString);

        boolean result = m.find();
        while (result) {
            // 有四个子串name title size value
            for (int i = 1; i <= m.groupCount(); i++) {
                LogUtil.getLog(Parser.class).info("第" + i + "组的子串内容为： " + m.group(i));
            }
            result = m.find();
        }

        varNamePat2 = Pattern.compile(
                "\\@([^\\(\\.]+)(\\.([^\\(]+))?(\\((.*?)\\))?",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        fieldString = "@paginator.total(len=2)";
        fieldString = "@total";
        m = varNamePat2.matcher(fieldString);

        result = m.find();
        while (result) {
            // 有四个子串name title size value
            for (int i = 1; i <= m.groupCount(); i++) {
                LogUtil.getLog(Parser.class).info("第" + i + "组的子串内容为： " + m.group(i));
            }
            result = m.find();
        }

        // getVarPartByNameString("");
    }

    /**
     * 找出变量的位置
     * .............{.........}.........{..........}....... etc
     *              ^         ^         ^          ^
     *              begin     end       begin      end
     */
    static class PosPair {
        int begin = 0;
        int end = 0;
    };
}
