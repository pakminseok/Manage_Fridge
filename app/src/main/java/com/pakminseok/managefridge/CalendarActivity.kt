package com.pakminseok.managefridge

import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pakminseok.managefridge.DTO.Fridge
import kotlinx.android.synthetic.main.activity_calendar.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import java.util.regex.Matcher
import java.util.regex.Pattern

class CalendarActivity : AppCompatActivity()
{
    lateinit var dbHandler: DBHandler
    lateinit var mCalendarView : CalendarView
    var viewat : String = SimpleDateFormat("yyyy-MM-dd").format(Date())
    lateinit var dialogCalendar : BottomSheetDialog

    private val REQUEST_CODE_SPEECH_INPUT = 100
    private val AUDIO_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        setSupportActionBar(calendar_toolbar)
        dbHandler = DBHandler(this)

        mCalendarView = findViewById(R.id.calendar)
        mCalendarView.setOnDateChangeListener(object : CalendarView.OnDateChangeListener{
            override fun onSelectedDayChange(
                view: CalendarView,
                year: Int,
                month: Int,
                dayOfMonth: Int
            ) {
                val choose_day = SimpleDateFormat("yyyy-MM-dd").parse("${year}-${month+1}-${dayOfMonth}")
                viewat = SimpleDateFormat("yyyy-MM-dd").format(choose_day)
                refreshList(viewat)
            }
        })

        val bottomNavigation : BottomNavigationView = findViewById(R.id.btm_nav)
        bottomNavigation.selectedItemId =R.id.calendar
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(
                        R.anim.anim_slide_in_left,
                        R.anim.anim_slide_out_right
                    )
                    finish()
                }
            }
            true
        }

        fab_voice.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                speak()
            } else {
                requestAudioPermission()
            }
        }
    }
    private fun requestAudioPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
            AlertDialog.Builder(this)
                .setTitle("오디오 권한 설정")
                .setMessage("음성인식 기능을 위해 권한 설정을 요청합니다.")
                .setPositiveButton("동의합니다.") { dialog, id ->
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
                }
                .setNegativeButton("거부합니다.") { dialog, id ->
                    dialog.dismiss()
                }
                .create().show()
        }else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == AUDIO_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "권한을 동의했습니다.", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "권한을 거부했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun speak()
    {
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
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

    private fun refreshList(day : String){
        dialogCalendar = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_calendar, null)
        val foodList = view.findViewById<RecyclerView>(R.id.rv_calendar)
        foodList.layoutManager = LinearLayoutManager(this)
        foodList.adapter = CalendarAdapter(this, dbHandler.getFridgeDaybyDay(day), day)

        val close = view.findViewById<ImageView>(R.id.iv_close)
        close.setOnClickListener {
            dialogCalendar.dismiss()
        }
        dialogCalendar.setCancelable(false)
        val todayDate = view.findViewById<TextView>(R.id.tv_calendar)
        val dateText = SimpleDateFormat("yyyy-MM-dd").parse(day)
        val cntOfToday = dbHandler.getFridgeCnt(day)
        if(cntOfToday > 0)
            todayDate.text = SimpleDateFormat("yyyy년MM월dd일").format(dateText) + " [ " + cntOfToday.toString() +"건 ] "
        else
            todayDate.text = SimpleDateFormat("yyyy년MM월dd일").format(dateText) + " [ 없음 ]"
        dialogCalendar.setContentView(view)
        dialogCalendar.show()
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
                dialogCalendar.dismiss()
                Toast.makeText(this, "수정했습니다.", Toast.LENGTH_SHORT).show()
                refreshList(viewat)
            }
        }
        dialog.setNegativeButton("취소") { DialogInterface, Int ->

        }
        dialog.show()

    }

    class CalendarAdapter(val activity : CalendarActivity, val list : MutableList<Fridge>, val viewat : String) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>(){
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
                        holder.itemView.setBackgroundColor(Color.rgb(169,16,22))
                        holder.fridgeRemain.text = (absRemainDay/24+1).toString()+"일 남음"
                    }
                    else
                    {
                        holder.itemView.setBackgroundColor(Color.rgb(255,255,255))
                        holder.fridgeRemain.text = (absRemainDay/24+1).toString()+"일 남음"
                    }
                }
                remainDay > 24 -> {
                    holder.itemView.setBackgroundColor(Color.rgb(36,36,36))
                    holder.fridgeRemain.text = (remainDay/24).toString()+"일 지남"
                }
                else  -> {
                    holder.itemView.setBackgroundColor(Color.rgb(169,16,22))
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
                                activity.dialogCalendar.dismiss()
                                Toast.makeText(activity, "삭제했습니다.", Toast.LENGTH_SHORT).show()
                                activity.refreshList(viewat)
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