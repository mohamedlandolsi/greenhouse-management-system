'use client';

import { useMemo, memo } from 'react';
import { 
  Fan, 
  Heater, 
  Droplets, 
  Lightbulb, 
  Settings,
  Power,
  PowerOff,
  AlertTriangle,
  Activity
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { useEquipmentStatusStream, EquipmentStatusEvent } from '@/hooks/use-sse';
import { ConnectionStatus } from './connection-status';
import { cn } from '@/lib/utils';

interface LiveEquipmentStatusProps {
  greenhouseId?: string;
  className?: string;
}

const equipmentIcons: Record<string, typeof Fan> = {
  VENTILATOR: Fan,
  HEATER: Heater,
  IRRIGATION: Droplets,
  LIGHTING: Lightbulb,
  DEFAULT: Settings,
};

const statusConfig: Record<string, { color: string; bgColor: string; icon: typeof Power }> = {
  ON: {
    color: 'text-green-500',
    bgColor: 'bg-green-500/10',
    icon: Power,
  },
  OFF: {
    color: 'text-gray-400',
    bgColor: 'bg-gray-500/10',
    icon: PowerOff,
  },
  ACTIVE: {
    color: 'text-green-500',
    bgColor: 'bg-green-500/10',
    icon: Activity,
  },
  INACTIVE: {
    color: 'text-gray-400',
    bgColor: 'bg-gray-500/10',
    icon: PowerOff,
  },
  ERROR: {
    color: 'text-red-500',
    bgColor: 'bg-red-500/10',
    icon: AlertTriangle,
  },
  MAINTENANCE: {
    color: 'text-yellow-500',
    bgColor: 'bg-yellow-500/10',
    icon: Settings,
  },
};

interface EquipmentState {
  [equipmentId: string]: EquipmentStatusEvent;
}

function LiveEquipmentStatusInner({ greenhouseId, className }: LiveEquipmentStatusProps) {
  const {
    data,
    isConnected,
    isConnecting,
    error,
    retryCount,
    connect,
  } = useEquipmentStatusStream({
    greenhouseId,
  });

  // Build equipment state map (latest status for each equipment)
  const equipmentState = useMemo(() => {
    const state: EquipmentState = {};
    data.forEach((event) => {
      if (event.equipmentId) {
        state[event.equipmentId] = event;
      }
    });
    return state;
  }, [data]);

  const equipmentList = Object.values(equipmentState);

  const activeCount = equipmentList.filter(
    (e) => e.status === 'ON' || e.status === 'ACTIVE'
  ).length;

  return (
    <Card className={cn('', className)}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <div className="flex items-center gap-2">
          <CardTitle className="text-base font-medium">Equipment Status</CardTitle>
          {equipmentList.length > 0 && (
            <Badge variant="secondary" className="bg-green-500/10 text-green-500">
              {activeCount} / {equipmentList.length} Active
            </Badge>
          )}
        </div>
        <ConnectionStatus
          isConnected={isConnected}
          isConnecting={isConnecting}
          error={error}
          retryCount={retryCount}
          onReconnect={connect}
          showLabel={false}
        />
      </CardHeader>
      <CardContent>
        {equipmentList.length === 0 ? (
          <div className="flex items-center justify-center h-32 text-center">
            {isConnecting ? (
              <div className="space-y-2">
                <Skeleton className="h-8 w-8 rounded-full mx-auto" />
                <p className="text-sm text-muted-foreground">Loading equipment...</p>
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">
                No equipment status updates
              </p>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
            {equipmentList.map((equipment) => (
              <EquipmentStatusBadge key={equipment.equipmentId} equipment={equipment} />
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

interface EquipmentStatusBadgeProps {
  equipment: EquipmentStatusEvent;
}

function EquipmentStatusBadge({ equipment }: EquipmentStatusBadgeProps) {
  const Icon = equipmentIcons[equipment.equipmentType?.toUpperCase()] || equipmentIcons.DEFAULT;
  const config = statusConfig[equipment.status?.toUpperCase()] || statusConfig.OFF;
  const StatusIcon = config.icon;

  const hasStatusChanged = equipment.previousStatus && 
    equipment.previousStatus !== equipment.status;

  return (
    <div
      className={cn(
        'relative rounded-lg border p-3 transition-all',
        config.bgColor,
        hasStatusChanged && 'ring-2 ring-primary ring-offset-2 animate-pulse'
      )}
    >
      <div className="flex items-center gap-2">
        <div className={cn('p-2 rounded-lg', config.bgColor)}>
          <Icon className={cn('h-5 w-5', config.color)} />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium truncate">
            {equipment.equipmentName || equipment.equipmentId}
          </p>
          <div className="flex items-center gap-1 mt-0.5">
            <StatusIcon className={cn('h-3 w-3', config.color)} />
            <span className={cn('text-xs', config.color)}>
              {equipment.status}
            </span>
          </div>
        </div>
      </div>
      {equipment.zoneName && (
        <p className="text-xs text-muted-foreground mt-2 truncate">
          {equipment.zoneName}
        </p>
      )}
      {equipment.triggeredBy && (
        <p className="text-xs text-muted-foreground mt-1">
          By: {equipment.triggeredBy}
        </p>
      )}
    </div>
  );
}

export const LiveEquipmentStatus = memo(LiveEquipmentStatusInner);
