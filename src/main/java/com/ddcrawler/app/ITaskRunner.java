package com.ddcrawler.app;

import com.ddcrawler.entity.AppTask;

import java.util.concurrent.Callable;

public interface ITaskRunner extends Callable<AppTask> {
    void setTask(AppTask appTask);
    boolean isSupportConcurrent();
}
