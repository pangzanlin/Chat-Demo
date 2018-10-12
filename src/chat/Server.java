package chat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 聊天室服务端
 * @author 庞赞林
 *
 */
public class Server {
	/*
	 * java.net.ServerSocket
	 * 运行在服务端的Socket
	 * ServerSocket有两个主要作用：
	 * 1：向操作系统申请端口，客户端就是通过这个端口与服务端应用程序建立连接的
	 * 2：监听服务端口，一旦客户端Socket通过端口连接，这里就会感知到并自动创建一个Socket与客户端连接
	 */
	private ServerSocket server;
	//存放所有客户端的输出流
	private List<PrintWriter> allOut;
	
	public Server() throws Exception{
		try {
			/*
			 * 实例化ServerSocket的同时，向系统申请服务端口，若端口已被占用会抛出异常
			 */
			server=new ServerSocket(8088);
			
			allOut=new ArrayList<PrintWriter>();
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void Star() {
		try {
			Scanner scan=new Scanner(System.in);
			/*
			 * ServerSocket提供了方法：
			 * Socket accept()
			 * 该方法是一个阻塞方法，调用后会一直监听端口，知道一个客户端通过该端口建立连接//阻塞方法：运行到此就卡住了，直到监听到
			 * 这时accept会返回一个Socket，通过这个Socket就可以与客户端通讯了
			 */
			while(true) {//循环接收不同客户端的连接
				System.out.println("等待与一个客户端连接...");
				Socket socket=server.accept();
				System.out.println("与一个客户端建立了连接！");
				
				//启动线程，来完成与该客户端的交互
				ClientHandler handler=new ClientHandler(socket);
				Thread t=new Thread(handler);
				t.start();
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			Server server=new Server();
			server.Star();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("服务端启动失败！");
		}
	}
	
	/*
	 * 该线程负责与指定客户端进行交互工作
	 */
	private class ClientHandler implements Runnable{
		/*
		 * 该线程就是通过这个Socket与指定客户端交互的
		 */
		private Socket socket;
		//客户端地址信息
		private String host;
		
		public ClientHandler(Socket socket) {
			this.socket=socket;
			/*
			 * 通过Socket获取远端计算机地址信息 对于服务器这边而言，远端是指客户端。
			 */
			InetAddress address=socket.getInetAddress();
			//获取ip地址的字符串形式
			host=address.getHostAddress();
		}
		
		//广播消息给所有客户端
		private void sendMessage(String message) {
			synchronized (allOut) {
				for(PrintWriter o:allOut) {
					o.println(message);
				}
			}
		}
		
		public void run() {
			PrintWriter pw=null;
			try {
				/*
				 * Socket提供的方法：
				 * InputStream getInputStream()
				 * 通过该方法获取的输入流可以读取到来自远端发送过来的数据
				 */
				InputStream in=socket.getInputStream();				
				InputStreamReader isr=new InputStreamReader(in,"utf-8");				
				BufferedReader br=new BufferedReader(isr);
				
				/*
				 * 通过Socket创建输出流，用于将消息送达给客户端
				 */
				OutputStream out=socket.getOutputStream();
				OutputStreamWriter osw=new OutputStreamWriter(out,"utf-8");
				pw=new PrintWriter(osw,true);
				//将输出流存入到共享集合中,并上锁
				synchronized (allOut) {
					allOut.add(pw);
				}
				
				//广播消息
				sendMessage(host+"上线了");
				
				String message=null;
				/*
				 * 使用br.readLine()读取客户端发送过来的一行字符串时，由于客户端所在系统不同，那么当客户端断开连接时这里执行的结果也不同
				 * 当Linux的客户端断开连接时： br.readLine方法会返回null
				 * 当Windows的客户端断开连接时： br.readLine方法会直接抛出异常
				 */
				while((message=br.readLine())!=null) {
//					System.out.println("客户端说："+message);
//					pw.println("客户端说："+message);
				//遍历allOut集合，将消息发送给所有客户端
//					synchronized (allOut) {
//						for(PrintWriter o:allOut) {
//							o.println(host+"说："+message);
//						}
//					}
					sendMessage(host+"说："+message);
				}
			} catch (Exception e) {
				
			}finally {
				//处理客户端断开连接后的操作
				//将该客户端的输出流从共享集合中删除
				synchronized (allOut) {
					allOut.remove(pw);
				}
				
				//广播该客户端下线
				sendMessage(host+"下线了,当前在线人数："+allOut.size()+"人");
				
				//将针对该客户端的socket关闭以释放资源
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}























