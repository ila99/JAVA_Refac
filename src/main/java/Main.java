import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        printOutsideRange();
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

    private static void printOwing(Invoice invoice) {

        // 배너 출력 로직을 함수로 추출
        printBanner();

        long outstanding = calculateOutstanding(invoice);

        // 마감일(dueDate)을 기록하는 로직을 함수로 추출
        recordDueDate(invoice);

        // 세부 사항 출력 로직을 함수로 추출
        printDetails(invoice, outstanding);
    }

    /**
     * 미해결 채무 계산
     * @param invoice
     * @return
     */
    private static long calculateOutstanding(Invoice invoice) {
        long result = 0;       // 코드 슬라이딩-> 변수 선언을 해당 변수를 사용하는 로직 근처로 위치함
                                // 변수 이름 변경 (outstanding --> result)
        // 미해결 채무(outstanding)를 계산
        for (Order order : invoice.getOrders()) {
            result += order.getAmount();
        }
        return result;
    }

    /**
     * 마감일 설정
     * @param invoice
     */
    private static void recordDueDate(Invoice invoice) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = LocalDate.of(today.getYear(),today.getMonth(),today.getDayOfMonth()+30);
        invoice.setDueDate(targetDate);
    }

    /**
     * 세부 사항 출력
     * @param invoice
     * @param outstanding
     */
    private static void printDetails(Invoice invoice, long outstanding) {
        System.out.println(String.format("고객명: %s", invoice.getCustomer()));
        System.out.println(String.format("채무액: %d", outstanding));
        System.out.println(String.format("마감일: %s",
                invoice.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
    }

    /**
     * 배너를 출력함
     */
    private static void printBanner() {
        System.out.println("***********************");
        System.out.println("******* 고객 채무 ******");
        System.out.println("***********************");
    }

    private int rating(Driver aDriver) {
        // 함수 인라인 -> 판별 로직을 함수로 분리하지 않고 처리함
        return aDriver.getNumberOfLateDeliveries() > 5 ? 2 : 1;
    }

    /**
     *  6.8 매개변수 객체 만들기
     * @link http://iloveulhj.github.io/posts/java/java-stream-api.html
     * @link https://codechacha.com/ko/java8-method-reference/
     */
    private static void printOutsideRange() {
        List<Map<String, String>> result = readingsOutsideRange(testData(), new NumberRange(40, 60));

        result.forEach(System.out::println);
    }
    private static List<Map<String, String>> testData() {
        List<Map<String, String>> listMap = new ArrayList<Map<String, String>>();
        listMap.add(new HashMap<String, String>() {{
            put("temp", "47");
            put("time", "2016-11-10 09:10");
        }});
        listMap.add(new HashMap<String, String>() {{
            put("temp", "53");
            put("time", "2016-11-10 09:20");
        }});
        listMap.add(new HashMap<String, String>() {{
            put("temp", "58");
            put("time", "2016-11-10 09:30");
        }});
        listMap.add(new HashMap<String, String>() {{
            put("temp", "53");
            put("time", "2016-11-10 09:40");
        }});
        listMap.add(new HashMap<String, String>() {{
            put("temp", "61");
            put("time", "2016-11-10 09:50");
        }});
        return listMap;
    }

    /**
     * 정상 범위를 벗어난 측정값을 가진 데이터만 찾아서 리턴
     * @param station
     * @param range
     * @return
     */
    private static List<Map<String, String>> readingsOutsideRange(List<Map<String, String>> station, NumberRange range) {
        return station.stream().filter(r -> {
                    int value = Integer.valueOf(r.getOrDefault("temp", "0"));

                    return value < range.getMin() || value > range.getMax();
                } ).collect(Collectors.toList());
    }
}
