package prenotazione.medica.medico.mapper;

import com.prenotasalute.commons.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import prenotazione.medica.auth.dto.request.SignupRequest;
import prenotazione.medica.medico.dto.MedicoCuranteDTO;
import prenotazione.medica.medico.entity.MedicoCurante;

/**
 * Mapper MapStruct per conversione tra {@link MedicoCurante} e {@link MedicoCuranteDTO}.
 * Estende {@link GenericMapper} della libreria commons; include mapping signup → entity.
 */
@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface MedicoCuranteMapper extends GenericMapper<MedicoCurante, MedicoCuranteDTO> {

    /**
     * Profilo medico da registrazione (username/password/ruolo gestiti nel service).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "richiesteMediche", ignore = true)
    @Mapping(target = "impegnative", ignore = true)
    MedicoCurante toEntityFromSignupRequest(SignupRequest request);

    @Override
    @Mapping(source = "account.email", target = "email")
    @Mapping(source = "dataDiNascita", target = "dataDiNascita", dateFormat = "yyyy-MM-dd")
    MedicoCuranteDTO toDTO(MedicoCurante entity);

    @Override
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "richiesteMediche", ignore = true)
    @Mapping(target = "impegnative", ignore = true)
    @Mapping(target = "dataDiNascita", ignore = true)
    MedicoCurante toEntity(MedicoCuranteDTO dto);

    @Override
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "richiesteMediche", ignore = true)
    @Mapping(target = "impegnative", ignore = true)
    @Mapping(target = "dataDiNascita", ignore = true)
    void updateEntityFromDTO(MedicoCuranteDTO dto, @MappingTarget MedicoCurante entity);
}

