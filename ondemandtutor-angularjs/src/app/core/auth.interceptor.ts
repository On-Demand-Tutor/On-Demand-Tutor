import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Loại trừ các endpoint không cần token
  const excludeUrls = ['/login', '/register', '/refresh','/update'];
  const shouldSkip = excludeUrls.some(url => req.url.includes(url));

  if (shouldSkip) {
    return next(req);
  }

  const t = localStorage.getItem('jwt');
  if (t) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${t}` }
    });
  }

  return next(req);
};
