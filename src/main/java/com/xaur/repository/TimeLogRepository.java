package com.xaur.repository;

import com.xaur.model.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {
    List<TimeLog> findByDeviceSerialNumber(String deviceSerialNumber);
    List<TimeLog> findByUserId(String userId);
    List<TimeLog> findByLogTimeBetween(LocalDateTime start, LocalDateTime end);
    List<TimeLog> findByUserIdAndDeviceSerialNumber(String userId,String deviceSerialNumber);
    List<TimeLog> findByUserIdAndLogTimeBetween(String userId, LocalDateTime start, LocalDateTime end);
    @Query(nativeQuery = true,value ="select * from time_logs where user_id=:userId and device_serial_number=:deviceSerialNumber and date(log_time) between :start and :end" )
    List<TimeLog> findByUserAndDateRange( String userId, String deviceSerialNumber, LocalDate start, LocalDate end);
    @Query(nativeQuery = true,value = "select count(log_time) from time_logs where device_serial_number in(:list) and datediff( date(log_time),:date)=0")
    Integer countOfLogTime(List<String> list,LocalDate date);
    @Query(nativeQuery = true,value = "select * from time_logs where date(log_time) between :start and :end")
    List<TimeLog> findByDateRange(LocalDate start,LocalDate end);
    @Query(nativeQuery = true,value = " SELECT * FROM time_logs WHERE device_serial_number = :deviceSerialNumber AND (:userId is null or user_id=:userId)and (:start IS NULL or DATE(log_time)>=:start) and(:end is null or DATE(log_time)<=:end)")
    List<TimeLog> findByDeviceSerialNumberAndDate(String deviceSerialNumber,LocalDate start,LocalDate end,String userId);
    @Query(nativeQuery = true,value = "select * from time_logs where user_id=:userId and date(log_time) between :start and :end")
    List<TimeLog> findByUserIdrAndDate(String userId,LocalDate start,LocalDate end);


}