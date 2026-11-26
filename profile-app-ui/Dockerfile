# ---- build stage ----
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# creates the static site in dist/profile-app-ui/browser
RUN npm run build

# ---- run stage ----
FROM nginxinc/nginx-unprivileged:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/profile-app-ui/browser /usr/share/nginx/html
EXPOSE 8080
