package services

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import datamodels.*

const val DATABASE_NAME = "MenuDB" //Offline Database

const val COL_ID = "id"

const val OFFLINE_FOOD_MENU_TABLE_NAME = "offline_food_menu"
const val COL_ITEM_ID = "item_id"
const val COL_ITEM_NAME = "item_name"
const val COL_ITEM_PRICE = "item_price"
const val COL_ITEM_DESC = "item_desc"
const val COL_ITEM_STAR = "item_star"
const val COL_ITEM_CATEGORY = "item_category"
const val COL_ITEM_IMAGE_URL = "item_image_url"

const val CART_TABLE_NAME = "current_cart"
const val CART_ITEM_ID = "item_id"
const val CART_IMAGE_URL = "image_url"
const val CART_ITEM_NAME = "item_name"
const val CART_ITEM_PRICE = "item_price"
const val CART_ITEM_SHORT_DESC = "item_short_desc"
const val CART_ITEM_STARS = "item_stars"
const val CART_ITEM_QTY = "item_qty"

const val ORDER_HISTORY_TABLE_NAME = "old_orders"
const val COL_ORDER_DATE = "order_date"
const val COL_ORDER_ID = "order_id"
const val COL_ORDER_STATUS = "order_status"
const val COL_ORDER_PAYMENT = "order_payment"
const val COL_ORDER_PRICE = "order_price"

const val CURRENT_ORDER_TABLE_NAME = "current_orders"
const val COL_CURRENT_ORDER_ID = "order_id"
const val COL_CURRENT_ORDER_TAKE_AWAY_TIME = "take_away_time"
const val COL_CURRENT_ORDER_PAYMENT_STATUS = "payment_status"
const val COL_CURRENT_ORDER_ITEM_NAMES = "item_names"
const val COL_CURRENT_ORDER_ITEM_QUANTITIES = "item_quantities"
const val COL_CURRENT_ORDER_TOTAL_ITEM_PRICE = "total_item_price"
const val COL_CURRENT_ORDER_TAX = "tax"
const val COL_CURRENT_ORDER_SUB_TOTAL = "sub_total"

const val SAVED_CARDS_TABLE_NAME = "saved_cards"
const val COL_SAVED_CARD_NUMBER = "card_number"
const val COL_SAVED_CARD_HOLDER_NAME = "card_holder_name"
const val COL_SAVED_CARD_EXPIRY_DATE = "expiry_date"

