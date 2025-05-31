import { createClient } from '@connectrpc/connect'
import { createGrpcWebTransport } from '@connectrpc/connect-web'
import { UserService } from '@/grpc/user/user_pb';

export function useUserService() {
  // const authInterceptor = useConnectInterceptor();
  const transport = createGrpcWebTransport({
    baseUrl: "http://localhost:8080",
    useBinaryFormat: true,
    //interceptors: [authInterceptor]
  })

  const client = createClient(UserService, transport)
  return client
}
