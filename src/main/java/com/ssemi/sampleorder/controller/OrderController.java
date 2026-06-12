package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.service.OrderService;
import com.ssemi.sampleorder.service.ProductionService;
import com.ssemi.sampleorder.view.ConsoleView;

import java.io.IOException;
import java.util.List;

public class OrderController {

    private final OrderService service;
    private final ProductionService productionService;
    private final ConsoleView view;

    public OrderController(OrderService service, ProductionService productionService, ConsoleView view) {
        this.service           = service;
        this.productionService = productionService;
        this.view              = view;
    }

    public void run() {
        while (true) {
            view.printOrderMenu();
            int choice = view.promptInt();
            switch (choice) {
                case 1 -> place();
                case 2 -> listReserved();
                case 3 -> approve();
                case 4 -> reject();
                case 0 -> { return; }
                default -> view.printMessage("잘못된 입력입니다.");
            }
        }
    }

    private void place() {
        try {
            String[] input = view.promptOrderInput();
            int quantity = Integer.parseInt(input[2]);
            service.place(input[0], input[1], quantity);
            view.printMessage("주문이 접수되었습니다. (상태: RESERVED)");
        } catch (NumberFormatException e) {
            view.printMessage("수량은 숫자로 입력해 주세요.");
        } catch (IllegalArgumentException e) {
            view.printMessage("[입력 오류] " + e.getMessage());
        } catch (IOException e) {
            view.printMessage("[오류] 주문 접수 실패: " + e.getMessage());
        }
    }

    private void listReserved() {
        try {
            view.printOrderList(service.getReserved());
        } catch (IOException e) {
            view.printMessage("[오류] 목록 조회 실패: " + e.getMessage());
        }
    }

    private void approve() {
        try {
            List<Order> reserved = service.getReserved();
            if (reserved.isEmpty()) {
                view.printMessage("승인 대기 중인 주문이 없습니다.");
                return;
            }
            view.printOrderList(reserved);
            String orderId = view.promptOrderId();
            int shortfall = service.getShortfall(orderId);
            if (shortfall > 0) {
                view.printStockShortfallWarning(shortfall);
                if (!view.promptYesNo()) {
                    service.reject(orderId);
                    view.printMessage("주문 '" + orderId + "' 이(가) 거절 처리되었습니다.");
                    return;
                }
            }
            OrderStatus newStatus = service.approve(orderId);
            view.printApprovalResult(orderId, newStatus);
            if (newStatus == OrderStatus.PRODUCING) {
                productionService.enqueue(orderId);
                view.printMessage("생산 라인에 등록되었습니다.");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            view.printMessage("[오류] " + e.getMessage());
        } catch (IOException e) {
            view.printMessage("[오류] 승인 처리 실패: " + e.getMessage());
        }
    }

    private void reject() {
        try {
            String orderId = view.promptOrderId();
            service.reject(orderId);
            view.printMessage("주문 '" + orderId + "' 이(가) 거절되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            view.printMessage("[오류] " + e.getMessage());
        } catch (IOException e) {
            view.printMessage("[오류] 거절 처리 실패: " + e.getMessage());
        }
    }
}