class DatabaseHandler(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {

        val createOfflineMenuTable = "CREATE TABLE $OFFLINE_FOOD_MENU_TABLE_NAME (" +
                "$COL_ITEM_ID VARCHAR(256), " +
                "$COL_ITEM_NAME VARCHAR(256), " +
                "$COL_ITEM_PRICE REAL," +
                "$COL_ITEM_DESC VARCHAR(256)," +
                "$COL_ITEM_CATEGORY VARCHAR(256)," +
                "$COL_ITEM_STAR REAL," +
                "$COL_ITEM_IMAGE_URL VARCHAR(256)" +
                ");"

        val createCartTable = "CREATE TABLE $CART_TABLE_NAME (" +
                "$CART_ITEM_ID VARCHAR(256), " +
                "$CART_ITEM_NAME VARCHAR(256), " +
                "$CART_ITEM_PRICE REAL," +
                "$CART_ITEM_SHORT_DESC VARCHAR(256)," +
                "$CART_ITEM_QTY INTEGER," +
                "$CART_ITEM_STARS REAL," +
                "$CART_IMAGE_URL VARCHAR(256)" +
                ");"

        val createOrderHistoryTable = "CREATE TABLE $ORDER_HISTORY_TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_ORDER_DATE VARCHAR(256), " +
                "$COL_ORDER_ID VARCHAR(256)," +
                "$COL_ORDER_STATUS VARCHAR(256)," +
                "$COL_ORDER_PAYMENT VARCHAR(256)," +
                "$COL_ORDER_PRICE VARCHAR(256)" +
                ");"

        val createCurrentOrdersTable = "CREATE TABLE $CURRENT_ORDER_TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_CURRENT_ORDER_ID VARCHAR(256), " +
                "$COL_CURRENT_ORDER_TAKE_AWAY_TIME VARCHAR(256), " +
                "$COL_CURRENT_ORDER_PAYMENT_STATUS VARCHAR(256), " +
                "$COL_CURRENT_ORDER_ITEM_NAMES VARCHAR(256), " +
                "$COL_CURRENT_ORDER_ITEM_QUANTITIES VARCHAR(256), " +
                "$COL_CURRENT_ORDER_TOTAL_ITEM_PRICE VARCHAR(256), " +
                "$COL_CURRENT_ORDER_TAX VARCHAR(256), " +
                "$COL_CURRENT_ORDER_SUB_TOTAL VARCHAR(256)" +
                ");"

        val createSavedCardsTable = "CREATE TABLE $SAVED_CARDS_TABLE_NAME (" +
                "$COL_SAVED_CARD_NUMBER VARCHAR(16) PRIMARY KEY, " +
                "$COL_SAVED_CARD_HOLDER_NAME VARCHAR(50), " +
                "$COL_SAVED_CARD_EXPIRY_DATE VARCHAR(5)" +
                ");"

        db?.execSQL(createOfflineMenuTable)
        db?.execSQL(createOrderHistoryTable)
        db?.execSQL(createCurrentOrdersTable)
        db?.execSQL(createSavedCardsTable)
        db?.execSQL(createCartTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    fun insertCartItem(item: CartItem) {
        val db = this.writableDatabase

        val query = "SELECT * FROM $CART_TABLE_NAME WHERE $CART_ITEM_ID='${item.itemID}';"
        val cursor = db.rawQuery(query, null);
        if (cursor.count > 0) {
            // Update qty, cart item is already added
            val cv = ContentValues()
            cv.put(CART_ITEM_QTY, item.quantity)
            db.update(CART_TABLE_NAME, cv, "$CART_ITEM_ID = ?", Array(1) { item.itemID })
            return
        }
        cursor.close();

        val cv = ContentValues()
        cv.put(CART_ITEM_ID, item.itemID)
        cv.put(CART_ITEM_NAME, item.itemName)
        cv.put(CART_ITEM_PRICE, item.itemPrice)
        cv.put(CART_ITEM_SHORT_DESC, item.itemShortDesc)
        cv.put(CART_IMAGE_URL, item.imageUrl)
        cv.put(CART_ITEM_STARS, item.itemStars)
        cv.put(CART_ITEM_QTY, item.quantity)

        val result = db.insert(CART_TABLE_NAME, null, cv)
        if (result == (-1).toLong()) {
            Toast.makeText(context, "Failed to Insert Data", Toast.LENGTH_SHORT).show()
        }
    }

    fun readCartData(): MutableList<CartItem> {
        val list: MutableList<CartItem> = ArrayList()

        val db = this.readableDatabase
        val query = "SELECT * from $CART_TABLE_NAME"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val item = CartItem()
                item.itemID = result.getString(result.getColumnIndex(CART_ITEM_ID)).toString()
                item.imageUrl =
                    result.getString(result.getColumnIndex(CART_IMAGE_URL)).toString()
                item.itemName = result.getString(result.getColumnIndex(CART_ITEM_NAME)).toString()
                item.itemPrice = result.getFloat(result.getColumnIndex(CART_ITEM_PRICE))
                item.itemShortDesc =
                    result.getString(result.getColumnIndex(CART_ITEM_SHORT_DESC)).toString()
                item.quantity = result.getInt(result.getColumnIndex(CART_ITEM_QTY))
                item.itemStars = result.getFloat(result.getColumnIndex(CART_ITEM_STARS))

                list.add(item)
            } while (result.moveToNext())
        }

        result.close()
        db.close()

        return list
    }

    fun deleteCartItem(item: CartItem) {
        try {
            val db = this.writableDatabase
            db.delete(CART_TABLE_NAME, "$CART_ITEM_ID = ?", Array(1){item.itemID})
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to Delete Cart Item\n$e", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearCartTable() {
        try {
            val db = this.writableDatabase
            db.delete(CART_TABLE_NAME, null, null)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to delete the records", Toast.LENGTH_SHORT).show()
        }
    }

    fun insertOfflineMenuData(item: MenuItem) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_ITEM_ID, item.itemID)
        cv.put(COL_ITEM_NAME, item.itemName)
        cv.put(COL_ITEM_PRICE, item.itemPrice)
        cv.put(COL_ITEM_DESC, item.itemShortDesc)
        cv.put(COL_ITEM_IMAGE_URL, item.imageUrl)
        cv.put(COL_ITEM_CATEGORY, item.itemTag)
        cv.put(COL_ITEM_STAR, item.itemStars)
        val result = db.insert(OFFLINE_FOOD_MENU_TABLE_NAME, null, cv)
        if (result == (-1).toLong()) {
            Toast.makeText(context, "Failed to Insert Data", Toast.LENGTH_SHORT).show()
        }
    }

    fun readOfflineMenuData(): MutableList<MenuItem> {
        val list: MutableList<MenuItem> = ArrayList()

        val db = this.readableDatabase
        val query = "SELECT * from $OFFLINE_FOOD_MENU_TABLE_NAME"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val item = MenuItem()
                item.itemID = result.getString(result.getColumnIndex(COL_ITEM_ID)).toString()
                item.imageUrl =
                    result.getString(result.getColumnIndex(COL_ITEM_IMAGE_URL)).toString()
                item.itemName = result.getString(result.getColumnIndex(COL_ITEM_NAME)).toString()
                item.itemPrice = result.getFloat(result.getColumnIndex(COL_ITEM_PRICE))
                item.itemShortDesc =
                    result.getString(result.getColumnIndex(COL_ITEM_DESC)).toString()
                item.itemTag = result.getString(result.getColumnIndex(COL_ITEM_CATEGORY)).toString()
                item.itemStars = result.getFloat(result.getColumnIndex(COL_ITEM_STAR))

                list.add(item)
            } while (result.moveToNext())
        }

        result.close()
        db.close()

        return list
    }

    fun clearTheOfflineMenuTable() {
        try {
            val db = this.writableDatabase
            db.delete(OFFLINE_FOOD_MENU_TABLE_NAME, null, null)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to delete the records", Toast.LENGTH_SHORT).show()
        }
    }



    fun deleteCurrentOrderRecord(orderId: String): String {
        val query = "DELETE FROM $CURRENT_ORDER_TABLE_NAME WHERE $COL_CURRENT_ORDER_ID='$orderId';"
        val db = this.writableDatabase
        db.execSQL(query)

        val updateOrderHistoryQuery =
            "UPDATE $ORDER_HISTORY_TABLE_NAME SET $COL_ORDER_STATUS='Order Cancelled' WHERE $COL_ORDER_ID='$orderId';"
        db.execSQL(updateOrderHistoryQuery)

        db.close()
        return "Order Cancelled"
    }

    fun dropCurrentOrdersTable() {
        try {
            val db = this.writableDatabase
            db.delete(CURRENT_ORDER_TABLE_NAME, null, null)
            Toast.makeText(context, "All records are deleted", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to delete the records", Toast.LENGTH_SHORT).show()
        }
    }


    fun dropOrderHistoryTable() {
        try {
            val db = this.writableDatabase
            db.delete(ORDER_HISTORY_TABLE_NAME, null, null)
            Toast.makeText(context, "All records are deleted", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to delete the records", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearSavedCards() {
        try {
            val db = this.writableDatabase
            db.delete(SAVED_CARDS_TABLE_NAME, null, null)
            Toast.makeText(context, "All cards are removed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to remove cards", Toast.LENGTH_SHORT).show()
        }
    }
}
