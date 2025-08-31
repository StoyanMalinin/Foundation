FROM node:lts
WORKDIR /usr/local/app

COPY frontend/foundation ./frontend/foundation

WORKDIR /usr/local/app/frontend/foundation

RUN npm install
RUN npm run build

CMD ["npm", "start"]