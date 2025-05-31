FROM node:22-alpine

# Install pnpm globally
RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /app

# Copy and install dependencies using pnpm
COPY pnpm-lock.yaml ./
COPY package.json ./
RUN pnpm install

# Copy the rest of the app
COPY . .

EXPOSE 5173

CMD ["pnpm", "dev"]
