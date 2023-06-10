public class Order {
    long amount = 0;
    Record data;

    Order (Record aRecord) {
        data = aRecord;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getQuantity() {
        return data.getQuantity();
    }

    public long getItemPrice() {
        return data.getItemPrice();
    }

    /**
     * 클래스 전체에 영향을 주는 변수는 메서드로 추출함
     */
    public double getPrice() {
        return getQuantity() * getItemPrice()
                - Math.max(0, getQuantity() - 500) * getItemPrice() * 0.05 *
                Math.min(getQuantity() * getItemPrice() *0.1, 100);
    }

    public double getBasePrice() {
        return getQuantity() * getItemPrice();
    }

    public double getQuantityDiscount() {
        return Math.max(0, getQuantity() - 500) * getItemPrice() * 0.05;
    }

    public double getShipping() {
        return Math.min(getBasePrice() * 0.1, 100);
    }
}
