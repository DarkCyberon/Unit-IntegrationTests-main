package com.example.demo.integration;

import com.example.demo.config.BookingProperties;
import com.example.demo.model.BookingResponse;
import com.example.demo.model.BookingResponse.Booking;
import com.example.demo.model.BookingResponse.BookingDates;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Random;

@Service
@AllArgsConstructor
public class BookingClient {
    private final RestTemplate restTemplate;
    private final BookingProperties properties;

    private final ObjectMapper mapper;
    private static final String BOOKING_PATH = "/booking";

    public int createBooking(String name) {

        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        var bookingRequest = new Booking(
                name,
                "some-last-name",
                new Random().nextInt(),
                true,
                new BookingDates(
                        LocalDate.now(),
                        LocalDate.now().plusDays(7)
                ),
                "No needs"
        );

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Booking> entity = new HttpEntity<>(bookingRequest, headers);
        var result = restTemplate.postForObject(properties.getUrl() + BOOKING_PATH, entity, BookingResponse.class);

        return result.getBookingId();
    }
}
