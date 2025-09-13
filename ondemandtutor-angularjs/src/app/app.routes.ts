import { Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { LoginComponent } from './login/login';
import { RegisterComponent } from './register/register';
import { UpdateComponent } from './update/update';
import { SearchTutorComponent } from './search_tutor/search_tutor';
import { ProfileComponent } from './profile/profile';
import { TutorProfileComponent } from './tutor_profile/tutor_profile';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'home', component: HomeComponent},
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'update', component: UpdateComponent },
  { path: 'search_tutor', component: SearchTutorComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'tutor/:id', component: TutorProfileComponent }
  
];
