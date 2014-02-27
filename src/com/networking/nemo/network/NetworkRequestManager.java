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

package com.networking.nemo.network;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.networking.nemo.deserializer.JsonKeyNotFoundExpection;
import com.networking.nemo.deserializer.JsonRequiredDeserializer;
import com.networking.nemo.enums.NetworkErrorReason;
import com.networking.nemo.request.JsonNetworkRequest;
import com.networking.nemo.request.JsonNetworkRequestError;
import com.networking.nemo.request.NetworkRequestHandle;
import com.networking.nemo.request.ValidatingJsonNetworkRequest;
import com.networking.nemo.util.NemoLog;
import com.networking.nemo.util.NetworkStateChecker;

/**
 * Class providing user interface for running network requests.
 * 
 * @author Niko Rehnbäck
 * 
 */
public class NetworkRequestManager {

	// Singleton
	private static NetworkRequestManager sNetworkRequestManager;

	// Maximum count of Requests allowed to run simultaneously
	private int mMaxRunningRequestCount = Integer.MAX_VALUE;

	private LinkedBlockingQueue<JsonNetworkRequest<?, JsonNetworkRequestError>> mRequestQueue = new LinkedBlockingQueue<JsonNetworkRequest<?, JsonNetworkRequestError>>();
	private Map<Integer, JsonNetworkRequest<?, JsonNetworkRequestError>> mRunningRequests = new ConcurrentHashMap<Integer, JsonNetworkRequest<?, JsonNetworkRequestError>>(
			16, 0.75f, mMaxRunningRequestCount);
	private AtomicInteger mNetworkRequestIdGenerator = new AtomicInteger();

	private NetworkRequestCookieManager mNetworkRequestCookieManager;
	private NetworkStateChecker mNetworkStateChecker;
	private RequestQueue mVolleyQueue;

	private Handler mNetworkRequestHandler = new Handler(Looper.getMainLooper());

	private NetworkRequestManager(final Context appContext) {
		mNetworkRequestCookieManager = new NetworkRequestCookieManager();
		mNetworkStateChecker = NetworkStateChecker.getInstance(appContext);
		mVolleyQueue = Volley.newRequestQueue(appContext);
	}

	public static NetworkRequestManager getInstance(final Context appContext) {
		if (appContext == null) {
			throw new IllegalArgumentException(
					"Parameter appContext cannot be null.");
		}

		if (sNetworkRequestManager == null) {
			sNetworkRequestManager = new NetworkRequestManager(appContext);
		}

		return sNetworkRequestManager;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException(
				"Cloning NetworkRequestManager is not allowed.");
	}

	public void setMaxRunningRequestCount(final int maxRunningRequestCount) {
		mMaxRunningRequestCount = maxRunningRequestCount;
	}

	public void clearCookies() {
		mNetworkRequestCookieManager.clearAllCookies();
	}

	public void setLogTag(final String tag) {
		NemoLog.setTag(tag);
	}

	/**
	 * Executes the NetworkRequest and notifies caller when finished.
	 * 
	 * @param jsonNetworkRequest
	 *            BaseJsonNetworkRequest to be executed.
	 * @return {@link NetworkRequestHandle}
	 */
	public synchronized NetworkRequestHandle executeJsonRequest(
			final JsonNetworkRequest<?, ?> jsonNetworkRequest) {
		// Skip null request
		if (jsonNetworkRequest == null) {
			return null;
		}

		// Get unique id for network request
		final int id = getUniqueNetworkRequestId();

		// Set unique id for request
		jsonNetworkRequest.setId(id);

		// Set request id for NetworkRequestHandle
		int[] requestId = new int[] { id };
		NetworkRequestHandle handle = new NetworkRequestHandle(
				sNetworkRequestManager);
		handle.setRequestIds(requestId);

		// Add to queue and execute/queue request
		addToQueue(jsonNetworkRequest);
		runDelayed();

		return handle;
	}

