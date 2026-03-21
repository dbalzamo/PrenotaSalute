package prenotazione.medica.paziente.mapper;

import com.prenotasalute.commons.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import prenotazione.medica.auth.dto.request.SignupRequest;
import prenotazione.medica.paziente.dto.PazienteDTO;
import prenotazione.medica.paziente.entity.Paziente;

/**
 * Mapper MapStruct per conversione tra {@link Paziente} e {@link PazienteDTO}.
 * Estende {@link GenericMapper} della libreria commons; include mapping signup → entity.
 */
@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PazienteMapper extends GenericMapper<Paziente, PazienteDTO> {

    /**
     * Profilo paziente da registrazione (username/password/ruolo/medicoCuranteId gestiti nel service).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "medicoCurante", ignore = true)
    @Mapping(target = "richiesteMediche", ignore = true)
    @Mapping(target = "impegnative", ignore = true)
    Paziente toEntityFromSignupRequest(SignupRequest request);

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

