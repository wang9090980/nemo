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

import java.util.HashMap;
import java.util.Map;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;
import com.networking.nemo.network.NetworkRequestListener;

/**
 * Simple class of Json network request.
 * 
 * @author Niko Rehnbäck
 * @param <T>
 *            Class which is expected to be the result of successful network
 *            request.
 * @param <K>
 *            Class which is expected to be the result of failed network
 *            request.
 */

public class JsonNetworkRequest<T, K extends JsonNetworkRequestError> {

	private int mId;
	private int mDependsOnId = -1;
	private int mStartIdOfQueue = -1;
	private int mMethod;
	private String mUrl;
	private Object mJsonBody;
	private Class<T> mClassOfSuccessfulObject;
	private Class<K> mClassOfFailedObject;
	private NetworkRequestListener<T, K> mListener;
	private Map<String, String> mHeaders = new HashMap<String, String>();

	public JsonNetworkRequest(final int method, final String url,
			final Object jsonBody, final Class<T> classOfSuccessfulObject,
			final Class<K> classOfFailedObject,
			NetworkRequestListener<T, K> listener) {
		mMethod = method;
		mUrl = url;
		mJsonBody = jsonBody;
		mClassOfSuccessfulObject = classOfSuccessfulObject;
		mClassOfFailedObject = classOfFailedObject;
		mListener = listener;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public int getDependsOnId() {
		return mDependsOnId;
	}

	public void setDependsOnId(int dependsOnId) {
		mDependsOnId = dependsOnId;
	}

	public int getStartIdOfQueue() {
		return mStartIdOfQueue;
	}

	public void setStartIdOfQueue(int startIdOfQueue) {
		mStartIdOfQueue = startIdOfQueue;
	}

	public int getMethod() {
		return mMethod;
	}

	public void setMethod(int method) {
		mMethod = method;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public Object getJsonBody() {
		return mJsonBody;
	}

	public void setJsonBody(Object jsonBody) {
		mJsonBody = jsonBody;
	}

	public Class<T> getClassOfSuccessfulObject() {
		return mClassOfSuccessfulObject;
	}

	public void setClassOfSuccessfulObject(Class<T> classOfSuccessfulObject) {
		mClassOfSuccessfulObject = classOfSuccessfulObject;
	}

	public Class<K> getClassOfFailedObject() {
		return mClassOfFailedObject;
	}

	public void setClassOfFailedObject(Class<K> classOfFailedObject) {
		mClassOfFailedObject = classOfFailedObject;
	}

	public void setListener(final NetworkRequestListener<T, K> listener) {
		mListener = listener;
	}

	public NetworkRequestListener<T, K> getListener() {
		return mListener;
	}

	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	public void setHeaders(Map<String, String> headers) {
		mHeaders = headers;
	}

	/**
	 * Method for setting RetryPolicy to the request.
	 * 
	 * @return {@link DefaultRetryPolicy} by default
	 */
	public RetryPolicy getRetryPolicy() {
		return new DefaultRetryPolicy();
	}

	/**
	 * Method for setting caching of request.
	 * 
	 * @return true, if the request should be cached by {@link Volley}.
	 */
	public boolean getShouldCache() {
		return true;
	}
}
