# Resumen del analisis de SonarQube

## eventos-gamer-web

Quality Gate: **ERROR**

- falla `reliability_rating`: 3 (GT 1)
- falla `security_rating`: 4 (GT 1)
- falla `critical_violations`: 1 (GT 0)
- falla `vulnerabilities`: 3 (GT 0)

Medidas: coverage 0.0, duplicated_lines_density 0.0, ncloc 2718, reliability_rating 3.0, security_rating 4.0, sqale_rating 1.0

| Tipo | BLOCKER | CRITICAL | MAJOR | MINOR | INFO | Total |
|---|---|---|---|---|---|---|
| VULNERABILITY | 0 | 1 | 2 | 0 | 0 | 3 |
| BUG | 0 | 0 | 7 | 0 | 0 | 7 |
| CODE_SMELL | 0 | 0 | 1 | 0 | 0 | 1 |

Security Hotspots: 0 (revisados 0), por prioridad {}

Vulnerabilidades y problemas bloqueantes o criticos:

- [VULNERABILITY / CRITICAL] java:S9345 src/main/java/cl/duoc/gamer/web/modelo/EnlacesCompartir.java:18: Make this class "final" or make this throwing constructor "private".
- [VULNERABILITY / MAJOR] java:S5693 src/main/resources/application.properties:27: The content length limit of 26214400 bytes is greater than the defined limit of 8388608; make sure it is safe here.
- [VULNERABILITY / MAJOR] java:S5693 src/main/resources/application.properties:28: The content length limit of 27262976 bytes is greater than the defined limit of 8388608; make sure it is safe here.

## eventos-gamer-ms-usuarios

Quality Gate: **ERROR**

- falla `security_rating`: 4 (GT 1)
- falla `critical_violations`: 1 (GT 0)
- falla `vulnerabilities`: 1 (GT 0)

Medidas: coverage 44.3, duplicated_lines_density 0.0, ncloc 680, reliability_rating 1.0, security_rating 4.0, sqale_rating 1.0

| Tipo | BLOCKER | CRITICAL | MAJOR | MINOR | INFO | Total |
|---|---|---|---|---|---|---|
| VULNERABILITY | 0 | 1 | 0 | 0 | 0 | 1 |
| BUG | 0 | 0 | 0 | 0 | 0 | 0 |
| CODE_SMELL | 0 | 0 | 0 | 0 | 0 | 0 |

Security Hotspots: 0 (revisados 0), por prioridad {}

Vulnerabilidades y problemas bloqueantes o criticos:

- [VULNERABILITY / CRITICAL] java:S4502 src/main/java/cl/duoc/gamer/usuarios/config/SeguridadConfig.java:39: Make sure disabling Spring Security's CSRF protection is safe here.

## eventos-gamer-ms-eventos

Quality Gate: **ERROR**

- falla `reliability_rating`: 3 (GT 1)
- falla `security_rating`: 4 (GT 1)
- falla `critical_violations`: 1 (GT 0)
- falla `vulnerabilities`: 3 (GT 0)

Medidas: coverage 28.3, duplicated_lines_density 2.4, ncloc 1866, reliability_rating 3.0, security_rating 4.0, sqale_rating 1.0

| Tipo | BLOCKER | CRITICAL | MAJOR | MINOR | INFO | Total |
|---|---|---|---|---|---|---|
| VULNERABILITY | 0 | 1 | 2 | 0 | 0 | 3 |
| BUG | 0 | 0 | 2 | 0 | 0 | 2 |
| CODE_SMELL | 0 | 0 | 0 | 0 | 0 | 0 |

Security Hotspots: 0 (revisados 0), por prioridad {}

Vulnerabilidades y problemas bloqueantes o criticos:

- [VULNERABILITY / MAJOR] java:S5693 src/main/resources/application.properties:21: The content length limit of 26214400 bytes is greater than the defined limit of 8388608; make sure it is safe here.
- [VULNERABILITY / MAJOR] java:S5693 src/main/resources/application.properties:22: The content length limit of 27262976 bytes is greater than the defined limit of 8388608; make sure it is safe here.
- [VULNERABILITY / CRITICAL] java:S4502 src/main/java/cl/duoc/gamer/eventos/config/SeguridadConfig.java:32: Make sure disabling Spring Security's CSRF protection is safe here.

