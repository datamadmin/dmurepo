import { Component, OnInit } from '@angular/core';
import { NotificationService } from 'src/app/core/services/notification.service';
import { AppService } from 'src/app/core/services/app.service';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
@Component({
    selector: 'app-change-password',
    templateUrl: './change-password.component.html',
    styleUrls: ['./change-password.component.scss']
})


export class ChangePasswordComponent implements OnInit {

    breadCrumbItems: Array<{}>;
    selectedUserId: any;

    changePasswordModel: any = {
        "newPassword": "",
        "confirmPassword": ""
    }

    constructor(private notificationService: NotificationService, 
        private appService: AppService,
        private router: Router,
        private route: ActivatedRoute) { 
    }

    ngOnInit() {
        this.breadCrumbItems = [{ label: 'Home', path: '/app/home' }, { label: 'Settings', active: true }, { label: 'Change Password', active: true }];
        this.route.queryParams
        .filter(params => params.selectedUserId)
        .subscribe(params => {
            this.selectedUserId = params["selectedUserId"] || "";
        });
    }

    clearChangePasswordModel() {
        this.changePasswordModel = {
            "newPassword": "",
            "confirmPassword": ""
        }
    }

    cancelClickFunction() {
        this.clearChangePasswordModel();
    }

    validateFormData() {
        if (this.changePasswordModel.newPassword.length < 1) {
            this.notificationService.showError("New Password is required");
            return false;
        }
        else if (this.changePasswordModel.confirmPassword.length < 1) {
            this.notificationService.showError("Confirm Password is required");
            return false;
        }
        else if (this.changePasswordModel.newPassword != this.changePasswordModel.confirmPassword) {
            this.notificationService.showError("New Password and confirm password should be same");
            return false;
        }
        return true;
    }
    updateClickFunction() {
        if (this.validateFormData()) {
            this.appService.resetPassword(this.changePasswordModel.newPassword,this.selectedUserId)
                .subscribe(
                    res => {
                        if (res) {
                            this.clearChangePasswordModel();
                            this.notificationService.showSuccess('Password changed successfully');
                            this.router.navigate(['/app/home']);
                        }
                        else {
                            this.notificationService.showError('Error while changing the password');
                        }
                    },
                    error => {
                        this.notificationService.showError(error || 'Error while changing the password');
                    });
        }
    }
}