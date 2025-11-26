'use client';

import { Settings2, Power, AlertCircle, CheckCircle2, Wrench } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { cn, getEquipmentTypeIcon, getEquipmentStatusLabel } from '@/lib/utils';
import { Equipement } from '@/types';

interface EquipmentStatusProps {
  equipment: Equipement[];
  loading?: boolean;
}

const statusConfig = {
  ACTIF: {
    icon: Power,
    color: 'text-green-500',
    bgColor: 'bg-green-500/10',
  },
  INACTIF: {
    icon: Power,
    color: 'text-gray-400',
    bgColor: 'bg-gray-500/10',
  },
  EN_PANNE: {
    icon: AlertCircle,
    color: 'text-red-500',
    bgColor: 'bg-red-500/10',
  },
  MAINTENANCE: {
    icon: Wrench,
    color: 'text-yellow-500',
    bgColor: 'bg-yellow-500/10',
  },
};

export function EquipmentStatus({ equipment, loading = false }: EquipmentStatusProps) {
  if (loading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-5 w-40" />
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {[1, 2, 3, 4].map((i) => (
              <Skeleton key={i} className="h-12 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  // Calculate stats
  const stats = {
    active: equipment.filter((e) => e.statut === 'ACTIF').length,
    inactive: equipment.filter((e) => e.statut === 'INACTIF').length,
    maintenance: equipment.filter((e) => e.statut === 'MAINTENANCE').length,
    broken: equipment.filter((e) => e.statut === 'EN_PANNE').length,
    total: equipment.length,
  };

  return (
    <Card>
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <CardTitle className="text-base font-medium">
            État des Équipements
          </CardTitle>
          <Badge variant="secondary">
            {stats.active}/{stats.total} actifs
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        {/* Stats Summary */}
        <div className="mb-4 grid grid-cols-4 gap-2">
          <div className="rounded-lg bg-green-500/10 p-2 text-center">
            <p className="text-lg font-bold text-green-600">{stats.active}</p>
            <p className="text-xs text-gray-500">Actifs</p>
          </div>
          <div className="rounded-lg bg-gray-500/10 p-2 text-center">
            <p className="text-lg font-bold text-gray-600">{stats.inactive}</p>
            <p className="text-xs text-gray-500">Inactifs</p>
          </div>
          <div className="rounded-lg bg-yellow-500/10 p-2 text-center">
            <p className="text-lg font-bold text-yellow-600">{stats.maintenance}</p>
            <p className="text-xs text-gray-500">Maintenance</p>
          </div>
          <div className="rounded-lg bg-red-500/10 p-2 text-center">
            <p className="text-lg font-bold text-red-600">{stats.broken}</p>
            <p className="text-xs text-gray-500">En panne</p>
          </div>
        </div>

        {/* Equipment List */}
        <div className="space-y-2 max-h-64 overflow-y-auto scrollbar-hide">
          {equipment.map((equip) => {
            const config = statusConfig[equip.statut] || statusConfig.INACTIF;
            const StatusIcon = config.icon;

            return (
              <div
                key={equip.id}
                className={cn(
                  'flex items-center justify-between rounded-lg border p-3 transition-colors hover:bg-gray-50 dark:hover:bg-gray-800',
                  equip.statut === 'EN_PANNE' && 'border-red-500/30 bg-red-500/5'
                )}
              >
                <div className="flex items-center gap-3">
                  <span className="text-xl">{getEquipmentTypeIcon(equip.type)}</span>
                  <div>
                    <p className="font-medium text-gray-900 dark:text-white">
                      {equip.nom}
                    </p>
                    <p className="text-xs text-gray-500">
                      {equip.localisation || 'Zone non définie'}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <div
                    className={cn(
                      'flex items-center gap-1 rounded-full px-2 py-1',
                      config.bgColor
                    )}
                  >
                    <StatusIcon className={cn('h-3 w-3', config.color)} />
                    <span className={cn('text-xs font-medium', config.color)}>
                      {getEquipmentStatusLabel(equip.statut)}
                    </span>
                  </div>
                </div>
              </div>
            );
          })}

          {equipment.length === 0 && (
            <div className="flex flex-col items-center justify-center py-8 text-gray-500">
              <Settings2 className="h-8 w-8 mb-2" />
              <p>Aucun équipement configuré</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
