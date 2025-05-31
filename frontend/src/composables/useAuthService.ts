import { createClient } from '@connectrpc/connect'
import { createGrpcWebTransport } from '@connectrpc/connect-web'
import { AuthService } from '@/grpc/auth/auth_pb'
import { useConnectInterceptor } from './useConnectInterceptor';

export function useAuthService() {
  const authInterceptor = useConnectInterceptor();
  const transport = createGrpcWebTransport({
    baseUrl: "http://localhost:8080",
    useBinaryFormat: true,
    interceptors: [authInterceptor]
  })

  const client = createClient(AuthService, transport)
  return client
}
