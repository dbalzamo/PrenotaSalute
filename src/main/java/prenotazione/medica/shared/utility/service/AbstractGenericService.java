package prenotazione.medica.shared.utility.service;

import prenotazione.medica.shared.utility.mapper.GenericMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import prenotazione.medica.shared.exception.ResourceNotFoundException;

import java.io.Serializable;
import java.util.List;

/**
 * CRUD generico su repository JPA + mapper MapStruct.
 */
public abstract class AbstractGenericService<E, DTO, ID extends Serializable> {

    protected final JpaRepository<E, ID> repository;
    protected final GenericMapper<E, DTO> mapper;

    protected AbstractGenericService(JpaRepository<E, ID> repository, GenericMapper<E, DTO> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public DTO findById(ID id) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("resource.notfound", id));
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<DTO> findAll() {
        return repository.findAll().stream().map(mapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Page<DTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    @Transactional
    public DTO create(DTO dto) {
        E entity = mapper.toEntity(dto);
        E saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional
    public DTO update(ID id, DTO dto) {
        E entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("resource.notfound", id));
        mapper.updateEntityFromDTO(dto, entity);
        return mapper.toDTO(repository.save(entity));
    }

    @Transactional
    public void deleteById(ID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("resource.notfound", id);
        }
        repository.deleteById(id);
    }
}
