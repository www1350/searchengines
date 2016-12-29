package com.absurd.lucene.factory;

import com.absurd.lucene.annotation.Searchable;
import com.absurd.lucene.annotation.SearchableId;
import com.absurd.lucene.annotation.SearchableProperty;
import com.absurd.lucene.enums.Index;
import com.absurd.lucene.enums.Store;
import com.alibaba.fastjson.JSON;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            indexSingle(clazz, writer, object);
            writer.forceMerge(1);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }  catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    public <T> void index(Collection<T> collection, Class<T> clazz) {
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
            for (T object : collection) {
                indexSingle(clazz, writer, object);
            }
                writer.forceMerge(1);
                writer.close();
            } catch(IOException e){
                e.printStackTrace();
            } catch(IllegalAccessException e){
                e.printStackTrace();
            } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private <T> void indexSingle(Class<T> clazz, IndexWriter writer, T object) throws IntrospectionException, IllegalAccessException, InvocationTargetException, IOException {
        Document doc = new Document();
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            SearchableId searchableId = field.getAnnotation(SearchableId.class);
            SearchableProperty searchableProperty = field.getAnnotation(SearchableProperty.class);
            if (searchableId == null && searchableProperty == null) {
                continue;
            }
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(),clazz);
            Method methodGetX = pd.getReadMethod();//Read对应get()方法
            Object propertyValue =  methodGetX.invoke(object);
            Class<?> type = field.getType();
            Field pathField = null;
            if (searchableId != null) {
                String name = searchableId.name();
                Store store = searchableId.store();
                switch (store) {
                    case STORED:
                        pathField = new StringField(name, propertyValue.toString(), Field.Store.YES);
                        break;
                    case NOT_STORED:
                        pathField = new StringField(name, propertyValue.toString(), Field.Store.NO);
                        break;
                    default:
                        throw new RuntimeException(clazz.getName() + ",此类无法创建索引, 请添加Store");
                }
                doc.add(pathField);
/*                        if (type.equals(Integer.class)) {
                    doc.add(new IntPoint(name, (Integer) propertyValue));
                } else if (type.equals(Long.class)) {
                    doc.add(new LongPoint(name, (Long) propertyValue));
                }*/
            } else if (searchableProperty != null) {
                String name = searchableProperty.name();
                Store store = searchableProperty.store();
                switch (store) {
                    case STORED:
                        pathField = new StringField(name, JSON.toJSONString(propertyValue), Field.Store.YES);
                        break;
                    case NOT_STORED:
                        pathField = new StringField(name, JSON.toJSONString(propertyValue), Field.Store.NO);
                        break;
                    default:
                        throw new RuntimeException(clazz.getName() + ",此类无法创建索引, 请添加Store");
                }
                doc.add(pathField);
            }

        }
        writer.addDocument(doc);
    }

    public <T> T searchById(Long id,Class<T> clazz){
        T obj =null;
        Searchable searchable = clazz.getAnnotation(Searchable.class);
        if (searchable == null) {
            throw new RuntimeException(clazz.getName() + ",此类无法索引, 请添加@Searchable注解");
        }
        String alias = searchable.alias();

        Analyzer analyzer = new StandardAnalyzer();
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath + File.separator + alias)));
            IndexSearcher searcher = new IndexSearcher(reader);

            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            Map<String,String> fieldNames = new HashMap<>();
            Document hitDoc=null;
            for (java.lang.reflect.Field field : fields) {
                SearchableId searchableId = field.getAnnotation(SearchableId.class);
                SearchableProperty searchableProperty = field.getAnnotation(SearchableProperty.class);
                if (searchableId != null) {
                    String name = searchableId.name();
                    fieldNames.put(name,field.getName());
                    QueryParser parser = new QueryParser(name, analyzer);
                    Query query = parser.parse(id.toString());
                    TopDocs results = searcher.search(query,1);
                    ScoreDoc[] hits = results.scoreDocs;
                    if (hits.length>0) {
                        hitDoc   =    searcher.doc(hits[0].doc);
                    }
                }else if (searchableProperty != null) {
                    String name = searchableProperty.name();
                    Store store = searchableProperty.store();
                    fieldNames.put(name,field.getName());
                }
            }
            obj = clazz.newInstance();
            if (hitDoc!=null ){
                for (Map.Entry<String,String> entry:fieldNames.entrySet()){
                    PropertyDescriptor pd = new PropertyDescriptor(entry.getValue(),clazz);
                    Method methodSetX = pd.getWriteMethod();//Read对应get()方法
                  String typeName=  pd.getPropertyType().getTypeName();
                    logger.warn(typeName);
                    if ("java.lang.Integer".equalsIgnoreCase(typeName))
                        methodSetX.invoke(obj, Integer.valueOf(hitDoc.get(entry.getKey())));
                    else if("java.lang.Long".equalsIgnoreCase(typeName))
                        methodSetX.invoke(obj, Long.valueOf(hitDoc.get(entry.getKey())));
/*                    else if("java.lang.String".equalsIgnoreCase(typeName))
                        methodSetX.invoke(obj, hitDoc.get(entry.getKey()));*/
                    else
                        methodSetX.invoke(obj, JSON.parseObject(hitDoc.get(entry.getKey()),pd.getPropertyType()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return obj;

    }


    }
