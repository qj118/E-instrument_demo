package com.lge.e_instrument;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
//import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class PianoActivity extends Activity {

	private ImageButton mButton[];
	private MusicUtils mUtils;
	private View mParent;
	private int mButtonId[];
	private boolean mHavePlayed[];
//	private int mCurrentKey;
//	private int mLastKey;
	private View mKeys;
	private boolean mRecordStarted = false;
	private int mMusic[] = { R.raw.do1, R.raw.re2, R.raw.mi3, R.raw.fa4, R.raw.sol5, R.raw.la6, R.raw.si7,
			R.raw.hduo8, R.raw.black1, R.raw.black2, R.raw.black3, R.raw.black4, R.raw.black5};
	
	private int mPressedKey[];
	
	private Sound mSound;
	private ArrayList<Sound> mSounds;
	private SoundSerializer mSoundSerializer;
	private long mSoundStart;
	private long mSoundEnd;
	private long mBlankStart;
	private long mBlankEnd;
	
	private ListView mFileList;
	private Dialog mChooseFileDialog;
	ArrayList<String> mFilesName;
	private File mDestDir;
	

	public Intent mPlayRecordIntent;
	public static String EXTRA_ID = "Number";
	public static String EXTRA_BLANK = "Blank";
	public static String EXTRA_PLAY = "Play";
//	private static Handler handlerPiano = new Handler();
	
	private MediaRecorder mRecorder;
	private boolean mIsMultiple = false;
	private File mMultipleFile;
	private CheckBox mMultipleTouch;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_piano);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mUtils = new MusicUtils(getApplicationContext(), mMusic);
		mDestDir = new File(Environment.getExternalStorageDirectory() + "/sounds");
		if(!mDestDir.exists())
		{
			mDestDir.mkdirs();
		}

		mButtonId = new int[13];
		mButtonId[0] = R.id.duo;
		mButtonId[1] = R.id.re;
		mButtonId[2] = R.id.mi;
		mButtonId[3] = R.id.fa;
		mButtonId[4] = R.id.sol;
		mButtonId[5] = R.id.la;
		mButtonId[6] = R.id.si;
		mButtonId[7] = R.id.hduo;
		mButtonId[8] = R.id.black1;
		mButtonId[9] = R.id.black2;
		mButtonId[10] = R.id.black3;
		mButtonId[11] = R.id.black4;
		mButtonId[12] = R.id.black5;

		mButton = new ImageButton[13];
		mHavePlayed = new boolean[13];

		for (int i = 0; i < mButton.length; i++) {
			mButton[i] = (ImageButton) findViewById(mButtonId[i]);
			mButton[i].setClickable(false);
			mHavePlayed[i] = false;
		}
		
		adjustLayout();

