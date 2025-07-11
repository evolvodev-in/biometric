package com.xaur.repository;

import com.xaur.model.HrTimeSheetLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeSheetRepository extends JpaRepository<HrTimeSheetLog,Long> {


    @Query(value = "select * from hr_timesheet_log where is_transfered=:isTransferred order by att_time limit 50",nativeQuery = true)
    List<HrTimeSheetLog> list(Boolean isTransferred);
}
