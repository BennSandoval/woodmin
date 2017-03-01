package app.bennsandoval.com.woodmin.models.v3.orders;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Mackbook on 4/8/16.
 */
public class ShippingLine {

    private Integer id;
    @SerializedName("method_title")
    private String methodTitle;
    @SerializedName("method_id")
    private String methodId;
    private String total;
    @SerializedName("total_tax")
    private String totalTax;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMethodTitle() {
        return methodTitle;
    }

    public void setMethodTitle(String methodTitle) {
        this.methodTitle = methodTitle;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(String totalTax) {
        this.totalTax = totalTax;
    }
}
