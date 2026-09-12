const fs = require('fs');
const path = require('path');

const apiGatewayUri = process.env['API_GATEWAY_URI'];

if (!apiGatewayUri) {
  console.error(
    '\n[set-env] ERROR: falta la variable de entorno API_GATEWAY_URI.\n' +
    '  -> En Netlify: Site settings > Environment variables > agregar API_GATEWAY_URI\n' +
    '     con el Invoke URL del API Gateway (ej: https://xvrf5us4ma.execute-api.us-east-1.amazonaws.com/dev)\n' +
    '  -> En local: exporta la variable antes de compilar, ej:\n' +
    '     API_GATEWAY_URI=https://xvrf5us4ma.execute-api.us-east-1.amazonaws.com/dev npm run build:prod\n'
  );
  process.exit(1);
}

const envFileContent = `// Este archivo se genera automáticamente en cada build de producción
// a partir de la variable de entorno API_GATEWAY_URI (ver set-env.js).
// No edites la URL a mano: si el API Gateway cambia de ID, actualiza la
// variable en Netlify (o en tu entorno local) y vuelve a compilar.
export const environment = {
  production: true,
  msalConfig: {
    clientId: '8efd0921-05be-42e5-a08a-44e70d75b3a2',
    authority: 'https://dittostore.ciamlogin.com/86e0474b-6c7d-4e7b-8343-30ed0fe0bcb7',
    redirectUri: 'https://dittostore.netlify.app',
    postLogoutRedirectUri: 'https://dittostore.netlify.app'
  },
  apiConfig: {
    scopes: ['api://dittostore.onmicrosoft.com/dittostore-backend/Store.Access'],
    bffUri: '${apiGatewayUri}',
    gatewayUri: '${apiGatewayUri}'
  }
};
`;

const outputPath = path.join(__dirname, 'src', 'environments', 'environment.prod.ts');
fs.writeFileSync(outputPath, envFileContent, { encoding: 'utf8' });

console.log(`[set-env] environment.prod.ts generado con API_GATEWAY_URI = ${apiGatewayUri}`);