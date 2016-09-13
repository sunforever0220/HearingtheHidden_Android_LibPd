package processing.test.somatheremin;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*;
import hypermedia.net.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException;

import android.content.res.Resources;
import android.media.MediaRecorder;

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

String left="", right=""; 
int distance_L=0,distance_R=0,recstart=0;
float x_left, x_right;
FloatList distL,distR;
//flags of value changes and occurance of steps 
boolean isLeft=false,isRight=false,stepped=false,datasent=false;
PrintWriter OUTPUT;
UDP udp;  // define the UDP object


//Pd settings
private static final String DISTANCEL = "#distanceL";
private static final String DISTANCER = "#distanceR";//count pace per 5 sec
private static final String DATASENT="#datasent";

public void setup() {
	
	camera(width/2, height/4, (height/2.0f) / tan(PI*30.0f / 180.0f), width/2, height/2, 0, 0, 1, 0);
	frameRate(1);
	smooth();
stroke(255);
  // create a new datagram connection on port 6000
  // and wait for incomming message
  udp = new UDP( this, 12000 );
  //udp.log( true );     // <-- printout the connection activity
  udp.listen( true );
  smooth();
  distL=new FloatList();distR=new FloatList();
  try{initPd();}catch(IOException e){}
  OUTPUT = createWriter("/storage/sdcard0/Hearingthehidden_dist_"+month()+day()+"_"+hour()+minute()+second()+".txt");
  OUTPUT.println("FrameCount"+","+"Distance_Left"+","+"Distance_Right");
}

//process events
public void draw() {
background(10,10,10,3);
double ratio=3f;
    stroke(255);noFill();
text("Left: "+distance_L+"cm", 50,50);
text("Right: "+distance_R+"cm", 300,50);
text("Datasent:"+datasent, 300,150);
PdBase.sendFloat(DISTANCEL, distance_L);
PdBase.sendFloat(DISTANCER, distance_R);


    if(distance_L!=0){
        x_left=width/2-distance_L*(float)ratio-15;
    }
    else if(distance_L==0){x_left=0;}
    if(distance_R!=0){
        x_right=width/2+distance_R*(float)ratio+15;
    }
    else if (distance_R==0){x_right=width;}
    
//draw user (stay in the middle of the screen)
    fill(102, 0, 51, 200);
    noStroke();
    //user
    pushMatrix();
    translate(width/2, height/2,0);
    sphere(18);
    fill(122*random(1,1.2f),20*random(1,1.2f),71*random(1,1.2f),50*random(1,1.2f));
    //sphereDetail(6);
    sphere(25);
    popMatrix();
    
    //left wall 1
    fill(200,200,0,150);
    pushMatrix();
    translate(x_left, height/2, -5);//left distance, y, z (change over time)
    box(5,40,40);
    popMatrix();
    //right wall 1
    pushMatrix();
    translate(x_right, height/2, -5);
    box(5, 40, 40);
    popMatrix();
    
    //left walls 2
    stroke(112,112,112,70);
    noFill();
    for(int i=distL.size()-1;i>1;i--){
        pushMatrix();
        translate(distL.get(i),height/2, -5+60*(distL.size()-i));
        box(5,40,40);
        popMatrix();
        //right walls 2
        pushMatrix();
        translate(distR.get(i), height/2, -5+60*(distL.size()-i));
        box(5,40,40);
        popMatrix();
    }//finish for loop
    distL.append(x_left);
    distR.append(x_right);
    
}

/**
 * To perform any action on datagram reception, you need to implement this 
 * handler in your code. This method will be automatically called by the UDP 
 * object each time he receive a nonnull message.
 * By default, this method have just one argument (the received message as 
 * byte[] array), but in addition, two arguments (representing in order the 
 * sender IP address and his port) can be set like below.
 */
// void receive( byte[] data ) {       // <-- default handler
public void receive( byte[] data ) {  // <-- extended handler

	for(int i=0; i < data.length; i++){
//res_message+=i+":"+data[i]+",";
if(data[i]!=13&&data[i]!=10){
if(data[i]==76){isLeft=true;isRight=false;datasent=true;}
else if(data[i]==82){isLeft=false;isRight=true;}
else if(data[i]>=48&&data[i]<=57){
if(isLeft){left+=data[i]-48;}else if(isRight){right+=data[i]-48;}
}
}//end ignoring /r/n
else if(data[i]==10){//the end of one data entry
distance_L=PApplet.parseInt(left);
distance_R=PApplet.parseInt(right);
if(datasent){
	//background(225);
PdBase.sendBang(DATASENT);
}
println("L: " + distance_L+" R: "+distance_R);
export2Txt(distance_L,distance_R);
left="";right="";
datasent=false;
}
 }
}


public void export2Txt(float distance_L, float distance_R){
	  OUTPUT.println(frameCount+","+distance_L+","+distance_R);  // here we export the coordinates of the vector using String concatenation!
	  OUTPUT.flush();
	  //println("data has been exported");
	}

public void stop() 
{ 
 PdAudio.stopAudio();
 PdBase.release();
}
public void onResume() //register sensor listener
{
  super.onResume();
  PdAudio.startAudio(this);
}

public void onPause() //unregister sensor listener
{
  super.onPause();
  //close data logging
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
		InputStream in = res.openRawResource(R.raw.somatheremin);
		patchFile = IoUtils.extractResource(in, "somatheremin.pd", getCacheDir());
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

}//end class
