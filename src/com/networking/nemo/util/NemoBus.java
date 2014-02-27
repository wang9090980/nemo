/*
 * Copyright (C) 2014 Niko Rehnb�ck
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

package com.networking.nemo.util;

import com.networking.nemo.enums.NetworkState;
import com.squareup.otto.Bus;

/**
 * Wrapper for {@link Bus} used to produce {@link NetworkState} changes.
 * 
 * @author Niko Rehnb�ck
 * 
 */
public final class NemoBus {

	private static final Bus BUS = new Bus();

	public static Bus getInstance() {
		return BUS;
	}

	private NemoBus() {
	}
}
