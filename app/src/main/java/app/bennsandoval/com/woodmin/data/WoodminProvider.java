package app.bennsandoval.com.woodmin.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Mackbook on 12/23/14.
 */
public class WoodminProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WoodminDbHelper mOpenHelper;

    private static final int SHOP = 100;
    private static final int SHOP_ID = 101;

    private static final int ORDER = 200;
    private static final int ORDER_ID = 201;

    private static final int PRODUCT = 300;
    private static final int PRODUCT_ID = 301;

    private static final int CONSUMER = 400;
    private static final int CONSUMER_ID = 401;

    @Override
    public boolean onCreate() {
        mOpenHelper = new WoodminDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "shop/#"
            case SHOP_ID:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WoodminContract.ShopEntry.TABLE_NAME,
                        projection,
                        WoodminContract.ShopEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "shop"
            case SHOP: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WoodminContract.ShopEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "order/#"
            case ORDER_ID:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WoodminContract.OrdersEntry.TABLE_NAME,
                        projection,
                        WoodminContract.OrdersEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "order"
            case ORDER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WoodminContract.OrdersEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "product/#"
            case PRODUCT_ID:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WoodminContract.ProductEntry.TABLE_NAME,
                        projection,
                        WoodminContract.ProductEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "product"
            case PRODUCT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WoodminContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "consumer/#"
            case CONSUMER_ID:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WoodminContract.CostumerEntry.TABLE_NAME,
                        projection,
                        WoodminContract.CostumerEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "consumer"
            case CONSUMER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WoodminContract.CostumerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHOP:
                return WoodminContract.ShopEntry.CONTENT_TYPE;
            case SHOP_ID:
                return WoodminContract.ShopEntry.CONTENT_ITEM_TYPE;
            case ORDER:
                return WoodminContract.OrdersEntry.CONTENT_TYPE;
            case ORDER_ID:
                return WoodminContract.OrdersEntry.CONTENT_ITEM_TYPE;
            case PRODUCT:
                return WoodminContract.ProductEntry.CONTENT_TYPE;
            case PRODUCT_ID:
                return WoodminContract.ProductEntry.CONTENT_ITEM_TYPE;
            case CONSUMER:
                return WoodminContract.CostumerEntry.CONTENT_TYPE;
            case CONSUMER_ID:
                return WoodminContract.CostumerEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case SHOP: {
                db.beginTransaction();
                try {
                    long _id = db.insert(WoodminContract.ShopEntry.TABLE_NAME, null, contentValues);
                    if ( _id > 0 )
                        returnUri = WoodminContract.ShopEntry.buildShopUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case ORDER: {
                db.beginTransaction();
                try {
                    long _id = db.insert(WoodminContract.OrdersEntry.TABLE_NAME, null, contentValues);
                    if ( _id > 0 )
                        returnUri = WoodminContract.OrdersEntry.buildOrderUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case PRODUCT: {
                db.beginTransaction();
                try {
                    long _id = db.insert(WoodminContract.ProductEntry.TABLE_NAME, null, contentValues);
                    if ( _id > 0 )
                        returnUri = WoodminContract.ProductEntry.buildOrderUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case CONSUMER: {
                db.beginTransaction();
                try {
                    long _id = db.insert(WoodminContract.CostumerEntry.TABLE_NAME, null, contentValues);
                    if ( _id > 0 )
                        returnUri = WoodminContract.CostumerEntry.buildOrderUri(_id);
                    else
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null, false);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case SHOP:
                db.beginTransaction();
                try {
                    rowsDeleted = db.delete(
                            WoodminContract.ShopEntry.TABLE_NAME, selection, selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case ORDER:
                db.beginTransaction();
                try {
                    rowsDeleted = db.delete(
                            WoodminContract.OrdersEntry.TABLE_NAME, selection, selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case PRODUCT:
                db.beginTransaction();
                try {
                    rowsDeleted = db.delete(
                            WoodminContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case CONSUMER:
                db.beginTransaction();
                try {
                    rowsDeleted = db.delete(
                            WoodminContract.CostumerEntry.TABLE_NAME, selection, selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }
        return rowsDeleted;

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SHOP:
                db.beginTransaction();
                try {
                    rowsUpdated = db.update(WoodminContract.ShopEntry.TABLE_NAME, contentValues, selection,
                            selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case ORDER:
                db.beginTransaction();
                try {
                    rowsUpdated = db.update(WoodminContract.OrdersEntry.TABLE_NAME, contentValues, selection,
                            selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case PRODUCT:
                db.beginTransaction();
                try {
                    rowsUpdated = db.update(WoodminContract.ProductEntry.TABLE_NAME, contentValues, selection,
                            selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case CONSUMER:
                db.beginTransaction();
                try {
                    rowsUpdated = db.update(WoodminContract.CostumerEntry.TABLE_NAME, contentValues, selection,
                            selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null, false);
        }
        return rowsUpdated;
    }

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WoodminContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, WoodminContract.PATH_SHOP, SHOP);
        matcher.addURI(authority, WoodminContract.PATH_SHOP + "/#", SHOP_ID);

        matcher.addURI(authority, WoodminContract.PATH_ORDER, ORDER);
        matcher.addURI(authority, WoodminContract.PATH_ORDER + "/#", ORDER_ID);

        matcher.addURI(authority, WoodminContract.PATH_PRODUCT, PRODUCT);
        matcher.addURI(authority, WoodminContract.PATH_PRODUCT + "/#", PRODUCT_ID);

        matcher.addURI(authority, WoodminContract.PATH_COSTUMER, CONSUMER);
        matcher.addURI(authority, WoodminContract.PATH_COSTUMER + "/#", CONSUMER_ID);

        return matcher;
    }
}
