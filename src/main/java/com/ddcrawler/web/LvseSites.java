package com.ddcrawler.web;

import com.ddcrawler.app.AppTaskStatus;
import com.ddcrawler.app.AppTaskMan;
import com.ddcrawler.app.ITaskRunner;
import com.ddcrawler.entity.AppTask;
import com.ddcrawler.entity.ComInfo;
import com.ddcrawler.map.ComInfoMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.Thread.sleep;

@Component
public class LvseSites implements ITaskRunner {

    private final Logger log = LoggerFactory.getLogger(LvseSites.class);

    private static volatile int requestCount = 0;

    @Autowired
    private Util util;

    @Autowired
    private ComInfoMap comInfoMap;

   /*
    @Value("${web.url}")
    private String root_url;
    */

   @Autowired
    private AppTaskMan appTaskMan;

    public LvseSites(){}

    public AppTask getAppTask() {
        return appTaskMan.getAppTask();
    }

    public AppTaskMan getAppTaskMan() {
        return appTaskMan;
    }

    public void setAppTaskMan(AppTaskMan appTaskMan) {
        this.appTaskMan = appTaskMan;
    }

    public static synchronized void increaseRequestCount(){
        requestCount++;

        //request 50/times and then sleep 1 minute to avoid block access
        try{
            if(requestCount>0 && (requestCount%30)==0)
                sleep(60000);
        }
        catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public AppTask call() {
        AppTask appTask = getAppTask();
        String url = appTask.getCurr_url() == null ? appTask.getRoot_url() : appTask.getCurr_url();
        appTaskMan.updateAppTasksStatus(AppTaskStatus.RUNNING);
        crawlLvseSites(url);

        if(appTask.getCurr_url()!= null && appTask.getCurr_url().equals(appTask.getLast_url())){
            appTaskMan.updateAppTasksStatus(AppTaskStatus.DONE);
        }
        else{
            try {
                //if holding, sleep 5mins and then try again until finish.
                while(appTask.getStatus().equals(AppTaskStatus.HOLDING.name())){
                    log.warn("Task [{}-{}] is holding, and wait 5*3mins then restart again.",appTask.getId(), appTask.getRoot_url());
                    Thread.sleep(300000*3);
                    call();
                }
            }
            catch (InterruptedException e){
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }

        return appTask;
    }

    @Override
    public void setTask(AppTask appTask) {
        this.appTaskMan.setTask(appTask);
    }

    @Override
    public boolean isSupportConcurrent() {
        return false;
    }

    public void crawlLvseSites(String url){
        log.info("Requesting URL: " + url);
        Document document = util.getContent(url);

       increaseRequestCount();

        if(document == null){
            log.warn("Can't get content from " + url);
            return;
        }
        else if(document.text().contains("Status=403")){  //Access deny, hold request, and decrease priority.
            AppTask appTask = appTaskMan.getAppTask();
            appTask.setStatus(AppTaskStatus.HOLDING.name());
            appTaskMan.updateAppTasks();
            return;
        }

        log.info(url);
        System.out.println("Host:" + Util.getHost(url));
        System.out.println("Location:" + url);

        ComInfo comInfo = new ComInfo();
        comInfo.setFrom_url(url);
        Element elemRegion = document.selectFirst("div.area#area1 a.on");
        if(elemRegion != null)
            comInfo.setRegion(elemRegion.text());

        Element elemCategory = document.selectFirst("div.area#area2 a.on");
        if(elemCategory != null)
            comInfo.setCategory(elemCategory.text());

        Elements slistingList = document.select("div#slisting"); //div.info h2:has(a)

        for (Element slisting: slistingList) {
            Element brand = slisting.selectFirst("div.info a:not(.visit)");
            Element link = slisting.selectFirst("div.info a.visit[href]");
            Element desc = slisting.selectFirst("div.info p:not(.l)");
            String aHref = link.absUrl("href");

            comInfo.setName(brand.text());
            comInfo.setDescription(desc.ownText());
            comInfo.setWeb_url(aHref);
            comInfo.setVisit_cnt(0);

            log.info("Brand: " + brand.text());
            log.info("Brand Link: " + aHref);

            ComInfo comInfoCheck = comInfoMap.getByCategoryAndUrl(comInfo.getCategory(), aHref);
            if(comInfoCheck == null)
                comInfoMap.insert(comInfo);
            else
                log.info("Duplicated: {}-{}",brand.text(),aHref);

        }

        //next page
        Element nextLink = document.selectFirst("a[href]:contains(下一页)");
        if(nextLink!=null){
            String nextPage = nextLink.absUrl("href");
            appTaskMan.updateAppTasksCurrUrl(nextPage);
            crawlLvseSites(nextPage);
        }
        else{
            nextLink = document.selectFirst("a[href]:contains(最后一页)");
            AppTask appTask = getAppTask();
            if(nextLink != null){
                String lastPage = nextLink.absUrl("href");
                appTask.setCurr_url(lastPage);
                appTask.setLast_url(lastPage);
                appTaskMan.updateAppTasks();
            }
            log.info("This is the last page: " + url);
        }
    }
}
