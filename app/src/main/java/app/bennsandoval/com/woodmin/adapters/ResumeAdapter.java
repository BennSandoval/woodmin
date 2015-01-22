package app.bennsandoval.com.woodmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.models.orders.Order;

public class ResumeAdapter extends ArrayAdapter<Order> {

    private Context mContext;
    private int mLayoutResourceId;
    private ArrayList<Order> mData = null;

    public ResumeAdapter(Context context, int layoutResourceId, ArrayList<Order> data) {
        super(context, layoutResourceId, data);
        this.mLayoutResourceId = layoutResourceId;
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(mLayoutResourceId, parent, false);

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

        Order order = mData.get(position);
        if(order.getStatus().toUpperCase().equals("COMPLETED")){
            holder.lyHeader.setBackgroundColor(mContext.getResources().getColor(R.color.primary));
            holder.txtStatus.setTextColor(mContext.getResources().getColor(R.color.primary));
        } else if(order.getStatus().toUpperCase().equals("CANCELLED")){
            holder.lyHeader.setBackgroundColor(mContext.getResources().getColor(R.color.red));
            holder.txtStatus.setTextColor(mContext.getResources().getColor(R.color.red));
        } else {
            holder.lyHeader.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            holder.txtStatus.setTextColor(mContext.getResources().getColor(R.color.orange));
        }
        holder.txtOrder.setText(mContext.getString(R.string.order) +" "+ order.getOrderNumber());
        holder.txtPrice.setText("$"+order.getTotal());
        holder.txtStatus.setText(order.getStatus().toUpperCase());

        if(order.getCustomer() != null && order.getCustomer().getFirstName() != null && order.getCustomer().getFirstName().length() > 0){
            holder.txtCustomer.setText(order.getCustomer().getFirstName()+ " " + order.getCustomer().getLastName());
        } else if(order.getBillingAddress() != null && order.getBillingAddress().getFirstName() != null && order.getBillingAddress().getFirstName().length() > 0){
            holder.txtCustomer.setText(order.getBillingAddress().getFirstName()+ " " + order.getBillingAddress().getLastName());
        } else {
            holder.txtCustomer.setText(mContext.getString(R.string.guest));
        }
        holder.txtItems.setText(String.valueOf(order.getItems().size()) + " " + mContext.getString(R.string.items));

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        holder.txtDate.setText(format.format(order.getCreatedAt()));

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
