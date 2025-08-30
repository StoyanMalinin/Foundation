#!/usr/bin/env python3

import json
import os
import sys

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

def main():
    if not os.path.exists(CONFIG_FILE_PATH):
        print("Creating config file...")
        create_config_file()
    else:
        print("Config file already exists.")

    print("Setting up frontend...")
    setup_frontend()

    print("Setup complete.")

def silent_remove_file(file_path):
    try:
        os.remove(file_path)
    except FileNotFoundError:
        pass

if __name__ == "__main__":
    main()