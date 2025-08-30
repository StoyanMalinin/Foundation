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

    config["tomtom"] = {}
    config["tomtom"]["api_key"] = "your_tomtom_api_key"

    config["cert"] = {}
    config["cert"]["password"] = "your_cert_password"

    config["frontend"] = {}

    if is_production_setup():
        config["frontend"]["origin"] = "https://ffoundationn.fun"
    else:
        config["frontend"]["origin"] = "http://localhost:3000"

    json_config = json.dumps(config)
    with open(CONFIG_FILE_PATH, "w") as f:
        f.write(json_config)

def main():
    if not os.path.exists(CONFIG_FILE_PATH):
        print("Creating config file...")
        create_config_file()
    else:
        print("Config file already exists.")

if __name__ == "__main__":
    main()