'use client';

import { useEffect, useState, useCallback } from 'react';
import { AlertTriangle, AlertCircle, Info, X, Bell, BellOff } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useToast } from '@/hooks/use-toast';
import { useAlertStream, AlertEvent } from '@/hooks/use-sse';
import { ConnectionStatus } from './connection-status';
import { cn, formatDistanceToNow } from '@/lib/utils';

interface AlertNotificationsProps {
  greenhouseId?: string;
  maxAlerts?: number;
  showToasts?: boolean;
  className?: string;
}

const severityConfig = {
  CRITICAL: {
    icon: AlertTriangle,
    color: 'text-red-500',
    bgColor: 'bg-red-500/10',
    borderColor: 'border-red-500/30',
    badgeVariant: 'destructive' as const,
  },
  WARNING: {
    icon: AlertCircle,
    color: 'text-yellow-500',
    bgColor: 'bg-yellow-500/10',
    borderColor: 'border-yellow-500/30',
    badgeVariant: 'secondary' as const,
  },
  INFO: {
    icon: Info,
    color: 'text-blue-500',
    bgColor: 'bg-blue-500/10',
    borderColor: 'border-blue-500/30',
    badgeVariant: 'outline' as const,
  },
};

export function AlertNotifications({
  greenhouseId,
  maxAlerts = 50,
  showToasts = true,
  className,
}: AlertNotificationsProps) {
  const { toast } = useToast();
  const [alerts, setAlerts] = useState<AlertEvent[]>([]);
  const [dismissedIds, setDismissedIds] = useState<Set<string>>(new Set());
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);

  const {
    data: streamData,
    latestData,
    isConnected,
    isConnecting,
    error,
    retryCount,
    connect,
    clearData,
  } = useAlertStream({
    greenhouseId,
    bufferSize: maxAlerts,
  });

  // Show toast notification for new alerts
  useEffect(() => {
    if (latestData && showToasts && notificationsEnabled) {
      const config = severityConfig[latestData.severity] || severityConfig.INFO;
      
      toast({
        title: `${latestData.severity} Alert`,
        description: latestData.message,
        variant: latestData.severity === 'CRITICAL' ? 'destructive' : 'default',
        duration: latestData.severity === 'CRITICAL' ? 10000 : 5000,
      });
    }
  }, [latestData, showToasts, notificationsEnabled, toast]);

  // Update alerts list from stream
  useEffect(() => {
    setAlerts(streamData.filter((alert) => !dismissedIds.has(alert.id)));
  }, [streamData, dismissedIds]);

  const dismissAlert = useCallback((id: string) => {
    setDismissedIds((prev) => new Set([...prev, id]));
  }, []);

  const clearAllAlerts = useCallback(() => {
    const ids = alerts.map((a) => a.id);
    setDismissedIds((prev) => new Set([...prev, ...ids]));
    clearData();
  }, [alerts, clearData]);

  const toggleNotifications = useCallback(() => {
    setNotificationsEnabled((prev) => !prev);
  }, []);

  const visibleAlerts = alerts.slice(0, maxAlerts);
  const criticalCount = visibleAlerts.filter((a) => a.severity === 'CRITICAL').length;
  const warningCount = visibleAlerts.filter((a) => a.severity === 'WARNING').length;

  return (
    <Card className={cn('flex flex-col', className)}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <div className="flex items-center gap-2">
          <CardTitle className="text-base font-medium">Live Alerts</CardTitle>
          {criticalCount > 0 && (
            <Badge variant="destructive" className="animate-pulse">
              {criticalCount} Critical
            </Badge>
          )}
          {warningCount > 0 && (
            <Badge variant="secondary" className="bg-yellow-500/10 text-yellow-500">
              {warningCount} Warning
            </Badge>
          )}
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={toggleNotifications}
            className="h-7 w-7 p-0"
          >
            {notificationsEnabled ? (
              <Bell className="h-4 w-4" />
            ) : (
              <BellOff className="h-4 w-4 text-muted-foreground" />
            )}
          </Button>
          <ConnectionStatus
            isConnected={isConnected}
            isConnecting={isConnecting}
            error={error}
            retryCount={retryCount}
            onReconnect={connect}
            showLabel={false}
          />
        </div>
      </CardHeader>
      <CardContent className="flex-1 p-0">
        {visibleAlerts.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-48 text-center p-4">
            <Info className="h-8 w-8 text-muted-foreground mb-2" />
            <p className="text-sm text-muted-foreground">No active alerts</p>
            <p className="text-xs text-muted-foreground mt-1">
              {isConnected ? 'Monitoring for new alerts...' : 'Connecting...'}
            </p>
          </div>
        ) : (
          <>
            <ScrollArea className="h-[300px] px-4">
              <div className="space-y-2 py-2">
                {visibleAlerts.map((alert) => {
                  const config = severityConfig[alert.severity] || severityConfig.INFO;
                  const Icon = config.icon;

                  return (
                    <div
                      key={alert.id}
                      className={cn(
                        'relative rounded-lg border p-3 transition-all',
                        config.bgColor,
                        config.borderColor,
                        'hover:shadow-sm'
                      )}
                    >
                      <Button
                        variant="ghost"
                        size="sm"
                        className="absolute right-1 top-1 h-6 w-6 p-0 opacity-50 hover:opacity-100"
                        onClick={() => dismissAlert(alert.id)}
                      >
                        <X className="h-3 w-3" />
                      </Button>
                      <div className="flex items-start gap-3">
                        <Icon className={cn('h-5 w-5 mt-0.5', config.color)} />
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <Badge variant={config.badgeVariant} className="text-xs">
                              {alert.severity}
                            </Badge>
                            <span className="text-xs text-muted-foreground">
                              {alert.parameterType}
                            </span>
                          </div>
                          <p className="mt-1 text-sm font-medium line-clamp-2">
                            {alert.message}
                          </p>
                          <div className="mt-1 flex items-center gap-2 text-xs text-muted-foreground">
                            <span>{alert.greenhouseName || 'Unknown'}</span>
                            <span>•</span>
                            <span>{formatDistanceToNow(alert.timestamp)}</span>
                            {alert.currentValue !== null && alert.thresholdValue !== null && (
                              <>
                                <span>•</span>
                                <span>
                                  {alert.currentValue.toFixed(1)} / {alert.thresholdValue.toFixed(1)}
                                </span>
                              </>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </ScrollArea>
            {visibleAlerts.length > 0 && (
              <div className="border-t p-2">
                <Button
                  variant="ghost"
                  size="sm"
                  className="w-full text-xs"
                  onClick={clearAllAlerts}
                >
                  Clear All ({visibleAlerts.length})
                </Button>
              </div>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}
