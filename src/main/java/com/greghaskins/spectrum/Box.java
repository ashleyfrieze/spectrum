package com.greghaskins.spectrum;

/**
 * For use with nested steps inside a test, this box allows data passing between tests
 */
public class Box<T> {
	// the boxed object
	private T object;

	/**
	 * Default constructor for null object
	 */
	public Box() {
	}

	/**
	 * Construct with the object to box
	 * @param object to box
	 */
	public Box(T object) {
		this.object = object;
	}

	/**
	 * @return the object in the box
	 */
	public T get() {
		return object;
	}

	/**
	 * Change the object in the box
	 * @param object new value
	 */
	public void set(T object) {
		this.object = object;
	}
}
