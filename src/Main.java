import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import io.WriteFile;

public class Main {
	
	
	static byte[] buffer = new byte[7];
	static int len = -1;
	static WriteFile w=new WriteFile();
	static String file="data.json";	
	static String path="/home/pi/data";	
	static String driver="/dev/ttyUSB0";
	
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
				t1.start();
				t2.start();
				
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
					Thread.sleep(500);
					this.in.read(buffer);
					for(int i=0; i<6; i++){							
						System.out.print("["+buffer[i]+"] ");						
					}
					System.out.println();					
					Thread.sleep(500);
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
                    
                    if(checkout()){                    	
                    	System.out.println(converter(buffer));
                    	writeDoc(String.valueOf(converter(buffer)));//寫入顯示用文字檔
                    }                    
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
			w.mkDir(path);
			(new Main()).connect(driver);	
		}catch(Exception e){
			path="C:\\dev\\a200data\\";
			driver="COM4";
			w.mkDir(path);
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
		String s=String.valueOf(toHex(b[3]))+String.valueOf(toHex(b[4]));		
		return Integer.parseInt(s, 16);
	}	

	/**
	 * 接收格式檢查
	 * 經常接收到格式錯亂的資料陣列
	 * 確實是裝置將值多送的問題, 但是格式正確的資料陣列也有可能重複
	 * 後來發現只要資料陣列第1個位置不是01就是有問題
	 * @return
	 * @throws InterruptedException 
	 */
	private static boolean checkout() throws InterruptedException{	
		if(buffer[0]!=1){
			t2.sleep(1000);
			System.out.println("error format! waited 1000! restart!");
			return false;
		}
		/*for(int i=0; i<6; i++){        	
        	for(int j=0; j<6; j++){
        		if(buffer[i]==buffer[j]){
        			if(i==j){
        				continue;
        			}
        			return false;
        		}
        	}
        }*/
		return true;
	}
	
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
	private static void writeDoc(String val){
		w.writeText_UTF8("{\"subject\":\"Math\",\"num\":"+val+"}", path+file);		
	}

}