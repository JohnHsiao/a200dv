import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import io.WriteFile;

public class Main {
	
	
	static byte[] buffer = new byte[7];
	static int len = -1;
	static WriteFile w=new WriteFile();
	static String file="data.json";	
	static String path="/home/pi/Public/html/";	
	static String driver="/dev/ttyUSB0";
	static boolean usbSaved=false;
	static Thread t1, t2;
	
	/**
	 * 裝置連線
	 * @param portName
	 * @throws Exception
	 */
	void connect(String portName) throws Exception {
		
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("無連線裝置或連接埠佔用");
		} else {
			int timeout = 2000;
			CommPort commPort = portIdentifier.open(this.getClass().getName(), timeout);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				InputStream in = serialPort.getInputStream();				
				OutputStream out = serialPort.getOutputStream();
				//(new Thread(new SerialWriter(out))).start();
				//(new Thread(new SerialReader(in))).start();
				t1=new Thread(new SerialWriter(out));
				t2=new Thread(new SerialReader(in));
				
				t2.start();
				t1.start();
				
			} else {
				System.out.println("無連線裝置或連接埠佔用");
			}
		}
	}
	
	/**
	 * 接收資料
	 * @author shawn
	 */
	public static class SerialReader implements Runnable {

		InputStream in;

		public SerialReader(InputStream in) {
			this.in = in;
		}
		
		public void run() {			
			while(true){
				try {
					this.in.read(buffer);					
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 發送命令
	 * @author shawn
	 */
	public static class SerialWriter implements Runnable {
		OutputStream out;
		char com[]={0X01, 0X04, 0X10, 0X01, 0X00, 0X01, 0X64, 0XCA};
		public SerialWriter(OutputStream out) {
			this.out = out;
		}
		public void run() {			
			while (true) {
                try {
                    for(int i=0; i<com.length; i++){
                    	out.write(com[i]);
                    }
                	writeTmp(String.valueOf(converter(buffer)));//寫入顯示用文字檔                                      
                    Thread.sleep(1000);//發送命令間隔
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }			
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("java.library.path="+System.getProperty("java.library.path"));
		try{
			//w.mkDir(path);
			(new Main()).connect(driver);	
		}catch(Exception e){
			path="C:\\dev\\template\\NiceAdmin\\";
			driver="COM4";
			//w.mkDir(path);
			(new Main()).connect(driver);	
		}		
		w.createNewFile(path+file);		
		
	}
	
	/**
	 * 特定格式轉換
	 * 第取3、4位置的bit資料，轉換為16進位數值
	 * 再將16進位轉為10進位
	 * @param b
	 * @return
	 */
	private static int converter(byte b[]){	
		for(int i=0; i<buffer.length; i++){							
			System.out.print("["+buffer[i]+"] ");						
		}
		
		String s;
		if(buffer[0]==1){
			s=String.valueOf(toHex(b[3]))+String.valueOf(toHex(b[4]));
		}else{
			s=String.valueOf(toHex(b[2]))+String.valueOf(toHex(b[3]));
		}
		
		System.out.println();
		int c=Integer.parseInt(s, 16);
		System.out.println(c+"↑");
		
		
		return c;
		//return Integer.parseInt(s, 16);
	}	

	/**
	 * 接收格式檢查
	 * 經常接收到格式錯亂的資料陣列
	 * 確實是裝置將值多送的問題, 但是格式正確的資料陣列也有可能重複
	 * 後來發現只要資料陣列第1個位置不是01就是有問題
	 * @return
	 * @throws InterruptedException 
	 */
	/*private static boolean checkout() throws InterruptedException{	
		if(buffer[0]!=1){
			t2.sleep(1000);
			System.out.println("error format! waited 1000! restart!");
			return false;
		}
		return true;
	}*/
	
	/**
	 * byte值轉16進制
	 * @param b
	 * @return
	 */
	private static String toHex(byte b){
		return (""+"0123456789ABCDEF".charAt(0xf&b>>4)+"0123456789ABCDEF".charAt(b&0xf));
	}
	
	/**
	 * 記錄文字檔
	 */
	private static void writeTmp(String val){
		w.writeText_UTF8("{\"stat\":\""+getStat()+"\",\"num\":"+val+"}", path+file);	
		w.writeText_UTF8_Apend("{\"v\":"+val+",\"d\":"+new Date().getTime()+"},", path+"all_"+file+"");
		//System.out.println(getStat());
	}
	
	/**
	 * 偵測儲存裝置
	 * 開發平台預設為pi
	 * @return
	 */
	private static boolean getStat(){		
		
		File dir = new File("/media/pi/A200DV");
		File[] fList;
		if(dir.exists()){
			fList= dir.listFiles();
			if(fList.length>0){
				if(!usbSaved)writeUsb(false);
				return true;
			}			
		}else{
			//for windows			
			dir=new File("F:/");
			if(!dir.exists()){
				usbSaved=false;
				return false;
			}else{				
				fList= dir.listFiles();
				if(fList.length>0){
					if(!usbSaved)writeUsb(true);					
					return true;
				}
			}			
		}
		usbSaved=false;
		return false;
	}
	
	 public static void writeUsb (boolean d) {
		 	String cmd, lmd[];
	        try {            
	            Runtime rt = Runtime.getRuntime ();
	            Process proc;
	            if(d){
	            	//windows
	            	cmd=new String("copy C:\\dev\\template\\NiceAdmin\\all_data.json F:\\all_data_"+new Date().getTime()+".json /Y");				
	            	runexec("cmd /c"+cmd);//window系統下的轉換
	            
	            }else{
	            	//proc = rt.exec ("cp /home/pi/Public/html/all_data.json /media/pi/A200DV/all_data_"+new Date().getTime()+".json");
	            	//linux
					lmd=new String[]{"cp","/home/pi/Public/html/all_data.json","/media/pi/A200DV/all_data_"+new Date().getTime()+".json"};
					Runtime.getRuntime().exec(lmd);  
	            }	            
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        
	        usbSaved=true;
	    }
	 
	 
	 
	 
	 /**
		 * 執行外部方法-windows
		 */
	private static void runexec(String cmd) throws IOException, InterruptedException {
		Process process;
		try { // 使用Runtime來執行command，生成Process對象 Runtime
			 
			Runtime runtime = Runtime.getRuntime(); process = runtime.exec(cmd); //取得命令结果的输出流
			 
			InputStream is = process.getInputStream(); // 取得命令結果的輸出流
			 
			InputStreamReader isr = new InputStreamReader(is); // 用緩衝器讀行
			 
			BufferedReader br = new BufferedReader(isr); String line = null;
			 
			/*while ((line = br.readLine()) != null) { 
				System.out.println(line); 
			}*/
			is.close(); isr.close(); br.close(); 
		} catch (IOException e) { 
			 //TODO Auto-generated catch block e.printStackTrace(); }
		}
	}

}