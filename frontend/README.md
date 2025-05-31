# .

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
pnpm install
```

### Compile and Hot-Reload for Development

```sh
pnpm dev
```

### Type-Check, Compile and Minify for Production

```sh
pnpm build
```

### Lint with [ESLint](https://eslint.org/)

```sh
pnpm lint
```

## gRPC-Web Integration

This project uses [Envoy](https://www.envoyproxy.io/) as a proxy to enable gRPC-Web support for browser clients. All gRPC-Web requests from the frontend should be sent to Envoy, which forwards them to the appropriate backend service.

- **gRPC-Web endpoint:** `http://localhost:8080`
- The Vite dev server is configured to proxy `/chat.`, `/user.`, `/admin.`, `/auth.`, and `/notification.` requests to Envoy for local development.
- Make sure Envoy is running (via Docker Compose or manually) when developing locally.

Example gRPC-Web client configuration:
```js
const client = new ChatServiceClient('http://localhost:8080');
```

See the `vite.config.ts` for proxy details.
