package info.pithos.serde;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;

/**
 * @author svarma
 *
 *         June 6, 2021
 *
 * @param <T>
 */
public class AvroSerde implements ObjectSerde<GenericRecord> {

	private final GenericRecord genericRecord;
	private final Schema schema;
	private final String json;

	private volatile byte[] binary;

	/**
	 * @param genericRecord
	 */
	public AvroSerde(GenericRecord genericRecord) {
		if (genericRecord == null) {
			throw new IllegalArgumentException("null genericRecord");
		}

		this.genericRecord = genericRecord;
		this.schema = this.genericRecord.getSchema();
		this.json = this.getJson();
		this.initBinary();
	}

	/**
	 * @param is
	 * @param parser
	 */
	public AvroSerde(InputStream is) {
		if (is == null) {
			throw new IllegalArgumentException("null is");
		}

		try {
			try {
				Schema.Parser parser = new Schema.Parser();
				this.schema = parser.parse(is);
				this.genericRecord = new GenericData.Record(schema);
			} catch (IOException e) {
				throw new SerdeException("Illegal avro content", e);
			}

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
	 * @param is
	 * @param parser
	 */
	public AvroSerde(String json, Schema schema) {
		if (json == null || json.isEmpty()) {
			throw new IllegalArgumentException("null or empty json");
		}

		if (schema == null) {
			throw new IllegalArgumentException("null avro schema");
		}

		this.schema = schema;

		DatumReader<GenericRecord> reader = new GenericDatumReader<>(this.schema);
		Decoder decoder = null;
		try {
			decoder = DecoderFactory.get().jsonDecoder(this.schema, json);
			this.genericRecord = reader.read(null, decoder);
		} catch (IOException e) {
			throw new SerdeException("Illegal avro content", e);
		}

		this.json = json;
		this.initBinary();
	}

	@Override
	public SerdeType getType() {
		return SerdeType.AVRO;
	}

	@Override
	public GenericRecord getObject() {
		return this.genericRecord;
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
	 * @return
	 */
	private String getJson() {
		DatumWriter<GenericRecord> avroWriter = new GenericDatumWriter<>(this.schema);
		Encoder jsonEncoder = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			jsonEncoder = EncoderFactory.get().jsonEncoder(this.schema, bos);
			avroWriter.write(this.genericRecord, jsonEncoder);
			jsonEncoder.flush();
			return new String(bos.toByteArray());
		} catch (IOException e) {
			throw new SerdeException("Failed to convert avro to json", e);
		}
	}

	/**
	 * 
	 */
	private void initBinary() {
		DatumWriter<GenericRecord> avroWriter = new GenericDatumWriter<>(this.schema);
		Encoder binaryEncoder = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			binaryEncoder = EncoderFactory.get().binaryEncoder(bos, null);
			avroWriter.write(this.genericRecord, binaryEncoder);
			binaryEncoder.flush();
			this.binary = bos.toByteArray();
		} catch (IOException e) {
			throw new SerdeException("Failed to convert avro to binary", e);
		}
	}
}
