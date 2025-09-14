import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  skills: string;
  qualifications: string;
  teachingGrades: string;
  rating?: number;
  price?: number;
  availableTime?: string;
  description?: string;
  promoFile?: string;
  verified: boolean;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private api = environment.apiUrls.userService;

  constructor(private http: HttpClient) {}

  getUserById(id: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.api}/getUser/${id}`);
  }
}