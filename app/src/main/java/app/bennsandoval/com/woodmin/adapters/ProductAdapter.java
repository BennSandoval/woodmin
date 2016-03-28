package app.bennsandoval.com.woodmin.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.data.WoodminContract;
import app.bennsandoval.com.woodmin.models.products.Product;

public class ProductAdapter extends CursorRecyclerViewAdapter<ProductAdapter.ViewHolder>  {

    private Context mContext;
    private int mLayoutResourceId;
    private View.OnClickListener mOnClickListener;

    public ProductAdapter(Context context, int layoutResourceId, Cursor cursor, View.OnClickListener onClickListener){
        super(context,cursor);
        this.mContext = context;
        this.mLayoutResourceId = layoutResourceId;
        this.mOnClickListener = onClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private TextView txtPrice;
        private TextView txtStock;
        private TextView txtDescription;

        public ViewHolder(View view) {
            super(view);
            txtName = (TextView) view.findViewById(R.id.name);
            txtPrice = (TextView) view.findViewById(R.id.price);
            txtStock = (TextView) view.findViewById(R.id.stock);
            txtDescription = (TextView) view.findViewById(R.id.description);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(mLayoutResourceId, parent, false);
        itemView.setOnClickListener(mOnClickListener);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        String json = cursor.getString(cursor.getColumnIndexOrThrow(WoodminContract.ProductEntry.COLUMN_JSON));
        if(json!=null) {
            Gson gson = new GsonBuilder().create();
            Product product = gson.fromJson(json, Product.class);

            if(product.getStockQuantity() > 0){
                holder.txtName.setBackgroundColor(mContext.getResources().getColor(R.color.primary));
                holder.txtStock.setTextColor(mContext.getResources().getColor(R.color.primary));
            } else {
                holder.txtName.setBackgroundColor(mContext.getResources().getColor(R.color.red));
                holder.txtStock.setTextColor(mContext.getResources().getColor(R.color.red));
            }

            holder.txtName.setText(product.getTitle());
            if(product.getCogsCost() != null) {
                holder.txtPrice.setText(mContext.getString(R.string.price_const, product.getPrice(), product.getCogsCost()));
            } else {
                holder.txtPrice.setText(mContext.getString(R.string.price, product.getPrice()));
            }
            holder.txtStock.setText(mContext.getString(R.string.stock, product.getStockQuantity()));

            String description = product.getShortDescription().replaceAll("\\<.*?>","");
            description = description.replaceAll("[\\t\\n\\r]"," ");
            description = description.replaceAll("&nbsp;"," ");

            holder.txtDescription.setText(description);
        }
    }

}
