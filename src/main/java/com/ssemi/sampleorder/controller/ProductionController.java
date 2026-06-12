package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.service.OrderService;
import com.ssemi.sampleorder.view.ConsoleView;

import java.io.IOException;
import java.util.List;

public class ProductionController {

    private final OrderService service;
    private final ConsoleView view;

    public ProductionController(OrderService service, ConsoleView view) {
        this.service = service;
        this.view    = view;
    }

    public void run() {
        while (true) {
            view.printProductionMenu();
            int choice = view.promptInt();
            switch (choice) {
                case 1 -> listProducing();
                case 2 -> completeProduction();
                case 0 -> { return; }
                default -> view.printMessage("잘못된 입력입니다.");
            }
        }
    }

    private void listProducing() {
        try {
            List<Order> producing = service.getByStatus(OrderStatus.PRODUCING);
            if (producing.isEmpty()) {
                view.printMessage("생산 중인 주문이 없습니다.");
                return;
            }
            view.printOrderList(producing);
        } catch (IOException e) {
            view.printMessage("[오류] 목록 조회 실패: " + e.getMessage());
        }
    }

    private void completeProduction() {
        try {
            List<Order> producing = service.getByStatus(OrderStatus.PRODUCING);
            if (producing.isEmpty()) {
                view.printMessage("생산 완료 처리할 주문이 없습니다.");
                return;
            }
            view.printOrderList(producing);
            String orderId = view.promptOrderId();
            Order order = producing.stream()
                    .filter(o -> o.getOrderId().equals(orderId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("PRODUCING 목록에 없는 주문 ID입니다: " + orderId));
            service.completeProduction(orderId);
            view.printProductionCompleteResult(orderId, order.getQuantity());
        } catch (IllegalStateException | IllegalArgumentException e) {
            view.printMessage("[오류] " + e.getMessage());
        } catch (IOException e) {
            view.printMessage("[오류] 생산 완료 처리 실패: " + e.getMessage());
        }
    }
}
