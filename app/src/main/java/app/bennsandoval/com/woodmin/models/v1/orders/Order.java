package app.bennsandoval.com.woodmin.models.v1.orders;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.bennsandoval.com.woodmin.models.v3.customers.BillingAddress;
import app.bennsandoval.com.woodmin.models.v3.customers.ShippingAddress;
import app.bennsandoval.com.woodmin.models.v3.orders.Item;
import app.bennsandoval.com.woodmin.models.v3.orders.ShippingLine;

public class Order {

    private int id;
    @SerializedName("parent_id")
    private Integer parentId;
    private String status;
    @SerializedName("order_key")
    private String orderKey;
    private Integer number;
    private String currency;
    private String version;
    @SerializedName("prices_include_tax")
    private Boolean pricesIncludeTax;
    @SerializedName("date_created")
    private Date dateCreated;
    @SerializedName("date_modified")
    private Date dateModified;
    @SerializedName("customer_id")
    private Integer customerId;
    @SerializedName("discount_total")
    private String discountTotal;
    @SerializedName("discount_tax")
    private String discountTax;
    @SerializedName("shipping_total")
    private String shippingTotal;
    @SerializedName("shipping_tax")
    private String shippingTax;
    @SerializedName("set_paid")
    private Boolean setPaid;
    @SerializedName("cart_tax")
    private String cartTax;
    private String total;
    @SerializedName("total_tax")
    private String totalTax;

    @SerializedName("billing")
    private BillingAddress billingAddress;
    @SerializedName("shipping")
    private ShippingAddress shippingAddress;

    @SerializedName("payment_method")
    private String paymentMethod;
    @SerializedName("payment_method_title")
    private String paymentMethodTitle;
    @SerializedName("transaction_id")
    private String transactionId;
    @SerializedName("customer_ip_address")
    private String customerIpAddress;
    @SerializedName("customer_user_agent")
    private String customerUserAgent;
    @SerializedName("created_via")
    private String createdVia;
    @SerializedName("customer_note")
    private String customerNote;
    @SerializedName("date_completed")
    private Date dateCompleted;
    @SerializedName("date_paid")
    private Date datePaid;
    @SerializedName("cart_hash")
    private String cart_hash;

    @SerializedName("line_items")
    private List<Item> lineItems = new ArrayList<>();

    @SerializedName("shipping_lines")
    private List<ShippingLine> shippingLines = new ArrayList<>();

    @SerializedName("coupon_lines")
    private List<CouponLine> couponLines = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getPricesIncludeTax() {
        return pricesIncludeTax;
    }

    public void setPricesIncludeTax(Boolean pricesIncludeTax) {
        this.pricesIncludeTax = pricesIncludeTax;
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

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(String discountTotal) {
        this.discountTotal = discountTotal;
    }

    public String getDiscountTax() {
        return discountTax;
    }

    public void setDiscountTax(String discountTax) {
        this.discountTax = discountTax;
    }

    public String getShippingTotal() {
        return shippingTotal;
    }

    public void setShippingTotal(String shippingTotal) {
        this.shippingTotal = shippingTotal;
    }

    public String getShippingTax() {
        return shippingTax;
    }

    public void setShippingTax(String shippingTax) {
        this.shippingTax = shippingTax;
    }

    public Boolean getSetPaid() {
        return setPaid;
    }

    public void setSetPaid(Boolean setPaid) {
        this.setPaid = setPaid;
    }

    public String getCartTax() {
        return cartTax;
    }

    public void setCartTax(String cartTax) {
        this.cartTax = cartTax;
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

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethodTitle() {
        return paymentMethodTitle;
    }

    public void setPaymentMethodTitle(String paymentMethodTitle) {
        this.paymentMethodTitle = paymentMethodTitle;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerIpAddress() {
        return customerIpAddress;
    }

    public void setCustomerIpAddress(String customerIpAddress) {
        this.customerIpAddress = customerIpAddress;
    }

    public String getCustomerUserAgent() {
        return customerUserAgent;
    }

    public void setCustomerUserAgent(String customerUserAgent) {
        this.customerUserAgent = customerUserAgent;
    }

    public String getCreatedVia() {
        return createdVia;
    }

    public void setCreatedVia(String createdVia) {
        this.createdVia = createdVia;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public Date getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    public Date getDatePaid() {
        return datePaid;
    }

    public void setDatePaid(Date datePaid) {
        this.datePaid = datePaid;
    }

    public String getCart_hash() {
        return cart_hash;
    }

    public void setCart_hash(String cart_hash) {
        this.cart_hash = cart_hash;
    }

    public List<Item> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<Item> lineItems) {
        this.lineItems = lineItems;
    }

    public List<ShippingLine> getShippingLines() {
        return shippingLines;
    }

    public void setShippingLines(List<ShippingLine> shippingLines) {
        this.shippingLines = shippingLines;
    }

    public List<CouponLine> getCouponLines() {
        return couponLines;
    }

    public void setCouponLines(List<CouponLine> couponLines) {
        this.couponLines = couponLines;
    }
}
