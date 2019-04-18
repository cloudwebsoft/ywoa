package com.redmoon.forum.search;

/**
 * Create a Lucene index.
 */
import java.io.*;
import java.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.Config;
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
        indexStorageDir = Global.realPath + cfg.getProperty("forum.fullTextSearchDir");
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
            MsgDb md = new MsgDb();
            UserMgr um = new UserMgr();
            while (ir.hasNext()) {
                md = (MsgDb)ir.next();
                Document doc = new Document();
                doc.add(new Field("id", Long.toString(md.getId()), Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("title", md.getTitle(), Field.Store.YES, Field.Index.TOKENIZED));
                doc.add(new Field("content", md.getContent(), Field.Store.YES, Field.Index.TOKENIZED));
                String nick = um.getUser(md.getName()).getNick();
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
            IndexSearcher is = null;
            is = new IndexSearcher(indexStorageDir);
            QueryParser parser = new QueryParser(fieldName, getAnalyzer());
            Query query = parser.parse(queryString);//检索词
            hits = is.search(query);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("seacher:" + e.getMessage());
            e.printStackTrace();
        }
        return hits;
    }


    /*
    //upfile下文件索引
    public static String FileReaderAll(String FileName, String charset) throws
            IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(FileName), charset));
        String line = new String();
        String temp = new String();

        while ((line = reader.readLine()) != null) {
            temp += line;
        }
        reader.close();
        return temp;
    }

    public boolean fileIndexer() throws IOException {
        boolean re = true;
        try {
            File fileDir = new File(Global.realPath + "forum/upfile");
            IndexWriter indexWriter= new IndexWriter(Global.realPath + "search/forum/txt", getAnalyzer(), true);
            File[] files = fileDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                //System.out.print("files[i].getName()=" + files[i].getName() + ";files[i].getPath()=" + files[i].getPath());
                if (files[i].isFile()) {
                    String temp = FileReaderAll(files[i].getCanonicalPath(), "GBK");
                    Document document = new Document();
                    System.out.print("path=" + files[i].getPath());
                    System.out.print("temp=" + temp);
                    Field FieldPath = new Field("path", files[i].getPath(), Field.Store.YES, Field.Index.NO);
                    Field FieldBody = new Field("body", temp, Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
                    document.add(FieldPath);
                    document.add(FieldBody);
                    indexWriter.addDocument(document);

                }
            }
            indexWriter.optimize();
            indexWriter.close();
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("index:" + e.getMessage());
        }
        return re;
    }
    */
}
