package org.metricshub.jawk.jrt;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * Jawk
 * ჻჻჻჻჻჻
 * Copyright (C) 2006 - 2025 MetricsHub
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import java.util.LinkedList;
import java.util.List;

import org.metricshub.jawk.util.AwkLogger;
import org.slf4j.Logger;

/**
 * Manages multiple blocking code segments simultaneously such that
 * unblocking one block condition releases the block of all other
 * block code segments.
 *
 * @see BlockObject
 * @author Danny Daglas
 */
public class BlockManager {

	private static final Logger LOG = AwkLogger.getLogger(BlockManager.class);

	private final Object notifierLock = "NOTIFIER_LOCK";
	private String notifier = null;

	/**
	 * Executes all block segments simultaneously, waiting for
	 * one block release.
	 * <p>
	 * The algorithm is as follows:
	 * <ul>
	 * <li>Collect linked block objects into a List.
	 * <li>Spawn a BlockThread for each block object.
	 * <li>Wait for notification from any of the BlockThreads.
	 * <li>Interrupt remaining block threads.
	 * <li>Wait for each BlockThread to die.
	 * <li>Return the block object notifier which satisfied their block condition.
	 * </ul>
	 * <p>
	 * And, the BlockThread algorithm is as follows:
	 *
	 * <ul>
	 * <li>try, catch for InterruptedException ...
	 *   <ul>
	 *   <li>Execute the BlockObject block segment.
	 *   <li>Assign the notifier from this BlockObject
	 *     if one isn't already assigned (to mitigate
	 *     a race condition).
	 *   <li>Notify the BlockManager.
	 *   </ul>
	 * <li>If interrupted, do nothing and return.
	 * </ul>
	 *
	 * @param bo BlockObject to employ. Other block objects
	 *   may be linked to this block object. In this event,
	 *   employ all block objects simultaneously.
	 * @return a {@link java.lang.String} object
	 */
	public String block(BlockObject bo) {
		// get all block objects
		List<BlockObject> bos = bo.getBlockObjects();
		// each block object contains a wait statement
		// (either indefinite or timed)

		// for each block object
		// 	spawn a thread (preferably using a threadpool)
		// 	do the wait
		//	signal a break in the block
		// interrupt all other threads, resulting in InterruptedExceptions

		List<Thread> threadList = new LinkedList<Thread>();
		synchronized (BlockManager.this) {
			for (BlockObject blockobj : bos) {
				// spawn a thread
				Thread t = new BlockThread(blockobj);
				t.start();
				threadList.add(t);
			}

			// now, wait for notification from one of the BlockThreads
			try {
				BlockManager.this.wait();
			} catch (InterruptedException ie) {}
		}

		// block successful, interrupt other blockers
		// and wait for thread deaths
		for (Thread t : threadList) {
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException ie) {}
		}

		// return who was the notifier
		assert notifier != null;
		return notifier;
	}

	private final class BlockThread extends Thread {

		private BlockObject bo;

		private BlockThread(BlockObject bo) {
			setName("BlockThread for " + bo.getNotifierTag());
			this.bo = bo;
		}

		@Override
		public void run() {
			try {
				bo.block();
				synchronized (notifierLock) {
					if (notifier == null) {
						notifier = bo.getNotifierTag();
					}
				}
				synchronized (BlockManager.this) {
					BlockManager.this.notify();
				}
			} catch (InterruptedException ie) {
			} catch (RuntimeException re) {
				LOG.error("exitting", re);
				System.exit(1);
			}
		}
	}
}
