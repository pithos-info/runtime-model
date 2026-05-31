package info.pithos.serde;

import java.io.Serializable;

/**
 * @author svarma
 *
 *         June 6, 2021
 * 
 *
 * @param <T>
 */
public interface ObjectSerde<T> {

	/**
	 * @author svarma
	 *
	 */
	public enum SerdeType {

	PLAIN("text"), JSON("application/json"), PROTO("application/protobuf"), AVRO("application/octet-stream"), Map("application/json");

		private String value;

		SerdeType(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * @return
	 */
	SerdeType getType();

	/**
	 * De-serialize
	 * 
	 * @return
	 */
	T getObject();

	/**
	 * String serialization
	 * 
	 * @return
	 */
	String serialize();

	/**
	 * String serialization in pretty prints
	 * 
	 * @return
	 */
	String prettyPrint();

	/**
	 * binary serialization
	 * 
	 * @return
	 */
	byte[] binary();

}
