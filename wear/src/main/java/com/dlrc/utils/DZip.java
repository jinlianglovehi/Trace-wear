package com.dlrc.utils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.*;
import android.os.AsyncTask;

/**
 * 
 * @author wangz
 example :  DZip.unzipAsync(Environment.getExternalStorageDirectory()+"/wzhtest/13.zip", Environment.getExternalStorageDirectory()+"/wzhtest/14.bin",new DZipListener(){

						@Override
						public void onComplete(int error, String input, String output) {
							Log.e("wzh", "unzip complete:"+error + " input="+input+" output="+output);
						}

						@Override
						public void onProgress(int progress) {
							// TODO Auto-generated method stub
							
						}});;
 */
public class DZip {
	
	public interface DZipListener
	{
		public void onComplete(int error, String input, String output);
		public void onProgress( int progress);
	}
	
	
	
	private DZip()
	{
			
	}
	
/**
 * zip file synchronically
 * @param inputPath: input file path
 * @param outputPath: output file path
 * @throws FileNotFoundException
 */
	public static void zipFile(String inputPath, String outputPath) throws FileNotFoundException
	{
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		FileInputStream is = null;
		GZIPOutputStream gop = null;
		try {
			is = new FileInputStream(inputPath);
			
			gop = new GZIPOutputStream(aos);
			
			byte[] buffer = new byte[1024*20];
			int len =0;
			while((len = is.read(buffer)) != -1)
			{
				gop.write(buffer, 0, len);
			}
			
			gop.finish();
			gop.close();
		
			FileOutputStream fos = new FileOutputStream(outputPath);
			aos.writeTo(fos);
			aos.close();
			fos.close();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	/**
	 * unzip file synchronically
	 * @param inputPath: input file path
	 * @param outputPath: output file path
	 *
	 */
	public static void unzipFile(String inputPath, String outputPath)
	{	
		
		FileOutputStream os = null;
		FileInputStream is = null;
		GZIPInputStream gip = null;
		try {
			
			is = new FileInputStream(inputPath);
			os = new FileOutputStream(outputPath);
			//ByteArrayInputStream aos = new ByteArrayInputStream(is);
			
			
			gip = new GZIPInputStream(is);
			
			byte[] buffer = new byte[1024*20];
			int len =0;
				
			while((len=gip.read(buffer))>0)
			{
				os.write(buffer, 0, len);
			}
			gip.close();
			os.close();
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	/**zip gzip file asynchronically
	 * 
	 * @param input: input file path
	 *             
	 * @param output: output file path
	 * 
	 * @param lis: event listener
	 */
	public static void zipAsync(String input, String output,DZipListener lis)
	{
		ZipAsync za = new ZipAsync(lis);
		
		za.execute(input, output);
	}
	/**
	 * unzip gzip file asynchronically
	 * 
	 * @param input: input file path
	 * @param output: output file path
	 * @param lis: event listener
	 */
	public static void unzipAsync(String input, String output, DZipListener lis)
	{
		UnzipAsync za = new UnzipAsync(lis);
		
		za.execute(input, output);
	}
	
	
	
	
	 static class ZipAsync extends AsyncTask<String, Integer, Integer>{

	
			String mInput;
			String mOutput;
			DZipListener mLis = null;
			public ZipAsync(DZipListener lis){
				super();
				mLis= lis;
			}
		
			protected Integer doInBackground(String... params) {
				if(params[0] != null)
					mInput = params[0];
				if(params[1] != null)
					mOutput = params[1];
				
				if(mInput==null || mOutput == null)
					return -1;
				
				File fInput = new File(mInput);
				if(!fInput.exists())
					return -2;
				if(!fInput.isFile())
					return -3;
			
				ByteArrayOutputStream aos = new ByteArrayOutputStream();
				FileInputStream is = null;
				GZIPOutputStream gop = null;
				try {
					is = new FileInputStream(mInput);
					
					
					gop = new GZIPOutputStream(aos);
					
					byte[] buffer = new byte[1024*20];
					int len =0;
					while((len = is.read(buffer)) != -1)
					{
						gop.write(buffer, 0, len);
					}
					
					gop.finish();
					gop.close();
				
					FileOutputStream fos = new FileOutputStream(mOutput);
					aos.writeTo(fos);
					aos.close();
					fos.close();
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					return -1;
					
				}
				
				return 0;
			}
	
			protected void onPostExecute(Integer result) {	
				super.onPostExecute(result);
				if(mLis != null)
					mLis.onComplete(result, mInput, mOutput);				
			}
			@Override
			protected void onPreExecute() {
	
				super.onPreExecute();
			}
			
			protected void onProgressUpdate(Integer... values){
				
			}
		}

	
	static class UnzipAsync extends AsyncTask<String, Integer, Integer>{

		String mInput = null;
		String mOutput = null;
		DZipListener mLis = null;
		
		public UnzipAsync(DZipListener lis){
			super();
			mLis= lis;
		}
	
		protected Integer doInBackground(String... params) {
			
			if(params[0] != null)
				mInput = params[0];
			if(params[1] != null)
				mOutput = params[1];
			
			if(mInput==null || mOutput == null)
				return -1;
			
			File fInput = new File(mInput);
			if(!fInput.exists())
				return -2;
			if(!fInput.isFile())
				return -3;
			
			FileOutputStream os = null;
			FileInputStream is = null;
			GZIPInputStream gip = null;
			try {
				
				is = new FileInputStream(mInput);
				os = new FileOutputStream(mOutput);
				//ByteArrayInputStream aos = new ByteArrayInputStream(is);
				
				
				gip = new GZIPInputStream(is);
				
				byte[] buffer = new byte[1024*20];
				int len =0;
					
				while((len=gip.read(buffer))>0)
				{
					os.write(buffer, 0, len);
				}
				gip.close();
				os.close();
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
			
			return 0;
		}

		protected void onPostExecute(Integer result) {

			super.onPostExecute(result);
			
			if(mLis != null)
				mLis.onComplete(result, mInput, mOutput);
			

		}
		@Override
		protected void onPreExecute() {

		//	System.out.println("pretExecute------");
			super.onPreExecute();
		}
		
		protected void onProgressUpdate(Integer... values){
			
		}
	}
	
}
