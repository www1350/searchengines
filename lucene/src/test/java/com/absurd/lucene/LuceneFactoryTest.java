package com.absurd.lucene;

import com.absurd.lucene.dto.UserDTO;
import com.absurd.lucene.factory.LuceneFactory;

import org.junit.Test;

/**
 * @author <a href="mailto:www_1350@163.com">王文伟</a>
 * @Title: searchengines
 * @Package com.absurd.lucene
 * @Description:
 * @date 2016/12/28 20:00
 */
public class LuceneFactoryTest {
    @Test
    public void index(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(22);
        userDTO.setUsername("fsdfsa");
        LuceneFactory
                .newInstance()
                .index(userDTO,UserDTO.class);

    }
}
