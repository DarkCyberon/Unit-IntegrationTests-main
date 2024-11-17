package com.example.demo;

import com.example.demo.core.StudentRepository;
import com.example.demo.core.StudentService;
import com.example.demo.integration.BookingClient;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8080)
@ActiveProfiles("test")
public class BookingClientTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private BookingClient bookingClient;

    @Autowired
    private StudentRepository studentRepository;

    //Integration test
    @Test
    public void bookingIdIsRight() throws JsonProcessingException {
        //Arrange
        int bookingId = 1;
        BookingResponse response = new BookingResponse(
                bookingId,
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
                .post("/booking")
                .willReturn(okJson(objectMapper.writeValueAsString(response)))
        );
        stubFor(WireMock
                .get("/jokes/random")
                .willReturn(okJson(objectMapper.writeValueAsString(new ChuckResponse(""))))
        );
        Student student = new Student("Gnome", "test@mail.com", Gender.MALE);
        testRestTemplate.postForEntity(
                "/api/v1/students",
                student,
                void.class
        );
        // act
        Student extractedStudent = testRestTemplate.getForObject(
                "/api/v1/students/1",
                Student.class
        );
        // assert
        assertEquals(bookingId, extractedStudent.getBookingId());
    }
    //Integration test
    @Test
    public void studentSavedToRepositoryWithRightBookingId() {
        //arrange
        int bookingId = 1;
        Student student = new Student("Gnome", "javaGnome@mail.com", Gender.OTHER);

        var chuckClient = mock(ChuckClient.class);
        when(chuckClient.getJoke()).thenReturn( new ChuckResponse("Случайная шутка"));
        
        var bookingClient = mock(BookingClient.class);
        when(bookingClient.createBooking(student.getName())).thenReturn(bookingId);

        StudentService studentService = new StudentService(studentRepository, chuckClient, bookingClient);

        //act
        studentService.addStudent(student);

        //assert
        Student expectedStudent = studentRepository.findById(student.getId()).orElse(null);
        assertNotEquals(expectedStudent, null);
        assertEquals(expectedStudent.getBookingId(), bookingId);
    }

    //Unit test
    @Test
    public void sendCorrectStudentName() throws JsonProcessingException {
        //arrange
        String studentName = "Gnome";
        BookingResponse response = new BookingResponse(
                1,
                new BookingResponse.Booking(
                        studentName,
                        "",
                        0,
                        false,
                        new BookingResponse.BookingDates(LocalDate.MIN, LocalDate.MAX),
                        ""
                )
        );
        stubFor(WireMock
                .post("/booking")
                .willReturn(okJson(objectMapper.writeValueAsString(response)))
        );
        //act
        bookingClient.createBooking(studentName);
        //assert
        verify(postRequestedFor(urlEqualTo("/booking"))
                .withRequestBody(matchingJsonPath("$.firstname", equalTo(studentName))));
    }
    //Unit test
    @Test
    public void throwNPEWhenCreateBookingWithNullResponse() {
        //arrange
        String studentName = "Gnome";
        stubFor(WireMock
                .post("/booking")
                .willReturn(null)
        );
        //assert
        assertThrows(NullPointerException.class, () -> bookingClient.createBooking(studentName));
    }
}

