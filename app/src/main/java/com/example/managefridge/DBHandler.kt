package com.example.managefridge

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.managefridge.DTO.Fridge

class DBHandler(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    override fun onCreate(db: SQLiteDatabase){
        val createFridgeTable : String = " CREATE TABLE FRIDGE (" +
                "$COL_ID integer PRIMARY KEY AUTOINCREMENT," +
                "$COL_NAME varchar," +
                "$COL_CREATED_AT datetime DEFAULT CURRENT_TIMESTAMP);"
                //"$COL_EXPIRATION_AT datetime," +
                //"$COL_WARNING_AT datetime);"
        db.execSQL(createFridgeTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun addFridge(fridge: Fridge){
        val db : SQLiteDatabase = writableDatabase
        val cv = ContentValues()
        cv.put(COL_NAME, fridge.itemName)
        val result : Long = db.insert(TABLE_FRIDGE, null, cv)
    }

    fun getFridge() : MutableList<Fridge> {
        val result : MutableList<Fridge> = ArrayList()
        val db : SQLiteDatabase = readableDatabase
        val queryResult : Cursor = db.rawQuery("SELECT * FROM $TABLE_FRIDGE", null)
        if(queryResult.moveToFirst()){
            do {
                val food = Fridge()
                food.id = queryResult.getLong(queryResult.getColumnIndex(COL_ID))
                food.itemName = queryResult.getString(queryResult.getColumnIndex(COL_NAME))
                food.createdAt = queryResult.getString(queryResult.getColumnIndex(COL_CREATED_AT))
                //food.expirationAt = queryResult.getString(queryResult.getColumnIndex(COL_EXPIRATION_AT))
                //food.warningAt = queryResult.getString(queryResult.getColumnIndex(COL_WARNING_AT))

                result.add(food)
            } while (queryResult.moveToNext())
        }
        queryResult.close()
        return result
    }
}
