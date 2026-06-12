package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.service.SampleService;
import com.ssemi.sampleorder.view.ConsoleView;

import java.io.IOException;

public class MainController {

    private final SampleService sampleService;
    private final ConsoleView view;
    private final SampleController sampleController;

    public MainController(SampleService sampleService, ConsoleView view) {
        this.sampleService = sampleService;
        this.view = view;
        this.sampleController = new SampleController(sampleService, view);
    }

    public void run() {
        while (true) {
            try {
                view.printMainMenu(sampleService.getAll());
                int choice = view.promptInt();
                switch (choice) {
                    case 1 -> sampleController.run();
                    case 2, 3, 4, 5 -> view.printMessage("아직 준비 중인 기능입니다.");
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
