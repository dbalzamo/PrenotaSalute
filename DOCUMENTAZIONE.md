# Documentazione architetturale – Prenotazione Medica (Backend)

Questo documento descrive in modo sintetico l’architettura del backend e dove trovare i dettagli nel codice. **Ogni classe Java è documentata con JavaDoc**: apri una classe nell’IDE e leggi la descrizione della classe, il ruolo nell’architettura e i metodi principali.

---

## 1. Punto di ingresso e configurazione

| Classe | Ruolo |
|--------|--------|
| **PrenotazioneMedicaApplication** | Avvio Spring Boot, abilita scheduling. |
| **ModelMapperConfig** | Bean `ModelMapper` per conversione entità ↔ DTO. |
| **WebSocketConfig** | Configurazione STOMP: endpoint `/ws`, prefissi `/app`, `/user/queue`, handshake JWT e interceptor SecurityContext. |
| **JwtHandshakeHandler** | Durante l’handshake WebSocket legge il JWT (es. da query `?token=...`) e imposta il Principal (username) sulla sessione. |
| **StompSecurityContextInterceptor** | Per ogni messaggio STOMP in ingresso (es. `/app/chat.send`) imposta il SecurityContext a partire dal Principal, così l’utente è identificabile. |

---

## 2. Modelli (entità JPA)

| Classe | Ruolo |
|--------|--------|
| **Account** | Credenziali e ruolo (PAZIENTE / MEDICO_CURANTE). Punto di ingresso per autenticazione. Relazioni opzionali con Paziente e MedicoCurante. |
| **Paziente** | Profilo anagrafico del paziente, collegato a Account; possiede richieste mediche, impegnative e medico curante associato. |
| **MedicoCurante** | Profilo del medico, collegato a Account; possiede richieste e impegnative. |
| **Message** | Messaggio della Posta (senderId/receiverId = id Account, content, read, sentAt). |
| **RichiestaMedica** | Richiesta del paziente al medico (stato, tipo, descrizione, date). |
| **Impegnativa** | Prescrizione emessa dal medico (regione, NRE, priorità, collegata a RichiestaMedica e PrestazioneSanitaria). |
| **PrestazioneSanitaria** | Dettaglio tecnico collegato a un’impegnativa. |
| **UserDetailsImpl** | Implementazione Spring Security di `UserDetails` costruita a partire da un `Account` (id, username, ruoli). |

---

## 3. DTO (request / response)

- **Request:** `AuthRequest`, `SignupRequest`, `RichiestaMedicaRequest`, `RifiutoRichiestaRequest`, `ImpegnativaRequest` – body delle POST/PUT.
- **Response:** `AuthResponse` (JWT, ruoli), `SignupResponse`, `MessageResponse`, `MedicoCuranteResponse`, `RichiestaMedicaMedicoResponse`, `ImpegnativaResponse`, ecc.
- **Altri DTO:** `MessageDTO`, `ConversazionePreviewDTO`, `PazientePerMessaggioDTO`, `MedicoCuranteListItemDTO`, `PazienteDTO`, `PrestazioneSanitariaDTO`.

Ogni DTO ha in JavaDoc lo scopo e l’endpoint o il contesto in cui viene usato.

---

## 4. Enum

- **ERuolo** – Ruoli utente (PAZIENTE, MEDICO_CURANTE, …).
- **EStatoRichiesta** – Stati richiesta (INVIATA, ACCETTATA, RIFIUTATA, SCADUTA, …).
- **ETipoRichiesta** – Tipo richiesta/impegnativa (VISITA, ESAME, …).
- **EPrioritàPrescrizione** – Priorità prescrizione (URGENTE, BREVE, …).

---

## 5. Repository (accesso dati)

Tutti estendono `JpaRepository<Entità, Long>` e sono documentati con il proprio ruolo e i consumer principali.

