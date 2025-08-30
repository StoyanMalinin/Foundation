#!/usr/bin/env python3

import json
import os
import subprocess
import sys
import requests

CONFIG_FILE_PATH = "../config/config.json"

def is_production_setup():
    return "--production" in sys.argv

def create_config_file():
    config = {}

    config["db"] = {}
    config["db"]["url"] = "jdbc:postgresql://127.0.0.1:5432/foundation"
    config["db"]["username"] = "postgres"
    config["db"]["password"] = "postgres"

    config["tomtom_api_key"] = "your_tomtom_api_key"

    config["cert"] = {}
    config["cert"]["password"] = "your_cert_password"

    config["jwt_secret"] = "your_jwt_secret"

    config["frontend"] = {}

    if is_production_setup():
        config["frontend"]["origin"] = "https://ffoundationn.fun"
    else:
        config["frontend"]["origin"] = "http://localhost:3000"

    json_config = json.dumps(config)
    os.makedirs(os.path.dirname(CONFIG_FILE_PATH), exist_ok=True)
    with open(CONFIG_FILE_PATH, "w+") as f:
        f.write(json_config)

def setup_frontend():
    silent_remove_file("../../frontend/foundation/.env")

    with open("../../frontend/foundation/.env", "w+") as f:
        if is_production_setup():
            f.write("NEXT_PUBLIC_BACKEND_API_BASE_URL=https://ffoundationn.fun:6969\n")
        else:
            f.write("NEXT_PUBLIC_BACKEND_API_BASE_URL=https://localhost:6969\n")

def setup_caddy():
    response = requests.get("http://localhost:2019/reverse_proxy/upstreams")
    if response.status_code != 200: # Caddy is not running
        sh("sudo apt-get update && sudo apt-get install -y caddy")
        sh("caddy start")

    print("checking the setup")

    SERVER_NAME = "foundation_server"
    response = requests.get(f"http://localhost:2019/config/apps/http/servers/{SERVER_NAME}")
    if response.ok:
        print("Caddy is already set up")
        return

    requests.put(f"http://localhost:2019/config/apps/http/servers/{SERVER_NAME}", json={
        "listen": [":443"],
        "routes": [
            {
                "match": [{ "host": ["ffoundationn.fun"] }],
                "handle": [
                {
                    "handler": "encode",
                    "encodings": {
                        "zstd": {},
                        "gzip": {}
                    }
                },
                {
                    "handler": "reverse_proxy",
                    "upstreams": [{ "dial": "127.0.0.1:3000" }]
                }
                ]
            }
        ],
        "automatic_https": { "disable_redirects": False }
    })

def silent_remove_file(file_path):
    try:
        os.remove(file_path)
    except FileNotFoundError:
        pass

def sh(cmd):
    subprocess.run(cmd, shell=True, check=True)

def main():
    if not os.path.exists(CONFIG_FILE_PATH):
        print("Creating config file...")
        create_config_file()
    else:
        print("Config file already exists.")

    print("Setting up frontend...")
    setup_frontend()

    if is_production_setup():
        print("Setting up Caddy...")
        setup_caddy()
    else:
        print("Skipping caddy setup")

    print("Setup complete.")

if __name__ == "__main__":
    main()