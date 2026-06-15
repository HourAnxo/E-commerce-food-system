# Frontend: build the Vite bundle, then serve it with Nginx and proxy /api
# to the backend container. Build context is food-frontend/.

# --- build stage ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# .env.production sets VITE_API_URL="" so the app makes relative /api calls
# that Nginx (below) proxies to the backend.
RUN npm run build

# --- runtime stage ---
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
