package app.bennsandoval.com.woodmin.models.v1.orders;

import com.google.gson.annotations.SerializedName;

/**
 * Created by bennsandoval on 2/28/17.
 */

public class CouponLine {

    private Integer id;
    private String code;
    private String type;
    private String discount;
    @SerializedName("discount_tax")
    private String discountTax;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDiscountTax() {
        return discountTax;
    }

    public void setDiscountTax(String discountTax) {
        this.discountTax = discountTax;
    }

}
