# Libreria Commons (com.prenotasalute:commons)
Ogni entità DEVE seguire questa struttura — ZERO eccezioni:
- Entity estende BaseEntity (id, createdAt, updatedAt già inclusi)
- Service estende AbstractGenericService<E, DTO, ID>
- Controller estende GenericController<DTO, ID>
- Mapper è interfaccia con @Mapper(componentModel = "spring") che estende GenericMapper<E, DTO>

NON riscrivere mai: getAll, getAllPaged, getById, create, update, delete.
Aggiungi metodi SOLO per logica specifica del dominio.

# Controller
- Estende sempre GenericController<DTO, ID>
- Usa @RestController, @RequestMapping, @RequiredArgsConstructor, @Validated
- Usa @Tag di Swagger con nome e descrizione del dominio
- Aggiungi SOLO endpoint non coperti dal GenericController
- @PreAuthorize su ogni metodo con i ruoli autorizzati
- Nessuna logica business — delega sempre al Service

# Service
- Estende sempre AbstractGenericService<E, DTO, ID>
- Usa @Service, @RequiredArgsConstructor, @Transactional(readOnly = true), @Slf4j
- Override SOLO dei metodi con logica specifica del dominio
- Chiama sempre super.metodo() per riutilizzare la logica base
- Pubblica domain events via ApplicationEventPublisher per ogni azione rilevante
- MAI loggare dati sensibili — solo ID e metadati

# Entity
- Estende sempre BaseEntity — NON ridichiarare id, createdAt, updatedAt
- Usa @Entity, @Table(name = "nome_tabella_snake_case")
- Usa @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder
- Usa @Version per optimistic locking — obbligatorio su ogni entity modificabile
- Relazioni @OneToMany e @ManyToMany sempre lazy loading
- Soft delete: is_deleted e deleted_at — MAI cancellazione fisica

# DTO
- Usa record Java per Request DTO con Bean Validation
- Usa class con @Schema di Swagger per Response DTO
- MAI esporre: version, password, dati sensibili, ID interni
- Valida SEMPRE con @Valid sugli endpoint
- Validazioni custom con @Constraint per CF, telefono, email

# Mapper
- SEMPRE interfaccia con @Mapper(componentModel = "spring") — MAI classe con @Component
- NON implementare mai manualmente toDTO, toEntity, updateEntityFromDTO
- MapStruct genera automaticamente i metodi per campi con lo stesso nome
- Usa @Mapping solo per campi con nome diverso o relazioni complesse
- Usa @Mapping(target = "campo", ignore = true) per relazioni risolte nel Service

# Repository
- Estende JpaRepository<E, ID>
- Usa proiezioni DTO per query di sola lettura
- Paginazione obbligatoria per tutte le query che restituiscono liste
- Nomi metodi descrittivi: findAllByIsDeletedFalse()

# Exception Handling
- Eccezioni custom estendono RuntimeException con errorCode
- @RestControllerAdvice con @ExceptionHandler per ogni tipo di eccezione
- EntityNotFoundException → 404
- BusinessValidationException → 422
- MethodArgumentNotValidException → 400
- MAI esporre stack trace nelle response
- Messaggi di errore tramite chiavi i18n: "error.resource.notfound"

# Error Response Standard
Ogni response usa ApiResponse<T> come envelope:
- data: il payload della risposta
- message: messaggio descrittivo
- errors: lista errori di validazione
- timestamp: data e ora della risposta

# API REST
- Versioning obbligatorio: /api/v1/...
- Path al plurale e kebab-case: /api/v1/richieste-mediche
- Paginazione obbligatoria per liste, default size=20, max=100
- Sempre ResponseEntity con status code semanticamente corretto
- Swagger: @Tag sul Controller, @Operation su ogni endpoint, @Schema sui DTO

# Performance
- Paginazione obbligatoria, default size=20, max=100
- Proiezioni DTO per query di sola lettura
- Lazy loading default per @OneToMany e @ManyToMany
- Evita N+1 con @EntityGraph o JOIN FETCH

# Logging
- Usa SEMPRE @Slf4j di Lombok — mai System.out.println()
- ERROR: bug critici, WARN: anomalie, INFO: eventi business, DEBUG: solo sviluppo
- MAI loggare dati sensibili (password, token, CF, diagnosi)
- Formato: log.info("Evento: id={}", id) — mai concatenazione di stringhe

# Configurazione
- Usa application.yml, non application.properties
- Profili separati: dev, test, prod
- MAI valori sensibili nel codice — usa sempre variabili d'ambiente ${JWT_SECRET}
- Hibernate ddl-auto: none — schema gestito esclusivamente da Flyway

# Variabili d'Ambiente
- MAI hardcodare valori sensibili nel codice
- SEMPRE aggiornare .env.example quando aggiungi una nuova variabile
- MAI committare il file .env — solo .env.example

# Convenzioni di Naming
- Classi Java: PascalCase → PazienteService
- Metodi/Variabili: camelCase → findByCodiceFiscale()
- Costanti: UPPER_SNAKE_CASE → MAX_RETRY_ATTEMPTS
- Tabelle/Colonne DB: snake_case → data_nascita
- Endpoint REST: kebab-case → /api/v1/richieste-mediche

# Commit Message Standard