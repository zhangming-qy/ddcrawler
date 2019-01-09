package com.ddcrawler.map;

import com.ddcrawler.entity.MsgSites;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MsgSitesMap {

    @Insert("insert into msg_sites(site_name,domain_name,reg_url,post_url) values (#{site_name},#{domain_name},#{reg_url},#{post_url})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    public int insert(MsgSites msgSites);

    @Select("select id,site_name,domain_name,reg_url,post_url from msg_sites where domain_name = #{domain_name}")
    public MsgSites selectByDomain(@Param("domain_name") String domain_name);

}
