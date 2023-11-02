package com.ydl.sms.mapper;

import com.ydl.sms.entity.SysPerm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author Yang
* @description 针对表【sys_perm(权限表)】的数据库操作Mapper
* @createDate 2023-10-31 10:38:10
* @Entity com.ydl.sms.entity.SysPerm
*/
@Repository
public interface SysPermMapper extends BaseMapper<SysPerm> {

  List<String> getPermKeyByUserId(Long userId);

}




