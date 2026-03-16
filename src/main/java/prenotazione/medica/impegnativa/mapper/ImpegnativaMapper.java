package prenotazione.medica.impegnativa.mapper;

import com.prenotasalute.commons.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import prenotazione.medica.impegnativa.dto.ImpegnativaDTO;
import prenotazione.medica.impegnativa.entity.Impegnativa;

/**
 * Mapper MapStruct per conversione tra {@link Impegnativa} e {@link ImpegnativaDTO}.
 * Estende {@link GenericMapper} della libreria commons.
 * Le relazioni paziente e medicoCurante sono risolte nel service tramite idPaziente e idMedicoCurante.
 */
@Mapper(componentModel = "spring")
public interface ImpegnativaMapper extends GenericMapper<Impegnativa, ImpegnativaDTO> {

    @Override
    @Mapping(source = "paziente.id", target = "idPaziente")
    @Mapping(source = "medicoCurante.id", target = "idMedicoCurante")
    @Mapping(source = "priorità", target = "priorita")
    ImpegnativaDTO toDTO(Impegnativa entity);

    @Override
    @Mapping(target = "paziente", ignore = true)
    @Mapping(target = "medicoCurante", ignore = true)
    @Mapping(target = "prestazioneSanitaria", ignore = true)
    @Mapping(source = "priorita", target = "priorità")
    Impegnativa toEntity(ImpegnativaDTO dto);

    @Override
    @Mapping(target = "paziente", ignore = true)
    @Mapping(target = "medicoCurante", ignore = true)
    @Mapping(target = "prestazioneSanitaria", ignore = true)
    @Mapping(source = "priorita", target = "priorità")
    void updateEntityFromDTO(ImpegnativaDTO dto, @MappingTarget Impegnativa entity);
}
