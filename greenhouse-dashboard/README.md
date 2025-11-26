# Greenhouse Dashboard 🌿

A modern React/Next.js dashboard for monitoring and controlling greenhouse environments. Built with Next.js 15, TypeScript, TailwindCSS, and Shadcn/UI components.

## Features

### 📊 Dashboard Home
- Real-time metrics display (Temperature, Humidity, Luminosity)
- Interactive parameter charts with Recharts
- Equipment status overview
- Recent alerts notifications

### 📐 Parameters Management
- CRUD operations for greenhouse parameters
- Configure thresholds (min/max values)
- Parameter types: Temperature, Humidity, Luminosity, CO2, pH, Conductivity

### 📈 Measurements
- Historical measurement data with pagination
- Filter by date range, parameter type, and alerts
- Interactive line charts
- CSV export functionality
- Auto-refresh toggle

### ⚙️ Equipment Control
- Grid/List view of all equipment
- Status toggles (activate/deactivate)
- Equipment details with action history
- Manual action creation
- Equipment types: Ventilator, Heating, Lighting, Irrigation, Humidifier

### ⚡ Actions Log
- Action history with filters
- Status tracking (Pending, In Progress, Completed, Failed)
- Filter by equipment, status, action type
- Auto-refresh for real-time updates

### 🔔 Alerts
- Alert management with severity levels (Critical, Warning, Info)
- Acknowledge/dismiss functionality
- Filter by severity and status
- Bulk actions (acknowledge all, clear acknowledged)

## Tech Stack

- **Framework**: Next.js 15.0.3 with App Router
- **Language**: TypeScript
- **Styling**: TailwindCSS 3.4
- **UI Components**: Shadcn/UI (Radix UI primitives)
- **Charts**: Recharts
- **State Management**: React Query (TanStack Query)
- **HTTP Client**: Axios
- **Icons**: Lucide React
- **Date Handling**: date-fns
- **Notifications**: react-hot-toast

## Prerequisites

- Node.js 18+ 
- npm or yarn or pnpm
- Backend API running at `http://localhost:8080/api`

## Installation

1. **Clone the repository** (if not already done)
   ```bash
   cd greenhouse-management-system/greenhouse-dashboard
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   # or
   pnpm install
   ```

3. **Configure environment variables**
   
   Copy the example environment file:
   ```bash
   cp .env.example .env.local
   ```
   
   Update the values in `.env.local`:
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:8080/api
   NEXT_PUBLIC_ENVIRONNEMENT_API_URL=http://localhost:8080/api/environnement
   NEXT_PUBLIC_CONTROLE_API_URL=http://localhost:8080/api/controle
   ```

4. **Start the development server**
   ```bash
   npm run dev
   # or
   yarn dev
   # or
   pnpm dev
   ```

5. **Open the dashboard**
   
   Navigate to [http://localhost:3000](http://localhost:3000)

## Project Structure

```
greenhouse-dashboard/
├── src/
│   ├── app/                    # Next.js App Router pages
│   │   ├── page.tsx            # Dashboard home
│   │   ├── parametres/         # Parameters management
│   │   ├── mesures/            # Measurements display
│   │   ├── equipements/        # Equipment control
│   │   ├── actions/            # Actions log
│   │   ├── alertes/            # Alerts management
│   │   └── login/              # Authentication
│   │
│   ├── components/
│   │   ├── ui/                 # Shadcn/UI components
│   │   ├── layout/             # Layout components (Sidebar, Header)
│   │   └── dashboard/          # Dashboard-specific components
│   │
│   ├── hooks/                  # React Query hooks
│   │   ├── use-environnement.ts
│   │   └── use-controle.ts
│   │
│   ├── lib/
│   │   ├── api/               # API services
│   │   │   ├── api-client.ts  # Axios instance
│   │   │   ├── environnement.ts
│   │   │   └── controle.ts
│   │   ├── auth.tsx           # Authentication provider
│   │   └── utils.ts           # Utility functions
│   │
│   └── types/                 # TypeScript type definitions
│       └── index.ts
│
├── public/                    # Static assets
├── tailwind.config.ts         # TailwindCSS configuration
├── next.config.js             # Next.js configuration
└── tsconfig.json              # TypeScript configuration
```

## API Integration

The dashboard connects to two microservices through the API Gateway:

### Environnement Service (`/api/environnement`)
- `GET/POST /parametres` - List/Create parameters
- `GET/PUT/DELETE /parametres/:id` - Get/Update/Delete parameter
- `GET/POST /mesures` - List/Create measurements
- `GET /mesures/parametre/:id` - Measurements by parameter
- `GET /stats/latest` - Latest measurements

### Controle Service (`/api/controle`)
- `GET/POST /equipements` - List/Create equipment
- `GET/PUT/DELETE /equipements/:id` - Get/Update/Delete equipment
- `PATCH /equipements/:id/statut` - Update equipment status
- `GET/POST /actions` - List/Create actions
- `GET /actions/equipement/:id` - Actions by equipment

## Available Scripts

```bash
# Development
npm run dev          # Start development server

# Building
npm run build        # Create production build
npm run start        # Start production server

# Linting
npm run lint         # Run ESLint
```

## Authentication

The dashboard includes a JWT-ready authentication structure:

1. **Login Page** (`/login`) - User authentication form
2. **Auth Provider** (`src/lib/auth.tsx`) - Context for auth state
3. **Protected Routes** - HOC for securing pages
4. **Token Management** - Access/refresh token handling in API client

To enable full authentication:
1. Implement the auth endpoints in your backend
2. Update the login function in `src/lib/auth.tsx`
3. Wrap your app with `AuthProvider` and `ProtectedRoute`

## Theming

The dashboard supports light and dark modes with CSS variables:

- Theme toggle in the header
- Automatic system preference detection
- Custom color scheme based on green/emerald tones

## Responsive Design

- Fully responsive layout
- Mobile-friendly navigation with hamburger menu
- Adaptive grid layouts
- Touch-friendly controls

## Real-time Features

- Auto-refresh toggles on data-heavy pages
- React Query automatic background refetching
- SSE/WebSocket ready structure for live updates

## Customization

### Adding New Components

Use the Shadcn/UI CLI to add more components:
```bash
npx shadcn-ui@latest add [component-name]
```

### Extending API Services

1. Add type definitions in `src/types/index.ts`
2. Create API functions in `src/lib/api/`
3. Create React Query hooks in `src/hooks/`

## Troubleshooting

### API Connection Issues
- Ensure the backend services are running
- Check the API URL in `.env.local`
- Verify CORS settings on the backend

### Build Errors
- Clear `.next` folder and `node_modules`
- Run `npm install` again
- Check TypeScript errors with `npx tsc --noEmit`

## License

This project is part of the Greenhouse Management System microservices architecture.

## Contributing

1. Create a feature branch
2. Make your changes
3. Submit a pull request

---

Built with ❤️ using Next.js and TailwindCSS
