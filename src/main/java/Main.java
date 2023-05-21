import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

public class Main {
    public static void main(String[]args) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Invoice> invoices;
        Map<String, Play> plays;

        try {
            invoices = objectMapper.readValue(new File("src/main/resources/json/invoices.json"), new TypeReference<List<Invoice>>(){});
            plays = objectMapper.readValue(new File("src/main/resources/json/play.json"), new TypeReference<Map<String, Play>>(){});
        } catch (IOException e) {
            System.out.println("[ERROR] IOException: " + e.getMessage());
            return;
        }
        if (invoices == null || plays == null) {
            System.out.println("[ERROR] Fail to read json..");
            return;
        }
        for (Invoice invoice : invoices) {
            System.out.println(statement(invoice, plays));
        }
    }

    public static String statement(Invoice invoice, Map<String, Play> plays) {
        long totalAmount = 0;
        long volumeCredits = 0;

        StringBuffer result = new StringBuffer("청구 내역 (고객명: \"");
        result.append(invoice.getCustomer()).append("\"\n");

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        numberFormat.setMinimumFractionDigits(2);

        for (Performance perf : invoice.getPerformances()) {
            Play play = plays.get(perf.getPlayID());
            int thisAmount = 0;

            switch (play.getType()) {
                case "tragedy": // 비극
                    thisAmount = 40000;
                    if(perf.getAudience() > 30) {
                        thisAmount += 1000 * (perf.getAudience() - 30);
                    }
                    break;
                case "comedy": // 희극
                    thisAmount = 30000;
                    if(perf.getAudience() > 20) {
                        thisAmount += 10000 + 500 * (perf.getAudience() - 20);
                    }
                    thisAmount += 300 * perf.getAudience();
                    break;
                default:
                    throw new Error(String.format("알 수 없는 장르: %s", play.getType()));
            }

            // 포인트를 적립한다.
            volumeCredits += Math.max(perf.getAudience() - 30, 0);
            // 희극 관객 5명마다 추가 포인트를 제공한다.
            if (play.getType().equals("comedy")) volumeCredits += Math.floor(perf.getAudience() / 5);

            // 청구 내역을 출력한다.
            result.append("    ").append(play.getName()).append(": ").append(numberFormat.format(thisAmount/100));
            result.append(" (").append(perf.getAudience()).append("석)\n");
            totalAmount += thisAmount;
        }

        result.append("총액: ").append(numberFormat.format(totalAmount/100)).append("\n");
        result.append("적립 포인트: ").append(volumeCredits).append("점\n");

        return result.toString();
    }
}
