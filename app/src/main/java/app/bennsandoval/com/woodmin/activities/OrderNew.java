package app.bennsandoval.com.woodmin.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.Woodmin;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.interfaces.Woocommerce;
import app.bennsandoval.com.woodmin.models.v1.orders.OrderUpdate;
import app.bennsandoval.com.woodmin.models.v3.customers.BillingAddress;
import app.bennsandoval.com.woodmin.models.v3.customers.ShippingAddress;
import app.bennsandoval.com.woodmin.models.v1.orders.Coupon;
import app.bennsandoval.com.woodmin.models.v1.orders.CouponLine;
import app.bennsandoval.com.woodmin.models.v3.orders.Item;
import app.bennsandoval.com.woodmin.models.v3.orders.MetaItem;
import app.bennsandoval.com.woodmin.models.v1.orders.Order;
import app.bennsandoval.com.woodmin.models.v3.orders.ShippingLine;
import app.bennsandoval.com.woodmin.models.v3.products.Product;
import app.bennsandoval.com.woodmin.models.v3.products.Variation;
import app.bennsandoval.com.woodmin.utilities.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderNew extends AppCompatActivity {

    private final String LOG_TAG = OrderNew.class.getSimpleName();

    private Gson mGson = new GsonBuilder().create();
    private Order mOrder;
    private float mTotal = 0;
    private float mDiscount = 0;

    private EditText mEmail;
    private EditText mPhone;
    private TextView mPrice;
    private EditText mCustomerFirst;
    private EditText mCustomerLast;
    private EditText mCoupon;

    private EditText mBillingCompany;

    private EditText mBillingAddressOne;
    private EditText mBillingAddressTwo;
    private EditText mBillingAddressCity;
    private EditText mBillingAddressCountry;
    private EditText mBillingAddressCP;
    private EditText mBillingAddressState;

    private ProgressDialog mProgress;

    private static final String[] PRODUCT_PROJECTION = {
            WoodminContract.ProductEntry.COLUMN_ID,
            WoodminContract.ProductEntry.COLUMN_JSON,
    };
    private int COLUMN_PRODUCT_COLUMN_JSON = 1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private boolean mSaveIncomplete = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_add);
        mProgress = new ProgressDialog(OrderNew.this);
        mProgress.setTitle(getString(R.string.app_name));

        mEmail = (EditText) findViewById(R.id.email);
        mPhone = (EditText) findViewById(R.id.phone);
        mPrice = (TextView) findViewById(R.id.price);
        mCustomerFirst = (EditText) findViewById(R.id.customer_first);
        mCustomerLast = (EditText) findViewById(R.id.customer_last);

        mBillingCompany = (EditText) findViewById(R.id.company);

        mBillingAddressOne = (EditText) findViewById(R.id.billing_address_one);
        mBillingAddressTwo = (EditText) findViewById(R.id.billing_address_two);
        mBillingAddressCity = (EditText) findViewById(R.id.billing_city);
        mBillingAddressCountry = (EditText) findViewById(R.id.billing_country);
        mBillingAddressCP = (EditText) findViewById(R.id.billing_postal_code);
        mBillingAddressState = (EditText) findViewById(R.id.billing_state);

        mCoupon = (EditText) findViewById(R.id.cupon);
        final Button validate = (Button) findViewById(R.id.validate);
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setMessage(getString(R.string.validating));
                        mProgress.show();
                    }
                });

                HashMap<String, String> options = new HashMap<>();
                options.put("per_page", "10");
                options.put("page", "1");
                options.put("status", "any");
                options.put("search", mCoupon.getText().toString());

                Call<ArrayList<Coupon>> call = ((Woodmin)getApplication()).getWoocommerceApiHandler().getCoupons(options);
                call.enqueue(new Callback<ArrayList<Coupon>>() {

                    @Override
                    public void onResponse(Call<ArrayList<Coupon>> call, Response<ArrayList<Coupon>> response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgress.dismiss();
                            }
                        });
                        int statusCode = response.code();
                        if (statusCode == 200) {
                            ArrayList<Coupon> coupons = response.body();
                            for (Coupon coupon : coupons) {
                                if(coupon.getCode().toLowerCase().equals(mCoupon.getText().toString().toLowerCase())) {

                                    Float subtotal = 0.0f;
                                    Float shipping = 0.0f;
                                    for(Item itemInOrder: mOrder.getLineItems()) {
                                        subtotal += Float.valueOf(itemInOrder.getTotal());
                                    }
                                    final Float total = subtotal + shipping;
                                    if(coupon.getMinimumAmount() == null) {
                                        coupon.setMinimumAmount("0");
                                    }
                                    if(coupon.getMaximumAmount() == null || coupon.getMaximumAmount().equals("0.00")) {
                                        coupon.setMaximumAmount(String.valueOf(total));
                                    }
                                    if(total >= Double.valueOf(coupon.getMinimumAmount())) {
                                        if(total <= Double.valueOf(coupon.getMaximumAmount())) {

                                            mDiscount = Float.valueOf(coupon.getAmount());

                                            CouponLine couponLine = new CouponLine();
                                            couponLine.setId(coupon.getId());
                                            couponLine.setCode(coupon.getCode());
                                            couponLine.setType(coupon.getDiscountType());
                                            couponLine.setDiscount(String.valueOf(total * mDiscount/100));

                                            mOrder.getCouponLines().clear();
                                            mOrder.getCouponLines().add(couponLine);
                                            mCoupon.setText(couponLine.getCode() + " - " + coupon.getDescription() + " - " + coupon.getAmount() + " - " + coupon.getDiscountType() + " - $" + couponLine.getDiscount());
                                            mCoupon.setEnabled(false);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Utility.setPreferredShoppingCard(getApplicationContext(), mGson.toJson(mOrder));
                                                    refreshView();
                                                }
                                            });

                                            validate.setVisibility(View.GONE);

                                            Toast.makeText(getApplicationContext(), coupon.getDescription(), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No es posible aplicar el cupon: " + coupon.getDescription(), Toast.LENGTH_LONG).show();
                                            validate.setText("APLICAR");
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No es posible aplicar el cupon: " + coupon.getDescription(), Toast.LENGTH_LONG).show();
                                        validate.setText("APLICAR");
                                    }

                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Coupon>> call, Throwable t) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgress.dismiss();
                            }
                        });
                    }
                });

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mOrder.getLineItems().size() > 0) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OrderNew.this)
                                .setTitle(getString(R.string.new_order_title))
                                .setMessage(getString(R.string.order_create_confirmation))
                                .setCancelable(true)
                                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        createOrder();
                                    }
                                })
                                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        alertDialogBuilder.create().show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.invalid_items), Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSaveIncomplete) {
            mOrder.setPaymentMethod(getString(R.string.default_payment_code));
            mOrder.setPaymentMethodTitle(getString(R.string.default_payment));

            ShippingLine shippingLine = new ShippingLine();
            shippingLine.setMethodId(getString(R.string.default_shipping_method_id));
            shippingLine.setMethodTitle(getString(R.string.default_shipping_method_title));
            shippingLine.setTotal(getString(R.string.default_shipping_method_title));
            mOrder.getShippingLines().clear();
            mOrder.getShippingLines().add(shippingLine);

            mOrder.getCouponLines().clear();

            BillingAddress billingAddress = new BillingAddress();
            billingAddress.setFirstName(mCustomerFirst.getText().toString());
            billingAddress.setLastName(mCustomerLast.getText().toString());
            billingAddress.setCompany(mBillingCompany.getText().toString());
            billingAddress.setAddressOne(mBillingAddressOne.getText().toString());
            billingAddress.setAddressTwo(mBillingAddressTwo.getText().toString());
            billingAddress.setCity(mBillingAddressCity.getText().toString());
            billingAddress.setState(mBillingAddressState.getText().toString());
            billingAddress.setPostcode(mBillingAddressCP.getText().toString());
            billingAddress.setCountry(mBillingAddressCountry.getText().toString());
            billingAddress.setEmail(mEmail.getText().toString());
            billingAddress.setPhone(mPhone.getText().toString());
            mOrder.setBillingAddress(billingAddress);

            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setFirstName(mCustomerFirst.getText().toString());
            shippingAddress.setLastName(mCustomerLast.getText().toString());
            shippingAddress.setCompany(mBillingCompany.getText().toString());
            shippingAddress.setAddressOne(mBillingAddressOne.getText().toString());
            shippingAddress.setAddressTwo(mBillingAddressTwo.getText().toString());
            shippingAddress.setCity(mBillingAddressCity.getText().toString());
            shippingAddress.setState(mBillingAddressState.getText().toString());
            shippingAddress.setPostcode(mBillingAddressCP.getText().toString());
            shippingAddress.setCountry(mBillingAddressCountry.getText().toString());
            mOrder.setShippingAddress(shippingAddress);

            String json = mGson.toJson(mOrder);
            Log.i(LOG_TAG, json);

            Utility.setPreferredShoppingCard(getApplicationContext(), json);
        }

    }

    private void refreshView() {
        mTotal = 0;
        String json = Utility.getPreferredShoppingCard(getApplicationContext());
        if (json != null) {
            mOrder = mGson.fromJson(json, Order.class);
            if(mOrder.getBillingAddress() == null) {

                BillingAddress billingAddress = new BillingAddress();
                billingAddress.setCompany(getString(R.string.default_company));
                billingAddress.setFirstName(getString(R.string.default_first_name));
                billingAddress.setLastName(getString(R.string.default_last_name));
                billingAddress.setAddressOne(getString(R.string.default_line_one));
                billingAddress.setAddressTwo(getString(R.string.default_line_two));
                billingAddress.setCity(getString(R.string.default_city));
                billingAddress.setState(getString(R.string.default_state));
                billingAddress.setPostcode(getString(R.string.default_postal_code));
                billingAddress.setCountry(getString(R.string.default_country));
                billingAddress.setEmail(getString(R.string.default_email));
                billingAddress.setPhone(getString(R.string.default_phone));

                mOrder.setBillingAddress(billingAddress);

                Utility.setPreferredShoppingCard(getApplicationContext(), mGson.toJson(mOrder));
            }
        } else {
            mOrder = new Order();

            BillingAddress billingAddress = new BillingAddress();
            billingAddress.setCompany(getString(R.string.default_company));
            billingAddress.setFirstName(getString(R.string.default_first_name));
            billingAddress.setLastName(getString(R.string.default_last_name));
            billingAddress.setAddressOne(getString(R.string.default_line_one));
            billingAddress.setAddressTwo(getString(R.string.default_line_two));
            billingAddress.setCity(getString(R.string.default_city));
            billingAddress.setState(getString(R.string.default_state));
            billingAddress.setPostcode(getString(R.string.default_postal_code));
            billingAddress.setCountry(getString(R.string.default_country));
            billingAddress.setEmail(getString(R.string.default_email));
            billingAddress.setPhone(getString(R.string.default_phone));

            mOrder.setBillingAddress(billingAddress);

            Utility.setPreferredShoppingCard(getApplicationContext(), mGson.toJson(mOrder));
        }

        Button clear = (Button) findViewById(R.id.clear);
        if (mOrder.getLineItems().size() == 0) {
            clear.setVisibility(View.GONE);
        }
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreProducts();
                Utility.setPreferredShoppingCard(getApplicationContext(), null);
                mSaveIncomplete = false;
                finish();
            }
        });

        mPrice.setText(getString(R.string.price, '0'));

        mCustomerFirst.setText(mOrder.getBillingAddress().getFirstName());
        mCustomerLast.setText(mOrder.getBillingAddress().getLastName());
        mEmail.setText(mOrder.getBillingAddress().getEmail());
        mPhone.setText(mOrder.getBillingAddress().getPhone());

        mBillingCompany.setText(mOrder.getBillingAddress().getCompany());

        mBillingAddressOne.setText(mOrder.getBillingAddress().getAddressOne());
        mBillingAddressTwo.setText(mOrder.getBillingAddress().getAddressTwo());
        mBillingAddressCP.setText(mOrder.getBillingAddress().getPostcode());
        mBillingAddressState.setText(mOrder.getBillingAddress().getState());
        mBillingAddressCity.setText(mOrder.getBillingAddress().getCity());
        mBillingAddressCountry.setText(mOrder.getBillingAddress().getCountry());

        LinearLayout cardDetails = (LinearLayout) findViewById(R.id.shopping_card_details);
        while(cardDetails.getChildCount() > 2) {
            cardDetails.removeViewAt(2);
        }

        List<String> ids = new ArrayList<>();
        List<String> parameters = new ArrayList<>();

        for (Item item : mOrder.getLineItems()) {
            ids.add(String.valueOf(item.getProductId()));
            parameters.add("?");
        }

        String query = WoodminContract.ProductEntry.COLUMN_ID + " IN (" + TextUtils.join(", ", parameters) + ")";
        Cursor cursor = getContentResolver().query(WoodminContract.ProductEntry.CONTENT_URI,
                PRODUCT_PROJECTION,
                query,
                ids.toArray(new String[ids.size()]),
                null);

        List<Product> products = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    json = cursor.getString(COLUMN_PRODUCT_COLUMN_JSON);
                    if (json != null) {
                        Product product = mGson.fromJson(json, Product.class);
                        products.add(product);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        for (final Item item : mOrder.getLineItems()) {

            if(item.getTotal() == null){
                item.setTotal("0");
            }
            mTotal += Float.valueOf(item.getTotal());

            View child = getLayoutInflater().inflate(R.layout.activity_order_item, null);
            ImageView imageView = (ImageView) child.findViewById(R.id.image);
            TextView quantity = (TextView) child.findViewById(R.id.quantity);
            TextView description = (TextView) child.findViewById(R.id.description);
            TextView price = (TextView) child.findViewById(R.id.price);
            TextView sku = (TextView) child.findViewById(R.id.sku);

            quantity.setText(String.valueOf(item.getQuantity()));
            if (item.getMeta().size() > 0) {
                String descriptionWithMeta = item.getName();
                for (MetaItem itemMeta : item.getMeta()) {
                    descriptionWithMeta += "\n" + itemMeta.getLabel() + " " + itemMeta.getValue();
                }
                description.setText(descriptionWithMeta);
            } else {
                description.setText(item.getName());
            }

            String itemTotal = item.getTotal();
            if(mDiscount>0) {
                float totalItem = Float.valueOf(item.getTotal());
                float discountItem = totalItem * mDiscount/100;
                totalItem = totalItem - discountItem;
                itemTotal = String.valueOf(totalItem);
            }
            price.setText(getString(R.string.price, itemTotal));
            sku.setText(item.getSku());

            Product productForItem = null;
            for (Product product : products) {
                if (product.getId() == item.getProductId()) {
                    productForItem = product;
                    break;
                }
                for (Variation variation : product.getVariations()) {
                    if (variation.getId() == item.getProductId()) {
                        productForItem = product;
                        break;
                    }
                }
            }

            if (productForItem == null) {
                Log.v(LOG_TAG, "Missing product");
            } else {
                Picasso.with(getApplicationContext())
                        .load(productForItem.getFeaturedSrc())
                        .resize(50, 50)
                        .centerCrop()
                        .placeholder(android.R.color.transparent)
                        .error(R.drawable.ic_action_cancel)
                        .into(imageView);
            }
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TextView sku = (TextView) v.findViewById(R.id.sku);
                    //Toast.makeText(getApplicationContext(), sku.getText().toString(), Toast.LENGTH_LONG).show();

                    Product product = null;
                    String query = WoodminContract.ProductEntry.COLUMN_SKU + " == ?" ;
                    String[] parametersOrder = new String[]{ sku.getText().toString() };
                    Cursor cursor = getContentResolver().query(WoodminContract.ProductEntry.CONTENT_URI,
                            PRODUCT_PROJECTION,
                            query,
                            parametersOrder,
                            null);
                    if(cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                String json = cursor.getString(COLUMN_PRODUCT_COLUMN_JSON);
                                if(json!=null){
                                    product = mGson.fromJson(json, Product.class);
                                }
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }

                    if(product != null) {
                        Intent intent = new Intent(getApplicationContext(), OrderAddProduct.class);
                        intent.putExtra("product", product.getId());
                        startActivity(intent);
                    } else {
                        mOrder.getLineItems().remove(item);
                        String json = mGson.toJson(mOrder);
                        Utility.setPreferredShoppingCard(getApplicationContext(), json);
                        refreshView();
                    }

                }
            });
            cardDetails.addView(child);
        }

        if(mDiscount>0) {
            float discount = mTotal * mDiscount/100;
            mTotal = mTotal - discount;
        }
        mPrice.setText(getString(R.string.price, String.valueOf(mTotal)));

    }

    private void restoreProducts() {

        List<String> ids = new ArrayList<>();
        List<String> parameters = new ArrayList<>();

        for (Item item : mOrder.getLineItems()) {
            ids.add(String.valueOf(item.getProductId()));
            parameters.add("?");
        }

        String query = WoodminContract.ProductEntry.COLUMN_ID + " IN (" + TextUtils.join(", ", parameters) + ")";
        Cursor cursor = getContentResolver().query(WoodminContract.ProductEntry.CONTENT_URI,
                PRODUCT_PROJECTION,
                query,
                ids.toArray(new String[ids.size()]),
                null);

        List<Product> products = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String json = cursor.getString(COLUMN_PRODUCT_COLUMN_JSON);
                    if (json != null) {
                        Product product = mGson.fromJson(json, Product.class);
                        products.add(product);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        ArrayList<ContentValues> productsValues = new ArrayList<ContentValues>();

        for (Item item : mOrder.getLineItems()) {
            for (Product product : products) {

                int stockRestore = product.getStockQuantity() + item.getQuantity();
                if (product.getId() == item.getProductId()) {
                    product.setStockQuantity(stockRestore);
                }

                ContentValues productValues = new ContentValues();
                productValues.put(WoodminContract.ProductEntry.COLUMN_ID, product.getId());
                productValues.put(WoodminContract.ProductEntry.COLUMN_TITLE, product.getTitle());
                productValues.put(WoodminContract.ProductEntry.COLUMN_SKU, product.getSku());
                productValues.put(WoodminContract.ProductEntry.COLUMN_PRICE, product.getPrice());
                productValues.put(WoodminContract.ProductEntry.COLUMN_STOCK, product.getStockQuantity());
                productValues.put(WoodminContract.ProductEntry.COLUMN_JSON, mGson.toJson(product));
                productValues.put(WoodminContract.ProductEntry.COLUMN_ENABLE, 1);

                productsValues.add(productValues);

                for (Variation variation : product.getVariations()) {

                    //TODO, CHANGE THIS APPROACH
                    product.setSku(variation.getSku());
                    product.setPrice(variation.getPrice());

                    if (variation.getId() == item.getProductId()) {
                        product.setStockQuantity(variation.getStockQuantity() + item.getQuantity());
                    }

                    ContentValues variationValues = new ContentValues();
                    variationValues.put(WoodminContract.ProductEntry.COLUMN_ID, variation.getId());
                    variationValues.put(WoodminContract.ProductEntry.COLUMN_TITLE, product.getTitle());
                    variationValues.put(WoodminContract.ProductEntry.COLUMN_SKU, product.getSku());
                    variationValues.put(WoodminContract.ProductEntry.COLUMN_PRICE, product.getPrice());
                    variationValues.put(WoodminContract.ProductEntry.COLUMN_STOCK, product.getStockQuantity());
                    variationValues.put(WoodminContract.ProductEntry.COLUMN_JSON, mGson.toJson(product));
                    variationValues.put(WoodminContract.ProductEntry.COLUMN_ENABLE, 1);

                    productsValues.add(variationValues);

                }


            }
        }

        ContentValues[] productsValuesArray = new ContentValues[productsValues.size()];
        productsValuesArray = productsValues.toArray(productsValuesArray);
        int ordersRowsUpdated = getContentResolver().bulkInsert(WoodminContract.ProductEntry.CONTENT_URI, productsValuesArray);
        Log.v(LOG_TAG, "Products " + ordersRowsUpdated + " updated");

        getContentResolver().notifyChange(WoodminContract.ProductEntry.CONTENT_URI, null, false);

    }

    private void createOrder() {

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setEnabled(false);

        mProgress.setMessage(getString(R.string.create_order));
        mProgress.show();

        //mOrder.setStatus("completed");

        mOrder.setPaymentMethod(getString(R.string.default_payment_code));
        mOrder.setPaymentMethodTitle(getString(R.string.default_payment));
        mOrder.setSetPaid(true);

        ShippingLine shippingLine = new ShippingLine();
        shippingLine.setMethodId(getString(R.string.default_shipping_method_id));
        shippingLine.setMethodTitle(getString(R.string.default_shipping_method_title));
        shippingLine.setTotal(getString(R.string.default_shipping_method_title));
        mOrder.getShippingLines().clear();
        mOrder.getShippingLines().add(shippingLine);

        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setFirstName(mCustomerFirst.getText().toString());
        billingAddress.setLastName(mCustomerLast.getText().toString());
        billingAddress.setCompany(mBillingCompany.getText().toString());
        billingAddress.setAddressOne(mBillingAddressOne.getText().toString());
        billingAddress.setAddressTwo(mBillingAddressTwo.getText().toString());
        billingAddress.setCity(mBillingAddressCity.getText().toString());
        billingAddress.setState(mBillingAddressState.getText().toString());
        billingAddress.setPostcode(mBillingAddressCP.getText().toString());
        billingAddress.setCountry(mBillingAddressCountry.getText().toString());
        billingAddress.setEmail(mEmail.getText().toString());
        billingAddress.setPhone(mPhone.getText().toString());
        mOrder.setBillingAddress(billingAddress);

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFirstName(mCustomerFirst.getText().toString());
        shippingAddress.setLastName(mCustomerLast.getText().toString());
        shippingAddress.setCompany(mBillingCompany.getText().toString());
        shippingAddress.setAddressOne(mBillingAddressOne.getText().toString());
        shippingAddress.setAddressTwo(mBillingAddressTwo.getText().toString());
        shippingAddress.setCity(mBillingAddressCity.getText().toString());
        shippingAddress.setState(mBillingAddressState.getText().toString());
        shippingAddress.setPostcode(mBillingAddressCP.getText().toString());
        shippingAddress.setCountry(mBillingAddressCountry.getText().toString());
        mOrder.setShippingAddress(shippingAddress);

        for (Item item : mOrder.getLineItems()) {
            if(mDiscount >0) {
                float totalItem = Float.valueOf(item.getTotal());
                float discountItem = totalItem * mDiscount/100;
                totalItem = totalItem - discountItem;
                item.setTotal(String.valueOf(totalItem));
            }
        }

        String json = mGson.toJson(mOrder);
        Log.i(LOG_TAG, json);


        Woocommerce woocommerceApi = ((Woodmin) getApplication()).getWoocommerceApiHandler();
        Call<Order> call = woocommerceApi.insertOrderV1(mOrder);
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                int statusCode = response.code();
                if (statusCode == 201) {
                    Order order = response.body();

                    String json = mGson.toJson(order);
                    Log.i(LOG_TAG, json);

                    ContentValues orderValues = new ContentValues();
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_ID, order.getId());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_ORDER_NUMBER, order.getNumber());
                    if (order.getDateCreated() != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(order.getDateCreated());
                        calendar.add(Calendar.HOUR, -6);
                        order.setDateCreated(calendar.getTime());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CREATED_AT, WoodminContract.getDbDateString(order.getDateCreated()));
                    }
                    if (order.getDateModified() != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(order.getDateModified());
                        calendar.add(Calendar.HOUR, -6);
                        order.setDateModified(calendar.getTime());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_UPDATED_AT, WoodminContract.getDbDateString(order.getDateModified()));
                    }
                    if (order.getDateCompleted() != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(order.getDateCompleted());
                        calendar.add(Calendar.HOUR, -6);
                        order.setDateCompleted(calendar.getTime());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_COMPLETED_AT, WoodminContract.getDbDateString(order.getDateCompleted()));
                    }
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_STATUS, order.getStatus());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CURRENCY, order.getCurrency());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL, order.getTotal());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_LINE_ITEMS_QUANTITY, order.getLineItems().size());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_TAX, order.getTotalTax());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_SHIPPING, order.getShippingTotal());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CART_TAX, order.getCartTax());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_TAX, order.getShippingTax());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_DISCOUNT, order.getDiscountTotal());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_METHODS, order.getShippingLines().get(0).getMethodTitle());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_PAYMENT_DETAILS_METHOD_ID, order.getPaymentMethod());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_PAYMENT_DETAILS_METHOD_TITLE, order.getPaymentMethodTitle());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_PAYMENT_DETAILS_PAID, "1");
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_FIRST_NAME, order.getBillingAddress().getFirstName());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_LAST_NAME, order.getBillingAddress().getLastName());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_COMPANY, order.getBillingAddress().getCompany());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_ADDRESS_1, order.getBillingAddress().getAddressOne());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_ADDRESS_2, order.getBillingAddress().getAddressTwo());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_CITY, order.getBillingAddress().getCity());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_STATE, order.getBillingAddress().getState());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_POSTCODE, order.getBillingAddress().getPostcode());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_COUNTRY, order.getBillingAddress().getCountry());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_EMAIL, order.getBillingAddress().getEmail());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_PHONE, order.getBillingAddress().getPhone());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_ID, order.getCustomerId());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_EMAIL, order.getBillingAddress().getEmail());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_FIRST_NAME, order.getBillingAddress().getFirstName());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_LAST_NAME, order.getBillingAddress().getLastName());
                    if (order.getBillingAddress() != null) {
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_FIRST_NAME, order.getShippingAddress().getFirstName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_LAST_NAME, order.getShippingAddress().getLastName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_COMPANY, order.getShippingAddress().getCompany());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_ADDRESS_1, order.getShippingAddress().getAddressOne());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_ADDRESS_2, order.getShippingAddress().getAddressTwo());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_CITY, order.getShippingAddress().getCity());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_STATE, order.getShippingAddress().getState());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_POSTCODE, order.getShippingAddress().getPostcode());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_COUNTRY, order.getShippingAddress().getCountry());

                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_FIRST_NAME, order.getBillingAddress().getFirstName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_LAST_NAME, order.getBillingAddress().getLastName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_COMPANY, order.getBillingAddress().getCompany());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_ADDRESS_1, order.getBillingAddress().getAddressOne());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_ADDRESS_2, order.getBillingAddress().getAddressTwo());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_CITY, order.getBillingAddress().getCity());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_STATE, order.getBillingAddress().getState());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_POSTCODE, order.getBillingAddress().getPostcode());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_COUNTRY, order.getBillingAddress().getCountry());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_EMAIL, order.getBillingAddress().getEmail());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_PHONE, order.getBillingAddress().getPhone());
                    }
                    if (order.getShippingAddress() != null) {
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_FIRST_NAME, order.getShippingAddress().getFirstName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_LAST_NAME, order.getShippingAddress().getLastName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_COMPANY, order.getShippingAddress().getCompany());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_ADDRESS_1, order.getShippingAddress().getAddressOne());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_ADDRESS_2, order.getShippingAddress().getAddressTwo());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_CITY, order.getShippingAddress().getCity());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_STATE, order.getShippingAddress().getState());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_POSTCODE, order.getShippingAddress().getPostcode());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_COUNTRY, order.getShippingAddress().getCountry());
                    }
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_JSON, mGson.toJson(order));
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_ENABLE, 1);

                    Uri insertedOrderUri = getContentResolver().insert(WoodminContract.OrdersEntry.CONTENT_URI, orderValues);
                    long orderId = ContentUris.parseId(insertedOrderUri);
                    Log.d(LOG_TAG, "Orders successful updated ID: " + orderId);

                    finalizeOrder(order);

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.dismiss();
                        }
                    });
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    fab.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.dismiss();
                        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                        fab.setEnabled(true);
                    }
                });
            }
        });
    }

    private void finalizeOrder(Order order) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress.setMessage(getString(R.string.finalize_order));
            }
        });

        OrderUpdate orderUpdate = new OrderUpdate();
        orderUpdate.setStatus("completed");

        Woocommerce woocommerceApi = ((Woodmin) getApplication()).getWoocommerceApiHandler();
        Call<Order> call = woocommerceApi.updateOrderV1(String.valueOf(order.getNumber()), orderUpdate);
        call.enqueue(new Callback<Order>() {

            @Override
            public void onResponse(final Call<Order> call, final Response<Order> response) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.dismiss();
                    }
                });

                int statusCode = response.code();
                if (statusCode == 200) {
                    Order order = response.body();
                    String json = mGson.toJson(order);
                    Log.i(LOG_TAG, json);
                    /*
                    ContentValues orderValues = new ContentValues();
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_ID, order.getId());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_ORDER_NUMBER, order.getNumber());
                    if (order.getDateCreated() != null) {
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CREATED_AT, WoodminContract.getDbDateString(order.getDateCreated()));
                    }
                    if (order.getDateModified() != null) {
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_UPDATED_AT, WoodminContract.getDbDateString(order.getDateModified()));
                    }
                    if (order.getDateCompleted() != null) {
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_COMPLETED_AT, WoodminContract.getDbDateString(order.getDateCompleted()));
                    }
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_STATUS, order.getStatus());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CURRENCY, order.getCurrency());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL, order.getTotal());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_LINE_ITEMS_QUANTITY, order.getLineItems().size());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_TAX, order.getTotalTax());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_SHIPPING, order.getShippingTotal());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CART_TAX, order.getCartTax());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_TAX, order.getShippingTax());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_TOTAL_DISCOUNT, order.getDiscountTotal());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_METHODS, order.getShippingLines().get(0).getMethodTitle());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_PAYMENT_DETAILS_METHOD_ID, order.getPaymentMethod());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_PAYMENT_DETAILS_METHOD_TITLE, order.getPaymentMethodTitle());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_PAYMENT_DETAILS_PAID, "1");
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_FIRST_NAME, order.getBillingAddress().getFirstName());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_LAST_NAME, order.getBillingAddress().getLastName());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_COMPANY, order.getBillingAddress().getCompany());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_ADDRESS_1, order.getBillingAddress().getAddressOne());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_ADDRESS_2, order.getBillingAddress().getAddressTwo());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_CITY, order.getBillingAddress().getCity());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_STATE, order.getBillingAddress().getState());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_POSTCODE, order.getBillingAddress().getPostcode());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_COUNTRY, order.getBillingAddress().getCountry());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_EMAIL, order.getBillingAddress().getEmail());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_BILLING_PHONE, order.getBillingAddress().getPhone());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_ID, order.getCustomerId());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_EMAIL, order.getBillingAddress().getEmail());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_FIRST_NAME, order.getBillingAddress().getFirstName());
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_LAST_NAME, order.getBillingAddress().getLastName());
                    if (order.getBillingAddress() != null) {
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_FIRST_NAME, order.getShippingAddress().getFirstName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_LAST_NAME, order.getShippingAddress().getLastName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_COMPANY, order.getShippingAddress().getCompany());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_ADDRESS_1, order.getShippingAddress().getAddressOne());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_ADDRESS_2, order.getShippingAddress().getAddressTwo());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_CITY, order.getShippingAddress().getCity());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_STATE, order.getShippingAddress().getState());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_POSTCODE, order.getShippingAddress().getPostcode());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_SHIPPING_COUNTRY, order.getShippingAddress().getCountry());

                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_FIRST_NAME, order.getBillingAddress().getFirstName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_LAST_NAME, order.getBillingAddress().getLastName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_COMPANY, order.getBillingAddress().getCompany());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_ADDRESS_1, order.getBillingAddress().getAddressOne());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_ADDRESS_2, order.getBillingAddress().getAddressTwo());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_CITY, order.getBillingAddress().getCity());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_STATE, order.getBillingAddress().getState());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_POSTCODE, order.getBillingAddress().getPostcode());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_COUNTRY, order.getBillingAddress().getCountry());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_EMAIL, order.getBillingAddress().getEmail());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_BILLING_PHONE, order.getBillingAddress().getPhone());
                    }
                    if (order.getShippingAddress() != null) {
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_FIRST_NAME, order.getShippingAddress().getFirstName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_LAST_NAME, order.getShippingAddress().getLastName());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_COMPANY, order.getShippingAddress().getCompany());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_ADDRESS_1, order.getShippingAddress().getAddressOne());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_ADDRESS_2, order.getShippingAddress().getAddressTwo());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_CITY, order.getShippingAddress().getCity());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_STATE, order.getShippingAddress().getState());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_POSTCODE, order.getShippingAddress().getPostcode());
                        orderValues.put(WoodminContract.OrdersEntry.COLUMN_CUSTOMER_SHIPPING_COUNTRY, order.getShippingAddress().getCountry());
                    }
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_JSON, mGson.toJson(order));
                    orderValues.put(WoodminContract.OrdersEntry.COLUMN_ENABLE, 1);

                    Uri insertedOrderUri = getContentResolver().insert(WoodminContract.OrdersEntry.CONTENT_URI, orderValues);
                    long orderId = ContentUris.parseId(insertedOrderUri);
                    Log.d(LOG_TAG, "Orders successful updated ID: " + orderId);
                    */

                    Utility.setPreferredShoppingCard(getApplicationContext(), null);
                    mSaveIncomplete = false;

                    getContentResolver().notifyChange(WoodminContract.OrdersEntry.CONTENT_URI, null, false);
                    finish();

                } else {
                    Log.e(LOG_TAG, "onFailure ");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_update), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Log.e(LOG_TAG, "onFailure ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.error_update), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

}
