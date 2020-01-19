package com.pakminseok.managefridge

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

class PermissionHandler(val context: Context, val activity : Activity){
    private val AUDIO_PERMISSION_CODE = 1

    fun requestAudioPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.RECORD_AUDIO)) {
            AlertDialog.Builder(context)
                .setTitle("오디오 권한 설정")
                .setMessage("음성인식 기능을 위해 권한 설정을 요청합니다.")
                .setPositiveButton("동의합니다.") { dialog, id ->
                    ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
                }
                .setNegativeButton("거부합니다.") { dialog, id ->
                    dialog.dismiss()
                }
                .create().show()
        }else {
            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == AUDIO_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context, "권한을 동의했습니다.", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, "권한을 거부했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}