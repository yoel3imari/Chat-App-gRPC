import { createClient } from '@connectrpc/connect'
import { createGrpcWebTransport } from '@connectrpc/connect-web'
import { AdminService } from '@/grpc/admin/admin_pb'
import { useConnectInterceptor } from './useConnectInterceptor';

export function useAdminService() {
  const authInterceptor = useConnectInterceptor();
  const transport = createGrpcWebTransport({
    baseUrl: "http://localhost:8080",
    useBinaryFormat: true,
    interceptors: [authInterceptor],
  })

  const client = createClient(AdminService, transport)
  return client
}
