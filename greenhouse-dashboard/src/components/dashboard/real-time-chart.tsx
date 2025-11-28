'use client';

import { useMemo, memo } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
} from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { useMeasurementStream, MeasurementEvent } from '@/hooks/use-sse';
import { ConnectionStatus } from './connection-status';
import { cn } from '@/lib/utils';

interface RealTimeChartProps {
  title: string;
  parameterType: string;
  greenhouseId?: string;
  color?: string;
  minThreshold?: number;
  maxThreshold?: number;
  unit?: string;
  maxDataPoints?: number;
  height?: number;
  className?: string;
}

const colorMap: Record<string, string> = {
  temperature: '#ef4444',
  humidity: '#3b82f6',
  light: '#eab308',
  co2: '#22c55e',
  soil_moisture: '#8b5cf6',
  default: '#6366f1',
};

function RealTimeChartInner({
  title,
  parameterType,
  greenhouseId,
  color,
  minThreshold,
  maxThreshold,
  unit = '',
  maxDataPoints = 50,
  height = 300,
  className,
}: RealTimeChartProps) {
  const {
    data,
    latestData,
    isConnected,
    isConnecting,
    error,
    retryCount,
    connect,
  } = useMeasurementStream({
    parameterType,
    greenhouseId,
    bufferSize: maxDataPoints,
  });

  const chartColor = color || colorMap[parameterType.toLowerCase()] || colorMap.default;

  // Format data for the chart
  const chartData = useMemo(() => {
    return data.map((item, index) => ({
      time: new Date(item.timestamp).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      }),
      value: item.value,
      index,
    }));
  }, [data]);

  // Calculate Y-axis domain
  const yDomain = useMemo(() => {
    if (chartData.length === 0) return [0, 100];
    const values = chartData.map((d) => d.value);
    const min = Math.min(...values);
    const max = Math.max(...values);
    const padding = (max - min) * 0.1 || 10;
    
    let domainMin = min - padding;
    let domainMax = max + padding;
    
    // Extend domain to include thresholds if set
    if (minThreshold !== undefined) domainMin = Math.min(domainMin, minThreshold - padding);
    if (maxThreshold !== undefined) domainMax = Math.max(domainMax, maxThreshold + padding);
    
    return [Math.floor(domainMin), Math.ceil(domainMax)];
  }, [chartData, minThreshold, maxThreshold]);

  // Determine if current value is in warning state
  const isWarning = useMemo(() => {
    if (!latestData) return false;
    if (minThreshold !== undefined && latestData.value < minThreshold) return true;
    if (maxThreshold !== undefined && latestData.value > maxThreshold) return true;
    return false;
  }, [latestData, minThreshold, maxThreshold]);

  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <div className="flex items-center gap-2">
          <CardTitle className="text-base font-medium">{title}</CardTitle>
          {latestData && (
            <Badge
              variant={isWarning ? 'destructive' : 'secondary'}
              className={cn(
                'text-lg font-semibold',
                !isWarning && 'bg-primary/10 text-primary'
              )}
            >
              {latestData.value.toFixed(1)} {unit}
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
      <CardContent className="pt-0">
        {chartData.length === 0 ? (
          <div className="flex items-center justify-center" style={{ height }}>
            {isConnecting ? (
              <div className="space-y-2 text-center">
                <Skeleton className="h-4 w-32 mx-auto" />
                <p className="text-sm text-muted-foreground">Waiting for data...</p>
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">No data available</p>
            )}
          </div>
        ) : (
          <ResponsiveContainer width="100%" height={height}>
            <LineChart
              data={chartData}
              margin={{ top: 5, right: 5, left: -10, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
              <XAxis
                dataKey="time"
                tick={{ fontSize: 10 }}
                tickLine={false}
                axisLine={false}
                interval="preserveStartEnd"
              />
              <YAxis
                domain={yDomain}
                tick={{ fontSize: 10 }}
                tickLine={false}
                axisLine={false}
                tickFormatter={(value) => `${value}${unit}`}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: 'hsl(var(--popover))',
                  border: '1px solid hsl(var(--border))',
                  borderRadius: '6px',
                }}
                labelStyle={{ color: 'hsl(var(--popover-foreground))' }}
                formatter={(value: number) => [`${value.toFixed(2)} ${unit}`, parameterType]}
              />
              {minThreshold !== undefined && (
                <ReferenceLine
                  y={minThreshold}
                  stroke="#f97316"
                  strokeDasharray="5 5"
                  label={{ value: 'Min', fontSize: 10, fill: '#f97316' }}
                />
              )}
              {maxThreshold !== undefined && (
                <ReferenceLine
                  y={maxThreshold}
                  stroke="#ef4444"
                  strokeDasharray="5 5"
                  label={{ value: 'Max', fontSize: 10, fill: '#ef4444' }}
                />
              )}
              <Line
                type="monotone"
                dataKey="value"
                stroke={chartColor}
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4, strokeWidth: 0 }}
                isAnimationActive={false}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </CardContent>
    </Card>
  );
}

export const RealTimeChart = memo(RealTimeChartInner);
