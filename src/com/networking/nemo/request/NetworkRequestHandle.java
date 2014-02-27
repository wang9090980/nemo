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

package com.networking.nemo.request;

import com.networking.nemo.network.NetworkRequestManager;

/**
 * Provides methods for retrieving {@link JsonNetworkRequest} status and
 * canceling all {@link JsonNetworkRequest} associated to handle.
 * 
 * @author Niko Rehnbäck
 * 
 */
public class NetworkRequestHandle {

	private int[] mRequestIds;
	private NetworkRequestManager mNetworkRequestManager;

	public NetworkRequestHandle(NetworkRequestManager networkRequestManager) {
		mNetworkRequestManager = networkRequestManager;
	}

	/**
	 * Cancels all NetworkRequest associated with this handle.
	 */
	public void cancel() {
		mNetworkRequestManager.onNetworkRequestsCanceled(mRequestIds);
	}

	public void setRequestIds(int[] requestIds) {
		mRequestIds = requestIds;
	}
}
