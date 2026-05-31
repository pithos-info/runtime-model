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
