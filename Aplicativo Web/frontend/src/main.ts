import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

/** Punto de entrada: arranca el componente raíz con la configuración global. */
bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
