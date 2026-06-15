package com.hr.hrapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Holiday;
import com.hr.hrapp.repository.HolidayRepository;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    // Save Holiday
    public Holiday saveHoliday(Holiday holiday) {
        return holidayRepository.save(holiday);
    }

    // Get All Holidays
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    // Delete Holiday
    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }

    // Get Holiday By Id
    public Holiday getHolidayById(Long id) {
        return holidayRepository.findById(id)
                .orElse(null);
    }
}