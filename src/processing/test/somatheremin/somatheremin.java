package processing.test.somatheremin;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*;
//import hypermedia.net.*; 


import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter;
import java.net.*;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream; 
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaRecorder;
import android.os.PowerManager;
import android.util.Log;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;


public class somatheremin extends PApplet {

/**
 * (./) udp.pde - how to use UDP library as unicast connection
 * (cc) 2006, Cousot stephane for The Atelier Hypermedia
 * (->) http://hypermedia.loeil.org/processing/
 * This is the server (android) code that receives messages from the arduino/arduino simulator
 * Create a communication between Processing (Android) <-> LibPD 
**/

//String res_message="";

String left="", right="",res_message=""; 
int distance_L=0,distance_R=0,recstart=0,datalength=0;
float x_left, x_right;
FloatList distL,distR;
String h,min,s,m,d;//date
String str="";
//flags of value changes and occurance of steps 
boolean isLeft=false,isRight=false,stepped=false,datasent=false;
PrintWriter OUTPUT;
int c=0,c_prep=0;
boolean paintdistance=false, mouse_enabled=false, button_clicked=false;
int n=0,l=0,exep=0,perror=0;
//TCP variables

//wake lock
PowerManager pm;
PowerManager.WakeLock wl;
WifiManager wm;
WifiManager.WifiLock wifilock;
DatagramSocket socket;
DatagramPacket pack;
byte[] buf;
InetSocketAddress address;
MyDatagramReceiver myDatagramReceiver = null;
String exceptionClassName="";
//Pd settings
private static final String DISTANCEL = "#distanceL";
private static final String DISTANCER = "#distanceR";//count pace per 5 sec
private static final String DATASENT="#datasent";

public void setup() {
	
	camera(width/2, height/4, (height/2.0f) / tan(PI*30.0f / 180.0f), width/2, height/2, 0, 0, 1, 0);
	frameRate(100);
	smooth();
stroke(255);
  // and wait for incomming message
//wakelock setting       
pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "WLTag");
wl.acquire();
wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
wifilock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "WLTag");
wifilock.acquire();

myDatagramReceiver = new MyDatagramReceiver();
myDatagramReceiver.start();
//TCP Code
//thread("runTcpClient");

//store Distance data
  distL=new FloatList();distR=new FloatList();
  //Date Format
    if(month()<=9){m="0"+month();}else{m=""+month();}
    if(day()<=9){d="0"+day();}else{d=""+day();}
    if(hour()<=9){h="0"+hour();}else{h=""+hour();}
    if(minute()<=9){min="0"+minute();}else{min=""+minute();}
    if(second()<=9){s="0"+second();}else{s=""+second();}
 
  //output data to txt file
  OUTPUT = createWriter("/storage/sdcard0/Hearingthehidden_dist_"+m+d+"_"+h+min+s+".txt");
  OUTPUT.println("FrameCount"+","+"Distance_Left"+","+"Distance_Right");
}

