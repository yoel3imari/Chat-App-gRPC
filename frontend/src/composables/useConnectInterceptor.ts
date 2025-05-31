import TokenService from "@/services/TokenService";
import type { Interceptor } from "@connectrpc/connect";

export function useConnectInterceptor(): Interceptor {
  return (next) => async (req) => {
    const token = TokenService.getAccessToken()
    if (token) {
      req.header.set("Authorization", `Bearer ${token}`);
      // console.log(`Bearer ${token}`);

    }
    return next(req);
  };
}
