'use client';

import { useState } from 'react';
import { 
  RealTimeChart, 
  AlertNotifications, 
  LiveEquipmentStatus, 
  MeasurementTicker,
  ConnectionStatus 
} from '@/components/dashboard';
import { useCombinedStream } from '@/hooks/use-sse';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Activity, Zap, RefreshCw, Settings } from 'lucide-react';
import { cn } from '@/lib/utils';

export default function RealTimePage() {
  const [selectedGreenhouse, setSelectedGreenhouse] = useState<string | undefined>();
  
  // Combined stream for connection status indicator
  const {
    isConnected,
    isConnecting,
    error,
    retryCount,
    connect,
  } = useCombinedStream({
    greenhouseId: selectedGreenhouse,
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Surveillance en Temps Réel</h1>
          <p className="text-muted-foreground">
            Flux de données en direct via Server-Sent Events (SSE)
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Select
            value={selectedGreenhouse}
            onValueChange={(value) => setSelectedGreenhouse(value === 'all' ? undefined : value)}
          >
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Toutes les serres" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Toutes les serres</SelectItem>
              <SelectItem value="greenhouse-1">Serre 1</SelectItem>
              <SelectItem value="greenhouse-2">Serre 2</SelectItem>
              <SelectItem value="greenhouse-3">Serre 3</SelectItem>
            </SelectContent>
          </Select>
          <ConnectionStatus
            isConnected={isConnected}
            isConnecting={isConnecting}
            error={error}
            retryCount={retryCount}
            onReconnect={connect}
            showLabel={true}
          />
        </div>
      </div>

      {/* Live Measurement Ticker */}
      <MeasurementTicker
        greenhouseId={selectedGreenhouse}
        maxItems={15}
      />

      {/* Real-time Charts */}
      <div className="grid gap-4 lg:grid-cols-2 xl:grid-cols-3">
        <RealTimeChart
          title="Température en Direct"
          parameterType="temperature"
          greenhouseId={selectedGreenhouse}
          unit="°C"
          minThreshold={15}
          maxThreshold={30}
          height={250}
        />
        <RealTimeChart
          title="Humidité en Direct"
          parameterType="humidity"
          greenhouseId={selectedGreenhouse}
          unit="%"
          minThreshold={40}
          maxThreshold={80}
          height={250}
        />
        <RealTimeChart
          title="Luminosité en Direct"
          parameterType="light"
          greenhouseId={selectedGreenhouse}
          unit="lux"
          minThreshold={1000}
          maxThreshold={50000}
          height={250}
        />
      </div>

      {/* Equipment Status and Alerts */}
      <div className="grid gap-4 lg:grid-cols-2">
        <LiveEquipmentStatus
          greenhouseId={selectedGreenhouse}
        />
        <AlertNotifications
          greenhouseId={selectedGreenhouse}
          maxAlerts={20}
          showToasts={true}
        />
      </div>

      {/* SSE Connection Info Card */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Settings className="h-4 w-4" />
            Informations de Connexion SSE
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <InfoItem
              label="État de connexion"
              value={isConnected ? 'Connecté' : isConnecting ? 'Connexion...' : 'Déconnecté'}
              status={isConnected ? 'success' : isConnecting ? 'warning' : 'error'}
            />
            <InfoItem
              label="Tentatives de reconnexion"
              value={String(retryCount)}
              status={retryCount > 0 ? 'warning' : 'success'}
            />
            <InfoItem
              label="Endpoint"
              value="/api/stream/*"
              status="neutral"
            />
            <InfoItem
              label="Intervalle Keep-Alive"
              value="15 secondes"
              status="neutral"
            />
          </div>
          {error && (
            <div className="mt-4 p-3 rounded-lg bg-red-500/10 border border-red-500/20">
              <p className="text-sm text-red-500">{error}</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

interface InfoItemProps {
  label: string;
  value: string;
  status: 'success' | 'warning' | 'error' | 'neutral';
}

function InfoItem({ label, value, status }: InfoItemProps) {
  const statusColors = {
    success: 'text-green-500',
    warning: 'text-yellow-500',
    error: 'text-red-500',
    neutral: 'text-muted-foreground',
  };

  return (
    <div className="space-y-1">
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className={cn('text-sm font-medium', statusColors[status])}>{value}</p>
    </div>
  );
}
