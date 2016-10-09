package com.lge.e_instrument;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

public class AnotherActivity extends Activity {
	
	private Button mButton[];
	private MusicUtils mUtils;
	private View mParent;
	private int mButtonId[];
	private boolean mHavePlayed[];
	private int mCurrentKey;
	private int mLastKey;
	private View mKeys;
	private boolean mRecordStarted = false;
	private int mGuitar[] = {R.raw.do1, R.raw.re2, R.raw.mi3, R.raw.fa4, R.raw.sol5, R.raw.la6, R.raw.si7};
	private int mFlute[] = {R.raw.si7,R.raw.do1, R.raw.re2, R.raw.mi3, R.raw.fa4, R.raw.sol5, R.raw.la6};
	private int mViolin[] = {R.raw.la6, R.raw.si7,R.raw.do1, R.raw.re2, R.raw.mi3, R.raw.fa4, R.raw.sol5};
	
	private Sound mSound;
	private ArrayList<Sound> mSounds;
	private SoundSerializer mSoundSerializer;
	private long mSoundStart;
	private long mSoundEnd;
	private long mBlankStart;
	private long mBlankEnd;
	
	private ListView mFileList;
	private Dialog chooseFileDialog;
	ArrayList<String> filesName;
	private File mDestDir;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_another);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		mUtils = new MusicUtils(getApplicationContext(), mGuitar);
		mDestDir = new File(Environment.getExternalStorageDirectory() + "/otherSounds");
		
		if(!mDestDir.exists())
		{
			mDestDir.mkdirs();
		}
		
		mButtonId = new int[7];
		mButtonId[0] = R.id.duo;
		mButtonId[1] = R.id.re;
		mButtonId[2] = R.id.mi;
		mButtonId[3] = R.id.fa;
		mButtonId[4] = R.id.sol;
		mButtonId[5] = R.id.la;
		mButtonId[6] = R.id.si;
		
		
		mButton = new Button[7];
		mHavePlayed = new boolean[7];
		
		for(int i = 0; i < mButton.length; i++)
		{
			mButton[i] = (Button)findViewById(mButtonId[i]);
			mButton[i].setClickable(false);
			mHavePlayed[i] = false;
		}
		
		mCurrentKey = 0;
		mLastKey = 0;
		
		mParent = (View)findViewById(R.id.parent);
		mParent.setClickable(true);
		
		mParent.setOnTouchListener(new View.OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int temp;
				switch(event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					temp = isInAnyScale(event.getX(),event.getY(), mButton);
					if(temp != -1)
					{
						mCurrentKey = temp;
						mButton[mCurrentKey].setBackgroundResource(R.drawable.button_pressed);
						mUtils.soundPlay(mCurrentKey);
						Log.i("--", "sound" + mCurrentKey);
						mHavePlayed[mCurrentKey] = true;
						if (mRecordStarted) {
							mSound = new Sound();
							mSound.setNumber(mCurrentKey);
							mSoundStart = System.currentTimeMillis();
							if (mBlankStart != 0) {
								mBlankEnd = mSoundStart;
								mSound.setBlankD(mBlankEnd - mBlankStart);
							} else {
								mSound.setBlankD(0);
							}
						}
					}
					break;
				case MotionEvent.ACTION_MOVE:
					temp = mCurrentKey;
					for(int i = temp + 1; i >= temp - 1; i--)
					{
						if(i < 0 || i >= mButton.length)
						{
							continue;
						}
						
						if(isInScale(event.getX(), event.getY(), mButton[i]))
						{
							if(i == mCurrentKey)
							{
								if(!mHavePlayed[i])
								{
									mUtils.soundPlay(mCurrentKey);
								}
								break;
							}else
							{
								mLastKey = mCurrentKey;
								mCurrentKey = i;
								mButton[mCurrentKey].setBackgroundResource(R.drawable.button_pressed);
								mUtils.soundPlay(mCurrentKey);
								mHavePlayed[mCurrentKey] = true;
								mButton[mLastKey].setBackgroundResource(R.drawable.button);
								mHavePlayed[mLastKey] = false;
								if (mRecordStarted) {
									mBlankStart = mSoundEnd = System.currentTimeMillis();
									mSound.setSoundD(mSoundEnd - mSoundStart);
									mSounds.add(mSound);
									mSound = new Sound();
									mSound.setNumber(mCurrentKey);
									mSoundStart = System.currentTimeMillis();
									if (mBlankStart != 0) {
										mBlankEnd = mSoundStart;
										mSound.setBlankD(mBlankEnd - mBlankStart);
									} else {
										mSound.setBlankD(0);
									}
								}
								break;
							}
						}
					}
					break;
				case MotionEvent.ACTION_UP:
					mLastKey = mCurrentKey;
					mButton[mCurrentKey].setBackgroundResource(R.drawable.button);
					mHavePlayed[mCurrentKey] = false;
					if (mRecordStarted) {
						mBlankStart = mSoundEnd = System.currentTimeMillis();
						mSound.setSoundD(mSoundEnd - mSoundStart);
						mSounds.add(mSound);
					}
					break;
				}
				return true;
			}
		});
		mKeys = (View) findViewById(R.id.keys);
	}
	
	private boolean isInScale(float x, float y, Button button)
	{
		if(x > button.getLeft() && x < button.getRight() 
				&& y > button.getTop() + mKeys.getTop()
				&& y < button.getBottom() + mKeys.getTop())
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	private int isInAnyScale(float x, float y, Button[] button)
	{
		for(int i = 0; i < button.length; i++)
		{
			if(x > button[i].getLeft() && x < button[i].getRight()
					&& y > button[i].getTop() + mKeys.getTop()
					&& y < button[i].getBottom() + mKeys.getTop())
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.others, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
	 
		switch(item.getItemId())
		{
		case R.id.action_settings_other:
			final View v = this.getLayoutInflater().inflate(R.layout.setting_dialog, null);
		    Dialog setting_dialog = new AlertDialog.Builder(this)
		            .setView(v)
		            .setTitle(R.string.setting_title_string)
		            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		                @SuppressLint("ShowToast")
						@Override
		                public void onClick(DialogInterface dialog, int which) {
		                    // TODO Auto-generated method stub
		                	RadioGroup radioGroup = (RadioGroup)(v.findViewById(R.id.select_sound));
		                	switch(radioGroup.getCheckedRadioButtonId())
		                	{
		                	case R.id.guitar:
		                		mUtils = new MusicUtils(getApplicationContext(), mGuitar);
		                		Toast.makeText(getApplicationContext(), "Change to guitar", 10).show();
		                		break;
		                	case R.id.flute:
		                		mUtils = new MusicUtils(getApplicationContext(), mFlute);
		                		Toast.makeText(getApplicationContext(), "Change to flute", 10).show();
		                		break;
		                	case R.id.violin:
		                		mUtils = new MusicUtils(getApplicationContext(), mViolin);
		                		Toast.makeText(getApplicationContext(), "Change to violin", 10).show();
		                		break;
		                	}
		                }
		            })
		            .create();
		    setting_dialog.show();
			return true;
			
		case android.R.id.home:
			finish();
			return true;
			
		case R.id.about_other:
			AlertDialog about_dialog = new AlertDialog.Builder(this)
			.setView(this.getLayoutInflater().inflate(R.layout.about_dialog, null))
			.setTitle("About")
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}
			}).create();
			about_dialog.show();
			return true;
			
		case R.id.record_other:
			if(mRecordStarted == false)
			{
				item.setTitle(R.string.stop_record);
				mRecordStarted = true;
				mSoundSerializer = new SoundSerializer(getApplicationContext(), 
						new Date().toString() + ".json", mDestDir);
				mSounds = new ArrayList<Sound>();
				mSoundStart = mSoundEnd = mBlankStart = mBlankEnd = 0;
			}
			else
			{
				item.setTitle(R.string.record_sound);
				mRecordStarted = false;
				if(!mSounds.isEmpty()){
					  try {
						  mSoundSerializer.saveSounds(mSounds);
					  } catch (Exception e) {
						  e.printStackTrace();
					  }
					}
			}
			return true;
			
		case R.id.next_other:
			Intent i = new Intent(this, PianoActivity.class);
			startActivity(i);
			finish();
			return true;
			
		case R.id.load_other:
			obtainSoundsList();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@SuppressLint("ShowToast")
	private void obtainSoundsList()
	{
		File file = new File(Environment.getExternalStorageDirectory() + "/otherSounds");
		File[] soundFiles = file.listFiles();
		chooseFileDialog = new Dialog(this);
		mFileList = new ListView(this);
	    filesName = new ArrayList<String>();
		if(soundFiles.length != 0)
		{
		  for(int i = 0; i < soundFiles.length; i++)
		  {
			filesName.add(soundFiles[i].getName().toString());
		  }
		 mFileList.setAdapter(new ArrayAdapter<String>
		 (this,android.R.layout.simple_expandable_list_item_1,filesName));
		 mFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				mSoundSerializer = new SoundSerializer(getApplicationContext(), 
						filesName.get(arg2), mDestDir);
				try {
					mSounds = mSoundSerializer.loadSounds();
				} catch (Exception e) {
					e.printStackTrace();
				}
				chooseFileDialog.cancel();
				playRecord();
			}
			 
		});
		 chooseFileDialog.setContentView(mFileList);
		 chooseFileDialog.setTitle("Please pick one file to play:");
		 chooseFileDialog.setCancelable(true);
		 chooseFileDialog.show();
		}else{
			Toast.makeText(getApplicationContext(), "Please record sounds first.", 1000).show();
		}
	}
	
	public void playRecord() {
		for (Sound s : mSounds) {
			final long blank = s.getBlankD();
			final long play = s.getSoundD();
			final int number = s.getNumber();

			try {
				Thread.sleep(blank);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mUtils.soundPlay(number);
			try {				
				Thread.sleep(play);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
