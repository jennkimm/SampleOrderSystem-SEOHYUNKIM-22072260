package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.ProductionLine;
import com.ssemi.sampleorder.service.ProductionService;
import com.ssemi.sampleorder.view.ConsoleView;

import java.io.IOException;
import java.util.List;

public class ProductionController {

    private final ProductionService service;
    private final ConsoleView view;

    public ProductionController(ProductionService service, ConsoleView view) {
        this.service = service;
        this.view    = view;
    }

    public void run() {
        while (true) {
            view.printProductionMenu();
            int choice = view.promptInt();
            switch (choice) {
                case 1 -> listQueue();
                case 2 -> completeNext();
                case 0 -> { return; }
                default -> view.printMessage("잘못된 입력입니다.");
            }
        }
    }

    private void listQueue() {
        try {
            List<ProductionLine> queue = service.getQueue();
            view.printProductionQueue(queue);
        } catch (IOException e) {
            view.printMessage("[오류] 생산 큐 조회 실패: " + e.getMessage());
        }
    }

    private void completeNext() {
        try {
            List<ProductionLine> queue = service.getQueue();
            if (queue.isEmpty()) {
                view.printMessage("생산 완료 처리할 주문이 없습니다.");
                return;
            }
            ProductionLine head = queue.get(0);
            service.completeNext();
            view.printProductionCompleteResult(head.getOrderId(), head.getScheduledQty());
        } catch (IllegalStateException e) {
            view.printMessage("[오류] " + e.getMessage());
        } catch (IOException e) {
            view.printMessage("[오류] 생산 완료 처리 실패: " + e.getMessage());
        }
    }
}
