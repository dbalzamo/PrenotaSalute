package prenotazione.medica.mapper;

import com.prenotasalute.commons.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import prenotazione.medica.dto.PazienteDTO;
import prenotazione.medica.model.Paziente;

/**
 * Mapper MapStruct per conversione tra {@link Paziente} e {@link PazienteDTO}.
 * Estende {@link GenericMapper} della libreria commons.
 */
@Mapper(componentModel = "spring")
public interface PazienteMapper extends GenericMapper<Paziente, PazienteDTO> {

    @Override
    @Mapping(source = "account.email", target = "email")
    @Mapping(source = "dataDiNascita", target = "dataDiNascita", dateFormat = "yyyy-MM-dd")
    PazienteDTO toDTO(Paziente entity);

    @Override
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "richiesteMediche", ignore = true)
    @Mapping(target = "impegnative", ignore = true)
    @Mapping(target = "medicoCurante", ignore = true)
    @Mapping(target = "dataDiNascita", ignore = true)
    Paziente toEntity(PazienteDTO dto);

    @Override
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "richiesteMediche", ignore = true)
    @Mapping(target = "impegnative", ignore = true)
    @Mapping(target = "medicoCurante", ignore = true)
    @Mapping(target = "dataDiNascita", ignore = true)
    void updateEntityFromDTO(PazienteDTO dto, @MappingTarget Paziente entity);
}
