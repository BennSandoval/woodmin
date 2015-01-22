package app.bennsandoval.com.woodmin.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.actions.SearchIntents;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.activities.MainActivity;
import app.bennsandoval.com.woodmin.activities.OrderDetail;
import app.bennsandoval.com.woodmin.adapters.OrderAdapter;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.models.orders.Order;
import app.bennsandoval.com.woodmin.sync.WoodminSyncAdapter;

public class ResumeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    private final String LOG_TAG = ResumeFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";
    private Gson mGson = new GsonBuilder().create();
    private ArrayList<Order> mOrders = new ArrayList();
    private OrderAdapter mAdapter;

    private SwipeRefreshLayout mSwipeLayout;

    private static final int ORDER_LOADER = 100;
    private static final String[] ORDER_PROJECTION = {
            WoodminContract.OrdersEntry.COLUMN_JSON,
    };
    private int COLUMN_ORDER_COLUMN_COLUMN_JSON = 0;

    private SearchView mSearchView;
    private String mQuery;

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
        onNewIntent(getActivity().getIntent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_orders, container, false);

        mAdapter = new OrderAdapter(getActivity().getApplicationContext(),R.layout.fragment_order_list_item,mOrders);
        ListView list = (ListView)rootView.findViewById(R.id.list);
        list.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(ORDER_LOADER, null, this);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent orderIntent = new Intent(getActivity(), OrderDetail.class);
                orderIntent.putExtra("order", mOrders.get(position).getId());
                startActivity(orderIntent);
            }
        });

        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WoodminSyncAdapter.syncImmediately(getActivity());
            }
        });

        mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright,
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
        inflater.inflate(R.menu.order_fragment_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (mSearchView != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();
            SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            mSearchView.setSearchableInfo(info);
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setQueryHint(getActivity().getString(R.string.order_title_search));

            if(mQuery != null && mQuery.length() > 0) {
                mSearchView.setQuery(mQuery, true);
                mSearchView.setIconifiedByDefault(false);
                mSearchView.performClick();
                mSearchView.requestFocus();
            } else {
                mSearchView.setIconifiedByDefault(true);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");

        String sortOrder = WoodminContract.OrdersEntry.COLUMN_ORDER_NUMBER + " DESC";
        CursorLoader cursorLoader;
        Uri ordersUri = WoodminContract.OrdersEntry.CONTENT_URI;
        switch (id) {
            case ORDER_LOADER:
                if(mQuery != null && mQuery.length()>0){
                    String query = WoodminContract.OrdersEntry.COLUMN_ORDER_NUMBER + " LIKE ? OR  " +
                            WoodminContract.OrdersEntry.COLUMN_CUSTOMER_FIRST_NAME + " LIKE ? OR  " +
                            WoodminContract.OrdersEntry.COLUMN_CUSTOMER_LAST_NAME + " LIKE ? OR  " +
                            WoodminContract.OrdersEntry.COLUMN_BILLING_FIRST_NAME + " LIKE ? OR  " +
                            WoodminContract.OrdersEntry.COLUMN_BILLING_FIRST_NAME + " LIKE ? OR  " +
                            WoodminContract.OrdersEntry.COLUMN_BILLING_FIRST_NAME + " LIKE ?" ;
                    String[] parameters = new String[]{ "%"+mQuery+"%",
                            "%"+mQuery+"%",
                            "%"+mQuery+"%",
                            "%"+mQuery+"%",
                            "%"+mQuery+"%" };
                    cursorLoader = new CursorLoader(
                            getActivity().getApplicationContext(),
                            ordersUri,
                            ORDER_PROJECTION,
                            query,
                            parameters,
                            sortOrder);
                } else {
                    cursorLoader = new CursorLoader(
                            getActivity().getApplicationContext(),
                            ordersUri,
                            ORDER_PROJECTION,
                            null,
                            null,
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
                mOrders.clear();
                if (cursor.moveToFirst()) {
                    do {
                        String json = cursor.getString(COLUMN_ORDER_COLUMN_COLUMN_JSON);
                        if(json!=null){
                            Order order= mGson.fromJson(json, Order.class);
                            mOrders.add(order);
                            mAdapter.notifyDataSetChanged();
                        }
                    } while (cursor.moveToNext());
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
        switch (cursorLoader.getId()) {
            case ORDER_LOADER:
                mOrders.clear();
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mQuery = query;
        doSearch();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mQuery = newText;
        doSearch();
        return true;
    }

    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        if (action != null && (action.equals(Intent.ACTION_SEARCH) || action.equals(SearchIntents.ACTION_SEARCH))) {
            mQuery = intent.getStringExtra(SearchManager.QUERY);
            mQuery = mQuery.replace(getString(R.string.order_voice_search)+" ","");
        }
    }

    private void doSearch() {
        getActivity().getSupportLoaderManager().restartLoader(ORDER_LOADER, null, this);
        getActivity().getSupportLoaderManager().getLoader(ORDER_LOADER).forceLoad();
    }

}
