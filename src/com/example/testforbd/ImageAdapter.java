package com.example.testforbd;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	final String LOG_TAG = "ImageAdapter";
	
	private Context mContext;
	
	public static Bitmap[] mPicts;
	private Integer[] res_picts = { R.drawable.p10, R.drawable.p11,
			R.drawable.p12, R.drawable.p13, R.drawable.p14 };

	public String mPath = "";
	static int reqImageSize = 100;
	static int iPreviousSrcType = 0;
	
	public ImageAdapter(Context context) {
		Log.d(LOG_TAG, "ImageAdapter init count=" + getCount());
		mContext = context;
	}

	public int getCount() {
		return (mPicts == null) ? 0 : mPicts.length;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {		
		((ImageView) convertView).setImageBitmap(mPicts[position]);
		return convertView;
	}

	@SuppressLint("NewApi")
	public void getPictures() {
		String srcTypeS = MainActivity.getSP().getString("lstSource", "1");
		MainActivity.tvInfo.setText("");
		int srcType = Integer.parseInt(srcTypeS);
		Log.d(LOG_TAG, "getPictures: srcType=" + srcType);

		switch (srcType) {
		case 1:
			mPicts = new Bitmap[res_picts.length];

			for (int i = 0; i < res_picts.length; i++) {
				mPicts[i] = BitmapFactory.decodeResource(mContext.getResources(), res_picts[i]);
			}
			break;
		case 2:
		case 3:
			String folder = MainActivity.getSP().getString("edtSource", "");
			if (folder.length() == 0) folder = "Pictures";
			if (!folder.startsWith("/")) folder = "/" + folder;
			
			mPath = Environment.getRootDirectory() + folder;
			if (srcType == 3)
				mPath = Environment.getExternalStorageDirectory() + folder;

			File directory = new File(mPath);
			File[] listFiles = directory.listFiles();
			if (listFiles == null || listFiles.length == 0){
				MainActivity.tvInfo.setText("path = " + mPath);
				mPicts = null;
				return;
			}
			final BitmapFactory.Options options = new BitmapFactory.Options();
			mPicts = new Bitmap[listFiles.length];

			for (int i = 0; i < listFiles.length; i++) {
				File imgFile = new File(listFiles[i].toString());
				Log.d(LOG_TAG, String.format("getPictures: path=%s; url=%s", mPath, imgFile.getAbsolutePath()));
				
				options.inJustDecodeBounds = true;
				mPicts[i] = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                options.inSampleSize = ImageAdapter.calculateInSampleSize(options);
                
			    options.inJustDecodeBounds = false;
			    options.inPreferredConfig = Bitmap.Config.RGB_565;
				mPicts[i] = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
				Log.d(LOG_TAG, "mPicts = " + i + " len=" + mPicts[i].getByteCount());
			}
			break;
		case 4:
			if (iPreviousSrcType != srcType)
				ImageGetFromDB.GoToAuth(mContext);
			break;
		}
		iPreviousSrcType = srcType;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqImageSize || width > reqImageSize) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			while ((halfHeight / inSampleSize) > reqImageSize
					&& (halfWidth / inSampleSize) > reqImageSize) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}

