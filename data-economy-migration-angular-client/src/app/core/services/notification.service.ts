import { Injectable } from '@angular/core';
import { MessageService } from 'primeng/api';

@Injectable()
export class NotificationService {

    constructor(private messageService: MessageService) { }

    showSuccess(msg: string) {
        this.setMessage({ severity: 'success', detail: msg });
    }

    showInfo(msg: string) {
        this.setMessage({ severity: 'info', detail: msg });
    }

    showWarn(msg: string) {
        this.setMessage({ severity: 'warn', detail: msg });
    }

    showError(msg: string) {
        this.setMessage({ severity: 'error', detail: msg });
    }

    public setMessage(msg: any) {
        this.clear();
        this.messageService.add(msg);
    }

    public clear() {
        this.messageService.clear();
    }

}