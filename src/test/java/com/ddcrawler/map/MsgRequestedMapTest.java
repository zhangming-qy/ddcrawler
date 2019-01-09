package com.ddcrawler.map;

import com.ddcrawler.DdCrawlerApplication;
import com.ddcrawler.entity.MsgRequested;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DdCrawlerApplication.class, MsgRequestedMap.class})
public class MsgRequestedMapTest {

    @Autowired
    MsgRequestedMap msgRequestedMap;

    @Test
    public void insert() {
        MsgRequested msgRequested = new MsgRequested();
        msgRequested.setCom_info_id(1);
        Assert.assertNotNull(msgRequestedMap.insert(msgRequested));
    }
}