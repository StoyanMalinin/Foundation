# Foundation
This is a full-fledged platform **(still under construction)** dedicated to helping people organize themselves in case someone is missing or lost. The idea is simple - everybody reports the areas he has checked with the help of a mobile app. All this data gets aggregated and can be seen publicly on the website, but presenting it in a map where the areas that have recently been checked are marked in red. This aims to help organizers and volunteers, so that people can contribute to searches more seamlessly.

# [Developers only] Architecture & deployment

## Architecture
The platform consists of four components - database layer, backend service, web frontend and mobile application.

### Database layer
The database layer consists of a single Postgres instance (with a Postgis extension) that stores all of the user, location and search data. There is also a Redis instance that is used for caching the map tiles that are fetched from the Tomtom public API.

### Backend
The backend is a single stateless service that is written in Java. It acts as a RESTful API https server that the frontends (web & mobile) use. The server is backed by the [Eclipse Jetty](https://jetty.org/index.html) library. Authentication and authorization is done entirely here with the help of some libraries.

### Frontend
The web frontend is implemented as a [NextJS](https://nextjs.org/) server. It uses a mix of server- and client- side rendering in order to enhance security, simplicity and performance (more and more things are gradually being moved to the server side).

### Mobile app
The mobile app will be a cross platform application developed with the help of the [Expo](https://expo.dev/) platform. It is still in the very early stages of development. It's purpose will be a very thin client that periodically sends (or stores for later) the locations where people have been and reports them back to the backend server.

## Deployment
The platform is intended to be hosted on a single host with the help of [Docker](https://www.docker.com/) containers. The possibility to expand to more hosts is there, but it needs more work.

### Running locally
It is possible to run the full stack on your local machines. Here are the main steps:

#### Clone the repo
You can clone the repository and use it freely by doing:
```
git clone https://github.com/StoyanMalinin/Foundation
```
#### Setting up certificates
The backend needs to have a certificate stored in a `.jks` file in order to be reachable by the frontend. One way to obtain it is do the following steps:

- Install mkcert and trust it locally - [mkcert repository](https://github.com/FiloSottile/mkcert)
```
mkcert -install
```

- Create a self-signed certificate for *localhost*
```
mkcert localhost 127.0.0.1 ::1
```

- Convert to a `.jks` file

*TODO: Add more instructions*

- Put the `.jks` file into the `backend/secrets/` folder 

#### Setup configuration files
The configuration file template can be obtained by running the following commands from the repo root:
```
cd deploy/scripts/
python3 setup.py
```
In case you want to run the backend service separately (not as a docker container, you have to use the `--backend-outside-docker`).

Then go to `deploy/config/config.json` and change the fields, so that it match your current setup. You will usually have to change the Tomtom API key, your kestore (`.jks` file) password and optionally the `jwt_secret`.

#### Build docker images
Bare in mind that this can be a bit slow, especially the first time (a couple minutes per image).

##### Database
The PostgreSQL DB uses a custom image. In order to obtain it, you have to `cd deploy/docker/` from repo root and then run 
```
docker build -f foundation-postgis.Dockerfile -t foundation-postgis:latest ../../
```
##### Backend
In case you want to run the backend as a docker image (which is recommended) you have to `cd deploy/docker/` from repo root and then run 
```
docker build -f backend.Dockerfile -t backend:latest ../../
```

#### Run the app

##### Backend
You can start the full backend part by running (inside `deploy/docker/`)
```
docker-compose -f headless.docker-compose.yaml up
```

<br>

You can also run the backend as a standalone process, by running just the DB with
```
docker-compose -f db.docker-compose.yaml up
```
and then goint to `backend/FoundationBackend` and running 
```
mvn install
mvn package
mvn exec:java
```
**Important:** Don't forget to ensure that the `setup.py` script was run with the `--backend-outside-docker` flag.

##### Frontend
Go to `frontend/foundation` and then run
```
npm run dev
```

### Running in production
The setup is pretty similar, but it is way more tangled to the specific domain that you have avaialble. 

The main difference is that you have to run the `setup.py` script with the `--production` flag and then change the domain both in the `deploy/config/config.json` file and in the `frontend/foundation/.env` file.

In that case you might also want to run the frontend with the following commands:
```
npm run build
```
to build and then
```
npm run start
```

# Future work
There is a lot of work that can be done on the project, but here are the main points:

- Complete the mobile app
- Polish the web frontend (it currently works, but it is very bare-bones)
- Stress test for scalability with many users
- Simplify the deployment/development process
- Polish the RESTful API interface to be more idiomatic
- Do a lot of refactorings everywhere :)

