/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.saga.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class SagaContextImpl implements SagaContext {
  private final Map<String, SagaResponse> completedTransactions;
  private final Map<String, SagaResponse> completedCompensations;
  private final Set<String> abortedTransactions;
  private final Map<String, SagaRequest> hangingTransactions;

  public SagaContextImpl() {
    this.completedTransactions = new HashMap<>();
    this.completedCompensations = new HashMap<>();
    this.abortedTransactions = new HashSet<>();
    this.hangingTransactions = new HashMap<>();
  }

  @Override
  public boolean isCompensationStarted() {
    return !abortedTransactions.isEmpty() || !completedCompensations.isEmpty();
  }

  @Override
  public boolean isTransactionCompleted(SagaRequest request) {
    return completedTransactions.containsKey(request.id());
  }

  @Override
  public boolean isCompensationCompleted(SagaRequest request) {
    return completedCompensations.containsKey(request.id());
  }

  @Override
  public void beginTransaction(SagaRequest request) {
    hangingTransactions.put(request.id(), request);
  }

  @Override
  public void endTransaction(SagaRequest request, SagaResponse response) {
    completedTransactions.put(request.id(), response);
    hangingTransactions.remove(request.id());
  }

  @Override
  public void abortTransaction(SagaRequest request) {
    completedTransactions.remove(request.id());
    abortedTransactions.add(request.id());
    hangingTransactions.remove(request.id());
  }

  @Override
  public void compensateTransaction(SagaRequest request, SagaResponse response) {
    completedCompensations.put(request.id(), response);
    completedTransactions.remove(request.id());
  }

  @Override
  public void handleHangingTransactions(Consumer<SagaRequest> consumer)  {
    for (Iterator<SagaRequest> iterator = hangingTransactions.values().iterator(); iterator.hasNext(); ) {
      consumer.accept(iterator.next());
    }
  }

  @Override
  public SagaResponse responseOf(String requestId) {
    return completedTransactions.get(requestId);
  }
}