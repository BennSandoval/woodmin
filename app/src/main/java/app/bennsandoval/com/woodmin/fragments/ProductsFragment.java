package app.bennsandoval.com.woodmin.fragments;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.actions.SearchIntents;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.Woodmin;
import app.bennsandoval.com.woodmin.activities.MainActivity;
import app.bennsandoval.com.woodmin.activities.OrderAddProduct;
import app.bennsandoval.com.woodmin.activities.OrderNew;
import app.bennsandoval.com.woodmin.adapters.ProductAdapter;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.models.products.Product;
import app.bennsandoval.com.woodmin.models.products.Products;
import app.bennsandoval.com.woodmin.models.products.Variation;
import app.bennsandoval.com.woodmin.sync.WoodminSyncAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    private final String LOG_TAG = ProductsFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";
    private ProductAdapter mAdapter;

    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mRecyclerView;

    private static final int PRODUCT_LOADER = 200;
    private static final String[] PRODUCT_PROJECTION = {
            WoodminContract.ProductEntry._ID,
            WoodminContract.ProductEntry.COLUMN_ID,
            WoodminContract.ProductEntry.COLUMN_JSON,
    };

    private String mQuery;
    private int mPage = 0;
    private int mSize = 50;
    private boolean mLoading = true;

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

        View.OnClickListener onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(final View view) {
                int position = mRecyclerView.getChildAdapterPosition(view);
                mAdapter.getCursor().moveToPosition(position);
                int idSelected = mAdapter.getCursor().getInt(mAdapter.getCursor().getColumnIndex(WoodminContract.ProductEntry.COLUMN_ID));

                Intent intent = new Intent(getActivity(), OrderAddProduct.class);
                intent.putExtra("product", idSelected);
                startActivity(intent);
            }
        };

        mAdapter = new ProductAdapter(getActivity().getApplicationContext(), R.layout.fragment_product_list_item, null, onClickListener);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.list_products);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(mAdapter);

        getActivity().getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!mLoading){
                    mPage = 0;
                    getPageProducts();
                    //WoodminSyncAdapter.syncImmediately(getActivity());
                } else {
                    mSwipeLayout.setRefreshing(true);
                }
            }
        });

        mSwipeLayout.setColorSchemeResources(R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
            }

            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                boolean enable = false;
                if (view != null && view.getChildCount() > 0) {
                    enable = view.getChildAt(0).getTop() == 0;
                }
                mSwipeLayout.setEnabled(enable);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        if(fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent orderIntent = new Intent(getActivity(), OrderNew.class);
                    startActivity(orderIntent);

                }
            });
        }
        getPageProducts();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) context).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //menu.clear();
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.product_fragment_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (searchView != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();
            SearchableInfo info = searchManager.getSearchableInfo(getActivity().getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            searchView.setSearchableInfo(info);
            searchView.setOnQueryTextListener(this);
            searchView.setQueryHint(getActivity().getString(R.string.product_title_search));

            if(mQuery != null && mQuery.length() > 0) {
                searchView.setQuery(mQuery, true);
                searchView.setIconifiedByDefault(false);
                searchView.performClick();
                searchView.requestFocus();
            } else {
                searchView.setIconifiedByDefault(true);
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

        String sortOrder = WoodminContract.ProductEntry.COLUMN_TITLE + " ASC";
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
                    //String query = WoodminContract.ProductEntry.COLUMN_ENABLE + " = ?" ;
                    //String[] parameters = new String[]{ String.valueOf("1") };
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
                if(mSwipeLayout != null && !mLoading){
                    mSwipeLayout.setRefreshing(false);
                }
                mAdapter.changeCursor(cursor);
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

    private void getPageProducts() {
        mLoading = true;
        if(mSwipeLayout != null) {
            mSwipeLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeLayout.setRefreshing(mLoading);
                }
            }, 2000);
        }

        Log.v(LOG_TAG,"Products Read Total:" + mAdapter.getItemCount() + " Page : " + mPage);

        HashMap<String, String> options = new HashMap<>();
        options.put("filter[limit]", String.valueOf(mSize));
        /*
        if(date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            options.put("filter[updated_at_min]", dateFormat.format(date));
        }
        */
        options.put("page", String.valueOf(mPage));
        options.put("filter[post_status]", "any");

        Call<Products> call = ((Woodmin)getActivity().getApplication()).getWoocommerceApiHandler().getProducts(options);
        call.enqueue(new Callback<Products>() {
            @Override
            public void onResponse(Call<Products> call, Response<Products> response) {

                int statusCode = response.code();
                if (statusCode == 200) {
                    final List<Product> products = response.body().getProducts();
                    Log.v(LOG_TAG,"Success Product page " + mPage + " products " + products.size());
                    new Thread(new Runnable() {
                        public void run() {
                            Gson gson = new Gson();
                            ArrayList<ContentValues> productsValues = new ArrayList<ContentValues>();
                            for (Product product : products) {

                                //getImages(product)

                                ContentValues productValues = new ContentValues();
                                productValues.put(WoodminContract.ProductEntry.COLUMN_ID, product.getId());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_TITLE, product.getTitle());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_SKU, product.getSku());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_PRICE, product.getPrice());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_STOCK, product.getStockQuantity());
                                productValues.put(WoodminContract.ProductEntry.COLUMN_JSON, gson.toJson(product));
                                productValues.put(WoodminContract.ProductEntry.COLUMN_ENABLE, 1);

                                productsValues.add(productValues);

                                for(Variation variation:product.getVariations()) {

                                    //TODO, CHANGE THIS APPROACH
                                    product.setSku(variation.getSku());
                                    product.setPrice(variation.getPrice());
                                    product.setStockQuantity(variation.getStockQuantity());

                                    ContentValues variationValues = new ContentValues();
                                    variationValues.put(WoodminContract.ProductEntry.COLUMN_ID, variation.getId());
                                    variationValues.put(WoodminContract.ProductEntry.COLUMN_TITLE, product.getTitle());
                                    variationValues.put(WoodminContract.ProductEntry.COLUMN_SKU, product.getSku());
                                    variationValues.put(WoodminContract.ProductEntry.COLUMN_PRICE, product.getPrice());
                                    variationValues.put(WoodminContract.ProductEntry.COLUMN_STOCK, product.getStockQuantity());
                                    variationValues.put(WoodminContract.ProductEntry.COLUMN_JSON, gson.toJson(product));
                                    variationValues.put(WoodminContract.ProductEntry.COLUMN_ENABLE, 1);

                                    productsValues.add(variationValues);

                                }

                            }

                            if(getContext() != null) {
                                ContentValues[] productsValuesArray = new ContentValues[productsValues.size()];
                                productsValuesArray = productsValues.toArray(productsValuesArray);
                                int ordersRowsUpdated = getContext().getContentResolver().bulkInsert(WoodminContract.ProductEntry.CONTENT_URI, productsValuesArray);
                                Log.v(LOG_TAG, "Products " + ordersRowsUpdated + " updated");
                                getContext().getContentResolver().notifyChange(WoodminContract.ProductEntry.CONTENT_URI, null, false);
                            }
                        }
                    }).start();
                    if(products.size() == mSize) {
                        if(getActivity() != null) {
                            getPageProducts();
                        }
                    } else {
                        mLoading = false;
                        if(mSwipeLayout != null){
                            mSwipeLayout.setRefreshing(mLoading);
                        }
                    }
                }
                mPage++;
            }

            @Override
            public void onFailure(Call<Products> call, Throwable t) {
                mLoading = false;
                Log.v(LOG_TAG, "onFailure " + mPage + " error " + t.getMessage());
                if(mSwipeLayout != null){
                    mSwipeLayout.setRefreshing(mLoading);
                }
            }
        });

    }

}
