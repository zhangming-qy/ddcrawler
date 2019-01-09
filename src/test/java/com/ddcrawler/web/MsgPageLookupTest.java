package com.ddcrawler.web;

import com.ddcrawler.DdCrawlerApplication;
import com.ddcrawler.entity.MsgSites;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DdCrawlerApplication.class, MsgPageLookup.class})
public class MsgPageLookupTest {

    @Autowired
    private MsgPageLookup msgPageLookup;

    @Test
    public void contexLoads() throws Exception {
        Assert.assertNotNull(msgPageLookup);
    }

    @Test
    public void getMsgSites() {
        String url = "https://passport.111.com.cn/sso/register.action";
        MsgSites msgSites = msgPageLookup.getMsgSites(url, true);
        //Assert.assertEquals("https://passport.111.com.cn/sso/login.action", msgSites.getReg_url());
        Assert.assertNotNull(msgSites);
    }
}