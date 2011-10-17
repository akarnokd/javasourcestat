
package hu.javasourcestat;

import java.util.HashMap;
import java.util.Map;

/**
 * @author akarnokd, 2008.03.13.
 * @version $Revision 1.0$
 */
public class Counters {
	/** The map of the counters. */
	public final Map<String, LongValue> counters = new HashMap<String, LongValue>();
	/**
	 * Add a counter value.
	 * @param name the name
	 * @param value the value
	 */
	public void add(String name, long value) {
		LongValue v = counters.get(name);
		if (v == null) {
			counters.put(name, new LongValue(value));
			return;
		}
		v.add(value);
	}
	/**
	 * Set if greater than the current value.
	 * @param name the name
	 * @param value the value
	 */
	public void max(String name, long value) {
		LongValue v = counters.get(name);
		if (v == null) {
			counters.put(name, new LongValue(value));
			return;
		}
		v.setIfGreater(value);
	}
	/**
	 * Set if lower than the current value.
	 * @param name the name
	 * @param value the value
	 */
	public void min(String name, long value) {
		LongValue v = counters.get(name);
		if (v == null) {
			counters.put(name, new LongValue(value));
			return;
		}
		v.setIfLower(value);
	}
	/**
	 * Average the given counter.
	 * @param name the name
	 * @param count the counter
	 */
	public void avg(String name, long count) {
		LongValue v = counters.get(name);
		if (v != null) {
			v.idiv(count);
		}
	}
	/**
	 * Average all entries.
	 * @param count the count
	 */
	public void avgAll(long count) {
		for (LongValue v : counters.values()) {
			v.idiv(count);
		}
	}
	/**
	 * Get a given counter.
	 * @param name the name
	 * @return the value
	 */
	public long get(String name) {
		LongValue v = counters.get(name);
		return v != null ? v.longValue() : 0L;
	}
}
