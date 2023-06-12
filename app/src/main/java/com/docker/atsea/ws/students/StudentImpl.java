package com.docker.atsea.ws.students;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Student")
public class StudentImpl implements Student {
    private String name;

    public StudentImpl(String name) {
        this.name = name;
    }

    public StudentImpl() {};

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}