import { Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { LoginComponent } from './login/login';
import { RegisterComponent } from './register/register';
import { UpdateComponent } from './update/update';
import { SearchTutorComponent } from './search_tutor/search_tutor';
import { SearchOnlyComponent } from './search_only/search_only';
import { ChatComponent } from './chat/chat';
import { ProfileComponent } from './profile/profile';
import { TutorProfileComponent } from './tutor_profile/tutor_profile';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'home', component: HomeComponent},
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'update', component: UpdateComponent },
  { path: 'search_tutor', component: SearchTutorComponent },
  { path: 'find', component: SearchOnlyComponent },
  { path: 'chat', component: ChatComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'tutor/:id', component: TutorProfileComponent },
  { path: 'chat/:id', component: ChatComponent }
];