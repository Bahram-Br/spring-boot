package com.bahram.studentapi.service;

import com.bahram.studentapi.dto.StudentRequest;
import com.bahram.studentapi.exception.DuplicateEmailException;
import com.bahram.studentapi.exception.StudentNotFoundException;
import com.bahram.studentapi.model.Student;
import com.bahram.studentapi.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

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

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public Student addStudent(Student student){
        student.setEmail(normalizeEmail(student.getEmail()));
        if (student.getEmail() != null && studentRepository.existsByEmail(student.getEmail())) {
            throw new DuplicateEmailException(student.getEmail());
        }
        return studentRepository.save(student);
    }

    public Student updateStudent(int id, StudentRequest request){

        Student existingStu = studentRepository.findById(id)
                .orElseThrow( ()-> new StudentNotFoundException(id) );

        existingStu.setAge(request.getAge());
        existingStu.setName(request.getName());
        existingStu.setGender(request.getGender());
        existingStu.setMajor(request.getMajor());

        String email = normalizeEmail(request.getEmail());
        if (email != null && !email.isBlank()) {
            if (studentRepository.existsByEmailAndIdNot(email, id)) {
                throw new DuplicateEmailException(email);
            }
            existingStu.setEmail(email);
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
