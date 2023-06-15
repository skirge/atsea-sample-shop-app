package com.docker.atsea.ws.students;


import org.springframework.stereotype.Component;

import javax.jws.WebService;
import java.util.LinkedHashMap;
import java.util.Map;

@WebService(endpointInterface = "com.docker.atsea.ws.students.Students")
@Component
public class StudentsImpl implements Students {
    private Map<Integer, Student> students
            = new LinkedHashMap<Integer, Student>();

    public String hello(String name) {
        return "Hello " + name;
    }

    public String helloStudent(Student student) {
        if(student!=null) {
            students.put(students.size() + 1, student);
            return "Hello " + student.getName();
        } else {
            throw new IllegalArgumentException("Student is required!");
        }
    }

    public Map<Integer, Student> getStudents() {
        return students;
    }
}