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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.actions.SearchIntents;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.activities.MainActivity;
import app.bennsandoval.com.woodmin.adapters.CustomerAdapter;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.models.customers.Customer;

public class CustomersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    private final String LOG_TAG = CustomersFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";
    private Gson mGson = new GsonBuilder().create();
    private ArrayList<Customer> mCustomers = new ArrayList();
    private CustomerAdapter mAdapter;

    private static final int CUSTOMER_LOADER = 300;
    private static final String[] CUSTOMER_PROJECTION = {
            WoodminContract.CostumerEntry.COLUMN_JSON,
    };
    private int COLUMN_CUSTOMER_COLUMN_JSON = 0;

    private SearchView mSearchView;
    private String mQuery;

    public static CustomersFragment newInstance(int sectionNumber) {
        CustomersFragment fragment = new CustomersFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public CustomersFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        onNewIntent(getActivity().getIntent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_customers, container, false);

        mAdapter = new CustomerAdapter(getActivity().getApplicationContext(),R.layout.fragment_customer_list_item,mCustomers);
        ListView list = (ListView)rootView.findViewById(R.id.list);
        list.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(CUSTOMER_LOADER, null, this);

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
        inflater.inflate(R.menu.customer_fragment_menu, menu);

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
            mSearchView.setQueryHint(getActivity().getString(R.string.customer_title_search));

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

        String sortOrder = WoodminContract.CostumerEntry._ID + " ASC";
        CursorLoader cursorLoader;
        Uri costumersUri = WoodminContract.CostumerEntry.CONTENT_URI;
        switch (id) {
            case CUSTOMER_LOADER:
                if(mQuery != null && mQuery.length()>0){
                    String query = WoodminContract.CostumerEntry.COLUMN_LAST_NAME + " LIKE ? OR  " +
                            WoodminContract.CostumerEntry.COLUMN_EMAIL + " LIKE ? OR  " +
                            WoodminContract.CostumerEntry.COLUMN_SHIPPING_LAST_NAME + " LIKE ? OR  " +
                            WoodminContract.CostumerEntry.COLUMN_SHIPPING_FIRST_NAME + " LIKE ? OR  " +
                            WoodminContract.CostumerEntry.COLUMN_BILLING_FIRST_NAME + " LIKE ? OR  " +
                            WoodminContract.CostumerEntry.COLUMN_BILLING_LAST_NAME + " LIKE ? OR  " +
                            WoodminContract.CostumerEntry.COLUMN_FIRST_NAME + " LIKE ?" ;
                    String[] parameters = new String[]{ "%"+mQuery+"%", "%"+mQuery+"%", "%"+mQuery+"%", "%"+mQuery+"%", "%"+mQuery+"%", "%"+mQuery+"%", "%"+mQuery+"%" };
                    cursorLoader = new CursorLoader(
                            getActivity().getApplicationContext(),
                            costumersUri,
                            CUSTOMER_PROJECTION,
                            query,
                            parameters,
                            sortOrder);
                } else {
                    cursorLoader = new CursorLoader(
                            getActivity().getApplicationContext(),
                            costumersUri,
                            CUSTOMER_PROJECTION,
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
            case CUSTOMER_LOADER:
                mCustomers.clear();
                if (cursor.moveToFirst()) {
                    do {
                        String json = cursor.getString(COLUMN_CUSTOMER_COLUMN_JSON);
                        if(json!=null){
                            Customer customer= mGson.fromJson(json, Customer.class);
                            mCustomers.add(customer);
                        }
                    } while (cursor.moveToNext());
                }
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(LOG_TAG, "onLoaderReset");
        switch (cursorLoader.getId()) {
            case CUSTOMER_LOADER:
                mCustomers.clear();
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
            mQuery = mQuery.replace(getString(R.string.customer_voice_search)+" ","");
        }
    }

    private void doSearch() {
        getActivity().getSupportLoaderManager().restartLoader(CUSTOMER_LOADER, null, this);
        getActivity().getSupportLoaderManager().getLoader(CUSTOMER_LOADER).forceLoad();
    }

}
