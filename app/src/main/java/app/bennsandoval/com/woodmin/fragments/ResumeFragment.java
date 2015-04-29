package app.bennsandoval.com.woodmin.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.activities.MainActivity;
import app.bennsandoval.com.woodmin.adapters.ResumeAdapter;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.models.DataResume;
import app.bennsandoval.com.woodmin.models.Resume;
import app.bennsandoval.com.woodmin.models.orders.Order;
import app.bennsandoval.com.woodmin.models.products.Product;
import app.bennsandoval.com.woodmin.sync.WoodminSyncAdapter;

public class ResumeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ResumeFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";
    private Gson mGson = new GsonBuilder().create();
    private ArrayList<Resume> mResume = new ArrayList();
    private ResumeAdapter mAdapter;

    private SwipeRefreshLayout mSwipeLayout;

    private static final int ORDER_LOADER = 10;
    private static final String[] ORDER_PROJECTION = {
            WoodminContract.OrdersEntry.COLUMN_JSON,
    };
    private int COLUMN_ORDER_COLUMN_COLUMN_JSON = 0;

    private static final int PRODUCT_LOADER = 20;
    private int COLUMN_PRODUCT_COLUMN_COLUMN_JSON = 0;

    public static ResumeFragment newInstance(int sectionNumber) {
        ResumeFragment fragment = new ResumeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ResumeFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_resume, container, false);

        mAdapter = new ResumeAdapter(getActivity().getApplicationContext(),R.layout.fragment_resume_list_item, mResume);
        ListView list = (ListView)rootView.findViewById(R.id.list);
        list.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(ORDER_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WoodminSyncAdapter.syncImmediately(getActivity());
            }
        });

        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //menu.clear();
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.resume_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");

        String sortOrder = WoodminContract.OrdersEntry.COLUMN_ORDER_NUMBER + " DESC";
        String sortProduct = WoodminContract.ProductEntry.COLUMN_STOCK + " ASC";
        CursorLoader cursorLoader;
        Uri ordersUri = WoodminContract.OrdersEntry.CONTENT_URI;
        Uri productsUri = WoodminContract.ProductEntry.CONTENT_URI;
        switch (id) {
            case ORDER_LOADER:
                cursorLoader = new CursorLoader(
                        getActivity().getApplicationContext(),
                        ordersUri,
                        ORDER_PROJECTION,
                        null,
                        null,
                        sortOrder);
                break;
            case PRODUCT_LOADER:
                cursorLoader = new CursorLoader(
                        getActivity().getApplicationContext(),
                        productsUri,
                        ORDER_PROJECTION,
                        null,
                        null,
                        sortProduct);
                break;
            default:
                cursorLoader = null;
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        Iterator<Resume> resumeIterator = mResume.iterator();
        switch (cursorLoader.getId()) {
            case ORDER_LOADER:
                while (resumeIterator.hasNext()) {
                    Resume resume = resumeIterator.next();
                    if(!resume.getTitle().equals("STOCK WARNINGS")){
                        resumeIterator.remove();
                    }
                }
                if (cursor.moveToFirst()) {

                    Resume resume = new Resume();
                    resume.setTitle("SALES");

                    String date = null;
                    int items = 0;
                    float total = 0;
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    do {
                        String json = cursor.getString(COLUMN_ORDER_COLUMN_COLUMN_JSON);
                        if(json!=null){
                            Order order = mGson.fromJson(json, Order.class);

                            if(date != null && !simpleDateFormat.format(order.getCreatedAt()).equals(date)){

                                DataResume resumeDay = new DataResume();
                                resumeDay.setField1(date);
                                resumeDay.setField2(items + " " + getString(R.string.items));
                                resumeDay.setField3("$"+total);
                                resume.getData().add(resumeDay);

                                items = order.getItems().size();
                                total = Float.valueOf(order.getTotal());

                            } else {
                                items += order.getItems().size();
                                total += Float.valueOf(order.getTotal());
                            }

                            date = simpleDateFormat.format(order.getCreatedAt());

                            if (resume.getData().size() == 3){
                                break;
                            }

                        }
                    } while (cursor.moveToNext());

                    mResume.add(resume);
                    mAdapter.notifyDataSetChanged();
                }
                if(mSwipeLayout != null){
                    mSwipeLayout.setRefreshing(false);
                }
                break;
            case PRODUCT_LOADER:
                while (resumeIterator.hasNext()) {
                    Resume resume = resumeIterator.next();
                    if(resume.getTitle().equals("STOCK WARNINGS")){
                        resumeIterator.remove();
                    }
                }
                if (cursor.moveToFirst()) {

                    Resume resume = new Resume();
                    resume.setTitle("STOCK WARNINGS");

                    do {
                        String json = cursor.getString(COLUMN_PRODUCT_COLUMN_COLUMN_JSON);
                        if(json!=null){
                            Product product = mGson.fromJson(json, Product.class);

                            DataResume resumeProduct = new DataResume();
                            resumeProduct.setField1(product.getTitle());
                            resumeProduct.setField2("");
                            resumeProduct.setField3(product.getStockQuantity() + " " + getString(R.string.items));
                            resume.getData().add(resumeProduct);

                            if (resume.getData().size() == 3){
                                break;
                            }

                        }
                    } while (cursor.moveToNext());

                    mResume.add(resume);
                    mAdapter.notifyDataSetChanged();
                }
                if(mSwipeLayout != null){
                    mSwipeLayout.setRefreshing(false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(LOG_TAG, "onLoaderReset");
        Iterator<Resume> resumeIterator = mResume.iterator();

        switch (cursorLoader.getId()) {
            case ORDER_LOADER:
                while (resumeIterator.hasNext()) {
                    Resume resume = resumeIterator.next();
                    if(!resume.getTitle().equals("STOCK WARNINGS")){
                        resumeIterator.remove();
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            case PRODUCT_LOADER:
                while (resumeIterator.hasNext()) {
                    Resume resume = resumeIterator.next();
                    if(resume.getTitle().equals("STOCK WARNINGS")){
                        resumeIterator.remove();
                    }
                }
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }
}
