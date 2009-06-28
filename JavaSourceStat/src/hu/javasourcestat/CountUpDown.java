
package hu.javasourcestat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to signal end of recursive processing during a concurrent treewalk.
 * @author karnokd, 2008.03.13.
 * @version $Revision 1.0$
 */
public class CountUpDown {
	/** The guarding lock. */
	private final Lock lock = new ReentrantLock();
	/** The condition to wait. */
	private final Condition cond = lock.newCondition();
	/** The entry-exit counter. */
	private final AtomicInteger count = new AtomicInteger();
	/** Increment the counter. */
	public void increment() {
		count.incrementAndGet();
	}
	/** Decrement value. */
	public void decrement() {
		int value = count.decrementAndGet();
		if (value == 0) {
			lock.lock();
			try {
				cond.signalAll();
			} finally {
				lock.unlock();
			}
		} else
		if (value < 0) {
			throw new IllegalStateException("Counter < 0 :" + value);
		}
	}
	/** 
	 * Await the notification.
	 * @throws InterruptedException when interrupted 
	 */
	public void await() throws InterruptedException {
		lock.lock();
		try {
			while (count.get() > 0) {
				cond.await();
			}
		} finally {
			lock.unlock();
		}
	}
	/**
	 * Await the notification or the timeout.
	 * @param time the time
	 * @param unit the unit
	 * @return false if timeout
	 * @throws InterruptedException when interruped
	 */
	public boolean await(long time, TimeUnit unit) throws InterruptedException {
		lock.lock();
		try {
			if (count.get() > 0) {
				return cond.await(time, unit);
			}
			return true;
		} finally {
			lock.unlock();
		}
	}
}
