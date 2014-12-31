package app.bennsandoval.com.woodmin.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.interfaces.Woocommerce;
import app.bennsandoval.com.woodmin.models.customers.Customers;
import app.bennsandoval.com.woodmin.models.orders.Count;
import app.bennsandoval.com.woodmin.models.customers.Customer;
import app.bennsandoval.com.woodmin.models.orders.Order;
import app.bennsandoval.com.woodmin.models.orders.Orders;
import app.bennsandoval.com.woodmin.models.products.Product;
import app.bennsandoval.com.woodmin.models.products.Products;
import app.bennsandoval.com.woodmin.models.shop.Shop;
import app.bennsandoval.com.woodmin.utilities.Utility;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class WoodminSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = WoodminSyncAdapter.class.getSimpleName();
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private int sizePage = 10;

    private int sizeOrders = 0;
    private int pageOrder = 1;

    private int sizeProducts = 0;
    private int pageProduct= 1;

    private int sizeCustomers = 0;
    private int pageCustomer= 1;

    private Gson gson = new GsonBuilder().create();

    public WoodminSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        Context context = getContext();
        String user = Utility.getPreferredUser(context);

        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        final String authenticationHeader = "Basic " + Base64.encodeToString(
                (user + ":" + accountManager.getPassword(account)).getBytes(),
                Base64.NO_WRAP);

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("Authorization", authenticationHeader);
                request.addHeader("Accept" , "application/json");
                request.addHeader("Content-Type" , "application/json");
            }
        };

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(60000, TimeUnit.MILLISECONDS);
        client.setReadTimeout(60000, TimeUnit.MILLISECONDS);
        client.setCache(null);

        String server = Utility.getPreferredServer(context);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(server+"/wc-api/v2")
                .setClient(new OkClient(client))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(requestInterceptor)
                .build();

        Woocommerce woocommerceApi = restAdapter.create(Woocommerce.class);

        //Shop
        synchronizeShop(woocommerceApi,context);

        //Orders
        synchronizeOrders(woocommerceApi,context);

        //Products
        synchronizeProducts(woocommerceApi,context);

        //Customers
        synchronizeCustomers(woocommerceApi,context);

        Long lastSyncKey =  Utility.getPreferredLastSync(getContext());
        Log.v(LOG_TAG, "Last sync" + lastSyncKey);
        Utility.setPreferredLastSync(getContext(), System.currentTimeMillis());

    }

    private void synchronizeCustomers(final Woocommerce woocommerceApi, final Context context) {

        woocommerceApi.countCustomers(new Callback<Count>() {

            @Override
            public void success(Count count, Response response) {

                try {
                    sizeCustomers = Integer.valueOf(count.getCount());
                } catch (NumberFormatException exception) {
                    Log.e(LOG_TAG, "NumberFormatException " + exception.getMessage());
                }

                int costumerRowsDeleted = context.getContentResolver().delete(WoodminContract.CostumerEntry.CONTENT_URI,null,null);
                Log.v(LOG_TAG,costumerRowsDeleted + " old costumer deleted");
                Log.v(LOG_TAG,sizeCustomers+" new costumer");

                while (sizeCustomers > 0) {
                    Log.v(LOG_TAG, "Read Customers " + sizeCustomers + " Page " + pageCustomer);

                    HashMap<String, String> options = new HashMap<>();
                    options.put("filter[limit]", String.valueOf(sizePage));
                    options.put("page", String.valueOf(pageCustomer));

                    woocommerceApi.getCustomers(options, new Callback<Customers>() {
                        @Override
                        public void success(Customers customers, Response response) {
                            Log.v(LOG_TAG, "Sucess page");

                            for (Customer customer : customers.getCustomers()) {

                                ContentValues customerValues = new ContentValues();
                                customerValues.put(WoodminContract.CostumerEntry.COLUMN_ID, customer.getId());
                                customerValues.put(WoodminContract.CostumerEntry.COLUMN_EMAIL, customer.getEmail());
                                customerValues.put(WoodminContract.CostumerEntry.COLUMN_FIRST_NAME, customer.getFirstName());
                                customerValues.put(WoodminContract.CostumerEntry.COLUMN_LAST_NAME, customer.getLastName());
                                customerValues.put(WoodminContract.CostumerEntry.COLUMN_USERNAME, customer.getUsername());
                                customerValues.put(WoodminContract.CostumerEntry.COLUMN_LAST_ORDER_ID, customer.getLastOrderId());

                                if(customer.getBillingAddress() != null){
                                    customerValues.put(WoodminContract.CostumerEntry.COLUMN_BILLING_FIRST_NAME, customer.getBillingAddress().getFirstName());
                                    customerValues.put(WoodminContract.CostumerEntry.COLUMN_BILLING_LAST_NAME, customer.getBillingAddress().getLastName());
                                }
                                if(customer.getShippingAddress() != null){
                                    customerValues.put(WoodminContract.CostumerEntry.COLUMN_SHIPPING_FIRST_NAME, customer.getShippingAddress().getFirstName());
                                    customerValues.put(WoodminContract.CostumerEntry.COLUMN_SHIPPING_LAST_NAME, customer.getShippingAddress().getLastName());
                                }
                                customerValues.put(WoodminContract.CostumerEntry.COLUMN_JSON,gson.toJson(customer));

                                Uri insertedCustomertUri = getContext().getContentResolver().insert(WoodminContract.CostumerEntry.CONTENT_URI, customerValues);
                                long customerId = ContentUris.parseId(insertedCustomertUri);
                                Log.d(LOG_TAG, "Customer successful inserted ID: " + customerId);

                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.v(LOG_TAG, "Customers sync error");
                            if (error.getCause() instanceof SSLHandshakeException) {
                                Log.e(LOG_TAG, "SSLHandshakeException Customers sync");
                            } else if (error.getResponse() == null) {
                                Log.e(LOG_TAG, "Not response error Customers sync");
                            } else {
                                int httpCode = error.getResponse().getStatus();
                                Log.e(LOG_TAG, httpCode + " error Customers sync");
                                switch (httpCode) {
                                    case 401:
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    });

                    sizeCustomers = sizeCustomers - sizePage;
                    pageCustomer++;

                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.v(LOG_TAG, "Products sync error");
                if (error.getCause() instanceof SSLHandshakeException) {
                    Log.e(LOG_TAG, "SSLHandshakeException Products sync");
                } else if (error.getResponse() == null) {
                    Log.e(LOG_TAG, "Not response error Products sync");
                } else {
                    int httpCode = error.getResponse().getStatus();
                    Log.e(LOG_TAG, httpCode + " error Products sync");
                    switch (httpCode) {
                        case 401:
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void synchronizeProducts(final Woocommerce woocommerceApi, final Context context) {

        woocommerceApi.countProducts(new Callback<Count>() {

            @Override
            public void success(Count count, Response response) {

                try {
                    sizeProducts = Integer.valueOf(count.getCount());
                } catch (NumberFormatException exception) {
                    Log.e(LOG_TAG, "NumberFormatException " + exception.getMessage());
                }

                int productRowsDeleted = context.getContentResolver().delete(WoodminContract.ProductEntry.CONTENT_URI,null,null);
                Log.v(LOG_TAG,"Product " + productRowsDeleted + " old deleted");
                Log.v(LOG_TAG,"Product " + sizeProducts+" new");

                while (sizeProducts > 0) {
                    Log.v(LOG_TAG, "Read Products " + sizeProducts + " Page " + pageProduct);

                    HashMap<String, String> options = new HashMap<>();
                    options.put("filter[limit]", String.valueOf(sizePage));
                    options.put("page", String.valueOf(pageProduct));

                    woocommerceApi.getProducts(options, new Callback<Products>() {
                        @Override
                        public void success(Products products, Response response) {
                            Log.v(LOG_TAG, "Sucess page");

                            for (Product product : products.getProducts()) {

                                ContentValues productValues = new ContentValues();
                                productValues.put(WoodminContract.ProductEntry.COLUMN_ID, product.getId());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_TITLE, product.getTitle());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_SKU, product.getSku());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_PRICE, product.getPrice());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_STOCK, product.getStockQuantity());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_JSON,gson.toJson(product));

                                Uri insertedProductUri = getContext().getContentResolver().insert(WoodminContract.ProductEntry.CONTENT_URI, productValues);
                                long productId = ContentUris.parseId(insertedProductUri);
                                Log.d(LOG_TAG, "Product successful inserted ID: " + productId);

                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.v(LOG_TAG, "Products sync error");
                            if (error.getCause() instanceof SSLHandshakeException) {
                                Log.e(LOG_TAG, "SSLHandshakeException Products sync");
                            } else if (error.getResponse() == null) {
                                Log.e(LOG_TAG, "Not response error Products sync");
                            } else {
                                int httpCode = error.getResponse().getStatus();
                                Log.e(LOG_TAG, httpCode + " error Products sync");
                                switch (httpCode) {
                                    case 401:
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    });

                    sizeProducts = sizeProducts - sizePage;
                    pageProduct++;

                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.v(LOG_TAG, "Products sync error");
                if (error.getCause() instanceof SSLHandshakeException) {
                    Log.e(LOG_TAG, "SSLHandshakeException Products sync");
                } else if (error.getResponse() == null) {
                    Log.e(LOG_TAG, "Not response error Products sync");
                } else {
                    int httpCode = error.getResponse().getStatus();
                    Log.e(LOG_TAG, httpCode + " error Products sync");
                    switch (httpCode) {
                        case 401:
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void synchronizeOrders(final Woocommerce woocommerceApi, final Context context) {

        woocommerceApi.countOrders(new Callback<Count>() {

            @Override
            public void success(Count count, Response response) {

                try {
                    sizeOrders = Integer.valueOf(count.getCount());
                } catch (NumberFormatException exception){
                    Log.e(LOG_TAG,"NumberFormatException "+exception.getMessage());
                }

                int ordersRowsDeleted = context.getContentResolver().delete(WoodminContract.OrdersEntry.CONTENT_URI,null,null);
                Log.v(LOG_TAG,"Orders " + ordersRowsDeleted + " old deleted");
                Log.v(LOG_TAG,"Orders " +sizeOrders+" new");

                while (sizeOrders > 0){
                    Log.v(LOG_TAG,"Product Read "+sizeOrders+" Page "+pageOrder);

                    HashMap<String, String> options = new HashMap<>();
                    options.put("status","pending,processing,on-hold,completed,cancelled,refunded,failed");
                    options.put("filter[limit]",String.valueOf(sizePage));
                    options.put("page",String.valueOf(pageOrder));
                    options.put("filter[q]","");

                    woocommerceApi.getOrders(options,new Callback<Orders>() {
                        @Override
                        public void success(Orders orders, Response response) {
                            Log.v(LOG_TAG,"Sucess page");

                            for(Order order:orders.getOrders()){

                                ContentValues orderValues = new ContentValues();

                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_ID, order.getId());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_ORDER_NUMBER, order.getOrderNumber());

                                //orderValues.put(WoodminContract.OrdersEntry.COLUMN_CREATED_AT, order.getCreatedAt());
                                //orderValues.put(WoodminContract.OrdersEntry.COLUMN_UPDATED_AT, order.getUpdatedAt());
                                //orderValues.put(WoodminContract.OrdersEntry.COLUMN_COMPLETED_AT, order.getCompletedAt());

                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_STATUS, order.getStatus());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CURRENCY, order.getCurrency());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL, order.getTotal());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SUBTOTAL, order.getSubtotal());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_LINE_ITEMS_QUANTITY, order.getTotalLineItemsQuantity());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_TAX, order.getTotalTax());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_SHIPPING, order.getTotalShipping());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CART_TAX, order.getCartTax());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_TAX, order.getShippingTax());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_DISCOUNT, order.getTotalDiscount());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CART_DISCOUNT, order.getCartDiscount());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_ORDER_DISCOUNT, order.getOrderDiscount());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_METHODS, order.getShippingMethods());

                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_NOTE, order.getNote());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_VIEW_ORDER_URL, order.getViewOrderUrl());

                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_PAYMENT_DETAILS_METHOD_ID, order.getPaymentDetails().getMethodId());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_PAYMENT_DETAILS_METHOD_TITLE, order.getPaymentDetails().getMethodTitle());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_PAYMENT_DETAILS_PAID, order.getPaymentDetails().isPaid() ? "1" : "0");

                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_FIRST_NAME, order.getBillingAddress().getFirstName());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_LAST_NAME , order.getBillingAddress().getLastName());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_COMPANY, order.getBillingAddress().getCompany());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_ADDRESS_1, order.getBillingAddress().getAddressOne());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_ADDRESS_2, order.getBillingAddress().getAddressTwo());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_CITY, order.getBillingAddress().getCity());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_STATE, order.getBillingAddress().getState());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_POSTCODE, order.getBillingAddress().getPostcode());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_COUNTRY, order.getBillingAddress().getCountry());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_EMAIL, order.getBillingAddress().getEmail());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_PHONE, order.getBillingAddress().getPhone());

                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_FIRST_NAME, order.getShippingAddress().getFirstName());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_LAST_NAME, order.getShippingAddress().getLastName());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_COMPANY, order.getShippingAddress().getCompany());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_ADDRESS_1, order.getShippingAddress().getAddressOne());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_ADDRESS_2, order.getShippingAddress().getAddressTwo());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_CITY, order.getShippingAddress().getCity());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_STATE, order.getShippingAddress().getState());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_POSTCODE, order.getShippingAddress().getPostcode());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_COUNTRY, order.getShippingAddress().getCountry());

                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_ID, order.getCustomerId());

                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_EMAIL, order.getCustomer().getEmail());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_FIRST_NAME, order.getCustomer().getFirstName());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_LAST_NAME, order.getCustomer().getLastName());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_USERNAME, order.getCustomer().getUsername());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_LAST_ORDER_ID, order.getCustomer().getLastOrderId());
                                //orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_LAST_ORDER_DATE, order.getCustomer().getLastOrderDate());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_ORDERS_COUNT, order.getCustomer().getOrdersCount());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_TOTAL_SPEND, order.getCustomer().getTotalSpent());
                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_AVATAR_URL, order.getCustomer().getAvatarUrl());

                                if(order.getCustomer().getBillingAddress()!= null){
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_FIRST_NAME, order.getCustomer().getBillingAddress().getFirstName());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_LAST_NAME, order.getCustomer().getBillingAddress().getLastName());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_COMPANY, order.getCustomer().getBillingAddress().getCompany());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_ADDRESS_1, order.getCustomer().getBillingAddress().getAddressOne());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_ADDRESS_2, order.getCustomer().getBillingAddress().getAddressTwo());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_CITY, order.getCustomer().getBillingAddress().getCity());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_STATE, order.getCustomer().getBillingAddress().getState());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_POSTCODE, order.getCustomer().getBillingAddress().getPostcode());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_COUNTRY, order.getCustomer().getBillingAddress().getCountry());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_EMAIL, order.getCustomer().getBillingAddress().getEmail());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_PHONE, order.getCustomer().getBillingAddress().getPhone());
                                }

                                if(order.getCustomer().getShippingAddress() != null){
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_FIRST_NAME, order.getCustomer().getShippingAddress().getFirstName());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_LAST_NAME , order.getCustomer().getShippingAddress().getLastName());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_COMPANY, order.getCustomer().getShippingAddress().getCompany());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_ADDRESS_1, order.getCustomer().getShippingAddress().getAddressOne());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_ADDRESS_2, order.getCustomer().getShippingAddress().getAddressTwo());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_CITY, order.getCustomer().getShippingAddress().getCity());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_STATE, order.getCustomer().getShippingAddress().getState());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_POSTCODE, order.getCustomer().getShippingAddress().getPostcode());
                                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_COUNTRY, order.getCustomer().getShippingAddress().getCountry());
                                }

                                orderValues.put(WoodminContract.OrdersEntry.COLUMN_JSON,gson.toJson(order));

                                Uri insertedOrderUri = getContext().getContentResolver().insert(WoodminContract.OrdersEntry.CONTENT_URI, orderValues);
                                long orderId = ContentUris.parseId(insertedOrderUri);
                                Log.d(LOG_TAG, "Order successful inserted ID: " + orderId);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.v(LOG_TAG,"Orders sync error");
                            if (error.getCause() instanceof SSLHandshakeException) {
                                Log.e(LOG_TAG,"SSLHandshakeException Orders sync");
                            } else if (error.getResponse()==null) {
                                Log.e(LOG_TAG,"Not response error Orders sync");
                            } else {
                                int httpCode = error.getResponse().getStatus();
                                Log.e(LOG_TAG,httpCode + " error Orders sync");
                                switch (httpCode){
                                    case 401:
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    });

                    sizeOrders = sizeOrders - sizePage;
                    pageOrder++;

                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.v(LOG_TAG,"Orders sync error");
                if (error.getCause() instanceof SSLHandshakeException) {
                    Log.e(LOG_TAG,"SSLHandshakeException Orders sync");
                } else if (error.getResponse()==null) {
                    Log.e(LOG_TAG,"Not response error Orders sync");
                } else {
                    int httpCode = error.getResponse().getStatus();
                    Log.e(LOG_TAG,httpCode + " error Orders sync");
                    switch (httpCode){
                        case 401:
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void synchronizeShop(Woocommerce woocommerceApi, final Context context) {

        woocommerceApi.getShop(new Callback<Shop>() {
            @Override
            public void success(Shop shop, Response response) {
                Log.v(LOG_TAG,"Shop sync success");

                int shopRowsDeleted = context.getContentResolver().delete(WoodminContract.ShopEntry.CONTENT_URI,null,null);
                Log.v(LOG_TAG,shopRowsDeleted + " Shop rows deleted");

                ContentValues shopValues = new ContentValues();
                shopValues.put(WoodminContract.ShopEntry.COLUMN_NAME, shop.getStore().getName());
                shopValues.put(WoodminContract.ShopEntry.COLUMN_DESCRIPTION, shop.getStore().getDescription());
                shopValues.put(WoodminContract.ShopEntry.COLUMN_URL, shop.getStore().getUrl());
                shopValues.put(WoodminContract.ShopEntry.COLUMN_WC_VERSION, shop.getStore().getWcVersion());
                shopValues.put(WoodminContract.ShopEntry.COLUMN_META_CURRENCY, shop.getStore().getMeta().getCurrency());
                shopValues.put(WoodminContract.ShopEntry.COLUMN_META_CURRENCY_FORMAT, shop.getStore().getMeta().getCurrencyFormat());
                shopValues.put(WoodminContract.ShopEntry.COLUMN_META_DIMENSION_UNIT, shop.getStore().getMeta().getDimensionUnit());
                shopValues.put(WoodminContract.ShopEntry.COLUMN_META_TAXI_INCLUDE, shop.getStore().getMeta().isTaxIncluded() ? "1" : "0");
                shopValues.put(WoodminContract.ShopEntry.COLUMN_META_TIMEZONE, shop.getStore().getMeta().getTimezone());
                shopValues.put(WoodminContract.ShopEntry.COLUMN_META_WEIGHT_UNIT, shop.getStore().getMeta().getWeightUnit());

                Uri insertedShopUri = getContext().getContentResolver().insert(WoodminContract.ShopEntry.CONTENT_URI, shopValues);
                long shopId = ContentUris.parseId(insertedShopUri);
                Log.d(LOG_TAG, "Shop successful inserted ID: " + shopId);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.v(LOG_TAG,"Shop sync error");
                if (error.getCause() instanceof SSLHandshakeException) {
                    Log.e(LOG_TAG,"SSLHandshakeException Shop sync");
                } else if (error.getResponse()==null) {
                    Log.e(LOG_TAG,"Not response error Shop sync");
                } else {
                    int httpCode = error.getResponse().getStatus();
                    Log.e(LOG_TAG,httpCode + " error Shop sync");
                    switch (httpCode){
                        case 401:
                            break;
                        default:
                            break;
                    }
                }
            }
        });

    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static Account getSyncAccount(Context context) {

        String user = Utility.getPreferredUser(context);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account account = new Account("Woodmin", context.getString(R.string.sync_account_type));
        if ( accountManager.getPassword(account) == null  ) {
            String secret = Utility.getPreferredSecret(context);
            if (!accountManager.addAccountExplicitly(account, secret, null)) {
                return null;
            }
            onAccountCreated(account, context);
        }
        return account;

    }

    private static void onAccountCreated(Account newAccount, Context context) {
        WoodminSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }
}
