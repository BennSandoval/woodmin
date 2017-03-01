package app.bennsandoval.com.woodmin.interfaces;

import java.util.ArrayList;
import java.util.Map;

import app.bennsandoval.com.woodmin.models.v1.orders.Order;

import app.bennsandoval.com.woodmin.models.v3.customers.Customers;
import app.bennsandoval.com.woodmin.models.v3.orders.Count;
import app.bennsandoval.com.woodmin.models.v1.orders.Coupon;
import app.bennsandoval.com.woodmin.models.v3.orders.Notes;
import app.bennsandoval.com.woodmin.models.v3.orders.OrderResponse;
import app.bennsandoval.com.woodmin.models.v3.orders.OrderUpdate;
import app.bennsandoval.com.woodmin.models.v3.orders.Orders;
import app.bennsandoval.com.woodmin.models.v3.products.ProductResponse;
import app.bennsandoval.com.woodmin.models.v3.products.Products;
import app.bennsandoval.com.woodmin.models.v3.shop.Shop;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface Woocommerce {


    @GET("wc-api/v3/")
    Call<Shop> getShop();


    @GET("wc-api/v3/orders/count")
    Call<Count> countOrders();

    @GET("wc-api/v3/orders")
    Call<Orders> getOrders(@QueryMap Map<String, String> options);

    @GET("wc-api/v3/orders/{orderId}/notes")
    Call<Notes> getOrdersNotes(@QueryMap Map<String, String> options,
                               @Path("orderId") String orderId);

    @PUT("wc-api/v3/orders/{orderId}")
    Call<OrderResponse> updateOrder(@Path("orderId") String orderId,
                                    @Body OrderUpdate order);

    @POST("wc-api/v3/orders")
    Call<OrderResponse> insertOrder(@Body OrderResponse order);

    @GET("wc-api/v3/products/count")
    Call<Count> countProducts();

    @GET("wc-api/v3/products")
    Call<Products> getProducts(@QueryMap Map<String, String> options);

    @GET("wc-api/v3/products/{productId}")
    Call<ProductResponse> getProduct(@Path("productId") int productId);

    @GET("wc-api/v3/customers/count")
    Call<Count> countCustomers();

    @GET("wc-api/v3/customers")
    Call<Customers> getCustomers(@QueryMap Map<String, String> options);




    @GET("wp-json/wc/v1/coupons")
    Call<ArrayList<Coupon>> getCoupons(@QueryMap Map<String, String> options);

    @POST("wp-json/wc/v1/orders")
    Call<Order> insertOrderV1(@Body Order order);

    @GET("wp-json/wc/v1/orders")
    Call<Order> getOrdersV1(@QueryMap Map<String, String> options);

    @PUT("wp-json/wc/v1/orders/{orderId}")
    Call<Order> updateOrderV1(@Path("orderId") String orderId,
                              @Body app.bennsandoval.com.woodmin.models.v1.orders.OrderUpdate order);

    /*
    @GET("wp-json/wc/v1/orders/count")
    Call<Count> countOrders();

    @GET("wp-json/wc/v1/wc-api/v3/orders/{orderId}/notes")
    Call<Notes> getOrdersNotes(@QueryMap Map<String, String> options,
                               @Path("orderId") String orderId);

    @GET("wp-json/wc/v1/wc-api/v3/products/count")
    Call<Count> countProducts();

    @GET("wp-json/wc/v1/wc-api/v3/products")
    Call<Products> getProducts(@QueryMap Map<String, String> options);

    @GET("wp-json/wc/v1/wc-api/v3/customers/count")
    Call<Count> countCustomers();

    @GET("wp-json/wc/v1/wc-api/v3/customers")
    Call<Customers> getCustomers(@QueryMap Map<String, String> options);

    @PUT("wp-json/wc/v1/wc-api/v3/orders/{orderId}")
    Call<OrderResponse> updateOrder(@Path("orderId") String orderId,
                                    @Body OrderUpdate order);

    @POST("wp-json/wc/v1/wc-api/v3/orders")
    Call<OrderResponse> insertOrder(@Body OrderResponse order);
    */
}
