package com.example.testforbd;

import java.io.File;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SetProfile extends //PreferenceFragment	
	PreferenceActivity 
	implements OnSharedPreferenceChangeListener {
	
	final String LOG_TAG = "SetProfile";
	ListPreference lpProfList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(LOG_TAG, " onCreate");

		getPreferenceManager().setSharedPreferencesName(MainActivity.sProfile);
		addPreferencesFromResource(R.xml.setprofile);
		initSummary(getPreferenceScreen());
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceGroup) {
			PreferenceGroup pGrp = (PreferenceGroup) p;
			for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
				initSummary(pGrp.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
	}

	private void updatePrefSummary(Preference p) {
		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference) p;
			CharSequence listSummary = listPref.getSummary();
			if (listSummary == null || listSummary.length() == 0){
				lpProfList = listPref;
				getListSP();
			}
			else {
				MainActivity.sProfile = listPref.getEntry().toString();
				p.setSummary(listPref.getEntry());
			}
		} else if (p instanceof EditTextPreference || p instanceof MultiSelectListPreference) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			String text = editTextPref.getText();
			if (text.length() > 0){
				Toast.makeText(MainActivity.globalContext, "New Profile is " + text, Toast.LENGTH_SHORT).show();
				CharSequence[] cs = new CharSequence[lpProfList.getEntries().length + 1];
				int iDef = -1;
				for (int i = 0; i < lpProfList.getEntries().length; i++)
					cs[i] = lpProfList.getEntries()[i];
				cs[cs.length - 1] = text;
				lpProfList.setEntries(cs);
				for (int i = 0; i < lpProfList.getEntries().length; i++)
					if (text.equals(lpProfList.getEntries()[i])) iDef = i;
				lpProfList.setValueIndex(iDef);
				lpProfList.setSummary(text);
				editTextPref.setText("");
			}
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updatePrefSummary(findPreference(key));
	}
	@Override
	public void onResume() {
		super.onResume();

		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}
	@Override
	public void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);

    	SharedPreferences sp = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(MainActivity.globalContext);
    	sp.edit().putString("sProfile", MainActivity.sProfile).commit();
	}
	
	void getListSP(){
		File prefsdir = new File(
				MainActivity.globalContext.getApplicationInfo().dataDir, "shared_prefs");

	    if(prefsdir.exists() && prefsdir.isDirectory()){
	        String[] list = prefsdir.list();
	        Log.d(LOG_TAG, "getListSP");
	        CharSequence[] cs = new CharSequence[list.length - 1];
	        int iDef = -1;
	        int j = 0;
			for (int i = 0; i < list.length; i++) {
				String item = list[i].substring(0, list[i].length() - 4);
				if (item.equals(MainActivity.sProfile)) iDef = j;
				if (! list[i].equals(MainActivity.globalContext.getPackageName() + "_preferences.xml"))
					cs[j++] = item;
				Log.d(LOG_TAG, String.format("i:%s; j:%s; item:%s", i, j, list[i]));
			}
			lpProfList.setEntries(cs);
			lpProfList.setValueIndex(iDef);
			lpProfList.setSummary(MainActivity.sProfile);
	    }
	}	
}
