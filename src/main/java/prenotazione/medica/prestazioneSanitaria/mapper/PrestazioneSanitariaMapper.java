package prenotazione.medica.prestazioneSanitaria.mapper;

import prenotazione.medica.shared.utility.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import prenotazione.medica.prestazioneSanitaria.dto.PrestazioneSanitariaDTO;
import prenotazione.medica.prestazioneSanitaria.entity.PrestazioneSanitaria;

/**
 * Mapper MapStruct per conversione tra {@link PrestazioneSanitaria} e {@link PrestazioneSanitariaDTO}.
 * Estende {@link GenericMapper} della libreria commons.
 */
@Mapper(componentModel = "spring")
public interface PrestazioneSanitariaMapper extends GenericMapper<PrestazioneSanitaria, PrestazioneSanitariaDTO> {

    @Override
    PrestazioneSanitariaDTO toDTO(PrestazioneSanitaria entity);

    @Override
    @Mapping(target = "impegnativa", ignore = true)
    PrestazioneSanitaria toEntity(PrestazioneSanitariaDTO dto);

    @Override
    @Mapping(target = "impegnativa", ignore = true)
    void updateEntityFromDTO(PrestazioneSanitariaDTO dto, @MappingTarget PrestazioneSanitaria entity);
}
