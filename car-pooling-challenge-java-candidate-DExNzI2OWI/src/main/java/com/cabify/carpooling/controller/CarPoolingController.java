package com.cabify.carpooling.controller;

import java.util.List;
import java.util.NoSuchElementException;

import com.cabify.carpooling.service.exception.DuplicatedIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.cabify.carpooling.model.Car;
import com.cabify.carpooling.model.Journey;
import com.cabify.carpooling.service.CarPoolingService;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

@RestController
@RequestMapping("/*")
public class CarPoolingController {

    @Autowired
    private CarPoolingService carJourneyService;

    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    public void getStatus() {
    }

    @PutMapping(value = "/cars", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void putCars(@RequestBody List<Car> cars) {
        carJourneyService.resetCars(cars);
    }

    @PostMapping("/journey")
    public ResponseEntity<String> postJourney(@Validated @RequestBody Journey journey) {
        if (journey.getId() <= 0) {
            throw new IllegalArgumentException("Journey ID value not valid");
        }
        carJourneyService.newJourney(journey);
        return new ResponseEntity<>("New journey created", HttpStatus.ACCEPTED);
    }

    @PostMapping("/dropoff")
    public ResponseEntity<String> postDropoff(@RequestParam("ID") int journeyID) {
        if (journeyID <= 0) {
            throw new IllegalArgumentException("Journey ID value not valid");
        }
        Car car = carJourneyService.dropoff(journeyID);
        if (car != null) {
            carJourneyService.reassign(car);
        }
        return new ResponseEntity<>("Dropoff completed", HttpStatus.OK);
    }

    @PostMapping(
            value = "/locate",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = "application/json")
    public @ResponseBody ResponseEntity<Car> postLocate(@RequestParam("ID") final int journeyID) {
        if (journeyID <= 0) {
            throw new IllegalArgumentException("Journey ID value not valid");
        }
        Car car = carJourneyService.locate(journeyID);
        if (car == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(car, HttpStatus.OK);
    }


}
