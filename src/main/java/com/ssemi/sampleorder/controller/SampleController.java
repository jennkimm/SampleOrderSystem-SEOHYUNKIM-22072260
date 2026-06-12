package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.service.SampleService;
import com.ssemi.sampleorder.view.ConsoleView;

import java.io.IOException;
import java.util.List;

public class SampleController {

    private final SampleService service;
    private final ConsoleView view;

    public SampleController(SampleService service, ConsoleView view) {
        this.service = service;
        this.view = view;
    }

    public void run() {
        while (true) {
            view.printSampleMenu();
            int choice = view.promptInt();
            switch (choice) {
                case 1 -> register();
                case 2 -> listAll();
                case 3 -> search();
                case 0 -> { return; }
                default -> view.printMessage("잘못된 입력입니다.");
            }
        }
    }

    private void register() {
        try {
            Sample sample = view.promptSampleInput();
            service.register(sample);
            view.printMessage("시료 '" + sample.getName() + "' 이(가) 등록되었습니다.");
        } catch (NumberFormatException e) {
            view.printMessage("입력값이 올바르지 않습니다. 숫자를 입력해 주세요.");
        } catch (IOException e) {
            view.printMessage("[오류] 시료 저장 실패: " + e.getMessage());
        }
    }

    private void listAll() {
        try {
            List<Sample> samples = service.getAll();
            view.printSampleList(samples);
        } catch (IOException e) {
            view.printMessage("[오류] 시료 조회 실패: " + e.getMessage());
        }
    }

    private void search() {
        try {
            String keyword = view.promptSearchKeyword();
            List<Sample> result = service.searchByName(keyword);
            if (result.isEmpty()) {
                view.printMessage("'" + keyword + "' 에 해당하는 시료가 없습니다.");
            } else {
                view.printSampleList(result);
            }
        } catch (IOException e) {
            view.printMessage("[오류] 시료 검색 실패: " + e.getMessage());
        }
    }
}
