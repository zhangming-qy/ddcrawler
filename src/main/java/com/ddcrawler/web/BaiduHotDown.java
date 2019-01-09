package com.ddcrawler.web;

import com.ddcrawler.dirver.BaiDuHotProcess;
import com.ddcrawler.entity.BaiduHot;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.ddcrawler.map.BaiduHotMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.ArrayList;

//百度股市热门下载
@RestController
public class BaiduHotDown {
    public  static Logger logger;
    @Autowired
    BaiduHotMap baiduHotMap;

    //@Scheduled(cron = "0/20 * * * * ? ")
    public void downBaiduHot(){
        String url = "https://gupiao.baidu.com/concept/";
        try {
            Document doc = Jsoup.connect(url).get();
            ArrayList<BaiduHot> abh = new BaiDuHotProcess().processBaiduHot(doc);
            for(BaiduHot b:abh){
                baiduHotMap.InsertBaiduHot(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
