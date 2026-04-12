package prenotazione.medica.shared.utility.mapper;

import org.mapstruct.MappingTarget;

/**
 * Contratto MapStruct per entity ↔ DTO (CRUD generico).
 *
 * @param <E>    tipo entità JPA
 * @param <DTO>  tipo DTO esposto dalle API
 */
public interface GenericMapper<E, DTO> {

    DTO toDTO(E entity);

    E toEntity(DTO dto);

    void updateEntityFromDTO(DTO dto, @MappingTarget E entity);
}
