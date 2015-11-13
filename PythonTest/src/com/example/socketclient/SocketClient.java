package com.example.socketclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.example.common.ParseSimDataType;
import com.example.datatype.SimDataType;


public class SocketClient {
	private  Socket client = null;
	private  BufferedReader in_buff = null;
	private  PrintWriter out_buff = null;
	// 默认的IP与port
	private  static String HOST_IP_ADDR = "10.50.62.34";
	private  static int HOST_PORT = 12000;
	
	public SocketClient() {

		try {
			client = new Socket(HOST_IP_ADDR, HOST_PORT);
			InputStream is = client.getInputStream();
			OutputStream os = client.getOutputStream();
			in_buff = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			out_buff = new PrintWriter(os, true); 

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	public String getHOST_IP_ADDR() {
		return HOST_IP_ADDR;
	}


	public static void setHOST_IP_ADDR(String hOST_IP_ADDR) {
		HOST_IP_ADDR = hOST_IP_ADDR;
	}


	public int getHOST_PORT() {
		return HOST_PORT;
	}


	public static void setHOST_PORT(int hOST_PORT) {
		HOST_PORT = hOST_PORT;
	}


	public void CloseSocket() {
		try {
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out_buff = null;
			in_buff= null;
			client = null;
		}
	}
	
	
	private String SendAndRecive(String message) {
		
		String result = "";
		try {
			out_buff.println(message);
			out_buff.flush(); 
            
            //accept server response 		   
            result = in_buff.readLine();           
			
		} catch (Exception e) {
			result = "";
			Log.e("TcpClient", e.toString());
			e.printStackTrace();
		}
		return result;
	}	
	
	/**
	 * 获取温度数据
	 * @return 温度数据列表
	 */
	public List<SimDataType> GetTemperature() {
		List<SimDataType> resultlist = new ArrayList<SimDataType>();
		
		// 消息类型与服务器端对应
		String message = String.format("%s#%s#%s#%s", "app1", "get", "gettemperature", "None");
		
		String result = SendAndRecive(message);
		
		try {
			resultlist = ParseSimDataType.JsonData2List(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CloseSocket();
		return resultlist;
	}
	
	
	/**
	 * 发送心跳包，维持长连接
	 * @return 推送的消息文件
	 */
	public String sendHeartBeat() {
		
		// 消息类型与服务器端对应
		String message = String.format("%s#%s#%s#%s", "app", "app", "app", "app");
		
		String result = SendAndRecive(message);

		return result;
	}
	
	
	/**
	 * 发送命令至服务器端
	 * @return 发送成功与否
	 */
	public String postMessage(String postMessageStr){
		// 消息类型与服务器端对应
        String message = String.format("%s#%s#%s#%s", "app2", "post", "postmessage", postMessageStr);
		Log.d("postMessage:message----", message);
		String result = SendAndRecive(message);
		Log.d("postMessage:result----", result);
		CloseSocket();
		return result;
	}
}
