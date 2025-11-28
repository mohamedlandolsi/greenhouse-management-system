'use client';

import { useMemo, memo, useEffect, useRef } from 'react';
import { Thermometer, Droplets, Sun, Activity } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useMeasurementStream, MeasurementEvent } from '@/hooks/use-sse';
import { ConnectionStatus } from './connection-status';
import { cn } from '@/lib/utils';

interface MeasurementTickerProps {
  greenhouseId?: string;
  maxItems?: number;
  className?: string;
}

const parameterIcons: Record<string, typeof Thermometer> = {
  temperature: Thermometer,
  humidity: Droplets,
  light: Sun,
  default: Activity,
};

const parameterColors: Record<string, string> = {
  temperature: 'text-red-500 bg-red-500/10',
  humidity: 'text-blue-500 bg-blue-500/10',
  light: 'text-yellow-500 bg-yellow-500/10',
  default: 'text-purple-500 bg-purple-500/10',
};

function MeasurementTickerInner({
  greenhouseId,
  maxItems = 10,
  className,
}: MeasurementTickerProps) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const {
    data,
    latestData,
    isConnected,
    isConnecting,
    error,
    retryCount,
    connect,
  } = useMeasurementStream({
    greenhouseId,
    bufferSize: maxItems,
  });

  // Auto-scroll to latest
  useEffect(() => {
    if (scrollRef.current && latestData) {
      scrollRef.current.scrollLeft = scrollRef.current.scrollWidth;
    }
  }, [latestData]);

  const visibleData = data.slice(-maxItems);

  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <div className="flex items-center gap-2">
          <CardTitle className="text-base font-medium">Live Measurements</CardTitle>
          {isConnected && (
            <Badge variant="outline" className="text-xs animate-pulse">
              <Activity className="h-3 w-3 mr-1" />
              Streaming
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
      <CardContent className="pb-3">
        {visibleData.length === 0 ? (
          <div className="flex items-center justify-center h-16 text-center">
            <p className="text-sm text-muted-foreground">
              {isConnecting ? 'Waiting for measurements...' : 'No measurements yet'}
            </p>
          </div>
        ) : (
          <div
            ref={scrollRef}
            className="flex gap-2 overflow-x-auto pb-2 scroll-smooth"
            style={{ scrollbarWidth: 'thin' }}
          >
            {visibleData.map((measurement, index) => (
              <MeasurementItem
                key={`${measurement.id}-${index}`}
                measurement={measurement}
                isLatest={index === visibleData.length - 1}
              />
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

interface MeasurementItemProps {
  measurement: MeasurementEvent;
  isLatest: boolean;
}

function MeasurementItem({ measurement, isLatest }: MeasurementItemProps) {
  const paramType = measurement.parameterType?.toLowerCase() || 'default';
  const Icon = parameterIcons[paramType] || parameterIcons.default;
  const colorClass = parameterColors[paramType] || parameterColors.default;
  const [textColor, bgColor] = colorClass.split(' ');

  return (
    <div
      className={cn(
        'flex-shrink-0 rounded-lg border p-2 min-w-[120px] transition-all',
        bgColor,
        isLatest && 'ring-2 ring-primary ring-offset-1'
      )}
    >
      <div className="flex items-center gap-2">
        <Icon className={cn('h-4 w-4', textColor)} />
        <span className="text-xs font-medium text-muted-foreground">
          {measurement.parameterType}
        </span>
      </div>
      <div className="mt-1">
        <span className={cn('text-lg font-bold', textColor)}>
          {measurement.value?.toFixed(1)}
        </span>
        <span className="text-xs text-muted-foreground ml-1">
          {measurement.unit}
        </span>
      </div>
      <p className="text-xs text-muted-foreground truncate mt-1">
        {measurement.capteurName || measurement.zoneName || 'Unknown'}
      </p>
      <p className="text-xs text-muted-foreground/60">
        {new Date(measurement.timestamp).toLocaleTimeString()}
      </p>
    </div>
  );
}

export const MeasurementTicker = memo(MeasurementTickerInner);
