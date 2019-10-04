import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import 'rxjs/add/operator/filter';
import { Table } from 'primeng/table';
import { AppService } from 'src/app/core/services/app.service';
import { NotificationService } from 'src/app/core/services/notification.service';
import { AuthenticationService } from 'src/app/core/services/auth.service';

@Component({
    selector: 'app-history',
    templateUrl: './history.component.html',
    styleUrls: ['./history.component.scss'],
    providers: []
})

export class HistoryComponent implements OnInit, AfterViewInit {
    // bread crum data
    breadCrumbItems: Array<{}>;

    // Table data
    masterList: any = [];
    detailList: any = [];
    masterCols: any;
    detailCols: any;

    showMaster: boolean = true;
    showDetail: boolean = false;

    selectedRec: any;

    filterParams: any = {};

    isTokenizationEnabled: boolean = false;

    @ViewChild("masterTable", { static: false }) masterTable: Table;

    constructor(
        private route: ActivatedRoute,
        private appService: AppService,
        private notificationService: NotificationService,
        private authenticationService: AuthenticationService
    ) { }

    ngAfterViewInit() {
        this.masterTable.filter(this.filterParams.status, "status", 'startsWith');
    }

    ngOnInit() {
        this.isTokenizationEnabled = this.authenticationService.isTokenizationEnabled();
        // tslint:disable-next-line: max-line-length
        this.breadCrumbItems = [{ label: 'Home', path: '/app/home' }, { label: 'History', active: true }];

        /**
         * fetch data
         */
        this._fetchData();

        if (this.isTokenizationEnabled) {
            this.masterCols = [
                { field: 'requestNo', header: 'Request No' },
                { field: 'userId', header: 'Requested By' },
                { field: 'requestedTime', header: 'Requested Time' },
                { field: 'tknztnEnabled', header: 'Tokenization Enabled' },
                { field: 'status', header: 'Status' },
                { field: 'requestType', header: 'Request Type' },
                { field: 'scriptGenCmpltTime', header: 'Script Generation Completed Time' },
                { field: 'exctnCmpltTime', header: 'Execution Completed Time' }
            ];
        }
        else {
            this.masterCols = [
                { field: 'requestNo', header: 'Request No' },
                { field: 'userId', header: 'Requested By' },
                { field: 'requestedTime', header: 'Requested Time' },
                { field: 'status', header: 'Status' },
                { field: 'requestType', header: 'Request Type' },
                { field: 'scriptGenCmpltTime', header: 'Script Generation Completed Time' },
                { field: 'exctnCmpltTime', header: 'Execution Completed Time' }
            ];

        }

        this.detailCols = [
            { field: 'srNo', header: 'Sr.No' },
            { field: 'schemaName', header: 'DB Name' },
            { field: 'tableName', header: 'Table Name' },
            { field: 'filterCondition', header: 'Filter Condition' },
            { field: 'targetS3Bucket', header: 'Target Bucket Name' },
            { field: 'incrementalFlag', header: 'Incremental Flag' },
            { field: 'incrementalClmn', header: 'Incremental Column' },
            { field: 'status', header: 'Request Status' }
        ];

        for (const key in this.masterCols) {
            this.filterParams[key] = "";
        }

        this.route.queryParams
            .filter(params => params.status)
            .subscribe(params => {
                this.filterParams.status = params["status"] || "";
            });

    }

    /**
     * fetches the table value
     */
    _fetchData() {
        this.appService.getHistoryMainList().subscribe(
            (res: any) => {
                this.masterList = res;
            },
            (error) => {
                this.notificationService.showError(error || "System Temporarly unavailable");
            });
    }

    downloadTokenizationDetails() {

    }

    openReconDetails(selectedItem: any) {
        this.detailList = [];
        this.selectedRec = selectedItem;
        this.appService.getHistoryDetailsById(this.selectedRec.requestNo).subscribe(
            (res: any) => {
                this.detailList = res;
                this.showMaster = false;
                this.showDetail = true;
            },
            (error) => {
                this.notificationService.showError(error || "System Temporarly unavailable");
            });
    }

    cancelDetails() {
        this.selectedRec = null;
        this.showMaster = true;
        this.showDetail = false;
        this.detailList = [];
        this._fetchData();
    }
}
