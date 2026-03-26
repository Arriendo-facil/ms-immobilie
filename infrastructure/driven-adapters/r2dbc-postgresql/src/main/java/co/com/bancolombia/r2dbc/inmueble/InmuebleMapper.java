package co.com.bancolombia.r2dbc.inmueble;

import co.com.bancolombia.model.inmueble.BusinessType;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.PropertyType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InmuebleMapper {

    InmuebleEntity toEntity(Inmueble inmueble);

    Inmueble toDomain(InmuebleEntity entity);

    default String businessTypeToString(BusinessType type) {
        return type != null ? type.name() : null;
    }

    default BusinessType stringToBusinessType(String value) {
        return value != null ? BusinessType.valueOf(value) : null;
    }

    default String propertyTypeToString(PropertyType type) {
        return type != null ? type.name() : null;
    }

    default PropertyType stringToPropertyType(String value) {
        return value != null ? PropertyType.valueOf(value) : null;
    }

    default String inmuebleStatusToString(InmuebleStatus status) {
        return status != null ? status.name() : null;
    }

    default InmuebleStatus stringToInmuebleStatus(String value) {
        return value != null ? InmuebleStatus.valueOf(value) : null;
    }
}
