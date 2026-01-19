FROM node:20-bookworm AS builder

WORKDIR /workspace

# pnpm 설치
RUN corepack enable && corepack prepare pnpm@9.15.4 --activate

# workspace 의존성 메타데이터 복사
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml /workspace/
COPY frontend/package.json /workspace/frontend/package.json

RUN pnpm install --frozen-lockfile

COPY frontend /workspace/frontend

# Vite 빌드 타임 환경변수(필요 시 GitHub Actions에서 주입)
ARG VITE_API_BASE_URL
ARG VITE_API_TIMEOUT
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_API_TIMEOUT=${VITE_API_TIMEOUT}

RUN pnpm -C frontend build


FROM nginx:1.27-alpine

COPY deploy/nginx/nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /workspace/frontend/dist /usr/share/nginx/html

EXPOSE 80

