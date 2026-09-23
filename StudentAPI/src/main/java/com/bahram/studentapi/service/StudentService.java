package com.bahram.studentapi.service;

import com.bahram.studentapi.dto.StudentRequest;
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

    public List<Student> getStudents(){
        return studentRepository.findAll();
    }

    public Student getStudent(int id){
        return studentRepository.findById(id)
                .orElseThrow(
                () -> new StudentNotFoundException(id)
        );
    }

    public Student addStudent(Student student){
        return studentRepository.save(student);
    }

    public Student updateStudent(int id, StudentRequest request){

        Student existingStu = studentRepository.findById(id)
                .orElseThrow( ()-> new StudentNotFoundException(id) );

        existingStu.setAge(request.getAge());
        existingStu.setName(request.getName());
        existingStu.setGender(request.getGender());
        existingStu.setMajor(request.getMajor());

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            existingStu.setEmail(request.getEmail());
        }

        return studentRepository.save(existingStu);
    }

    public Student deleteStudent(int id){

        Student student = studentRepository.findById(id).orElseThrow( () -> new StudentNotFoundException(id));

        studentRepository.deleteById(id);

        return student;
    }

    public List<Student> studentByName(String name){

        return studentRepository.findByName(name);
    }

    public List<Student> studentsByMajor(String major){
        return studentRepository.findByMajor(major);
    }

    public List<Student> studentByEmail(String email){
        return studentRepository.findByEmail(email);
    }

    public List<Student> studentsOlderThan(int age){
        return studentRepository.findByAgeGreaterThan(age);

    }
}
