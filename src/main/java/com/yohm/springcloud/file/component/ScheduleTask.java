package com.yohm.springcloud.file.component;

import com.yohm.springcloud.file.annotation.CronScheduled;
import com.yohm.springcloud.file.model.CronModel;
import com.yohm.springcloud.file.service.CronService;
import com.yohm.springcloud.file.vo.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class ScheduleTask implements ApplicationContextAware, BeanFactoryAware, BeanPostProcessor, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleTask.class);

    @Autowired
    private ScheduleJob scheduleJob;

    @Autowired
    private CronService cronService;

    private ApplicationContext applicationContext;
    private BeanFactory beanFactory;
    ScheduledTaskRegistrar scheduledTaskRegistrar = new ScheduledTaskRegistrar();
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(50);
    private boolean init = true;

    public void configureTasks(){
        LOG.info("========configureTasks========");
        JsonResponse<List<CronModel>> jsonResponse = cronService.listCron(1);
        List<CronModel> cronModelList = jsonResponse.getResult();
        if(jsonResponse.getCode() == 400||CollectionUtils.isEmpty(cronModelList)){
            LOG.error("查询cron列表失败: {}",jsonResponse.getMessage());
            return;
        }
        Class<? extends ScheduleJob> clazz = scheduleJob.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            CronScheduled annotation = method.getAnnotation(CronScheduled.class);
            if(annotation == null){
                continue;
            }
            String cron = cronModelList.stream()
                    .filter(o -> annotation.desc().equals(o.getName()))
                    .findAny()
                    .orElseThrow()
                    .getCron();
            if(StringUtils.isEmpty(cron)){
                continue;
            }
            ScheduledMethodRunnable runnable = new ScheduledMethodRunnable(scheduleJob, method);
            scheduledTaskRegistrar.addTriggerTask(
                    runnable,
                    triggerContext -> new CronTrigger(cron).nextExecutionTime(triggerContext));
        }
        scheduledTaskRegistrar.setScheduler(scheduledExecutorService);
        scheduledTaskRegistrar.afterPropertiesSet();
    }

    public void reRegister(){
        LOG.info("========reRegister========");
        scheduledTaskRegistrar.destroy();
        scheduledTaskRegistrar.setTriggerTasksList(new ArrayList<>());
        LOG.info("定时任务已销毁,剩余任务数量:{}",scheduledTaskRegistrar.getTriggerTaskList().size());
        configureTasks();
        LOG.info("定时任务已重新配置,现有任务数量:{}",scheduledTaskRegistrar.getTriggerTaskList().size());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void destroy() throws Exception {
        scheduledTaskRegistrar.destroy();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(init){
            configureTasks();
            init = false;
        }
        return bean;
    }
}
