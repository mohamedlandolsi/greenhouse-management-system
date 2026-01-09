'use client';

import { useRouter } from 'next/navigation';
import { MetricCard, ParameterChart, EquipmentStatus, RecentAlerts } from '@/components/dashboard';
import { useParametres, useAlerts, useRecentMesures } from '@/hooks/use-environnement';
import { useEquipements } from '@/hooks/use-controle';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Activity, Clock, RefreshCw } from 'lucide-react';
import { useState, useEffect } from 'react';
import { formatDate } from '@/lib/utils';

export default function DashboardPage() {
  const router = useRouter();
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null);
  const [isClient, setIsClient] = useState(false);

  // Fix hydration mismatch - only set date after client mount
  useEffect(() => {
    setIsClient(true);
    setLastUpdate(new Date());
  }, []);

  // Fetch data
  const { data: parametresData, isLoading: loadingParametres } = useParametres(0, 100);
  const { data: equipementsData, isLoading: loadingEquipements } = useEquipements(0, 100);
  const { data: alertsData, isLoading: loadingAlerts } = useAlerts(undefined, 0, 20);

  // Get parameters by type
  const parameters = parametresData?.content || [];
  const tempParam = parameters.find((p) => p.type === 'TEMPERATURE');
  const humidityParam = parameters.find((p) => p.type === 'HUMIDITE');
  const lightParam = parameters.find((p) => p.type === 'LUMINOSITE');

  // Fetch recent measurements for each parameter
  const { data: tempMesures } = useRecentMesures(tempParam?.id || 0, 50);
  const { data: humidityMesures } = useRecentMesures(humidityParam?.id || 0, 50);
  const { data: lightMesures } = useRecentMesures(lightParam?.id || 0, 50);

  const equipment = equipementsData?.content || [];
  const alerts = alertsData?.content || [];

  // Update last update time periodically
  useEffect(() => {
    if (!isClient) return;
    const interval = setInterval(() => {
      setLastUpdate(new Date());
    }, 10000);
    return () => clearInterval(interval);
  }, [isClient]);

  // Get latest values
  const latestTemp = tempMesures?.[0]?.valeur ?? 0;
  const latestHumidity = humidityMesures?.[0]?.valeur ?? 0;
  const latestLight = lightMesures?.[0]?.valeur ?? 0;

  // Convert mesures to chart data
  const tempChartData = (tempMesures || [])
    .slice()
    .reverse()
    .map((m) => ({ timestamp: m.dateMesure, value: m.valeur }));
  const humidityChartData = (humidityMesures || [])
    .slice()
    .reverse()
    .map((m) => ({ timestamp: m.dateMesure, value: m.valeur }));
  const lightChartData = (lightMesures || [])
    .slice()
    .reverse()
    .map((m) => ({ timestamp: m.dateMesure, value: m.valeur }));

  // Transform alerts to match expected format
  const alertsList = alerts.map((alert) => ({
    eventId: `alert-${alert.id}`,
    mesureId: alert.id,
    parametreId: alert.parametreId,
    parametreType: alert.parametreType || 'UNKNOWN',
    valeur: alert.valeur,
    seuilMin: alert.seuilMin || 0,
    seuilMax: alert.seuilMax || 0,
    dateMesure: alert.dateMesure,
    severity: 'HIGH' as const, // Default severity since it's not in Mesure
    message: `Valeur ${alert.valeur} hors seuils`,
    eventTimestamp: alert.createdAt,
    acknowledged: false,
  }));

  return (
    <div className="space-y-6">
      {/* Header with Status */}
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Vue d&apos;ensemble de votre serre connectée
          </p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 text-sm text-gray-500">
            <Clock className="h-4 w-4" />
            <span>Dernière mise à jour: {lastUpdate ? formatDate(lastUpdate, 'HH:mm:ss') : '--:--:--'}</span>
          </div>
          <Badge variant="outline" className="gap-1">
            <Activity className="h-3 w-3 text-green-500 animate-pulse" />
            En direct
          </Badge>
        </div>
      </div>

      {/* Metrics Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <MetricCard
          title="Température"
          value={latestTemp}
          unit={tempParam?.unite || '°C'}
          min={tempParam?.seuilMin || 15}
          max={tempParam?.seuilMax || 30}
          icon="temperature"
          loading={loadingParametres}
        />
        <MetricCard
          title="Humidité"
          value={latestHumidity}
          unit={humidityParam?.unite || '%'}
          min={humidityParam?.seuilMin || 40}
          max={humidityParam?.seuilMax || 80}
          icon="humidity"
          loading={loadingParametres}
        />
        <MetricCard
          title="Luminosité"
          value={latestLight}
          unit={lightParam?.unite || 'lux'}
          min={lightParam?.seuilMin || 1000}
          max={lightParam?.seuilMax || 50000}
          icon="light"
          loading={loadingParametres}
        />
      </div>

      {/* Charts Section */}
      <div className="grid gap-4 lg:grid-cols-2">
        <ParameterChart
          title="Température (dernières 24h)"
          data={tempChartData}
          parametreType="TEMPERATURE"
          unit={tempParam?.unite || '°C'}
          seuilMin={tempParam?.seuilMin}
          seuilMax={tempParam?.seuilMax}
          loading={loadingParametres || !tempMesures}
        />
        <ParameterChart
          title="Humidité (dernières 24h)"
          data={humidityChartData}
          parametreType="HUMIDITE"
          unit={humidityParam?.unite || '%'}
          seuilMin={humidityParam?.seuilMin}
          seuilMax={humidityParam?.seuilMax}
          loading={loadingParametres || !humidityMesures}
        />
      </div>

      {/* Equipment and Alerts */}
      <div className="grid gap-4 lg:grid-cols-2">
        <EquipmentStatus
          equipment={equipment}
          loading={loadingEquipements}
        />
        <RecentAlerts
          alerts={alertsList}
          loading={loadingAlerts}
          onViewAll={() => router.push('/alertes')}
        />
      </div>

      {/* Luminosity Chart */}
      <ParameterChart
        title="Luminosité (dernières 24h)"
        data={lightChartData}
        parametreType="LUMINOSITE"
        unit={lightParam?.unite || 'lux'}
        seuilMin={lightParam?.seuilMin}
        seuilMax={lightParam?.seuilMax}
        loading={loadingParametres || !lightMesures}
      />
    </div>
  );
}
