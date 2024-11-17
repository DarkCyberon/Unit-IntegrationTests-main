package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookingResponse {
    @JsonProperty("bookingid")
    private int bookingId;

    @JsonProperty("booking")
    private Booking booking;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Booking {
        @JsonProperty("firstname")
        private String firstName;

        @JsonProperty("lastname")
        private String lastName;

        @JsonProperty("totalprice")
        private int totalPrice;

        @JsonProperty("depositpaid")
        private boolean depositPaid;

        @JsonProperty("bookingdates")
        private BookingDates bookingDates;

        @JsonProperty("additionalneeds")
        private String additionalNeeds;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class BookingDates {

        @JsonProperty("checkin")
        private LocalDate checkIn;

        @JsonProperty("checkout")
        private LocalDate checkOut;


    }
}
