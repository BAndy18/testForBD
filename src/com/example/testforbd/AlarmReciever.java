package com.example.testforbd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReciever extends BroadcastReceiver {
	final String LOG_TAG = "AlarmReciever";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving

		int id = intent.getIntExtra("id", -1);
		Log.d(LOG_TAG, "AlarmReciever onReceive id=" + id);
		if (id == 1 && !MainActivity.isRunning(MainActivity.globalContext)) {
			Intent mainIntent = new Intent(context, MainActivity.class);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(mainIntent);
		} else if (id == 0 && MainActivity.isRunning(MainActivity.globalContext)) {
			MainActivity.globalContext.finish();
		}
	}
}
