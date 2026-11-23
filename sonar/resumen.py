"""Resume el ultimo analisis de cada proyecto en SonarQube.

Escribe en la carpeta indicada:
  resumen.md     estado del Quality Gate, problemas por tipo y severidad, hotspots y el detalle de
                 las vulnerabilidades y de los problemas bloqueantes y criticos
  hallazgos.json la respuesta completa de la API, para revisarla despues

Uso: python resumen.py reportes/antes
"""
import base64
import json
import os
import sys
import urllib.parse
import urllib.request

URL = os.environ.get("SONAR_URL", "http://localhost:9100")
PROYECTOS = ["eventos-gamer-web", "eventos-gamer-ms-usuarios", "eventos-gamer-ms-eventos"]
TIPOS = ["VULNERABILITY", "BUG", "CODE_SMELL"]
SEVERIDADES = ["BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO"]


def leer_env():
    valores = {}
    with open(os.path.join(os.path.dirname(__file__), ".env"), encoding="utf-8") as archivo:
        for linea in archivo:
            if "=" in linea and not linea.startswith("#"):
                clave, valor = linea.strip().split("=", 1)
                valores[clave] = valor
    return valores


def consultar(ruta, credencial, **parametros):
    direccion = f"{URL}/api/{ruta}?{urllib.parse.urlencode(parametros)}"
    peticion = urllib.request.Request(direccion, headers={"Authorization": f"Basic {credencial}"})
    with urllib.request.urlopen(peticion, timeout=30) as respuesta:
        return json.load(respuesta)


def todos(ruta, credencial, campo, **parametros):
    elementos, pagina = [], 1
    while True:
        datos = consultar(ruta, credencial, p=pagina, ps=500, **parametros)
        elementos += datos[campo]
        if pagina * 500 >= datos["paging"]["total"]:
            return elementos
        pagina += 1


def main():
    carpeta = sys.argv[1]
    clave = leer_env()["SONAR_ADMIN_CLAVE"]
    credencial = base64.b64encode(f"admin:{clave}".encode()).decode()
    lineas, completo = [], {}
    for proyecto in PROYECTOS:
        gate = consultar("qualitygates/project_status", credencial, projectKey=proyecto)["projectStatus"]
        problemas = todos("issues/search", credencial, "issues", components=proyecto,
                          issueStatuses="OPEN,CONFIRMED")
        hotspots = todos("hotspots/search", credencial, "hotspots", project=proyecto)
        medidas = consultar("measures/component", credencial, component=proyecto,
                            metricKeys="ncloc,coverage,security_rating,reliability_rating,sqale_rating,"
                                       "duplicated_lines_density")["component"]["measures"]
        completo[proyecto] = {"gate": gate, "issues": problemas, "hotspots": hotspots, "medidas": medidas}

        lineas.append(f"## {proyecto}\n")
        lineas.append(f"Quality Gate: **{gate['status']}**\n")
        for condicion in gate["conditions"]:
            if condicion["status"] == "ERROR":
                lineas.append(f"- falla `{condicion['metricKey']}`: {condicion.get('actualValue')} "
                              f"({condicion['comparator']} {condicion['errorThreshold']})")
        valores = {medida["metric"]: medida.get("value", "-") for medida in medidas}
        lineas.append("\nMedidas: " + ", ".join(f"{clave} {valor}" for clave, valor in sorted(valores.items())) + "\n")
        lineas.append("| Tipo | " + " | ".join(SEVERIDADES) + " | Total |")
        lineas.append("|---|" + "---|" * (len(SEVERIDADES) + 1))
        for tipo in TIPOS:
            del_tipo = [p for p in problemas if p["type"] == tipo]
            cuenta = [str(sum(1 for p in del_tipo if p["severity"] == s)) for s in SEVERIDADES]
            lineas.append(f"| {tipo} | " + " | ".join(cuenta) + f" | {len(del_tipo)} |")
        revisados = sum(1 for h in hotspots if h["status"] == "REVIEWED")
        prioridades = {}
        for hotspot in hotspots:
            prioridades[hotspot["vulnerabilityProbability"]] = prioridades.get(hotspot["vulnerabilityProbability"], 0) + 1
        lineas.append(f"\nSecurity Hotspots: {len(hotspots)} (revisados {revisados}), por prioridad {prioridades}\n")

        importantes = [p for p in problemas if p["type"] == "VULNERABILITY" or p["severity"] in ("BLOCKER", "CRITICAL")]
        if importantes:
            lineas.append("Vulnerabilidades y problemas bloqueantes o criticos:\n")
            for problema in importantes:
                archivo = problema["component"].split(":", 1)[-1]
                lineas.append(f"- [{problema['type']} / {problema['severity']}] {problema['rule']} "
                              f"{archivo}:{problema.get('line', '-')}: {problema['message']}")
        if hotspots:
            lineas.append("\nHotspots:\n")
            for hotspot in hotspots:
                archivo = hotspot["component"].split(":", 1)[-1]
                lineas.append(f"- [{hotspot['vulnerabilityProbability']} / {hotspot['status']}"
                              f"{'/' + hotspot['resolution'] if hotspot.get('resolution') else ''}] "
                              f"{hotspot['ruleKey']} {archivo}:{hotspot.get('line', '-')}: {hotspot['message']}")
        lineas.append("")

    with open(os.path.join(carpeta, "resumen.md"), "w", encoding="utf-8") as archivo:
        archivo.write("# Resumen del analisis de SonarQube\n\n" + "\n".join(lineas) + "\n")
    with open(os.path.join(carpeta, "hallazgos.json"), "w", encoding="utf-8") as archivo:
        json.dump(completo, archivo, ensure_ascii=False, indent=1)
    print(f"Resumen en {carpeta}/resumen.md")


if __name__ == "__main__":
    main()
