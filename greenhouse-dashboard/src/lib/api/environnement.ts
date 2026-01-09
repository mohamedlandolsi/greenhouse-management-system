import { api } from './api-client';
import {
  Parametre,
  ParametreRequest,
  Mesure,
  MesureRequest,
  PageResponse,
  MesureFilters,
} from '@/types';

const ENVIRONNEMENT_API = process.env.NEXT_PUBLIC_ENVIRONNEMENT_API || '/api/environnement';

// ==========================================
// Parametre API
// ==========================================

export const parametreApi = {
  // Get all parameters - Backend returns array, wrap in PageResponse format
  getAll: async (page = 0, size = 20): Promise<PageResponse<Parametre>> => {
    const data = await api.get<Parametre[]>(`${ENVIRONNEMENT_API}/parametres`);
    // Backend returns array directly, wrap it for compatibility
    return {
      content: data,
      totalElements: data.length,
      totalPages: 1,
      size: data.length,
      number: 0,
      first: true,
      last: true,
      empty: data.length === 0,
      numberOfElements: data.length,
      pageable: {
        pageNumber: 0,
        pageSize: data.length,
        sort: { sorted: false, unsorted: true, empty: true },
        offset: 0,
        paged: false,
        unpaged: true,
      },
      sort: { sorted: false, unsorted: true, empty: true },
    };
  },

  // Get parameter by ID
  getById: (id: number) => 
    api.get<Parametre>(`${ENVIRONNEMENT_API}/parametres/${id}`),

  // Get parameters by type
  getByType: (type: string) => 
    api.get<Parametre[]>(`${ENVIRONNEMENT_API}/parametres/type/${type}`),

  // Create new parameter
  create: (data: ParametreRequest) => 
    api.post<Parametre>(`${ENVIRONNEMENT_API}/parametres`, data),

  // Update parameter
  update: (id: number, data: ParametreRequest) => 
    api.put<Parametre>(`${ENVIRONNEMENT_API}/parametres/${id}`, data),

  // Delete parameter
  delete: (id: number) => 
    api.delete<void>(`${ENVIRONNEMENT_API}/parametres/${id}`),
};

// ==========================================
// Mesure API
// ==========================================

export const mesureApi = {
  // Get all measurements with pagination
  getAll: (params: MesureFilters) => 
    api.get<PageResponse<Mesure>>(`${ENVIRONNEMENT_API}/mesures`, params),

  // Get measurements by parameter ID
  getByParametreId: (parametreId: number, page = 0, size = 20) => 
    api.get<PageResponse<Mesure>>(`${ENVIRONNEMENT_API}/mesures/parametre/${parametreId}`, { page, size }),

  // Get measurements by date range
  getByDateRange: (startDate: string, endDate: string, parametreId?: number, page = 0, size = 100) =>
    api.get<PageResponse<Mesure>>(`${ENVIRONNEMENT_API}/mesures/range`, {
      startDate,
      endDate,
      parametreId,
      page,
      size,
    }),

  // Get recent measurements for a parameter
  getRecent: (parametreId: number, limit = 10) =>
    api.get<Mesure[]>(`${ENVIRONNEMENT_API}/mesures/recent/${parametreId}`, { limit }),

  // Get alerts
  getAlerts: (parametreId?: number, page = 0, size = 20) =>
    api.get<PageResponse<Mesure>>(`${ENVIRONNEMENT_API}/mesures/alerts`, { parametreId, page, size }),

  // Create new measurement
  create: (data: MesureRequest) => 
    api.post<Mesure>(`${ENVIRONNEMENT_API}/mesures`, data),
};

// ==========================================
// Statistics API (for dashboard)
// ==========================================

export const statsApi = {
  // Get current metrics for all parameters
  getCurrentMetrics: async () => {
    const parametres = await parametreApi.getAll(0, 100);
    const metrics: Record<string, { current: number; min: number; max: number; unit: string }> = {};
    
    for (const param of parametres.content) {
      const recent = await mesureApi.getRecent(param.id, 1);
      metrics[param.type.toLowerCase()] = {
        current: recent[0]?.valeur || 0,
        min: param.seuilMin,
        max: param.seuilMax,
        unit: param.unite,
      };
    }
    
    return metrics;
  },

  // Get chart data for last 24 hours
  getLast24HoursData: async (parametreId: number) => {
    const endDate = new Date().toISOString();
    const startDate = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();
    
    const response = await mesureApi.getByDateRange(startDate, endDate, parametreId, 0, 500);
    
    return response.content.map(m => ({
      timestamp: m.dateMesure,
      value: m.valeur,
    }));
  },
};
