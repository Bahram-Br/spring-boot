package com.bahram.studentapi.controller;

import com.bahram.studentapi.model.Student;
import com.bahram.studentapi.repository.StudentRepository;
import com.bahram.studentapi.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bahram.studentapi.dto.StudentRequest;

import java.util.List;

@RestController
public class Controller {

    private final StudentService studentService;

    private final StudentRepository studentRepository;

    public Controller(StudentService studentService,
                      StudentRepository studentRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }

    @PostMapping("/students")
    public Student addstudent(@Valid @RequestBody StudentRequest request){

         Student student = new Student(
                 request.getAge(),
                 request.getName(),
                 request.getGender(),
                 request.getMajor()
         );
         return studentService.addStudent(student);
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Student> student(@PathVariable int id){
        Student student =  studentService.getStudent(id);

        if(student == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    @GetMapping("/students")
    public List<Student> students(){
        return studentService.getStudents();
    }

    @PutMapping("/students/{id}")
    public Student updateStudent(@PathVariable int id,
                                 @RequestBody Student student){
        return studentService.updateStudent(id, student);
    }

    @DeleteMapping("/students/{id}")
    public Student deleteStudent(@PathVariable int id){
        return studentService.deleteStudent(id);
    }

    @GetMapping("/students/name/{name}")
    public Student getStudentByName(@PathVariable String name){
        return studentService.studentByName(name);
    }

    @GetMapping("/students/major/{major}")
    public List<Student> studentsByMajor(@PathVariable String major){
        return studentService.studentsByMajor(major);
    }

    @GetMapping("/students/email/{email}")
    public Student studentByEmail(@PathVariable String email){
        return studentService.studentByEmail(email);
    }

    @GetMapping("/students/age/{age}")
    public List<Student> studentsByAge(@PathVariable int age){
        return studentService.studentByOlderThan(age);
    }

}
