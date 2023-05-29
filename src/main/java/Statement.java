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
        PerformanceCalculator performanceCalculator = createPerformanceCalculator(aPerformance, playFor(aPerformance));
        // 공연 정보 계산기 추가
        Performance result = new Performance();
        result.setPlayID(aPerformance.getPlayID());
        result.setAudience(aPerformance.getAudience());
        result.setPlay(performanceCalculator.getPlay());
        result.setAmount(performanceCalculator.getAmount());
        result.setVolumeCredits(performanceCalculator.getVolumeCredit());

        return result;
    }

    private Play playFor(Performance aPerformance) {
        return plays.get(aPerformance.getPlayID());
    }

    private long volumeCreditFor(Performance aPerformance) {
        return createPerformanceCalculator(aPerformance, playFor(aPerformance)).getVolumeCredit();
    }

    private long amountFor(Performance aPerformance) {
        return createPerformanceCalculator(aPerformance, playFor(aPerformance)).getAmount();
    }

    private long totalAmount(Map<DataType, Object> data) {
        return ((List<Performance>)data.get(DataType.PERFORMANCES))
                .stream()
                .mapToLong(ob -> (ob.getAmount() + ob.getAmount()))
                .reduce(0, Long::sum);
    }

    // 값 누적 로직 분리 --> 만약 반복 횟수가 성능에 영향을 끼칠 정도이면 분리할 지 말 지는 상황에 맞게 고려해야 함
    private long totalVolumeCredits(Map<DataType, Object> data) {
        long volumeCredits = 0;            // 문장 슬라이드 - 변수 초기화 문장을 for 문 앞으로
        // 반복문 쪼개기
        for (Performance perf : (List<Performance>)data.get(DataType.PERFORMANCES)) {
            volumeCredits += perf.getVolumeCredits();     // 함수 추출
        }
        return volumeCredits;
    }

    private PerformanceCalculator createPerformanceCalculator(Performance aPerformance, Play aPlay) {
        if (aPlay.getType().equals(PlayType.TRAGEDY.getValue())) {
            return new TragedyCalculator(aPerformance, playFor(aPerformance));
        }
        else if (aPlay.getType().equals(PlayType.COMEDY.getValue())) {
            return new ComedyCalculator(aPerformance, playFor(aPerformance));
        }
        else {
            throw new Error(String.format("알 수 없는 장르: %s", aPerformance.getPlay().getType()));
        }
    }

}
