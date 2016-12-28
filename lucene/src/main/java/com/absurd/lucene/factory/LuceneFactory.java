package com.absurd.lucene.factory;

import com.absurd.lucene.annotation.Searchable;
import com.absurd.lucene.annotation.SearchableId;
import com.absurd.lucene.annotation.SearchableProperty;
import com.absurd.lucene.enums.Index;
import com.absurd.lucene.enums.Store;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author <a href="mailto:www_1350@163.com">王文伟</a>
 * @Title: searchengines
 * @Package com.absurd.lucene.factory
 * @Description:
 * @date 2016/12/28 19:13
 */
public class LuceneFactory {
    private final static Logger logger = LoggerFactory.getLogger(LuceneFactory.class);
    private static LuceneFactory factory;
    private static String indexPath;
    private static IndexWriterConfig.OpenMode openMode;

    private LuceneFactory() {
    }

    public static String getIndexPath() {
        return indexPath;
    }

    public static LuceneFactory setIndexPath(String indexPath) {
        LuceneFactory.indexPath = indexPath;
        return factory;
    }

    public  static LuceneFactory newInstance(){
        if (factory==null){
            InputStream inStream = null;
            Properties props = null;
            try {
                inStream = LuceneFactory.class.getClassLoader().getResourceAsStream("lucene.properties");
                props = new Properties();
                props.load(inStream);
                indexPath = props.getProperty("lucene.indexpath");
              String mode =  props.getProperty("lucene.openmode");
                if ("APPEND".equalsIgnoreCase(mode)){
                    openMode= IndexWriterConfig.OpenMode.APPEND;
                }else if("CREATE_OR_APPEND".equalsIgnoreCase(mode)){
                    openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
                }else{
                    openMode = IndexWriterConfig.OpenMode.CREATE;
                }

            } catch(FileNotFoundException e) {
                throw new RuntimeException("未找到search.properties配置文件",e);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new LuceneFactory();
        }
        return factory;
    }


    public <T> void index(T object,Class<T> clazz) {
        Searchable searchable = clazz.getAnnotation(Searchable.class);
        if (searchable == null) {
            throw new RuntimeException(clazz.getName() + ",此类无法创建索引, 请添加@Searchable注解");
        }
        String alias = searchable.alias();
        try {
            Directory dir = FSDirectory.open(Paths.get(indexPath + File.separator + alias));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(openMode);
//            iwc.setRAMBufferSizeMB(256.0);
            IndexWriter writer = new IndexWriter(dir, iwc);
            Document doc = new Document();
            java.lang.reflect.Field[] fields = clazz.getFields();
            for (java.lang.reflect.Field field : fields) {
                SearchableId searchableId = field.getAnnotation(SearchableId.class);
                SearchableProperty searchableProperty =  field.getAnnotation(SearchableProperty.class);
                if (searchableId==null && searchableProperty==null){
                    continue;
                }
                Object propertyValue = object.getClass().getField(field.getName()).get(object);
                Class<?> type = field.getType();
                if (searchableId != null) {
                    String name = searchableId.name();
                    Store store = searchableId.store();
                    Index index = searchableId.index();
                    if (type.equals(Integer.class)) {
                        doc.add(new IntPoint(name, (Integer)propertyValue));
                    }else if(type.equals(Long.class)){
                        doc.add(new LongPoint(name, (Long)propertyValue));
                    }
                }else if(searchableProperty!=null){
                    String name = searchableProperty.name();
                    Store store = searchableProperty.store();
                    Index index = searchableProperty.index();
                    Field pathField = null;
                    switch (store){
                        case STORED:
                            pathField =  new StringField(name, propertyValue.toString(), Field.Store.YES);
                            break;
                        case NOT_STORED:
                            pathField = new StringField(name, propertyValue.toString(), Field.Store.NO);
                            break;
                        default:
                            throw new RuntimeException(clazz.getName() + ",此类无法创建索引, 请添加Store");
                    }
                    doc.add(pathField);
                }

            }
            writer.addDocument(doc);
            writer.forceMerge(1);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


    }
