package com.hr.hrapp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Location;
import com.hr.hrapp.entity.Notification;
import com.hr.hrapp.entity.Timesheet;
import com.hr.hrapp.repository.NotificationRepository;

@Service
public class TimesheetValidationService {

    @Autowired
    private LocationService locationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${gps.validation.radius.km:5}")
    private double radiusKm;

    /**
     * Validate timesheet coordinates against employee's assigned office location.
     * This method will set status and locationMatched on the timesheet and send
     * notifications/emails when there is a mismatch.
     */
    public void validateAndNotify(Timesheet ts, Employee emp) {

        Double lat = ts.getLatitude();
        Double lon = ts.getLongitude();

        // If coords missing, fall back to city-based matching (handled by caller via actualLocation)
        if (lat == null || lon == null) {
            fallbackStringMatch(ts, emp);
            return;
        }

        Optional<Location> officeOpt = locationService.findByName(emp.getLocation());

        if (officeOpt.isPresent()) {
            Location office = officeOpt.get();
            double distance = locationService.distanceInKm(lat, lon, office.getLatitude(), office.getLongitude());

            if (distance <= radiusKm) {
                ts.setLocationMatched(true);
                ts.setStatus("APPROVED");
            } else {
                ts.setLocationMatched(false);
                ts.setStatus("LOCATION_MISMATCH");

                sendMismatchNotifications(ts, emp, distance);
            }
        } else {
            // No office coordinates available for this employee's location - fall back to string comparison
            fallbackStringMatch(ts, emp);
        }
    }

    private void fallbackStringMatch(Timesheet ts, Employee emp) {
        String detectedCity = ts.getActualLocation();

        if (emp.getLocation() != null && emp.getLocation().equalsIgnoreCase(detectedCity)) {
            ts.setLocationMatched(true);
            ts.setStatus("APPROVED");
        } else {
            ts.setLocationMatched(false);
            ts.setStatus("LOCATION_MISMATCH");
            sendMismatchNotifications(ts, emp, null);
        }
    }

    private void sendMismatchNotifications(Timesheet ts, Employee emp, Double distanceKm) {

        String detected = ts.getActualLocation() == null ? "UNKNOWN" : ts.getActualLocation();

        String employeeBody = "<h2>Location Mismatch Detected</h2>" +
                "<p>Assigned Location: " + emp.getLocation() + "</p>" +
                "<p>Actual Location: " + detected + "</p>" +
                (distanceKm != null ? ("<p>Distance from office: " + String.format("%.2f km", distanceKm) + "</p>") : "") +
                "<p>Please contact your manager.</p>";

        emailService.sendMail(emp.getEmail(), "Location Mismatch Detected", employeeBody);

        Notification empNotification = new Notification();
        empNotification.setEmployeeId(emp.getEmpId());
        empNotification.setMessage("Location mismatch detected. Waiting for manager approval.");
        empNotification.setRead(false);
        empNotification.setCreatedAt(java.time.LocalDateTime.now());
        notificationRepository.save(empNotification);

        Employee manager = emp.getManager();
        if (manager != null) {
            String managerBody = "<h2>Location Mismatch Alert</h2>" +
                    "<p>Employee: " + emp.getName() + "</p>" +
                    "<p>Assigned Location: " + emp.getLocation() + "</p>" +
                    "<p>Actual Location: " + detected + "</p>" +
                    (distanceKm != null ? ("<p>Distance from office: " + String.format("%.2f km", distanceKm) + "</p>") : "");

            emailService.sendMail(manager.getEmail(), "Employee Location Mismatch", managerBody);

            Notification managerNotification = new Notification();
            managerNotification.setEmployeeId(manager.getEmpId());
            managerNotification.setMessage("Location mismatch request from " + emp.getName());
            managerNotification.setRead(false);
            managerNotification.setCreatedAt(java.time.LocalDateTime.now());
            notificationRepository.save(managerNotification);
        }
    }
}
