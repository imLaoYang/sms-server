package com.ydl.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ydl.sms.entity.SignatureEntity;
import com.ydl.sms.mapper.SignatureMapper;
import com.ydl.sms.service.SignatureService;
import org.springframework.stereotype.Service;

@Service
public class SignatureServiceImpl extends ServiceImpl<SignatureMapper, SignatureEntity> implements SignatureService {


  // 自动生成signature表中的code签名编码
  @Override
  public String getCode() {

    // 表中查询code是否存在
    LambdaQueryWrapper<SignatureEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByDesc(SignatureEntity::getCode);
    wrapper.last("limit 1");

    // sql = select * from signature order by  code desc limit 1
    SignatureEntity entity = baseMapper.selectOne(wrapper);
    if (entity != null){

      String code = entity.getCode();
      if ( code.startsWith("DXQM")) {
        int num = Integer.parseInt(code.split("_")[1]) + 1;

        // 生成code模板 DXQM_000000000
        return "DXQM_" + String.format("%09d", num);
      }

    }


    return "DXQM_000000001";
  }

  @Override
  public SignatureEntity getByName(String name) {

    LambdaQueryWrapper<SignatureEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SignatureEntity::getName,name);
    return this.baseMapper.selectOne(wrapper);


  }

}
