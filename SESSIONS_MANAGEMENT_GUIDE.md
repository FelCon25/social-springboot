# Guida all'Implementazione della Gestione Sessioni (Fai da Te)

Questa guida ti spiega passo-passo come progettare e scrivere la funzionalità che permette a un utente autenticato di **vedere i propri dispositivi connessi (sessioni attive)** e **disconnetterne uno specifico da remoto** (es. *"scollega quel vecchio tablet"*).

---

## 1. Cosa andrai a creare (Architettura REST)

Creerai un nuovo endpoint dedicato alle sessioni protetto da Spring Security:
1. **`GET /api/v1/sessions`** ➔ Restituisce la lista di tutte le sessioni attive dell'utente autenticato.
2. **`DELETE /api/v1/sessions/{id}`** ➔ Disattiva una specifica sessione (termina l'accesso per quel dispositivo).

---

## 2. I 5 Step per Scrivere il Codice

Ecco la traccia dettagliata che puoi seguire per implementare tutto da solo:

### 📑 Step 1: Creare il DTO `SessionResponse`
Per evitare di esporre l'entità JPA `Session` (che contiene il riferimento ciclico all'utente ed evita problemi di caricamento `LAZY`), crea un DTO pulito.

**Cosa fare:** Crea un record `SessionResponse.java` nel pacchetto `com.social.backend.auth.dto` con i campi:
- `Long id`
- `String ipAddress`
- `String userAgent`
- `Instant createdAt`
- `Instant expiresAt`

---

### 🗄️ Step 2: Aggiornare `SessionRepository`
Hai bisogno di un metodo che trovi tutte le sessioni di un utente che siano **attive** e **non scadute**.

**Cosa fare:** Apri [SessionRepository.java](file:///home/dany/Desktop/dev/sclprjct/backend/src/main/java/com/social/backend/auth/repository/SessionRepository.java) e aggiungi questo metodo sfruttando le query automatiche di Spring Data JPA:

```java
List<Session> findByUserIdAndIsActiveTrueAndExpiresAtAfter(Long userId, Instant now);
```

---

### ⚙️ Step 3: Implementare la logica in `SessionService`
Ora creiamo la logica di business. Ci servono due metodi principali.

**Cosa fare:** Apri [SessionService.java](file:///home/dany/Desktop/dev/sclprjct/backend/src/main/java/com/social/backend/auth/service/SessionService.java) e scrivi:

1. **`getActiveSessions(Long userId)`**:
   - Chiama il repository per recuperare le sessioni.
   - Mappa la lista di entità `Session` in una lista di `SessionResponse` (puoi usare `.stream().map(...)` o un semplice ciclo).

2. **`revokeSession(Long userId, Long sessionId)`**:
   - Recupera la sessione dal DB tramite `sessionRepository.findById(sessionId)`.
   - ⚠️ **MOLTO IMPORTANTE PER LA SICUREZZA:** Controlla che la sessione appartenga all'utente che sta facendo la richiesta (`session.getUser().getId().equals(userId)`). Se non appartiene a lui, lancia un'eccezione personalizzata (es. `SessionNotFoundException` o un errore di accesso negato).
   - Se il controllo passa, chiama `sessionRepository.deactivateById(sessionId, Instant.now())`.

---

### 🎮 Step 4: Creare il `SessionController`
Ora esponiamo le API al web. Questo controller deve essere accessibile solo agli utenti autenticati.

**Cosa fare:** Crea un nuovo file `SessionController.java` nel pacchetto `com.social.backend.auth.controller`.
1. Mappalo su `@RequestMapping("/api/v1/sessions")`.
2. Inietta il `SessionService`.
3. Crea il metodo per il `GET`:
   - Come prendi l'ID dell'utente loggato? Semplice! Usa l'annotazione di Spring Security `@AuthenticationPrincipal`:
     ```java
     @GetMapping
     public List<SessionResponse> getSessions(@AuthenticationPrincipal AuthenticatedUser currentUser) {
         return sessionService.getActiveSessions(currentUser.id());
     }
     ```
4. Crea il metodo per il `DELETE`:
   ```java
   @DeleteMapping("/{id}")
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void revokeSession(
           @PathVariable Long id,
           @AuthenticationPrincipal AuthenticatedUser currentUser) {
       sessionService.revokeSession(currentUser.id(), id);
   }
   ```

---

### 🔒 Step 5: Assicurarsi che le rotte siano protette
Verifica che le nuove rotte siano protette in [SecurityConfig.java](file:///home/dany/Desktop/dev/sclprjct/backend/src/main/java/com/social/backend/security/config/SecurityConfig.java). 
Poiché `/api/v1/sessions/**` non è incluso nell'array `PUBLIC_ENDPOINTS`, Spring Security le proteggerà automaticamente richiedendo un `AccessToken` valido!

---

## 🌟 Feature Pro (Opzionale): Come mostrare "Questo Dispositivo"

Sarebbe fantastico se nel frontend l'utente vedesse la scritta **"Dispositivo attuale"** di fianco alla sessione da cui è connesso, vero?

Per farlo, il client deve sapere quale ID sessione corrisponde all'Access Token che sta usando in quel momento.

### Come implementarlo:
1. **Aggiungi il Claim al JWT:**
   Apri [JwtService.java](file:///home/dany/Desktop/dev/sclprjct/backend/src/main/java/com/social/backend/security/jwt/JwtService.java). Modifica `generateAccessToken` in modo che accetti anche `Long sessionId` e aggiungilo come claim:
   ```java
   .claim("sid", sessionId)
   ```
2. **Aggiorna l'estrazione del Principal:**
   Aggiorna la classe `AuthenticatedUser` e il metodo `toPrincipal` in `JwtService` per includere l'ID sessione (`sid`).
3. **Passa il Session ID all'emissione dei token:**
   In `AuthService.java`, quando chiami `generateAccessToken`, passagli `session.getId()`.
4. **Aggiorna il DTO `SessionResponse`:**
   Aggiungi un campo `boolean isCurrent` nel record `SessionResponse`.
5. **Mappatura nel Service:**
   Quando mappi la sessione nel service, imposta `isCurrent` a `true` se il `session.getId()` corrisponde al `sessionId` presente nell'`AuthenticatedUser` loggato!

---

Questa implementazione darà una marcia in più alla tua applicazione e ti permetterà di padroneggiare ancora meglio Spring Boot e Spring Security. Buon codice! Se hai dubbi su un passaggio specifico, chiedi pure.
