import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { format, formatDistanceToNow, parseISO } from 'date-fns';
import { fr } from 'date-fns/locale';

// ==========================================
// Class Name Utilities
// ==========================================

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// ==========================================
// Date Utilities
// ==========================================

export function formatDate(date: string | Date, formatStr = 'dd/MM/yyyy HH:mm') {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return format(d, formatStr, { locale: fr });
}

export function formatDateShort(date: string | Date) {
  return formatDate(date, 'dd/MM HH:mm');
}

export function formatRelativeTime(date: string | Date) {
  const d = typeof date === 'string' ? parseISO(date) : date;
  return formatDistanceToNow(d, { addSuffix: true, locale: fr });
}

export function formatChartTime(date: string | Date) {
  return formatDate(date, 'HH:mm');
}

// ==========================================
// Number Utilities
// ==========================================

export function formatNumber(value: number, decimals = 1) {
  return value.toFixed(decimals);
}

export function formatPercentage(value: number, total: number) {
  if (total === 0) return '0%';
  return `${((value / total) * 100).toFixed(1)}%`;
}

// ==========================================
// Status Utilities
// ==========================================

export function getParameterStatus(
  value: number,
  min: number,
  max: number
): 'normal' | 'warning' | 'critical' {
  if (value < min || value > max) {
    const deviation = value < min ? (min - value) / min : (value - max) / max;
    return deviation > 0.25 ? 'critical' : 'warning';
  }
  return 'normal';
}

export function getStatusColor(status: 'normal' | 'warning' | 'critical') {
  switch (status) {
    case 'normal':
      return 'text-green-500';
    case 'warning':
      return 'text-yellow-500';
    case 'critical':
      return 'text-red-500';
    default:
      return 'text-gray-500';
  }
}

export function getStatusBgColor(status: 'normal' | 'warning' | 'critical') {
  switch (status) {
    case 'normal':
      return 'bg-green-500/10';
    case 'warning':
      return 'bg-yellow-500/10';
    case 'critical':
      return 'bg-red-500/10';
    default:
      return 'bg-gray-500/10';
  }
}

// ==========================================
// Equipment Status Utilities
// ==========================================

export function getEquipmentStatusColor(statut: string) {
  switch (statut) {
    case 'ACTIF':
      return 'text-green-500 bg-green-500/10';
    case 'INACTIF':
      return 'text-gray-500 bg-gray-500/10';
    case 'EN_PANNE':
      return 'text-red-500 bg-red-500/10';
    case 'MAINTENANCE':
      return 'text-yellow-500 bg-yellow-500/10';
    default:
      return 'text-gray-500 bg-gray-500/10';
  }
}

export function getEquipmentStatusLabel(statut: string) {
  switch (statut) {
    case 'ACTIF':
      return 'Actif';
    case 'INACTIF':
      return 'Inactif';
    case 'EN_PANNE':
      return 'En panne';
    case 'MAINTENANCE':
      return 'Maintenance';
    default:
      return statut;
  }
}

// ==========================================
// Action Status Utilities
// ==========================================

export function getActionStatusColor(statut: string) {
  switch (statut) {
    case 'EXECUTEE':
    case 'TERMINEE':
      return 'text-green-500 bg-green-500/10';
    case 'EN_ATTENTE':
      return 'text-yellow-500 bg-yellow-500/10';
    case 'EN_COURS':
      return 'text-blue-500 bg-blue-500/10';
    case 'ECHOUEE':
      return 'text-red-500 bg-red-500/10';
    case 'ANNULEE':
      return 'text-gray-500 bg-gray-500/10';
    default:
      return 'text-gray-500 bg-gray-500/10';
  }
}

export function getActionStatusLabel(statut: string) {
  switch (statut) {
    case 'EXECUTEE':
      return 'Exécutée';
    case 'TERMINEE':
      return 'Terminée';
    case 'EN_ATTENTE':
      return 'En attente';
    case 'EN_COURS':
      return 'En cours';
    case 'ECHOUEE':
      return 'Échouée';
    case 'ANNULEE':
      return 'Annulée';
    default:
      return statut;
  }
}

// ==========================================
// Alert Severity Utilities
// ==========================================

