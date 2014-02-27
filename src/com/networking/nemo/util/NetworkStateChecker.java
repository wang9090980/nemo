/*
 * Copyright (C) 2014 Niko Rehnbäck
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networking.nemo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Simple helper class to check whether network is connected or not.
 * 
 * @author Niko Rehnbäck
 * 
 */
public class NetworkStateChecker {

	private static NetworkStateChecker sNetworkStateChecker;
	private Context mAppContext;

	private NetworkStateChecker(Context appContext) {
		mAppContext = appContext;
	}

	public static NetworkStateChecker getInstance(Context appContext) {
		if (sNetworkStateChecker == null) {
			sNetworkStateChecker = new NetworkStateChecker(appContext);
		}

		return sNetworkStateChecker;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException(
				"Cloning NetworkStateChecker is not allowed.");
	}

	public boolean isNetworkConnected() {
		ConnectivityManager connectivity = (ConnectivityManager) mAppContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();

			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
