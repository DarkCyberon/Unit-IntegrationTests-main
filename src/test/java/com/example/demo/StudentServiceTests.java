package com.example.demo;

import com.example.demo.core.StudentRepository;
import com.example.demo.core.StudentService;
import com.example.demo.exception.StudentNotFoundException;
import com.example.demo.model.Gender;
import com.example.demo.model.Student;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8080)
@ActiveProfiles("test")
public class StudentServiceTests {

    @Autowired
    private StudentRepository studentRepository;

    //Unit test
    @Test
    public void returnExistedStudent() {
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
    public void throwExceptionForNotExistedStudent()  {
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
}

