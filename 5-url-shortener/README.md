# 🔗 URL Shortener API

Encurtador de URLs em **Spring Boot 3** + **Java 21**, com cache em memória via Caffeine, codificação **Base62**, validação anti-XSS e tracking de cliques assíncrono usando Virtual Threads.

---

## 🎯 O que o projeto demonstra

- **Algoritmo Base62** para gerar códigos curtos de forma determinística a partir do ID — sem risco de colisão e sem custo de retry
- **Cache em memória com Caffeine** no caminho crítico de redirect (a operação mais frequente do sistema)
- **Processamento assíncrono com `@Async` + Virtual Threads** (Java 21) para não bloquear o redirect com o tracking de cliques
- **Validação contra XSS**: bloqueia esquemas perigosos (`javascript:`, `data:`, `file:`) antes da persistência
- **Suporte a `X-Forwarded-For`** para identificar IP real atrás de proxy reverso (Cloudflare, Render, Nginx)
- **API REST** com status codes semânticos: `201 Created`, `302 Found`, `404 Not Found`, `410 Gone` (expirada)
- **Multi-profile** (dev com H2, prod com PostgreSQL) — mesmo código, ambientes diferentes
- **Testes unitários e de integração** com JUnit 5 + Mockito + MockMvc

---

## 🛠️ Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 (LTS, Virtual Threads) |
| Framework | Spring Boot 3.3 |
| Persistência | Spring Data JPA / Hibernate |
| Cache | Caffeine |
| Async | Spring `@Async` + Virtual Threads |
| Banco (dev) | H2 (memória) |
| Banco (prod) | PostgreSQL |
| Documentação | SpringDoc OpenAPI 2.6 |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc |
| Build | Maven |
| Container | Docker (multi-stage) |

---

## 🧭 Arquitetura

```
┌──────────────────┐
│   POST /api/urls │ → cria URL curta (Base62 do ID auto-incremental)
│   GET /api/urls/ │ → consulta estatísticas
│   {code}/stats   │
│   DELETE         │ → remove (invalida cache)
└────────┬─────────┘
         │
         ▼
┌────────────────────┐
│   GET /{code}      │ → 302 redirect (operação mais frequente)
│                    │
│   ┌────────────┐   │
│   │   Cache    │   │ ← Caffeine: 5min TTL, 10k entradas
│   │ (Caffeine) │   │
│   └─────┬──────┘   │
│         │ miss     │
│         ▼          │
│   ┌────────────┐   │   ┌─────────────┐
│   │ Repository │───┼──→│ PostgreSQL  │
│   └────────────┘   │   └─────────────┘
│                    │
│   @Async tracking ─┼──→ Click table (não bloqueia resposta)
└────────────────────┘
```

---

## 📁 Estrutura

```
src/main/java/com/lubj9/urlshortener/
├── UrlShortenerApplication.java
├── controller/
│   ├── UrlController.java         (CRUD + stats)
│   └── RedirectController.java    (lida com /{code})
├── service/
│   ├── UrlService.java            (lógica principal + @Cacheable/@CacheEvict)
│   ├── UrlValidator.java          (anti-XSS, validação semântica)
│   └── ClickTrackerService.java   (@Async)
├── repository/
│   ├── UrlRepository.java
│   └── ClickRepository.java       (queries customizadas com @Query)
├── model/
│   ├── Url.java
│   └── Click.java
├── dto/
│   ├── CreateUrlRequest.java
│   ├── UrlResponse.java
│   └── StatsResponse.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── InvalidUrlException.java
│   ├── ExpiredUrlException.java
│   └── GlobalExceptionHandler.java
├── config/
│   ├── OpenAPIConfig.java
│   └── AsyncConfig.java           (Virtual Threads)
└── util/
    └── Base62.java                (algoritmo de encoding)

src/test/java/com/lubj9/urlshortener/
├── util/Base62Test.java           (8 testes — fronteira, round-trip, erros)
├── service/UrlValidatorTest.java  (5 testes — XSS, esquemas, fronteira)
└── controller/UrlControllerIT.java (4 testes E2E)
```

---

## 🚀 Como rodar

