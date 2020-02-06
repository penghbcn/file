package com.yohm.springcloud.file.mapper;

import com.yohm.springcloud.file.annotation.Macro;
import com.yohm.springcloud.file.model.CronModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
@Macro(name="cron_t",content = "id,name,cron,status,created_by,created_time,modified_by,modified_time")
@Macro(name="cronModel",content = "id,name,cron,status,created_by createdBy, created_time createdTime,modified_by modifiedBy,modified_time modifiedTime")
@Macro(name="fieldList",content = "#{cronModel.id},#{cronModel.name},#{cronModel.cron},#{cronModel.status}," +
        "#{cronModel.createdBy},#{cronModel.createdTime},#{cronModel.modifiedBy},#{cronModel.modifiedTime}")
public interface CronMapper {

    @Insert("insert into cron_t(@cron_t ) values(#{cronModel.id},#{cronModel.name},#{cronModel.cron},#{cronModel.status},#{cronModel.createdBy},#{cronModel.createdTime},#{cronModel.modifiedBy},#{cronModel.modifiedTime} )")
    int insertOne(@Param("cronModel")CronModel cronModel);

    @Delete("delete from cron_t where id=#{id}")
    int deleteById(int id);

    @Update("update cron_t set name=#{name},cron=#{cron} where id=#{id}")
    int updateById(@Param("id")int id,@Param("name")String name,@Param("cron")String cron);

    @Select("select @cronModel from cron_t where status=#{status}")
    List<CronModel> listByStatus(int status);
}
