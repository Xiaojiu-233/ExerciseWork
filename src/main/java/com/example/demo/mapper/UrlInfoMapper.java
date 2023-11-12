package com.example.demo.mapper;

import com.example.demo.entity.UrlInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UrlInfoMapper {

    @Select("select * from haman where platform_name = #{platform} and batch_no = #{batch} " +
            "limit #{start},#{count}")
    List<UrlInfo> getHamanList(String platform, String batch, int start, int count);

    @Select("select count(*) from haman where platform_name = #{platform} and batch_no = #{batch}")
    int getHamanCount(String platform, String batch);

    @Delete("delete from haman where id = #{id}")
    void deleteHaman(String id);
}
