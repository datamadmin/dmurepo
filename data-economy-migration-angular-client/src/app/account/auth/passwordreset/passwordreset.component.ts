import { Component, OnInit, AfterViewInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from 'src/app/core/services/notification.service';
import { AppService } from 'src/app/core/services/app.service';

@Component({
  selector: 'app-passwordreset',
  templateUrl: './passwordreset.component.html',
  styleUrls: ['./passwordreset.component.scss']
})
export class PasswordresetComponent implements OnInit, AfterViewInit {

  resetForm: FormGroup;
  submitted = false;
  error = '';
  success = '';
  loading = false;

  constructor(private formBuilder: FormBuilder, private route: ActivatedRoute, private router: Router,
    private appService: AppService, private notificationService: NotificationService) { }

  ngOnInit() {

    this.resetForm = this.formBuilder.group({
      userName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngAfterViewInit() {
    document.body.classList.add('authentication-bg');
    document.body.classList.add('authentication-bg-pattern');
  }

  // convenience getter for easy access to form fields
  get f() { return this.resetForm.controls; }

  /**
   * On submit form
   */
  onSubmit() {
    this.submitted = true;

    // stop here if form is invalid
    if (this.resetForm.invalid) {
      return;
    }

    this.loading = true;
    this.appService.forgotPassword(this.f.userName.value, this.f.email.value)
      .subscribe(
        res => {
          if (res) {
            this.submitted = false;
            this.resetForm.reset();
            this.notificationService.showSuccess('Password sent to the user email successfully');
            this.router.navigate(['/app/login'])
          }
          else {
            this.notificationService.showError('Error while processing the request');
          }
        },
        error => {
          this.notificationService.showError(error || 'Error while processing the request');
          this.loading = false;
        });
  }
}