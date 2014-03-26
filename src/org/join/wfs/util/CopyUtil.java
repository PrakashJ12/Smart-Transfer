package org.join.wfs.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

public class CopyUtil {

	private AssetManager manager;

	public CopyUtil(Context context) {
		manager = context.getAssets();
	}

	public boolean assetsCopy() {
		try {
			assetsCopy("wfs", Environment.getExternalStorageDirectory()
					+ "/.wfs"); // the assets. wfs file not found ==
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void assetsCopy(String assetsPath, String dirPath)
			throws IOException {
		String[] list = manager.list(assetsPath);
		if (list.length == 0) { // File
			InputStream in = manager.open(assetsPath);
			File file = new File(dirPath);
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileOutputStream fout = new FileOutputStream(file);
			/* Copy*/
			byte[] buf = new byte[1024];
			int count;
			while ((count = in.read(buf)) != -1) {
				fout.write(buf, 0, count);
				fout.flush();
			}
			/* Close*/
			in.close();
			fout.close();
		} else { // Directory
			for (String path : list) {
				assetsCopy(assetsPath + "/" + path, dirPath + "/" + path);
			}
		}
	}

}
