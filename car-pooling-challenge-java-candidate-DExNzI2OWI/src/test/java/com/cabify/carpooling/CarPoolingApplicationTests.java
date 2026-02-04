package com.cabify.carpooling;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cabify.carpooling.model.Car;
import com.cabify.carpooling.model.Journey;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CarPoolingApplicationTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void CarPoolingController_GetStatus_ReturnOkStatus() throws Exception {
        mvc.perform(get("/status")).andExpect(status().isOk());
    }

    @Test
    public void CarPoolingController_PutCars_ReturnOkStatus() throws Exception {
        var cars = List.of(
                new Car(1, 4),
                new Car(2, 6));
        mvc.perform(put("/cars")
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(cars)))
                .andExpect(status().isOk());
    }

    @Test
    public void CarPoolingController_PutCars_ReturnUnsupportedMediaTypeStatus() throws Exception {
        mvc.perform(put("/cars")
                        .contentType(MediaType.TEXT_PLAIN).
                        content(objectMapper.writeValueAsString("")))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void CarPoolingController_PutCars_ReturnBadRequestStatus() throws Exception {
        var cars = List.of(
                new Car(1, 3),
                new Car(2, 7));
        mvc.perform(put("/cars")
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(cars)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void CarPoolingController_PostJourney_ReturnOkStatus() throws Exception {
        var journey = new Journey(1, 4);
        mvc.perform(post("/journey").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journey)))
                .andExpect(status().isAccepted());
    }

    @Test
    public void CarPoolingController_PostJourney_ReturnBadRequestStatus() throws Exception {
        var journey = new Journey(0, 4);
        mvc.perform(post("/journey").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journey)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void CarPoolingController_PostDropoff_ReturnOkStatus() throws Exception {
        var journey = new Journey(2, 4);
        mvc.perform(post("/journey").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journey)))
                .andExpect(status().isAccepted());
        mvc.perform(post("/dropoff").contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content("ID=" + journey.getId()))
                .andExpect(status().isOk());
    }

    @Test
    public void CarPoolingController_PostDropoff_ReturnBadRequestStatus() throws Exception {
        var journey = new Journey(3, 4);
        mvc.perform(post("/journey").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journey)))
                .andExpect(status().isAccepted());
        mvc.perform(post("/dropoff").contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content("ID=0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void CarPoolingController_PostDropoff_ReturnNotFoundStatus() throws Exception {
        var journey = new Journey(4, 4);
        mvc.perform(post("/journey").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journey)))
                .andExpect(status().isAccepted());
        mvc.perform(post("/dropoff").contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .content("ID=10"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void itShouldManageJourney() throws Exception {
        var cars = List.of(
                new Car(1, 6),
                new Car(2, 6));
        var journey = new Journey(5, 3);
        mvc.perform(put("/cars").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cars)))
                .andExpect(status().isOk());
        mvc.perform(post("/journey").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journey)))
                .andExpect(status().isAccepted());
        mvc.perform(post("/locate").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("ID=" + journey.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"maxSeats\":6,\"availableSeats\":3}"));
        mvc.perform(post("/dropoff").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("ID=" + journey.getId()))
                .andExpect(status().isOk());
    }
}
