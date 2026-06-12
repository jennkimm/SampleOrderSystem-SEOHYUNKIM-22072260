package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.model.StockStatus;

import java.util.List;
import java.util.Map;
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
        System.out.println("  2. 주문 (접수 / 승인 / 거절)");
        System.out.println("  3. 모니터링");
        System.out.println("  4. 출고 처리");
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

    public void printSamplePage(List<Sample> page, int currentPage, int totalPages, int totalCount) {
        System.out.printf("%n  === 등록 시료 목록 (총 %d개,  %d / %d 페이지) ===%n",
                totalCount, currentPage, totalPages);
        System.out.printf("  %-8s %-16s %8s %6s %9s%n", "ID", "이름", "생산시간(분)", "수율", "재고(ea)");
        System.out.println("  " + "-".repeat(57));
        for (Sample s : page) {
            System.out.printf("  %-8s %-16s %8.1f %6.2f %6d ea%n",
                    s.getSampleId(), s.getName(), s.getAvgProductionTime(),
                    s.getYieldRate(), s.getStock());
        }
        System.out.println();
        StringBuilder nav = new StringBuilder("  ");
        if (currentPage > 1)          nav.append("[< 이전]  ");
        if (currentPage < totalPages)  nav.append("[> 다음]  ");
        nav.append("[0 돌아가기]");
        System.out.println(nav);
        System.out.print("  입력: ");
    }

    public String promptPageNavigation() {
        return scanner.nextLine().trim();
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

    public void printOrderMenu() {
        System.out.println("\n--- 주문 관리 ---");
        System.out.println("  1. 주문 접수");
        System.out.println("  2. 접수 목록 조회 (RESERVED)");
        System.out.println("  3. 주문 승인");
        System.out.println("  4. 주문 거절");
        System.out.println("  0. 메인 메뉴로");
        System.out.print("선택: ");
    }

    public void printOrderList(List<Order> orders) {
        if (orders.isEmpty()) {
            System.out.println("  표시할 주문이 없습니다.");
            return;
        }
        System.out.println();
        System.out.printf("  %-12s %-8s %-12s %6s %-10s%n", "주문ID", "시료ID", "고객명", "수량", "상태");
        System.out.println("  " + "-".repeat(56));
        for (Order o : orders) {
            System.out.printf("  %-12s %-8s %-12s %6d %-10s%n",
                    o.getOrderId(), o.getSampleId(), o.getCustomerName(),
                    o.getQuantity(), o.getStatus());
        }
    }

    public String[] promptOrderInput() {
        System.out.print("시료 ID: ");
        String sampleId = scanner.nextLine().trim();
        System.out.print("고객명: ");
        String customerName = scanner.nextLine().trim();
        System.out.print("주문 수량: ");
        String quantity = scanner.nextLine().trim();
        return new String[]{sampleId, customerName, quantity};
    }

    public String promptOrderId() {
        System.out.print("주문 ID: ");
        return scanner.nextLine().trim();
    }

    public void printApprovalResult(String orderId, OrderStatus newStatus) {
        System.out.println("\n  ✔ 승인 처리 완료");
        System.out.printf("    주문 번호 : %s%n", orderId);
        System.out.printf("    변경 상태 : %s%n", newStatus);
    }

    public void printStockShortfallWarning(int shortfall) {
        System.out.printf("%n  [주의] 재고 부족 — %d ea 가 부족합니다.%n", shortfall);
        System.out.println("  승인 시 PRODUCING 상태로 전환됩니다.");
        System.out.print("  승인하시겠습니까? (Y/N): ");
    }

    public boolean promptYesNo() {
        return "Y".equalsIgnoreCase(scanner.nextLine().trim());
    }

    public void printMonitoringMenu() {
        System.out.println("\n--- 모니터링 ---");
        System.out.println("  1. 주문 현황 조회 (상태별)");
        System.out.println("  2. 시료 재고 현황");
        System.out.println("  0. 메인 메뉴로");
        System.out.print("선택: ");
    }

    public void printOrderStatusSummary(Map<OrderStatus, List<Order>> summary) {
        System.out.println("\n========== 주문 현황 (REJECTED 제외) ==========");
        for (OrderStatus status : new OrderStatus[]{
                OrderStatus.RESERVED, OrderStatus.CONFIRMED,
                OrderStatus.PRODUCING, OrderStatus.RELEASE}) {
            List<Order> orders = summary.getOrDefault(status, List.of());
            System.out.printf("%n  [%s] — %d건%n", status, orders.size());
            for (Order o : orders) {
                System.out.printf("    %-12s | 시료: %-8s | 고객: %-12s | 수량: %d%n",
                        o.getOrderId(), o.getSampleId(), o.getCustomerName(), o.getQuantity());
            }
        }
        System.out.println("\n================================================");
    }

    public void printStockStatusSummary(List<Sample> samples, Map<String, StockStatus> statusMap) {
        System.out.println("\n========== 시료 재고 현황 ==========");
        System.out.printf("  %-8s %-16s %6s %6s%n", "ID", "이름", "재고(ea)", "상태");
        System.out.println("  " + "-".repeat(42));
        for (Sample s : samples) {
            StockStatus st = statusMap.getOrDefault(s.getSampleId(), StockStatus.SUFFICIENT);
            System.out.printf("  %-8s %-16s %6d   %s%n",
                    s.getSampleId(), s.getName(), s.getStock(), st.getLabel());
        }
        System.out.println("=====================================");
    }

    public void printReleaseMenu() {
        System.out.println("\n--- 출고 처리 ---");
        System.out.println("  1. CONFIRMED 주문 목록 조회");
        System.out.println("  2. 출고 처리");
        System.out.println("  0. 메인 메뉴로");
        System.out.print("선택: ");
    }

    public void printMessage(String message) {
        System.out.println("  " + message);
    }
}
