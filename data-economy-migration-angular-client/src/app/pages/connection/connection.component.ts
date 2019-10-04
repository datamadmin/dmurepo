import { Component, OnInit } from '@angular/core';
import { ConfirmationService } from 'primeng/api';
import { AppService } from 'src/app/core/services/app.service';
import { NotificationService } from 'src/app/core/services/notification.service';
import { AuthenticationService } from 'src/app/core/services/auth.service';
import { Router } from '@angular/router';

import {
    CONNECTION_TYPE,
    AUTHENTICATION_TYPE,
    FORMAT_TYPE,
    COMPRESSION_TYPE,
    YES_OR_NO_OPTIONS,
    CONNECTION_GROUP,
    SC_CREDENTIALS_ACCESS_TYPE,
    CREDENTIAL_STORAGE_TYPE
} from 'src/app/core/constants/connection.constants';

@Component({
    selector: 'app-connection',
    templateUrl: './connection.component.html',
    styleUrls: ['./connection.component.scss']
})

export class ConnectionComponent implements OnInit {

    breadCrumbItems: Array<{}>;

    awsToS3ConnectionFlag: boolean = false;
    hdfsConnectionFlag: boolean = false;
    isAdmin: boolean = false;

    constructor(
        private confirmationService: ConfirmationService,
        private appService: AppService,
        private notificationService: NotificationService,
        private router: Router,
        private authenticationService: AuthenticationService,

    ) { }


    private connectionModel = {
        "connectionGroup": "",
        "connectionType": CONNECTION_TYPE.DIRECT_HDFS,
        "awsAccessIdLc": "",
        "awsSecretKeyLc": "",
        "awsAccessIdSc": "",
        "awsSecretKeySc": "",
        "roleArn": "",
        "principalArn": "",
        "samlProviderArn": "",
        "roleSesnName": "",
        "policyArnMembers": "",
        "externalId": "",
        "fdrtdUserName": "",
        "inlineSesnPolicy": "",
        "duration": 3600,
        "ldapUserName": "",
        "ldapUserPassw": "",
        "ldapDomain": "",
        "scCrdntlAccessType": SC_CREDENTIALS_ACCESS_TYPE.ASSUME,
        "hiveConnEnabled": false,
        "impalaConnEnabled": false,
        "sparkConnEnabled": false,
        "hiveHostName": "",
        "hivePortNmbr": "",
        "impalaHostName": "",
        "impalaPortNmbr": "",
        "sqlWhDir": "",
        "hiveMsUri": "",
        "authenticationType": AUTHENTICATION_TYPE.UNSECURED,
        "credentialStrgType": "",
        "hdfsLdapUserName": "",
        "hdfsLdapUserPassw": "",
        "hdfsLdapDomain": "",
        "kerberosHostRealm": "",
        "kerberosHostFqdn": "",
        "kerberosServiceName": "",
        "sslKeystorePath": "",
        "tgtFormatPropTempDto": {
            "formatType": FORMAT_TYPE.SOURCE,
            "compressionType": COMPRESSION_TYPE.SOURCE,
            "fieldDelimiter": ""
        },
        "tgtOtherPropDto": {
            "parallelJobs": "",
            "parallelUsrRqst": "",
            "tempHiveDB": "",
            "tempHdfsDir": "",
            "hdfsEdgeNode": "",
            "hdfsUserName": "",
            "hdfsPemLocation": "",
            "hadoopInstallDir": "",
            "tokenizationInd": YES_OR_NO_OPTIONS.NO,
            "ptgyDirPath": ""
        }
    }

    ngOnInit() {
        this.breadCrumbItems = [{ label: 'Home', path: '/app/home' }, { label: 'Settings', active: true }, { label: 'Connection', active: true }];
        this.isAdmin = this.authenticationService.currentUser()["userRole"] === 'Admin' ? true : false;
        this.getConnectionInfo();
    }

    getConnectionInfo() {
        this.appService.getConnectionDetails().subscribe(
            (res: any) => {
                this.connectionModel = res;
            },
            (error) => {
                this.notificationService.showError(error || "System Temporarly unavailable");
            });
    };

    cancelConnection(connectionGroup: CONNECTION_GROUP) {
        if (confirm("Are you sure to want to cancel")) {
            this.getConnectionInfo();
        }
    }

    isPropValExists(value) {
        if (value == null || value == undefined || value.length < 1) {
            return false;
        }
        return true;
    }

