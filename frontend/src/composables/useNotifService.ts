import { createClient } from '@connectrpc/connect'
import { createGrpcWebTransport } from '@connectrpc/connect-web'
import { NotificationService } from '@/grpc/notification/notification_pb'
import { useConnectInterceptor } from './useConnectInterceptor';

export function useNotificationService() {
  const authInterceptor = useConnectInterceptor();
  const transport = createGrpcWebTransport({
    baseUrl: "http://localhost:8080",
    useBinaryFormat: true,
    interceptors: [authInterceptor],
  })

  const client = createClient(NotificationService, transport)
  return client
}
