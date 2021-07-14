package com.redmoon.oa.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.js.fan.util.StrUtil;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.CRF.CRFSegment;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.suggest.Suggester;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

public class HanLPUtil {
	
	public static List<String> segment(String content) {
		List<Term> list = HanLP.segment(content);
		List<String> l = new ArrayList<String>();
		Iterator<Term> ir = list.iterator();
		while (ir.hasNext()) {
			Term term = (Term)ir.next();
			l.add(term.word);
		}
		return l;
	}
	
    public static void main(String[] args) {
        System.out.println("首次编译运行时，HanLP会自动构建词典缓存，请稍候……\n");
        // 分词
        StringBuffer keywords = new StringBuffer();
        String xmmc = "中电智能卡有限责任公司"; // 防水材料研发生产项目
        List list = HanLPUtil.segment(xmmc);
        if (list.size()>=3) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                String word = (String)ir.next();
                if (word.equals("项目") || word.equals("有限公司")) {
                    continue;
                }

                if (word.length()==4) {
                    String word1 = word.substring(0, 2);
                    String word2 = word.substring(2);
                    StrUtil.concat(keywords, " ", "+" + word1 + " +" + word2);
                }
                else if (word.length()>=1) {
                    StrUtil.concat(keywords, " ", "+" + word);
                }
            }
            System.out.println(keywords);
        }


        //第一次运行会有文件找不到的错误但不影响运行，缓存完成后就不会再有了
        String str = "中电智能卡有限责任公司"; // "北京航空航天大学";
        System.out.println("标准分词：");
        System.out.println(HanLP.segment(str)); // "你好，欢迎使用HanLP！"));
        System.out.println("\n");

        List<Term> termList = NLPTokenizer.segment(str); // "中国科学院计算技术研究所的宗成庆教授正在教授自然语言处理课程");
        System.out.println("NLP分词：");
        System.out.println(termList);
        System.out.println("\n");

        List<Term> termList2 = NShortSegment.parse(str);
        System.out.println("短分词：");
        System.out.println(termList2);
        System.out.println("\n");

        DijkstraSegment ds = new DijkstraSegment();
        List<Term> termList3 = ds.seg(str);
        System.out.println("最短分词：");
        System.out.println(termList3);
        System.out.println("\n");

        CRFSegment cs = new CRFSegment();
        List<Term> termList4 = cs.seg(str);
        System.out.println("CRF分词：");
        System.out.println(termList4);
        System.out.println("\n");

        System.out.println("智能推荐：");
        getSegement();
        System.out.println("\n");

        System.out.println("关键字提取：");
        getMainIdea();
        System.out.println("\n");

        System.out.println("自动摘要：");
        getZhaiYao();
        System.out.println("\n");

        System.out.println("短语提取：");
        getDuanYu();
        System.out.println("\n");
    }

    /**
     * 智能推荐部分
     */
    public static void getSegement() {
        Suggester suggester = new Suggester();
        String[] titleArray = ("威廉王子发表演说 呼吁保护野生动物\n" + "《时代》年度人物最终入围名单出炉 普京马云入选\n" + "“黑格比”横扫菲：菲吸取“海燕”经验及早疏散\n"
                + "日本保密法将正式生效 日媒指其损害国民知情权\n" + "英报告说空气污染带来“公共健康危机”").split("\\n");
        for (String title : titleArray) {
            suggester.addSentence(title);
        }
        System.out.println(suggester.suggest("发言", 1)); // 语义
        System.out.println(suggester.suggest("危机公共", 1)); // 字符
        System.out.println(suggester.suggest("mayun", 1)); // 拼音
    }

    /**
     * 关键字提取
     */
    public static void getMainIdea() {
        // String content = "程序员(英文Programmer)是从事程序开发、维护的专业人员。一般将程序员分为程序设计人员和程序编码人员，但两者的界限并不非常清楚，特别是在中国。软件从业人员分为初级程序员、高级程序员、系统分析员和项目经理四大类。";
        // String content = "南京市同安智能科技有限公司";
        String content = "南京众元科技有限公司";
        List<String> keywordList = HanLP.extractKeyword(content, 5);
        System.out.println(keywordList);
    }

    /**
     * 自动摘要
     */
    public static void getZhaiYao() {
        String document = "算法可大致分为基本算法、数据结构的算法、数论算法、计算几何的算法、图的算法、动态规划以及数值分析、加密算法、排序算法、检索算法、随机化算法、并行算法、厄米变形模型、随机森林算法。\n"
                + "算法可以宽泛的分为三类，\n" + "一，有限的确定性算法，这类算法在有限的一段时间内终止。他们可能要花很长时间来执行指定的任务，但仍将在一定的时间内终止。这类算法得出的结果常取决于输入值。\n"
                + "二，有限的非确定算法，这类算法在有限的时间内终止。然而，对于一个（或一些）给定的数值，算法的结果并不是唯一的或确定的。\n"
                + "三，无限的算法，是那些由于没有定义终止定义条件，或定义的条件无法由输入的数据满足而不终止运行的算法。通常，无限算法的产生是由于未能确定的定义终止条件。";
        List<String> sentenceList = HanLP.extractSummary(document, 3);
        System.out.println(sentenceList);
    }

    /**
     * 短语提取
     */
    public static void getDuanYu() {
        String text = "算法工程师\n"
                + "算法（Algorithm）是一系列解决问题的清晰指令，也就是说，能够对一定规范的输入，在有限时间内获得所要求的输出。如果一个算法有缺陷，或不适合于某个问题，执行这个算法将不会解决这个问题。不同的算法可能用不同的时间、空间或效率来完成同样的任务。一个算法的优劣可以用空间复杂度与时间复杂度来衡量。算法工程师就是利用算法处理事物的人。\n"
                + "\n" + "1职位简介\n" + "算法工程师是一个非常高端的职位；\n" + "专业要求：计算机、电子、通信、数学等相关专业；\n"
                + "学历要求：本科及其以上的学历，大多数是硕士学历及其以上；\n" + "语言要求：英语要求是熟练，基本上能阅读国外专业书刊；\n"
                + "必须掌握计算机相关知识，熟练使用仿真工具MATLAB等，必须会一门编程语言。\n" + "\n" + "2研究方向\n"
                + "视频算法工程师、图像处理算法工程师、音频算法工程师 通信基带算法工程师\n" + "\n" + "3目前国内外状况\n"
                + "目前国内从事算法研究的工程师不少，但是高级算法工程师却很少，是一个非常紧缺的专业工程师。算法工程师根据研究领域来分主要有音频/视频算法处理、图像技术方面的二维信息算法处理和通信物理层、雷达信号处理、生物医学信号处理等领域的一维信息算法处理。\n"
                + "在计算机音视频和图形图像技术等二维信息算法处理方面目前比较先进的视频处理算法：机器视觉成为此类算法研究的核心；另外还有2D转3D算法(2D-to-3D conversion)，去隔行算法(de-interlacing)，运动估计运动补偿算法(Motion estimation/Motion Compensation)，去噪算法(Noise Reduction)，缩放算法(scaling)，锐化处理算法(Sharpness)，超分辨率算法(Super Resolution),手势识别(gesture recognition),人脸识别(face recognition)。\n"
                + "在通信物理层等一维信息领域目前常用的算法：无线领域的RRM、RTT，传送领域的调制解调、信道均衡、信号检测、网络优化、信号分解等。\n" + "另外数据挖掘、互联网搜索算法也成为当今的热门方向。\n"
                + "算法工程师逐渐往人工智能方向发展。";
        List<String> phraseList = HanLP.extractPhrase(text, 10);
        System.out.println(phraseList);
    }
}
