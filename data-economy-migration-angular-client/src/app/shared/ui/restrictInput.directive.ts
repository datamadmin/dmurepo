import { Directive, ElementRef, Input } from '@angular/core';
import * as Inputmask from 'inputmask';


@Directive({
    selector: '[restrict-input]',
})
export class RestrictInputDirective {

    // map of some of the regex strings I'm using (TODO: add your own)
    private regexMap = {
        numberOnly: '^[0-9]*$',
        charsOnly: '([A-z]*\\s)*',
        alpha: '^[a-zA-Z]*$',       
        alphaNumeric: '^[a-zA-Z0-9 ]*$'
    };

    constructor(private el: ElementRef) { }

    @Input('restrict-input')
    public set defineInputType(type: string) {
        Inputmask({ regex: this.regexMap[type], placeholder: '' })
            .mask(this.el.nativeElement);
    }

}