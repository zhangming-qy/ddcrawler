package com.ddcrawler.map;

import com.ddcrawler.entity.ComInfo;
import com.ddcrawler.entity.MsgRequested;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DbMap {

    @Update("truncate table com_info")
    void deleteComInfo();

    @Select("select * from com_info")
    List<ComInfo> getAllComInfo();

    @Update("truncate table app_tasks")
    void deleteAppTasks();

    @Update("truncate table msg_sites")
    void deleteMsgSites();

    @Update("truncate table msg_requested")
    void deleteMsgRequested();

    @Select("select * from msg_requested")
    List<MsgRequested> getAllMsgRequested();
}