- **AccountRepository** – findByUsername, existsByUsername/Email, usato da UserDetailsServiceImpl, AccountService, ChatController.
- **PazienteRepository** – findByAccountId, findByMedicoCurante_Id.
- **MedicoCuranteRepository** – findByAccountId.
- **MessageRepository** – findConversation, findLatestInConversation, countByReceiverIdAndReadFalse, countByReceiverIdAndSenderIdAndReadFalse.
- **RichiestaMedicaRepository** – filtri per stato/paziente/medico, scadutaRichieste (job).
- **ImpegnativaRepository** – CRUD base.

---

## 6. Servizi (logica di business)

| Servizio | Ruolo |
|----------|--------|
| **AccountService** | Login (JWT + cookie), logout, creazione account in signup. |
| **UserDetailsServiceImpl** | Caricamento utente per username (per filtro JWT e WebSocket). |
| **MessageService** | Invio messaggi (persistenza + notifica WebSocket), conversazioni, mark as read, conteggio non letti, anteprime lista chat medico. |
| **MedicoCuranteService** | Ricerca medico, creazione in signup, elenco pazienti, elenco medici per signup. |
| **PazienteService** | Ricerca paziente, creazione in signup, medico curante (GET/PUT), DTO medico per Posta. |
| **RichiestaMedicaService** | CRUD richieste, visualizza/accetta/rifiuta, job scadenza, notifiche. |
| **ImpegnativaService** | Generazione impegnativa da richiesta accettata. |

---

## 7. Controller (API REST e WebSocket)

| Controller | Ruolo |
|------------|--------|
| **AuthController** | POST login, logout, signup; GET medici-curanti. |
| **ChatController** | WebSocket `/app/chat.send`; REST /api/messages (invia, conversazione, read, unread/count). |
| **MedicoCuranteController** | CRUD /api/v1/medici-curanti; GET /me, /pazienti, /conversazioni. |
| **PazienteController** | CRUD /api/v1/pazienti; GET /me, /mio-medico; PUT /mio-medico, /updatePaziente. |
| **RichiestaMedicaController** | CRUD /api/v1/richieste-mediche; crea-richiesta, mie-richieste, medico/richieste, trova-richiesta, visualizza, accetta, rifiuta. |
| **ImpegnativaController** | CRUD /api/v1/impegnative; POST /genera-impegnativa. |
| **PrestazioneSanitariaController** | CRUD /api/v1/prestazioni-sanitarie. |

---

## 8. Sicurezza

| Classe | Ruolo |
|--------|--------|
| **WebSecurityConfig** | Configurazione HTTP: path pubblici (/api/auth/**, /ws/**), sessione STATELESS, filtro JWT, entry point 401 JSON. |
| **JwtService** | Generazione/validazione JWT, estrazione da header/cookie/query; generazione cookie. |
| **JwtAuthenticationFilter** | Per ogni richiesta HTTP estrae il JWT, valida, carica UserDetails e imposta SecurityContext. |
| **AuthEntryPointJwt** | Risposta 401 JSON quando si accede a risorsa protetta senza autenticazione. |
| **SecurityUtils** | Restituisce l’id account corrente dal SecurityContext (per controller REST). |

---

## Flusso tipico

1. **Login:** richiesta POST /api/auth/login → AccountService.loginAccount → AuthenticationManager + JwtService → risposta con JWT e cookie.
2. **Richieste successive:** header `Authorization: Bearer <token>` (o cookie) → JwtAuthenticationFilter valida il token, carica UserDetails, imposta SecurityContext → controller usa SecurityUtils.getCurrentAccountId() o @PreAuthorize.
3. **WebSocket (Posta):** client si connette a `/ws?token=...` → JwtHandshakeHandler valida il token e imposta il Principal sulla sessione → messaggi su `/app/chat.send` → StompSecurityContextInterceptor (opzionale) e ChatController.sendMessage(Principal) ricavano l’account e delegano a MessageService.
4. **Messaggio inviato:** MessageService salva il messaggio, poi SimpMessagingTemplate.convertAndSendToUser(username, "/queue/messages", response) notifica il destinatario in tempo reale.

Per lo studio del codice, apri le classi nell’ordine che ti interessa (es. AuthController → AccountService → JwtService) e leggi i JavaDoc in cima alla classe e ai metodi principali.
