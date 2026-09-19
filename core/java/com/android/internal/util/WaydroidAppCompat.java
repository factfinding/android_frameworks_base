/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util;

import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import java.lang.reflect.Field;

/** Process-local compatibility experiments for the Waydroid native bridge. @hide */
public final class WaydroidAppCompat {
    private static final String TAG = "WaydroidAppCompat";

    private WaydroidAppCompat() {}

    /** Called after zygote fork, before loading application code or providers. */
    public static void apply(String packageName) {
        if (!"com.instagram.android".equals(packageName)
                || !"libberberis_arm64.so".equals(
                        SystemProperties.get("ro.dalvik.vm.native.bridge"))
                || !SystemProperties.getBoolean("debug.waydroid.instagram_native_av1", false)) {
            return;
        }

        // Instagram 442's emulator policy prefers platform dav1d over its bundled
        // ARM64 decoder. Its own sorting overrides the platform codec ranks.
        // Keep this opt-in: MODEL also participates in other application policies.
        // Reflection changes only this fork's Java field, never system properties
        // or the zygote. Restart the app after changing the property to undo it.
        try {
            Field model = Build.class.getDeclaredField("MODEL");
            model.setAccessible(true);
            model.set(null, "Waydroid Emulator");
            Log.i(TAG, "Enabled Instagram platform AV1 compatibility model");
        } catch (ReflectiveOperationException | SecurityException e) {
            Log.e(TAG, "Cannot apply Instagram platform AV1 compatibility model", e);
        }
    }
}
