package org.techtown.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.RemoteInput                                                                // 호환성 고려
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.techtown.notification.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)                                   // 뷰들을 가져올 binding객체 생성
        setContentView(binding.root)                                                                // binding객체를 이용해 뷰들 띄우기

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()                                    // 여러개의 허락 요청을 받아옴
        ) {
            if (it.all { permission -> permission.value == true}) {                                 // 모든 요청들이 허락되어 it에 true가 담긴다면
                noti()
            } else {
                Toast.makeText(this, "permission denied...", Toast.LENGTH_SHORT).show()
            }
        }

        binding.notificationButton.setOnClickListener {                                             // 알림 받기 버튼이 눌리면
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(ContextCompat.checkSelfPermission(                                               // 이 앱이 특정 권한이 있는지 체크
                        this,
                        "android.permission.POST_NOTIFICATIONS"
                    ) == PackageManager.PERMISSION_GRANTED                                          // 권한이 있다면
                ) {
                    noti()                                                                          // noti()함수 실행
                } else {
                    permissionLauncher.launch (
                        arrayOf(
                            "android.permission.POST_NOTIFICATIONS"
                        )
                    )
                }
            } else {
                noti()
            }
        }
    }

    fun noti() {                                                                                    // 알림을 만드는것부터 실핼하는 것까지의 함수
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager                 // 시스템에서 알림을 관리할 매니저 생성

        val builder: NotificationCompat.Builder                                                     // 채널 설정을 위한 변수(builder)선언
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {                                        // 26버전(Oreo)이상일 때
            // 26버전 이상부터는 채널 생성이 필수
            val channelId = "one-channel"                                                           // 채널 아이디 생성
            val channelName = "My Channel One"                                                      // 채널 이름 설정
            val channel = NotificationChannel(                                                      // 알림을 위한 채널 생성
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT                                              // 알림의 중요도를 보통으로 설정
            ).apply {
                description = "My Channel One Description"
                setShowBadge(true)                                                                  // 알림 배지를 생성하여 화면 위에 띄움
                val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)     // 알림소리를 가져오는 uri(경로) 생성
                // RingtoneManager: 안드로이드의 알림소리재생을 위한 틀래스, getDefaultUri: 기본 알림소리 가져오기
                val audioAttributes = AudioAttributes.Builder()                                     // 오디오를 재생할 객체를 담을 변수 생성
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)                      // 이 오디오는 알림음 이라는 유형을 지정
                    .setUsage(AudioAttributes.USAGE_ALARM)                                          // 이 오디오의 사용 목적은 알림
                    .build()                                                                        // 객체 생성
                setSound(uri, audioAttributes)                                                      // 최종적으로 소리 설정
                enableVibration(true)                                                       // 진동기능 추가
            }
            manager.createNotificationChannel(channel)                                              // 채널을 NotificationManager에 등록
            builder = NotificationCompat.Builder(this, channelId)                            // 채널을 이용하여 builder생성
        } else {
            builder = NotificationCompat.Builder(this)                                       // 26버전 미만은 채널이 필요없음
        }

        builder.run {                                                                               // 알림의 기본 정보
            setSmallIcon(R.drawable.small)                                                          // 작은 아이콘 생성
            setWhen(System.currentTimeMillis())                                                     // 현재시간에 알림 발생
            setContentTitle("홍길동")                                                                // 알림 제목 설정
            setContentText("안녕하세요.")                                                             // 알림 내용 설정
            setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.big))                   // big.jpg를 가져오기위해 받아오는 비트맵 객체 생성
        }

        val KEY_TEXT_REPLY = "key_text_reply"                                                       // 사용자가 입력한 데이터를 옮기는데 사용할 고유키 생성
        var replyLabel = "답장"                                                                      // 입력칸 안내 레이블
        var remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {                    // 고유 키를 통해 답장공간 생성
            setLabel(replyLabel)                                                                    // 사용자가 답장을 입력할 공간에 레이블 생성
            build()                                                                                 // 답장 객체 생성
        }
        val replyIntent = Intent(this, ReplyReceiver::class.java)                      // 답장 데이터를 받아오는 부분
        val replyPendingIntent = PendingIntent.getBroadcast(
            this, 30, replyIntent, PendingIntent.FLAG_MUTABLE
        )

        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.send,
                "답장",                                                                         // 입력칸을 여는 작업버튼의 제목
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()
        )

        manager.notify(11, builder.build())                                                      // 만들어진 알림을 표시
    }
}