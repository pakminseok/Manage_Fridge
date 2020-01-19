package com.pakminseok.managefridge

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pakminseok.managefridge.DTO.Fridge
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception
import java.util.regex.Matcher
import java.util.regex.Pattern

class DashboardActivity : AppCompatActivity() {

    private lateinit var dbHandler: DBHandler
    private lateinit var permissionHandler : PermissionHandler

    private var isOpen = false
    private val REQUEST_CODE_SPEECH_INPUT = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setSupportActionBar(dashboard_toolbar)

        dbHandler = DBHandler(this)
        permissionHandler = PermissionHandler(this, this)

        rv_dashboard.layoutManager = LinearLayoutManager(this)

        val fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        val fabRClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        val fabRCounterClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_counter_clockwise)

        fab_dashboard.setOnClickListener {
            if(isOpen)
            {
                fab_keyboard_type.startAnimation(fabClose)
                fab_voice_type.startAnimation(fabClose)
                fab_dashboard.startAnimation(fabRClockwise)

                fab_keyboard_type.isEnabled=false
                fab_voice_type.isEnabled=false
                isOpen = false
            }

            else {
                fab_keyboard_type.startAnimation(fabOpen)
                fab_voice_type.startAnimation(fabOpen)
                fab_dashboard.startAnimation(fabRCounterClockwise)

                fab_keyboard_type.isClickable = true
                fab_voice_type.isClickable = true

                isOpen = true
            }

            fab_voice_type.setOnClickListener {
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                    speak()
                } else {
                    permissionHandler.requestAudioPermission()
                }
                fab_keyboard_type.startAnimation(fabClose)
                fab_voice_type.startAnimation(fabClose)
                fab_dashboard.startAnimation(fabRClockwise)

                fab_keyboard_type.isClickable = false
                fab_voice_type.isClickable = false

                isOpen = false
            }

            fab_keyboard_type.setOnClickListener {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("냉장고에 넣기")
                val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
                val foodName = view.findViewById<EditText>(R.id.ev_fridge)
                val expirationAt = view.findViewById<TextView>(R.id.tv_expiration_at)

                dialog.setView(view)

                expirationAt.setOnClickListener {
                    val c = Calendar.getInstance()
                    val year = c.get(Calendar.YEAR)
                    val month = c.get(Calendar.MONTH)
                    val day = c.get(Calendar.DAY_OF_MONTH)

                    val dateListener = object : DatePickerDialog.OnDateSetListener {
                        override fun onDateSet(
                            view: DatePicker?,
                            year: Int,
                            month: Int,
                            dayOfMonth: Int
                        ) {
                            val expAt =
                                SimpleDateFormat("yyyy-MM-dd").parse("${year}-${month + 1}-${dayOfMonth}")
                            expirationAt.setText(SimpleDateFormat("yyyy-MM-dd").format(expAt).toString())
                        }
                    }
                    val builder = DatePickerDialog(this, dateListener, year, month, day)
                    builder.show()
                }

                dialog.setPositiveButton("추가") { DialogInterface, Int ->
                    if (foodName.text.isNotEmpty() && expirationAt.text.isNotEmpty()) {
                        val food = Fridge()
                        food.itemName = foodName.text.toString()
                        food.expirationAt = expirationAt.text.toString()
                        dbHandler.addFridge(food)
                        refreshList()
                        Toast.makeText(this, "식품을 추가했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "식품명과 유통기한을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.setNegativeButton("취소") { DialogInterface, Int ->

                }
                dialog.show()
                fab_keyboard_type.startAnimation(fabClose)
                fab_voice_type.startAnimation(fabClose)
                fab_dashboard.startAnimation(fabRClockwise)

                fab_keyboard_type.isClickable = false
                fab_voice_type.isClickable = false

                isOpen = false
            }
        }

        val bottomNavigation : BottomNavigationView = findViewById(R.id.btm_nav)
        bottomNavigation.selectedItemId =R.id.home
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    finish()
                }
            }
            true
        }
    }

    private fun speak()
    {
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "냉장고에 넣을 식품과 유통기한을 말해주세요")

        try {
            startActivityForResult(mIntent, REQUEST_CODE_SPEECH_INPUT)
        }
        catch (e : Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun addFridgeVoice(foodName : String, expirationAt : String)
    {
        val food = Fridge()
        food.itemName = foodName
        food.expirationAt = expirationAt
        dbHandler.addFridge(food)
        Toast.makeText(this, "냉장고에 식품을 넣었습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_SPEECH_INPUT -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                    var pattern : Pattern = Pattern.compile("(19|20)\\d{2}년 ([1-9]|1[0-2])월 ([1-9]|[1-2][0-9]|3[0-1])일")
                    var matcher : Matcher = pattern.matcher(result[0])

                    if(matcher.find()) {
                        when (matcher.start()) {
                            0 -> {
                                Toast.makeText(this, "식품명을 말해주세요", Toast.LENGTH_SHORT).show()
                                speak()
                                return
                            }
                            else -> {
                                val foodName : String = result[0].substring(0, matcher.start() - 1)

                                val getExpiratoinAt = SimpleDateFormat("yyyy년 MM월 dd일").parse(
                                    result[0].substring(
                                        matcher.start(),
                                        matcher.end()
                                    )
                                )
                                val expirationAt : String = SimpleDateFormat("yyyy-MM-dd").format(getExpiratoinAt).toString()

                                addFridgeVoice(foodName, expirationAt)
                                return
                            }
                        }
                    }

                    pattern = Pattern.compile("\\d{2}년 ([1-9]|1[0-2])월 ([1-9]|[1-2][0-9]|3[0-1])일")
                    matcher = pattern.matcher(result[0])
                    if(matcher.find()) {
                        when (matcher.start()) {
                            0 -> {
                                Toast.makeText(this, "식품명을 말해주세요", Toast.LENGTH_SHORT).show()
                                speak()
                                return
                            }
                            else -> {
                                val foodName : String = result[0].substring(0, matcher.start() - 1)

                                val getExpiratoinAt = SimpleDateFormat("yy년 MM월 dd일").parse(
                                    result[0].substring(
                                        matcher.start(),
                                        matcher.end()
                                    )
                                )
                                val expirationAt : String = SimpleDateFormat("yyyy-MM-dd").format(getExpiratoinAt).toString()

                                addFridgeVoice(foodName, expirationAt)
                                return
                            }
                        }
                    }

                    pattern = Pattern.compile("([1-9]|1[0-2])월 ([1-9]|[1-2][0-9]|3[0-1])일")
                    matcher = pattern.matcher(result[0])
                    if(matcher.find()) {
                        when (matcher.start()) {
                            0 -> {
                                Toast.makeText(this, "식품명을 말해주세요", Toast.LENGTH_SHORT).show()
                                speak()
                                return
                            }
                            else -> {
                                val foodName : String = result[0].substring(0, matcher.start() - 1)

                                val getExpiratoinAt = SimpleDateFormat("MM월 dd일").parse(
                                    result[0].substring(
                                        matcher.start(),
                                        matcher.end()
                                    )
                                )

                                val year: String =
                                    (Calendar.getInstance().get(Calendar.YEAR)).toString()
                                val expirationAt : String = SimpleDateFormat("$year-MM-dd").format(getExpiratoinAt).toString()

                                addFridgeVoice(foodName, expirationAt)
                                return
                            }
                        }
                    }

                    Toast.makeText(this, "유통기한을 말해주세요", Toast.LENGTH_SHORT).show()
                    speak()
                }
            }
        }
    }


    fun updateFridge(fridge : Fridge){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("수정하기")
        val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
        val foodName = view.findViewById<EditText>(R.id.ev_fridge)
        val expirationAt = view.findViewById<TextView>(R.id.tv_expiration_at)
        foodName.setText(fridge.itemName)
        expirationAt.setText(fridge.expirationAt)
        dialog.setView(view)

        expirationAt.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dateListener = object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view : DatePicker?, year : Int, month : Int, dayOfMonth : Int) {
                    val expAt = SimpleDateFormat("yyyy-MM-dd").parse("${year}-${month+1}-${dayOfMonth}")
                    expirationAt.setText(SimpleDateFormat("yyyy-MM-dd").format(expAt).toString())
                }
            }
            val builder = DatePickerDialog(this, dateListener, year, month, day)
            builder.show()
        }

        dialog.setPositiveButton("수정완료") { DialogInterface, Int ->
            if (foodName.text.isNotEmpty() && expirationAt.text.isNotEmpty()) {
                fridge.itemName = foodName.text.toString()
                fridge.expirationAt = expirationAt.text.toString()
                dbHandler.updateFridge(fridge)
                Toast.makeText(this, "수정했습니다.", Toast.LENGTH_SHORT).show()
                refreshList()
            }
        }
        dialog.setNegativeButton("취소") { DialogInterface, Int ->

        }
        dialog.show()

    }
    override fun onResume(){
        refreshList()
        super.onResume()
    }

    private fun refreshList(){
        rv_dashboard.adapter = DashboardAdapter(this, dbHandler.getFridge())
    }

    class DashboardAdapter(val activity : DashboardActivity, val list : MutableList<Fridge>) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.show_list_fridge, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.fridgeName.text = list[position].itemName

            val expirationDate = SimpleDateFormat("yyyy-MM-dd").parse(list[position].expirationAt)
            val expirationAt = SimpleDateFormat("yyyy년MM월dd일").format(expirationDate)

            holder.fridgeExpirationAt.text = expirationAt

            val remainDay = (Date().time-expirationDate.time) / (1000*60*60)

            when {
                remainDay < 0 -> {
                    val absRemainDay = -remainDay
                    if (absRemainDay <= 72)
                    {
                        holder.itemView.setBackgroundResource(R.drawable.item_radius_red)
                        holder.fridgeRemain.text = (absRemainDay/24+1).toString()+"일 남음"
                    }
                    else
                    {
                        holder.itemView.setBackgroundResource(R.drawable.item_radius_white)
                        holder.fridgeRemain.text = (absRemainDay/24+1).toString()+"일 남음"
                    }
                }
                remainDay > 24 -> {
                    holder.itemView.setBackgroundResource(R.drawable.item_radius_black)
                    holder.fridgeRemain.text = (remainDay/24).toString()+"일 지남"
                }
                else  -> {
                    holder.itemView.setBackgroundResource(R.drawable.item_radius_red)
                    holder.fridgeRemain.text = "오늘까지"
                }
            }

            //click menu
            holder.menu.setOnClickListener{
                val popup = PopupMenu(activity, holder.menu)
                popup.inflate(R.menu.dashboard_child)
                popup.setOnMenuItemClickListener {

                    when(it.itemId)
                    {
                        R.id.menu_edit -> {
                            activity.updateFridge(list[position])
                        }
                        R.id.menu_delete->{
                            val dialog = AlertDialog.Builder(activity)
                            dialog.setTitle("삭제하기")
                            dialog.setMessage("정말로 삭제하시겠습니까?")
                            dialog.setPositiveButton("네, 삭제할래요") { DialogInterface, Int ->
                                activity.dbHandler.deleteFridge(list[position].id)
                                Toast.makeText(activity, "삭제했습니다.", Toast.LENGTH_SHORT).show()
                                activity.refreshList()
                            }
                            dialog.setNegativeButton("아니오") { DialogInterface, Int ->

                            }
                            dialog.show()
                        }
                    }
                    true
                }
                popup.show()
            }


        }

        class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
            val fridgeName : TextView = v.findViewById(R.id.tv_item_of_fridge)
            val fridgeExpirationAt : TextView = v.findViewById(R.id.tv_expiration_fridge)
            val fridgeRemain : TextView = v.findViewById(R.id.tv_remain)

            val menu : ImageView = v.findViewById(R.id.iv_menu)
        }
    }
}
