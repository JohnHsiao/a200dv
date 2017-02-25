import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TwoWaySerialComm
{
    public TwoWaySerialComm()
    {
        super();
    }
    
    void connect ( String portName ) throws Exception{
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(19200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
                
                (new Thread(new SerialReader(in))).start();
                (new Thread(new SerialWriter(out))).start();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    /** */
    public static class SerialReader implements Runnable{
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                while ( true )
                {
                	this.in.read(buffer);
					for(int i=0; i<5; i++){							
						System.out.print("["+buffer[i]+"] ");						
					}
					System.out.println();
					Thread.sleep(1000);
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }            
        }
    }

    /** */
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        char com[]={0X01, 0X04, 0X10, 0X01, 0X00, 0X01, 0X64, 0XCA};
        public SerialWriter (OutputStream out){
            this.out = out;
        }
        
        public void run ()
        {
        	while ( true ){
                try {
                	for(int i=0; i<com.length; i++){
                		
                    	out.write(com[i]);
                    }
                	Thread.sleep(500);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } 
                    
        }
    }
    
    public static void main ( String[] args )
    {
        try
        {
            (new TwoWaySerialComm()).connect("COM4");
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}