package com.example.testforbd;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends Fragment {

	public static Fragment globalFragment = null;

	final String LOG_TAG = "MainFragment";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "onCreate");
		globalFragment = this;
		setHasOptionsMenu(true);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreateView");
		View v = inflater.inflate(R.layout.fragment_main, null);

		return v;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(LOG_TAG, "onActivityCreated");
	}

	public void toggleHideyBar(int iKey4Pressed) {
		Log.d(LOG_TAG, "toggleHideyBar iKey4Pressed=" + iKey4Pressed);
		// The UI options currently enabled are represented by a bitfield.
		// getSystemUiVisibility() gives us that bitfield.
		
		int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility(); 
		int newUiOptions = uiOptions; 
		//boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions); 
		//if (isImmersiveModeEnabled) { Log.i(LOG_TAG, "Turning immersive mode mode off. ");} 
		//else { Log.i(LOG_TAG, "Turning immersive mode mode on."); }
		  
		 // Navigation bar hiding: Backwards compatible to ICS. 
		 if (Build.VERSION.SDK_INT >= 14) { newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; }
		  
		 // Status bar hiding: Backwards compatible to Jellybean 
		 if (Build.VERSION.SDK_INT >= 16) { newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN; }
		 //if (Build.VERSION.SDK_INT >= 18) { newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; }
		 
		if (iKey4Pressed == 1) 
			getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		else
			getActivity().getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
		if (iKey4Pressed < 0)
			getActivity().getActionBar().hide();		 
	}
}
