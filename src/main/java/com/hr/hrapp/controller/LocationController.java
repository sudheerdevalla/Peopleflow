package com.hr.hrapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.hr.hrapp.entity.Location;
import com.hr.hrapp.repository.LocationRepo;

@RestController
@RequestMapping("/api")
public class LocationController {

    @Autowired
    private LocationRepo locationRepo;

    @GetMapping("/locations")
    public List<Location> getLocations() {
        return locationRepo.findAll();
    }
}
