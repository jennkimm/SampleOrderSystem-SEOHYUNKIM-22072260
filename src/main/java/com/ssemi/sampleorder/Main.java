package com.ssemi.sampleorder;

import com.ssemi.sampleorder.controller.MainController;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.service.OrderService;
import com.ssemi.sampleorder.service.SampleService;
import com.ssemi.sampleorder.view.ConsoleView;

public class Main {

    private static final String SAMPLE_DATA_FILE = "data/samples.json";
    private static final String ORDER_DATA_FILE  = "data/orders.json";

    public static void main(String[] args) {
        SampleRepository sampleRepo = new SampleRepository(SAMPLE_DATA_FILE);
        OrderRepository  orderRepo  = new OrderRepository(ORDER_DATA_FILE);
        SampleService sampleService = new SampleService(sampleRepo);
        OrderService  orderService  = new OrderService(sampleRepo, orderRepo);
        ConsoleView view = new ConsoleView();
        MainController mainController = new MainController(sampleService, orderService, view);
        mainController.run();
    }
}
