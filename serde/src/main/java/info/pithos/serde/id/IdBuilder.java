package info.pithos.serde.id;

import java.io.Serializable;

/**
 * @author svarma
 *
 *         July 25, 2021
 *
 */
public interface IdBuilder<T, I extends Serializable> {

	/**
	 * @return
	 */
	I getIdValue(T object);
}
