'use client';

import { useEffect, useState } from 'react';
import { Wifi, WifiOff, RefreshCw, AlertCircle } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { cn } from '@/lib/utils';

interface ConnectionStatusProps {
  isConnected: boolean;
  isConnecting: boolean;
  error: string | null;
  retryCount: number;
  onReconnect: () => void;
  showLabel?: boolean;
  className?: string;
}

export function ConnectionStatus({
  isConnected,
  isConnecting,
  error,
  retryCount,
  onReconnect,
  showLabel = true,
  className,
}: ConnectionStatusProps) {
  const [pulse, setPulse] = useState(false);

  // Pulse animation when connected
  useEffect(() => {
    if (isConnected) {
      const interval = setInterval(() => {
        setPulse(true);
        setTimeout(() => setPulse(false), 500);
      }, 3000);
      return () => clearInterval(interval);
    }
  }, [isConnected]);

  const getStatusInfo = () => {
    if (isConnecting) {
      return {
        icon: RefreshCw,
        label: 'Connecting...',
        variant: 'secondary' as const,
        color: 'text-yellow-500',
        bgColor: 'bg-yellow-500/10',
      };
    }
    if (isConnected) {
      return {
        icon: Wifi,
        label: 'Live',
        variant: 'default' as const,
        color: 'text-green-500',
        bgColor: 'bg-green-500/10',
      };
    }
    return {
      icon: WifiOff,
      label: error ? 'Error' : 'Disconnected',
      variant: 'destructive' as const,
      color: 'text-red-500',
      bgColor: 'bg-red-500/10',
    };
  };

  const status = getStatusInfo();
  const StatusIcon = status.icon;

  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <div className={cn('flex items-center gap-2', className)}>
            <Badge
              variant={status.variant}
              className={cn(
                'flex items-center gap-1.5 px-2 py-1',
                status.bgColor,
                status.color
              )}
            >
              <StatusIcon
                className={cn(
                  'h-3 w-3',
                  isConnecting && 'animate-spin',
                  pulse && 'animate-pulse'
                )}
              />
              {showLabel && (
                <span className="text-xs font-medium">{status.label}</span>
              )}
            </Badge>
            {!isConnected && !isConnecting && (
              <Button
                variant="ghost"
                size="sm"
                onClick={onReconnect}
                className="h-6 px-2"
              >
                <RefreshCw className="h-3 w-3 mr-1" />
                Retry
              </Button>
            )}
          </div>
        </TooltipTrigger>
        <TooltipContent>
          <div className="text-sm">
            {isConnected && <p>Real-time data stream active</p>}
            {isConnecting && <p>Establishing connection...</p>}
            {!isConnected && !isConnecting && (
              <div>
                <p>{error || 'Connection lost'}</p>
                {retryCount > 0 && (
                  <p className="text-xs text-muted-foreground mt-1">
                    Retry attempts: {retryCount}
                  </p>
                )}
              </div>
            )}
          </div>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}
