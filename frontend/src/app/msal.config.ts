import { PublicClientApplication, InteractionType, LogLevel, BrowserCacheLocation } from '@azure/msal-browser';
import { MsalGuardConfiguration, MsalInterceptorConfiguration } from '@azure/msal-angular';
import { environment } from '../environments/environment';

export function MSALInstanceFactory(): PublicClientApplication {
  return new PublicClientApplication({
    auth: {
      clientId: environment.msalConfig.clientId,
      authority: environment.msalConfig.authority,
      redirectUri: environment.msalConfig.redirectUri,
      postLogoutRedirectUri: environment.msalConfig.postLogoutRedirectUri,
      knownAuthorities: ['dittostore.ciamlogin.com']
    },
    cache: {
      cacheLocation: BrowserCacheLocation.LocalStorage,
      storeAuthStateInCookie: false
    },
    system: {
      loggerOptions: {
        logLevel: LogLevel.Warning,
        loggerCallback: () => {},
        piiLoggingEnabled: false
      }
    }
  });
}

export function MSALGuardConfigFactory(): MsalGuardConfiguration {
  return {
    interactionType: InteractionType.Redirect,
    authRequest: {
      scopes: environment.apiConfig.scopes
    }
  };
}

export function MSALInterceptorConfigFactory(): MsalInterceptorConfiguration {
  const protectedResourceMap = new Map<string, Array<string>>();

  protectedResourceMap.set(environment.apiConfig.bffUri + '/bff/*', environment.apiConfig.scopes);
  protectedResourceMap.set(environment.apiConfig.bffUri + '/api/carritos/*', environment.apiConfig.scopes);
  protectedResourceMap.set(environment.apiConfig.bffUri + '/api/pedidos/*', environment.apiConfig.scopes);
  protectedResourceMap.set(environment.apiConfig.bffUri + '/api/pagos/*', environment.apiConfig.scopes);
  protectedResourceMap.set(environment.apiConfig.bffUri + '/api/reviews/*', environment.apiConfig.scopes);

  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap
  };

}