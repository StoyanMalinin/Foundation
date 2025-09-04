#!/usr/bin/env bash

docker build -f backend.Dockerfile -t backend:latest ../../
docker build -f foundation-postgis.Dockerfile -t foundation-postgis:latest ../../