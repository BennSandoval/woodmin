package app.bennsandoval.com.woodmin.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.Woodmin;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.interfaces.Woocommerce;
import app.bennsandoval.com.woodmin.models.orders.Item;
import app.bennsandoval.com.woodmin.models.orders.MetaItem;
import app.bennsandoval.com.woodmin.models.orders.Note;
import app.bennsandoval.com.woodmin.models.orders.Notes;
import app.bennsandoval.com.woodmin.models.orders.Order;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetail extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = OrderDetail.class.getSimpleName();

    private int mDocumentId;
    private Order mOrderSelected;
    private Gson mGson = new GsonBuilder().create();

    private LinearLayout mHeader;
    private TextView mOrder;
    private TextView mEmail;
    private TextView mPhone;
    private LinearLayout mLyPhone;
    private LinearLayout mLyEmail;
    private TextView mPrice;
    private TextView mStatus;
    private TextView mCustomer;
    private TextView mItems;
    private TextView mDate;

    private TextView mPayment;
    private TextView mAmount;
    private TextView mTaxes;
    private TextView mTotal;

    private TextView mBilling;
    private TextView mShipping;

    private static final int ORDER_LOADER = 101;
    private static final String[] ORDER_PROJECTION = {
            WoodminContract.OrdersEntry.COLUMN_JSON,
    };
    private int COLUMN_ORDER_COLUMN_COLUMN_JSON = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDocumentId = getIntent().getIntExtra("order", -1);

        setContentView(R.layout.activity_order_detail);
        mHeader = (LinearLayout) findViewById(R.id.header);
        mOrder = (TextView) findViewById(R.id.order);
        mPrice = (TextView) findViewById(R.id.price);
        mEmail = (TextView) findViewById(R.id.email);
        mPhone = (TextView) findViewById(R.id.phone);
        mLyPhone = (LinearLayout) findViewById(R.id.call_button);
        mLyEmail = (LinearLayout) findViewById(R.id.email_button);
        mStatus = (TextView) findViewById(R.id.status);
        mCustomer = (TextView) findViewById(R.id.customer);
        mItems = (TextView) findViewById(R.id.items);
        mDate = (TextView) findViewById(R.id.date);

        mPayment = (TextView) findViewById(R.id.payment);
        mAmount = (TextView) findViewById(R.id.amount);
        mTaxes = (TextView) findViewById(R.id.taxes);
        mTotal = (TextView) findViewById(R.id.total);

        mBilling = (TextView) findViewById(R.id.billing);
        mShipping = (TextView) findViewById(R.id.shipping);

        getSupportLoaderManager().initLoader(ORDER_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Log.d(LOG_TAG, "onCreateLoader");
        String sortOrder = WoodminContract.OrdersEntry._ID + " ASC";
        CursorLoader cursorLoader = null;
        Uri ordersUri = WoodminContract.OrdersEntry.CONTENT_URI;
        switch (id) {
            case ORDER_LOADER:
                if(mDocumentId > 0){
                    String query = WoodminContract.OrdersEntry.COLUMN_ID + " == ?" ;
                    String[] parameters = new String[]{ String.valueOf(mDocumentId) };
                    cursorLoader = new CursorLoader(
                            getApplicationContext(),
                            ordersUri,
                            ORDER_PROJECTION,
                            query,
                            parameters,
                            sortOrder);
                }
                break;
            default:
                cursorLoader = null;
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        switch (cursorLoader.getId()) {
            case ORDER_LOADER:
                mOrderSelected = null;
                if (cursor.moveToFirst()) {
                    do {
                        String json = cursor.getString(COLUMN_ORDER_COLUMN_COLUMN_JSON);
                        if(json!=null){
                            Order order= mGson.fromJson(json, Order.class);
                            mOrderSelected = order;
                        }
                    } while (cursor.moveToNext());
                    fillView();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(LOG_TAG, "onLoaderReset");
        switch (cursorLoader.getId()) {
            case ORDER_LOADER:
                mOrderSelected = null;
                break;
            default:
                break;
        }
    }

    private void fillView(){

        if(mOrderSelected != null) {

            if(mOrderSelected.getStatus().toUpperCase().equals("COMPLETED")){
                mHeader.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                mStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else if(mOrderSelected.getStatus().toUpperCase().equals("CANCELLED")){
                mHeader.setBackgroundColor(getResources().getColor(R.color.red));
                mStatus.setTextColor(getResources().getColor(R.color.red));
            } else {
                mHeader.setBackgroundColor(getResources().getColor(R.color.orange));
                mStatus.setTextColor(getResources().getColor(R.color.orange));
            }
            mOrder.setText(getString(R.string.order) + " " + mOrderSelected.getOrderNumber());
            mPrice.setText("$" + mOrderSelected.getTotal());
            mStatus.setText(mOrderSelected.getStatus().toUpperCase());


            if(mOrderSelected.getCustomer() != null &&
                    mOrderSelected.getCustomer().getBillingAddress() != null &&
                    mOrderSelected.getCustomer().getBillingAddress().getEmail() != null &&
                    mOrderSelected.getCustomer().getBillingAddress().getEmail().length() > 1){

                mEmail.setText(mOrderSelected.getCustomer().getBillingAddress().getEmail());
                mLyEmail.setVisibility(View.VISIBLE);
                mLyEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",mOrderSelected.getCustomer().getBillingAddress().getEmail(), null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                        startActivity(Intent.createChooser(emailIntent, "Woodmin"));
                    }
                });

            } else {
                mLyEmail.setVisibility(View.GONE);
            }

            if(mOrderSelected.getCustomer() != null &&
                    mOrderSelected.getCustomer().getBillingAddress() != null &&
                    mOrderSelected.getCustomer().getBillingAddress().getPhone() != null &&
                    mOrderSelected.getCustomer().getBillingAddress().getPhone().length() > 1){

                mPhone.setText(mOrderSelected.getCustomer().getBillingAddress().getPhone());
                mLyPhone.setVisibility(View.VISIBLE);
                mLyPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + mOrderSelected.getCustomer().getBillingAddress().getPhone()));
                        startActivity(callIntent);
                    }
                });
            } else {
                mLyPhone.setVisibility(View.GONE);
            }

            if(mOrderSelected.getBillingAddress().getFirstName() != null && mOrderSelected.getBillingAddress().getFirstName().length() > 0){
                mCustomer.setText(mOrderSelected.getBillingAddress().getFirstName() + " " + mOrderSelected.getBillingAddress().getLastName());
            } else {
                mCustomer.setText(getString(R.string.guest));
            }
            mItems.setText(String.valueOf(mOrderSelected.getItems().size()) + " " + getString(R.string.items));

            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", Locale.getDefault());
            mDate.setText(format.format(mOrderSelected.getCreatedAt()));

            if(mOrderSelected.getPaymentDetails() != null){
                mPayment.setText(mOrderSelected.getPaymentDetails().getMethodTitle());
            }
            mAmount.setText("$"+mOrderSelected.getSubtotal());
            mTaxes.setText("$"+mOrderSelected.getTotalTax());
            mTotal.setText("$"+mOrderSelected.getTotal());

            if(mOrderSelected.getCustomer() != null &&
                    mOrderSelected.getBillingAddress() != null){

                String address = mOrderSelected.getBillingAddress().getAddressOne() + " " +
                        mOrderSelected.getBillingAddress().getAddressTwo() + " " +
                        mOrderSelected.getBillingAddress().getPostcode() + " " +
                        mOrderSelected.getBillingAddress().getCountry() + " " +
                        mOrderSelected.getBillingAddress().getState() + " " +
                        mOrderSelected.getBillingAddress().getCity();
                mBilling.setText(address);

            }

            if(mOrderSelected.getCustomer() != null &&
                    mOrderSelected.getShippingAddress() != null){

                String addres = mOrderSelected.getShippingAddress().getAddressOne() + " " +
                        mOrderSelected.getShippingAddress().getAddressTwo() + " " +
                        mOrderSelected.getShippingAddress().getPostcode() + " " +
                        mOrderSelected.getShippingAddress().getCountry() + " " +
                        mOrderSelected.getShippingAddress().getState() + " " +
                        mOrderSelected.getShippingAddress().getCity();

                mShipping.setText(addres);
            }

            LinearLayout cart = (LinearLayout)findViewById(R.id.cart);
            for(Item item:mOrderSelected.getItems()){

                View child = getLayoutInflater().inflate(R.layout.activity_order_item, null);
                TextView items = (TextView) child.findViewById(R.id.items);
                TextView description = (TextView) child.findViewById(R.id.description);
                TextView price = (TextView) child.findViewById(R.id.price);

                items.setText(String.valueOf(item.getQuantity()));
                if(item.getMeta().size()>0){
                    String descriptionWithMeta = item.getName();
                    for(MetaItem itemMeta:item.getMeta()){
                        descriptionWithMeta += "\n" + itemMeta.getLabel() + " " + itemMeta.getValue();
                    }
                    description.setText(descriptionWithMeta);
                } else {
                    description.setText(item.getName());
                }
                price.setText("$"+item.getTotal());

                cart.addView(child);
            }
            getNotes();

        } else {
            finish();
        }

    }

    private void getNotes() {

        Woocommerce woocommerceApi = ((Woodmin) getApplication()).getWoocommerceApiHandler();
        HashMap<String, String> options = new HashMap<>();
        Call<Notes> call = woocommerceApi.getOrdersNotes(options, String.valueOf(mOrderSelected.getId()));
        call.enqueue(new Callback<Notes>() {

            @Override
            public void onResponse(Call<Notes> call, Response<Notes> response) {

                int statusCode = response.code();
                if (statusCode == 200) {
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    final LinearLayout notesView = (LinearLayout) findViewById(R.id.notes);
                    for (Note note : response.body().getNotes()) {

                        final View child = getLayoutInflater().inflate(R.layout.activity_note_item, null);
                        TextView privateNote = (TextView) child.findViewById(R.id.private_note);
                        TextView noteText = (TextView) child.findViewById(R.id.note_text);
                        TextView noteDate = (TextView) child.findViewById(R.id.note_date);

                        if (note.isCustomerNote()) {
                            privateNote.setText(getString(R.string.public_note));
                        } else {
                            privateNote.setText(getString(R.string.private_note));
                        }

                        noteText.setText(note.getNote());
                        noteDate.setText(format.format(note.getCreatedAt()));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notesView.addView(child);
                            }
                        });

                    }
                }
            }

            @Override
            public void onFailure(Call<Notes> call, Throwable throwable) {
                Log.e(LOG_TAG, "onFailure ");
            }

        });
    }
}
