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

import com.networking.nemo.enums.NetworkErrorReason;

/**
 * Simple network error class.
 * 
 * @author Niko Rehnbäck
 * 
 */
public class JsonNetworkRequestError {

	private int mHttpStatusCode;
	private NetworkErrorReason mReason;

	public int getHttpStatusCode() {
		return mHttpStatusCode;
	}

	public void setHttpStatusCode(int httpStatusCode) {
		mHttpStatusCode = httpStatusCode;
	}

	public NetworkErrorReason getReason() {
		return mReason;
	}

	public void setReason(NetworkErrorReason reason) {
		mReason = reason;
	}
}
