package com.example.demo;

import com.example.demo.config.ChuckProperties;
import com.example.demo.integration.ChuckClient;
import com.example.demo.model.BookingResponse;
import com.example.demo.model.ChuckResponse;
import com.example.demo.model.Gender;
import com.example.demo.model.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8080)
@ActiveProfiles("test")
public class ChuckServiceTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ChuckProperties chuckProperties;

    //Integration test
    @Test
    public void returnDefaultAnswerWhenServerHasAnError() throws JsonProcessingException {
        //arrange
        ChuckResponse defaultResponse = new ChuckResponse("Случайная шутка");

        var response = new BookingResponse(
                1,
                new BookingResponse.Booking(
                        "",
                        "",
                        0,
                        false,
                        new BookingResponse.BookingDates(LocalDate.MIN, LocalDate.MAX),
                        ""
                )
        );
        stubFor(WireMock
                .get("/jokes/random")
                .willReturn(status(500))
        );
        stubFor(WireMock
                .post("/booking")
                .willReturn(okJson(objectMapper.writeValueAsString(response)))
        );
        Student student = new Student("Ivan", "test@mail.com", Gender.MALE);
        //act
        testRestTemplate.postForObject(
                "/api/v1/students",
                student,
                void.class
        );
        Student extractedStudent = testRestTemplate.getForObject("/api/v1/students/1", Student.class);
        //assert
        assertEquals(defaultResponse.getValue(), extractedStudent.getJoke());
    }

    //Unit test
    @Test
    public void throwExceptionWhenCodeIsNot200And500() {
        // arrange
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(
                eq(chuckProperties.getUrl()),
                eq(HttpMethod.GET),
                isNull(),
                Mockito.<ParameterizedTypeReference<ChuckResponse>>any())
        ).thenThrow(new HttpServerErrorException(HttpStatus.NOT_FOUND));
        ChuckClient client = new ChuckClient(restTemplate, chuckProperties);
        //assert
        assertThrows(HttpServerErrorException.class, client::getJoke);
    }
    //Unit test
    @Test
    public void returnJokeWhenCodeIs200() {
        // arrange
        RestTemplate restTemplate = mock(RestTemplate.class);
        String testJoke = "Тестовая шутка";
        when(restTemplate.exchange(
                eq(chuckProperties.getUrl()),
                eq(HttpMethod.GET),
                isNull(),
                Mockito.<ParameterizedTypeReference<ChuckResponse>>any())
        ).thenReturn(ResponseEntity.ok(new ChuckResponse(testJoke)));
        ChuckClient client = new ChuckClient(restTemplate, chuckProperties);
        //act
        ChuckResponse response = client.getJoke();
        //assert
        assertEquals(testJoke, response.getValue());
    }
}

