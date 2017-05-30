package com.greghaskins.spectrum;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Similar to {@link Variable} this class is a box to hold a reference. The aim is that this reference
 * can be shared across multiple threads safely. This is achieved by providing functions to peek at the
 * object inside while synchronized by SharedVariable. If code is restricted to using {@link #map(Function)}
 * and {@link #modify(Consumer)} methods, then a greater degree of thread safety is achieved.<br><br>
 * The code allows getting and setting of the inner reference, which can lead to problems if the
 * object inside is not thread safe. However, there will be situations where it is necessary
 * to access the reference directly.
 */
public final class SharedVariable<T> implements Supplier<T> {
	private T value;

	/**
	 * Construct the shared variable with an initial value.
	 * @param initialValue the value the variable has to start with.
	 */
	public SharedVariable(T initialValue) {
		this.value = initialValue;
	}

	/**
	 * Read something from the inner value and return it. This allows the outer code to inspect
	 * or calculate something from the object inside this box.<br><br>
	 * <b>Note:</b> if you use this to return a reference to something that's not thread safe then you
	 * will have to manage the thread safety of that yourself. Examples of this could include {@link Function#identity()}
	 * or returning a reference to a mutable value from a list.<br><br>
	 * <b>Note:</b> <code>map(Function::identity)</code> is better achieved by using {@link #get()}<br><br>
	 * <b>Note:</b> if your code can allow the value inside here to be null, then your function needs to handle null too
	 * @param mapping the functor that maps from the internal value to required value
	 * @param <R> return type
	 * @return the result of running the mapping function on the internal state once it's available to the calling thread.
	 */
	public synchronized <R> R map(final Function<T, R> mapping) {
		return mapping.apply(value);
	}

	/**
	 * Do something to the object inside the SharedVariable (not the reference) when the calling thread has access to it.
	 * This gives the {@link Consumer} provided a chance to change the state of the object held inside this box. <br><br>
	 * <b>Note:</b> if your code can allow the value inside here to be null, then your function needs to handle null too
	 * @param modifier consumer that modifies the state of the internals of this box.
	 */
	public synchronized void modify(final Consumer<T> modifier) {
		modifier.accept(value);
	}

	/**
	 * Get the current value of this Variable.
	 * This is essentially a shortcut for <code>map(Function::identity)</code>.<br>
	 * <br><b>Note:</b> holding the reference to the value is likely to violate thread safety if
	 * the object itself is not thread safe. Similarly, another thread could change the reference
	 * via {@link #set(Object)} and the detached reference would be unaffected.<br><br>
	 * Only use this function out of necessity.
	 *
	 * @return current value
	 */
	@Override
	public synchronized T get() {
		return value;
	}

	/**
	 * Change the value of this Variable.
	 *
	 * @param value new value
	 */
	public void set(final T value) {
		set(() -> value);
	}

	/**
	 * Change the value of this Variable.<br><br>
	 * This uses a supplier as input to allow a thread safe copying of references between SharedVariables
	 * and to allow for the calling code to defer evaluation of its required new value for the SharedVariable
	 * until it has exclusive access to it.
	 *
	 * @param value supplier of the new value.
	 */
	public synchronized void set(final Supplier<T> value) {
		this.value = value.get();
	}
}
