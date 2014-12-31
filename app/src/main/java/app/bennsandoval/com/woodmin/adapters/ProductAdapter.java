package app.bennsandoval.com.woodmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.models.products.Product;

public class ProductAdapter extends ArrayAdapter<Product> {

    Context context;
    int layoutResourceId;
    ArrayList<Product> data = null;

    public ProductAdapter(Context context, int layoutResourceId, ArrayList<Product> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ItemHolder();
            holder.txtName = (TextView) row.findViewById(R.id.name);
            holder.txtPrice = (TextView) row.findViewById(R.id.price);
            holder.txtStock = (TextView) row.findViewById(R.id.stock);
            holder.txtDescription = (TextView) row.findViewById(R.id.description);

            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }

        Product product = data.get(position);
        holder.txtName.setText(product.getTitle());
        holder.txtPrice.setText(context.getString(R.string.price)+" $"+product.getPrice());
        holder.txtStock.setText(context.getString(R.string.stock)+": "+product.getStockQuantity()+" "+context.getString(R.string.units));

        String description = product.getDescription().replaceAll("\\<.*?>","");
        description = description.replaceAll("[\\t\\n\\r]"," ");
        description = description.replaceAll("&nbsp;"," ");

        holder.txtDescription.setText(description);

        return row;
    }

    public static class ItemHolder {
        private TextView txtName;
        private TextView txtPrice;
        private TextView txtStock;
        private TextView txtDescription;
    }

}
