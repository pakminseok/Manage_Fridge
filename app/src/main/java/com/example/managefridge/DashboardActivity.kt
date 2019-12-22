package com.example.managefridge

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.managefridge.DTO.Fridge
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DashboardActivity : AppCompatActivity() {

    lateinit var dbHandler: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(dashboard_toolbar)
        dbHandler = DBHandler(this)
        rv_dashboard.layoutManager = LinearLayoutManager(this)

        fab_dashboard.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val foodName = view.findViewById<EditText>(R.id.ev_fridge)
            dialog.setView(view)
            dialog.setPositiveButton("Add") { DialogInterface, Int ->
                if (foodName.text.isNotEmpty()) {
                    val food = Fridge()
                    food.itemName = foodName.text.toString()
                    dbHandler.addFridge(food)
                    refreshList()
                }
            }
            dialog.setNegativeButton("Cancel") { DialogInterface, Int ->

            }
            dialog.show()
        }

    }

    override fun onResume(){
        refreshList()
        super.onResume()
    }

    private fun refreshList(){
        rv_dashboard.adapter = DashboardAdapter(this, dbHandler.getFridge())
    }

    class DashboardAdapter(val context : Context, val list : MutableList<Fridge>) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.show_list_fridge, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.fridgeName.text = list[position].itemName

            val dateToString = SimpleDateFormat("yyyy-MM-dd").parse((list[position].expirationAt).toString())
            val expirationAt = SimpleDateFormat("yyyy년MM월dd일").format(dateToString)
            holder.fridgeExpirationAt.text = expirationAt

            val remainDay = (Date().time-dateToString.time) / (1000*60*60*24)

            when {
                remainDay > 0 -> holder.fridgeRemain.text = remainDay.toString()+"일 남음"
                remainDay < 0 -> holder.fridgeRemain.text = remainDay.toString()+"일 지남"
                else  -> holder.fridgeRemain.text = "오늘 까지"
            }
        }

        class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
            val fridgeName : TextView = v.findViewById(R.id.tv_item_of_fridge)
            val fridgeExpirationAt : TextView = v.findViewById(R.id.tv_expiration_fridge)
            val fridgeRemain : TextView = v.findViewById(R.id.tv_remain)
        }
    }
}
