export const environment = {
  production: false,
  msalConfig: {
    clientId: '8efd0921-05be-42e5-a08a-44e70d75b3a2',
    authority: 'https://dittostore.ciamlogin.com/86e0474b-6c7d-4e7b-8343-30ed0fe0bcb7',
    redirectUri: 'http://localhost:4200',
    postLogoutRedirectUri: 'http://localhost:4200'
  },
  apiConfig: {
    scopes: ['api://dittostore.onmicrosoft.com/dittostore-backend/Store.Access'],
    bffUri: 'http://localhost:8081' 
  }
};