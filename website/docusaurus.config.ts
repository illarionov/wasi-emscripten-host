import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'Wasi-emscripten-host',
  tagline: 'Dinosaurs are cool',
  favicon: 'img/favicon.ico',

  url: 'https://weh.released.at',
  baseUrl: '/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'illarionov', // Usually your GitHub org/user name.
  projectName: 'wasi-emscripten-host', // Usually your repo name.

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  staticDirectories: ['static', process.env.API_REFERENCE_DIRECTORY ?? 'kdoc' ],

  presets: [
    [
      'classic',
      {
        blog: false,
        docs: {
          routeBasePath: '/',
          sidebarPath: './sidebars.ts',
          breadcrumbs: false,
          editUrl:
            'https://github.com/illarionov/wasi-emscripten-host/tree/main/website',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/docusaurus-social-card.jpg',
    metadata: [{
      name: "keywords",
        content: "kotlin, wasm, emscripten, graalvm, chicory, chasm"
      }],
    navbar: {
      title: 'Wasi-emscripten-host',
      logo: {
        alt: 'No Logo',
        src: 'img/logo.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: "https://weh.released.at/api/index.html",
          label: 'API',
          position: 'left'
        },
        {
          href: 'https://github.com/illarionov/wasi-emscripten-host',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'light',

      copyright: `Copyright © ${new Date().getFullYear()} Wasi-emscripten-host authors. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'kotlin'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
