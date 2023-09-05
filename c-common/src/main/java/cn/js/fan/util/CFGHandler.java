package cn.js.fan.util;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import java.util.Properties;

//使用DefaultHandler的好处 是 不必陈列出所有方法,
public class CFGHandler extends DefaultHandler {

  //定义一个Properties 用来存放 dbhost dbuser dbpassword的值
  private Properties props;
  private String currentSet;
  private String currentName;
  private StringBuffer currentValue = new StringBuffer();

  //构建器初始化props
  public CFGHandler() {
    this.props = new Properties();
  }

  public Properties getProps() {
    return this.props;
  }

  //定义开始解析元素的方法. 这里是将<xxx>中的名称xxx提取出来.
  @Override
  public void startElement(String uri, String localName, String qName,
                           Attributes attributes) throws SAXException {
    currentValue.delete(0, currentValue.length());
    this.currentName = qName;
  }

  //这里是将<xxx></xxx>之间的值加入到currentValue
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    currentValue.append(ch, start, length);
  }

  //在遇到</xxx>结束后,将之前的名称和值一一对应保存在props中
  @Override
  public void endElement(String uri, String localName, String qName) throws
      SAXException {
    props.put(qName.toLowerCase(), currentValue.toString().trim());
  }

}
