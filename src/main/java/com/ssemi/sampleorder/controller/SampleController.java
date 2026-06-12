package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.service.SampleService;
import com.ssemi.sampleorder.view.ConsoleView;

import java.io.IOException;
import java.util.List;

public class SampleController {

    private static final int PAGE_SIZE = 5;

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
            if (samples.isEmpty()) {
                view.printMessage("등록된 시료가 없습니다.");
                return;
            }
            int totalPages = (int) Math.ceil((double) samples.size() / PAGE_SIZE);
            int currentPage = 1;
            while (true) {
                int from = (currentPage - 1) * PAGE_SIZE;
                int to = Math.min(from + PAGE_SIZE, samples.size());
                view.printSamplePage(samples.subList(from, to), currentPage, totalPages, samples.size());
                String nav = view.promptPageNavigation();
                switch (nav) {
                    case "<" -> { if (currentPage > 1) currentPage--; }
                    case ">" -> { if (currentPage < totalPages) currentPage++; }
                    case "0", "" -> { return; }
                    default -> {} // 그 외 입력 무시
                }
            }
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
