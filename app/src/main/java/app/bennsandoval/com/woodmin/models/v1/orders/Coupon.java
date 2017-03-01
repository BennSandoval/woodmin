package app.bennsandoval.com.woodmin.models.v1.orders;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by bennsandoval on 2/28/17.
 */

public class Coupon {

    private Integer id;
    private String code;
    @SerializedName("date_created")
    private Date dateCreated;
    @SerializedName("date_modified")
    private Date dateModified;
    @SerializedName("discount_type")
    private String discountType;
    private String description;
    private String amount;
    @SerializedName("expiry_date")
    private Date expiryDate;
    @SerializedName("usage_count")
    private Integer usageCount;
    @SerializedName("usage_limit")
    private Integer usageLimit;
    @SerializedName("usage_limit_per_user")
    private Integer usageLimitPerUser;
    @SerializedName("limit_usage_to_x_items")
    private Integer limitUsageToXItems;
    @SerializedName("individual_use")
    private Boolean individualUse;
    @SerializedName("free_shipping")
    private Boolean freeShipping;
    @SerializedName("exclude_sale_items")
    private Boolean excludeSaleItems;
    @SerializedName("minimum_amount")
    private String minimumAmount;
    @SerializedName("maximum_amount")
    private String maximumAmount;

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

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Integer getUsageLimitPerUser() {
        return usageLimitPerUser;
    }

    public void setUsageLimitPerUser(Integer usageLimitPerUser) {
        this.usageLimitPerUser = usageLimitPerUser;
    }

    public Integer getLimitUsageToXItems() {
        return limitUsageToXItems;
    }

    public void setLimitUsageToXItems(Integer limitUsageToXItems) {
        this.limitUsageToXItems = limitUsageToXItems;
    }

    public Boolean getIndividualUse() {
        return individualUse;
    }

    public void setIndividualUse(Boolean individualUse) {
        this.individualUse = individualUse;
    }

    public Boolean getFreeShipping() {
        return freeShipping;
    }

    public void setFreeShipping(Boolean freeShipping) {
        this.freeShipping = freeShipping;
    }

    public Boolean getExcludeSaleItems() {
        return excludeSaleItems;
    }

    public void setExcludeSaleItems(Boolean excludeSaleItems) {
        this.excludeSaleItems = excludeSaleItems;
    }

    public String getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(String minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public String getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(String maximumAmount) {
        this.maximumAmount = maximumAmount;
    }
}
