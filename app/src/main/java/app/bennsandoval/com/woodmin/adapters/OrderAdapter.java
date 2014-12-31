package app.bennsandoval.com.woodmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.models.orders.Order;

public class OrderAdapter extends ArrayAdapter<Order> {

    Context context;
    int layoutResourceId;
    ArrayList<Order> data = null;

    public OrderAdapter(Context context, int layoutResourceId,ArrayList<Order> data) {
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
            holder.lyHeader = (LinearLayout) row.findViewById(R.id.header);
            holder.txtOrder = (TextView) row.findViewById(R.id.order);
            holder.txtPrice = (TextView) row.findViewById(R.id.price);
            holder.txtStatus = (TextView) row.findViewById(R.id.status);
            holder.txtCustomer = (TextView) row.findViewById(R.id.customer);
            holder.txtItems = (TextView) row.findViewById(R.id.items);
            holder.txtDate = (TextView) row.findViewById(R.id.date);

            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }

        Order order = data.get(position);
        if(order.getStatus().toUpperCase().equals("COMPLETE")){
            holder.lyHeader.setBackgroundColor(context.getResources().getColor(R.color.primary));
        } else if(order.getStatus().toUpperCase().equals("CANCELLED")){
            holder.lyHeader.setBackgroundColor(context.getResources().getColor(R.color.red));
        } else {
            holder.lyHeader.setBackgroundColor(context.getResources().getColor(R.color.orange));
        }
        holder.txtOrder.setText(context.getString(R.string.order) +" "+ order.getOrderNumber());
        holder.txtPrice.setText("$"+order.getTotal());
        holder.txtStatus.setText(order.getStatus().toUpperCase());
        if(order.getBillingAddress().getFirstName() != null && order.getBillingAddress().getFirstName().length() > 0){
            holder.txtCustomer.setText(order.getBillingAddress().getFirstName()+ " " + order.getBillingAddress().getLastName());
        } else {
            holder.txtCustomer.setText(context.getString(R.string.guest));
        }
        holder.txtItems.setText(String.valueOf(order.getItems().size())+" "+context.getString(R.string.items));
        holder.txtDate.setText(order.getCreatedAt().toString());

        return row;
    }

    public static class ItemHolder {
        private LinearLayout lyHeader;
        private TextView txtOrder;
        private TextView txtPrice;
        private TextView txtStatus;
        private TextView txtCustomer;
        private TextView txtItems;
        private TextView txtDate;
    }

}
