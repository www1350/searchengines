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
    private ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    @Test
    public void index(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(22);
        userDTO.setUsername("fsdfsa");
        userDTO.setGroupDTO(new GroupDTO(12L,"第二组"));
        startTime.set(System.currentTimeMillis());
        LuceneFactory
                .newInstance();
        long   elapsedTime = System.currentTimeMillis() - startTime.get();
        logger.debug("{} take {} ms","newInstance", elapsedTime);
        startTime.set(System.currentTimeMillis());
        LuceneFactory
                .newInstance()
                .index(userDTO,UserDTO.class);
        UserDTO u2 =  LuceneFactory.newInstance().searchById(22L,UserDTO.class);
         elapsedTime = System.currentTimeMillis() - startTime.get();
        logger.debug("{} take {} ms","index", elapsedTime);
        logger.info(u2.toString());
        startTime.set(System.currentTimeMillis());
        LuceneFactory
                .newInstance()
                .index(new GroupDTO(12L,"第一组"),GroupDTO.class);
        elapsedTime = System.currentTimeMillis() - startTime.get();
        logger.debug("{} take {} ms","index", elapsedTime);
        startTime.set(System.currentTimeMillis());
        GroupDTO groupDTO =  LuceneFactory.newInstance().searchById(12L,GroupDTO.class);
        elapsedTime = System.currentTimeMillis() - startTime.get();
        logger.debug("{} take {} ms","search", elapsedTime);
        logger.info(groupDTO.toString());

    }
    @Test
    public void indexMul(){
     List<UserDTO> list =   Arrays.asList(new UserDTO(11,"我饿11访问时"),new UserDTO(12,"我饿21访问时"),new UserDTO(13,"我饿访323问时"),new UserDTO(14,"我饿434访问时"),new UserDTO(15,"我饿5454访问时"));
        startTime.set(System.currentTimeMillis());
        LuceneFactory
                .newInstance()
                .index(list,UserDTO.class);
        long elapsedTime = System.currentTimeMillis() - startTime.get();
        logger.debug("{} take {} ms","index", elapsedTime);
        startTime.set(System.currentTimeMillis());
        UserDTO u2 =  LuceneFactory.newInstance().searchById(12L,UserDTO.class);
        elapsedTime = System.currentTimeMillis() - startTime.get();
        logger.debug("{} take {} ms","search", elapsedTime);
        logger.info(u2.toString());

        startTime.set(System.currentTimeMillis());
        UserDTO u3 =  LuceneFactory.newInstance().searchById(22L,UserDTO.class);
        elapsedTime = System.currentTimeMillis() - startTime.get();
        logger.debug("{} take {} ms","search", elapsedTime);
        logger.info(u3.toString());
    }

}
