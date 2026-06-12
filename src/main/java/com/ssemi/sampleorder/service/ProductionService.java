package com.ssemi.sampleorder.service;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.ProductionLine;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.ProductionLineRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductionService {

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final ProductionLineRepository productionLineRepository;

    public ProductionService(SampleRepository sampleRepository,
                             OrderRepository orderRepository,
                             ProductionLineRepository productionLineRepository) {
        this.sampleRepository      = sampleRepository;
        this.orderRepository       = orderRepository;
        this.productionLineRepository = productionLineRepository;
    }

    public void enqueue(String orderId) throws IOException {
        Order order   = orderRepository.findById(orderId);
        Sample sample = sampleRepository.findById(order.getSampleId());
        int shortfall = Math.max(0, order.getQuantity() - sample.getStock());
        ProductionLine line = new ProductionLine(orderId, shortfall,
                sample.getYieldRate(), sample.getAvgProductionTime());
        productionLineRepository.enqueue(line);
    }

    public void completeNext() throws IOException {
        ProductionLine head = productionLineRepository.dequeueHead();
        Order order   = orderRepository.findById(head.getOrderId());
        Sample sample = sampleRepository.findById(order.getSampleId());
        sample.setStock(sample.getStock() + head.getScheduledQty());
        sampleRepository.update(sample);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.update(order);
    }

    public List<ProductionLine> getQueue() throws IOException {
        return productionLineRepository.findAll();
    }

    public List<ProductionLineDetail> getQueueDetails() throws IOException {
        List<ProductionLine> queue = productionLineRepository.findAll();
        List<ProductionLineDetail> details = new ArrayList<>();
        for (ProductionLine pl : queue) {
            Order order   = orderRepository.findById(pl.getOrderId());
            Sample sample = sampleRepository.findById(order.getSampleId());
            details.add(new ProductionLineDetail(pl, sample.getName(),
                    order.getQuantity(), sample.getStock()));
        }
        return details;
    }
}
