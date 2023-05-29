import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static Statement statement;
    public static void main(String[]args) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Invoice> invoices;

        Map<String, Play> plays;  // 불변한 정적 맵으로 변경

        try {
            invoices    = initializeInvoiceList(objectMapper);
            plays       = initializePlayMap(objectMapper);
            statement   = new Statement(plays);
        } catch (IOException e) {
            System.out.println("[ERROR] IOException: " + e.getMessage());
            return;
        }

        for (Invoice invoice : invoices) {
            System.out.println(htmlStatement(invoice));
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
    public static String htmlStatement(Invoice invoice) {   // 매개변수 삭제
        return renderHtml(statement.createStatementData(invoice));
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

    /**
     * 2023-05-29: 단위 쪼개기, 함수 추출하기
     * @param data
     * @return
     */
    private static String renderHtml(Map<DataType, Object> data) {
        StringBuffer result = new StringBuffer("<h1>청구 내역 (고객명: \"");
        result.append((String)data.get(DataType.CUSTOMER)).append("\"</h1>\n");  // 고객 데이터를 중간 데이터로부터 얻음
        result.append("<table>\n");
        result.append("<tr><th>연극</th><th>좌석 수</th><th>금액</th></tr>\n");
        for (Performance perf : (List<Performance>)data.get(DataType.PERFORMANCES)) {
            // 청구 내역을 출력한다.
            result.append("   <tr><td>").append(perf.getPlay().getName()).append("</td>");
            result.append("<td>(").append(perf.getAudience()).append("석)</td>");
            result.append("<td>").append(usd(perf.getAmount())).append("</td></tr>\n");  // 인라인
        }
        result.append("</table>\n");
        result.append("<p>총액: <em>").append(usd((long)data.get(DataType.TOTAL_AMOUNT))).append("</em></p>\n");
        result.append("<p>적립 포인트: <em>").append((long)data.get(DataType.TOTAL_VOLUME_CREDITS)).append("</em>점</p>\n");        // 인라인

        return result.toString();
    }

    // usb로 변환
    private static String usd(long aNumber) {
        NumberFormat numFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
        numFormat.setMinimumFractionDigits(2);
        return numFormat.format(aNumber/100);
    }
}
