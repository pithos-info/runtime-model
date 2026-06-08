package info.pithos.serde;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
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
public class ProtoBufSerde<T extends Message> implements ObjectSerde<T> {

	/** Shared YAML-aware mapper. Exposed for callers that need pre-parse access to the YAML tree. */
	public static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

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
	public ProtoBufSerde(String json, Message.Builder builder) {
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
	 * Parses a YAML document into a proto message.
	 *
	 * <p>YAML keys must be camelCase and match proto field names (the same convention
	 * used by protobuf's JSON format). Fields present in the YAML but absent from the
	 * proto are silently ignored, so config files may carry extra sections (e.g. a
	 * {@code server} block) alongside proto-mapped sections.
	 *
	 * @param reader  source of the YAML document
	 * @param builder empty builder for the target message type
	 * @param <T>     proto message type
	 * @return a ProtoBufSerde wrapping the parsed message
	 */
	public static <T extends Message> ProtoBufSerde<T> fromYaml(Reader reader, Message.Builder builder) {
		try {
			JsonNode tree = YAML_MAPPER.readTree(reader);
			return fromJsonNode(tree, builder);
		} catch (IOException e) {
			throw new SerdeException("Failed to read YAML: " + e.getMessage(), e);
		}
	}

	/**
	 * Converts an already-parsed {@link JsonNode} into a proto message.
	 *
	 * <p>Useful when the caller needs to inspect or extract fields from the parsed
	 * tree (e.g. non-proto sections like server ports) before delegating proto
	 * construction to this method.
	 *
	 * @param node    the parsed JSON/YAML tree
	 * @param builder empty builder for the target message type
	 * @param <T>     proto message type
	 * @return a ProtoBufSerde wrapping the parsed message
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Message> ProtoBufSerde<T> fromJsonNode(JsonNode node, Message.Builder builder) {
		try {
			String json = JSON_MAPPER.writeValueAsString(node);
			JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
			return new ProtoBufSerde<>((T) builder.build());
		} catch (IOException e) {
			throw new SerdeException("Failed to convert JSON node to protobuf: " + e.getMessage(), e);
		}
	}

	/**
	 * @param proto
	 * @return
	 */
	public static <T extends Message> Map<String, Object> convert(T proto) {
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