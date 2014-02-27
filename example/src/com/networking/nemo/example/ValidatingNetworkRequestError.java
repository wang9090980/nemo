package com.networking.nemo.example;

import com.google.gson.annotations.SerializedName;
import com.networking.nemo.deserializer.JsonRequired;
import com.networking.nemo.request.JsonNetworkRequestError;

public class ValidatingNetworkRequestError extends JsonNetworkRequestError {

	@JsonRequired
	@SerializedName("status")
	private String mStatus; // Do not init because we are using JsonRequired
							// annotation

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String status) {
		mStatus = status;
	}
}