    validateConnectionDetails(connectionGroup: CONNECTION_GROUP) {
        if (connectionGroup === CONNECTION_GROUP.AWS_TO_S3) {
            if (this.connectionModel.connectionType === CONNECTION_TYPE.DIRECT_LC) {
                if (!this.isPropValExists(this.connectionModel.awsAccessIdLc)) {
                    this.notificationService.showError("AWS Access ID is mandatory");
                    return false;
                }
                else if (!this.isPropValExists(this.connectionModel.awsSecretKeyLc)) {
                    this.notificationService.showError("AWS Secret Key is mandatory");
                    return false;
                }
            }

            else if (this.connectionModel.connectionType === CONNECTION_TYPE.DIRECT_SC) {
                if (this.connectionModel.scCrdntlAccessType === SC_CREDENTIALS_ACCESS_TYPE.ASSUME) {
                    if (!this.isPropValExists(this.connectionModel.awsAccessIdSc)) {
                        this.notificationService.showError("AWS Access ID is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.awsSecretKeySc)) {
                        this.notificationService.showError("AWS Secret Key is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.roleArn)) {
                        this.notificationService.showError("Role ARN is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.roleSesnName)) {
                        this.notificationService.showError("Role Session Name is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.inlineSesnPolicy)) {
                        this.notificationService.showError("Inline Session Policy is mandatory");
                        return false;
                    }
                }
                else if (this.connectionModel.scCrdntlAccessType === SC_CREDENTIALS_ACCESS_TYPE.ASSUME_SAML) {
                    if (!this.isPropValExists(this.connectionModel.ldapUserName)) {
                        this.notificationService.showError("LDAP User Name is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.ldapUserPassw)) {
                        this.notificationService.showError("LDAP User Password is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.ldapDomain)) {
                        this.notificationService.showError("LDAP Domain is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.samlProviderArn)) {
                        this.notificationService.showError("SAML Provider ARN is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.principalArn)) {
                        this.notificationService.showError("Principal ARN is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.roleArn)) {
                        this.notificationService.showError("Role ARN is mandatory");
                        return false;
                    }
                }
                else if (this.connectionModel.scCrdntlAccessType === SC_CREDENTIALS_ACCESS_TYPE.AWS_FEDERATED_USER) {
                    if (!this.isPropValExists(this.connectionModel.fdrtdUserName)) {
                        this.notificationService.showError("Federated User Name is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.awsAccessIdSc)) {
                        this.notificationService.showError("AWS Access ID is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.awsSecretKeySc)) {
                        this.notificationService.showError("AWS Secret Key is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.inlineSesnPolicy)) {
                        this.notificationService.showError("Inline Session Policy is mandatory");
                        return false;
                    }
                }
                else {
                    this.notificationService.showError("Please select AWS Credential Access type");
                    return false;
                }
            }
        }

        else if (connectionGroup === CONNECTION_GROUP.HDFS) {
            if (this.connectionModel.hiveConnEnabled || this.connectionModel.impalaConnEnabled || this.connectionModel.sparkConnEnabled) {
                if (this.connectionModel.hiveConnEnabled) {
                    if (!this.isPropValExists(this.connectionModel.hiveHostName)) {
                        this.notificationService.showError("Hive Host Name is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.hivePortNmbr)) {
                        this.notificationService.showError("Hive Port Number is mandatory");
                        return false;
                    }
                }

                if (this.connectionModel.impalaConnEnabled) {
                    if (!this.isPropValExists(this.connectionModel.impalaHostName)) {
                        this.notificationService.showError("Impala Host Name is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.impalaPortNmbr)) {
                        this.notificationService.showError("Impala Port Number is mandatory");
                        return false;
                    }
                }

                if (this.connectionModel.sparkConnEnabled) {
                    if (!this.isPropValExists(this.connectionModel.sqlWhDir)) {
                        this.notificationService.showError("SQL Warehouse Dir is mandatory");
                        return false;
                    }
                    else if (!this.isPropValExists(this.connectionModel.hiveMsUri)) {
                        this.notificationService.showError("Hive Metastore URI is mandatory");
                        return false;
                    }
                }
            }
            else {
                this.notificationService.showError("Please select atleast one connection");
                return false;
            }

            if (this.connectionModel.authenticationType === AUTHENTICATION_TYPE.UNSECURED || this.connectionModel.authenticationType === AUTHENTICATION_TYPE.SECURED) {
                if (this.connectionModel.authenticationType == AUTHENTICATION_TYPE.SECURED) {
                    if (this.connectionModel.credentialStrgType === CREDENTIAL_STORAGE_TYPE.LDAP || this.connectionModel.credentialStrgType === CREDENTIAL_STORAGE_TYPE.KERBEROS) {
                        if (this.connectionModel.credentialStrgType === CREDENTIAL_STORAGE_TYPE.LDAP) {
                            if (!this.isPropValExists(this.connectionModel.hdfsLdapUserName)) {
                                this.notificationService.showError("LDAP User Name is mandatory");
                                return false;
                            }
                            else if (!this.isPropValExists(this.connectionModel.hdfsLdapUserPassw)) {
                                this.notificationService.showError("LDAP Password  is mandatory");
                                return false;
                            }
                            else if (!this.isPropValExists(this.connectionModel.hdfsLdapDomain)) {
                                this.notificationService.showError("LDAP Domain is mandatory");
                                return false;
                            }
                        }
                        else if (this.connectionModel.credentialStrgType === CREDENTIAL_STORAGE_TYPE.KERBEROS) {
                            if (!this.isPropValExists(this.connectionModel.kerberosHostRealm)) {
                                this.notificationService.showError("Host Realm is mandatory");
                                return false;
                            }
                            else if (!this.isPropValExists(this.connectionModel.kerberosHostFqdn)) {
                                this.notificationService.showError("Host FQDN is mandatory");
                                return false;
                            }
                            else if (!this.isPropValExists(this.connectionModel.kerberosServiceName)) {
                                this.notificationService.showError("Service Name is mandatory");
                                return false;
                            }
                            else if (!this.isPropValExists(this.connectionModel.sslKeystorePath)) {
                                this.notificationService.showError("SSL Keystore Path is mandatory");
                                return false;
                            }
                        }
                    }
                    else {
                        this.notificationService.showError("Please select authentication storage type");
                        return false;
                    }
                }
            }
            else {
                this.notificationService.showError("Please select authentication type");
                return false;
            }
        }

        else if (connectionGroup === CONNECTION_GROUP.TARGET_FILE_PROPS) {
            if (this.isPropValExists(this.connectionModel.tgtFormatPropTempDto.formatType)) {
                if (this.connectionModel.tgtFormatPropTempDto.formatType == FORMAT_TYPE.TEXT) {
                    if (!this.isPropValExists(this.connectionModel.tgtFormatPropTempDto.fieldDelimiter)) {
                        this.notificationService.showError("Field Delimiter is mandatory");
                        return false;
                    }
                }
            }
            else {
                this.notificationService.showError("Please select Target Format");
                return false;
            }

            if (!this.isPropValExists(this.connectionModel.tgtFormatPropTempDto.compressionType)) {
                this.notificationService.showError("Please select Target Compression");
                return false;
            }
        }

        else if (connectionGroup === CONNECTION_GROUP.OTHER_PROPS) {
            if (!this.isPropValExists(this.connectionModel.tgtOtherPropDto.parallelJobs) || parseInt(this.connectionModel.tgtOtherPropDto.parallelJobs) <= 0) {
                this.notificationService.showError("No of Parallel Copy Jobs should be greater than Zero");
                return false;
            }
            else if (!this.isPropValExists(this.connectionModel.tgtOtherPropDto.parallelUsrRqst) || parseInt(this.connectionModel.tgtOtherPropDto.parallelUsrRqst) <= 0) {
                this.notificationService.showError("No of Parallel User Request Processing should be greater than Zero");
                return false;
            }
        }
        else {

        }
        return true;
    }

    markConnectionFlag(connectionGroup: CONNECTION_GROUP, status) {
        switch (connectionGroup) {
            case CONNECTION_GROUP.AWS_TO_S3:
                this.awsToS3ConnectionFlag = status;
                break;
            case CONNECTION_GROUP.HDFS:
                this.hdfsConnectionFlag = status;
                break;
            default:
                break;
        }
    }

    testConnection(connectionGroup: CONNECTION_GROUP) {
        if (this.validateConnectionDetails(connectionGroup)) {
            this.connectionModel.connectionGroup = connectionGroup;
            this.appService.testConnection(this.connectionModel).subscribe((res) => {
                this.markConnectionFlag(connectionGroup, true);
                this.notificationService.showSuccess('Test connection successfull');
            }, (error) => {
                console.log(error);
                this.markConnectionFlag(connectionGroup, false);
                this.notificationService.showError(error || 'System Temporarly Unavailable . Please try again');
            });
        }
    }

    saveConnection(connectionGroup) {
        // if (this.validateConnectionDetails(connectionGroup)) {
        this.connectionModel.connectionGroup = connectionGroup;
        this.appService.saveConnection(this.connectionModel).subscribe((res) => {
            this.markConnectionFlag(connectionGroup, false);
            if (this.connectionModel.connectionGroup === CONNECTION_GROUP.OTHER_PROPS) {
                this.authenticationService.setTokenizationEnabled(this.connectionModel.tgtOtherPropDto.tokenizationInd === YES_OR_NO_OPTIONS.YES ? true : false);
            }
            this.notificationService.showSuccess('Connection saved successfully');
        }, (error) => {
            this.notificationService.showError(error || 'System Temporarly Unavailable . Please try again');
        });
        // }
    }

}

