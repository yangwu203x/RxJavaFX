/**
 * Copyright 2017 Netflix, Inc.
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
package io.reactivex.rxjavafx.observers;

import com.sun.javafx.binding.ExpressionHelper;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.Consumer;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


final class BindingSubscriber<T> implements Subscriber<T>, ObservableValue<T>, Binding<T> {

    private final Consumer<Throwable> onError;
    private final ConnectableFlowable<T> flowable;
    private boolean connected = false;
    private Subscription subscription;
    private ExpressionHelper<T> helper;
    private T value;

    BindingSubscriber(Consumer<Throwable> onError) {
        this.flowable = null;
        this.onError = onError;
    }
    BindingSubscriber(ConnectableFlowable<T> flowable, Consumer<Throwable> onError) {
        this.flowable = flowable;
        this.onError = onError;
    }

    @Override
    public void onComplete() {
        //do nothing
    }

    @Override
    public void onError(Throwable e) {
        try {
            onError.accept(e);
        } catch (Throwable e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onSubscribe(Subscription s) {
        subscription = s;
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
        value = t;
        fireValueChangedEvent();
    }
    @Override
    public T getValue() {
        if (!connected && flowable != null) {
            flowable.connect();
            connected = true;
        }
        return value;
    }
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void invalidate() {
        //does nothing
    }

    @Override
    public ObservableList<?> getDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.cancel();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(InvalidationListener listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(ChangeListener<? super T> listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(InvalidationListener listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    /**
     * Notify the currently registered observers of a value change.
     *
     * This implementation will ignore all adds and removes of observers that
     * are done while a notification is processed. The changes take effect in
     * the following call to fireValueChangedEvent.
     */
    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(helper);
    }
}
