package com.equator.eagle.threadpool;


/**
 * @Author: Equator
 * @Date: 2019/12/4 20:18
 **/

public interface EagleThreadPool<Job extends Runnable> {
    // 执行一个Job
    void execute(Job job);

    // 获取线程池当前待执行任务数目
    int getJobSize();

    // 添加工作线程
    int addWorkers(int num);

    // 减少工作线程
    int removeWorkers(int num);

    // 关闭线程池
    void shutdown();
}
