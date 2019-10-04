import { Directive, HostListener, Input } from '@angular/core';

@Directive({
    selector: '[blockCopyPaste]'
})
export class BlockCopyPasteDirective {
    constructor() { }

    private blockCopyPaste: any = false;

    @Input('blockCopyPaste')
    public set defineIsCopyPasteAllowed(blockCopyPaste: any) {
        this.blockCopyPaste = blockCopyPaste != "" ? blockCopyPaste : false;
    }

    @HostListener('paste', ['$event']) blockPaste(e: KeyboardEvent) {
        if (!this.blockCopyPaste) {
            e.preventDefault();
        }
    }

    @HostListener('copy', ['$event']) blockCopy(e: KeyboardEvent) {
        if (!this.blockCopyPaste) {
            e.preventDefault();
        }
    }

    @HostListener('cut', ['$event']) blockCut(e: KeyboardEvent) {
        if (!this.blockCopyPaste) {
            e.preventDefault();
        }
    }
}