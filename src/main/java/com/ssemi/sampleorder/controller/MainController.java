package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.service.MonitoringService;
import com.ssemi.sampleorder.service.OrderService;
import com.ssemi.sampleorder.service.SampleService;
import com.ssemi.sampleorder.view.ConsoleView;

import java.io.IOException;

public class MainController {

    private final SampleService sampleService;
    private final ConsoleView view;
    private final SampleController sampleController;
    private final OrderController orderController;
    private final MonitoringController monitoringController;
    private final ReleaseController releaseController;
    private final ProductionController productionController;

    public MainController(SampleService sampleService, OrderService orderService,
                          MonitoringService monitoringService, ConsoleView view) {
        this.sampleService        = sampleService;
        this.view                 = view;
        this.sampleController     = new SampleController(sampleService, view);
        this.orderController      = new OrderController(orderService, view);
        this.monitoringController = new MonitoringController(monitoringService, view);
        this.releaseController    = new ReleaseController(orderService, view);
        this.productionController = new ProductionController(orderService, view);
    }

    public void run() {
        while (true) {
            try {
                view.printMainMenu(sampleService.getAll());
                int choice = view.promptInt();
                switch (choice) {
                    case 1 -> sampleController.run();
                    case 2 -> orderController.run();
                    case 3 -> monitoringController.run();
                    case 4 -> releaseController.run();
                    case 5 -> productionController.run();
                    case 0 -> {
                        view.printMessage("시스템을 종료합니다.");
                        return;
                    }
                    default -> view.printMessage("잘못된 입력입니다.");
                }
            } catch (IOException e) {
                view.printMessage("[오류] " + e.getMessage());
            }
        }
    }
}
