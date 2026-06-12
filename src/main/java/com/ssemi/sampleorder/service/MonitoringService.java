package com.ssemi.sampleorder.service;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.model.StockStatus;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MonitoringService {

    private static final Set<OrderStatus> MONITORED_STATUSES = EnumSet.of(
            OrderStatus.RESERVED, OrderStatus.CONFIRMED,
            OrderStatus.PRODUCING, OrderStatus.RELEASE
    );

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;

    public MonitoringService(SampleRepository sampleRepository, OrderRepository orderRepository) {
        this.sampleRepository = sampleRepository;
        this.orderRepository  = orderRepository;
    }

    public Map<OrderStatus, List<Order>> getOrderSummary() throws IOException {
        return orderRepository.findAll().stream()
                .filter(o -> MONITORED_STATUSES.contains(o.getStatus()))
                .collect(Collectors.groupingBy(Order::getStatus));
    }

    public StockStatus judgeStockStatus(Sample sample, int confirmedQty) {
        if (sample.getStock() == 0) return StockStatus.DEPLETED;
        if (sample.getStock() < confirmedQty) return StockStatus.INSUFFICIENT;
        return StockStatus.SUFFICIENT;
    }

    public List<Sample> getAllSamples() throws IOException {
        return sampleRepository.findAll();
    }

    // 재고 상태 판단 기준: CONFIRMED(출고 대기) + PRODUCING(생산 중) 수량 합산
    public int getTotalPendingQty(String sampleId) throws IOException {
        return orderRepository.findAll().stream()
                .filter(o -> o.getSampleId().equals(sampleId))
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED
                          || o.getStatus() == OrderStatus.PRODUCING)
                .mapToInt(Order::getQuantity)
                .sum();
    }
}