export function getSeverityColor(severity: string) {
  switch (severity) {
    case 'CRITICAL':
      return 'text-red-600 bg-red-500/10 border-red-500';
    case 'HIGH':
      return 'text-orange-500 bg-orange-500/10 border-orange-500';
    case 'MEDIUM':
      return 'text-yellow-500 bg-yellow-500/10 border-yellow-500';
    case 'LOW':
      return 'text-blue-500 bg-blue-500/10 border-blue-500';
    default:
      return 'text-gray-500 bg-gray-500/10 border-gray-500';
  }
}

export function getSeverityLabel(severity: string) {
  switch (severity) {
    case 'CRITICAL':
      return 'Critique';
    case 'HIGH':
      return 'Haute';
    case 'MEDIUM':
      return 'Moyenne';
    case 'LOW':
      return 'Basse';
    default:
      return severity;
  }
}

// ==========================================
// Parameter Type Utilities
// ==========================================

export function getParameterTypeLabel(type: string) {
  switch (type) {
    case 'TEMPERATURE':
      return 'Température';
    case 'HUMIDITE':
      return 'Humidité';
    case 'LUMINOSITE':
      return 'Luminosité';
    default:
      return type;
  }
}

// Alias for getParameterTypeLabel
export const getParameterLabel = getParameterTypeLabel;

export function getParameterTypeIcon(type: string) {
  switch (type) {
    case 'TEMPERATURE':
      return '🌡️';
    case 'HUMIDITE':
      return '💧';
    case 'LUMINOSITE':
      return '☀️';
    default:
      return '📊';
  }
}

export function getParameterTypeColor(type: string) {
  switch (type) {
    case 'TEMPERATURE':
      return '#ef4444'; // red
    case 'HUMIDITE':
      return '#3b82f6'; // blue
    case 'LUMINOSITE':
      return '#f59e0b'; // amber
    default:
      return '#6b7280'; // gray
  }
}

// ==========================================
// Equipment Type Utilities
// ==========================================

export function getEquipmentTypeLabel(type: string) {
  switch (type) {
    case 'VENTILATEUR':
      return 'Ventilateur';
    case 'CHAUFFAGE':
      return 'Chauffage';
    case 'ECLAIRAGE':
      return 'Éclairage';
    case 'ARROSAGE':
      return 'Arrosage';
    case 'HUMIDIFICATEUR':
      return 'Humidificateur';
    default:
      return type;
  }
}

export function getEquipmentTypeIcon(type: string) {
  switch (type) {
    case 'VENTILATEUR':
      return '🌀';
    case 'CHAUFFAGE':
      return '🔥';
    case 'ECLAIRAGE':
      return '💡';
    case 'ARROSAGE':
      return '🚿';
    case 'HUMIDIFICATEUR':
      return '💨';
    default:
      return '⚙️';
  }
}

// ==========================================
// CSV Export Utility
// ==========================================

export function exportToCSV<T extends Record<string, unknown>>(
  data: T[],
  filename: string,
  headers?: Record<keyof T, string>
) {
  if (data.length === 0) return;

  const keys = Object.keys(data[0]) as (keyof T)[];
  const headerRow = headers
    ? keys.map((k) => headers[k] || String(k)).join(',')
    : keys.join(',');

  const rows = data.map((item) =>
    keys
      .map((key) => {
        const value = item[key];
        if (value === null || value === undefined) return '';
        if (typeof value === 'string' && value.includes(',')) {
          return `"${value}"`;
        }
        return String(value);
      })
      .join(',')
  );

  const csv = [headerRow, ...rows].join('\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `${filename}_${format(new Date(), 'yyyy-MM-dd_HH-mm')}.csv`;
  link.click();
}

// ==========================================
// Debounce Utility
// ==========================================

export function debounce<T extends (...args: unknown[]) => unknown>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout;
  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
}

// ==========================================
// Local Storage Utility
// ==========================================

export function getFromStorage<T>(key: string, defaultValue: T): T {
  if (typeof window === 'undefined') return defaultValue;
  try {
    const item = localStorage.getItem(key);
    return item ? JSON.parse(item) : defaultValue;
  } catch {
    return defaultValue;
  }
}

export function setToStorage<T>(key: string, value: T): void {
  if (typeof window === 'undefined') return;
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    console.error('Failed to save to localStorage');
  }
}
