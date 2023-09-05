package cn.js.fan.util;

import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.net.URL;
import java.io.File;

public class CFGParser {

    public CFGParser() {
    }

    //定义一个Properties 用来存放 dbhost dbuser dbpassword的值
    private Properties props;

    //这里的props
    public Properties getProps() {
        return this.props;
    }

    public void parse(String filename) throws Exception {
        //将我们的解析器对象化
        CFGHandler handler = new CFGHandler();

        //获取SAX工厂对象
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);

        //获取SAX解析
        SAXParser parser = factory.newSAXParser();
        InputStream inputStream = null;
        try {
            Resource resource = new ClassPathResource(filename);
            inputStream = resource.getInputStream();
            parser.parse(inputStream, handler);
            props = handler.getProps();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
    }

    public void parseFile(String filename) throws Exception {
        //将我们的解析器对象化
        CFGHandler handler = new CFGHandler();

        //获取SAX工厂对象
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);

        //获取SAX解析
        SAXParser parser = factory.newSAXParser();

        File f = new File(filename);
        if (!f.exists()) {
            return;
        }

        //将解析器和解析对象myenv.xml联系起来,开始解析
        parser.parse(f, handler);
        //获取解析成功后的属性 以后 我们其他应用程序只要调用本程序的props就可以提取出属性名称和值了
        props = handler.getProps();
    }
}
