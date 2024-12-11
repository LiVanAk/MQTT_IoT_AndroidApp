package com.example.mqtt20;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtt20.databinding.ActivityMainBinding;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private String TAG = "led";
    private ActivityMainBinding binding;
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
//    private String mqttUrl = "tcp://192.168.112.126:1883";
    private String mqttUrl = "tcp://df6faaff.ala.dedicated.aliyun.emqxcloud.cn:1883";
    private String clientId = "Android";
    private String username = "android";
    private String password = "123456";
//    private String topic = "weight";
    private String topic;
    private String message;
    private int mode = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initMqttClient();

        EditText etTopic = findViewById(R.id.et_topic); // 获取 topic 输入框
        EditText etMessage = findViewById(R.id.et_message); // 获取 message 输入框

        binding.btnConnect.setOnClickListener(v -> {
            connect();
        });

        binding.btnTopic.setOnClickListener(v -> {
            subscribe(etTopic);
        });

        binding.btnMode.setOnClickListener(v -> {
            changeMode();
        });

        binding.btnMeasure.setOnClickListener(v -> {
            measure();
        });

        binding.btnCheck.setOnClickListener(v -> {
            etc();
        });

        binding.btnEcho.setOnClickListener(v -> {
            echo(etTopic, etMessage);
        });

        binding.btnDisconnect.setOnClickListener(v -> {
            disconnect();
        });

        binding.btnClear.setOnClickListener(v -> {
            binding.tvDeal.setText("操作：\n");
            binding.tvRecord.setText("记录：\n");
        });
    }

    private void initMqttClient() {
        mqttAndroidClient = new MqttAndroidClient(this, mqttUrl, clientId);
        mqttAndroidClient.setCallback(new MqttCallback());
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(false);
        mqttConnectOptions.setUserName(username); // 设置用户名
        mqttConnectOptions.setPassword(password.toCharArray()); // 设置密码
    }

    private void connect() {
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            return;
        }
        Log.i(TAG,"建立连接");
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    appendStatus("连接成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    appendStatus("连接失败");
                }
            });
        } catch (MqttException e) {
            Log.i(TAG, "connect Exceptions : " + e);
            e.printStackTrace();
        }
    }

//    EditText etTopic = findViewById(R.id.et_topic);
    private void subscribe(EditText etTopic) {
        // 获取用户输入的topic
        topic = etTopic.getText().toString().trim();  // 获取并去除空格

        if (topic.isEmpty()) {
            appendStatus("请输入订阅的topic");
            return;
        }

        if (mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
            appendStatus("请先连接MQTT服务器");
            return;
        }
        Log.i(TAG,"订阅topic");
        try {
            mqttAndroidClient.subscribe(topic, 2, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    appendStatus( "订阅\""+topic+"\"成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    appendStatus(topic + "订阅失败");
                }
            });
        } catch (MqttException e) {
            Log.i(TAG, "subscribe Exceptions : " + e);
            e.printStackTrace();
        }
    }

    private void echo(EditText etTopic, EditText etMessage) {
        message = etMessage.getText().toString().trim();
        topic = etTopic.getText().toString().trim();  // 使用订阅的 topic 发送消息
        if (mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
            appendStatus("请先连接MQTT服务器");
            return;
        }
        Log.i(TAG,"echo测试");
        MqttMessage Message = new MqttMessage();
        Message.setQos(2);
        Message.setPayload(message.getBytes());
        try {
            mqttAndroidClient.publish(topic, Message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    appendStatus("向\""+topic+"\"发送消息成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    appendStatus("向\""+topic+"\"发送消息失败");
                }
            });
        } catch (MqttException e) {
            Log.i(TAG, "echo Exceptions : " + e);
            e.printStackTrace();
        }
    }

    private void changeMode() {
        if (mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
            appendStatus("请先连接MQTT服务器");
            return;
        }
        topic = "mode";
        mode = 1-mode;
        message = String.valueOf(mode);
        Log.i(TAG,"echo测试");
        MqttMessage Message = new MqttMessage();
        Message.setQos(2);
        Message.setPayload(message.getBytes());
        try {
            mqttAndroidClient.publish(topic, Message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    if(mode==1){
                        appendStatus("模式转变为主动发送模式");
                    }
                    else{
                        appendStatus("模式转变为询问模式");
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    appendStatus("模式转变失败");
                }
            });
        } catch (MqttException e) {
            Log.i(TAG, "echo Exceptions : " + e);
            e.printStackTrace();
        }
    }

    private void measure() {
        if (mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
            appendStatus("请先连接MQTT服务器");
            return;
        }
        if (mode==1) {
            appendStatus("请先转变为询问模式");
            return;
        }
        topic = "weight";
        message = "measure";
        Log.i(TAG,"echo测试");
        MqttMessage Message = new MqttMessage();
        Message.setQos(2);
        Message.setPayload(message.getBytes());
        try {
            mqttAndroidClient.publish(topic, Message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    appendStatus("称重成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    appendStatus("称重失败");
                }
            });
        } catch (MqttException e) {
            Log.i(TAG, "echo Exceptions : " + e);
            e.printStackTrace();
        }
    }

    private void etc() {
        if (mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
            appendStatus("请先连接MQTT服务器");
            return;
        }
        if (mode==1) {
            appendStatus("请先转变为询问模式");
            return;
        }
        topic = "etc";
        message = "check";
        Log.i(TAG,"echo测试");
        MqttMessage Message = new MqttMessage();
        Message.setQos(2);
        Message.setPayload(message.getBytes());
        try {
            mqttAndroidClient.publish(topic, Message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    appendStatus("获取标签成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    appendStatus("获取标签失败");
                }
            });
        } catch (MqttException e) {
            Log.i(TAG, "echo Exceptions : " + e);
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if (mqttAndroidClient == null || !mqttAndroidClient.isConnected()) {
            return;
        }
        Log.i(TAG,"断开连接");
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            Log.i(TAG, "disconnect Exceptions : " + e);
            e.printStackTrace();
        }
    }

    public class MqttCallback implements MqttCallbackExtended {

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            if (reconnect) {
                appendStatus("重连成功");
            } else {
                appendStatus("初始化成功");
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            appendStatus("连接断开");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            appendMessage(1, new String(message.getPayload()));
            appendStatus("消息接收成功");
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            appendMessage(0, message);
        }
    }

    private void appendStatus(String msg) {
        binding.tvDeal.append(getCurrentTime() + " " + msg + "\n");
        binding.scrollDeal.post(() -> binding.scrollDeal.fullScroll(View.FOCUS_DOWN));
        binding.scrollRecord.post(() -> binding.scrollRecord.fullScroll(View.FOCUS_DOWN));
    }

    //0:发送
    //1:接收
    private void appendMessage(int type, String msg) {
        if (0 == type) {
            binding.tvRecord.append("发送消息：" + msg + "\n");
        } else {
            binding.tvRecord.append("接收消息：" + msg + "\n");
        }
        binding.scrollDeal.post(() -> binding.scrollDeal.fullScroll(View.FOCUS_DOWN));
        binding.scrollRecord.post(() -> binding.scrollRecord.fullScroll(View.FOCUS_DOWN));
    }

    private String getCurrentTime() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        return sdf.format(currentTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }
}