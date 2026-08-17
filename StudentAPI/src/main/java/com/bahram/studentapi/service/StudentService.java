package com.bahram.studentapi.service;

import com.bahram.studentapi.exception.StudentNotFoundException;
import com.bahram.studentapi.model.Student;
import com.bahram.studentapi.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Searches all students
    public List<Student> getStudents(){
        return studentRepository.findAll();
    }

    // Searches Student by ID
    public Student getStudent(int id){
        return studentRepository.findById(id)
                .orElseThrow(
                () -> new StudentNotFoundException(id)
        );
    }

    // Adds a new student
    public Student addStudent(Student student){
        return studentRepository.save(student);
    }

    // Updates a student
    public Student updateStudent(int id, Student student){
        return studentRepository.save(student);
    }

    // Deletes a student
    public Student deleteStudent(int id){

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null){
            return null;
        }

        studentRepository.deleteById(id);

        return student;
    }

    public Student studentByName(String name){

        return studentRepository.findByName(name);
    }

    public List<Student> studentsByMajor(String major){
        return studentRepository.findByMajor(major);
    }

    public Student studentByEmail(String email){
        return studentRepository.findByEmail(email);
    }

    public List<Student> studentByOlderThan(int age){
        if (age < 15) {
            return null;
        }
        return studentRepository.findByAgeGreaterThan(age);

    }
}