	/**
	 * Executes all the NetworkRequests and notifies caller when each request is
	 * finished. Next request gets ran when first one is completed.
	 * 
	 * @param jsonNetworkRequests
	 *            List of BaseJsonNetworkRequest to be executed.
	 * @return {@link NetworkRequestHandle}
	 */
	public synchronized NetworkRequestHandle executeJsonRequestsLinked(
			final List<JsonNetworkRequest<?, JsonNetworkRequestError>> jsonNetworkRequests) {
		// Skip null or empty list
		if (jsonNetworkRequests == null || jsonNetworkRequests.isEmpty()) {
			return null;
		}

		int[] requestIds = new int[jsonNetworkRequests.size()];

		// Get the first id of this queue
		final int startIdOfQueue = getNextNetworkRequestId();

		for (int i = 0; i < jsonNetworkRequests.size(); i++) {
			JsonNetworkRequest<?, ?> jsonNetworkRequest = (JsonNetworkRequest<?, ?>) jsonNetworkRequests
					.get(i);

			// Get unique id for network request
			final int id = getUniqueNetworkRequestId();

			// Set unique id for request
			jsonNetworkRequest.setId(id);

			// Set id dependency and start id of queue
			if (i > 0) {
				jsonNetworkRequest.setDependsOnId(id - 1);
				jsonNetworkRequest.setStartIdOfQueue(startIdOfQueue);
			}

			requestIds[i] = id;
		}

		// Set request ids for NetworkRequestHandle
		NetworkRequestHandle handle = new NetworkRequestHandle(
				sNetworkRequestManager);
		handle.setRequestIds(requestIds);

		// Add to queue and execute/queue request
		addToQueue((List<JsonNetworkRequest<?, JsonNetworkRequestError>>) jsonNetworkRequests);
		runDelayed();

		return handle;
	}

	public synchronized <T> void onNetworkRequestsCanceled(int[] requestIds) {
		if (requestIds != null) {
			for (Integer id : requestIds) {
				// Remove from queue
				Iterator<JsonNetworkRequest<?, JsonNetworkRequestError>> iter = mRequestQueue
						.iterator();
				while (iter.hasNext()) {
					JsonNetworkRequest<?, ?> r = iter.next();

					if (r.getId() == id) {
						// Log
						NemoLog.debug(NetworkRequestManager.class,
								"cancel, url: " + r.getUrl());

						iter.remove();
						break;
					}
				}

				// Remove from running commands and notify listener
				JsonNetworkRequest<?, ?> r = mRunningRequests.remove(id);
				r.getListener().onNetworkRequestCanceled();

				// Log
				NemoLog.debug(NetworkRequestManager.class,
						"cancel, url: " + r.getUrl());

				// Tell Volley to cancel
				mVolleyQueue.cancelAll(id);
			}
		}
	}

	public synchronized void cancelAll() {
		// Cancel all from queue
		Iterator<JsonNetworkRequest<?, JsonNetworkRequestError>> iterQ = mRequestQueue
				.iterator();
		while (iterQ.hasNext()) {
			JsonNetworkRequest<?, ?> r = iterQ.next();

			// Log
			NemoLog.debug(NetworkRequestManager.class,
					"cancel, url: " + r.getUrl());

			iterQ.remove();
		}

		Iterator<Integer> iterR = mRunningRequests.keySet().iterator();

		while (iterR.hasNext()) {
			JsonNetworkRequest<?, ?> r = mRunningRequests.remove(iterR.next());

			// Log
			NemoLog.debug(NetworkRequestManager.class,
					"cancel, url: " + r.getUrl());
		}
	}

	private int getNextNetworkRequestId() {
		return mNetworkRequestIdGenerator.get();
	}

	private int getUniqueNetworkRequestId() {
		return mNetworkRequestIdGenerator.getAndIncrement();
	}

	@SuppressWarnings("unchecked")
	private <T, K> void addToQueue(
			final JsonNetworkRequest<?, ?> jsonNetworkRequest) {
		mRequestQueue
				.add((JsonNetworkRequest<?, JsonNetworkRequestError>) jsonNetworkRequest);
	}

	private <T, K> void addToQueue(
			final List<JsonNetworkRequest<?, JsonNetworkRequestError>> requests) {
		mRequestQueue.addAll(requests);
	}

	private void runDelayed() {
		mNetworkRequestHandler.removeCallbacks(mRequestRunner);
		mNetworkRequestHandler.post(mRequestRunner);
	}

	private Runnable mRequestRunner = new Runnable() {
		@Override
		public void run() {
			runRequests();
		}
	};

