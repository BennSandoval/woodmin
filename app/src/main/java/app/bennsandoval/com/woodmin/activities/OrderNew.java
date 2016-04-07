package app.bennsandoval.com.woodmin.activities;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
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
import java.util.List;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.models.customers.BillingAddress;
import app.bennsandoval.com.woodmin.models.orders.Item;
import app.bennsandoval.com.woodmin.models.orders.MetaItem;
import app.bennsandoval.com.woodmin.models.orders.Order;
import app.bennsandoval.com.woodmin.models.products.Product;
import app.bennsandoval.com.woodmin.models.products.Variation;
import app.bennsandoval.com.woodmin.utilities.Utility;

public class OrderNew extends AppCompatActivity {

    private final String LOG_TAG = OrderDetail.class.getSimpleName();

    private Gson mGson = new GsonBuilder().create();
    private Order mOrderSelected;
    private float mTotal = 0;

    private EditText mEmail;
    private EditText mPhone;
    private TextView mPrice;
    private EditText mCustomerFirst;
    private EditText mCustomerLast;

    private EditText mBillingAddressOne;
    private EditText mBillingAddressTwo;
    private EditText mBillingAddressCity;
    private EditText mBillingAddressCountry;
    private EditText mBillingAddressCP;
    private EditText mBillingAddressState;

    private static final String[] PRODUCT_PROJECTION = {
            WoodminContract.ProductEntry.COLUMN_ID,
            WoodminContract.ProductEntry.COLUMN_JSON,
    };
    private int COLUMN_PRODUCT_COLUMN_JSON = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_add);

        String json = Utility.getPreferredShoppingCard(getApplicationContext());
        if(json != null) {
            mOrderSelected = mGson.fromJson(json, Order.class);
        } else {
            mOrderSelected = new Order();

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

            mOrderSelected.setBillingAddress(billingAddress);

            Utility.setPreferredShoppingCard(getApplicationContext(), mGson.toJson(mOrderSelected));
        }

        mEmail = (EditText) findViewById(R.id.email);
        mPhone = (EditText) findViewById(R.id.phone);
        mPrice = (TextView) findViewById(R.id.price);
        mCustomerFirst = (EditText) findViewById(R.id.customer_first);
        mCustomerLast = (EditText) findViewById(R.id.customer_last);

        mBillingAddressOne = (EditText) findViewById(R.id.billing_address_one);
        mBillingAddressTwo = (EditText) findViewById(R.id.billing_address_two);
        mBillingAddressCity = (EditText) findViewById(R.id.billing_city);
        mBillingAddressCountry = (EditText) findViewById(R.id.billing_country);
        mBillingAddressCP = (EditText) findViewById(R.id.billing_postal_code);
        mBillingAddressState = (EditText) findViewById(R.id.billing_state);

        Button clear = (Button) findViewById(R.id.clear);
        if(mOrderSelected.getItems().size() == 0) {
            clear.setVisibility(View.GONE);
        }
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreProducts();
                mOrderSelected.getItems().clear();
                Utility.setPreferredShoppingCard(getApplicationContext(), mGson.toJson(mOrderSelected));
                finish();
            }
        });

        mPrice.setText(getString(R.string.price, '0'));

        mCustomerFirst.setText(mOrderSelected.getBillingAddress().getFirstName());
        mCustomerLast.setText(mOrderSelected.getBillingAddress().getLastName());
        mEmail.setText(mOrderSelected.getBillingAddress().getEmail());
        mPhone.setText(mOrderSelected.getBillingAddress().getPhone());

        mBillingAddressOne.setText(mOrderSelected.getBillingAddress().getAddressOne());
        mBillingAddressTwo.setText(mOrderSelected.getBillingAddress().getAddressTwo());
        mBillingAddressCP.setText(mOrderSelected.getBillingAddress().getPostcode());
        mBillingAddressState.setText(mOrderSelected.getBillingAddress().getState());
        mBillingAddressCity.setText(mOrderSelected.getBillingAddress().getCity());
        mBillingAddressCountry.setText(mOrderSelected.getBillingAddress().getCountry());

        List<String> ids = new ArrayList<>();
        List<String> parameters = new ArrayList<>();

        for(Item item:mOrderSelected.getItems()) {
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
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    json = cursor.getString(COLUMN_PRODUCT_COLUMN_JSON);
                    if(json!=null) {
                        Product product = mGson.fromJson(json, Product.class);
                        products.add(product);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        for(Item item:mOrderSelected.getItems()) {
            mTotal += Float.valueOf(item.getTotal());

            View child = getLayoutInflater().inflate(R.layout.activity_order_item, null);
            ImageView imageView = (ImageView) child.findViewById(R.id.image);
            TextView quantity = (TextView) child.findViewById(R.id.quantity);
            TextView description = (TextView) child.findViewById(R.id.description);
            TextView price = (TextView) child.findViewById(R.id.price);
            TextView sku = (TextView) child.findViewById(R.id.sku);

            quantity.setText(String.valueOf(item.getQuantity()));
            if(item.getMeta().size()>0){
                String descriptionWithMeta = item.getName();
                for(MetaItem itemMeta:item.getMeta()){
                    descriptionWithMeta += "\n" + itemMeta.getLabel() + " " + itemMeta.getValue();
                }
                description.setText(descriptionWithMeta);
            } else {
                description.setText(item.getName());
            }
            price.setText(getString(R.string.price, item.getTotal()));
            sku.setText(item.getSku());

            Product productForItem = null;
            for(Product product: products) {
                if(product.getId() == item.getProductId()) {
                    productForItem = product;
                    break;
                }
                for(Variation variation:product.getVariations()) {
                    if(variation.getId() == item.getProductId()) {
                        productForItem = product;
                        break;
                    }
                }
            }

            if(productForItem == null) {
                Log.v(LOG_TAG, "Missing product");
            } else {
                Picasso.with(getApplicationContext())
                        .load(productForItem.getFeaturedSrc())
                        .resize(50, 50)
                        .centerCrop()
                        .placeholder(R.drawable.cloud)
                        .error(R.drawable.ic_action_cancel)
                        .into(imageView);
            }
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TextView sku = (TextView) v.findViewById(R.id.sku);
                    Toast.makeText(getApplicationContext(), sku.getText().toString(), Toast.LENGTH_LONG).show();
                }
            });

            LinearLayout cardDetails = (LinearLayout)findViewById(R.id.shopping_card_details);
            cardDetails.addView(child);
        }
        mPrice.setText(getString(R.string.price, String.valueOf(mTotal)));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OrderNew.this)
                            .setTitle(getString(R.string.new_order_title))
                            .setMessage(getString(R.string.order_create_confirmation))
                            .setCancelable(false)
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

                }
            });
        }
    }

    private void restoreProducts() {

        List<String> ids = new ArrayList<>();
        List<String> parameters = new ArrayList<>();

        for(Item item:mOrderSelected.getItems()) {
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
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String json = cursor.getString(COLUMN_PRODUCT_COLUMN_JSON);
                    if(json!=null) {
                        Product product = mGson.fromJson(json, Product.class);
                        products.add(product);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        ArrayList<ContentValues> productsValues = new ArrayList<ContentValues>();

        for(Item item:mOrderSelected.getItems()) {
            for(Product product:products) {

                int stockRestore = product.getStockQuantity() + item.getQuantity();
                if(product.getId() == item.getProductId()) {
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

                for(Variation variation:product.getVariations()) {

                    //TODO, CHANGE THIS APPROACH
                    product.setSku(variation.getSku());
                    product.setPrice(variation.getPrice());

                    if(variation.getId() == item.getProductId()) {
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
    }

}
