package com.hr.hrapp.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Notification;
import com.hr.hrapp.entity.Timesheet;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.NotificationRepository;
import com.hr.hrapp.repository.TimesheetRepository;

@Service
public class LocationMismatchScheduler {

    @Autowired
    private TimesheetRepository timesheetRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Scheduled(cron = "0 0 9 * * *")
    
    public void checkLocationMismatch() {

        System.out.println("Location Scheduler Running...");

        List<Timesheet> list =
                timesheetRepository
                .findByStatus("LOCATION_MISMATCH");

        System.out.println("Records Found = " + list.size());

       /* for(Timesheet ts : list) {
        	
        	System.out.println(
        		    "Created At = " + ts.getCreatedAt());

        		System.out.println(
        		    "After 3 Days = "
        		    + ts.getCreatedAt().plusDays(3));

        		System.out.println(
        		    "Now = "
        		    + LocalDateTime.now());

            if(ts.getCreatedAt() != null &&
               ts.getCreatedAt()
                 .plusDays(3)
                 .isBefore(LocalDateTime.now())) {

                ts.setStatus("AUTO_REJECTED");
                
                timesheetRepository.save(ts);
                
                Notification notification =
                        new Notification();

                notification.setEmployeeId(
                        ts.getEmployeeId());

                notification.setMessage(
                        "Your location mismatch timesheet has been auto rejected after 3 days.");

                notification.setRead(false);

                notification.setCreatedAt(
                        LocalDateTime.now());

                notificationRepository.save(
                        notification);

                System.out.println(
                    "Auto Rejected Timesheet Id = "
                    + ts.getId());
            }
        }*/
        
        for(Timesheet ts : list) {

            System.out.println(
                "Created At = " + ts.getCreatedAt());

            if(ts.getCreatedAt() != null &&
               ts.getCreatedAt()
                 .plusDays(3)
                 .isBefore(LocalDateTime.now())) {

                System.out.println(
                    "After 3 Days = "
                    + ts.getCreatedAt().plusDays(3));

                System.out.println(
                    "Now = "
                    + LocalDateTime.now());

                ts.setStatus("AUTO_REJECTED");

                timesheetRepository.save(ts);

                Employee emp =
                        employeeRepository.findByEmpId(
                                ts.getEmployeeId());

                if(emp != null) {

                    emp.setLeaves(
                            emp.getLeaves() - 1);

                    employeeRepository.save(emp);

                    System.out.println(
                            "1 Leave Deducted From Employee = "
                            + emp.getEmpId());
                }

                Notification notification =
                        new Notification();

                notification.setEmployeeId(
                        ts.getEmployeeId());

                notification.setMessage(
                        "Your location mismatch timesheet has been auto rejected after 3 days. One leave has been deducted.");

                notification.setRead(false);

                notification.setCreatedAt(
                        LocalDateTime.now());

                notificationRepository.save(
                        notification);

                System.out.println(
                    "Auto Rejected Timesheet Id = "
                    + ts.getId());
            }
        }
    }
}