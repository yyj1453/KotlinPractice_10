package org.techtown.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService

class ReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val replyTxt = RemoteInput.getResultsFromIntent(intent)                                     // 알림의 입력글을 받아오는 변수
            ?.getCharSequence("key_text_reply")                                                 // 고유키를 이용해 데이터 받아옴
        Log.d("yyj", "replyTxt : $replyTxt")

        val manager = context.getSystemService(
            AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(11)

    }
}