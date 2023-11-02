package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.SysPerm;
import com.ydl.sms.mapper.SysPermMapper;
import com.ydl.sms.service.SysPermService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Yang
 * @description 针对表【sys_perm(权限表)】的数据库操作Service实现
 * @createDate 2023-10-31 10:38:10
 */


@Service
public class SysPermServiceImpl extends ServiceImpl<SysPermMapper, SysPerm>
        implements SysPermService {
  @Override
  public List<String> getPermKeyByUserId(Long userId) {
    return baseMapper.getPermKeyByUserId(userId);
  }
}




