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
import app.bennsandoval.com.woodmin.adapters.ProductAdapter;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.models.products.Product;

public class ProductsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    private final String LOG_TAG = ProductsFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";
    private Gson mGson = new GsonBuilder().create();
    private ArrayList<Product> mProducts = new ArrayList();
    private ProductAdapter mAdapter;

    private static final int PRODUCT_LOADER = 200;
    private static final String[] PRODUCT_PROJECTION = {
            WoodminContract.ProductEntry.COLUMN_JSON,
    };
    private int COLUMN_PRODUCT_COLUMN_COLUMN_JSON = 0;

    private SearchView mSearchView;
    private String mQuery;

    public static ProductsFragment newInstance(int sectionNumber) {
        ProductsFragment fragment = new ProductsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ProductsFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        onNewIntent(getActivity().getIntent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_products, container, false);

        mAdapter = new ProductAdapter(getActivity().getApplicationContext(),R.layout.fragment_product_list_item,mProducts);
        ListView list = (ListView)rootView.findViewById(R.id.list);
        list.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);

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
        inflater.inflate(R.menu.product_fragment_menu, menu);

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
            mSearchView.setQueryHint(getActivity().getString(R.string.product_title_search));

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

        String sortOrder = WoodminContract.ProductEntry._ID + " ASC";
        CursorLoader cursorLoader;
        Uri productsUri = WoodminContract.ProductEntry.CONTENT_URI;
        switch (id) {
            case PRODUCT_LOADER:
                if(mQuery != null && mQuery.length()>0){
                    String query = WoodminContract.ProductEntry.COLUMN_ID + " LIKE ? OR  " + WoodminContract.ProductEntry.COLUMN_TITLE + " LIKE ? OR  " + WoodminContract.ProductEntry.COLUMN_SKU + " LIKE ?" ;
                    String[] parameters = new String[]{ "%"+mQuery+"%", "%"+mQuery+"%", "%"+mQuery+"%" };
                    cursorLoader = new CursorLoader(
                            getActivity().getApplicationContext(),
                            productsUri,
                            PRODUCT_PROJECTION,
                            query,
                            parameters,
                            sortOrder);
                } else {
                    cursorLoader = new CursorLoader(
                            getActivity().getApplicationContext(),
                            productsUri,
                            PRODUCT_PROJECTION,
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
            case PRODUCT_LOADER:
                mProducts.clear();
                if (cursor.moveToFirst()) {
                    do {
                        String json = cursor.getString(COLUMN_PRODUCT_COLUMN_COLUMN_JSON);
                        if(json!=null){
                            Product product= mGson.fromJson(json, Product.class);
                            mProducts.add(product);
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
            case PRODUCT_LOADER:
                mProducts.clear();
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
            mQuery = mQuery.replace(getString(R.string.product_voice_search)+" ","");
        }
    }

    private void doSearch() {
        getActivity().getSupportLoaderManager().restartLoader(PRODUCT_LOADER, null, this);
        getActivity().getSupportLoaderManager().getLoader(PRODUCT_LOADER).forceLoad();
    }

}
