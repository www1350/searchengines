package com.absurd.lucene.dto;

import com.absurd.lucene.annotation.Searchable;
import com.absurd.lucene.annotation.SearchableId;
import com.absurd.lucene.annotation.SearchableProperty;
import com.absurd.lucene.enums.Index;
import com.absurd.lucene.enums.Store;

import java.io.Serializable;

/**
 * @author <a href="mailto:www_1350@163.com">王文伟</a>
 * @Title: searchengines
 * @Package com.absurd.lucene.dto
 * @Description:
 * @date 2016/12/29 15:49
 */
@Searchable(alias = "article")
public class ArticleDTO implements Serializable {
    @SearchableId(name="id",store = Store.STORED,index = Index.ANALYZER)
    private Long id;
    @SearchableProperty(name="title",store = Store.STORED,index = Index.ANALYZER)
    private String title;
    @SearchableProperty(name="body",store = Store.STORED,index = Index.ANALYZER)
    private String body;
    @SearchableProperty(name="author",store = Store.STORED,index = Index.ANALYZER)
    private String author;
    @SearchableProperty(name="source",store = Store.STORED,index = Index.ANALYZER)
    private String source;
    @SearchableProperty(name="thumbnail",store = Store.STORED,index = Index.ANALYZER)
    private String thumbnail;
    @SearchableProperty(name="img",store = Store.STORED,index = Index.ANALYZER)
    private String img;
    @SearchableProperty(name="description",store = Store.STORED,index = Index.ANALYZER)
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ArticleDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", author='" + author + '\'' +
                ", source='" + source + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", img='" + img + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
