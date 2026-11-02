#!/usr/bin/env python3
"""Prueba real HTTP con Jetty y Derby de red, usando una base temporal aislada."""
import json
import os
from pathlib import Path
import socket
import subprocess
import tempfile
import time
import urllib.request
import urllib.error

ROOT = Path(__file__).resolve().parents[1]
JAVA = os.environ.get("LAB_JAVA", "java")
CP = str(ROOT / "target/classes") + os.pathsep + str(ROOT / "target/dependency/*")

def free_port():
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        return sock.getsockname()[1]

def request(base, path, data=None):
    req = urllib.request.Request(base + path, data=None if data is None else json.dumps(data).encode(), headers={"Content-Type": "application/json"})
    try:
        response = urllib.request.urlopen(req, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        body = response.read().decode()
        try:
            body = json.loads(body)
        except ValueError:
            pass
        return response.status, body

def main():
    db_port, web_port = free_port(), free_port()
    while web_port == db_port:
        web_port = free_port()
    processes = []
    with tempfile.TemporaryDirectory(prefix="lab6-http-") as temp:
        with open(Path(temp) / "servers.log", "w+") as log:
            try:
                derby = subprocess.Popen([JAVA, "-Dderby.system.home=" + temp, "-cp", CP, "org.apache.derby.drda.NetworkServerControl", "start", "-h", "127.0.0.1", "-p", str(db_port), "-noSecurityManager"], cwd=ROOT, stdout=log, stderr=log)
                processes.append(derby)
                for _ in range(100):
                    if derby.poll() is not None:
                        raise RuntimeError("Derby terminó antes de iniciar")
                    try:
                        with socket.create_connection(("127.0.0.1", db_port), timeout=.2):
                            break
                    except OSError:
                        time.sleep(.2)
                else:
                    raise RuntimeError("Derby no inició")
                env = dict(os.environ, PORT=str(web_port))
                app = subprocess.Popen([JAVA, "-Djavax.persistence.jdbc.url=jdbc:derby://127.0.0.1:" + str(db_port) + "/HttpTestDB;create=true", "-cp", CP, "com.example.Main"], cwd=ROOT, env=env, stdout=log, stderr=log)
                processes.append(app)
                base = "http://127.0.0.1:" + str(web_port)
                for _ in range(150):
                    if app.poll() is not None:
                        raise RuntimeError("Jetty terminó antes de iniciar")
                    try:
                        status, page = request(base, "/")
                        if status == 200:
                            break
                    except (OSError, urllib.error.URLError):
                        pass
                    time.sleep(.2)
                else:
                    raise RuntimeError("Jetty no inició")
                assert "Laboratorio 6 JPA-1 - 3 corte" in page
                base += "/rest/competitors"
                assert request(base, "/get") == (200, [])
                registration = dict(name="Laura", surname="Gomez", address="laura.gomez@gmail.com", password="clave123", age=20, telephone="7659675", cellphone="3002345436", city="Bogota", country="Colombia")
                status, added = request(base, "/add", registration)
                assert status == 200 and added["competitor_id"] > 0, (status, added)
                credentials = dict(address=registration["address"], password="clave123")
                status, competitor = request(base, "/login", credentials)
                assert status == 200 and competitor["id"] == added["competitor_id"], (status, competitor)
                assert "password" not in competitor
                assert request(base, "/get")[1][0]["id"] == added["competitor_id"]
                credentials["password"] = "incorrecta"
                assert request(base, "/login", credentials)[0] == 401
                credentials["address"] = "inexistente@example.com"
                assert request(base, "/login", credentials)[0] == 401
                assert request(base, "/login", {})[0] == 401
                assert request(base, "/add", registration)[0] == 409
                assert request(base, "/add", {})[0] == 400
                print("OK: página HTTP 200; registro HTTP 200 con ID; consulta persistente HTTP 200; login correcto HTTP 200 sin contraseña; contraseña incorrecta, correo inexistente y datos vacíos HTTP 401; correo duplicado HTTP 409; registro vacío HTTP 400.")
            except Exception:
                log.flush()
                log.seek(0)
                print(log.read())
                raise
            finally:
                for process in reversed(processes):
                    process.terminate()
                    try:
                        process.wait(timeout=10)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait()

if __name__ == "__main__":
    main()
