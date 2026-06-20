/*
 * Copyright 2026 Pithos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

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
