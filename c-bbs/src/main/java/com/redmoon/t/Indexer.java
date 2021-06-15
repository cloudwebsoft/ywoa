package com.redmoon.t;

/**
 * Create a Lucene index.
 */
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
import com.redmoon.forum.person.UserMgr;

public class Indexer {
    String indexStorageDir;

    public Indexer() {
        Config cfg = Config.getInstance();
        indexStorageDir = Global.realPath + cfg.getProperty("t.fullTextSearchDir");
        File file = new File(indexStorageDir);
        if (!file.isDirectory()) {
            file.mkdirs();
        }
    }

    public int delDocument(long id) {
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

    public boolean index(Vector vt, boolean isIncrement ) {
        boolean re = true;
        try {
            Iterator ir = vt.iterator();
            IndexWriter writer = new IndexWriter(indexStorageDir, getAnalyzer(), !isIncrement);
            TMsgDb md = new TMsgDb();
            UserMgr um = new UserMgr();
            while (ir.hasNext()) {
                md = (TMsgDb)ir.next();
                Document doc = new Document();
                doc.add(new Field("id", Long.toString(md.getLong("id")), Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("content", md.getString("content"), Field.Store.YES, Field.Index.TOKENIZED));
                String nick = um.getUser(md.getString("user_name")).getNick();
                if (nick==null)
                    nick = "";
                doc.add(new Field("nick", nick, Field.Store.YES, Field.Index.TOKENIZED));
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
            IndexSearcher is = new IndexSearcher(indexStorageDir);
            QueryParser parser = new QueryParser(fieldName, getAnalyzer());
            Query query = parser.parse(queryString);//检索词
            hits = is.search(query);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("seacher:" + e.getMessage());
            e.printStackTrace();
        }
        return hits;
    }
}
