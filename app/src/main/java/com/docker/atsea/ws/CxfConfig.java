package com.docker.atsea.ws;

import com.docker.atsea.ws.customers.CustomerImpl;
import com.docker.atsea.ws.orders.OrderImpl;
import com.docker.atsea.ws.students.StudentsImpl;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;
import java.util.Map;

@Configuration
public class CxfConfig {
    @Autowired
    private Bus bus;

    @Autowired
    StudentsImpl s;

    @Autowired
    CustomerImpl c;

    @Autowired
    OrderImpl o;

    @Bean
    public Endpoint getEndpoint1() {
        EndpointImpl studentEndpoint = new EndpointImpl(bus, s);
        studentEndpoint.publish("/StudentService");
        return studentEndpoint;
    }

    @Bean
    public Endpoint getEndpoint2() {
        EndpointImpl endpoint = new EndpointImpl(bus, c);
        Map<String, Object> props = endpoint.getProperties();
        props.put(Message.FAULT_STACKTRACE_ENABLED, true);
        props.put(Message.EXCEPTION_MESSAGE_CAUSE_ENABLED, true);
        endpoint.setProperties(props);
        endpoint.publish("/CustomerService");
        return endpoint;
    }

    @Bean
    public Endpoint getEndpoint3() {
        EndpointImpl endpoint = new EndpointImpl(bus,o);
        endpoint.publish("/OrderService");
        return endpoint;
    }
}
