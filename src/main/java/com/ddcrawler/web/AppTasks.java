package com.ddcrawler.web;

import com.ddcrawler.entity.AppTask;
import com.ddcrawler.map.AppTaskMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AppTasks {
    @Autowired
    private AppTaskMap appTaskMap;

    @RequestMapping("/apptasks")
    public String getAllTask(){

        List<AppTask> appTasks = appTaskMap.getAll();

        for(AppTask appTask : appTasks){

        }

        return  "";
    }
}
