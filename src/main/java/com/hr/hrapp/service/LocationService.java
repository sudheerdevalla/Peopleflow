package com.hr.hrapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public String getCity(
            Double latitude,
            Double longitude) {

        if(latitude == null ||
           longitude == null) {

            return "UNKNOWN";
        }

        if(latitude > 12 &&
           latitude < 14) {

            return "Chennai";
        }

        if(latitude > 17 &&
           latitude < 18) {

            return "Hyderabad";
        }

        return "OTHER";
    }

    /**
     * Find a Location entity by office/site name (case-insensitive).
     */
    public Optional<Location> findByName(String name) {
        if (name == null) return Optional.empty();
        return repo.findByNameIgnoreCase(name);
    }

    /**
     * Calculate Haversine distance between two coordinates in kilometers.
     */
    public double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
