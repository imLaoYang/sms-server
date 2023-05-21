package com.ydl.sms.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DaysUtil {

  /**
   * 获取两个日期之间的所有日期
   *
   * @param startTime 开始日期
   * @param endTime   结束日期
   * @return 天数集合
   */
  public static List<String> getDays(LocalDateTime startTime, LocalDateTime endTime, String format) {

    if (startTime != null && endTime != null) {

      // 返回的日期集合
      List<String> days = new ArrayList<>();

      DateFormat dateFormat = new SimpleDateFormat(format);

      Date start = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
      Date end = Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());

      Calendar tempStart = Calendar.getInstance();
      tempStart.setTime(start);

      Calendar tempEnd = Calendar.getInstance();
      tempEnd.setTime(end);
      //  tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
      while (tempStart.before(tempEnd)) {
        days.add(dateFormat.format(tempStart.getTime()));
        tempStart.add(Calendar.DAY_OF_YEAR, 1);
      }

      return days;
    }

    return null;
  }

}
