import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { CookieService } from '../services/cookie.service';

import { environment } from 'src/environments/environment';
import { AuthenticationService } from './auth.service';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AppService {
    constructor(
        private http: HttpClient,
        private cookieService: CookieService,
        private authenticationService: AuthenticationService
    ) { }

    getCurrentUserId(): any {
        let currentUserId = this.authenticationService.currentUser()["id"];
        return currentUserId;
    }

    getCurrentUserName(): any {
        let currentUserName = this.authenticationService.currentUser()["userName"];
        return currentUserName;
    }

    requestModel: any;
    requestPreviewList: any = [];
    isRequestBackClicked: boolean = false;

    public basketCountSubscription = new BehaviorSubject(0);

    addUser(userModel: any) {
        return this.http.post(`${environment.apiUrl}/users/save`, userModel);
    }

    updateUser(userModel: any) {
        return this.http.post(`${environment.apiUrl}/users/edit`, userModel);
    }

    deleteUser(userId: any) {
        const params = {
            "userId": userId
        }
        return this.http.get(`${environment.apiUrl}/users/delete`, { params });
    }

    resetPassword(password: any, selectedUserId: any) {
        if (selectedUserId == null || selectedUserId.length == 0) {
            selectedUserId = this.getCurrentUserId();
        }
        const params = {
            "id": selectedUserId,
            "password": password
        }
        return this.http.get(`${environment.apiUrl}/users/resetPassword`, { params });
    }

    forgotPassword(userName: any, emailid: any) {
        const params = {
            "userName": userName,
            "emailid": emailid
        }
        return this.http.get(`${environment.apiUrl}/users/forgotPassword`, { params });
    }


    getAllUsers(): any {
        return this.http.get(`${environment.apiUrl}/users/all`);
    }

    getHistoryMainList() {
        if (this.authenticationService.currentUser()["userRole"] === 'Admin' ? true : false) {
            return this.http.get(`${environment.apiUrl}/history/main/all`);
        }
        else {
            const params = {
                "userId": this.authenticationService.currentUser()["userName"]
            }
            return this.http.get(`${environment.apiUrl}/history/main/byUserId`, { params });
        }
    }

    getHistoryDetailsById(requestNo: any) {
        const params = {
            "requestNumber": requestNo
        }
        return this.http.get(`${environment.apiUrl}/history/getHistoryDetail`, { params });
    }

    getReconMainList() {


        if (this.authenticationService.currentUser()["userRole"] === 'Admin' ? true : false) {
            return this.http.get(`${environment.apiUrl}/recon/all`);
        }
        else {
            const params = {
                "userId": this.authenticationService.currentUser()["userName"]
            }
            return this.http.get(`${environment.apiUrl}/recon/byUserId`, { params });
        }
    }

    getReconDetailsById(requestNo: any) {
        return this.http.get(`${environment.apiUrl}/recon/detail/details/${requestNo}`);
    }

    testConnection(connectionModel: any) {
        return this.http.post(`${environment.apiUrl}/connection/validate`, connectionModel);
    }

    saveConnection(connectionModel: any) {
        return this.http.post(`${environment.apiUrl}/connection/save`, connectionModel);
    }

    getConnectionDetails() {
        return this.http.get(`${environment.apiUrl}/connection/get`);
    }

    getDatabaseNameList() {
        return this.http.get(`${environment.apiUrl}/request/all`);
    }

    getRequestPreviewData(requestModel: any) {
        return this.http.get(`${environment.apiUrl}/request/all/${requestModel.schemaName}`);
    }

    saveRequestPreviewData(basketList: any) {
        let headers = new HttpHeaders({
            'userId': this.getCurrentUserName()
        });
        return this.http.post(`${environment.apiUrl}/basket/save`, basketList, { headers: headers });
    }

    getAllBasketItems(): any {
        const params = {
            'userId': this.getCurrentUserName()
        };
        return this.http.get(`${environment.apiUrl}/basket/getBasketDetailsByUserId`, { params });
    }

    saveBasketData(basketList: any) {
        let headers = new HttpHeaders({
            'userId': this.getCurrentUserName()
        });
        return this.http.post(`${environment.apiUrl}/basket/save/purge`, basketList, { headers: headers });
    }

    clearAllBasketItems(): any {
        const params = {
            'userId': this.getCurrentUserName()
        };
        return this.http.get(`${environment.apiUrl}/basket/clear`, { params });
    }

    cancelAllBasketItems(): any {
        const params = {
            'userId': this.getCurrentUserName()
        };
        return this.http.delete(`${environment.apiUrl}/basket/delete`, { params });
    }

    getHomeScreenData() {
        if (this.authenticationService.currentUser()["userRole"] === 'Admin' ? true : false) {
            return this.http.get(`${environment.apiUrl}/home/status`);
        }
        else {
            const params = {
                "userId": this.authenticationService.currentUser()["userName"]
            }
            return this.http.get(`${environment.apiUrl}/home/statusByuserId`, { params });
        }

    }
    checkLableExist(lableName: any) {
        const params = {
            "lableName": lableName
        }
        return this.http.get(`${environment.apiUrl}/request/checkLableExist`, { params });
    }
}