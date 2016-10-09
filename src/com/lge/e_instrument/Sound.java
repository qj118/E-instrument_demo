package com.lge.e_instrument;

import org.json.JSONException;
import org.json.JSONObject;

public class Sound {
	
	private static final String JSON_NUM = "number";
	private static final String JSON_DOWN_DURATION = "sound duration";
	private static final String JSON_BLANK_DURATION = "blank duration";
	
	private int mNumber;
	private long mSoundD;
	private long mBlankD;
	
	public Sound()
	{}
	
	public Sound(JSONObject json) throws JSONException
	{
		mNumber = json.getInt(JSON_NUM);
		mSoundD = json.getLong(JSON_DOWN_DURATION);
		mBlankD = json.getLong(JSON_BLANK_DURATION);
	}

	public JSONObject toJSON() throws JSONException
	{
	    JSONObject json = new JSONObject();
	    json.put(JSON_NUM, mNumber);
	    json.put(JSON_DOWN_DURATION, mSoundD);
	    json.put(JSON_BLANK_DURATION, mBlankD);
	    return json;
	}

	public int getNumber() {
		return mNumber;
	}

	public void setNumber(int number) {
		mNumber = number;
	}

	public long getSoundD() {
		return mSoundD;
	}

	public void setSoundD(long soundD) {
		mSoundD = soundD;
	}

	public long getBlankD() {
		return mBlankD;
	}

	public void setBlankD(long blankD) {
		mBlankD = blankD;
	}
}
