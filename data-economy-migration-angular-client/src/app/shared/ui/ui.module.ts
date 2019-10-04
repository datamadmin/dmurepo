import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { NgbCollapseModule, NgbDatepickerModule, NgbTimepickerModule, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ClickOutsideModule } from 'ng-click-outside';

import { SlimscrollDirective } from './slimscroll.directive';
import { CountToDirective } from './count-to.directive';
import { PagetitleComponent } from './pagetitle/pagetitle.component';
import { PortletComponent } from './portlet/portlet.component';
import { BlockCopyPasteDirective } from './block-copy-paste.directive';
import { RestrictInputDirective } from './restrictInput.directive';
import { TrimValueAccessor } from './trim-value-accessor.directive';

@NgModule({
  // tslint:disable-next-line: max-line-length
  declarations: [
    SlimscrollDirective,
    CountToDirective,
    PagetitleComponent,
    PortletComponent,
    TrimValueAccessor,
    RestrictInputDirective,
    BlockCopyPasteDirective
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ClickOutsideModule,
    NgbCollapseModule,
    NgbDatepickerModule,
    NgbTimepickerModule,
    NgbDropdownModule
  ],
  // tslint:disable-next-line: max-line-length
  exports: [
    SlimscrollDirective,
    CountToDirective,
    PagetitleComponent,
    PortletComponent,
    TrimValueAccessor,
    RestrictInputDirective,
    BlockCopyPasteDirective
  ]
})
export class UIModule { }
