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
 * @date 2016/12/29 10:35
 */
@Searchable(alias = "group")
public class GroupDTO implements Serializable{
    @SearchableId(name="id",store = Store.STORED,index = Index.ANALYZER)
    private Long id;
    @SearchableProperty(name="groupname",store = Store.STORED,index = Index.ANALYZER)
    private String groupName;

    public GroupDTO() {
    }

    public GroupDTO(Long id, String groupName) {
        this.id = id;
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "GroupDTO{" +
                "id=" + id +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
