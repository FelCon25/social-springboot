# Guida al Test del Refresh Token Endpoint su Postman

Questa guida ti mostrerà passo-passo come testare il meccanismo di **Refresh Token Rotation** e **Token Theft Detection** della tua applicazione Spring Boot utilizzando Postman.

## Prerequisiti

Assicurati che:
1. Il tuo backend Spring Boot sia in esecuzione (porta predefinita, es. `8080`).
2. Tu abbia già registrato un utente di test tramite l'endpoint `/api/v1/auth/register`.

---

## 1. Ottenere i Token (Login)

Prima di poter testare il refresh, hai bisogno di un `refreshToken` valido.

1. Crea una nuova Request in Postman:
   - **Metodo:** `POST`
   - **URL:** `http://localhost:8080/api/v1/auth/login`
2. Vai nel tab **Body**, seleziona **raw** e scegli **JSON**.
3. Inserisci le credenziali del tuo utente:
   ```json
   {
       "identifier": "la_tua_email_o_username",
       "password": "la_tua_password"
   }
   ```
4. Clicca su **Send**.
5. Nella risposta (Status `200 OK`), riceverai un JSON simile a questo:
   ```json
   {
       "accessToken": "eyJh...",
       "refreshToken": "una_stringa_opaca_molto_lunga",
       "tokenType": "Bearer"
   }
   ```
6. **Copia** il valore del `refreshToken`.

---

## 2. Test 1: Refresh Token con Successo (Rotation)

Questo test verifica che un token valido venga ruotato correttamente, emettendo nuovi token.

1. Crea una nuova Request in Postman:
   - **Metodo:** `POST`
   - **URL:** `http://localhost:8080/api/v1/auth/refresh`
2. Vai nel tab **Body**, seleziona **raw** e scegli **JSON**.
3. Inserisci il token che hai copiato al passo precedente:
   ```json
   {
       "refreshToken": "IL_TUO_REFRESH_TOKEN_SALVATO_PRIMA"
   }
   ```
4. Clicca su **Send**.
5. **Risultato atteso (Status `200 OK`):** 
   - Riceverai una nuova coppia di `accessToken` e `refreshToken`.
   - Il `refreshToken` che hai appena inviato verrà contrassegnato come *usato* (`usedAt` != null) nel database.

> 💡 **Nota:** Ricordati di copiare il **nuovo** `refreshToken` appena ricevuto. D'ora in poi, l'app dovrà usare questo!

---

## 3. Test 2: Rilevamento Furto del Token (Token Theft Detection)

Ora testeremo il sistema di sicurezza. Cosa succede se un hacker ruba un token vecchio e prova a usarlo?

1. Torna alla Request creata nel **Test 1** (`/api/v1/auth/refresh`).
2. Lascia nel body il **vecchio** `refreshToken` (quello che hai appena consumato nel Test 1).
3. Clicca su **Send**.
4. **Risultato atteso (Status `401 Unauthorized` o errore gestito):**
   - L'API rifiuterà la richiesta lanciando la tua `InvalidRefreshTokenException`.
   - **Dietro le quinte:** Il `RefreshTokenService` avrà revocato TUTTI i refresh token legati a quella sessione e avrà disattivato la sessione (`sessionRepository.deactivateById`).
5. **Verifica:** Se ora provi a usare il **nuovo** `refreshToken` ottenuto nel Test 1, fallirà anche lui! La sessione intera è stata chiusa per sicurezza.

---

## 4. Test 3: Utilizzo di Token Alterato o Inesistente

Verifichiamo che il backend blocchi token inventati.

1. Crea una nuova request verso `/api/v1/auth/refresh`.
2. Inserisci nel body un token finto:
   ```json
   {
       "refreshToken": "token-finto-12345"
   }
   ```
3. Clicca su **Send**.
4. **Risultato atteso (Status `401 Unauthorized`):** Il sistema non troverà l'hash nel database e lancerà la `InvalidRefreshTokenException`.

---

## 5. Controllo del Database (Opzionale)

Se hai un client database (es. DBeaver, pgAdmin), puoi lanciare queste query per verificare cosa è successo sotto il cofano durante i test:

```sql
-- Controlla lo stato delle sessioni (vedrai isActive = false dopo il Test 2)
SELECT * FROM sessions;

-- Controlla la storia dei token
SELECT id, session_id, is_revoked, used_at, replaced_by, created_at
FROM refresh_tokens 
ORDER BY id DESC;
```
Noterai che grazie al tuo codice:
- Il primo token ha un `used_at` valorizzato e il `replaced_by` che punta al secondo token.
- Dopo l'attacco (Test 2), tutti i token della sessione hanno `is_revoked = true` (grazie a `revokeAllBySessionId`).
