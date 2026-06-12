package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.model.StockStatus;
import com.ssemi.sampleorder.service.MonitoringService;
import com.ssemi.sampleorder.view.ConsoleView;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MonitoringController {

    private final MonitoringService service;
    private final ConsoleView view;

    public MonitoringController(MonitoringService service, ConsoleView view) {
        this.service = service;
        this.view    = view;
    }

    public void run() {
        while (true) {
            view.printMonitoringMenu();
            int choice = view.promptInt();
            switch (choice) {
                case 1 -> showOrderStatus();
                case 2 -> showStockStatus();
                case 0 -> { return; }
                default -> view.printMessage("잘못된 입력입니다.");
            }
        }
    }

    private void showOrderStatus() {
        try {
            view.printOrderStatusSummary(service.getOrderSummary());
        } catch (IOException e) {
            view.printMessage("[오류] 주문 현황 조회 실패: " + e.getMessage());
        }
    }

    private void showStockStatus() {
        try {
            List<Sample> samples = service.getAllSamples();
            Map<String, StockStatus> statusMap = new LinkedHashMap<>();
            for (Sample s : samples) {
                int confirmedQty = service.getTotalConfirmedQty(s.getSampleId());
                statusMap.put(s.getSampleId(), service.judgeStockStatus(s, confirmedQty));
            }
            view.printStockStatusSummary(samples, statusMap);
        } catch (IOException e) {
            view.printMessage("[오류] 재고 현황 조회 실패: " + e.getMessage());
        }
    }
}
