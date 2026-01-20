FROM node:20-bookworm AS builder

WORKDIR /workspace/frontend

RUN corepack enable && corepack prepare pnpm@9.15.4 --activate

COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY frontend ./

# Vite 빌드 타임 환경변수(필요 시 GitHub Actions에서 주입)
ARG VITE_API_BASE_URL
ARG VITE_API_TIMEOUT
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_API_TIMEOUT=${VITE_API_TIMEOUT}

RUN pnpm build


FROM nginx:1.27-alpine

COPY deploy/nginx/nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /workspace/frontend/dist /usr/share/nginx/html

EXPOSE 80