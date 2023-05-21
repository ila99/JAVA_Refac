import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

public class Main {
    private static Map<String, Play> plays;
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

    public static String statement(Invoice invoice) {   // 매개변수 삭제
        long totalAmount = 0;
        long volumeCredits = 0;

        StringBuffer result = new StringBuffer("청구 내역 (고객명: \"");
        result.append(invoice.getCustomer()).append("\"\n");

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        numberFormat.setMinimumFractionDigits(2);

        for (Performance perf : invoice.getPerformances()) {
            long thisAmount = amountFor(perf);        // 함수 추출, 인라인
            // 포인트를 적립한다.
            volumeCredits += Math.max(perf.getAudience() - 30, 0);
            // 희극 관객 5명마다 추가 포인트를 제공한다.
            if (playFor(perf).getType().equals("comedy")) volumeCredits += Math.floor(perf.getAudience() / 5);   // 인라인

            // 청구 내역을 출력한다.
            result.append("    ").append(playFor(perf).getName()).append(": ").append(numberFormat.format(thisAmount/100));  // 인라인
            result.append(" (").append(perf.getAudience()).append("석)\n");
            totalAmount += thisAmount;
        }

        result.append("총액: ").append(numberFormat.format(totalAmount/100)).append("\n");
        result.append("적립 포인트: ").append(volumeCredits).append("점\n");

        return result.toString();
    }

    
    private static Play playFor(Performance perf) {
        return plays.get(perf.getPlayID());
    }

    private static long amountFor(Performance aPerformance) {    // 매개변수 삭제
        long result = 0;
        switch (playFor(aPerformance).getType()) {      // 인라인
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
                throw new Error(String.format("알 수 없는 장르: %s", playFor(aPerformance).getType()));
        }
        return result;
    }

}
