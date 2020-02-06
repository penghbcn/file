package com.yohm.springcloud.file.service.impl;

import com.yohm.springcloud.file.component.ScheduleTask;
import com.yohm.springcloud.file.mapper.CronMapper;
import com.yohm.springcloud.file.model.CronModel;
import com.yohm.springcloud.file.service.CronService;
import com.yohm.springcloud.file.vo.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class CronServiceImpl implements CronService {

    public static final String regEx = "(((([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?,)*([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?)|(([\\*]|[0-9]|[0-5][0-9])/([0-9]|[0-5][0-9]))|([\\?])|([\\*]))[\\s](((([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?,)*([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?)|(([\\*]|[0-9]|[0-5][0-9])/([0-9]|[0-5][0-9]))|([\\?])|([\\*]))[\\s](((([0-9]|[0-1][0-9]|[2][0-3])(-([0-9]|[0-1][0-9]|[2][0-3]))?,)*([0-9]|[0-1][0-9]|[2][0-3])(-([0-9]|[0-1][0-9]|[2][0-3]))?)|(([\\*]|[0-9]|[0-1][0-9]|[2][0-3])/([0-9]|[0-1][0-9]|[2][0-3]))|([\\?])|([\\*]))[\\s](((([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(-([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]))?,)*([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(-([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]))?(C)?)|(([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])/([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(C)?)|(L(-[0-9])?)|(L(-[1-2][0-9])?)|(L(-[3][0-1])?)|(LW)|([1-9]W)|([1-3][0-9]W)|([\\?])|([\\*]))[\\s](((([1-9]|0[1-9]|1[0-2])(-([1-9]|0[1-9]|1[0-2]))?,)*([1-9]|0[1-9]|1[0-2])(-([1-9]|0[1-9]|1[0-2]))?)|(([1-9]|0[1-9]|1[0-2])/([1-9]|0[1-9]|1[0-2]))|(((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?,)*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?)|((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)/(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))|([\\?])|([\\*]))[\\s]((([1-7](-([1-7]))?,)*([1-7])(-([1-7]))?)|([1-7]/([1-7]))|(((MON|TUE|WED|THU|FRI|SAT|SUN)(-(MON|TUE|WED|THU|FRI|SAT|SUN))?,)*(MON|TUE|WED|THU|FRI|SAT|SUN)(-(MON|TUE|WED|THU|FRI|SAT|SUN))?(C)?)|((MON|TUE|WED|THU|FRI|SAT|SUN)/(MON|TUE|WED|THU|FRI|SAT|SUN)(C)?)|(([1-7]|(MON|TUE|WED|THU|FRI|SAT|SUN))?(L|LW)?)|(([1-7]|MON|TUE|WED|THU|FRI|SAT|SUN)#([1-7])?)|([\\?])|([\\*]))([\\s]?(([\\*])?|(19[7-9][0-9])|(20[0-9][0-9]))?| (((19[7-9][0-9])|(20[0-9][0-9]))/((19[7-9][0-9])|(20[0-9][0-9])))?| ((((19[7-9][0-9])|(20[0-9][0-9]))(-((19[7-9][0-9])|(20[0-9][0-9])))?,)*((19[7-9][0-9])|(20[0-9][0-9]))(-((19[7-9][0-9])|(20[0-9][0-9])))?)?)";

    @Autowired
    private CronMapper cronMapper;

    @Autowired
    private ScheduleTask scheduleTask;

    @Override
    public JsonResponse addCron(String name, String cron, String createdBy) {
        if(StringUtils.isEmpty(cron)||!cron.matches(regEx)){
            return new JsonResponse(400,"非法的cron表达式!请检查后再试");
        }
        Date createDate = new Date();
        CronModel cronModel = new CronModel();
        cronModel.setName(name);
        cronModel.setCron(cron);
        cronModel.setStatus(1);
        cronModel.setCreatedBy(createdBy);
        cronModel.setCreatedTime(createDate);
        cronModel.setModifiedBy(createdBy);
        cronModel.setModifiedTime(createDate);
        cronMapper.insertOne(cronModel);
        return new JsonResponse(200,"",cronModel);
    }

    @Override
    public JsonResponse deleteCron(int id, String modifiedBy) {
        cronMapper.deleteById(id);
        return new JsonResponse(200,"",id);
    }

    @Override
    public JsonResponse updateCron(int id, String name, String cron, String modifiedBy) {
        if(StringUtils.isEmpty(cron)||!cron.matches(regEx)){
            return new JsonResponse(400,"非法的cron表达式!请检查后再试");
        }
        cronMapper.updateById(id,name,cron);
        scheduleTask.reRegister();
        return new JsonResponse(200,"",cron);
    }

    @Override
    public JsonResponse<List<CronModel>> listCron(int status) {
        List<CronModel> cronModels = cronMapper.listByStatus(status);
        return new JsonResponse<>(200,"",cronModels);
    }
}
