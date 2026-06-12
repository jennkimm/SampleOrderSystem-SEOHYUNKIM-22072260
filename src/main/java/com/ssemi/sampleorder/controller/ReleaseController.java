package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.service.OrderService;
import com.ssemi.sampleorder.view.ConsoleView;

import java.io.IOException;

public class ReleaseController {

    private final OrderService service;
    private final ConsoleView view;

    public ReleaseController(OrderService service, ConsoleView view) {
        this.service = service;
        this.view    = view;
    }

    public void run() {
        while (true) {
            view.printReleaseMenu();
            int choice = view.promptInt();
            switch (choice) {
                case 1 -> listConfirmed();
                case 2 -> release();
                case 0 -> { return; }
                default -> view.printMessage("잘못된 입력입니다.");
            }
        }
    }

    private void listConfirmed() {
        try {
            view.printOrderList(service.getByStatus(OrderStatus.CONFIRMED));
        } catch (IOException e) {
            view.printMessage("[오류] 목록 조회 실패: " + e.getMessage());
        }
    }

    private void release() {
        try {
            String orderId = view.promptOrderId();
            service.release(orderId);
            view.printMessage("주문 '" + orderId + "' 출고 처리 완료. (상태: RELEASE)");
        } catch (IllegalStateException | IllegalArgumentException e) {
            view.printMessage("[오류] " + e.getMessage());
        } catch (IOException e) {
            view.printMessage("[오류] 출고 처리 실패: " + e.getMessage());
        }
    }
}
