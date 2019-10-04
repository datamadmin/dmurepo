import { Component, OnInit, ViewChild } from '@angular/core';
import { Table } from 'primeng/table';
import { ActivatedRoute } from '@angular/router';
import { NotificationService } from 'src/app/core/services/notification.service';
import { AppService } from 'src/app/core/services/app.service';

@Component({
  selector: 'app-recon',
  templateUrl: './recon.component.html',
  styleUrls: ['./recon.component.scss'],
  providers: []
})

/**
 * Advanced table component - handling the advanced table with navbar and content
 */
export class ReconComponent implements OnInit {
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

  @ViewChild("masterTable", { static: false }) masterTable: Table;

  constructor(
    private route: ActivatedRoute,
    private appService: AppService,
    private notificationService: NotificationService
  ) { }

  ngAfterViewInit() {
    this.masterTable.filter(this.filterParams.status, "status", 'startsWith');
  }

  ngOnInit() {
    // tslint:disable-next-line: max-line-length
    this.breadCrumbItems = [{ label: 'Home', path: '/app/home' }, { label: 'Recon', active: true }];

    /**
     * fetch data
     */
    this._fetchData();


    this.masterCols = [
      { field: 'requestNo', header: 'Request No' },
      { field: 'userId', header: 'Requested By' },
      { field: 'requestedTime', header: 'Requested Time' },
      { field: 'status', header: 'Recon Status' },
      { field: 'requestType', header: 'Request Type' },
      { field: 'reconStartTime', header: 'Recon Start Time' },
      { field: 'reconCmpltTime', header: 'Recon Completed Time' },
    ];

    this.detailCols = [
      { field: 'srNo', header: 'Sr.no' },
      { field: 'schemaName', header: 'Database Name' },
      { field: 'tableName', header: 'Table Name' },
      { field: 'filterCondition', header: 'Filter Condition' },
      { field: 'targetS3Bucket', header: 'Target Bucket Name' },
      { field: 'incrementalFlag', header: 'Incremental Flag' },
      { field: 'incrementalColumn', header: 'Incremental Column' },
      { field: 'sourceCount', header: 'Source Account' },
      { field: 'targetCount', header: 'Target Account' },
      { field: 'status', header: 'Recon Status' }
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
    this.appService.getReconMainList().subscribe(
      (res: any) => {
        this.masterList = res;
      },
      (error) => {
        this.notificationService.showError(error || "System Temporarly unavailable");
      });
  }

  openReconDetails(selectedItem: any) {
    this.detailList = [];
    this.selectedRec = selectedItem;
    this.appService.getReconDetailsById(this.selectedRec.requestNo).subscribe(
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