//process events
public void draw() {

double ratio=3f;
background(0);
    stroke(255);noFill();
    
  //thread("ReceiveMsg");
   //text(str,150,100);

   //text("Left: "+distance_L+"cm", 50,50);
   //text("Right: "+distance_R+"cm", 300,50);
  // text(""+c+" data received after "+c_prep+"th data.", 100,100);
  // text(n+"         "+l+"        "+perror+"      "+exep,100,150);
  // text(exceptionClassName, 50,200);
    stroke(255);
    textSize(40);
    text("Press",width/2-50,200);
    text ("To Enter", width/2-100,height/2+250);
    fill(255,255,255,200);noStroke();
    ellipse(width/2,height/2,150,150);
   
    
   mouse_enabled=true;
    
    //if button clicked:
    if(button_clicked){
    	background(0);
    	//stroke(255);
        textSize(40);
        text("(Re)Start",150,200);
        text("the Hat",150,350);
        text("to Begin",150,500);
    	mouse_enabled=false;
    
   try {
   if(frameCount%100==0){
  // background(0);
	stroke(255);noFill();
	
  
  if(c!=c_prep){paintdistance=true;c_prep=c;}
  else if(c==c_prep&&c!=0){
  paintdistance=false;
  background(0);
	//stroke(255);
  textSize(40);
  text("(Re)Start",150,200);
  text("the Hat",150,350);
  text("to Begin",150,500);
  }
   }//end if framecount%100==0

 //draw user (stay in the middle of the screen)

   if(paintdistance){
		  background(0);
	   stroke(255);noFill();
	  // text("Left: "+distance_L+"cm", 50,50);
	  // text("Right: "+distance_R+"cm", 300,50);
	  // text(""+c+" data received after "+c_prep+"th data.", 100,100);
	  // text(exceptionClassName, 50,200);
	 //  text(n+"         "+l+"        "+perror+"      "+exep,100,150);
	   fill(102, 0, 51, 200);
   noStroke();
   //user
   pushMatrix();
   translate(width/2, height/2,0);
   sphere(18);
   fill(122*random(1,1.2f),20*random(1,1.2f),71*random(1,1.2f),50*random(1,1.2f));
   noStroke();
   //sphereDetail(6);
   sphere(25);
   popMatrix();
   
   //left wall 1
  
   x_left=width/2-distance_L*(float)ratio-15;
   if(distance_L!=0){
   fill(200,200,0,150);
   noStroke();
   pushMatrix();
   translate(x_left, height/2, -5);//left distance, y, z (change over time)
   box(5,40,40);
   popMatrix();
   }
   //right wall 1
   
   x_right=width/2+distance_R*(float)ratio+15;
   if(distance_R!=0){
   fill(200,200,0,150);
   noStroke();
   pushMatrix();
   translate(x_right, height/2, -5);
   box(5, 40, 40);
   popMatrix();
   }
   //left walls 2
   if(frameCount%10==0){
   if(distL.size()==10)
   {
   	distL.remove(1);distR.remove(1);
   }
   distL.append(x_left);
   distR.append(x_right);
   }//end if frameCount%10==0
   
   //draw other walls in every frame
   if(distL.size()>=1){
	   stroke(112,112,112,70);
	   noFill();
   for(int i=distL.size()-1;i>0;i--){
	   if(distL.get(i)!=width/2-15){
       pushMatrix();
       translate(distL.get(i),height/2, -5+60*(distL.size()-i));
       box(5,40,40);
       popMatrix();
	   }
       //right walls 2
	   if(distR.get(i)!=width/2+15){
       pushMatrix();
       translate(distR.get(i), height/2, -5+60*(distR.size()-i));
       box(5,40,40);
       popMatrix();
	   }
     }//finish for loop   
   }//finish if
//text("Datasent:"+datasent, 300,150);
   }//finish if (paintdistance)
PdBase.sendFloat(DISTANCEL, distance_L);
PdBase.sendFloat(DISTANCER, distance_R);
}
catch (IllegalArgumentException e) 
{
	/*
  freqOfTone_breath=offset1;
  freqOfTone_step=2*offset2;
  genTone(freqOfTone_breath,freqOfTone_step);
  */
	//send offset data to Pd
  e.printStackTrace();
} 
catch (IllegalStateException e) 
{/*
  freqOfTone_breath=2*offset2;
  freqOfTone_step=3*offset3;
  genTone(freqOfTone_breath,freqOfTone_step);*/
  e.printStackTrace();
} 
catch (RuntimeException e) 
{/*
  freqOfTone_breath=offset1+offset2+offset3;
  freqOfTone_step=offset1;
  genTone(freqOfTone_breath,freqOfTone_step);*/
  e.printStackTrace();
} //end catch
}//end button_clicked
}//end of draw

/**
 * To perform any action on datagram reception, you need to implement this 
 * handler in your code. This method will be automatically called by the UDP 
 * object each time he receive a nonnull message.
 * By default, this method have just one argument (the received message as 
 * byte[] array), but in addition, two arguments (representing in order the 
 * sender IP address and his port) can be set like below.
 */


