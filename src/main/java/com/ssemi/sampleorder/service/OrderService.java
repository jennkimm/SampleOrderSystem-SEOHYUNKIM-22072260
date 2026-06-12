package com.ssemi.sampleorder.service;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.ProductionLine;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderService {

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;

    public OrderService(SampleRepository sampleRepository, OrderRepository orderRepository) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
    }

    public void place(String sampleId, String customerName, int quantity) throws IOException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다. 입력값: " + quantity);
        }
        sampleRepository.findById(sampleId);
        String orderId = "O" + System.currentTimeMillis();
        orderRepository.save(new Order(orderId, sampleId, customerName, quantity, OrderStatus.RESERVED));
    }

    public OrderStatus approve(String orderId, List<ProductionLine> productionQueue) throws IOException {
        Order order = orderRepository.findById(orderId);
        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new IllegalStateException("RESERVED 상태의 주문만 승인할 수 있습니다. 현재 상태: " + order.getStatus());
        }
        int effectiveStock = computeEffectiveStock(order.getSampleId(), productionQueue);
        OrderStatus next = effectiveStock >= order.getQuantity() ? OrderStatus.CONFIRMED : OrderStatus.PRODUCING;
        order.setStatus(next);
        orderRepository.update(order);
        return next;
    }

    private int computeEffectiveStock(String sampleId, List<ProductionLine> productionQueue) throws IOException {
        Sample sample = sampleRepository.findById(sampleId);

        int confirmedQty = orderRepository.findByStatus(OrderStatus.CONFIRMED).stream()
                .filter(o -> o.getSampleId().equals(sampleId))
                .mapToInt(Order::getQuantity).sum();

        List<Order> producingOrders = orderRepository.findByStatus(OrderStatus.PRODUCING).stream()
                .filter(o -> o.getSampleId().equals(sampleId))
                .collect(Collectors.toList());

        int producingQty = producingOrders.stream().mapToInt(Order::getQuantity).sum();

        Set<String> producingOrderIds = producingOrders.stream()
                .map(Order::getOrderId).collect(Collectors.toSet());

        int scheduledQty = productionQueue.stream()
                .filter(pl -> producingOrderIds.contains(pl.getOrderId()))
                .mapToInt(ProductionLine::getScheduledQty).sum();

        return sample.getStock() + scheduledQty - confirmedQty - producingQty;
    }

    public void reject(String orderId) throws IOException {
        Order order = orderRepository.findById(orderId);
        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new IllegalStateException("RESERVED 상태의 주문만 거절할 수 있습니다. 현재 상태: " + order.getStatus());
        }
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.update(order);
    }

    public void release(String orderId) throws IOException {
        Order order = orderRepository.findById(orderId);
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("CONFIRMED 상태의 주문만 출고할 수 있습니다. 현재 상태: " + order.getStatus());
        }
        Sample sample = sampleRepository.findById(order.getSampleId());
        sample.setStock(sample.getStock() - order.getQuantity());
        sampleRepository.update(sample);
        order.setStatus(OrderStatus.RELEASE);
        orderRepository.update(order);
    }

    public int getShortfall(String orderId, List<ProductionLine> productionQueue) throws IOException {
        Order order = orderRepository.findById(orderId);
        int effectiveStock = computeEffectiveStock(order.getSampleId(), productionQueue);
        return Math.max(0, order.getQuantity() - effectiveStock);
    }

    public List<Order> getReserved() throws IOException {
        return orderRepository.findByStatus(OrderStatus.RESERVED);
    }

    public List<Order> getByStatus(OrderStatus status) throws IOException {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getAll() throws IOException {
        return orderRepository.findAll();
    }
}
