'use client';

import { Thermometer, Droplets, Sun, TrendingUp, TrendingDown, Minus } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { cn, formatNumber, getParameterStatus, getStatusColor, getStatusBgColor } from '@/lib/utils';

interface MetricCardProps {
  title: string;
  value: number;
  unit: string;
  min: number;
  max: number;
  icon: 'temperature' | 'humidity' | 'light';
  trend?: 'up' | 'down' | 'stable';
  trendValue?: number;
  loading?: boolean;
}

const iconMap = {
  temperature: Thermometer,
  humidity: Droplets,
  light: Sun,
};

const colorMap = {
  temperature: {
    bg: 'bg-gradient-to-br from-red-500/10 to-orange-500/10',
    icon: 'text-red-500',
    border: 'border-red-500/20',
  },
  humidity: {
    bg: 'bg-gradient-to-br from-blue-500/10 to-cyan-500/10',
    icon: 'text-blue-500',
    border: 'border-blue-500/20',
  },
  light: {
    bg: 'bg-gradient-to-br from-yellow-500/10 to-amber-500/10',
    icon: 'text-yellow-500',
    border: 'border-yellow-500/20',
  },
};

export function MetricCard({
  title,
  value,
  unit,
  min,
  max,
  icon,
  trend,
  trendValue,
  loading = false,
}: MetricCardProps) {
  const Icon = iconMap[icon];
  const colors = colorMap[icon];
  const status = getParameterStatus(value, min, max);

  if (loading) {
    return (
      <Card className={cn('metric-card', colors.bg, colors.border)}>
        <CardContent className="p-0">
          <div className="flex items-start justify-between">
            <div className="space-y-3">
              <Skeleton className="h-4 w-24" />
              <Skeleton className="h-10 w-32" />
              <Skeleton className="h-3 w-28" />
            </div>
            <Skeleton className="h-12 w-12 rounded-xl" />
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={cn('metric-card border', colors.bg, colors.border)}>
      <CardContent className="p-0">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-sm font-medium text-gray-500 dark:text-gray-400">
              {title}
            </p>
            <div className="mt-2 flex items-baseline gap-2">
              <span
                className={cn(
                  'text-3xl font-bold',
                  status === 'normal'
                    ? 'text-gray-900 dark:text-white'
                    : getStatusColor(status)
                )}
              >
                {formatNumber(value)}
              </span>
              <span className="text-lg text-gray-500 dark:text-gray-400">
                {unit}
              </span>
            </div>
            <div className="mt-2 flex items-center gap-2">
              <span
                className={cn(
                  'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium',
                  getStatusBgColor(status),
                  getStatusColor(status)
                )}
              >
                {status === 'normal'
                  ? 'Normal'
                  : status === 'warning'
                  ? 'Attention'
                  : 'Critique'}
              </span>
              {trend && (
                <span className="flex items-center gap-1 text-xs text-gray-500">
                  {trend === 'up' && (
                    <TrendingUp className="h-3 w-3 text-green-500" />
                  )}
                  {trend === 'down' && (
                    <TrendingDown className="h-3 w-3 text-red-500" />
                  )}
                  {trend === 'stable' && (
                    <Minus className="h-3 w-3 text-gray-400" />
                  )}
                  {trendValue && `${trendValue > 0 ? '+' : ''}${trendValue}%`}
                </span>
              )}
            </div>
            <p className="mt-2 text-xs text-gray-400">
              Seuils: {min} - {max} {unit}
            </p>
          </div>
          <div
            className={cn(
              'flex h-12 w-12 items-center justify-center rounded-xl',
              colors.bg
            )}
          >
            <Icon className={cn('h-6 w-6', colors.icon)} />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
