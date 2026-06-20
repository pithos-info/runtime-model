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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StringSerde implements ObjectSerde<String> {

	private final String text;

	public StringSerde(String text) {
		if (text == null || text.isEmpty()) {
			throw new IllegalArgumentException("null or empty text");
		}
		this.text = text;
	}

	public StringSerde(InputStream is) {
		if (is == null) {
			throw new IllegalArgumentException("null is");
		}
		try {
			text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new SerdeException("error reading text", e);
		}
	}

	@Override
	public SerdeType getType() {
		return SerdeType.PLAIN;
	}

	@Override
	public String getObject() {
		return this.text;
	}

	@Override
	public String serialize() {
		return this.text;
	}

	@Override
	public String prettyPrint() {
		return this.text;
	}

	@Override
	public byte[] binary() {
		return this.text.getBytes(StandardCharsets.UTF_8);
	}
}
