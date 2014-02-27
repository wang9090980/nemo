package com.networking.nemo.example;

import com.google.gson.annotations.SerializedName;

public class User {

	@SerializedName("user_id")
	private String mId;

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}
}
