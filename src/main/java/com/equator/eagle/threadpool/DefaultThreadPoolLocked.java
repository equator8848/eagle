package com.equator.eagle.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Equator
 * @Date: 2019/12/4 20:39
 **/

@Slf4j
public class DefaultThreadPoolLocked<Job extends Runnable> implements EagleThreadPool<Job> {
    // 线程池工作者数目
    private static final int maxWorkerNumber = 16;
    private static final int defaultWorkerNumber = 4;
    private static final int minWorkerNumber = 1;
    // 工作者编号（线程名）
    private AtomicInteger workerId = new AtomicInteger();
    // 工作者队列
    private final List<Worker> workerList = new LinkedList<>();
    // 工作任务队列
    private final List<Job> jobList = new LinkedList<>();
    // 是否接受新的任务
    private volatile boolean isWorking = true;

    // 工作者内部类
    class Worker implements Runnable {
        private volatile boolean isRunning = true;
        private volatile boolean isHandling = false;

        @Override
        public void run() {
            while (isRunning) {
                Job job = null;
                synchronized (jobList) {
                    while (jobList.isEmpty()) {
                        try {
                            // 当前线程在jobList上等待，问题的根源
                            jobList.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    // 取一个任务
                    job = jobList.remove(0);
                }
                if (job != null) {
                    try {
                        isHandling = true;
                        job.run();
                    } catch (Exception e) {
                    } finally {
                        isHandling = false;
                    }
                }
            }
        }

        public void close() {
            this.isRunning = false;
        }
    }

    public DefaultThreadPoolLocked() {
        initWorkers(defaultWorkerNumber);
    }

    public DefaultThreadPoolLocked(int initialWorkerNumber) {
        initWorkers(initialWorkerNumber);
    }

    // 初始化工作者线程
    public int initWorkers(int num) {
        if (num < minWorkerNumber) {
            num = minWorkerNumber;
        }
        int freeCapacity = maxWorkerNumber - workerList.size();
        if (num >= freeCapacity) {
            num = freeCapacity;
        }
        for (int i = 0; i < num; i++) {
            Worker worker = new Worker();
            workerList.add(worker);
            Thread thread = new Thread(worker, "Worker-" + workerId.incrementAndGet());
            thread.start();
        }
        return num;
    }

    @Override
    public void execute(Job job) {
        if (isWorking && job != null) {
            synchronized (jobList) {
                jobList.add(job);
                jobList.notify();
            }
        } else {
            log.debug("thread pool is waiting to close or job is null");
        }
    }

    @Override
    public int getJobSize() {
        return jobList.size();
    }

    @Override
    public int addWorkers(int num) {
        synchronized (jobList) {
            return initWorkers(num);
        }
    }

    @Override
    public int removeWorkers(int num) {
        int count = 0;
        synchronized (jobList) {
            for (int i = 0; i < num; i++) {
                Worker worker = workerList.get(i);
                if (!worker.isHandling) {
                    worker.close();
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void shutdown() {
        isWorking = false;
        while (!jobList.isEmpty()) {
            log.debug("sorry, jobList is not null, jobList size :{}, waiting to close", jobList.size());
        }
        for (Worker worker : workerList) {
            worker.close();
        }
    }

    // 测试线程池
    public static void main(String[] args) {
        DefaultThreadPoolLocked defaultThreadPool = new DefaultThreadPoolLocked();
        int count = 1000;
        while (count > 0) {
            int finalCount = count;
            defaultThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    int flag = 10;
                    while (flag > 0) {
                        log.debug("job{} say {}", finalCount, flag);
                        flag--;
                    }
                    log.debug("job{} done", finalCount);
                }
            });
            if (count == 500) {
                defaultThreadPool.shutdown();
            }
            count--;
        }
    }
}
