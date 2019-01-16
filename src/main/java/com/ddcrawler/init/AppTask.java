package com.ddcrawler.init;

import com.ddcrawler.app.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppTask {
    private final Logger log = LoggerFactory.getLogger(AppTask.class);

    @RequestMapping("/init/apptask")
    public String opAppTask(@RequestParam("op") String op){
        switch (op){
            case "restart":
                TaskScheduler.restart();
                return "restart successfully!";
            case "shutdown":
                TaskScheduler.shutdown();
                return "shutdown successfully!";
            case "cancel":
                TaskScheduler.cancel();
                return "cancel tasks successfully!";

        }
        return "Not supported operation!";
    }
}
