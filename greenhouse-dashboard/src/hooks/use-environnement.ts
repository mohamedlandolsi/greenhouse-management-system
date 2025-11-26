import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { parametreApi, mesureApi, statsApi } from '@/lib/api';
import { ParametreRequest, MesureRequest, MesureFilters } from '@/types';

// ==========================================
// Query Keys
// ==========================================

export const environnementKeys = {
  all: ['environnement'] as const,
  parametres: () => [...environnementKeys.all, 'parametres'] as const,
  parametre: (id: number) => [...environnementKeys.parametres(), id] as const,
  parametresByType: (type: string) => [...environnementKeys.parametres(), 'type', type] as const,
  mesures: () => [...environnementKeys.all, 'mesures'] as const,
  mesure: (filters: MesureFilters) => [...environnementKeys.mesures(), filters] as const,
  mesuresByParametre: (parametreId: number) => [...environnementKeys.mesures(), 'parametre', parametreId] as const,
  recentMesures: (parametreId: number) => [...environnementKeys.mesures(), 'recent', parametreId] as const,
  alerts: () => [...environnementKeys.mesures(), 'alerts'] as const,
  metrics: () => [...environnementKeys.all, 'metrics'] as const,
  chartData: (parametreId: number) => [...environnementKeys.all, 'chart', parametreId] as const,
};

// ==========================================
// Parametre Hooks
// ==========================================

export function useParametres(page = 0, size = 20) {
  return useQuery({
    queryKey: environnementKeys.parametres(),
    queryFn: () => parametreApi.getAll(page, size),
    staleTime: 30000, // 30 seconds
  });
}

export function useParametre(id: number) {
  return useQuery({
    queryKey: environnementKeys.parametre(id),
    queryFn: () => parametreApi.getById(id),
    enabled: !!id,
  });
}

export function useParametresByType(type: string) {
  return useQuery({
    queryKey: environnementKeys.parametresByType(type),
    queryFn: () => parametreApi.getByType(type),
    enabled: !!type,
  });
}

export function useCreateParametre() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data: ParametreRequest) => parametreApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: environnementKeys.parametres() });
    },
  });
}

export function useUpdateParametre() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: ParametreRequest }) => 
      parametreApi.update(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: environnementKeys.parametre(id) });
      queryClient.invalidateQueries({ queryKey: environnementKeys.parametres() });
    },
  });
}

export function useDeleteParametre() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id: number) => parametreApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: environnementKeys.parametres() });
    },
  });
}

// ==========================================
// Mesure Hooks
// ==========================================

export function useMesures(filters: MesureFilters) {
  return useQuery({
    queryKey: environnementKeys.mesure(filters),
    queryFn: () => mesureApi.getAll(filters),
    staleTime: 10000, // 10 seconds
  });
}

export function useMesuresByParametre(parametreId: number, page = 0, size = 20) {
  return useQuery({
    queryKey: environnementKeys.mesuresByParametre(parametreId),
    queryFn: () => mesureApi.getByParametreId(parametreId, page, size),
    enabled: !!parametreId,
    staleTime: 10000,
  });
}

export function useRecentMesures(parametreId: number, limit = 10) {
  return useQuery({
    queryKey: environnementKeys.recentMesures(parametreId),
    queryFn: () => mesureApi.getRecent(parametreId, limit),
    enabled: !!parametreId,
    staleTime: 5000, // 5 seconds - more frequent refresh for recent data
    refetchInterval: 10000, // Auto-refresh every 10 seconds
  });
}

export function useMesuresByDateRange(
  startDate: string,
  endDate: string,
  parametreId?: number,
  page = 0,
  size = 100
) {
  return useQuery({
    queryKey: [...environnementKeys.mesures(), 'range', startDate, endDate, parametreId],
    queryFn: () => mesureApi.getByDateRange(startDate, endDate, parametreId, page, size),
    enabled: !!startDate && !!endDate,
    staleTime: 30000,
  });
}

export function useAlerts(parametreId?: number, page = 0, size = 20) {
  return useQuery({
    queryKey: environnementKeys.alerts(),
    queryFn: () => mesureApi.getAlerts(parametreId, page, size),
    staleTime: 5000,
    refetchInterval: 5000, // Auto-refresh every 5 seconds for alerts
  });
}

export function useCreateMesure() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data: MesureRequest) => mesureApi.create(data),
    onSuccess: (mesure) => {
      queryClient.invalidateQueries({ queryKey: environnementKeys.mesures() });
      queryClient.invalidateQueries({ 
        queryKey: environnementKeys.recentMesures(mesure.parametreId) 
      });
      if (mesure.alerte) {
        queryClient.invalidateQueries({ queryKey: environnementKeys.alerts() });
      }
    },
  });
}

// ==========================================
// Dashboard/Stats Hooks
// ==========================================

export function useCurrentMetrics() {
  return useQuery({
    queryKey: environnementKeys.metrics(),
    queryFn: () => statsApi.getCurrentMetrics(),
    staleTime: 10000,
    refetchInterval: 10000,
  });
}

export function useChartData(parametreId: number) {
  return useQuery({
    queryKey: environnementKeys.chartData(parametreId),
    queryFn: () => statsApi.getLast24HoursData(parametreId),
    enabled: !!parametreId,
    staleTime: 60000, // 1 minute
    refetchInterval: 60000,
  });
}
