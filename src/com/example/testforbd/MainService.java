package com.example.testforbd;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {
	final String LOG_TAG = "MainService";
	
	public void onCreate() {
		super.onCreate();
		Log.d(LOG_TAG, "onCreate");
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "onStartCommand");

		registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		if (MainActivity.getSP().getBoolean("chbStart", false)) {
			String stmpStart = MainActivity.getSP().getString("tmpStart", "");
			Log.d(LOG_TAG, "onStartCommand stmpStart=" + stmpStart);
			setAlarm(stmpStart, 1);			
		}
		if (MainActivity.getSP().getBoolean("chbStop", false)) {
			String stmpStop = MainActivity.getSP().getString("tmpStop", "");
			Log.d(LOG_TAG, "onStartCommand stmpStop=" + stmpStop);
			setAlarm(stmpStop, 0);			
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	void setAlarm(String sTime, int iPI) {
		String[] sV = sTime.split(":");
		int iH, iM;
		if (sV.length != 2) return;
		try {
			iH = Integer.parseInt(sV[0]);
			iM = Integer.parseInt(sV[1]);
		} catch(NumberFormatException nfe) {        	
        	Log.d(LOG_TAG, "ERR setAlarm: Could not time: " + sTime);
        	return;
        }	
		Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();

        calSet.set(Calendar.HOUR_OF_DAY, iH);
        calSet.set(Calendar.MINUTE, iM);
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);

        if (calSet.compareTo(calNow) <= 0) {
            // Today Set time passed, count to tomorrow
            calSet.add(Calendar.DATE, 1);
        }
        
        Intent intentAlarm = new Intent(getBaseContext(), AlarmReciever.class).putExtra("id", iPI);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), iPI, intentAlarm, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5000,
        alarmManager.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(),
                pendingIntent);
        
        Log.d(LOG_TAG, "setAlarm: time: " + calSet + " id=" + iPI);
	}

	public void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "onDestroy");
	}

	public IBinder onBind(Intent intent) {
		Log.d(LOG_TAG, "onBind");
		return null;
	}
	
	BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			Boolean bAsCharge = MainActivity.getSP().getBoolean("chbAsCharge", false);
			if (status == 2 && bAsCharge && ! MainActivity.isRunning(MainActivity.globalContext)){
				Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getApplication().startActivity(mainIntent);
			}
			Log.d(LOG_TAG, "batteryReceiver status=" + status + " chbAsCharge=" + bAsCharge);
		}
	};
}
