package com.greghaskins.spectrum;

/**
 * For executing test blocks within rules or in parallel
 */
public interface BlockExecutor {
	void execute(Block block, boolean isRoot) throws Throwable;
}
