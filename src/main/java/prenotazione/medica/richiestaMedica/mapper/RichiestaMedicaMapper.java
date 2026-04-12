package prenotazione.medica.richiestaMedica.mapper;

import prenotazione.medica.shared.utility.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import prenotazione.medica.richiestaMedica.entity.RichiestaMedica;
import prenotazione.medica.richiestaMedica.dto.RichiestaMedicaDTO;

/**
 * Mapper MapStruct per conversione tra {@link RichiestaMedica} e {@link RichiestaMedicaDTO}.
 * Estende {@link GenericMapper} della libreria commons.
 * Le relazioni paziente e medicoCurante sono risolte nel service tramite idPaziente e idMedicoCurante.
 */
@Mapper(componentModel = "spring")
public interface RichiestaMedicaMapper extends GenericMapper<RichiestaMedica, RichiestaMedicaDTO> {

    @Override
    @Mapping(source = "paziente.id", target = "idPaziente")
    @Mapping(source = "medicoCurante.id", target = "idMedicoCurante")
    RichiestaMedicaDTO toDTO(RichiestaMedica entity);

    @Override
    @Mapping(target = "paziente", ignore = true)
    @Mapping(target = "medicoCurante", ignore = true)
    RichiestaMedica toEntity(RichiestaMedicaDTO dto);

    @Override
    @Mapping(target = "paziente", ignore = true)
    @Mapping(target = "medicoCurante", ignore = true)
    void updateEntityFromDTO(RichiestaMedicaDTO dto, @MappingTarget RichiestaMedica entity);
}
