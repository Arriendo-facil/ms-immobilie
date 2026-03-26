package co.com.bancolombia.api.mapper;

import co.com.bancolombia.api.dto.inmueble.CreateInmuebleDto;
import co.com.bancolombia.api.dto.inmueble.FotoDto;
import co.com.bancolombia.api.dto.inmueble.FotoResponse;
import co.com.bancolombia.api.dto.inmueble.InmuebleResponse;
import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleConFotos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InmuebleApiMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "pausedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Inmueble toInmueble(CreateInmuebleDto dto, String userId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "propertyId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Foto toFoto(FotoDto dto);

    List<Foto> toFotos(List<FotoDto> dtos);

    FotoResponse toFotoResponse(Foto foto);

    @Mapping(source = "inmueble.id", target = "id")
    @Mapping(source = "inmueble.userId", target = "userId")
    @Mapping(source = "inmueble.title", target = "title")
    @Mapping(source = "inmueble.description", target = "description")
    @Mapping(source = "inmueble.squareMeters", target = "squareMeters")
    @Mapping(source = "inmueble.price", target = "price")
    @Mapping(source = "inmueble.businessType", target = "businessType")
    @Mapping(source = "inmueble.propertyType", target = "propertyType")
    @Mapping(source = "inmueble.status", target = "status")
    @Mapping(source = "inmueble.department", target = "department")
    @Mapping(source = "inmueble.country", target = "country")
    @Mapping(source = "inmueble.city", target = "city")
    @Mapping(source = "inmueble.fullAddress", target = "fullAddress")
    @Mapping(source = "inmueble.publishedAt", target = "publishedAt")
    @Mapping(source = "inmueble.expiresAt", target = "expiresAt")
    @Mapping(source = "inmueble.createdAt", target = "createdAt")
    @Mapping(source = "photos", target = "fotos")
    InmuebleResponse toResponse(InmuebleConFotos result);
}
