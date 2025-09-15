import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Bỏ qua các endpoint public
  const isPublic =
    /\/api\/users\/(login|register|refresh)\b/.test(req.url) ||
    /\/api\/students\/search-tutor\b/.test(req.url);

  if (isPublic) return next(req);

  // Đọc token – đảm bảo khớp key bạn lưu khi login
  const token =
    localStorage.getItem('access_token') ??
    localStorage.getItem('jwt') ??
    localStorage.getItem('token');

  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
