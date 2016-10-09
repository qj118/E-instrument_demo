package com.lge.e_instrument;

import java.util.HashMap;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class MusicUtils {
	
	int mMusic[];
	
	SoundPool mSoundPool;
	HashMap<Integer, Integer> mSoundPoolMap;
	
	@SuppressLint("UseSparseArrays")
	@SuppressWarnings("deprecation")
	public MusicUtils(Context context, int music[])
	{
		mMusic = music;
		mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		mSoundPoolMap = new HashMap<Integer, Integer>();
		for(int i = 0;i < mMusic.length; i++)
		{
			mSoundPoolMap.put(i, mSoundPool.load(context, mMusic[i], 1));
		}
	}
	
	public int soundPlay(int number)
	{
		return mSoundPool.play(mSoundPoolMap.get(number), 100, 100, 1, 0, 1.0f);
	}
	
	public int soundOver()
	{
		return mSoundPool.play(mSoundPoolMap.get(1), 100,100, 1, 0, 1.0f);
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		mSoundPool.release();
		super.finalize();
	}

}
