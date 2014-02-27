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

/**
 * Network request callbacks for success, error or cancelation.
 * 
 * @author Niko Rehnbäck
 * 
 * @param <T>
 *            Class which is expected to be the result of successful network
 *            request.
 * @param <K>
 *            Class which is expected to be the result of failed network
 *            request.
 */

public interface NetworkRequestListener<T, K> {
	void onNetworkRequestSuccess(T result);

	void onNetworkRequestError(K error);

	void onNetworkRequestCanceled();
}
