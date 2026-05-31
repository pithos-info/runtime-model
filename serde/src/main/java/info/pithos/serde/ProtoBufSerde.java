package info.pithos.serde;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.google.protobuf.util.JsonFormat;

/**
 * @author svarma
 *
 *         June 6, 2021
 *
 * @param <T>
 */
public class ProtoBufSerde<T extends GeneratedMessageV3> implements ObjectSerde<T> {

	private final T proto;
	private final String json;

	private volatile byte[] binary;

	/**
	 * @param proto
	 */
	public ProtoBufSerde(T proto) {
		if (proto == null) {
			throw new IllegalArgumentException("null proto");
		}

		this.proto = proto;
		this.json = this.getJson();
		this.initBinary();
	}

	/**
	 * @param is
	 * @param parser
	 */
	public ProtoBufSerde(InputStream is, Parser<T> parser) {
		if (is == null) {
			throw new IllegalArgumentException("null is");
		}

		if (parser == null) {
			throw new IllegalArgumentException("null parser");
		}

		try {
			this.proto = parser.parseDelimitedFrom(is);
		} catch (InvalidProtocolBufferException e) {
			throw new SerdeException("Illegal protobuf content", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// ignore
			}
		}

		this.json = this.getJson();
		this.initBinary();
	}

	/**
	 * @param json
	 * @param builder
	 */
	public ProtoBufSerde(String json, GeneratedMessageV3.Builder<?> builder) {
		if (json == null || json.isEmpty()) {
			throw new IllegalArgumentException("null or empty json");
		}

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		this.json = json;

		try {
			JsonFormat.parser().merge(json, builder);
		} catch (InvalidProtocolBufferException e) {
			throw new SerdeException("json incompatible with protobuf", e);
		}

		this.proto = (T) builder.build();
		this.initBinary();
	}

	@Override
	public SerdeType getType() {
		return SerdeType.PROTO;
	}

	@Override
	public T getObject() {
		return this.proto;
	}

	@Override
	public String serialize() {
		return this.json;
	}

	@Override
	public String prettyPrint() {
		return this.json;
	}

	@Override
	public byte[] binary() {
		return Arrays.copyOf(this.binary, this.binary.length);
	}

	/**
	 * @param proto
	 * @return
	 */
	public static <T extends GeneratedMessageV3> Map<String, Object> convert(T proto) {
		Map<String, Object> map = new HashMap<>();

		proto.getAllFields().forEach((field, value) -> {
			map.put(field.getName(), value);
		});

		return map;
	}

	/**
	 * @return
	 */
	private String getJson() {
		try {
			return JsonFormat.printer().print(this.proto);
		} catch (InvalidProtocolBufferException e) {
			throw new SerdeException("json incompatible with protobuf", e);
		}
	}

	private void initBinary() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			this.proto.writeDelimitedTo(bos);
			this.binary = bos.toByteArray();
		} catch (IOException e) {
			throw new SerdeException("failed to binary serialize protobuf", e);
		} finally {
			try {
				bos.close();
			} catch (IOException e) {
				// ignore
			}
		}
	} // initBinary
}