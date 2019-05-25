/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.atomix.utils.AtomixRuntimeException;

/**
 * DistributedPrimitive that is a synchronous (blocking) version of
 * another.
 *
 * @param <T> type of DistributedPrimitive
 */
public abstract class Synchronous<T extends AsyncPrimitive> implements SyncPrimitive {

  private final T primitive;

  public Synchronous(T primitive) {
    this.primitive = primitive;
  }

  @Override
  public String name() {
    return primitive.name();
  }

  @Override
  public void delete() {
    try {
      primitive.delete().get(DEFAULT_OPERATION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new AtomixRuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      primitive.close().get(DEFAULT_OPERATION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new AtomixRuntimeException(e);
    }
  }
}