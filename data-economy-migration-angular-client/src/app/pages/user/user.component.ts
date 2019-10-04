import { Component, OnInit } from '@angular/core';
import { ConfirmationService } from 'primeng/api';
import { AppService } from 'src/app/core/services/app.service';
import { NotificationService } from 'src/app/core/services/notification.service';

enum USER_ACTION_TYPE {
    CREATE,
    UPDATE
}

@Component({
    selector: 'app-user',
    templateUrl: './user.component.html',
    styleUrls: ['./user.component.scss']
})


export class UserComponent implements OnInit {

    breadCrumbItems: Array<{}>;

    userCols: any;
    userList: any;

    isAdd: boolean = false;
    isUpdate: boolean = false;

    userModel: any = {
        id: null,
        userName: "",
        emailid: "",
        userRole: "Admin"
    };

    constructor(
        private confirmationService: ConfirmationService,
        private appService: AppService,
        private notificationService: NotificationService
    ) { }

    ngOnInit() {

        this.breadCrumbItems = [{ label: 'Home', path: '/app/home' }, { label: 'Settings', active: true }, { label: 'Users', active: true }];

        this.userCols = [
            { field: 'userName', header: 'User Name' },
            { field: 'userRole', header: 'User Role' },
            { field: 'emailid', header: 'User Email' }
        ];
        this.fetchUserData();
    }

    fetchUserData() {
        this.appService.getAllUsers().subscribe(
            data => {
                this.userList = data;
            },
            error => {
                console.log(error);
            });
    }

    resetUserModel() {
        this.userModel = {
            id: null,
            userName: "",
            emailid: "",
            userRole: "Admin"
        };
    }

    showAddUser() {
        this.resetUserModel();
        this.isAdd = true;
    };

    showUpdateUser(selectedUser: any) {
        this.userModel = Object.assign({}, selectedUser);
        this.isUpdate = true;
    };

    showDeleteUser(selectedUser: any) {
        this.confirmationService.confirm({
            message: 'Are you sure want to delete the user?',
            accept: () => {
                this.deleteUserFunction(selectedUser);
            }
        });
    };

    cancelClickFunction() {
        this.isAdd = false;
        this.isUpdate = false;
        this.resetUserModel();
    }

    deleteUserFunction(selectedUser: any) {
        this.appService.deleteUser(selectedUser.id).subscribe(
            (res) => {
                this.notificationService.showSuccess("User deleted successfully");
                this.fetchUserData();
            },
            (error) => {
                console.log(error);
                this.notificationService.showError(error ||  "Error while deleting user");
            });
    }

    validateUserModel(action: USER_ACTION_TYPE) {
        if (action == USER_ACTION_TYPE.CREATE) {
            if (this.userModel.userName.trim().length < 1) {
                this.notificationService.showError('Please enter username');
                return false;
            }
            else if (this.userModel.emailid.trim().length < 1) {
                this.notificationService.showError('Please enter email');
                return false;
            }
            return true;
        }
        else if (action == USER_ACTION_TYPE.UPDATE) {
            return true;
        }
    };

    addUserClickFunction() {
        if (this.validateUserModel(USER_ACTION_TYPE.CREATE)) {
            this.isAdd = false;
            this.appService.addUser(this.userModel).subscribe(
                (res) => {
                    this.notificationService.showSuccess("User created successfully");
                    this.fetchUserData();
                },
                (error) => {
                    this.isAdd = true;
                    this.notificationService.showError(error ||  "Error while creating user");
                });
        }

    }

    updateUserClickFunction() {
        if (this.validateUserModel(USER_ACTION_TYPE.UPDATE)) {
            this.isUpdate = false;
            this.appService.updateUser(this.userModel).subscribe(
                (res) => {
                    this.notificationService.showSuccess("User information updated successfully");
                    this.fetchUserData();
                },
                (error) => {
                    this.isUpdate = true;
                    this.notificationService.showError(error ||  "Error while updating user information");
                });
        }
    }
}
