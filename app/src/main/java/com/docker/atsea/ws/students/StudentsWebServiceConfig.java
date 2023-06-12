package com.docker.atsea.ws.students;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

@Configuration
public class StudentsWebServiceConfig {

    @Autowired
    private Bus bus;

    @Bean
    Endpoint getEndpoint() {
        EndpointImpl studentEndpoint = new EndpointImpl(bus, new StudentsImpl());
        studentEndpoint.publish("/HelloStudents");
        return studentEndpoint;
    }
}