//		mCurrentKey = 0;
//		mLastKey = 0;
		mPressedKey = new int[5];
		for(int i = 0; i < mPressedKey.length; i++)
		{
			mPressedKey[i] = -1;
		}

		mParent = (View) findViewById(R.id.parent);
		mParent.setClickable(true);

		mParent.setOnTouchListener(new View.OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int temp;
				int tempIndex;
				int pointercount;
				
				pointercount = event.getPointerCount();
				for(int count = 0; count < pointercount; count++)
				{
					boolean moveflag = false;
					temp = isInAnyScale(event.getX(count), event.getY(count),mButton);
					if(temp != -1)
					{
						switch(event.getActionMasked())
						{
						case MotionEvent.ACTION_DOWN:
							
						case MotionEvent.ACTION_POINTER_DOWN:
							if(count != 0)
							{
								if(mPressedKey[count - 1] == -1)
								{
									mPressedKey[count - 1] = temp;
								}
								else{
									mPressedKey[count] = temp;
								}
							}else{
								mPressedKey[count] = temp;
							}
							if(!mHavePlayed[temp]){
								buttonDownColor(temp);
								mUtils.soundPlay(temp);
								mHavePlayed[temp] = true;
								if (mRecordStarted && !mIsMultiple) {
									mSound = new Sound();
									mSound.setNumber(temp);
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
							temp = mPressedKey[count];
							for(int i = temp + 1;i >= temp - 1; i--)
							{
								if(i < 0 || i >= mButton.length)
								{
									continue;
								}
								if(isInScale(event.getX(count),event.getY(count), mButton[i]))
								{
									moveflag = true;
									if(i != temp)
									{
										boolean laststill = false;
										boolean nextstill = false;
										mPressedKey[count] = -1;
										for(int j = 0;j < pointercount; j++)
										{
											if(mPressedKey[j] == temp)
											{
												laststill = true;
											}
											if(mPressedKey[j] == i)
											{
												nextstill = true;
											}
										}
										if(!nextstill)
										{
											buttonDownColor(i);
											mUtils.soundPlay(i);
											mHavePlayed[i] = true;
										}
										
										mPressedKey[count] = i;
										
										if(!laststill)
										{
											buttonUpColor(temp);
											mHavePlayed[temp] = false;
											if (mRecordStarted && !mIsMultiple) {
												mBlankStart = mSoundEnd = System.currentTimeMillis();
												mSound.setSoundD(mSoundEnd - mSoundStart);
												mSounds.add(mSound);
												mSound = new Sound();
												mSound.setNumber(i);
												mSoundStart = System.currentTimeMillis();
												mBlankEnd = mSoundStart;
												mSound.setBlankD(mBlankEnd - mBlankStart);
											}
										}
										break;
									}
								}
							}
							break;
						case MotionEvent.ACTION_UP:
						case MotionEvent.ACTION_POINTER_UP:
							tempIndex = event.getActionIndex();
							if(tempIndex == count)
							{
								boolean still = false;
								for(int t = count;t < 5;t++)
								{
									if(t != 4)
									{
										if(mPressedKey[t + 1] >= 0)
										{
											mPressedKey[t] = mPressedKey[t+1];
										}
										else{
											mPressedKey[t] = -1;
										}
									}else{
										mPressedKey[t] = -1;
									}
								}
								for(int i = 0; i < mPressedKey.length; i++)
								{
									if(mPressedKey[i] == temp)
									{
										still = true;
										break;
									}
								}
								if(!still)
								{
									buttonUpColor(temp);
									mHavePlayed[temp] = false;
									if (mRecordStarted && !mIsMultiple) {
										mBlankStart = mSoundEnd = System.currentTimeMillis();
										mSound.setSoundD(mSoundEnd - mSoundStart);
										mSounds.add(mSound);
									}
								}
								break;
							}
							
						}
					}
					if(event.getActionMasked() == MotionEvent.ACTION_MOVE && !moveflag)
					{
						if(mPressedKey[count] != -1)
						{
							buttonUpColor(mPressedKey[count]);
							mHavePlayed[mPressedKey[count]] = false;
							for(int t = count; t < 5; t++)
							{
								if(t != 4)
								{
									if(mPressedKey[t+1] >= 0)
									{
										mPressedKey[t] = mPressedKey[t+1];
									}else{
										mPressedKey[t] = -1;
									}
								}else{
									mPressedKey[t] = -1;
								}
							}
						}
					}
				}
				return false;
			}
		});
		mKeys = (View) findViewById(R.id.keys);
		mMultipleTouch = (CheckBox)findViewById(R.id.multiple_touch);
		mMultipleTouch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(!mRecordStarted){
					
				   mIsMultiple = arg1;
				}
			}
			
		});
	}

	private boolean isInScale(float x, float y, ImageButton button) {
		if (x > button.getLeft() && x < button.getRight() && y > button.getTop() + mKeys.getTop()
				&& y < button.getBottom() + mKeys.getTop()) {
			return true;
		} else {
			return false;
		}
	}

	private int isInAnyScale(float x, float y, ImageButton[] button) {
		for (int i = 0; i < button.length - 5; i++) {
			System.out.println(button[i].getLeft());
			System.out.println(button[i].getRight());
			if (x > button[i].getLeft() && x < button[i].getRight() && y > button[i].getTop() + mKeys.getTop()
					&& y < button[i].getBottom() + mKeys.getTop()) {
				for(int j = button.length - 5; j < button.length; j++)
				{
					if(x > button[j].getLeft() && x < button[j].getRight() && y > button[j].getTop() + mKeys.getTop()
					&& y < button[j].getBottom() + mKeys.getTop())
					{
						return j;
					}
				}
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.piano, menu);
		return true;
	}



	@SuppressLint("SimpleDateFormat")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.about:
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setView(LayoutInflater.from(this).inflate(R.layout.about_dialog, null))
					.setTitle("About")
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub

						}
					}).create();
			dialog.show();
			return true;

		case R.id.record:
			if (mRecordStarted == false) {
				item.setTitle(R.string.stop_record);
				mRecordStarted = true;
				mMultipleTouch.setClickable(false);
				SimpleDateFormat dateFormat;
				if(mIsMultiple)
				{
					try{
					  dateFormat = new SimpleDateFormat("yyyyMMDDHHmmss");
					  mMultipleFile = new File(mDestDir,  dateFormat.format(new Date()).toString() 
							  + ".amr");
					  mRecorder = new MediaRecorder();
					  mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				      mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					  mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
					  mRecorder.setOutputFile(mMultipleFile.getAbsolutePath());
					  mRecorder.prepare();
					  mRecorder.start();
					}catch(Exception e)
					{
						e.printStackTrace();
					}	
				}
				else
				{
					dateFormat = new SimpleDateFormat("yyyyMMDDHHmmss");
					mSounds = new ArrayList<Sound>();
					mSoundSerializer = new SoundSerializer(getApplicationContext(), 
							dateFormat.format(new Date()).toString() + ".json", mDestDir);
					mSoundStart = mSoundEnd = mBlankStart = mBlankEnd = 0;
				}
				
			} else {
				item.setTitle(R.string.record_sound);
				mRecordStarted = false;
				mMultipleTouch.setClickable(true);
				if(mIsMultiple)
				{
					if(mMultipleFile != null && mMultipleFile.exists()){
						mRecorder.stop();
						mRecorder.release();
						mRecorder = null;
					}
				}
				else
				{
					if(!mSounds.isEmpty()){
						  try {
							  mSoundSerializer.saveSounds(mSounds);
						  } catch (Exception e) {
							 e.printStackTrace();
						  }
						}
				}

			}
			return true;

		case R.id.next:
			
			Intent i = new Intent(this, AnotherActivity.class);
			startActivity(i);
			finish();
			
			return true;

		case R.id.load:
			
			obtainSoundsList();
