# CLAUDE.md — bikecheck

This file provides context and conventions for AI assistants working in this repository.

## Project Overview

**bikecheck** is a Progressive Web App (PWA) for tracking bicycle maintenance checklists.
The project is currently in its initial stage — only a README exists; no application code has been written yet.

- **Language of the project (user/UI):** Dutch (Nederlands)
- **Intended type:** PWA (installable, offline-capable web app)
- **Domain:** Bicycle maintenance scheduling and checklist tracking

## Current Repository State

```
bikecheck/
├── .git/
├── CLAUDE.md        ← this file
└── README.md        ← one-line project description
```

No application code, dependencies, tests, or build configuration exist yet.

## Technology Decisions (to be made)

None have been committed yet. When the stack is chosen, document it here. Likely candidates given the PWA nature:

- **Frontend framework:** Vue 3 / React / Svelte
- **Build tool:** Vite
- **CSS:** Tailwind CSS / plain CSS
- **State management:** Pinia (Vue) / Zustand (React) / Svelte stores
- **Offline/PWA:** vite-plugin-pwa / Workbox
- **Data persistence:** IndexedDB / localStorage / PocketBase / Supabase
- **Testing:** Vitest + Playwright (or Cypress for e2e)
- **Language:** TypeScript preferred over plain JavaScript

## Development Workflow

Once the project is initialized, the expected workflow is:

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Run tests
npm run test

# Build for production
npm run build

# Preview production build
npm run preview
```

Update this section when actual scripts are defined in `package.json`.

## Conventions (to be established)

Document choices here as they are made. Suggested defaults:

### File & Directory Structure

Follow the conventions of the chosen framework. For Vite-based projects:

```
src/
  components/     # Reusable UI components
  views/          # Page-level components (route targets)
  stores/         # State management
  composables/    # Shared logic (Vue) or hooks (React)
  assets/         # Static assets (images, fonts)
  types/          # TypeScript type definitions
public/           # Files served as-is (manifest.json, icons)
```

### Naming

- **Components:** PascalCase (`BikeChecklist.vue`)
- **Files (non-component):** kebab-case (`use-checklist.ts`)
- **Variables/functions:** camelCase
- **Constants:** UPPER_SNAKE_CASE

### Language

- **UI text / user-facing strings:** Dutch
- **Code, comments, commit messages, and documentation:** English

### Git

- Commit messages in English, imperative mood (`Add checklist item deletion`, not `Added` or `Adds`)
- Keep commits focused; one logical change per commit
- Branch names: `feature/<short-description>`, `fix/<short-description>`

### TypeScript

- Prefer explicit types over `any`
- Use `interface` for object shapes, `type` for unions/aliases
- Enable strict mode in `tsconfig.json`

## PWA Requirements

When implementing PWA features, ensure:

- `manifest.json` includes name, short_name, icons (192px and 512px minimum), theme_color, background_color, display: `standalone`
- A service worker handles offline caching of the app shell
- The app works offline for reading existing checklists

## Testing Guidelines

Once tests are added:

- Unit test business logic (checklist state, calculations) independently of UI
- Integration test component interactions
- E2e tests cover the critical user journey: create checklist → check items → view history

## AI Assistant Notes

- This project is **very early stage**. When asked to implement features, first check whether a tech stack has been chosen; if not, propose one before writing code.
- The project README is in Dutch; UI copy should be Dutch.
- Code and documentation should be in English.
- Before adding dependencies, consider bundle size impact on PWA performance.
- When creating files, follow the directory conventions above.
- If `package.json` or framework config files are not yet present, initialize them as part of the implementation task.
