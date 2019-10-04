import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { HomeComponent } from './home/home.component';
import { RequestComponent } from './request/request.component';
import { HistoryComponent } from './history/history.component';
import { ReconComponent } from './recon/recon.component';
import { HelpComponent } from './help/help.component';
import { UserComponent } from './user/user.component';
import { ConnectionComponent } from './connection/connection.component';
import { RequestPreviewComponent } from './request-preview/request-preview.component';
import { BasketComponent } from './basket/basket.component';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { RoleGuard } from '../core/guards/role.guard';
import { USER_ROLE } from '../core/constants/role.constants';

const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full', data: { 'allowedRoles': [USER_ROLE.ADMIN, USER_ROLE.NORMAL_USER] } },
  { path: 'home', component: HomeComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN, USER_ROLE.NORMAL_USER] } },
  { path: 'request', component: RequestComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN, USER_ROLE.NORMAL_USER] } },
  { path: 'request/preview', component: RequestPreviewComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN, USER_ROLE.NORMAL_USER] } },
  { path: 'basket', component: BasketComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN, USER_ROLE.NORMAL_USER] } },
  { path: 'history', component: HistoryComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN, USER_ROLE.NORMAL_USER] } },
  { path: 'recon', component: ReconComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN, USER_ROLE.NORMAL_USER] } },
  { path: 'help', component: HelpComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN, USER_ROLE.NORMAL_USER] } },
  { path: 'settings/users', component: UserComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN] }, canActivate: [RoleGuard] },
  { path: 'settings/connection', component: ConnectionComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN], canActivate: [RoleGuard] } },
  { path: 'settings/change-password', component: ChangePasswordComponent, data: { 'allowedRoles': [USER_ROLE.ADMIN] } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PagesRoutingModule { }