	private void runRequests() {
		if (mRequestQueue.size() > 0) {
			try {
				runRequestsFromQueue();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private <T, K> void runRequestsFromQueue() throws InterruptedException {
		Iterator<JsonNetworkRequest<?, JsonNetworkRequestError>> iter = mRequestQueue
				.iterator();

		while (iter.hasNext()
				&& mRunningRequests.size() < mMaxRunningRequestCount) {
			// Get the base request
			@SuppressWarnings("unchecked")
			final JsonNetworkRequest<T, JsonNetworkRequestError> baseRequest = (JsonNetworkRequest<T, JsonNetworkRequestError>) iter
					.next();

			// Check if we can run it
			if (isAllowedToRun(baseRequest)) {
				// Now it's safe to remove from queue
				iter.remove();

				// If there is no network available notify listener
				if (!mNetworkStateChecker.isNetworkConnected()) {
					try {
						JsonNetworkRequestError error = baseRequest
								.getClassOfFailedObject().newInstance();
						error.setReason(NetworkErrorReason.NO_NETWORK);
						baseRequest.getListener().onNetworkRequestError(error);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}

					continue;
				}

				// Create response listener
				Listener<Object> responseListener = new Listener<Object>() {

					@SuppressWarnings("unchecked")
					@Override
					public void onResponse(Object response) {
						// Remove from running requests
						mRunningRequests.remove(baseRequest.getId());

						NetworkRequestListener<T, K> listener = (NetworkRequestListener<T, K>) baseRequest
								.getListener();

						// Notify listener
						if (listener != null) {
							listener.onNetworkRequestSuccess((T) response);
						}

						// Start more requests if in queue
						runDelayed();
					}
				};

				ErrorListener errorResponseListener = new ErrorListener() {

					@SuppressWarnings("unchecked")
					@Override
					public void onErrorResponse(VolleyError error) {
						NemoVolleyError e = (NemoVolleyError) error;

						JsonNetworkRequestError err = (JsonNetworkRequestError) e
								.getErrorObject();

						// Check for JsonKeyNotFoundExpection
						if (e.getCause() != null
								&& e.getCause() instanceof JsonKeyNotFoundExpection) {
							// Log
							JsonKeyNotFoundExpection ex = (JsonKeyNotFoundExpection) e
									.getCause();

							NemoLog.error(NetworkRequestManager.class,
									"request url: " + baseRequest.getUrl());
							NemoLog.error(
									NetworkRequestManager.class,
									"response missing field in JSON: "
											+ ex.getMessage());

							err.setReason(NetworkErrorReason.KEY_NOT_FOUND);
						} else if (err.getHttpStatusCode() != HttpStatus.SC_OK) {
							err.setReason(NetworkErrorReason.HTTP_ERROR);
						}

						// Remove from running requests
						mRunningRequests.remove(baseRequest.getId());

						NetworkRequestListener<T, K> listener = (NetworkRequestListener<T, K>) baseRequest
								.getListener();

						if (listener != null) {
							listener.onNetworkRequestError((K) err);
						}

						// Start more requests if in queue
						runDelayed();
					}
				};

				// Create JsonNetworkRequest
				Gson gson = new Gson();
				String body = gson.toJson(baseRequest.getJsonBody());

				VolleyRequest<T, JsonNetworkRequestError> request = new VolleyRequest<T, JsonNetworkRequestError>(
						(JsonNetworkRequest<T, JsonNetworkRequestError>) baseRequest,
						body, responseListener, errorResponseListener);
				request.setId(baseRequest.getId());

				// Change request running
				mRunningRequests.put(baseRequest.getId(), baseRequest);

				request.run(mVolleyQueue);

				// Log
				String logMethod = "";
				switch (baseRequest.getMethod()) {
				case Method.GET:
					logMethod = "GET";
					break;
				case Method.POST:
					logMethod = "POST";
					break;
				case Method.PUT:
					logMethod = "PUT";
					break;
				case Method.DELETE:
					logMethod = "DELETE";
					break;
				}

				NemoLog.debug(NetworkRequestManager.class, "run " + logMethod
						+ ": " + baseRequest.getUrl());

				if (baseRequest.getMethod() == Method.POST) {
					NemoLog.debug(NetworkRequestManager.class, "parameters: "
							+ body);
				}
			}
		}
	}

	private boolean isAllowedToRun(JsonNetworkRequest<?, ?> baseRequest) {
		int dependsOnId = baseRequest.getDependsOnId();

		if (dependsOnId > -1) {
			// If running requests contains id which is lower/equal than
			// dependsOnId and greater/equal to start id of queue, this request
			// must wait
			Iterator<Entry<Integer, JsonNetworkRequest<?, JsonNetworkRequestError>>> iter = mRunningRequests
					.entrySet().iterator();
			while (iter.hasNext()) {
				int id = iter.next().getValue().getId();

				if (id <= dependsOnId && id >= baseRequest.getStartIdOfQueue()) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Json network request to be ran with Volley.
	 * 
	 * @author Niko Rehnbäck
	 * 
	 * @param <T>
	 *            Class type of successfully parsed request.
	 * @param <K>
	 *            Class type of failed request.
	 */
	private class VolleyRequest<T, K extends JsonNetworkRequestError> extends
			JsonRequest<Object> {

		private int mId;
		private JsonNetworkRequest<T, K> mBaseRequest;

		public VolleyRequest(JsonNetworkRequest<T, K> baseRequest, String body,
				Listener<Object> responseListener,
				ErrorListener errorResponseListener) {
			super(baseRequest.getMethod(), baseRequest.getUrl(), body,
					responseListener, errorResponseListener);

			mBaseRequest = baseRequest;

			setRetryPolicy(mBaseRequest.getRetryPolicy());
			setShouldCache(mBaseRequest.getShouldCache());
		}

		public void setId(int id) {
			mId = id;
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			Map<String, String> headers = super.getHeaders();

			// Add user defined headers to default headers
			if (mBaseRequest.getHeaders() != null) {
				mBaseRequest.getHeaders().putAll(headers);
			}

			// Put Cookies to headers
			mNetworkRequestCookieManager.putCookies(headers,
					getDomainName(getUrl()));

			return mBaseRequest.getHeaders();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Response<Object> parseNetworkResponse(NetworkResponse response) {
			// Check for null data
			if (response == null || response.data == null) {
				// Log
				NemoLog.error(NetworkRequestManager.class,
						"empty response, url: " + mBaseRequest.getUrl());

				try {
					return Response
							.error(new NemoVolleyError(response, mBaseRequest
									.getClassOfFailedObject().newInstance()));
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			// Parse Cookies
			mNetworkRequestCookieManager.storeCookies(response,
					getDomainName(getUrl()), System.currentTimeMillis());

			String json = new String(response.data);
			Gson gson = new GsonBuilder().registerTypeAdapter(
					mBaseRequest.getClassOfSuccessfulObject(),
					new JsonRequiredDeserializer<T>()).create();

			Object success = gson.fromJson(json,
					mBaseRequest.getClassOfSuccessfulObject());

			// Log
			NemoLog.debug(NetworkRequestManager.class, "success, url: "
					+ mBaseRequest.getUrl());
			NemoLog.debug(NetworkRequestManager.class, "response: " + json);

			// Check if result needs more validation
			if (mBaseRequest instanceof ValidatingJsonNetworkRequest<?, ?>) {
				Object error = gson.fromJson(json,
						mBaseRequest.getClassOfFailedObject());

				if (((ValidatingJsonNetworkRequest<T, K>) mBaseRequest)
						.isValid((K) error)) {
					return Response.success(success,
							HttpHeaderParser.parseCacheHeaders(response));
				} else {
					((JsonNetworkRequestError) error)
							.setReason(NetworkErrorReason.VALIDATION_ERROR);

					return Response.error(new NemoVolleyError(response,
							(K) error));
				}
			}

			return Response.success(success,
					HttpHeaderParser.parseCacheHeaders(response));
		}

		@Override
		protected VolleyError parseNetworkError(VolleyError volleyError) {
			// Log
			NemoLog.error(NetworkRequestManager.class, "error, url: "
					+ mBaseRequest.getUrl());
			if (volleyError.getLocalizedMessage() != null) {
				NemoLog.error(NetworkRequestManager.class, "message: "
						+ volleyError.getLocalizedMessage());
			}

			NetworkResponse response = volleyError.networkResponse;

			// Check for null data
			if (response == null || response.data == null) {
				// Log
				NemoLog.error(NetworkRequestManager.class,
						"empty response, url: " + mBaseRequest.getUrl());
			}

			try {
				return new NemoVolleyError(response, mBaseRequest
						.getClassOfFailedObject().newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			return new NemoVolleyError(response, null);
		}

		public void run(RequestQueue networkRequestQueue) {
			setTag(mId);

			networkRequestQueue.add(this);
		}

		private String getDomainName(String url) {
			try {
				URI uri = new URI(url);
				String domain = uri.getHost();

				return domain.startsWith("www.") ? domain.substring(4) : domain;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return "";
		}
	}

	/**
	 * Simple error wrapper class.
	 * 
	 * @author Niko Rehnbäck
	 * 
	 */
	@SuppressWarnings("serial")
	private class NemoVolleyError extends VolleyError {

		private Object mErrorObject;

		public NemoVolleyError(NetworkResponse response, Object error) {
			super(response);

			((JsonNetworkRequestError) error)
					.setHttpStatusCode(response.statusCode);
			mErrorObject = error;
		}

		public Object getErrorObject() {
			return mErrorObject;
		}
	}

	/**
	 * Cookie manager to read and set Cookies from server.
	 * 
	 * @author Niko Rehnbäck
	 * 
	 */
	private class NetworkRequestCookieManager {

		private static final String SET_COOKIE_KEY = "Set-Cookie";
		private static final String COOKIE_KEY = "Cookie";

		private Map<String, NetworkRequestCookie> mCookies = new HashMap<String, NetworkRequestCookie>();

		public void clearAllCookies() {
			mCookies.clear();
		}

		public void storeCookies(NetworkResponse response, final String domain,
				long requestTimestamp) {
			Map<String, String> headerFields = response.headers;

			if (headerFields != null) {
				String cookie = headerFields.get(SET_COOKIE_KEY);

				if (cookie != null && cookie.length() > 0) {
					storeCookie(cookie, requestTimestamp, domain);
				}
			}
		}

		private void storeCookie(String cookieString, long requestTimestamp,
				String domain) {

			String[] cookieValues = cookieString.split("-");

			for (int i = 0; i < cookieValues.length; i++) {
				String c = cookieValues[i];

				NetworkRequestCookie cookie = new NetworkRequestCookie();
				cookie.setName(c.substring(0, c.indexOf("=")));
				cookie.setValue(c.substring(c.indexOf("=") + 1,
						c.indexOf("; ") != -1 ? c.indexOf("; ") : c.length()));
				cookie.setTimeStamp(requestTimestamp);
				cookie.setDomain(domain);

				if (!mCookies.containsKey(cookie.getName())
						|| mCookies.get(cookie.getName()).getTimeStamp() < requestTimestamp) {
					mCookies.put(cookie.getName(), cookie);

					// Log
					NemoLog.debug(NetworkRequestCookieManager.class,
							"store cookie, domain: " + domain);
					NemoLog.debug(NetworkRequestCookieManager.class, c);
				}
			}
		}

		public void putCookies(Map<String, String> headers, final String domain) {
			StringBuffer cookieBuf = new StringBuffer();
			Iterator<NetworkRequestCookie> it = mCookies.values().iterator();

			while (it.hasNext()) {
				NetworkRequestCookie cookie = it.next();

				if (!cookie.getName().equals("")
						&& !cookie.getValue().equals("")
						&& cookie.getDomain().equalsIgnoreCase(domain)) {
					cookieBuf
							.append(cookie.getName() + "=" + cookie.getValue());
				}
				if (it.hasNext()) {
					cookieBuf.append("; ");
				}
			}

			String cookieString = cookieBuf.toString();

			// Only put if cookies found
			if (cookieString.length() > 0) {
				headers.put(COOKIE_KEY, cookieString);

				// Log
				NemoLog.debug(NetworkRequestCookieManager.class,
						"put cookies, domain: " + domain);
				NemoLog.debug(NetworkRequestCookieManager.class, cookieString);
			}
		}

		/**
		 * Simple cookie class.
		 * 
		 * @author Niko Rehnbäck
		 * 
		 */
		private class NetworkRequestCookie {
			private String mName = "";
			private String mValue = "";
			private String mDomain = "";
			private long mTimeStamp = 0;

			public String getName() {
				return mName;
			}

			public void setName(String name) {
				mName = name;
			}

			public String getValue() {
				return mValue;
			}

			public void setValue(String value) {
				mValue = value;
			}

			public String getDomain() {
				return mDomain;
			}

			public void setDomain(String domain) {
				mDomain = domain;
			}

			public long getTimeStamp() {
				return mTimeStamp;
			}

			public void setTimeStamp(long timeStamp) {
				mTimeStamp = timeStamp;
			}
		}
	}
}
