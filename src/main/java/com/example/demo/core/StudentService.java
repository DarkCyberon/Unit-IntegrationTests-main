package com.example.demo.core;

import com.example.demo.integration.BookingClient;
import com.example.demo.integration.ChuckClient;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.StudentNotFoundException;
import com.example.demo.integration.ChuckClient;
import com.example.demo.model.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final ChuckClient chuckClient;
    private final BookingClient bookingClient;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(
                        "Студент с id = " + studentId + " не существует в БД"
                ));
    }

    public void addStudent(Student student) {
        Boolean existsEmail = studentRepository
                .selectExistsEmail(student.getEmail());
        if (existsEmail) {
            throw new BadRequestException(
                    "Email " + student.getEmail() + " taken");
        }
        student.setJoke(chuckClient.getJoke().getValue());
        student.setBookingId(bookingClient.createBooking(student.getName()));
        studentRepository.save(student);
    }
    public void deleteStudent(Long studentId) {
        if(!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException(
                    "Student with id " + studentId + " does not exists");
        }
        studentRepository.deleteById(studentId);
    }
}
