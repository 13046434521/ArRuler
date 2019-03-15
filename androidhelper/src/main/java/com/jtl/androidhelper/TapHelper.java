/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jtl.androidhelper;

import android.view.MotionEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Helper to detect taps using Android GestureDetector, and pass the taps between UI thread and
 * render thread.
 */
public final class TapHelper {
  private final BlockingQueue<MotionEvent> queuedSingleTaps = new ArrayBlockingQueue<>(16);

  public MotionEvent poll() {
    return queuedSingleTaps.poll();//
  }

  public void push(MotionEvent me){
      queuedSingleTaps.add(me);
  }

}
