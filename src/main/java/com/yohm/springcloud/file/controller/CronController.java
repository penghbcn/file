package com.yohm.springcloud.file.controller;

import com.yohm.springcloud.file.service.CronService;
import com.yohm.springcloud.file.vo.CronVO;
import com.yohm.springcloud.file.vo.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cron")
public class CronController {

    @Autowired
    private CronService cronService;

    @PostMapping("/add")
    public JsonResponse addCron(@RequestBody CronVO cronVO) {
        String name = cronVO.getName();
        String cron = cronVO.getCron();
        String createdBy = cronVO.getOperator();
        return cronService.addCron(name, cron, createdBy);
    }

    @PostMapping("/delete")
    public JsonResponse deleteCron(@RequestBody CronVO cronVO) {
        int id = cronVO.getId();
        String modifiedBy = cronVO.getOperator();
        return cronService.deleteCron(id, modifiedBy);
    }

    @PostMapping("/update")
    public JsonResponse updateCron(@RequestBody CronVO cronVO) {
        int id = cronVO.getId();
        String name = cronVO.getName();
        String cron = cronVO.getCron();
        String modifiedBy = cronVO.getOperator();
        return cronService.updateCron(id, name, cron, modifiedBy);
    }

    @GetMapping("/list")
    public JsonResponse listCron(int status) {
        return cronService.listCron(status);
    }

}
