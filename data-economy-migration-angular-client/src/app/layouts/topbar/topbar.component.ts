import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';

import { AuthenticationService } from '../../core/services/auth.service';
import { AppService } from 'src/app/core/services/app.service';


@Component({
  selector: 'app-topbar',
  templateUrl: './topbar.component.html',
  styleUrls: ['./topbar.component.scss']
})
export class TopbarComponent implements OnInit {

  notificationItems: Array<{}>;
  openMobileMenu: boolean;

  @Output() settingsButtonClicked = new EventEmitter();
  @Output() mobileMenuButtonClicked = new EventEmitter();

  basketCount: number;

  currentUser: any;

  constructor(
    private router: Router,
    private authService: AuthenticationService,
    private appService: AppService
  ) {
    this.appService.basketCountSubscription.subscribe((basketCount: any) => {
      this.basketCount = basketCount;
    });
  }

  ngOnInit() {
    this.openMobileMenu = false;
    this.currentUser = this.authService.currentUser();
    this.basketCount = this.currentUser["basketCount"] ? this.currentUser["basketCount"] : 0;
  }

  /**
   * Toggles the right sidebar
   */
  toggleRightSidebar() {
    this.settingsButtonClicked.emit();
  }

  /**
   * Toggle the menu bar when having mobile screen
   */
  toggleMobileMenu(event: any) {
    event.preventDefault();
    this.openMobileMenu = !this.openMobileMenu;
    this.mobileMenuButtonClicked.emit();
  }

  /**
   * Logout the user
   */
  logout() {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  openBasketDetails() {
    if (this.router.url !== '/app/request/preview') {
      if (this.basketCount != null && this.basketCount > 0) {
        this.router.navigate(['/app/basket']);
      }
    }
  }

}
