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
 * @date 2016/12/28 19:00
 */
@Searchable(alias = "user")
public class UserDTO implements Serializable {
    @SearchableId(name="id",store = Store.STORED,index = Index.ANALYZER)
    private Integer id;
    @SearchableProperty(name="username",store = Store.STORED,index = Index.ANALYZER)
    private String username;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
