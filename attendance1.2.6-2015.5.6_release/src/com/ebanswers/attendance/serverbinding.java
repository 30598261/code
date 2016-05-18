package com.ebanswers.attendance;

import com.ebanswers.attendance.R;
import com.example.util.AbstractActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/** 
* <p>Example of binding and unbinding to the {@link LocalService}. 
* This demonstrates the implementation of a service which the client will 
* bind to, receiving an object through which it can communicate with the service.</p> 
*/ 
public class serverbinding extends AbstractActivity { 
        private boolean mIsBound; 
        private Button button, button1;
        private InteractionService mBoundService; 
        @Override
		public void onCreate(Bundle savedInstanceState) { 
                super.onCreate(savedInstanceState); 

                initUI(savedInstanceState, R.layout.server_binding); 
        } 

        private ServiceConnection mConnection = new ServiceConnection() { 
                public void onServiceConnected(ComponentName className, IBinder service) { 
                        // This is called when the connection with the service has been 
                        // established, giving us the service object we can use to 
                        // interact with the service.    Because we have bound to a explicit 
                        // service that we know is running in our own process, we can 
                        // cast its IBinder to a concrete class and directly access it. 
                        mBoundService = ((InteractionService.LocalBinder)service).getService();  
                } 

                public void onServiceDisconnected(ComponentName className) { 
                        // This is called when the connection with the service has been 
                        // unexpectedly disconnected -- that is, its process crashed. 
                        // Because it is running in our same process, we should never 
                        // see this happen. 
                        mBoundService = null; 
                } 
        }; 

        private OnClickListener mBindListener = new OnClickListener() { 
                public void onClick(View v) { 
                        // Establish a connection with the service.    We use an explicit 
                        // class name because we want a specific service implementation that 
                        // we know will be running in our own process (and thus won't be 
                        // supporting component replacement by other applications). 
                        bindService(new Intent(serverbinding.this, InteractionService.class), mConnection, Context.BIND_AUTO_CREATE); 
                        mIsBound = true; 
                } 
        }; 

        private OnClickListener mUnbindListener = new OnClickListener() { 
                public void onClick(View v) { 
                        if (mIsBound) { 
                        	// Detach our existing connection. 
                            unbindService(mConnection); 
                            mIsBound = false; 
                        } 
                } 
        };
		@Override
		public void findView() {
			// TODO Auto-generated method stub
			 button = (Button)findViewById(R.id.bind); 
			 button1 = (Button)findViewById(R.id.unbind); 
		}

		@Override
		public void fillData() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setListener() {
			// TODO Auto-generated method stub
			 button.setOnClickListener(mBindListener); 
             button1.setOnClickListener(mUnbindListener); 
		}

		@Override
		public Bundle saveData() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void loadData(Bundle bundle) {
			// TODO Auto-generated method stub
			
		} 
}