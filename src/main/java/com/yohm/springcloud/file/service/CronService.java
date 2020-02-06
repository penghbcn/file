package com.yohm.springcloud.file.service;

import com.yohm.springcloud.file.model.CronModel;
import com.yohm.springcloud.file.vo.JsonResponse;

import java.util.List;

public interface CronService {
    JsonResponse addCron(String name,String cron,String createdBy);

    JsonResponse deleteCron(int id,String modifiedBy);

    JsonResponse updateCron(int id,String name,String cron,String modifiedBy);

    JsonResponse<List<CronModel>> listCron(int status);
}
