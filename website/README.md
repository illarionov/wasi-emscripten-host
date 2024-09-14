# Website

This website is built using [Docusaurus](https://docusaurus.io/), a modern static website generator.

### Installation

```
$ npm install
```

### Local Development

```
$ npm run start
```

This command starts a local development server and opens up a browser window. Most changes are reflected live without having to restart the server.

### Build

```
$ npm run build
```

This command generates static content into the `build` directory and can be served using any static contents hosting service.

### Deployment

The static version of the site is built using Gradle and deployed using GitHub Actions.

```
$ ./gradlew aggregate-documentation:buildWebsite
```

This command, executed from the root of the project, builds the site for deployment into the 
`aggregate-documentation/build/outputs/website/` directory.
