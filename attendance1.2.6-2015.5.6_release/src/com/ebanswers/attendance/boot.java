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
				// System.out.println("����");
                 // ������ϵ�����ݡ�ͨ����¼���ݲ�ѯ��Service
				  //��ߵ�XXX.class����Ҫ�����ķ���  
		        Intent service = new Intent(context, InteractionService.class);  
		        context.startService(service);  
			 }
		}	
}
