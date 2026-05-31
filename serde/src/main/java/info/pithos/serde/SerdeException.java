package info.pithos.serde;

/**
 * @author svarma
 *
 * June 6, 2021
 *
 */
@SuppressWarnings("serial")
public class SerdeException extends RuntimeException {

	/**
	 * @param message
	 * @param e
	 */
	public SerdeException(String message, Exception e) {
		super(message, e);
	}
}
