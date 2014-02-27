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

package com.networking.nemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.networking.nemo.enums.NetworkState;
import com.networking.nemo.util.NemoBus;

public class NetworkStateReceiver extends BroadcastReceiver {

	private NetworkState mState = null;

	public NetworkStateReceiver() {
		NemoBus.getInstance().register(this);
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent.getExtras() != null) {
			final ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo networkInfo = connectivityManager
					.getActiveNetworkInfo();

			if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
				// Connected
				if (networkInfo.isConnected()) {
					mState = NetworkState.CONNECTED;
				}
			} else if (intent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
				// No connection
				mState = NetworkState.DISCONNECTED;
			}
		}

		if (mState != null) {
			NemoBus.getInstance().post(mState);
		}

		NemoBus.getInstance().unregister(this);
	}
}
