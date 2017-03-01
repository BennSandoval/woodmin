package app.bennsandoval.com.woodmin.interfaces;

import app.bennsandoval.com.woodmin.models.v3.customers.Customer;

/**
 * Created by Mackbook on 1/5/15.
 */
public interface CustomerActions {
    void sendEmail(Customer customer);
    void makeACall(Customer customer);
}
