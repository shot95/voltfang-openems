import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { TranslateService } from '@ngx-translate/core';
import { ComponentJsonApiRequest } from 'src/app/shared/jsonrpc/request/componentJsonApiRequest';
import { ExecuteSystemCommandRequest } from 'src/app/shared/jsonrpc/request/executeCommandRequest';
import { ExecuteSystemCommandResponse } from 'src/app/shared/jsonrpc/response/executeSystemCommandResponse';
import { Service, Utils, Websocket } from '../../../shared/shared';

type CommandFunction = (...args: (string | boolean | number)[]) => string;

// oEMS changes here needs changes here also: edge:/etc/sudoers.d/010_oems_allowed-commands
const COMMANDS: { [key: string]: CommandFunction; } = {
  'ping': (ip: string) => `/usr/bin/ping -4 -c4 ${ip}`, // oEMS
  'oems-restart': () => "/home/oems/maintenance/predefined-commands/restartOemsEdgeService.sh", // oEMS
  'system-restart': () => "/home/oems/maintenance/predefined-commands/restartSystem.sh", // oEMS
  'network-info': () => "/sbin/ifconfig", // oEMS
  'routing-info': () => "/sbin/route -n", // oEMS
  'show-network-neighbors': () => "/usr/sbin/arp -a", // oEMS
};

@Component({
  selector: SystemExecuteComponent.SELECTOR,
  templateUrl: './systemexecute.component.html',
})
export class SystemExecuteComponent implements OnInit {

  private static readonly SELECTOR = "systemExecute";

  public form: FormGroup;

  public model: any = {};
  public options: FormlyFormOptions = {};
  public fields: FormlyFieldConfig[] = [{
    key: 'predefined',
    type: 'radio',
    templateOptions: { options: [{ value: 'ping', label: 'Ping device in network' }] },
  }, {
    key: 'ping',
    hideExpression: (model: any, formState: any) => this.model['predefined'] !== 'ping',
    fieldGroup: [{
      key: 'ip',
      type: 'input',
      templateOptions: {
        label: 'IP-Address', placeholder: "192.168.0.1", required: true, pattern: /(\d{1,3}\.){3}\d{1,3}/,
      },
      validation: {
        messages: {
          pattern: (error, field: FormlyFieldConfig) => `"${field.formControl.value}" is not a valid IP Address`,
        },
      },
    }],
  }, {
    key: 'predefined',
    type: 'radio',
    templateOptions: {
      options: [
        { value: 'oems-restart', label: 'Restart oEMS Edge service' }, // oEMS
      ],
    },
  }, {
    key: 'predefined',
    type: 'radio',
    templateOptions: {
      options: [
        { value: 'system-restart', label: 'Restart the system' },
      ],
    },
  },{
    key: 'predefined',
    type: 'radio',
    templateOptions: {
      options: [
        { value: 'network-info', label: 'Show network information' }, // oEMS
      ],
    },
  },{
    key: 'predefined',
    type: 'radio',
    templateOptions: {
      options: [
        { value: 'routing-info', label: 'Show routing information' }, // oEMS
      ],
    },
  },{
    key: 'predefined',
    type: 'radio',
    templateOptions: {
      options: [
        { value: 'show-network-neighbors', label: 'Show network neighbors' }, // oEMS
      ],
    },
  }];

  constructor(
    private route: ActivatedRoute,
    protected utils: Utils,
    private websocket: Websocket,
    private service: Service,
    private translate: TranslateService,
    private formBuilder: FormBuilder,
  ) {
  }

  ngOnInit() {
    this.service.setCurrentComponent({ languageKey: 'Edge.Config.Index.systemExecute' }, this.route);
    this.form = this.formBuilder.group({
      username: new FormControl("root"),
      password: new FormControl(""),
      timeoutSeconds: new FormControl(5),
      runInBackground: new FormControl(false),
      command: new FormControl(""),
    });
  }

  public loading: boolean = false;
  public stdout: string[] = [];
  public stderr: string[] = [];
  public commandLogs: ExecuteSystemCommandRequest[] = [];

  public updatePredefined() {
    let command;
    if (!this.form.valid) {
      command = "";
    } else {
      const m = this.model;
      const cmd = COMMANDS[m.predefined];
      switch (m.predefined) {
        case "ping":
          command = cmd(m.ping.ip);
          break;
        case "oems-restart": // oEMS
        case "system-restart": // oEMS
        case "network-info" : // oEMS
        case "routing-info": // oEMS
        case "show-network-neighbors": // oEMS
        default:
          command = cmd();
      }
    }
    this.form.controls['command'].setValue(command);
  }

  public submit() {
    const username = this.form.controls['username'];
    const password = this.form.controls['password'];
    const timeoutSeconds = this.form.controls['timeoutSeconds'];
    const runInBackground = this.form.controls['runInBackground'];
    const command = this.form.controls['command'];

    this.service.getCurrentEdge().then(edge => {
      this.loading = true;
      this.stdout = [];
      this.stderr = [];
      const executeSystemCommandRequest = new ExecuteSystemCommandRequest({
        username: username.value,
        password: password.value,
        timeoutSeconds: timeoutSeconds.value,
        runInBackground: runInBackground.value,
        command: command.value,
      });

      edge.sendRequest(this.websocket,
        new ComponentJsonApiRequest({
          componentId: "_host",
          payload: executeSystemCommandRequest,
        })).then(response => {
          const result = (response as ExecuteSystemCommandResponse).result;
          this.loading = false;
          if (result.stdout.length == 0) {
            this.stdout = [""];
          } else {
            this.stdout = result.stdout;
          }
          this.stderr = result.stderr;

        }).catch(reason => {
          this.loading = false;
          this.stderr = ["Error executing system command:", reason.error.message];
        });
      this.commandLogs.unshift(executeSystemCommandRequest);
    });
  }

}
