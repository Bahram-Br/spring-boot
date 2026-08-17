package com.bahram.studentapi.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@JsonPropertyOrder({"id", "age", "name", "gender", "email", "major"})
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Integer id;

    @Min(1)
    private  Integer age;

    @NotBlank
    private String name;

    private String gender;
    private String email;
    private String major;

    public Student(){

    }

    public Student(Integer age, String name, String gender, String major){

        this.age = age;
        this.name = name;
        this.gender = gender;
        this.email = name.toLowerCase() + "@stu" + ".email.com";
        this.major = major;

    }

    public Integer getId(){
        return id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.email = name.toLowerCase() + "@stu.email.com";
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }
}
