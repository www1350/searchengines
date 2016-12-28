package com.absurd.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:www_1350@163.com">王文伟</a>
 * @Title: searchengines
 * @Package com.absurd.lucene
 * @Description:
 * @date 2016/12/28 13:54
 */
public class SimpleTest {
  private final static   Logger logger = LoggerFactory.getLogger(SimpleTest.class);

    @Test
    public void indexAndSearch() throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer();

        // Store the index in memory:
        Directory directory = new RAMDirectory();
        // To store an index on disk, use this instead:
//        Directory directory = FSDirectory.open("/tmp/testindex");

        ///
        // 1.Create Documents by adding Fields;
        Document doc = new Document();
        String text = "This is the text to be indexed.";
        doc.add(new Field("fieldname", text, TextField.TYPE_STORED));

        ///
        // 2.Create an IndexWriter and add documents to it with addDocument();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        iwriter.addDocument(doc);
        iwriter.close();

        ///
        // 3.Call QueryParser.parse() to build a query from a string;
        // Parse a simple query that searches for "text":
        QueryParser parser = new QueryParser("fieldname", analyzer);
        Query query = parser.parse("text");


        ///
        // 4.Create an IndexSearcher and pass the query to its search() method.
        // Now search the index:
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        ScoreDoc[] hits = isearcher.search(query,1000).scoreDocs;
        assertEquals(1, hits.length);
        // Iterate through the results:
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = isearcher.doc(hits[i].doc);
            assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
        }
        ireader.close();
        directory.close();
    }

    @Test
    public void indexTest() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();

        Directory dir = FSDirectory.open(Paths.get("index"));
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwc.setRAMBufferSizeMB(256.0);
        IndexWriter writer = new IndexWriter(dir, iwc);

        Path docDir = Paths.get("doc");
        indexDocs(writer, docDir);
        writer.forceMerge(1);
        writer.close();

    }

    static void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    logger.info(file.toString());
                    indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            Document doc = new Document();
            Field pathField = new StringField("path", file.toString(), Field.Store.YES);
            doc.add(pathField);
            doc.add(new LongPoint("modified", lastModified));
            doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                writer.addDocument(doc);
            }else{
                writer.updateDocument(new Term("path", file.toString()), doc);
                logger.info(file.toString());
            }
        }
    }



    @Test
    public void searchTest() throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
        IndexSearcher searcher = new IndexSearcher(reader);
//        BufferedReader in = null;
//        in = Files.newBufferedReader(Paths.get("index"), StandardCharsets.UTF_8);
        QueryParser parser = new QueryParser("contents", analyzer);
        Query query = parser.parse("数据类型");
/*        Date start = new Date();
            searcher.search(query, 100);
        Date end = new Date();
        logger.info("Time: "+(end.getTime()-start.getTime())+"ms");*/
        doPagingSearch( searcher, query, 1);
        reader.close();
    }

    static void doPagingSearch(IndexSearcher searcher, Query query,
                                     int hitsPerPage) throws IOException {
        TopDocs results = searcher.search(query, 5 * hitsPerPage);
        ScoreDoc[] hits = results.scoreDocs;
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = searcher.doc(hits[i].doc);
            logger.info(hitDoc.toString());
           logger.info(hitDoc.get("path"));
        }
    }
}
