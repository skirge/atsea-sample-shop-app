package com.docker.atsea.ws.students;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;

@WebService
public interface Students {
        public String hello(@XmlElement(required=true, nillable=false) String name);

        public String helloStudent(@XmlElement(required=true, nillable=false) Student student);

        @XmlJavaTypeAdapter(StudentMapAdapter.class)
        public Map<Integer, Student> getStudents();
    }

