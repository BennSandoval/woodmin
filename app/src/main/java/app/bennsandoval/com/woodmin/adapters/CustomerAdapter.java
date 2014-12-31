package app.bennsandoval.com.woodmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import app.bennsandoval.com.woodmin.R;
import app.bennsandoval.com.woodmin.models.customers.Customer;

public class CustomerAdapter extends ArrayAdapter<Customer> {

    Context context;
    int layoutResourceId;
    ArrayList<Customer> data = null;

    public CustomerAdapter(Context context, int layoutResourceId, ArrayList<Customer> data) {
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
            holder.txtSpend = (TextView) row.findViewById(R.id.spend);
            holder.txtEmail = (TextView) row.findViewById(R.id.email);
            holder.txtPhone = (TextView) row.findViewById(R.id.phone);

            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }

        Customer customer = data.get(position);
        if(customer.getBillingAddress() != null){
            holder.txtName.setText(customer.getBillingAddress().getFirstName() + " " +customer.getBillingAddress().getLastName());
        } else {
            holder.txtName.setText(customer.getFirstName() + " " +customer.getLastName());
        }

        if(holder.txtName.getText().length() == 1){
            holder.txtName.setText(context.getString(R.string.guest));
        }

        holder.txtSpend.setText("$" + customer.getTotalSpent());

        holder.txtEmail.setText(customer.getEmail());
        if(customer.getBillingAddress() != null){
            holder.txtPhone.setText(customer.getBillingAddress().getPhone());
        }

        return row;
    }

    public static class ItemHolder {
        private TextView txtName;
        private TextView txtSpend;
        private TextView txtEmail;
        private TextView txtPhone;
    }

}
