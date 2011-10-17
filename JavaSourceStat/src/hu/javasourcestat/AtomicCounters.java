
package hu.javasourcestat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author akarnokd, 2008.03.12.
 * @version $Revision 1.0$
 */
public class AtomicCounters {
	/** Map of counters. */
	public final ConcurrentMap<String, AtomicLong> counters = new ConcurrentHashMap<String, AtomicLong>();
	/**
	 * Add a value to the counter.
	 * @param name the name, not null
	 * @param value the value
	 */
	public void add(String name, long value) {
		AtomicLong c = counters.get(name);
		if (c == null) {
			c = new AtomicLong(value);
			c = counters.putIfAbsent(name, c);
			if (c == null) {
				return;
			}
		}
		c.addAndGet(value);
	}
}
