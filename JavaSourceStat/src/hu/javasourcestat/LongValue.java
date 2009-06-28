
package hu.javasourcestat;

/**
 * A modifiable long number value.
 * It is mutable and not thread safe!
 * @author karnokd, 2008.03.13.
 * @version $Revision 1.0$
 */
public class LongValue extends Number {
	/** */
	private static final long serialVersionUID = 8783010948289218986L;
	/** The value. */
	private long value;
	/**
	 * Constructor. Initializes the value to null.
	 */
	public LongValue() {
		this(0L);
	}
	/**
	 * Constructor.
	 * @param value the initial value
	 */
	public LongValue(long value) {
		this.value = value;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double doubleValue() {
		return value;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public float floatValue() {
		return value;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int intValue() {
		return (int)value;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long longValue() {
		return value;
	}
	/**
	 * Set a new value.
	 * @param value the value
	 */
	public void setValue(long value) {
		this.value = value;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof LongValue && ((LongValue)obj).value == value;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return (int)(value >> 32 ^ (value & 0xFFFFFFFFL));
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Long.toString(value);
	}
	/**
	 * Set the new value if it is grater than the current value.
	 * @param value the new value
	 * @return true if the new value is greater
	 */
	public boolean setIfGreater(long value) {
		if (this.value < value) {
			this.value = value;
			return true;
		}
		return false;
	}
	/**
	 * Set the new value if it is lower than the current value.
	 * @param value the new value
	 * @return true if the new value is lower
	 */
	public boolean setIfLower(long value) {
		if (this.value > value) {
			this.value = value;
			return true;
		}
		return false;
	}
	/**
	 * Add a value to the current value.
	 * @param value the value to add
	 */
	public void add(long value) {
		this.value += value;
	}
	/**
	 * Substract a value.
	 * @param value the value
	 */
	public void sub(long value) {
		this.value -= value;
	}
	/**
	 * Divide the current value by the given value.
	 * @param value the value to divide with
	 */
	public void idiv(long value) {
		this.value /= value;
	}
	/**
	 * Multiply the current value.
	 * @param value the value to muliply with
	 */
	public void imul(long value) {
		this.value *= value;
	}
}
