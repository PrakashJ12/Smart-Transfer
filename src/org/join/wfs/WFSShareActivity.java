package org.join.wfs;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.join.wfs.server.WebService;
import org.join.wfs.util.CopyUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WFSShareActivity extends Activity implements OnCheckedChangeListener {

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
		
		ArrayList<Uri> myUris = getFileUris();
		if (myUris == null || myUris.size() == 0) {
			finish();
			return;
		}
		
		urlText.setText("File(s): " + Uri.decode(myUris.toString()));
	}

	private void initViews() {
		toggleBtn = (ToggleButton) findViewById(R.id.toggleBtn);
		toggleBtn.setOnCheckedChangeListener(this);
		urlText = (TextView) findViewById(R.id.urlText);
	}

	private void initFiles() {
		new CopyUtil(this).assetsCopy();
	}

	private ArrayList<Uri> getFileUris() {
		Intent dataIntent = getIntent();
		ArrayList<Uri> theUris = new ArrayList<Uri>();

		if (Intent.ACTION_SEND_MULTIPLE.equals(dataIntent.getAction())) {
			ArrayList<Parcelable> list = dataIntent
					.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			if (list != null) {
				for (Parcelable parcelable : list) {
					Uri stream = (Uri) parcelable;
					if (stream != null) {
						theUris.add(stream);
					}
				}
			}
			return theUris;
		}

		Bundle extras = dataIntent.getExtras();

		Uri myUri = (Uri) extras.get(Intent.EXTRA_STREAM);

		if (myUri == null) {
			String tempString = (String) extras.get(Intent.EXTRA_TEXT);
			if (tempString == null) {
				Toast.makeText(this, "Error obtaining the file path...",
						Toast.LENGTH_LONG).show();
				return null;
			}

			myUri = Uri.parse(tempString);

			if (myUri == null) {
				Toast.makeText(this, "Error obtaining the file path",
						Toast.LENGTH_LONG).show();
				return null;
			}
		}

		theUris.add(myUri);
		return theUris;
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			String ip = getLocalIpAddress();
			if (ip == null) {
				Toast.makeText(this, R.string.msg_net_off, Toast.LENGTH_SHORT)
						.show();
				urlText.setText("");
			} else {
				startService(intent);
				urlText.setText("http://" + ip + ":" + WebService.PORT + "/");
			}
		} else {
			stopService(intent);
			urlText.setText("");
		}
	}

	/** Get the current IP address 获取当前IP地址 */
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