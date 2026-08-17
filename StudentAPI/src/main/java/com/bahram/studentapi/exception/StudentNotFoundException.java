package com.bahram.studentapi.exception;

import com.bahram.studentapi.model.Student;

public class StudentNotFoundException extends RuntimeException {

//    public StudentNotFoundException(Student student) {
//        super("Student not found with id: " + student.getId());
//    }
    public StudentNotFoundException(int id){
        super("Student not found with id: " + id);
    }


}
