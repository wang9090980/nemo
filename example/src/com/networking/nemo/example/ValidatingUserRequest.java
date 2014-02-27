package com.networking.nemo.example;

import com.networking.nemo.network.NetworkRequestListener;
import com.networking.nemo.request.ValidatingJsonNetworkRequest;

public class ValidatingUserRequest extends
		ValidatingJsonNetworkRequest<User, ValidatingNetworkRequestError> {

	public ValidatingUserRequest(int method, String url, Object jsonBody,
			Class<User> classOfSuccessfulObject,
			Class<ValidatingNetworkRequestError> classOfFailedObject,
			NetworkRequestListener<User, ValidatingNetworkRequestError> listener) {
		super(method, url, jsonBody, classOfSuccessfulObject,
				classOfFailedObject, listener);
	}

	@Override
	public boolean isValid(ValidatingNetworkRequestError error) {
		return error.getStatus().equals("0"); // Only response with json status
												// 0 are valid
	}

	@Override
	public boolean getShouldCache() {
		return false;
	}
}
