package co.com.bancolombia.model.inmueble;

import co.com.bancolombia.model.foto.Foto;

import java.util.List;

public record InmuebleConFotos(Inmueble inmueble, List<Foto> photos) {
}
