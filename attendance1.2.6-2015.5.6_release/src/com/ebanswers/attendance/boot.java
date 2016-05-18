package com.ebanswers.attendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class boot  extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			 if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
					 action.equals("com.ebanswers.attendance")) {
				// System.out.println("启动");
                 // 启动联系人数据、通话记录数据查询的Service
				  //后边的XXX.class就是要启动的服务  
		        Intent service = new Intent(context, InteractionService.class);  
		        context.startService(service);  
			 }
		}	
}
