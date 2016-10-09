package com.lge.e_instrument;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class PlayRecordService extends IntentService {
	
	public static final String TAG = "PlayRecordService";
	private int mNum[];
	private long mBlank[];
	private long mPlay[];
	private MusicUtils mUtils;
	private int mMusic[] = { R.raw.do1, R.raw.re2, R.raw.mi3, R.raw.fa4, R.raw.sol5, R.raw.la6, R.raw.si7,
			R.raw.hduo8 };
	
	public PlayRecordService(){
		
		super("com.lge.e-instrument.playrecordservice");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG,"onHandleIntent start!");
		mUtils = new MusicUtils(getApplicationContext(), mMusic); 
		mNum = intent.getIntArrayExtra(PianoActivity.EXTRA_ID);
		mBlank = intent.getLongArrayExtra(PianoActivity.EXTRA_BLANK);
		mPlay = intent.getLongArrayExtra(PianoActivity.EXTRA_PLAY);
		
		for(int i = 0; i < mNum.length;i++)
		{
			final long blank = mBlank[i];
			final long play = mPlay[i];
			final int number = mNum[i];
			
			try {
				Thread.sleep(blank);
				
			} catch (InterruptedException e) {
				Log.e("PlayRecord", "", e);
			}
			mUtils.soundPlay(number);

			Log.i("PlayRecord", "Play " + number + " " + blank + " " + play);
			
			try {				
				Thread.sleep(play);
			} catch (InterruptedException e) {
				Log.e("PlayRecord", "", e);
			}
		}
	}

}
