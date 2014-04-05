package org.join.wfs;

import gravity.android.discovery.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.join.wfs.server.WebService;
import org.join.wfs.util.CopyUtil;



import android.R.bool;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WFSActivity extends Activity implements OnCheckedChangeListener {

	private ToggleButton toggleBtn;
	private TextView urlText;

	private Intent intent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initViews();
		initFiles();

		intent = new Intent(this, WebService.class);
	}

	private void initViews() {
		toggleBtn = (ToggleButton) findViewById(R.id.toggleBtn);
		toggleBtn.setOnCheckedChangeListener(this);
		urlText = (TextView) findViewById(R.id.urlText);
	}

	private void initFiles() {
		new CopyUtil(this).assetsCopy();
	}
	
	
	public class AsyncTaskSearchDownload extends AsyncTask<String, Void, ArrayList<DiscoveryServerInfo>>
	{

		@Override
		protected ArrayList<DiscoveryServerInfo> doInBackground(
				String... params) {
			// TODO Auto-generated method stub
			WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			//DiscoveryClient Client = new DiscoveryClient();
			ArrayList<DiscoveryServerInfo> userList = DiscoveryClient.findServer(wifiManager, "MYAPP_TOKEN");
			
			return userList;
		}

		@Override
		protected void onPostExecute(ArrayList<DiscoveryServerInfo> resultDiscoveryServerInfo) {
			// TODO Auto-generated method stub
			super.onPostExecute(resultDiscoveryServerInfo);
			
			
			
			if(resultDiscoveryServerInfo.isEmpty())
			{
				urlText.setText("No servers found");
			}
			for (DiscoveryServerInfo dsi : resultDiscoveryServerInfo)
			{
				urlText.setText(dsi.name);
				
				
				Toast.makeText(getApplicationContext(), "File is being transferred", Toast.LENGTH_SHORT).show();
//				String urlNew = "http://"+CommClass.ip+"/SystemControl/mobile/download_1.jsp?cmd_file_path="+send+"&file_name="+selected;
				String urlNew = "http://"+dsi.ip+":"+dsi.port+dsi.name;
				Log.d("File Network URL", urlNew);
				DownloadManager dm;
				dm=(DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				Request req=new Request(Uri.parse(urlNew));
				req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				//req.addRequestHeader("cmd_file_path", send);
				//req.addRequestHeader("file_name", selected);
				req.allowScanningByMediaScanner();
				
				String urlToFilename = dsi.name;
				//int idx = urlToFilename.replaceAll("\\", "/").lastIndexOf("/");
				int idx = urlToFilename.lastIndexOf("/");
				String filename = idx >= 0 ? urlToFilename.substring(idx + 1) : urlToFilename;
				
				req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
				req.setTitle("File Transfer");
				req.setDescription("Downloading Remote File");
				long en=dm.enqueue(req);
			}
		}

	
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		if (isChecked) {
			
			urlText.setText("");
			new AsyncTaskSearchDownload().execute("Test");
			
			
			
//			
//			String ip = getLocalIpAddress();
//			if (ip == null) {
//				Toast.makeText(this, R.string.msg_net_off, Toast.LENGTH_SHORT)
//						.show();
//				urlText.setText("");
//			} else {
//				startService(intent);
//				urlText.setText("http://" + ip + ":" + WebService.PORT + "/");
//			}
		} else {
			
			
			
//			stopService(intent);
			urlText.setText("");
			
		}
	}

	/** Get the current IP address  */
	private String getLocalIpAddress() {
		try {
			// Traverse the network interface
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				// Traverse the IP address
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					// Non-return return address
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onBackPressed() {
		if (intent != null) {
			stopService(intent);
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}