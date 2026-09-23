package com.bahram.studentapi.controller;

import com.bahram.studentapi.model.Student;

import com.bahram.studentapi.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.bahram.studentapi.dto.StudentRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Validated
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {

        this.studentService = studentService;

    }

    @PostMapping("/students")
    public Student addStudent(@Valid @RequestBody StudentRequest request){

        Student student = new Student(
                request.getAge(),
                request.getName(),
                request.getGender(),
                request.getMajor()
        );

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            student.setEmail(request.getEmail());
        }

         return studentService.addStudent(student);
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable int id){

        return ResponseEntity.ok(studentService.getStudent(id));
    }

    @GetMapping("/students")
    public List<Student> getStudents(){
        return studentService.getStudents();
    }

    @PutMapping("/students/{id}")
    public Student updateStudent(@PathVariable int id,
                                 @Valid @RequestBody StudentRequest request){
        return studentService.updateStudent(id, request);
    }

    @DeleteMapping("/students/{id}")
    public Student deleteStudent(@PathVariable int id){
        return studentService.deleteStudent(id);
    }

    @GetMapping("/students/name/{name}")
    public List<Student> getStudentByName(@PathVariable String name){
        return studentService.studentByName(name);
    }

    @GetMapping("/students/major/{major}")
    public List<Student> studentsByMajor(@PathVariable String major){
        return studentService.studentsByMajor(major);
    }

    @GetMapping("/students/email/{email}")
    public List<Student> studentByEmail(@PathVariable String email){
        return studentService.studentByEmail(email);
    }

    @GetMapping("/students/older-than/{age}")
    public List<Student> studentsOlderThan(@PathVariable @Min(15) int age){
        return studentService.studentsOlderThan(age);
    }

}
