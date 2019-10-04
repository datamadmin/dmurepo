import { Component, OnInit } from '@angular/core';
import { LoaderService } from './core/services/loader.service';
import { AppService } from './core/services/app.service';

@Component({
  selector: 'app',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  isSpinnerVisible: boolean = false;
  loadingMessage: String = "";

  constructor(
    private loaderService: LoaderService,
    private appService: AppService
  ) { }

  ngOnInit() {
    this.loaderService.isLoading.subscribe((isSpinnerVisible) => {
      this.isSpinnerVisible = isSpinnerVisible;
    });
  }

}
