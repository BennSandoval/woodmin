package app.bennsandoval.com.woodmin.interfaces;

import java.util.Map;

import app.bennsandoval.com.woodmin.models.customers.Customers;
import app.bennsandoval.com.woodmin.models.orders.Count;
import app.bennsandoval.com.woodmin.models.orders.Orders;
import app.bennsandoval.com.woodmin.models.products.Products;
import app.bennsandoval.com.woodmin.models.shop.Shop;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface Woocommerce {

    @GET("/")
    void getShop(Callback<Shop> shopInfo);

    @GET("/orders/count")
    void countOrders(Callback<Count> ordersCount);

    //?status=pending,processing,on-hold,completed,cancelled,refunded,failed&filter[limit]=100&page=0&filter[q]=
    @GET("/orders")
    void getOrders(@QueryMap Map<String, String> options,
                   Callback<Orders> orders);

    @GET("/products/count")
    void countProducts(Callback<Count> productsCount);

    //?filter[limit]=10&page=0&filter[q]=
    @GET("/products")
    void getProducts(@QueryMap Map<String, String> options,
                   Callback<Products> products);

    @GET("/customers/count")
    void countCustomers(Callback<Count> productsCount);

    //?filter[limit]=10&page=0&filter[q]=
    @GET("/customers")
    void getCustomers(@QueryMap Map<String, String> options,
                     Callback<Customers> products);
}
