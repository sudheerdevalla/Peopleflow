package com.hr.hrapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Location;
import com.hr.hrapp.repository.LocationRepo;

@Service
public class LocationService {

    @Autowired
    private LocationRepo repo;

    public List<Location> getAllLocations() {
        return repo.findAll();
    }
}
