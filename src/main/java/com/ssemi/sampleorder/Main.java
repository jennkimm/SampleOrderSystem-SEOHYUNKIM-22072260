package com.ssemi.sampleorder;

import com.ssemi.sampleorder.controller.MainController;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.service.SampleService;
import com.ssemi.sampleorder.view.ConsoleView;

public class Main {

    private static final String DATA_FILE = "data/samples.json";

    public static void main(String[] args) {
        SampleRepository sampleRepo = new SampleRepository(DATA_FILE);
        SampleService sampleService = new SampleService(sampleRepo);
        ConsoleView view = new ConsoleView();
        MainController mainController = new MainController(sampleService, view);
        mainController.run();
    }
}
