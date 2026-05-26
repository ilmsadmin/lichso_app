# ============================================
# Stage 1: Development
# ============================================
FROM golang:1.25-alpine AS development

WORKDIR /app

# Install air for hot reload
RUN go install github.com/air-verse/air@latest

# Copy go mod files
COPY go.mod go.sum ./
RUN go mod download

# Copy source code
COPY . .

EXPOSE 8080

CMD ["air", "-c", ".air.toml"]

# ============================================
# Stage 2: Builder
# ============================================
FROM golang:1.25-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-w -s" -o /server ./cmd/server/main.go
RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-w -s" -o /migrate ./cmd/migrate/main.go

# ============================================
# Stage 3: Production
# ============================================
FROM alpine:3.19 AS production

RUN apk --no-cache add ca-certificates tzdata

WORKDIR /app

COPY --from=builder /server .
COPY --from=builder /migrate .
COPY --from=builder /app/migrations ./migrations

EXPOSE 8080

CMD ["./server"]
