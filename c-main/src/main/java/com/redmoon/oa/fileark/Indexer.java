package com.redmoon.oa.fileark;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import cn.js.fan.db.Conn;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.*;

import com.cloudwebsoft.framework.util.*;
import jeasy.analysis.*;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.*;

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
    transient Logger logger = Logger.getLogger(Document.class.getName());

    public Indexer() {
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        indexStorageDir = Global.realPath + cfg.get("fullTextSearchDir");
        // System.out.print("cms.fullTextSearchDir=" + cfg.getProperty("cms.fullTextSearchDir"));
        File file = new File(indexStorageDir);
        if (!file.exists()) {
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
            } catch (Exception e) {
            }
        }
        return r;
    }

    public boolean index(Document document, boolean isIncrement) {
        boolean re = true;
        try {
            IndexWriter writer = new IndexWriter(indexStorageDir, getAnalyzer(), !isIncrement);
            DocContent docc = new DocContent();
            org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
            doc.add(new Field("id", Long.toString(document.getId()), Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("title", document.getTitle(), Field.Store.YES, Field.Index.TOKENIZED));

            int count = 0, i = 1;
            count = document.getPageCount();
            String content = "";
            while (count > 0 && i <= count) {
                docc = docc.getDocContent(document.getId(), i);
                content += docc.getContent();
                i++;
            }
            doc.add(new Field("content", content, Field.Store.YES, Field.Index.TOKENIZED));
            writer.addDocument(doc);

            writer.optimize();
            writer.close();
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("index:" + StrUtil.trace(e));
        }
        return re;
    }

    public boolean indexSql(String sql, boolean isIncrement) {
        boolean re = true;
        try {
            IndexWriter writer = null;
            try {
                writer = new IndexWriter(indexStorageDir, getAnalyzer(), !isIncrement);
            } catch (IOException e) {
                writer = new IndexWriter(indexStorageDir, getAnalyzer(), isIncrement);
            }
            DocContent docc = new DocContent();

            ResultSet rs = null;
            String connname = Global.getDefaultDB();
            if (connname.equals("")) {
                logger.info("Document:默认数据库名为空！");
            }
            Conn conn = new Conn(connname);
            try {
                Directory dir = new Directory();
                rs = conn.executeQuery(sql);
                if (rs != null) {
                    while (rs.next()) {
                        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
                        Document document = new Document();
                        Leaf lf = dir.getLeaf(rs.getString(2));
                        if (lf == null) {
                            continue;
                        }
                        if (lf.isFulltext()) {
                            document = document.getDocument(rs.getInt(1));
                            doc.add(new Field("id", Long.toString(document.getId()), Field.Store.YES, Field.Index.TOKENIZED));
                            doc.add(new Field("title", document.getTitle(), Field.Store.YES, Field.Index.TOKENIZED));

                            int count = 0, i = 1;
                            count = document.getPageCount();
                            String content = "";
                            while (count > 0 && i <= count) {
                                docc = docc.getDocContent(document.getId(), i);
                                content += docc.getContent();
                                i++;
                            }
                            doc.add(new Field("content", content, Field.Store.YES, Field.Index.TOKENIZED));
                            writer.addDocument(doc);
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("index:" + e.getMessage());
                return false;
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }

            writer.optimize();
            writer.close();
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("index:" + StrUtil.trace(e));
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
            Query query = parser.parse(queryString);
            hits = is.search(query);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("seacher:" + e.getMessage());
            System.out.print(e);
        }
        return hits;
    }

    /**
     * @param siteCode
     * @param queryString
     * @param fieldName
     * @return
     */
    public Hits seacherSite(String siteCode, String queryString, String fieldName) {
        Hits hits = null;
        try {
            IndexSearcher is = new IndexSearcher(indexStorageDir);
            String[] fields = {fieldName, "siteCode"};
            BooleanClause.Occur[] flags = {BooleanClause.Occur.MUST,
                    BooleanClause.Occur.MUST};
            Query query = MultiFieldQueryParser.parse(new String[]{queryString, siteCode}, fields, flags, getAnalyzer());
            hits = is.search(query);

        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("seacher:" + StrUtil.trace(e));
        }
        return hits;
    }
}