### Pré-requisitos
- Java 21+ ([Temurin](https://adoptium.net/))
- Maven 3.9+

### Localmente

```bash
git clone https://github.com/lubj9/url-shortener.git
cd url-shortener
mvn spring-boot:run
```

API sobe em `http://localhost:8080`.

### URLs úteis

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Console H2 | http://localhost:8080/h2-console (JDBC: `jdbc:h2:mem:urlshortener`) |

### Com Docker

```bash
docker build -t url-shortener .
docker run -p 8080:8080 \
  -e DATABASE_URL=postgresql://user:senha@host:5432/banco \
  -e APP_BASE_URL=https://meudominio.com \
  -e SPRING_PROFILES_ACTIVE=prod \
  url-shortener
```

### Testes

```bash
mvn test
```

---

## 🌐 Endpoints

### Criar URL curta

```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.mackenzie.br"}'
```

Resposta `201 Created`:
```json
{
  "code": "1",
  "shortUrl": "http://localhost:8080/1",
  "originalUrl": "https://www.mackenzie.br",
  "createdAt": "2026-06-24T10:00:00Z",
  "expiresAt": null
}
```

### Seguir o redirect

```bash
curl -I http://localhost:8080/1
```

Resposta `302 Found` com `Location: https://www.mackenzie.br`.

### Estatísticas

```bash
curl http://localhost:8080/api/urls/1/stats
```

```json
{
  "code": "1",
  "originalUrl": "https://www.mackenzie.br",
  "totalClicks": 47,
  "createdAt": "2026-06-24T10:00:00Z",
  "lastClickAt": "2026-06-24T15:23:11Z",
  "expiresAt": null,
  "expired": false
}
```

### Outros

- `DELETE /api/urls/{code}` → `204 No Content`
- URL com `expiresAt` no passado → `410 Gone` no redirect
- URL com esquema `javascript:` → `400 Bad Request`

Há também o arquivo `requests.http` para testar via IntelliJ ou plugin REST Client do VS Code.

---

## 🧠 Decisões de design

| Decisão | Por quê |
|---|---|
| **Base62 encoding do ID** | Códigos curtos (`1`, `2`, `Z`, `10`, `100`, ...) sem colisão e sem necessidade de retry. 62⁶ ≈ 56 bilhões de combinações com 6 caracteres. |
| **Não usar UUID** | UUIDs têm ~36 caracteres — muito longos para uma URL curta. Difícil de digitar, ocupam mais espaço em link no Twitter, em QR codes. |
| **Não usar Base64** | Base64 inclui `+`, `/`, `=` — caracteres que precisam de URL-encoding. Base62 (alfanumérico puro) é URL-safe sem escape. |
| **Cache no caminho de redirect** | Redirect é a operação mais frequente do sistema (10x+ mais que criação). Caffeine com TTL de 5min reduz drasticamente a carga no banco. |
| **`@Async` no tracking de cliques** | Tracking é importante mas não crítico. O usuário não pode esperar pelo I/O do banco para ser redirecionado — adicionaria 10-100ms de latência. |
| **Virtual Threads (Java 21) no pool async** | Cada virtual thread custa ~kilobytes. Em pico de tráfego com 10k cliques simultâneos, é melhor que um pool fixo de 50 threads de plataforma. |
| **Validação anti-XSS no validator** | URLs `javascript:`, `data:`, `file:` poderiam ser usadas em ataques de phishing. Bloquear na entrada é defesa em profundidade. |
| **`X-Forwarded-For`** | Em produção atrás de Cloudflare/Render/Nginx, `request.getRemoteAddr()` retorna o IP do proxy, não do usuário. O header `X-Forwarded-For` carrega o IP real. |
| **`410 Gone` para URL expirada** | Mais semântico que `404 Not Found`: comunica ao cliente que o recurso existiu mas foi removido. Importante para SEO e crawlers. |
| **Reutilizar URLs sem expiração** | Se o usuário cria a mesma URL duas vezes, devolve o mesmo código. Evita inchar o banco e mantém estatísticas agregadas em um único registro. |

---

## ⚡ Performance

O cache faz toda a diferença no caminho crítico:

- **Sem cache** (cada redirect bate no banco): ~5-15ms por request
- **Com cache** (resolução em memória): ~0,1-0,5ms por request

Para um link viral com 1M de acessos/dia, isso significa milhões de queries economizadas no PostgreSQL.

---

## 🔮 Próximos passos

- [ ] Rate limiting por IP (Bucket4j) para evitar abuso
- [ ] Autenticação JWT — URLs privadas por usuário
- [ ] Sistema de quotas por usuário (free tier: 100 URLs/mês)
- [ ] Detecção de URLs phishing via integração com Google Safe Browsing
- [ ] Estatísticas avançadas (referer, geo-IP, navegador)
- [ ] Cache distribuído com Redis quando escalar horizontalmente
- [ ] QR Code generation para cada URL curta
- [ ] Custom slugs (`/meu-link` em vez de `/abc123`) com validação de unicidade
- [ ] Métricas com Spring Actuator + Prometheus

---

## 👤 Autor

**Lucas Zeferino Baracat**
Estudante de Sistemas de Informação na Universidade Presbiteriana Mackenzie
[LinkedIn](https://www.linkedin.com/in/lucasbaracat9/) · [GitHub](https://github.com/lubj9)
