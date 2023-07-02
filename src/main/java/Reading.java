import java.util.Map;

// 여러 함수를 클래스로 묶기
public class Reading {

    Reading() {}
    Reading(String _customer, int _quantity, int _month, int _year) {
        customer = _customer;
        quantity = _quantity;
        month = _month;
        year = _year;
    }

    String customer;

    int quantity;
    int month;
    int year;

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    // 공통 레코드를 사용하는 함수 각각을 각 클래스로 함수 옮기기
    public int getBaseCharge() {
        return baseRate(month, year) * quantity;
    }

    public int baseRate(int month, int year) {
        return 1;
    }

    public int getTaxableCharge() {
        return Math.max(0, getBaseCharge() - taxThreshold(year));
    }

    public int taxThreshold(int year) {
        return 2;
    }

    // 여러 함수를 변환 함수로 묶기
    /**
     * 입력 객체를 그대로 복사하여 변환하는 변환 함수
     */
    public Reading enrichReading(Reading original) {
        Reading result = new Reading();
        result.setCustomer(original.getCustomer());
        result.setMonth(original.getMonth());
        result.setYear(original.getYear());
        result.setQuantity(original.getQuantity());
        return result;
    }
}
