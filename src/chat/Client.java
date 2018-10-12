package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * 聊天室客户端
 * @author 庞赞林
 *
 */
public class Client {
	/*
	 * java.net.Socket
	 * 套接字
	 * Socket封装了TCP通讯协议，使用它可以基于TCP协议与远端计算机通讯
	 */
	private Socket socket;
	/*
	 * 客户端构造方法来初始化客户端
	 */
	public Client() throws Exception {
		try {
			/*
			 * 实例化Socket时，构造方法要求传入两个参数：（相当于电话）
			 * 1：String，指定服务端的IP地址
			 * 2：int，指定服务端打开的服务端口号
			 * 通过IP地址可以找到服务端所在计算机，通过端口号可以找到服务器上运行的服务端应用程序
			 */
			System.out.println("正在连接服务端...");
			socket=new Socket("localhost",8088);
			System.out.println("连接服务端成功!");
		} catch (Exception e) {
			//记录日志
			throw e;
		}
	}
	/*
	 * 客户端启动
	 */
	public void start() {
		try {
			Scanner scan=new Scanner(System.in);
			/*
			 * Socket提供了方法：
			 * OutputStream getOutputStream()
			 * 该方法可以获取一个输出流，通过该输出流写出的数据会发送给远端，这里的远端就是服务端
			 */
			OutputStream out=socket.getOutputStream();			
			OutputStreamWriter osw=new OutputStreamWriter(out,"utf-8");			
			PrintWriter pw=new PrintWriter(osw,true);
			
			//启动用于接受服务端发送过来的消息的线程
			ServerHandler handler=new ServerHandler();
			Thread t=new Thread(handler);
			t.start();
			
			String message=null;
			long lastSend=System.currentTimeMillis();
			while(true) {
				message=scan.nextLine();
				if(System.currentTimeMillis()-lastSend<1000) {
					System.out.println("说话太快了，稍等一下...");
				}else {
					pw.println(message);
					lastSend=System.currentTimeMillis();
				}
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		try {
			//实例化客户端
			Client client=new Client();
			//启动客户端
			client.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("客户端启动失败!");
		}
	}
	
	/*
	 * 该线程用来接受服务端发送过来的每条消息
	 */
	private class ServerHandler implements Runnable{
		public void run() {
			try {
				InputStream in=socket.getInputStream();
				InputStreamReader isr=new InputStreamReader(in);
				BufferedReader br=new BufferedReader(isr);
				
				String message=null;
				while((message=br.readLine())!=null) {
					System.out.println(message);
				}
			} catch (IOException e) {			
				e.printStackTrace();
			}			
		}
	}
}















