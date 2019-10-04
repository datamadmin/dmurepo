import { Component, OnInit, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { AppService } from 'src/app/core/services/app.service';
import { NotificationService } from 'src/app/core/services/notification.service';

// import * as Highcharts from 'highcharts';
// require('highcharts/highcharts-3d.js')(Highcharts);

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})

export class HomeComponent implements OnInit {


  // bread crumb items
  breadCrumbItems: Array<{}>;

  chartData: any[] = [];

  requestStatusChartConfig: any;
  reconStatusChartConfig: any;

  constructor(
    private ngZone: NgZone,
    private router: Router,
    private appService: AppService,
    private notificationService: NotificationService
  ) { }

  ngOnInit() {

    let self = this;

    this.requestStatusChartConfig = {
      type: 'pie',
      title: {
        text: "REQUEST STATUS",
        align: 'center',
        margin: 20,
        offsetX: 0,
        offsetY: 0,
        style: {
          fontSize: '14px'
        }
      },
      option: {
        pie: {
          expandOnClick: false
        }
      },
      height: 250,
      piechartcolor: ['#f7b84b', '#4fc6e1', '#1abc9c', "#f1556c"],
      dataLabels: {
        enabled: false
      },
      legend: {
        show: false,
      },
      tooltip: {
        x: {
          show: false
        }
      },
      grid: {
        show: false,
        padding: {
          top: 30,
          left: 0,
          right: 0,
          bottom: 0
        }
      },
      events: {
        dataPointSelection: (event, chartContext, config) => {
          self.contentClicked('request', event, chartContext, config);
        }
      },
      labels: ["In Progress", "Submitted", "Successful", "Failed"]
    };

    this.reconStatusChartConfig = {
      type: 'pie',
      title: {
        text: "RECON STATUS",
        align: 'center',
        margin: 20,
        offsetX: 0,
        offsetY: 0,
        style: {
          fontSize: '14px'
        }
      },
      option: {
        pie: {
          expandOnClick: false
        }
      },
      height: 250,
      piechartcolor: ['#007bff', '#f7b84b', '#4fc6e1', '#1abc9c', "#f1556c"],
      dataLabels: {
        enabled: false
      },
      legend: {
        show: false,
      },
      tooltip: {
        x: {
          show: false
        }
      },
      grid: {
        show: false,
        padding: {
          top: 30,
          left: 0,
          right: 0,
          bottom: 0
        }
      },
      events: {
        dataPointSelection: (event, chartContext, config) => {
          self.contentClicked('recon', event, chartContext, config);
        }
      },
      labels: ["Not Started", "In Progress", "Submitted", "Successful", "Failed"]
    };

    this.fetchChartData();
  }

  /**
   * content refresh
   */
  contentRefresh() {
    console.log('Data refresh requested');
    this.fetchChartData();
  }

  contentClicked(requestType, event, chartContext, config) {
    console.log(config);
    if (requestType == 'request') {
      let status = this.chartData[0]["requestStatus"]["labels"][config.dataPointIndex];
      this.ngZone.run(() => {
        this.router.navigate(['/app/history'], { queryParams: { status: status } });
      });
    }
    else if (requestType == 'recon') {
      let status = this.chartData[0]["reconStatus"]["labels"][config.dataPointIndex];
      this.ngZone.run(() => {
        this.router.navigate(['/app/recon'], { queryParams: { status: status } });
      });
    }
  }

  /**
   * fetches the dashboard-2 data
   */
  private fetchChartData() {
    this.appService.getHomeScreenData().subscribe(
      (res) => {
        this.chartData = [];
        let requestPropList = ["In Progress", "Submitted", "Successful", "Failed"];
        let reconPropList = ["Not Started", "In Progress", "Submitted", "Successful", "Failed"];

        let requestStatus = [];
        let reconStatus = [];

        requestPropList.forEach((prop) => {
          requestStatus.push(Math.round(res["reconHistoryMainCount"].hasOwnProperty(prop) ? (res["reconHistoryMainCount"][prop] / res["reconHistoryMainTotalCount"]) * 100 : 0));
        });

        reconPropList.forEach((prop) => {
          reconStatus.push(Math.round(res["reconMainCount"].hasOwnProperty(prop) ? (res["reconMainCount"][prop] / res["reconMainTotalCount"]) * 100 : 0));
        });

        this.chartData.push({
          "requestStatus": Object.assign({}, this.requestStatusChartConfig, { "series": requestStatus }),
          "reconStatus": Object.assign({}, this.reconStatusChartConfig, { "series": reconStatus })
        });
      },
      (error) => {
        console.log(error);
        this.notificationService.showError(error || "Error while fetching the info");
      });
  };
}
