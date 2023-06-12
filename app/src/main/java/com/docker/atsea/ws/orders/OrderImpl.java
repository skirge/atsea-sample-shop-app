package com.docker.atsea.ws.orders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@javax.jws.WebService(serviceName = "OrderService", portName = "OrderPort",
        targetNamespace = "http://service.ws.sample.order/",
        endpointInterface = "com.docker.atsea.ws.orders.Order")
public class OrderImpl implements Order {

    private static final Logger LOG = Logger.getLogger(OrderImpl.class.getName());

    private static final java.util.Map<Integer, com.docker.atsea.model.Order> orders = new HashMap<Integer, com.docker.atsea.model.Order>();

    @Override
    public int createOrder(com.docker.atsea.model.Order order) {
        LOG.info("Executing operation createOrder:[" + order.toString() + "]");
        orders.put(orders.size()+1, order);
        return orders.size();
    }

    @Override
    public int deleteOrder(Integer orderId) {
        orders.remove(orderId);
        return orders.size();
    }

    @Override
    public Map<Integer, com.docker.atsea.model.Order> getOrders() {
        return orders;
    }
}
