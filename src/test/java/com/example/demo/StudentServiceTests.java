package com.example.demo;

import com.example.demo.core.StudentRepository;
import com.example.demo.core.StudentService;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.StudentNotFoundException;
import com.example.demo.integration.BookingClient;
import com.example.demo.integration.ChuckClient;
import com.example.demo.model.ChuckResponse;
import com.example.demo.model.Gender;
import com.example.demo.model.Student;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8080)
@ActiveProfiles("test")
public class StudentServiceTests {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ChuckClient chuckClient;
    @Autowired
    private BookingClient bookingClient;

    //Unit test
    @Test
    public void getReturnExistedStudent() {
        //arrange
        Long id = 1L;
        Student expectedStudent = new Student(id, "Gnome", "javaGnome@mail.com", Gender.OTHER, "", 0);
        studentRepository = mock(StudentRepository.class);
        when(studentRepository
                .findById(id))
                .thenReturn(Optional.of(expectedStudent));
        StudentService studentService = new StudentService(studentRepository, null, null);
        //act
        Student actualStudent = studentService.getStudent(id);
        //assert
        assertEquals(expectedStudent, actualStudent);
    }

    //Unit test
    @Test
    public void throwExceptionForGetNotExistedStudent()  {
        //arrange
        Long id = 1L;
        studentRepository = mock(StudentRepository.class);
        when(studentRepository
                .findById(id))
                .thenReturn(Optional.empty());
        StudentService studentService = new StudentService(studentRepository, null, null);
        //assert
        assertThrows(StudentNotFoundException.class, () -> studentService.getStudent(id));
    }

    //Unit test
    @Test
    public void addNewStudent() {
        //arrange
        String joke = "";
        Long id = 1L;
        Student newStudent = new Student(id, "Gnome", "javaGnome@mail.com", Gender.OTHER, "", 0);
        chuckClient = mock(ChuckClient.class);
        studentRepository = mock(StudentRepository.class);
        bookingClient = mock(BookingClient.class);
        when(studentRepository
                .selectExistsEmail(newStudent.getEmail()))
                .thenReturn(false);
        when(chuckClient
                .getJoke())
                .thenReturn(new ChuckResponse(joke));
        when(bookingClient
                .createBooking(newStudent.getName()))
                .thenReturn(id.intValue());
        StudentService studentService = new StudentService(studentRepository, chuckClient, bookingClient);
        //assert
        assertDoesNotThrow(() -> studentService.addStudent(newStudent));
    }

    //Unit test
    @Test
    public void trowExceptionForAddExistedStudent() {
        //arrange
        String joke = "";
        Long id = 1L;
        Student newStudent = new Student(id, "Gnome", "javaGnome@mail.com", Gender.OTHER, "", 0);
        studentRepository = mock(StudentRepository.class);
        when(studentRepository
                .selectExistsEmail(newStudent.getEmail()))
                .thenReturn(true);
        StudentService studentService = new StudentService(studentRepository, null, null);
        //assert
        assertThrows(BadRequestException.class, () -> studentService.addStudent(newStudent));
    }

    //Unit test
    @Test
    public void deleteExistedStudent() {
        //arrange
        Long id = 1L;
        Student expectedStudent = new Student(id, "Gnome", "javaGnome@mail.com", Gender.OTHER, "", 0);
        studentRepository = mock(StudentRepository.class);
        when(studentRepository
                .existsById(id))
                .thenReturn(true);
        StudentService studentService = new StudentService(studentRepository, null, null);
        //assert
        assertDoesNotThrow(() -> studentService.deleteStudent(id));
    }

    //Unit test
    @Test
    public void throwExceptionForDeleteNotExistedStudent() {
        //arrange
        Long id = 1L;
        Student expectedStudent = new Student(id, "Gnome", "javaGnome@mail.com", Gender.OTHER, "", 0);
        studentRepository = mock(StudentRepository.class);
        when(studentRepository
                .existsById(id))
                .thenReturn(false);
        StudentService studentService = new StudentService(studentRepository, null, null);
        //assert
        assertThrows(StudentNotFoundException.class, () -> studentService.deleteStudent(id));
    }
}

