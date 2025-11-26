import { api } from './api-client';
import {
  Equipement,
  EquipementRequest,
  Action,
  ActionRequest,
  PageResponse,
  ActionFilters,
  EquipementStatut,
} from '@/types';

const CONTROLE_API = process.env.NEXT_PUBLIC_CONTROLE_API || '/controle';

// ==========================================
// Equipement API
// ==========================================

export const equipementApi = {
  // Get all equipment
  getAll: (page = 0, size = 20) => 
    api.get<PageResponse<Equipement>>(`${CONTROLE_API}/equipements`, { page, size }),

  // Get equipment by ID
  getById: (id: number) => 
    api.get<Equipement>(`${CONTROLE_API}/equipements/${id}`),

  // Get equipment by type
  getByType: (type: string) => 
    api.get<Equipement[]>(`${CONTROLE_API}/equipements/type/${type}`),

  // Get equipment by status
  getByStatut: (statut: EquipementStatut) => 
    api.get<Equipement[]>(`${CONTROLE_API}/equipements/statut/${statut}`),

  // Get active equipment
  getActive: () => 
    api.get<Equipement[]>(`${CONTROLE_API}/equipements/statut/ACTIF`),

  // Create new equipment
  create: (data: EquipementRequest) => 
    api.post<Equipement>(`${CONTROLE_API}/equipements`, data),

  // Update equipment
  update: (id: number, data: EquipementRequest) => 
    api.put<Equipement>(`${CONTROLE_API}/equipements/${id}`, data),

  // Update equipment status
  updateStatut: (id: number, statut: EquipementStatut) => 
    api.patch<Equipement>(`${CONTROLE_API}/equipements/${id}/statut`, { statut }),

  // Delete equipment
  delete: (id: number) => 
    api.delete<void>(`${CONTROLE_API}/equipements/${id}`),
};

// ==========================================
// Action API
// ==========================================

export const actionApi = {
  // Get all actions with pagination
  getAll: (params: ActionFilters) => 
    api.get<PageResponse<Action>>(`${CONTROLE_API}/actions`, params),

  // Get action by ID
  getById: (id: number) => 
    api.get<Action>(`${CONTROLE_API}/actions/${id}`),

  // Get actions by equipment ID
  getByEquipementId: (equipementId: number, page = 0, size = 20) => 
    api.get<PageResponse<Action>>(`${CONTROLE_API}/actions/equipement/${equipementId}`, { page, size }),

  // Create new action (manual)
  create: (data: ActionRequest) => 
    api.post<Action>(`${CONTROLE_API}/actions`, data),

  // Cancel pending action
  cancel: (id: number) => 
    api.patch<Action>(`${CONTROLE_API}/actions/${id}/cancel`, {}),
};

// ==========================================
// Equipment Statistics
// ==========================================

export const equipementStatsApi = {
  // Get equipment status summary
  getStatusSummary: async () => {
    const response = await equipementApi.getAll(0, 1000);
    const equipment = response.content;
    
    return {
      active: equipment.filter(e => e.statut === 'ACTIF').length,
      inactive: equipment.filter(e => e.statut === 'INACTIF').length,
      maintenance: equipment.filter(e => e.statut === 'MAINTENANCE').length,
      broken: equipment.filter(e => e.statut === 'EN_PANNE').length,
      total: equipment.length,
    };
  },

  // Get recent actions for equipment
  getRecentActions: async (equipementId: number, limit = 5) => {
    const response = await actionApi.getByEquipementId(equipementId, 0, limit);
    return response.content;
  },
};
