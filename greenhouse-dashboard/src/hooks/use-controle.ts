import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { equipementApi, actionApi, equipementStatsApi } from '@/lib/api';
import { EquipementRequest, ActionRequest, EquipementStatut, ActionFilters } from '@/types';

// ==========================================
// Query Keys
// ==========================================

export const controleKeys = {
  all: ['controle'] as const,
  equipements: () => [...controleKeys.all, 'equipements'] as const,
  equipement: (id: number) => [...controleKeys.equipements(), id] as const,
  equipementsByType: (type: string) => [...controleKeys.equipements(), 'type', type] as const,
  equipementsByStatut: (statut: string) => [...controleKeys.equipements(), 'statut', statut] as const,
  equipementStats: () => [...controleKeys.equipements(), 'stats'] as const,
  actions: () => [...controleKeys.all, 'actions'] as const,
  action: (id: number) => [...controleKeys.actions(), id] as const,
  actionsByEquipement: (equipementId: number) => [...controleKeys.actions(), 'equipement', equipementId] as const,
};

// ==========================================
// Equipement Hooks
// ==========================================

export function useEquipements(page = 0, size = 20) {
  return useQuery({
    queryKey: controleKeys.equipements(),
    queryFn: () => equipementApi.getAll(page, size),
    staleTime: 30000,
  });
}

export function useEquipement(id: number) {
  return useQuery({
    queryKey: controleKeys.equipement(id),
    queryFn: () => equipementApi.getById(id),
    enabled: !!id,
  });
}

export function useEquipementsByType(type: string) {
  return useQuery({
    queryKey: controleKeys.equipementsByType(type),
    queryFn: () => equipementApi.getByType(type),
    enabled: !!type,
  });
}

export function useEquipementsByStatut(statut: EquipementStatut) {
  return useQuery({
    queryKey: controleKeys.equipementsByStatut(statut),
    queryFn: () => equipementApi.getByStatut(statut),
    enabled: !!statut,
  });
}

export function useActiveEquipements() {
  return useQuery({
    queryKey: controleKeys.equipementsByStatut('ACTIF'),
    queryFn: () => equipementApi.getActive(),
    staleTime: 10000,
    refetchInterval: 10000,
  });
}

export function useEquipementStats() {
  return useQuery({
    queryKey: controleKeys.equipementStats(),
    queryFn: () => equipementStatsApi.getStatusSummary(),
    staleTime: 30000,
    refetchInterval: 30000,
  });
}

export function useCreateEquipement() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data: EquipementRequest) => equipementApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: controleKeys.equipements() });
      queryClient.invalidateQueries({ queryKey: controleKeys.equipementStats() });
    },
  });
}

export function useUpdateEquipement() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: EquipementRequest }) => 
      equipementApi.update(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: controleKeys.equipement(id) });
      queryClient.invalidateQueries({ queryKey: controleKeys.equipements() });
      queryClient.invalidateQueries({ queryKey: controleKeys.equipementStats() });
    },
  });
}

export function useUpdateEquipementStatut() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, statut }: { id: number; statut: EquipementStatut }) => 
      equipementApi.updateStatut(id, statut),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: controleKeys.equipement(id) });
      queryClient.invalidateQueries({ queryKey: controleKeys.equipements() });
      queryClient.invalidateQueries({ queryKey: controleKeys.equipementStats() });
    },
  });
}

export function useDeleteEquipement() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id: number) => equipementApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: controleKeys.equipements() });
      queryClient.invalidateQueries({ queryKey: controleKeys.equipementStats() });
    },
  });
}

// ==========================================
// Action Hooks
// ==========================================

export function useActions(filters: ActionFilters) {
  return useQuery({
    queryKey: [...controleKeys.actions(), filters],
    queryFn: () => actionApi.getAll(filters),
    staleTime: 10000,
    refetchInterval: 10000, // Auto-refresh for pending actions
  });
}

export function useAction(id: number) {
  return useQuery({
    queryKey: controleKeys.action(id),
    queryFn: () => actionApi.getById(id),
    enabled: !!id,
  });
}

export function useActionsByEquipement(equipementId: number, page = 0, size = 20) {
  return useQuery({
    queryKey: controleKeys.actionsByEquipement(equipementId),
    queryFn: () => actionApi.getByEquipementId(equipementId, page, size),
    enabled: !!equipementId,
    staleTime: 10000,
  });
}

export function useRecentEquipementActions(equipementId: number, limit = 5) {
  return useQuery({
    queryKey: [...controleKeys.actionsByEquipement(equipementId), 'recent'],
    queryFn: () => equipementStatsApi.getRecentActions(equipementId, limit),
    enabled: !!equipementId,
    staleTime: 10000,
  });
}

export function useCreateAction() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data: ActionRequest) => actionApi.create(data),
    onSuccess: (action) => {
      queryClient.invalidateQueries({ queryKey: controleKeys.actions() });
      queryClient.invalidateQueries({ 
        queryKey: controleKeys.actionsByEquipement(action.equipementId) 
      });
      queryClient.invalidateQueries({ 
        queryKey: controleKeys.equipement(action.equipementId) 
      });
    },
  });
}

export function useCancelAction() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id: number) => actionApi.cancel(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: controleKeys.actions() });
    },
  });
}
