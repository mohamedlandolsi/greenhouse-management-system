'use client';

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
import { Skeleton } from '@/components/ui/skeleton';
import { formatChartTime, getParameterTypeColor } from '@/lib/utils';

interface ChartDataPoint {
  timestamp: string;
  value: number;
}

interface ParameterChartProps {
  title: string;
  data: ChartDataPoint[];
  parametreType: string;
  unit: string;
  seuilMin?: number;
  seuilMax?: number;
  loading?: boolean;
}

export function ParameterChart({
  title,
  data,
  parametreType,
  unit,
  seuilMin,
  seuilMax,
  loading = false,
}: ParameterChartProps) {
  const color = getParameterTypeColor(parametreType);

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-5 w-40" />
        </CardHeader>
        <CardContent>
          <Skeleton className="h-64 w-full" />
        </CardContent>
      </Card>
    );
  }

  const formattedData = data.map((point) => ({
    ...point,
    time: formatChartTime(point.timestamp),
  }));

  // Calculate min/max for Y axis with some padding
  const values = data.map((d) => d.value);
  const minValue = Math.min(...values, seuilMin || Infinity);
  const maxValue = Math.max(...values, seuilMax || -Infinity);
  const padding = (maxValue - minValue) * 0.1;

  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-base font-medium">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart
              data={formattedData}
              margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
            >
              <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-gray-700" />
              <XAxis
                dataKey="time"
                tick={{ fontSize: 12 }}
                tickLine={false}
                axisLine={false}
                className="text-gray-500"
              />
              <YAxis
                domain={[minValue - padding, maxValue + padding]}
                tick={{ fontSize: 12 }}
                tickLine={false}
                axisLine={false}
                tickFormatter={(value) => `${value}${unit}`}
                width={60}
                className="text-gray-500"
              />
              <Tooltip
                content={({ active, payload }) => {
                  if (active && payload && payload.length) {
                    const data = payload[0].payload;
                    return (
                      <div className="rounded-lg border bg-white p-3 shadow-lg dark:bg-gray-800 dark:border-gray-700">
                        <p className="text-xs text-gray-500">{data.time}</p>
                        <p className="text-lg font-semibold" style={{ color }}>
                          {data.value} {unit}
                        </p>
                      </div>
                    );
                  }
                  return null;
                }}
              />
              {seuilMin !== undefined && (
                <ReferenceLine
                  y={seuilMin}
                  stroke="#f59e0b"
                  strokeDasharray="5 5"
                  label={{
                    value: `Min: ${seuilMin}`,
                    position: 'left',
                    fill: '#f59e0b',
                    fontSize: 10,
                  }}
                />
              )}
              {seuilMax !== undefined && (
                <ReferenceLine
                  y={seuilMax}
                  stroke="#ef4444"
                  strokeDasharray="5 5"
                  label={{
                    value: `Max: ${seuilMax}`,
                    position: 'left',
                    fill: '#ef4444',
                    fontSize: 10,
                  }}
                />
              )}
              <Line
                type="monotone"
                dataKey="value"
                stroke={color}
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4, fill: color }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
