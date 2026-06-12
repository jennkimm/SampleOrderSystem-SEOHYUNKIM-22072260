package com.ssemi.sampleorder.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderRepository {

    private final JsonFileRepository<Order> fileRepo;

    public OrderRepository(String filePath) {
        this.fileRepo = new JsonFileRepository<>(filePath, new TypeReference<List<Order>>() {});
    }

    public void save(Order order) throws IOException {
        List<Order> all = new ArrayList<>(fileRepo.readAll());
        all.add(order);
        fileRepo.writeAll(all);
    }

    public void update(Order order) throws IOException {
        List<Order> all = fileRepo.readAll().stream()
                .map(o -> o.getOrderId().equals(order.getOrderId()) ? order : o)
                .collect(Collectors.toCollection(ArrayList::new));
        fileRepo.writeAll(all);
    }

    public List<Order> findAll() throws IOException {
        return fileRepo.readAll();
    }

    public List<Order> findByStatus(OrderStatus status) throws IOException {
        return fileRepo.readAll().stream()
                .filter(o -> o.getStatus() == status)
                .toList();
    }

    public Order findById(String orderId) throws IOException {
        return fileRepo.readAll().stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ID " + orderId + " 에 해당하는 주문을(를) 찾을 수 없습니다."));
    }
}
