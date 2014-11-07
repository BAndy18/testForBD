package com.example.testforbd;

import java.io.BufferedInputStream;
import java.io.InputStream;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

public class ImageGetFromDB extends Activity {
	final static String LOG_TAG = "ImageGetFromDB";

	public static HttpClient httpclient;
	public static String sPaths;
	public static Bitmap[] mPicts;

	final static String DropB_key = "yg8gzisonsv0fwl";
	final static String DropB_secret = "x4ahgnpotrfbkk1";
	// from dropB API
	final static String request_url = "https://api.dropbox.com/1/oauth/request_token";
	final static String authorization_url = "https://www.dropbox.com/1/oauth/authorize";
	final static String access_url = "https://api.dropbox.com/1/oauth/access_token";
	final static String callBack_url = "mydrop://myBox.com";

	final static String getmetadata_url = "https://api.dropbox.com/1/metadata/auto";
	final static String getfiles_url = "https://api-content.dropbox.com/1/files/auto";

	public static CommonsHttpOAuthConsumer consumer;
	public static CommonsHttpOAuthProvider provider;

	Handler h;
	static String authURL = "";
	static int iCountMax = 10;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imagedb);

		String result = getIntent().getStringExtra("result");
		if (!result.equals("OK")) {
			authURL = "";
			ImageAdapter.mPicts = null;
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}

		Log.d(LOG_TAG, "AuthDB onCreate authURL = " + authURL);
		h = new Handler() {
			public void handleMessage(android.os.Message msg) {
				int iCount = sPaths.split(";").length;
				iCount = (iCount > iCountMax) ? iCountMax : iCount;

				EditText fName = (EditText) findViewById(R.id.fName);
				fName.setText(String.format("Loaded files: %s(%s)", msg.what, iCount));
				Log.d(LOG_TAG, "handleMessage msg.what =" + msg.what);
				
				if (msg.what == iCount || sPaths.length() == 0) {
					ImageAdapter.mPicts = mPicts;

					Intent intent = new Intent(getApplicationContext(), MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}
			};
		};

		getPictures();
	}

	@Override
	public void onBackPressed() {
		Log.d(LOG_TAG, "KEYCODE_BACK");
	}

	public static boolean GoToAuth(Context context) {
		if (authURL.length() == 0) {
			Log.d(LOG_TAG, "setUpAuthorization ");

			consumer = new CommonsHttpOAuthConsumer(DropB_key, DropB_secret);
			provider = new CommonsHttpOAuthProvider(request_url, access_url, authorization_url);

			try {
				authURL = provider.retrieveRequestToken(consumer, callBack_url);
			} catch (Exception e) {
				Log.d(LOG_TAG, "Error in retrieveRequestToken: " + e.getMessage());
				e.printStackTrace();
			}
		}
		Log.d(LOG_TAG, "authURL = " + authURL);
		if (authURL.length() > 0) {
			Intent mainIntent = new Intent(context, ImageGetFromDB.AuthDB.class).putExtra("authURL", authURL);
			context.startActivity(mainIntent);
		}
		return (authURL.length() > 0);
	}

	public void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "onDestroy");
	}

	public void onResume() {
		Log.d(LOG_TAG, "onResume");
		super.onResume();
	}

	void getPictures() {
		Log.d(LOG_TAG, "getPictures ");

		sPaths = "";
		httpclient = new DefaultHttpClient();
		getFolderPictures("/Photos");
		httpclient.getConnectionManager().shutdown();

		getFiles();
	}

	void getFolderPictures(String folderName) {
		folderName = folderName.replace(" ", "%20");
		if (!folderName.startsWith("/"))
			folderName = "/" + folderName;
		Log.d(LOG_TAG, "getFolderPictures = " + folderName);

		String uRL = getmetadata_url + folderName;
		HttpGet request = new HttpGet(uRL);

		try {
			consumer.sign(request);

			ResponseHandler<String> handler = new BasicResponseHandler();
			String result = httpclient.execute(request, handler);
			// Log.d(LOG_TAG, "result = " + result);

			JSONObject json_data = new JSONObject(result);
			JSONArray contArray = json_data.getJSONArray("contents");
			for (int i = 0; i < contArray.length(); i++) {
				JSONObject el = new JSONObject(contArray.get(i).toString());
				if (el.getString("is_dir").contains("true")) {
					getFolderPictures(el.getString("path"));
				} else if (el.getString("mime_type").startsWith("image")) {
					String s = el.getString("path");
					Log.d(LOG_TAG, "JSONObject sPath = " + s);
					sPaths += s.replace(" ", "%20") + ";";
				}
			}
		} catch (Exception e) {
			Log.d(LOG_TAG, "Error in getPictures: " + e.getMessage());
			if (!e.getMessage().contains("Unauthorized"))
				e.printStackTrace();
		}
	}

	void getFiles() {
		Log.d(LOG_TAG, "sPaths = " + sPaths + " len=" + sPaths.length());
		if (sPaths.length() == 0) {
			h.sendEmptyMessage(0);
			return;
		}

		Thread t = new Thread(new Runnable() {
			@SuppressLint("NewApi")
			public void run() {
				String[] sV = sPaths.split(";");
				mPicts = new Bitmap[sPaths.split(";").length];
				final BitmapFactory.Options options = new BitmapFactory.Options();

				try {
					httpclient = new DefaultHttpClient();
					h.sendEmptyMessage(0);
					for (int i = 0; i < sV.length; i++) {
						String uRL = getfiles_url + sV[i];
						HttpGet request = new HttpGet(uRL);
						consumer.sign(request);

						HttpResponse response = httpclient.execute(request);
						InputStream in = response.getEntity().getContent();
						BufferedInputStream bis = new BufferedInputStream(in, 8192);

						options.inJustDecodeBounds = true;
						mPicts[i] = BitmapFactory.decodeStream(bis, null, options);
						options.inSampleSize = ImageAdapter.calculateInSampleSize(options);

						options.inJustDecodeBounds = false;
						options.inPreferredConfig = Bitmap.Config.RGB_565;
						bis.reset();
						mPicts[i] = BitmapFactory.decodeStream(bis, null, options);
						bis.close();
						in.close();

						Log.d(LOG_TAG, "mPicts = " + i + " len=" + mPicts[i].getByteCount());
						h.sendEmptyMessage(i + 1);
					}
					httpclient.getConnectionManager().shutdown();
				} catch (Exception e) {
					Log.d(LOG_TAG, "Error in getFiles: " + e.getMessage());
					if (e.getMessage() != null && !e.getMessage().contains("Unauthorized"))
						e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public static class AuthDB extends Activity {
		final String LOG_TAG = "AuthDB";

		String authURL = "";

		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Log.d(LOG_TAG, "AuthDB onCreate");

			authURL = getIntent().getStringExtra("authURL");
			Log.d(LOG_TAG, "AuthDB onCreate authURL = " + authURL);

			if (authURL != null) {
				Intent intent2 = new Intent(Intent.ACTION_VIEW);
				intent2.setData(Uri.parse(authURL));
				startActivity(intent2);
			}
		}

		@Override
		public void onResume() {
		    super.onResume();

		    String result = "fail";
			Uri uri = this.getIntent().getData();
			Log.d(LOG_TAG, "onResume uri=" + uri);
			if (uri == null && authURL != null){
				authURL = null;
				return;
			}
			
			if (uri != null && uri.getHost().equals("myBox.com")) {
				try {
					// grab the token/secret items to carry on
					String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

					ImageGetFromDB.provider.retrieveAccessToken(ImageGetFromDB.consumer, verifier);
					String ACCESS_KEY = ImageGetFromDB.consumer.getToken();
					String ACCESS_SECRET = ImageGetFromDB.consumer.getTokenSecret();

					Log.d("OAuth Dropbox", ACCESS_KEY);
					Log.d("OAuth Dropbox", ACCESS_SECRET);
				} catch (Exception e) {
				}
				result = "OK";
			}
			Intent intent = new Intent(getApplicationContext(), ImageGetFromDB.class).putExtra("result", result);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}
}