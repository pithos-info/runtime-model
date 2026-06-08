package info.pithos.serde;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Message;

import java.util.Set;

/**
 * Copies scalar fields between two proto messages that share field names.
 *
 * <p>Intended for mapping between a data-layer proto (e.g. {@code Rbac.User})
 * and a public-API proto (e.g. {@code info.pithos.rbac.service.User}) without
 * writing per-field setter chains.
 *
 * <p><b>What gets copied:</b> any field present in the source that has a
 * matching field name in the target, the same proto type, and the same
 * repeated cardinality — provided it is a primitive scalar type (string,
 * integer, long, bool, float, double).
 *
 * <p><b>What is skipped:</b>
 * <ul>
 *   <li>Message-typed fields — the data model and service API use different
 *       Java classes for nested messages even when the field names match;
 *       callers must populate these (e.g. {@code groups}, {@code roles})
 *       explicitly after the scalar pass.</li>
 *   <li>Fields present in the source but absent from the target — silently
 *       dropped, so internal-only fields (e.g. {@code deleted},
 *       {@code keyHash}, {@code externalId}) are naturally excluded from
 *       the public API without any conditional logic.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * info.pithos.rbac.service.Enterprise api =
 *     ProtoBufMapper.map(dataEnterprise, info.pithos.rbac.service.Enterprise.newBuilder());
 * }</pre>
 */
public final class ProtoBufMapper {

    private static final Set<Type> SCALAR_TYPES = Set.of(
        Type.STRING,
        Type.BOOL,
        Type.INT32,  Type.SINT32,  Type.UINT32,  Type.FIXED32,  Type.SFIXED32,
        Type.INT64,  Type.SINT64,  Type.UINT64,  Type.FIXED64,  Type.SFIXED64,
        Type.FLOAT,
        Type.DOUBLE
    );

    /**
     * Maps scalar fields from {@code source} into {@code targetBuilder} by
     * matching field names, then builds and returns the target message.
     *
     * <p>The returned object is cast to {@code T} based on the caller's type
     * context; {@code targetBuilder} must be the builder for {@code T}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Message> T map(Message source, Message.Builder targetBuilder) {
        var targetDescriptor = targetBuilder.getDescriptorForType();

        source.getAllFields().forEach((srcField, value) -> {
            if (!SCALAR_TYPES.contains(srcField.getType())) return;

            FieldDescriptor tgtField = targetDescriptor.findFieldByName(srcField.getName());
            if (tgtField == null) return;

            if (tgtField.getType()       == srcField.getType()
             && tgtField.isRepeated()    == srcField.isRepeated()) {
                targetBuilder.setField(tgtField, value);
            }
        });

        return (T) targetBuilder.build();
    }

    private ProtoBufMapper() {}
}
