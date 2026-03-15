package prenotazione.medica.mapper;

import com.prenotasalute.commons.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import prenotazione.medica.dto.MedicoCuranteDTO;
import prenotazione.medica.model.MedicoCurante;

/**
 * Mapper MapStruct per conversione tra {@link MedicoCurante} e {@link MedicoCuranteDTO}.
 * Estende {@link GenericMapper} della libreria commons.
 */
@Mapper(componentModel = "spring")
public interface MedicoCuranteMapper extends GenericMapper<MedicoCurante, MedicoCuranteDTO> {

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
