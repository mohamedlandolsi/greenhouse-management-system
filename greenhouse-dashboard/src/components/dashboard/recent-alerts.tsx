'use client';

import { AlertTriangle, AlertCircle, Info, XCircle, Clock } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { cn, formatRelativeTime, getSeverityColor, getSeverityLabel } from '@/lib/utils';
import { Alert } from '@/types';

interface RecentAlertsProps {
  alerts: Alert[];
  loading?: boolean;
  onViewAll?: () => void;
}

const severityIcons: Record<string, typeof XCircle> = {
  CRITICAL: XCircle,
  HIGH: AlertTriangle,
  WARNING: AlertTriangle,
  MEDIUM: AlertCircle,
  INFO: Info,
  LOW: Info,
};

export function RecentAlerts({ alerts, loading = false, onViewAll }: RecentAlertsProps) {
  if (loading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-5 w-32" />
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-16 w-full" />
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  // Sort by severity and date
  const sortedAlerts = [...alerts].sort((a, b) => {
    const severityOrder: Record<string, number> = { CRITICAL: 0, HIGH: 1, WARNING: 2, MEDIUM: 3, INFO: 4, LOW: 5 };
    const severityDiff =
      (severityOrder[a.severity] ?? 6) - (severityOrder[b.severity] ?? 6);
    if (severityDiff !== 0) return severityDiff;
    return new Date(b.eventTimestamp).getTime() - new Date(a.eventTimestamp).getTime();
  });

  const criticalCount = alerts.filter((a) => a.severity === 'CRITICAL').length;
  const highCount = alerts.filter((a) => a.severity === 'HIGH').length;

  return (
    <Card className={cn(criticalCount > 0 && 'border-red-500/50')}>
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CardTitle className="text-base font-medium">
              Alertes Récentes
            </CardTitle>
            {criticalCount > 0 && (
              <Badge variant="destructive" className="animate-pulse">
                {criticalCount} critique{criticalCount > 1 ? 's' : ''}
              </Badge>
            )}
          </div>
          <Badge variant="secondary">
            {alerts.length} alerte{alerts.length > 1 ? 's' : ''}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        {/* Quick Stats */}
        {alerts.length > 0 && (
          <div className="mb-4 flex gap-2">
            {criticalCount > 0 && (
              <div className="flex items-center gap-1 rounded-full bg-red-500/10 px-2 py-1">
                <XCircle className="h-3 w-3 text-red-500" />
                <span className="text-xs font-medium text-red-500">
                  {criticalCount}
                </span>
              </div>
            )}
            {highCount > 0 && (
              <div className="flex items-center gap-1 rounded-full bg-orange-500/10 px-2 py-1">
                <AlertTriangle className="h-3 w-3 text-orange-500" />
                <span className="text-xs font-medium text-orange-500">
                  {highCount}
                </span>
              </div>
            )}
          </div>
        )}

        {/* Alerts List */}
        <div className="space-y-2 max-h-72 overflow-y-auto scrollbar-hide">
          {sortedAlerts.slice(0, 5).map((alert) => {
            const SeverityIcon = severityIcons[alert.severity] || Info;
            const severityColors = getSeverityColor(alert.severity);

            return (
              <div
                key={alert.eventId}
                className={cn(
                  'rounded-lg border p-3 transition-all hover:shadow-sm',
                  severityColors
                )}
              >
                <div className="flex items-start gap-3">
                  <SeverityIcon
                    className={cn(
                      'h-5 w-5 mt-0.5 flex-shrink-0',
                      alert.severity === 'CRITICAL' && 'text-red-500',
                      alert.severity === 'HIGH' && 'text-orange-500',
                      alert.severity === 'MEDIUM' && 'text-yellow-500',
                      alert.severity === 'LOW' && 'text-blue-500'
                    )}
                  />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-2">
                      <Badge
                        variant="outline"
                        className={cn('text-[10px]', severityColors)}
                      >
                        {getSeverityLabel(alert.severity)}
                      </Badge>
                      <span className="flex items-center gap-1 text-xs text-gray-500">
                        <Clock className="h-3 w-3" />
                        {formatRelativeTime(alert.eventTimestamp)}
                      </span>
                    </div>
                    <p className="mt-1 text-sm font-medium text-gray-900 dark:text-white truncate">
                      {alert.parametreType}
                    </p>
                    <p className="text-xs text-gray-500 line-clamp-2">
                      {alert.message}
                    </p>
                  </div>
                </div>
              </div>
            );
          })}

          {alerts.length === 0 && (
            <div className="flex flex-col items-center justify-center py-8 text-gray-500">
              <AlertCircle className="h-8 w-8 mb-2 text-green-500" />
              <p className="text-green-600 font-medium">Aucune alerte active</p>
              <p className="text-xs mt-1">Tous les paramètres sont normaux</p>
            </div>
          )}
        </div>

        {/* View All Button */}
        {alerts.length > 5 && (
          <Button
            variant="ghost"
            className="w-full mt-4"
            onClick={onViewAll}
          >
            Voir toutes les alertes ({alerts.length})
          </Button>
        )}
      </CardContent>
    </Card>
  );
}
