package co.com.bancolombia.r2dbc.foto;

import co.com.bancolombia.model.foto.Foto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FotoMapper {

    FotoEntity toEntity(Foto foto);

    Foto toDomain(FotoEntity entity);
}
