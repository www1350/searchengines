package com.absurd.lucene.annotation;

import com.absurd.lucene.enums.Index;
import com.absurd.lucene.enums.Store;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:www_1350@163.com">王文伟</a>
 * @Title: searchengines
 * @Package com.absurd.lucene.annotation
 * @Description:
 * @date 2016/12/28 19:02
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SearchableId {
     String name();
     Store store() default Store.STORED;
     Index index() default Index.ANALYZER;
}
