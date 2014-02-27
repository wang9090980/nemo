package com.networking.nemo.example;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request.Method;
import com.networking.nemo.enums.NetworkState;
import com.networking.nemo.network.NetworkRequestListener;
import com.networking.nemo.network.NetworkRequestManager;
import com.networking.nemo.request.JsonNetworkRequest;
import com.networking.nemo.request.JsonNetworkRequestError;
import com.networking.nemo.util.NemoBus;
import com.squareup.otto.Subscribe;

public class MainActivity extends Activity {

	private static final String TAG = "NemoExample";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		NemoBus.getInstance().register(this);

		NetworkRequestManager networkRequestManager = NetworkRequestManager
				.getInstance(getApplicationContext());
		networkRequestManager.setLogTag(TAG);

		// HTTP 200 is considered error if status != 0
		String testUrl = "http://echo.jsontest.com/user_id/123456/status/1";
		ValidatingUserRequest complexRequest = new ValidatingUserRequest(
				Method.GET,
				testUrl,
				"",
				User.class,
				ValidatingNetworkRequestError.class,
				new NetworkRequestListener<User, ValidatingNetworkRequestError>() {

					@Override
					public void onNetworkRequestCanceled() {
					}

					@Override
					public void onNetworkRequestError(
							ValidatingNetworkRequestError error) {
						Log.e(TAG,
								"ComplexRequest error, status: "
										+ error.getStatus() + "  http status: "
										+ error.getHttpStatusCode()
										+ " reason: " + error.getReason());
					}

					@Override
					public void onNetworkRequestSuccess(User user) {
					}
				});

		networkRequestManager.executeJsonRequest(complexRequest);

		testUrl = "http://echo.jsontest.com/user_id/123456/status/0";
		complexRequest = new ValidatingUserRequest(
				Method.GET,
				testUrl,
				"",
				User.class,
				ValidatingNetworkRequestError.class,
				new NetworkRequestListener<User, ValidatingNetworkRequestError>() {

					@Override
					public void onNetworkRequestCanceled() {
					}

					@Override
					public void onNetworkRequestError(
							ValidatingNetworkRequestError error) {
						Log.e(TAG, "ComplexRequest error, http status: "
								+ error.getHttpStatusCode() + "  reason: "
								+ error.getReason());
					}

					@Override
					public void onNetworkRequestSuccess(User user) {
						Log.e(TAG,
								"ComplexRequest success, user id: "
										+ user.getId());
					}
				});

		networkRequestManager.executeJsonRequest(complexRequest);

		// HTTP 200 is always success
		testUrl = "http://echo.jsontest.com/user_id/567890/status/0";
		JsonNetworkRequest<User, JsonNetworkRequestError> simpleRequest = new JsonNetworkRequest<User, JsonNetworkRequestError>(
				Method.GET, testUrl, "", User.class,
				JsonNetworkRequestError.class,
				new NetworkRequestListener<User, JsonNetworkRequestError>() {

					@Override
					public void onNetworkRequestCanceled() {
					}

					@Override
					public void onNetworkRequestError(
							JsonNetworkRequestError error) {
						Log.e(TAG,
								"SimpleRequest error, http status: "
										+ error.getHttpStatusCode()
										+ "  reason: " + error.getReason());
					}

					@Override
					public void onNetworkRequestSuccess(User user) {
						Log.e(TAG,
								"SimpleRequest success, user id: "
										+ user.getId());
					}
				});

		networkRequestManager.executeJsonRequest(simpleRequest);

		testUrl = "http://echo.jsontest.com/user_id/567890/status/1";
		simpleRequest = new JsonNetworkRequest<User, JsonNetworkRequestError>(
				Method.GET, testUrl, "", User.class,
				JsonNetworkRequestError.class,
				new NetworkRequestListener<User, JsonNetworkRequestError>() {

					@Override
					public void onNetworkRequestCanceled() {
					}

					@Override
					public void onNetworkRequestError(
							JsonNetworkRequestError error) {
						Log.e(TAG,
								"SimpleRequest error, http status: "
										+ error.getHttpStatusCode()
										+ "  reason: " + error.getReason());
					}

					@Override
					public void onNetworkRequestSuccess(User user) {
						Log.e(TAG,
								"SimpleRequest success, user id: "
										+ user.getId());
					}
				});

		networkRequestManager.executeJsonRequest(simpleRequest);

		List<JsonNetworkRequest<?, JsonNetworkRequestError>> jsonNetworkRequests = new ArrayList<JsonNetworkRequest<?, JsonNetworkRequestError>>();

		testUrl = "http://echo.jsontest.com/user_id/1";
		simpleRequest = new JsonNetworkRequest<User, JsonNetworkRequestError>(
				Method.GET, testUrl, "", User.class,
				JsonNetworkRequestError.class,
				new NetworkRequestListener<User, JsonNetworkRequestError>() {

					@Override
					public void onNetworkRequestCanceled() {
					}

					@Override
					public void onNetworkRequestError(
							JsonNetworkRequestError error) {
						Log.e(TAG,
								"SimpleRequest error, http status: "
										+ error.getHttpStatusCode()
										+ "  reason: " + error.getReason());
					}

					@Override
					public void onNetworkRequestSuccess(User user) {
						Log.e(TAG,
								"SimpleRequest success, user id: "
										+ user.getId());
					}
				});

		jsonNetworkRequests.add(simpleRequest);

		testUrl = "http://echo.jsontest.com/user_id/2";
		simpleRequest = new JsonNetworkRequest<User, JsonNetworkRequestError>(
				Method.GET, testUrl, "", User.class,
				JsonNetworkRequestError.class,
				new NetworkRequestListener<User, JsonNetworkRequestError>() {

					@Override
					public void onNetworkRequestCanceled() {
					}

					@Override
					public void onNetworkRequestError(
							JsonNetworkRequestError error) {
						Log.e(TAG,
								"SimpleRequest error, http status: "
										+ error.getHttpStatusCode()
										+ "  reason: " + error.getReason());
					}

					@Override
					public void onNetworkRequestSuccess(User user) {
						Log.e(TAG,
								"SimpleRequest success, user id: "
										+ user.getId());
					}
				});

		jsonNetworkRequests.add(simpleRequest);

		testUrl = "http://echo.jsontest.com/user_id/3";
		simpleRequest = new JsonNetworkRequest<User, JsonNetworkRequestError>(
				Method.GET, testUrl, "", User.class,
				JsonNetworkRequestError.class,
				new NetworkRequestListener<User, JsonNetworkRequestError>() {

					@Override
					public void onNetworkRequestCanceled() {
					}

					@Override
					public void onNetworkRequestError(
							JsonNetworkRequestError error) {
						Log.e(TAG,
								"SimpleRequest error, http status: "
										+ error.getHttpStatusCode()
										+ "  reason: " + error.getReason());
					}

					@Override
					public void onNetworkRequestSuccess(User user) {
						Log.e(TAG,
								"SimpleRequest success, user id: "
										+ user.getId());
					}
				});

		jsonNetworkRequests.add(simpleRequest);

		networkRequestManager.executeJsonRequestsLinked(jsonNetworkRequests);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		NemoBus.getInstance().unregister(this);
	}

	@Subscribe
	public void onNetworkStateChanged(NetworkState state) {
		switch (state) {
		case CONNECTED:
			Log.e(TAG, "Network state changed, current state: " + state);
			break;
		case DISCONNECTED:
			Log.e(TAG, "Network state changed, current state: " + state);
			break;
		}
	}
}
