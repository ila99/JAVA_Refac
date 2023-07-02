public class Product {
    int basePrice;
    int quantity;
    int discountThreshold;
    int discountRate;

    public int getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(int discountRate) {
        this.discountRate = discountRate;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(int basePrice) {
        this.basePrice = basePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getDiscountThreshold() {
        return discountThreshold;
    }

    public void setDiscountThreshold(int discountThreshold) {
        this.discountThreshold = discountThreshold;
    }
}
