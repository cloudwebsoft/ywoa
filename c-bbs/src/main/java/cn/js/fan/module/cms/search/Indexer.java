package cn.js.fan.module.cms.search;

import java.io.*;
import java.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import jeasy.analysis.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.*;
import cn.js.fan.module.cms.Document;
import cn.js.fan.module.cms.DocContent;

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
public class Indexer {
    String indexStorageDir;

    public Indexer() {
        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
        indexStorageDir = Global.realPath + cfg.getProperty("cms.fullTextSearchDir");
        // System.out.print("cms.fullTextSearchDir=" + cfg.getProperty("cms.fullTextSearchDir"));
        File file = new File(indexStorageDir);
        if (!file.isDirectory()) {
            file.mkdirs();
        }
    }

    public int delDocument(int id) {
        int r = -1;
        IndexReader reader = null;
        try {
            reader = IndexReader.open(indexStorageDir);
            r = reader.deleteDocuments(new Term("id", "" + id));
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("delDocument:" + e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (Exception e) {}
        }
        return r;
    }

    public boolean index(Vector vt, boolean isIncrement) {
        boolean re = true;
        try {
            Iterator ir = vt.iterator();
            IndexWriter writer = new IndexWriter(indexStorageDir, getAnalyzer(), !isIncrement);
            cn.js.fan.module.cms.Document document = new cn.js.fan.module.cms.Document();
            DocContent docc = new DocContent();
            while (ir.hasNext()) {
                document = (cn.js.fan.module.cms.Document)ir.next();
                org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
                doc.add(new Field("id", Long.toString(document.getId()), Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("title", document.getTitle(), Field.Store.YES, Field.Index.TOKENIZED));
                int count = 0, i = 1;
                count = document.getPageCount();
                String content = "";
                while(count > 0 && i <= count){
                    docc = docc.getDocContent(document.getId(), i);
                    content += docc.getContent();
                    i++;
                }
                doc.add(new Field("content", content, Field.Store.YES, Field.Index.TOKENIZED));
                writer.addDocument(doc);
            }
            writer.optimize();
            writer.close();
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("index:" + e.getMessage());
        }
        return re;
    }

    public Analyzer getAnalyzer() {
        MMAnalyzer analyzer = new MMAnalyzer(2);
        return analyzer;
    }

    public Hits seacher(String queryString, String fieldName) {
        Hits hits = null;
        try {
            IndexSearcher is = null;
            is = new IndexSearcher(indexStorageDir);
            QueryParser parser = new QueryParser(fieldName, getAnalyzer());
            Query query = parser.parse(queryString);//检索词
            hits = is.search(query);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("seacher:" + e.getMessage());
            System.out.print(e);
        }
        return hits;
    }
}
