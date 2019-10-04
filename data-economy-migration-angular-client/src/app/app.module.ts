import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { ErrorInterceptor } from './core/helpers/error.interceptor';
import { JwtInterceptor } from './core/helpers/jwt.interceptor';
import { LayoutsModule } from './layouts/layouts.module';
import { UIModule } from './shared/ui/ui.module';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';

import {
  NgbDropdownModule,
  NgbAlertModule,
  NgbToastModule,
  NgbPopoverModule
} from '@ng-bootstrap/ng-bootstrap';

import { BusyModule } from 'angular2-busy';

import { AppService } from './core/services/app.service';
import { LoaderInterceptor } from './core/helpers/loader.interceptor';
import { LoaderService } from './core/services/loader.service';
import { NotificationService } from './core/services/notification.service';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    LayoutsModule,
    UIModule,
    AppRoutingModule,
    NgbDropdownModule,
    NgbAlertModule,
    NgbToastModule,
    NgbPopoverModule,
    BusyModule,
    ToastModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LoaderInterceptor, multi: true }, ,
    LoaderService,
    NotificationService,
    MessageService,
    AppService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
