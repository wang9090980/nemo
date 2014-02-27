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

package com.networking.nemo.util;

import android.util.Log;

/**
 * Helper class for logging.
 * 
 * @author Niko Rehnbäck
 * 
 */
public class NemoLog {

	private static String sTag = "nemo";

	public static void setTag(String tag) {
		sTag = tag;
	}

	public static void verbose(Object message) {
		verbose(null, message);
	}

	public static <T> void verbose(Class<T> caller, Object message) {
		if (!nullLog(message)) {
			if (caller != null) {
				Log.v(sTag, caller.getSimpleName() + ": " + message.toString());
			} else {
				Log.v(sTag, message.toString());
			}
		}
	}

	public static void debug(Object message) {
		debug(null, message);
	}

	public static <T> void debug(Class<T> caller, Object message) {
		if (!nullLog(message)) {
			if (caller != null) {
				Log.d(sTag, caller.getSimpleName() + ": " + message.toString());
			} else {
				Log.d(sTag, message.toString());
			}
		}
	}

	public static void info(Object message) {
		info(null, message);
	}

	public static <T> void info(Class<T> caller, Object message) {
		if (!nullLog(message)) {
			if (caller != null) {
				Log.i(sTag, caller.getSimpleName() + ": " + message.toString());
			} else {
				Log.i(sTag, message.toString());
			}
		}
	}

	public static void warn(Object message) {
		warn(null, message);
	}

	public static <T> void warn(Class<T> caller, Object message) {
		if (!nullLog(message)) {
			if (caller != null) {
				Log.w(sTag, caller.getSimpleName() + ": " + message.toString());
			} else {
				Log.w(sTag, message.toString());
			}
		}
	}

	public static void error(Object message) {
		error(null, message);
	}

	public static <T> void error(Class<T> caller, Object message) {
		if (!nullLog(message)) {
			if (caller != null) {
				Log.e(sTag, caller.getSimpleName() + ": " + message.toString());
			} else {
				Log.e(sTag, message.toString());
			}
		}
	}

	private static boolean nullLog(Object message) {
		if (message == null) {
			Log.e(sTag, "Unable to log null object.");
			return true;
		}

		return false;
	}
}