//			startService(playRecordIntent);
//			new MyThreadRecord().start();
//			new MyThreadPiano().start();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void adjustLayout()
    {
    	DisplayMetrics metric = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metric);
    	int width = (int) (metric.widthPixels - 20)/8;
    	int height = (int) (metric.heightPixels * 4/5) * 5 / 6;
    	AbsoluteLayout.LayoutParams params;
    	for(int i = 0; i < 8; i++)
    	{
			params = (AbsoluteLayout.LayoutParams)mButton[i].getLayoutParams();
    		params.x = width * i;
    		params.width = width;
    		mButton[i].setLayoutParams(params);
    	}
    	
    	params = (AbsoluteLayout.LayoutParams)mButton[8].getLayoutParams();
    	params.x = width*4/7;
    	params.width = width * 2/3;
    	params.height = height/2;
    	mButton[8].setLayoutParams(params);
    	
    	params = (AbsoluteLayout.LayoutParams)mButton[9].getLayoutParams();
    	params.x = width * 3/4 + width;
    	params.width = width * 2/3;
    	params.height = height/2;
    	mButton[9].setLayoutParams(params);
    	
    	params = (AbsoluteLayout.LayoutParams)mButton[10].getLayoutParams();
    	params.x = width * 4/7 + 3*width;
    	params.width = width * 2/3;
    	params.height = height/2;
    	mButton[10].setLayoutParams(params);
    	
    	params = (AbsoluteLayout.LayoutParams)mButton[11].getLayoutParams();
    	params.x = width * 2/3 + 4*width;
    	params.width = width * 2/3;
    	params.height = height/2;
    	mButton[11].setLayoutParams(params);
    	
    	params = (AbsoluteLayout.LayoutParams)mButton[12].getLayoutParams();
    	params.x = width * 6/7 + 5*width;
    	params.width = width * 2/3;
    	params.height = height/2;
    	mButton[12].setLayoutParams(params);
    }
	
	@SuppressLint("ShowToast")
	private void obtainSoundsList()
	{
		File file = new File(Environment.getExternalStorageDirectory() + "/sounds");
		File[] soundFiles = file.listFiles();
		mChooseFileDialog = new Dialog(this);
		mFileList = new ListView(this);
	    mFilesName = new ArrayList<String>();
		if(soundFiles.length != 0)
		{
		  for(int i = 0; i < soundFiles.length; i++)
		  {
			mFilesName.add(soundFiles[i].getName().toString());
		  }
		 mFileList.setAdapter(new ArrayAdapter<String>
		 (this,android.R.layout.simple_expandable_list_item_1,mFilesName));
		 mFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				if(mFilesName.get(arg2).endsWith(".json"))
				{
				    mSoundSerializer = new SoundSerializer(getApplicationContext(), 
						mFilesName.get(arg2), mDestDir);
				    try {
					    mSounds = mSoundSerializer.loadSounds();
				    } catch (Exception e) {
					    e.printStackTrace();
				    }
				    mChooseFileDialog.cancel();
					playRecord();
				}
				else if(mFilesName.get(arg2).endsWith(".amr"))
				{
					mMultipleFile = new File(mDestDir, mFilesName.get(arg2));
					mChooseFileDialog.cancel();
					MediaPlayer mediaPlay = new MediaPlayer();
					try{
					  mediaPlay.setDataSource(mMultipleFile.getAbsolutePath().toString());
					  mediaPlay.prepare();
					  mediaPlay.start();
					}catch(Exception e)
					{
						e.printStackTrace();
					}
					
				}
			}
			 
		});
		 mFileList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				  File deleteFile = new File(mDestDir, mFilesName.get(arg2));
				  deleteFile.delete();
				  mFilesName.remove(arg2);
				  if(mFilesName.isEmpty())
				  {
					mChooseFileDialog.cancel();
				  }
				  ((ArrayAdapter<String>)mFileList.getAdapter()).notifyDataSetChanged();
				return false;
			}
		});
		 mChooseFileDialog.setContentView(mFileList);
		 mChooseFileDialog.setTitle("Please pick one file to play:");
		 mChooseFileDialog.setCancelable(true);
		 mChooseFileDialog.show();
		}
		else{
			Toast.makeText(getApplicationContext(), "Please record sounds first.", 1000).show();
		}
	}
	

	private void buttonDownColor(int i) {
		switch (i) {
		case 0:
			mButton[i].setImageResource(R.drawable.duo2);
			break;
		case 1:
			mButton[i].setImageResource(R.drawable.re2);
			break;
		case 2:
			mButton[i].setImageResource(R.drawable.mi2);
			break;
		case 3:
			mButton[i].setImageResource(R.drawable.fa2);
			break;
		case 4:
			mButton[i].setImageResource(R.drawable.sol2);
			break;
		case 5:
			mButton[i].setImageResource(R.drawable.la2);
			break;
		case 6:
			mButton[i].setImageResource(R.drawable.si2);
			break;
		case 7:
			mButton[i].setImageResource(R.drawable.hduo2);
			break;
		case 8:
			mButton[i].setImageResource(R.drawable.back1);
			break;
		case 9:
			mButton[i].setImageResource(R.drawable.back2);
			break;
		case 10:
			mButton[i].setImageResource(R.drawable.back3);
			break;
		case 11:
			mButton[i].setImageResource(R.drawable.back4);
			break;
		case 12:
			mButton[i].setImageResource(R.drawable.back5);
			break;
		}
	}

	private void buttonUpColor(int i) {
		switch (i) {
		case 0:
			mButton[i].setImageResource(R.drawable.duo1);
			break;
		case 1:
			mButton[i].setImageResource(R.drawable.re1);
			break;
		case 2:
			mButton[i].setImageResource(R.drawable.mi1);
			break;
		case 3:
			mButton[i].setImageResource(R.drawable.fa1);
			break;
		case 4:
			mButton[i].setImageResource(R.drawable.sol1);
			break;
		case 5:
			mButton[i].setImageResource(R.drawable.la1);
			break;
		case 6:
			mButton[i].setImageResource(R.drawable.si1);
			break;
		case 7:
			mButton[i].setImageResource(R.drawable.hduo1);
			break;
		case 8:
			mButton[i].setImageResource(R.drawable.black1);
			break;
		case 9:
			mButton[i].setImageResource(R.drawable.black2);
			break;
		case 10:
			mButton[i].setImageResource(R.drawable.black3);
			break;
		case 11:
			mButton[i].setImageResource(R.drawable.black4);
			break;
		case 12:
			mButton[i].setImageResource(R.drawable.black5);
			break;
		}
	}
	
	

	private void playRecord() {
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
	
	/*public void playPiano()
	{
		for (Sound s : mSounds) {
			final long blank = s.getBlankD();
			final long play = s.getSoundD();
			final int number = s.getNumber();

			try {
				Thread.sleep(blank);
				
			} catch (InterruptedException e) {
				Log.e("PlayRecord", "", e);
			}
			
			buttonDownColor(number);
			
			Log.i("PlayRecord", "Play " + s.getNumber() + " " + blank + " " + play);
			
			
			try {				
				Thread.sleep(play);
			} catch (InterruptedException e) {
				Log.e("PlayRecord", "", e);
			}
		    buttonUpColor(number);
		}
	}
	
	private void putPlayRecordExtras()
	{
		final int size = mSounds.size();
		long mBlank[] = new long[size];
		int mNum[] = new int[size];
		long mPlay[] = new long[size];
		
		for(int i = 0; i < size;i ++)
		{
			mNum[i] = mSounds.get(i).getNumber();
			mBlank[i] = mSounds.get(i).getBlankD();
			mPlay[i] = mSounds.get(i).getSoundD();
		}
		
		playRecordIntent.putExtra(EXTRA_ID, mNum);
		playRecordIntent.putExtra(EXTRA_BLANK, mBlank);
		playRecordIntent.putExtra(EXTRA_PLAY, mPlay);
	}
	
	class MyThreadPiano extends Thread{
		
		@Override
		public void run()
		{
			handlerPiano.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					playPiano();
				}
			});
		}
	}
	
    class MyThreadRecord extends Thread{
		
		@Override
		public void run()
		{
			playRecord();
		}
	}*/
}
