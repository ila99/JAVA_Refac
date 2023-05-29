import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Statement {
    private Map<String, Play> plays;
    Statement(Map<String, Play> plays) {
        this.plays = plays;
    }
    /**
     * 중간 데이터 저장 객체 생성
     */
    public Map<DataType, Object> createStatementData(Invoice invoice) {
        Map<DataType, Object> statementData = new HashMap<DataType, Object>();  // 중간 구조 데이터 객체
        statementData.put(DataType.CUSTOMER, invoice.getCustomer());    // 고객 데이터를 중간 데이터로 옮김
        statementData.put(DataType.PERFORMANCES, (invoice.getPerformances()).stream().map(p -> enrichPerformance(p)).collect(Collectors.toList()));  // 공연 정보를 중간 데이터로 옮김
        statementData.put(DataType.TOTAL_AMOUNT, totalAmount(statementData));
        statementData.put(DataType.TOTAL_VOLUME_CREDITS, totalVolumeCredits(statementData));
        return statementData;
    }

    /**
     * Performance deep copy
     * @param aPerformance
     * @return
     */
    private Performance enrichPerformance(Performance aPerformance) {
        Performance result = new Performance();
        result.setPlayID(aPerformance.getPlayID());
        result.setAudience(aPerformance.getAudience());
        result.setPlay(playFor(result));
        result.setAmount(amountFor(result));
        result.setVolumeCredits(volumeCreditFor(result));

        return result;
    }

    private Play playFor(Performance aPerformance) {
        return plays.get(aPerformance.getPlayID());
    }

    private long amountFor(Performance aPerformance) {    // 매개변수 삭제
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

    private long volumeCreditFor(Performance aPerformance) {
        long result = 0;

        // 포인트를 적립한다.
        result += Math.max(aPerformance.getAudience() - 30, 0);
        // 희극 관객 5명마다 추가 포인트를 제공한다.
        if (aPerformance.getPlay().getType().equals("comedy")) result += Math.floor(aPerformance.getAudience() / 5);   // 인라인
        return result;
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
}
