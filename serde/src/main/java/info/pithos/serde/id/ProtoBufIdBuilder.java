package info.pithos.serde.id;

import java.io.Serializable;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;

/**
 * @author svarma
 *
 *         July 25, 2021
 *
 */
public interface ProtoBufIdBuilder<T extends GeneratedMessageV3, I extends Serializable> extends IdBuilder<T, I> {

	/**
	 * @return
	 */
	FieldDescriptor getId();

	/**
	 * @return
	 */
	FieldDescriptor getId(T proto);
}
