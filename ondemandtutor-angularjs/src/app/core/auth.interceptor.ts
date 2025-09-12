// core/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Những endpoint public: không gắn token
  const isPublic = /\/api\/users\/(login|register|refresh)\b/.test(req.url)
                || /\/api\/students\/search-tutor\b/.test(req.url);

  if (isPublic) return next(req);

  const token = localStorage.getItem('jwt') ?? localStorage.getItem('access_token');
  if (token) req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });

  return next(req);
};
