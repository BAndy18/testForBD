package com.example.testforbd;

import java.util.List;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("NewApi")
public class MainActivity extends Activity {
	//ActionBarActivity {

	public static Activity globalContext = null;
	public static String sProfile = "default";
	
	final String LOG_TAG = "MainActivity";

	SharedPreferences sp;
	static TextView tvInfo;
	ImageView imageView1;
	ImageView imageView2;
	ImageAdapter imageAdapter = new ImageAdapter(this);
	long interval = 1;
	int iPicture = -1;
	
	//int bKey4Pressed = -1;
	int iKey4Pressed = -1;
	long lLastKey4Pressed = 0;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        globalContext = this;
        
        sp = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this);
        
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment);
        
        tvInfo = (TextView) fragment.getView().findViewById(R.id.tvInfo);
        imageView1 = (ImageView) fragment.getView().findViewById(R.id.imageView1);
        imageView2 = (ImageView) fragment.getView().findViewById(R.id.imageView2);

        Log.d(LOG_TAG, "onCreate: " + iPicture);
    } 


	@Override
    protected void onResume() {
    	iPicture = sp.getInt("iPicture", 0);
    	sProfile = sp.getString("sProfile", "default");
    	Log.d(LOG_TAG, "onResume: iPicture = " + iPicture + "; Profile = " + sProfile);

    	interval = Integer.parseInt(getSP().getString("edtInterval", "2"));
        
        if (getSP().getBoolean("chbFullSrc", false)){
        	((MainFragment) MainFragment.globalFragment).toggleHideyBar(iKey4Pressed);
        } else
        	getActionBar().show();
        
        if (getSP().getBoolean("chbBlock", false)){
        	Toast.makeText(this, "Keys is blocked, for unblock press 444", Toast.LENGTH_LONG).show();
        	iKey4Pressed = 0;
        	((MainFragment) MainFragment.globalFragment).toggleHideyBar(iKey4Pressed);
        } else
        	iKey4Pressed = -1;
        
       	startService(new Intent(this, MainService.class));
        
		imageAdapter.getPictures();
		showNexPicture();
        
        super.onResume();         
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	   if (keyCode == KeyEvent.KEYCODE_BACK)
	   {
		   Log.d(LOG_TAG, "KEYCODE_BACK");
		   if (iKey4Pressed < 0)
			   finish();
	   }
	   if (keyCode == KeyEvent.KEYCODE_4){
		   if (iKey4Pressed >= 0){
			   iKey4Pressed = (System.currentTimeMillis() - lLastKey4Pressed < 500) ? iKey4Pressed+1:0;
			   Log.d(LOG_TAG, "*** KEYCODE_4 i=" + iKey4Pressed);
			   if (iKey4Pressed == 2){
				   Log.d(LOG_TAG, "*** KEYCODE_4 3 time");
				   ((MainFragment) MainFragment.globalFragment).toggleHideyBar(iKey4Pressed);
				   Toast.makeText(this, "Keys unblocked", Toast.LENGTH_SHORT).show();
				   iKey4Pressed = -1;
			   }
			   lLastKey4Pressed = System.currentTimeMillis();
		   }
	   }
	 return false;
	}
       
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        public void run() { showNexPicture(); }
    };
    
	void showNexPicture() {
    	handler.removeCallbacks(runnable);
    	
    	if (interval > 0) {
    		iPicture++;
    		
    		//Log.d(LOG_TAG, "showNexPicture getCount= " + imageAdapter.getCount());
    		if (imageAdapter.getCount() == 0){
    			imageView1.setImageResource(R.drawable.noimage);
    			return;
    		}
    		if (iPicture >= imageAdapter.getCount()) iPicture = 0;
    		int iPicture2 = (iPicture > 0) ? iPicture - 1 : imageAdapter.getCount() - 1;
    		
    		Point size = new Point();
    		getWindowManager().getDefaultDisplay().getSize(size);
    		//size.y = getWindowManager().getDefaultDisplay().getHeight();
    		//size.x = getWindowManager().getDefaultDisplay().getWidth();

    		String animType = getSP().getString("lstEffect", "1");
    		int animDuration = 1000;
    		
    		if (getSP().getBoolean("chbDebug", false)) 
    			tvInfo.setText(String.format("path=%s; iP=%s; int=%s; anim=%s; scr=%sx%s", 
    					imageAdapter.mPath, iPicture, interval, animType, size.y, size.x));

			AnimatorSet set1 = new AnimatorSet();
			AnimatorSet set2 = new AnimatorSet();
    		switch (Integer.parseInt(animType)) {
    		case 1:
	    	    set1.play(ObjectAnimator.ofFloat(imageView1, "translationX", -size.x, 0).setDuration(animDuration));
	    	    set2.play(ObjectAnimator.ofFloat(imageView2, "translationX", 0, size.x).setDuration(animDuration));
	    	    break;
    		case 2:
	    	    set1.play(ObjectAnimator.ofFloat(imageView1, "translationY", -size.y, 0).setDuration(animDuration));
	    	    set2.play(ObjectAnimator.ofFloat(imageView2, "translationY", 0, size.y).setDuration(animDuration));
	    	    break;
    		case 3:
	    	    set1.play(ObjectAnimator.ofFloat(imageView1, "alpha", 0, 1).setDuration(animDuration));
	    	    set2.play(ObjectAnimator.ofFloat(imageView2, "alpha", 1, 0).setDuration(animDuration));
	    	    break;
    		/*case 0: 
	    		Drawable[] layers = new Drawable[2];
	    		layers[0] = getResources().getDrawable(res_picts[iPicture2]);
	    		layers[1] = getResources().getDrawable(res_picts[iPicture]);
	    		TransitionDrawable transition = new TransitionDrawable(layers);
	    		imageView1.setImageDrawable(transition);
	    		//transition.setCrossFadeEnabled(false);
	    		transition.startTransition(1500);
	    		break;
    		case -1:
	    		ia.getView(iPicture, imageView1, null);
	    		break;/**/
    		}
    	    AnimatorSet set3 = new AnimatorSet();
    	    set3.playTogether(set1,set2);

    	    imageAdapter.getView(iPicture, imageView1, null);
    	    imageAdapter.getView(iPicture2, imageView2, null);
    	    set3.start();
    		
	    	handler.postDelayed(runnable, interval * 1000);
	    	Log.d(LOG_TAG, "showNexPicture iPicture = " + iPicture);
    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	handler.removeCallbacks(runnable);
    	sp.edit().putInt("iPicture", iPicture).commit();
    	sp.edit().putString("sProfile", sProfile).commit();
    	
    	Log.d(LOG_TAG, "MainActivity: onDestroy()");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuItem mi = menu.add(0, 1, 0, R.string.action_settings);
        mi.setIntent(new Intent(this, Settings.class));
        
        mi = menu.add(0, 2, 1, R.string.action_profiles);
        mi.setIntent(new Intent(this, SetProfile.class));
        
        return super.onCreateOptionsMenu(menu);
    }    

  /*  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d(LOG_TAG, "onOptionsItemSelected id=" + id);
        if (id == 2) {
//        	getFragmentManager().beginTransaction().replace(android.R.id.content, new SetProfile()).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }/**/

	public static SharedPreferences getSP() {
		return globalContext.getSharedPreferences(sProfile, MODE_PRIVATE);
	}
	
	public static boolean isRunning(Context ctx) {
		if (ctx == null) return false;
		
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) 
                return true;                                  
        }

        return false;
    }	
}
