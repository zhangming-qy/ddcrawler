package com.ddcrawler.init;


import com.ddcrawler.app.AppTaskStatus;
import com.ddcrawler.entity.AppTask;
import com.ddcrawler.map.AppTaskMap;
import com.ddcrawler.web.LvseSites;
import com.ddcrawler.web.Util;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.List;

@RestController
public class Lvse {

    private final Logger log = LoggerFactory.getLogger(Lvse.class);

    @Autowired
    private AppTaskMap appTaskMap;

    @Autowired
    private Util util;

    @Value("${web.lvse.url}")
    private String web_url;

    @Value("${web.lvse.category}")
    private String web_catetory;

    @Value("${web.lvse.next}")
    private String web_next;

    @Value("${web.lvse.last}")
    private String web_last;

    @RequestMapping("/init/lvse")
    public String initTask(@RequestParam("url") String url){
        StringBuilder stringBuilder = new StringBuilder();

        log.info("Requesting URL: " + url);
        Document document = util.getContent(url);

        if(document == null){
            log.warn("Can't get content from " + url);
            return "";
        }

        Elements elems = document.select(web_catetory);

        for(Element elem : elems){
            String link = elem.absUrl("href");

            List<AppTask> appTasks = appTaskMap.getAppTasksByRootUrlAndJClass(link, LvseSites.class.getName());

            stringBuilder.append(link);
            stringBuilder.append("<br>");

            if(appTasks.size() > 0) continue;

            AppTask appTask = new AppTask();
            appTask.setRoot_url(link);
            appTask.setGroup_name("lvse");
            appTask.setOrder_num(elems.indexOf(elem));
            appTask.setJclass(LvseSites.class.getName());
            appTask.setStatus(AppTaskStatus.PENDING.name());
            appTask.setCreated_time(new Timestamp(System.currentTimeMillis()));
            appTask.setModified_time(new Timestamp(System.currentTimeMillis()));
            appTaskMap.insert(appTask);
        }

        return stringBuilder.toString();
    }

    @RequestMapping("/init/lvse/update")
    public String updateTask(){
        List<AppTask> appTasks = appTaskMap.getAppTasksByGroup("lvse");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<table border='1px' cellspacing='0' cellpadding='0' cellspacing='4'  border-collapse='collapse'>");
        stringBuilder.append("<th>first page</th><th>last page</th>");
        int i=0;
        for(AppTask appTask : appTasks){
            if(appTask.getLast_url() == null && i<10){
                String root_url = appTask.getRoot_url();
                stringBuilder.append("<tr><td>");
                stringBuilder.append(root_url);
                stringBuilder.append("</td><td>");

                Document docLink = util.getContent(root_url);
                Element elemLast = docLink.selectFirst(web_last);
                String lastPage = elemLast.absUrl("href");

                if(appTask.getLast_url() == null || !appTask.getLast_url().equals(lastPage)) {
                    appTask.setStatus(AppTaskStatus.PENDING.name());
                    appTask.setLast_url(lastPage);
                    appTaskMap.update(appTask);
                }

                stringBuilder.append(appTask.getLast_url());
                stringBuilder.append("</td></tr>");
                i++;
            }
        }
        stringBuilder.append("</table>");
        return stringBuilder.toString();
    }

    @RequestMapping("/init/lvse/msg")
    public String createMsgTask(){
        return "TODO";
    }
}
