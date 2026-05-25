# ============================================
# Stage 1: Dependencies
# ============================================
FROM node:20-alpine AS deps

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci || npm install

# ============================================
# Stage 2: Development
# ============================================
FROM node:20-alpine AS development

WORKDIR /app

COPY --from=deps /app/node_modules ./node_modules
COPY . .

EXPOSE 3000

CMD ["npx", "next", "dev"]

# ============================================
# Stage 3: Builder
# ============================================
FROM node:20-alpine AS builder

WORKDIR /app

COPY --from=deps /app/node_modules ./node_modules
COPY . .

ENV NEXT_TELEMETRY_DISABLED=1

# Build args for NEXT_PUBLIC env vars (baked at build time)
ARG NEXT_PUBLIC_API_URL=http://localhost:8080/api
ARG NEXT_PUBLIC_APP_URL=https://lichso.vn
ARG NEXT_PUBLIC_APP_NAME="Lịch Số"
ARG NEXT_PUBLIC_API_TIMEOUT=10000
ARG NEXT_PUBLIC_ACCESS_TOKEN_KEY=lichso_access_token
ARG NEXT_PUBLIC_REFRESH_TOKEN_KEY=lichso_refresh_token
ARG NEXT_PUBLIC_ENABLE_SOCIAL_LOGIN=false
ARG NEXT_PUBLIC_GOOGLE_CLIENT_ID=""
ARG NEXT_PUBLIC_FACEBOOK_APP_ID=""
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_APP_URL=$NEXT_PUBLIC_APP_URL
ENV NEXT_PUBLIC_APP_NAME=$NEXT_PUBLIC_APP_NAME
ENV NEXT_PUBLIC_API_TIMEOUT=$NEXT_PUBLIC_API_TIMEOUT
ENV NEXT_PUBLIC_ACCESS_TOKEN_KEY=$NEXT_PUBLIC_ACCESS_TOKEN_KEY
ENV NEXT_PUBLIC_REFRESH_TOKEN_KEY=$NEXT_PUBLIC_REFRESH_TOKEN_KEY
ENV NEXT_PUBLIC_ENABLE_SOCIAL_LOGIN=$NEXT_PUBLIC_ENABLE_SOCIAL_LOGIN
ENV NEXT_PUBLIC_GOOGLE_CLIENT_ID=$NEXT_PUBLIC_GOOGLE_CLIENT_ID
ENV NEXT_PUBLIC_FACEBOOK_APP_ID=$NEXT_PUBLIC_FACEBOOK_APP_ID

RUN npx next build

# ============================================
# Stage 4: Production
# ============================================
FROM node:20-alpine AS production

WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

CMD ["node", "server.js"]
