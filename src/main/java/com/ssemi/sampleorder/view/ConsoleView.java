package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.model.Sample;

import java.util.List;
import java.util.Scanner;

public class ConsoleView {

    private final Scanner scanner = new Scanner(System.in);

    public void printMainMenu(List<Sample> summary) {
        System.out.println("\n========================================");
        System.out.println("    S-Semi 시료 생산주문관리 시스템");
        System.out.println("========================================");
        System.out.printf("  등록된 시료 수: %d개%n", summary.size());
        System.out.println("----------------------------------------");
        System.out.println("  1. 시료 관리");
        System.out.println("  2. 주문 (접수 / 승인 / 거절)  [준비 중]");
        System.out.println("  3. 모니터링                   [준비 중]");
        System.out.println("  4. 출고 처리                  [준비 중]");
        System.out.println("  5. 생산 라인                  [준비 중]");
        System.out.println("  0. 종료");
        System.out.println("========================================");
        System.out.print("선택: ");
    }

    public void printSampleMenu() {
        System.out.println("\n--- 시료 관리 ---");
        System.out.println("  1. 시료 등록");
        System.out.println("  2. 시료 목록 조회");
        System.out.println("  3. 시료 검색");
        System.out.println("  0. 메인 메뉴로");
        System.out.print("선택: ");
    }

    public void printSampleList(List<Sample> samples) {
        if (samples.isEmpty()) {
            System.out.println("  등록된 시료가 없습니다.");
            return;
        }
        System.out.println();
        System.out.printf("  %-8s %-16s %8s %6s %9s%n", "ID", "이름", "생산시간(분)", "수율", "재고(ea)");
        System.out.println("  " + "-".repeat(57));
        for (Sample s : samples) {
            System.out.printf("  %-8s %-16s %8.1f %6.2f %6d ea%n",
                    s.getSampleId(), s.getName(), s.getAvgProductionTime(),
                    s.getYieldRate(), s.getStock());
        }
    }

    public Sample promptSampleInput() {
        System.out.print("시료 ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("이름: ");
        String name = scanner.nextLine().trim();
        System.out.print("평균 생산시간 (분, 소수점 한 자리 가능): ");
        double time = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("수율 (0.0~1.0): ");
        double yield = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("초기 재고: ");
        int stock = Integer.parseInt(scanner.nextLine().trim());
        return new Sample(id, name, time, yield, stock);
    }

    public String promptSearchKeyword() {
        System.out.print("검색할 이름 키워드: ");
        return scanner.nextLine().trim();
    }

    public int promptInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void printMessage(String message) {
        System.out.println("  " + message);
    }
}
