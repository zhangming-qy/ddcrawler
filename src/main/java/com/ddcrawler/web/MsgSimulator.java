package com.ddcrawler.web;

import com.ddcrawler.app.AppTaskMan;
import com.ddcrawler.app.AppTaskStatus;
import com.ddcrawler.app.ITaskRunner;
import com.ddcrawler.entity.AppTask;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

public class MsgSimulator implements ITaskRunner {
    private final Logger log = LoggerFactory.getLogger(MsgSimulator.class);

    @Value("${web.default.phone}")
    private String defaultPhone;

    @Value("${web.default.password}")
    private String defaultPwd;

    @Value("${web.button.msgcode}")
    private String msgCodeButton;

    @Value("${web.a.msgcode}")
    private String msgCodeLink;

    @Autowired
    private AppTaskMan appTaskMan;

    @Autowired
    private Util util;

    @Override
    public void setTask(AppTask appTask) {
        appTaskMan.setTask(appTask);
    }

    @Override
    public boolean isSupportConcurrent() {
        return true;
    }

    @Override
    public AppTask call() throws Exception {
        AppTask appTask = appTaskMan.getAppTask();
        String url = appTask.getCurr_url() == null ? appTask.getRoot_url() : appTask.getCurr_url();
        appTaskMan.updateAppTasksStatus(AppTaskStatus.RUNNING);
        simulate(url);
        return appTask;
    }

    public void simulate(String url){
        try (final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)){
            final HtmlPage page = webClient.getPage(url);
            DomNodeList<DomElement> inputList = page.getElementsByTagName("input");
            final Iterator<DomElement> inputIterable = inputList.iterator();

            //set phone and password
            while (inputIterable.hasNext()){
                HtmlInput input = (HtmlInput)inputIterable.next();
                String inputType = input.getTypeAttribute();

               if(inputType.toLowerCase().equals("text")){
                   input.setValueAttribute(defaultPhone);
               }
               else if(inputType.toLowerCase().equals("password")){
                   input.setValueAttribute(defaultPwd);
               }
            }

            List<HtmlButton> buttonList = page.getByXPath(msgCodeButton);

            if(!buttonList.isEmpty()){
                final Iterator<HtmlButton> buttonIterator = buttonList.iterator();
                while (buttonIterator.hasNext()){
                    HtmlButton button = buttonIterator.next();
                    button.click();
                }
            }
            else {
                List<HtmlAnchor> anchorList = page.getByXPath(msgCodeLink);
                final Iterator<HtmlAnchor> anchorIterator = anchorList.iterator();
                while (anchorIterator.hasNext()){
                    HtmlAnchor anchor = anchorIterator.next();
                    anchor.click();
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
