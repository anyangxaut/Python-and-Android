package com.example.pythontest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socketclient.SocketClient;

public class MainActivity extends Activity {

	// handler中使用到的常量，用于消息标识（相当于ID）
	private static final int FAILURE = 0;
	private static final int SUCCESS = 1;
	private static final int SENDFAILURE = 2;
	private static final int SENDSUCCESS = 3;
	// layout控件声明
	private TextView recvMsg = null;
	private Button conButton = null;
	private Button sendButton = null;
	private Button cancelButton = null;
	private Button btn_gotoTemerprature = null;
	private EditText edt_message = null;
	private TextView connect_state = null;
	// Socket网络通信实例
	private SocketClient m_client;
	// 服务器端响应字符串
	private String m_test = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置layout布局文件
		setContentView(R.layout.activity_main);
		// 获取layout控件对象实例
		recvMsg = (TextView)findViewById(R.id.recv_msg);
		conButton = (Button)findViewById(R.id.con_button);
		sendButton = (Button)findViewById(R.id.ok);
		cancelButton = (Button)findViewById(R.id.cancel);
		btn_gotoTemerprature = (Button)findViewById(R.id.btn_goto_temperature);		
		connect_state = (TextView)findViewById(R.id.connect_state);		
		edt_message = (EditText)findViewById(R.id.messtr);
		// 给button按钮绑定监听器，监听点击时间-----实现socket长连接
		conButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// android中不允许主线程访问网络，因此重新创建一个线程进行网络操作
				new Thread(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub					 
				        try { 				        	
				        	// ip和port的设定
				        	String ip = Ipsetting.IPADDRESS;
				        	int port = Integer.parseInt(Ipsetting.PORT);
				        	// 判断ip和port是否合法
				        	if( !ip.equals("") && port > 1024 && port < 65536) {
				        		SocketClient.setHOST_IP_ADDR(ip);
				        		SocketClient.setHOST_PORT(port);
				        	}
				        	// 创建Socket通信对象
				        	if (m_client == null) {
				        		m_client = new SocketClient();
				        	}
				        	// 长连接---用于消息推送			        	
				        	while(true){
				        		m_test = m_client.sendHeartBeat();
				        		if (!m_test.equals("")) {
				        			Message message = myHandler.obtainMessage();
					        		message.what = MainActivity.SUCCESS; 
					        		myHandler.sendMessage(message);
				        		}
//					        	} else {
//					        		message.what = MainActivity.FAILURE; 
//					        	}				                
				        		}			        					        	

				        } catch (Exception e) { 
				            e.printStackTrace(); 
				        }  
				    
					}
					
				}).start(); 
			}
			
		});		
		// 给button按钮绑定监听器，监听点击时间-----实现以图表形式展现数据
		btn_gotoTemerprature.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(connect_state.getText().toString().equals("Connected")) {
					// 实现页面跳转
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, TemperatureActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(),
							"Please connect to the server!", Toast.LENGTH_LONG)
							.show();
				}
				//finish();
			}
		});
		// 给button按钮绑定监听器，监听点击时间------实现app向服务器发送命令
		sendButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 获取命令文本
				final String postMessageStr = edt_message.getText().toString(); 
				new Thread(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub					
						if (m_client == null) {
			        		m_client = new SocketClient();
			        	}
			        	Message message = myHandler.obtainMessage();
			        	String recvMsg = m_client.postMessage(postMessageStr);			        	
			        	if (!recvMsg.equals("")) {
			        		message.what = MainActivity.SENDSUCCESS; 
			        	} else {
			        		message.what = MainActivity.SENDFAILURE; 
			        	}
		                myHandler.sendMessage(message);
					}
					
				}).start();

			}});
		// 给button按钮绑定监听器，监听点击时间-----清空edittext
	     cancelButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				edt_message.setText("");
			}});
	}
	
	
	/**
	 * 当联网操作结束时进行消息处理
	 */
	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MainActivity.SUCCESS:{
				connect_state.setText("Connected");
				recvMsg.setText(m_test);
				break;
			}
			case MainActivity.FAILURE:{
				Toast.makeText(getApplicationContext(),
						"Failed to connect to the server!", Toast.LENGTH_LONG)
						.show();
				break;
			}
			case MainActivity.SENDSUCCESS:{
				recvMsg.setText("Command send successfully!");
				break;
			}
			case MainActivity.SENDFAILURE:{
				recvMsg.setText("Command fail to send!");
				break;
			}
			}
			super.handleMessage(msg);
		}
	};

	// 菜单选项设置
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Group ID
				int groupId = 0;
				// The order position of the item
				int menuItemOrder = Menu.NONE;

				menu.add(groupId, 0, menuItemOrder, "IP Setting").setIcon(R.drawable.icon);
				menu.add(groupId, 1, menuItemOrder, "Exit").setIcon(R.drawable.icon);
				
				return true;
	}
	// 点击菜单选项所触发的事件响应
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:{
			Ipsetting dialog = new Ipsetting(MainActivity.this, R.style.MyDialog);
			dialog.show();
			break;
		}
		case 1:{
			if (m_client != null) {
        		m_client.CloseSocket();
        		m_client = null;
        	}
			finish();
			break;
		}
		default:break;
		}
		return true;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("MainActivity:", "--------->onPause");
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("MainActivity:", "--------->onStop");
	}

	// 当退出app时，关闭socket连接
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (m_client != null) {
    		m_client.CloseSocket();
    		m_client = null;
    	}
		Log.d("MainActivity:", "--------->onDestroy");
	}

}
