package com.absurd.lucene;

import com.absurd.lucene.dto.GroupDTO;
import com.absurd.lucene.dto.UserDTO;
import com.absurd.lucene.factory.LuceneFactory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:www_1350@163.com">王文伟</a>
 * @Title: searchengines
 * @Package com.absurd.lucene
 * @Description:
 * @date 2016/12/28 20:00
 */
public class LuceneFactoryTest {
    private final static Logger logger = LoggerFactory.getLogger(LuceneFactoryTest.class);
    @Test
    public void index(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(22);
        userDTO.setUsername("fsdfsa");
        userDTO.setGroupDTO(new GroupDTO(12L,"第一组"));
        LuceneFactory
                .newInstance()
                .index(userDTO,UserDTO.class);
        UserDTO u2 =  LuceneFactory.newInstance().searchById(22L,UserDTO.class);
        logger.info(u2.toString());
        LuceneFactory
                .newInstance()
                .index(new GroupDTO(12L,"第一组"),GroupDTO.class);
        GroupDTO groupDTO =  LuceneFactory.newInstance().searchById(12L,GroupDTO.class);
        logger.info(groupDTO.toString());

    }
    @Test
    public void indexMul(){
     List<UserDTO> list =   Arrays.asList(new UserDTO(11,"我饿11访问时"),new UserDTO(12,"我饿21访问时"),new UserDTO(13,"我饿访323问时"),new UserDTO(14,"我饿434访问时"),new UserDTO(15,"我饿5454访问时"));
        LuceneFactory
                .newInstance()
                .index(list,UserDTO.class);
        UserDTO u2 =  LuceneFactory.newInstance().searchById(12L,UserDTO.class);
        logger.info(u2.toString());

        UserDTO u3 =  LuceneFactory.newInstance().searchById(22L,UserDTO.class);
        logger.info(u3.toString());
    }

}
