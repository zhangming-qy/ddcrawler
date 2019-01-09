package com.ddcrawler.map;

import com.ddcrawler.entity.StockPrice;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockPriceMap {

    @Insert("insert into stock(stock_id,data) values(#{stock_id},#{data})")
    public void insertIntoStock(StockPrice stockPrice);

}
