import { Component, OnInit } from '@angular/core';
import { NotificationService } from 'src/app/core/services/notification.service';
import { AppService } from 'src/app/core/services/app.service';

@Component({
    selector: 'app-change-password',
    templateUrl: './change-password.component.html',
    styleUrls: ['./change-password.component.scss']
})


export class ChangePasswordComponent implements OnInit {

    breadCrumbItems: Array<{}>;

    changePasswordModel: any = {
        "newPassword": "",
        "confirmPassword": ""
    }

    constructor(private notificationService: NotificationService, private appService: AppService) { }

    ngOnInit() {
        this.breadCrumbItems = [{ label: 'Home', path: '/app/home' }, { label: 'Settings', active: true }, { label: 'Change Password', active: true }];

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
            this.appService.resetPassword(this.changePasswordModel.newPassword)
                .subscribe(
                    res => {
                        if (res) {
                            this.clearChangePasswordModel();
                            this.notificationService.showSuccess('Password changed successfully');
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