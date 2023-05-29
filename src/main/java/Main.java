import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static Map<String, Play> plays;

    private static enum DataType {CUSTOMER, PERFORMANCES, TOTAL_AMOUNT, TOTAL_VOLUME_CREDITS};
    public static void main(String[]args) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Invoice> invoices;
        //Map<String, Play> plays;  // 불변한 정적 맵으로 변경

        try {
            invoices    = initializeInvoiceList(objectMapper);
            plays       = initializePlayMap(objectMapper);
        } catch (IOException e) {
            System.out.println("[ERROR] IOException: " + e.getMessage());
            return;
        }

        for (Invoice invoice : invoices) {
            System.out.println(statement(invoice));
        }
    }

    private static Map<String, Play> initializePlayMap(ObjectMapper objectMapper) throws IOException {
        return objectMapper.readValue(new File("src/main/resources/json/play.json"), new TypeReference<Map<String, Play>>(){});
    }

    private static List<Invoice> initializeInvoiceList(ObjectMapper objectMapper) throws IOException {
        return objectMapper.readValue(new File("src/main/resources/json/invoices.json"), new TypeReference<List<Invoice>>(){});
    }

    /**
     * 2023-05-29: 단위 쪼개기, 함수 추출하기
     * @param invoice, plays
     * @return
     */
    public static String statement(Invoice invoice) {   // 매개변수 삭제
        return renderPlainText(createStatementData(invoice));
    }

    /**
     * 중간 데이터 저장 객체 생성
     */
    private static Map<DataType, Object> createStatementData(Invoice invoice) {
        Map<DataType, Object> statementData = new HashMap<DataType, Object>();  // 중간 구조 데이터 객체
        statementData.put(DataType.CUSTOMER, invoice.getCustomer());    // 고객 데이터를 중간 데이터로 옮김
        statementData.put(DataType.PERFORMANCES, (invoice.getPerformances()).stream().map(Main::enrichPerformance).collect(Collectors.toList()));  // 공연 정보를 중간 데이터로 옮김
        statementData.put(DataType.TOTAL_AMOUNT, totalAmount(statementData));
        statementData.put(DataType.TOTAL_VOLUME_CREDITS, totalVolumeCredits(statementData));
        return statementData;
    }

    /**
     * Performance deep copy
     * @param aPerformance
     * @return
     */
    private static Performance enrichPerformance(Performance aPerformance) {
        Performance result = new Performance();
        result.setPlayID(aPerformance.getPlayID());
        result.setAudience(aPerformance.getAudience());
        result.setPlay(playFor(result));
        result.setAmount(amountFor(result));
        result.setVolumeCredits(volumeCreditFor(result));

        return result;
    }

    /**
     * 2023-05-29: 단위 쪼개기, 함수 추출하기
     * @param data
     * @return
     */
    private static String renderPlainText(Map<DataType, Object> data) {
        StringBuffer result = new StringBuffer("청구 내역 (고객명: \"");
        result.append((String)data.get(DataType.CUSTOMER)).append("\"\n");  // 고객 데이터를 중간 데이터로부터 얻음

        for (Performance perf : (List<Performance>)data.get(DataType.PERFORMANCES)) {
            // 청구 내역을 출력한다.
            result.append("    ").append(perf.getPlay().getName()).append(": ").append(usd(perf.getAmount()));  // 인라인
            result.append(" (").append(perf.getAudience()).append("석)\n");
        }
        result.append("총액: ").append(usd((long)data.get(DataType.TOTAL_AMOUNT))).append("\n");
        result.append("적립 포인트: ").append((long)data.get(DataType.TOTAL_VOLUME_CREDITS)).append("점\n");        // 인라인

        return result.toString();
    }

    private static long totalAmount(Map<DataType, Object> data) {
        return ((List<Performance>)data.get(DataType.PERFORMANCES))
                .stream()
                .mapToLong(ob -> (ob.getAmount() + ob.getAmount()))
                .reduce(0, Long::sum);
    }

    // 값 누적 로직 분리 --> 만약 반복 횟수가 성능에 영향을 끼칠 정도이면 분리할 지 말 지는 상황에 맞게 고려해야 함
    private static long totalVolumeCredits(Map<DataType, Object> data) {
        long volumeCredits = 0;            // 문장 슬라이드 - 변수 초기화 문장을 for 문 앞으로
        // 반복문 쪼개기
        for (Performance perf : (List<Performance>)data.get(DataType.PERFORMANCES)) {
            volumeCredits += perf.getVolumeCredits();     // 함수 추출
        }
        return volumeCredits;
    }

    // usb로 변환
    private static String usd(long aNumber) {
        NumberFormat numFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        numFormat.setMinimumFractionDigits(2);
        return numFormat.format(aNumber/100);
    }

    private static long volumeCreditFor(Performance aPerformance) {
        long result = 0;

        // 포인트를 적립한다.
        result += Math.max(aPerformance.getAudience() - 30, 0);
        // 희극 관객 5명마다 추가 포인트를 제공한다.
        if (aPerformance.getPlay().getType().equals("comedy")) result += Math.floor(aPerformance.getAudience() / 5);   // 인라인
        return result;
    }
    private static Play playFor(Performance aPerformance) {
        return plays.get(aPerformance.getPlayID());
    }

    private static long amountFor(Performance aPerformance) {    // 매개변수 삭제
        long result = 0;
        switch (aPerformance.getPlay().getType()) {      // 인라인
            case "tragedy": // 비극
                result = 40000;
                if(aPerformance.getAudience() > 30) {
                    result += 1000 * (aPerformance.getAudience() - 30);
                }
                break;
            case "comedy": // 희극
                result = 30000;
                if(aPerformance.getAudience() > 20) {
                    result += 10000 + 500 * (aPerformance.getAudience() - 20);
                }
                result += 300 * aPerformance.getAudience();
                break;
            default:
                throw new Error(String.format("알 수 없는 장르: %s", aPerformance.getPlay().getType()));
        }
        return result;
    }

}
