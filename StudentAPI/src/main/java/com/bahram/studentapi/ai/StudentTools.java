package com.bahram.studentapi.ai;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.bahram.studentapi.model.Student;
import com.bahram.studentapi.service.StudentService;

@Component 
public class StudentTools {
    
    private final StudentService studentService;

    public StudentTools(StudentService studentService) {
        this.studentService = studentService;
    }   

    @Tool(description = "Get all students in the database")
    public List<Student> getAllStudents() {
        return studentService.getStudents();
    }

    @Tool(description = "Find a student by major, for example: 'Computer Science'")
    public List<Student> findStudentByMajor(
        @ToolParam (description = "The major to search for") String major){


            return studentService.studentsByMajor(major);
    }

    @Tool(description = "Add a new student to the database")
    public Student addNewStudent(
        @ToolParam(description = "The name of the student") String name,
        @ToolParam(description = "The age of the student") int age,
        @ToolParam(description = "The major of the student") String major,
        @ToolParam(description = "The gender of the student") String gender,
        @ToolParam(description = "The email of the student", required = false) String email) {

            Student student = new Student(age, name, gender, major);
            
            if (email != null && !email.isBlank()) {
                student.setEmail(email);
            }

            return studentService.addStudent(student);
        } 
}
