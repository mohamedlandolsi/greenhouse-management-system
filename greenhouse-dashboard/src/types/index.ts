// ==========================================
// Parameter Types
// ==========================================

export type ParametreType = 'TEMPERATURE' | 'HUMIDITE' | 'LUMINOSITE';

export interface Parametre {
  id: number;
  nom: string;
  type: ParametreType;
  unite: string;
  seuilMin: number;
  seuilMax: number;
  description?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface ParametreRequest {
  nom: string;
  type: ParametreType;
  unite: string;
  seuilMin: number;
  seuilMax: number;
  description?: string;
}

// ==========================================
// Measurement Types
// ==========================================

export interface Mesure {
  id: number;
  parametreId: number;
  parametreType?: ParametreType;
  valeur: number;
  dateMesure: string;
  alerte: boolean;
  seuilMin?: number;
  seuilMax?: number;
  unite?: string;
  createdAt: string;
}

export interface MesureRequest {
  parametreId: number;
  valeur: number;
  dateMesure?: string;
}

// ==========================================
// Equipment Types
// ==========================================

export type EquipementType = 
  | 'VENTILATEUR' 
  | 'POMPE' 
  | 'CHAUFFAGE' 
  | 'ECLAIRAGE';

export type EquipementStatut = 'ACTIF' | 'INACTIF' | 'EN_PANNE' | 'MAINTENANCE';

// Backend uses EtatEquipement for equipment state
export type EtatEquipement = 'ACTIF' | 'INACTIF' | 'MAINTENANCE';

export interface Equipement {
  id: number;
  nom: string;
  type: EquipementType;
  statut: EquipementStatut;
  localisation?: string;
  description?: string;
  derniereAction?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface EquipementRequest {
  nom: string;
  type: EquipementType;
  etat: EtatEquipement;
  parametreAssocie?: number;
}

// ==========================================
// Action Types
// ==========================================

export type TypeAction = 'ACTIVER' | 'DESACTIVER' | 'AJUSTER' | 'URGENCE';

export type StatutAction = 'EN_ATTENTE' | 'EN_COURS' | 'EXECUTEE' | 'TERMINEE' | 'ECHOUEE' | 'ANNULEE';

export interface Action {
  id: number;
  equipementId: number;
  parametreId?: number;
  typeAction: TypeAction;
  valeurCible?: number;
  valeurActuelle?: number;
  statut: StatutAction;
  declencheurType?: 'AUTOMATIQUE' | 'MANUEL';
  dateExecution?: string;
  executedAt?: string;
  resultat?: string;
  createdAt: string;
}

export interface ActionRequest {
  equipementId: number;
  parametreId?: number;
  typeAction: TypeAction;
  valeurCible?: number;
  valeurActuelle?: number;
  resultat?: string;
}

// ==========================================
// Alert Types
// ==========================================

export type AlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | 'WARNING' | 'INFO';

export interface Alert {
  id?: number;
  eventId: string;
  mesureId: number;
  parametreId: number;
  parametreType: string;
  valeur: number;
  seuil?: number;
  seuilMin: number;
  seuilMax: number;
  dateMesure: string;
  severity: AlertSeverity;
  message: string;
  eventTimestamp: string;
  acknowledged?: boolean;
  createdAt?: string;
}

// ==========================================
// API Response Types
// ==========================================

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  numberOfElements: number;
  empty: boolean;
}

export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path?: string;
  errors?: Record<string, string>;
}

// ==========================================
// Dashboard Types
// ==========================================

export interface DashboardMetrics {
  temperature: {
    current: number;
    min: number;
    max: number;
    unit: string;
    status: 'normal' | 'warning' | 'critical';
  };
  humidity: {
    current: number;
    min: number;
    max: number;
    unit: string;
    status: 'normal' | 'warning' | 'critical';
  };
  luminosity: {
    current: number;
    min: number;
    max: number;
    unit: string;
    status: 'normal' | 'warning' | 'critical';
  };
  equipmentStatus: {
    active: number;
    inactive: number;
    maintenance: number;
    total: number;
  };
  alertCount: {
    critical: number;
    high: number;
    medium: number;
    low: number;
    total: number;
  };
}

export interface ChartDataPoint {
  timestamp: string;
  value: number;
  label?: string;
}

// ==========================================
// Real-time Event Types
// ==========================================

export interface SSEMeasurementEvent {
  eventId: string;
  mesureId: number;
  parametreId: number;
  parametreType: string;
  parametreName: string;
  valeur: number;
  unite: string;
  seuilMin: number;
  seuilMax: number;
  isAlert: boolean;
  dateMesure: string;
  eventTimestamp: string;
}

export interface SSEEquipmentActionEvent {
  eventId: string;
  equipementId: number;
  equipementName: string;
  equipementType: EquipementType;
  actionId: number;
  typeAction: TypeAction;
  statut: StatutAction;
  valeurCible?: number;
  valeurActuelle?: number;
  parametreId?: number;
  dateExecution: string;
  resultat?: string;
  isAutomatic: boolean;
}

// ==========================================
// Filter Types
// ==========================================

export interface DateRangeFilter {
  startDate: string;
  endDate: string;
}

export interface PaginationParams {
  [key: string]: unknown;
  page: number;
  size: number;
}

export interface MesureFilters extends PaginationParams {
  [key: string]: unknown;
  parametreId?: number;
  startDate?: string;
  endDate?: string;
  alertsOnly?: boolean;
}

export interface ActionFilters extends PaginationParams {
  [key: string]: unknown;
  equipementId?: number;
  statut?: StatutAction;
  typeAction?: TypeAction;
}

// ==========================================
// Auth Types (JWT-ready)
// ==========================================

export interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: User;
  expiresIn: number;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}
