package com.bahram.studentapi.repository;

import com.bahram.studentapi.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    Student findByName(String name);
    List<Student> findByMajor(String major);
    Student findByEmail(String email);
    List<Student> findByAgeGreaterThan(int age);
}