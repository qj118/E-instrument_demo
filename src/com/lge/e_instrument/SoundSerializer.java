package com.lge.e_instrument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import android.annotation.SuppressLint;
import android.content.Context;

public class SoundSerializer {

//	private Context mContext;
	private String mFilename;
	private File mFile;
	private File mDestDir;
	
	@SuppressLint("SdCardPath")
	public SoundSerializer(Context c, String f, File destDir)
	{
//		mContext = c;
		mFilename = f;
		mDestDir = destDir; 
		mFile = new File(mDestDir, mFilename);
	}
	
	public void saveSounds(ArrayList<Sound> sounds)
	          throws JSONException, IOException
	{
	    JSONArray array = new JSONArray();
	    for(Sound s : sounds)
	    {
	        array.put(s.toJSON());
	    }
	    Writer writer = null;
	    try{
	        OutputStream out = new FileOutputStream(mFile);
	        writer = new OutputStreamWriter(out);
	        writer.write(array.toString());
	    }finally{
	        if(writer != null)
	            writer.close();
	    }
	}
	
	public ArrayList<Sound> loadSounds() throws IOException, JSONException
	{
		ArrayList<Sound> sounds = new ArrayList<Sound>();
		BufferedReader reader = null;
		try{
			InputStream in = new FileInputStream(mFile);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null)
			{
				jsonString.append(line);
			}
			
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
			for(int i = 0; i < array.length();i++)
			{
				sounds.add(new Sound(array.getJSONObject(i)));
			}
		}catch (FileNotFoundException e)
		{}finally{
			if(reader != null)
				reader.close();
		}
		return sounds;
	}
}
