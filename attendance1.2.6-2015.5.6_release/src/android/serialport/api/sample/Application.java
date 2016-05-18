/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android.serialport.api.sample;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import com.ebanswers.attendance.TestRolateAnimActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.view.WindowManager;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class Application extends android.app.Application implements  Thread.UncaughtExceptionHandler{

	public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
	private SerialPort mSerialPort = null;

	
	private WindowManager.LayoutParams wmParams=new WindowManager.LayoutParams();
	public WindowManager.LayoutParams getMywmParams(){
		return wmParams;
	}
	
	 @Override 
	 public void onCreate() { 
	         super.onCreate(); 
	         //…Ë÷√Thread Exception Handler 
	         Thread.setDefaultUncaughtExceptionHandler(this); 	   
	 } 

	public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {				
				/* Open the serial port */
			mSerialPort = new SerialPort(new File("/dev/ttyS1"), 9600, 0);
		}
		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}


	 @Override 
	 public void uncaughtException(Thread thread, Throwable ex) { 
	         System.out.println("uncaughtException"); 
	       //  System.exit(0); 
	         Intent intent = new Intent(this, TestRolateAnimActivity.class); 
	         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
	         Intent.FLAG_ACTIVITY_NEW_TASK); 
	         startActivity(intent); 
	     }
}
