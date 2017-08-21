package com.gpstrace.dlrc.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BluetoothFileTransfer {
	
	
	private BluetoothChatService mBCS;
	
	//private int mRole = 0; // 0 client 1 server
	
	private OnFileTransListener mEvtListener = null;
	
	public interface OnFileTransListener
	{
		
		public abstract void onFileTransferCompleted(int error);
		public abstract void onFileReceived(String path);
		public abstract void onMessage(String msg);
		public abstract void onStatusChanged(int status);
	}
	
	private int mSendFileStatus = -1;
	private static final int S_FT_ERROR=-1;
	private static final int S_FT_READY = 0;
	private static final int S_FT_SEND_COMPLETE=1;
	private static final int S_FT_SEND_FILE_INFO = 2;
	private static final int S_FT_SEND_FILE_DATA = 3;
	
	private int mRcvFileStatus = S_FT_RCV_FILE_INFO;
	private long mRcvFileSize = 0;
	private long mRcvFileSizeReceived = 0;
	private String mRcvFileName = "";
	private String mStoragePath = "";
	private static final int S_FT_RCV_FILE_INFO = 0;
	private static final int S_FT_RCV_FILE_DATA = 1;
	private static final int S_FT_RCV_FILE_COMPLETE = 1;
	
	private String mSendFilePath = "";
	private BufferedOutputStream mOutStream = null;
	private BufferedInputStream  mInStream = null;
	public Context mContext;
	
	private long mSendFileSize = 0;
	private int _role = 0;
	public BluetoothFileTransfer(Context context, OnFileTransListener lis, int role)
	{
		_role =role;
		mEvtListener = lis;
		mContext = context;
		mBCS = new BluetoothChatService(mContext,mHandler,role);
		
	}
	
	Handler mHandler = new Handler()
	{
		 public void handleMessage(Message msg) {   
             switch (msg.what) {   
                  case Constants.MESSAGE_DEVICE_NAME:
//					  Toast.makeText(mContext,"Find Phone",Toast.LENGTH_SHORT).show();
                       break;
                  case Constants.MESSAGE_READ:
                  {
                	 
                	  mEvtListener.onMessage("MESSAGE_READ");
                	  if(mSendFileStatus==S_FT_SEND_FILE_INFO && _role==0)	
                	  {

                		  byte[] data = (byte[]) msg.obj;
                		  int size = msg.arg1;
						  //BEGIN:Luk add status for BT connection
						  int connectStatus=msg.arg2;
						  if(connectStatus!=0) {
							  mEvtListener.onFileTransferCompleted(-1);
							  break;
						  }
                		  String sMsg = new String(data,0, size);
                		  if(sMsg.compareTo("ok")==0)
                		  {
                			  mSendFileStatus=S_FT_SEND_FILE_DATA;
                		  }
                		  
                		  // send data
                		  Runnable r = new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								byte[] rData = new byte[1024*10];
								
								long sentSize = 0;
								while(true)
								{
									int nRead;
									try {
										nRead = mInStream.read(rData);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										mEvtListener.onFileTransferCompleted(-1);
										return;
									}
									 if(nRead==-1)
									 {
										 if(sentSize == mSendFileSize)
										 {
											 mEvtListener.onFileTransferCompleted(0);
										 }
										 else
										 {
											 mEvtListener.onFileTransferCompleted(-1);
										 }
										 break;
									 }
									 
									 if(nRead==rData.length)
									 {
										 int res = mBCS.write(rData);
										 if(res != 0){
											 mEvtListener.onFileTransferCompleted(-1);
											 break;
										 }
										 
									 }
									 else
									 {
										 byte[] data = new byte[nRead];
										 for(int i=0; i<nRead; i++)
											 data[i] = rData[i];
										 
										 int res = mBCS.write(data);
										 if(res != 0){
											 mEvtListener.onFileTransferCompleted(-1);
											 break;
										 }
									 }
									 
									 sentSize += nRead;
								}
							}

                		  };

                		 new Thread(r).start();

                	  }
                	  else if( _role==1 )
                	  {
                		  if(mRcvFileStatus==S_FT_RCV_FILE_INFO)
                		  {
                			  //response "ok"
                			  byte[] data = (byte[]) msg.obj;
                    		  int size = msg.arg1;


                    		  String sMsg = new String(data,0, size);

                    		  int pos = sMsg.indexOf("=");
                    		  String value0 = sMsg.substring(0, pos);
                    		  if(value0.compareTo("name")==0)
                    		  {
                    			  String strRemain = sMsg.substring(pos+1);

                    			  int pos2 = strRemain.indexOf("=");

                    			  String vName = strRemain.substring(0,pos2);
                    			  String vSize = strRemain.substring(pos2+1);

                    			  mRcvFileName= vName;
                    			  mRcvFileSize = Integer.valueOf(vSize);
                    			  mRcvFileStatus = S_FT_RCV_FILE_DATA;
                    			  String ok = "ok";
                    			  mBCS.write(ok.getBytes());
                    			  mRcvFileSizeReceived = 0;
                    			  try {
                    				  //File f = new File()

									mOutStream = new BufferedOutputStream(new FileOutputStream(mStoragePath+"/"+mRcvFileName));
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                    		  }
                		  }
                		  else if (mRcvFileStatus == S_FT_RCV_FILE_DATA)
                		  {
                			  byte[] data = (byte[]) msg.obj;
                    		  try {
								mOutStream.write(data,0, msg.arg1);
								mRcvFileSizeReceived += msg.arg1;

								if(mRcvFileSizeReceived >=mRcvFileSize)
								{
									mOutStream.close();
									mRcvFileStatus = S_FT_RCV_FILE_INFO;

									if(mEvtListener!= null)
									{
										mEvtListener.onFileReceived(mRcvFileName);
									}
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                		  }
                	  }
                  }
                	  break;
                  case Constants.MESSAGE_STATE_CHANGE:
                  {
                	  if(msg.arg1==BluetoothChatService.STATE_CONNECTED )
                	  {
                		  // connect to server ok
						  Toast.makeText(mContext,"Connect ",Toast.LENGTH_SHORT).show();
                		  mEvtListener.onMessage("connected");
                		  //if(mRole==0)
                		 // {
                		//	  sendFile(Environment.getExternalStorageDirectory()+"/wzhtest/report.txt");
                		  //}

          				//mBtFileTrans.asServer(););

                		  if(_role==0)
                		  {
                			  mRcvFileStatus = S_FT_RCV_FILE_INFO;
                		  }
                	  }
                	  else{
                		  mEvtListener.onMessage("shit");
                	  }
                	  mEvtListener.onStatusChanged(msg.arg1);

                  }

                	  break;
                  case Constants.MESSAGE_TOAST:

                  {

                	  Bundle b = msg.getData();
                	  String t = b.getString(Constants.TOAST);
                      Toast.makeText(mContext, t, Toast.LENGTH_LONG).show();

                  }
                	  break;
                  case Constants.MESSAGE_WRITE:

                	  break;
             }
             super.handleMessage(msg);
        }
	};


	public void asServer()
	{

		_role = 1;
		mBCS.start(1);


	}

	public void asClient(BluetoothDevice device)
	{
		_role = 0;
		mBCS.start(0);
		mBCS.connect(device, false);

	}

	public void stop()
	{
		mBCS.getHandler().removeCallbacks(mBCS._restart);//remove mBCS Callbacks,this may not stop after onDestroy Luk
		mBCS.stop();
	}

	public int sendFile(String path)
	{
		if(_role==1)
			return -1;
//		Log.d("LUKIN","BluetoothChatService="+mBCS.getState());
		if(mBCS.getState() != BluetoothChatService.STATE_CONNECTED)
			return -2;

		mSendFilePath = path;

		File sf = new File(mSendFilePath);

		if(!sf.exists() || !sf.isFile())
			return -3;

		String  fName = sf.getName();
		long    fSize= sf.length();

		String fileInfo = "name="+fName+"="+fSize;

		try {
			mInStream = new BufferedInputStream(new FileInputStream(mSendFilePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -4;
		}

		mSendFileSize = fSize;
		int res = mBCS.write(fileInfo.getBytes());
		if(res==0){
			mSendFileStatus = S_FT_SEND_FILE_INFO;
			return 0;
		}else{
			return -5;
		}
	}
	
	public void setStoragePath(String path)
	{
		File fp = new File(path);
		if(!fp.exists())
		{
			fp.mkdir();
		}
		mStoragePath = path;
		 //String directoryPath = Environment.getExternalStorageDirectory().getPath() + "/GPSTest";
	}
}
