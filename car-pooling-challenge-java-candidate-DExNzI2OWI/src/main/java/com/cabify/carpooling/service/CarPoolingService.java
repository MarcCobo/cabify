package com.cabify.carpooling.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cabify.carpooling.model.Car;
import com.cabify.carpooling.model.Journey;
import com.cabify.carpooling.service.exception.DuplicatedIdException;

@Service
public class CarPoolingService {

    private final List<Car> cars = new ArrayList<>();
    private final List<Journey> journeys = new ArrayList<>();
    private final List<Journey> pending = new ArrayList<>();

    public void resetCars(List<Car> cars) {
        this.cars.clear();
        this.journeys.clear();
        this.pending.clear();
        for (Car c1 : cars) {
            if (c1.getMaxSeats() < 4 || c1.getMaxSeats() > 6) {
                throw new IllegalArgumentException("Invalid seats");
            }
            if (this.cars.stream().anyMatch(c2 -> c2.getID() == c1.getID())) {
                throw new DuplicatedIdException("IDs are duplicated");
            }
            this.cars.add(c1);
        }
    }

    public void newJourney(Journey journey) {
        if (this.journeys.stream().anyMatch(j -> j.getId() == journey.getId())) {
            throw new DuplicatedIdException("Journey ID is already used");
        }
        if (!this.pending.isEmpty()) {
            this.pending.forEach(j -> {
                Optional<Car> car = findCar(j.getPassengers());
                car.ifPresent(c -> {
                    System.out.format(">> Car %d assigned to pending journey %d\n", c.getID(), j.getId());
                    j.setAssignedTo(c);
                    c.setAvailableSeats(c.getAvailableSeats() - j.getPassengers());
                    this.pending.removeIf(j2 -> j2.getId() == j.getId());
                    this.journeys.add(j);
                });
            });
        }
        Optional<Car> car = findCar(journey.getPassengers());
        if (car.isPresent()) {
            journey.setAssignedTo(car.orElse(null));
            car.get().setAvailableSeats(car.get().getAvailableSeats() - journey.getPassengers());
            this.journeys.add(journey);
        } else {
            this.pending.add(journey);
        }
    }

    public Car dropoff(int journeyID) {
        Journey journey = this.journeys
                .stream()
                .filter(j -> j.getId() == journeyID)
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("ID not found"));
        Car car = journey.getAssignedTo();
        this.journeys.removeIf(j -> j.getId() == journey.getId());
        if (car != null) {
            car.setAvailableSeats(car.getAvailableSeats() + journey.getPassengers());
        } else {
            this.pending.removeIf(j -> j.getId() == journey.getId());
        }
        return car;
    }

    public void reassign(Car car) {
        Optional<Journey> journey = this.pending
                .stream()
                .filter(j -> j.getPassengers() <= car.getAvailableSeats())
                .findFirst();
        journey.ifPresent(j -> {
            System.out.format(">> Car %d reassigned to journey %d\n", car.getID(), j.getId());
            j.setAssignedTo(car);
            car.setAvailableSeats(car.getAvailableSeats() - j.getPassengers());
            this.pending.removeIf(j2 -> j2.getId() == j.getId());
        });
    }

    public Car locate(int journeyID) {
        Journey journey = this.journeys
                .stream()
                .filter(j -> j.getId() == journeyID)
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("Journey not found"));
        return journey.getAssignedTo();
    }

    private Optional<Car> findCar(int seats) {
        return this.cars.stream()
                .filter(c -> c.getAvailableSeats() >= seats)
                .filter(c -> c.getAvailableSeats() == c.getMaxSeats())
                .findAny();
    }

    public List<Car> getCars() {
        return cars;
    }

    public List<Journey> getJourneys() {
        return journeys;
    }
}
