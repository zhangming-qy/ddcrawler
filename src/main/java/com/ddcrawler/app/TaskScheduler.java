package com.ddcrawler.app;

import com.ddcrawler.entity.AppTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskScheduler {
    private final static Logger log = LoggerFactory.getLogger(TaskScheduler.class);
    private final static ConcurrentHashMap<String, Future<AppTask>> futureHashMap = new ConcurrentHashMap<>(32);
    private final static List<ScheduledFuture<?>> futureList = new ArrayList<>(128);
    private static volatile boolean isMaxAppTask = false;

    public final static int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private static ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
    private static ThreadPoolExecutor executor  = new ThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_SIZE*2, 1L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(5000));

    public int getActiveCount(){return executor.getActiveCount();}

    public Future<?> submit(Runnable task){
        return executor.submit(task);
    }

    public static int getScheduledActiveCount(){
        return scheduledExecutor.getActiveCount();
    }

    public static void setMaximumPoolSize(int maximumPoolSize){
        scheduledExecutor.setMaximumPoolSize(maximumPoolSize);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit){
        ScheduledFuture<?> future = scheduledExecutor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        futureList.add(future);
        return future;
    }

    public static ScheduledFuture<?> schedule(Runnable command){
        ScheduledFuture<?> future =  scheduledExecutor.schedule(command, 0, TimeUnit.NANOSECONDS);
        futureList.add(future);
        return future;
    }

    public static int getAppTaskCount(){
        return futureHashMap.size();
    }

    public static int getAllTaskCount(){
       return futureList.size() + futureHashMap.size();
    }

    public static boolean isAppTasksFull(){
        return isMaxAppTask;
    }

    public static Future<AppTask> submitAppTasks(AppTask appTask){
        //Tasks can't be more than core pool size
        if(futureHashMap.size() == CORE_POOL_SIZE){
            isMaxAppTask = true;
            log.info("Reached the maximum {} tasks, can't submit any more.", CORE_POOL_SIZE);
            return null;
        }

        try {
            Class clazz = Class.forName(appTask.getJclass());
            ITaskRunner task = (ITaskRunner)ApplicationContextProvider.getBean(clazz);

            if(task==null)
                throw new ClassNotFoundException(clazz.getName());

            //if does not support concurrent class, one class one thread, if exists, then skip.
            String futureKey = task.isSupportConcurrent() ? String.valueOf(appTask.getId()) : appTask.getJclass();

            if(futureHashMap.containsKey(futureKey)) {
                if(task.isSupportConcurrent())
                    log.info("Exist concurrent task {}-{}-{}.", appTask.getId(), appTask.getGroup_name(), appTask.getRoot_url());
                else
                    log.info("Can't submit non-concurrent task {}-{}-{}.", appTask.getId(), appTask.getGroup_name(), appTask.getRoot_url());
                return futureHashMap.get(futureKey);
            }

            task.setTask(appTask);
            Future<AppTask> appTaskFuture = scheduledExecutor.submit(task);
            futureHashMap.putIfAbsent(futureKey,appTaskFuture);
            log.info("Submit AppTask {}-{}-{}.", appTask.getId(), appTask.getGroup_name(), appTask.getRoot_url());
            return appTaskFuture;
        }
        catch (ClassNotFoundException ex){
            log.error("Can't found class {}.", appTask.getJclass());
        }

        return null;
    }

    public static void removeFinishedAppTasks(){
        //遍历任务的结果
        Iterator<Map.Entry<String, Future<AppTask>>> entries = futureHashMap.entrySet().iterator();

        while (entries.hasNext()) {
            try {
                Map.Entry<String, Future<AppTask>> entry = entries.next();
                Future<AppTask> fs = entry.getValue();
                if(fs.isDone()){
                    AppTask appTask = fs.get();
                    futureHashMap.remove(entry.getKey());
                    isMaxAppTask = false;
                    log.info("Task [id={}, root_url={}, group_name={}, java_class={}, status={}] execute finish.",
                            appTask.getId(),
                            appTask.getRoot_url(),
                            appTask.getGroup_name(),
                            appTask.getJclass(),
                            appTask.getStatus());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void restart(){
        if(futureHashMap.isEmpty() && futureList.isEmpty()) {
            CommandLineRunner taskRunner = ApplicationContextProvider.getBean("taskRunner", CommandLineRunner.class);
            ApplicationArguments args = ApplicationContextProvider.getBean(ApplicationArguments.class);
            try {
                taskRunner.run(args.getSourceArgs());
                log.info("Scheduled executor restart successfully!");
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void shutdown(){
        //just cancel all tasks, that make it can restart
        cancel();

/*        try {
            //Cancel scheduled but not started task, and avoid new ones
            scheduledExecutor.shutdown();

            //Wait for the running tasks
            scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS);

            //Interrupt the threads and shutdown the scheduler
            scheduledExecutor.shutdownNow();
            log.info("Scheduled executor have been shutdown.");
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }*/
    }

    public static void cancel(){

        ListIterator<ScheduledFuture<?>> futureListIterator = futureList.listIterator();
        while (futureListIterator.hasNext()){
            ScheduledFuture<?> scheduledFuture = futureListIterator.next();
            scheduledFuture.cancel(true);
            futureListIterator.remove();
        }

        Iterator<Map.Entry<String, Future<AppTask>>> entries = futureHashMap.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, Future<AppTask>> entry = entries.next();
            Future<AppTask> fs = entry.getValue();
            fs.cancel(true);
            entries.remove();
        }

        isMaxAppTask = false;

        log.info("All scheduled tasks have been canceled.");
    }
}
