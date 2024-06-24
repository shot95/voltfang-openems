import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { Edge, EdgeConfig, Service, Utils, Websocket, Widgets } from 'src/app/shared/shared';

@Component({
  selector: 'live',
  templateUrl: './live.component.html',
})
export class LiveComponent implements OnInit, OnDestroy {

  public edge: Edge = null;
  public config: EdgeConfig = null;
  public widgets: Widgets = null;
  private stopOnDestroy: Subject<void> = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router, // oEMS
    public service: Service,
    protected utils: Utils,
    protected websocket: Websocket,
  ) { }

  public ngOnInit() {
    this.service.setCurrentComponent('', this.route);
    this.service.currentEdge.subscribe((edge) => {
      this.edge = edge;
    });
    // oEMS Start
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.stopOnDestroy),
    ).subscribe(() => {
      this.updateConfig();
    });

    // Initial fetch of config
    this.updateConfig();
    // oEMS End
  }

  // oEMS method
  private updateConfig(): void {
    this.service.getConfig().then(config => {
      this.config = config;
      this.widgets = config.widgets;
    });
  }

  public ngOnDestroy() {
    this.stopOnDestroy.next();
    this.stopOnDestroy.complete();
  }
}
