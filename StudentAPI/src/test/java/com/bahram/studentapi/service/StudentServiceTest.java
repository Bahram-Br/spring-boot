package com.bahram.studentapi.service;

import com.bahram.studentapi.exception.DuplicateEmailException;
import com.bahram.studentapi.exception.StudentNotFoundException;
import com.bahram.studentapi.model.Student;
import com.bahram.studentapi.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    public void getStudentThrowsWhenMissing() {
        when(studentRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> studentService.getStudent(1));

        System.out.println("StudentServiceTest: getStudentThrowsWhenMissing");
    }

    @Test
    public void addStudentThrowsWhenEmailExists() {
        Student s = new Student(20, "Tom", "M", "Math");
        s.setEmail("tom@school.edu");
        when(studentRepository.existsByEmail("tom@school.edu")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> studentService.addStudent(s));
    }
}