/*
public void runTcpClient() {
    try {
        Socket s = new Socket("172.20.10.3",12000);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        //send output msg
       // String outMsg = "TCP connecting to " + TCP_SERVER_PORT + System.getProperty("line.separator"); 
        //out.write(outMsg);
       // out.flush();
        //accept server response
        String inMsg = in.readLine() + System.getProperty("\r\n");
        Log.i("TcpClient", "received: " + inMsg);
        String[] items = inMsg.split(":");
    	distance_L=PApplet.parseInt(items[0]);
    	distance_R=PApplet.parseInt(items[1]);
    	PdBase.sendBang(DATASENT);
    	export2Txt(frameCount,distance_L,distance_R);
        //close connection
       // s.close();
    } catch (UnknownHostException e) {e.printStackTrace();} 
      catch (IOException e) {e.printStackTrace();} 
}
*/

public void export2Txt(int fno, float distance_L, float distance_R){
	  OUTPUT.println(fno+","+distance_L+","+distance_R);  // here we export the coordinates of the vector using String concatenation!
	  OUTPUT.flush();
	  //println("data has been exported");
	}

public void mousePressed()
{if(mouse_enabled){
  button_clicked=true;
//for stepcounter (API 19+)
//abs_stepcount=0;
//println(abs_stepcount);//check step count
frameCount=0;
//start_time=millis();
//ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 60);
//tg.startTone(ToneGenerator.TONE_PROP_BEEP);
//start data logging (to sdcard)
//data logging for breath period and walking pace

try{initPd();}catch(IOException e){};
}
}

public void stop() 
{ 
 PdAudio.stopAudio();
 PdBase.release();
 wl.release();
 wifilock.release();
}
public void onResume() //register sensor listener
{
  super.onResume();
 // PdAudio.startAudio(this);
}

public void onPause() 
{
  super.onPause();

}


//initialize LibPd settings
public void initPd() throws IOException {
	AudioParameters.init(this);
	Resources res = getResources();
	//int inpch = AudioParameters.suggestInputChannels();
	PdAudio.initAudio(44100, 1, 2, 8, true); 
	File patchFile = null;
	try {
		PdBase.subscribe("android");
		 //load pd patch
		InputStream in = res.openRawResource(R.raw.somatheremincopy);
		patchFile = IoUtils.extractResource(in, "somatheremincopy.pd", getCacheDir());
		PdBase.openPatch(patchFile);
		PdAudio.startAudio(this);
		recstart=1;
		PdBase.sendFloat("#rec", recstart);
		
	  // Recording is now started
	} catch (IOException e) {
		//Log.e(TAG, e.toString());
		finish();
	} finally {
		if (patchFile != null) patchFile.delete();
	}
}//end initPd


public String sketchRenderer() { return P3D; }

public class MyDatagramReceiver extends Thread {
	boolean bKeepRunning = true;

    public void run() {
        //String str;
        buf = new byte[10];
        pack = new DatagramPacket(buf, buf.length);

        try {
            socket = new DatagramSocket(12000);
            n++;
            while(bKeepRunning) {
            	l++;
            	
            	//socket.setSoTimeout(5000);
            	try{
                socket.receive(pack);
            	}catch(Exception e){perror++;}
                if (pack==null){paintdistance=false;}
                else{
                paintdistance=true;
                c++;
                str = new String(pack.getData(), 0, pack.getLength());
                String[] items = str.split(":");
            	distance_L=PApplet.parseInt(items[0]);
            	distance_R=PApplet.parseInt(items[1]);	
            	PdBase.sendBang(DATASENT);
                }
            }
            
        } catch (Throwable e) {
        	exceptionClassName = e.getClass().getName();
            e.printStackTrace();exep++;
        }
        if (socket != null) {
            socket.close();
            n--;
        }
        
    };
    public void kill() { 
        bKeepRunning = false;
    }
}//end thread

}//end class
