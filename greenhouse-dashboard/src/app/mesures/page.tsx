'use client';

import { useState, useMemo } from 'react';
import {
  Download,
  Search,
  Filter,
  Calendar,
  Activity,
  AlertTriangle,
  RefreshCw,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { ParameterChart } from '@/components/dashboard';
import {
  useParametres,
  useMesures,
  useMesuresByDateRange,
} from '@/hooks/use-environnement';
import {
  cn,
  formatDate,
  formatNumber,
  getParameterTypeLabel,
  getParameterTypeIcon,
  getParameterTypeColor,
  exportToCSV,
} from '@/lib/utils';
import { Mesure, ParametreType } from '@/types';

export default function MesuresPage() {
  const [selectedParametre, setSelectedParametre] = useState<string>('all');
  const [alertsOnly, setAlertsOnly] = useState(false);
  const [dateRange, setDateRange] = useState({
    start: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
    end: new Date().toISOString().slice(0, 16),
  });
  const [page, setPage] = useState(0);

  // Queries
  const { data: parametresData, isLoading: loadingParams } = useParametres(0, 100);
  const { data: mesuresData, isLoading: loadingMesures, refetch } = useMesures({
    page,
    size: 50,
    parametreId: selectedParametre !== 'all' ? Number(selectedParametre) : undefined,
    alertsOnly,
  });

  const { data: chartData } = useMesuresByDateRange(
    dateRange.start,
    dateRange.end,
    selectedParametre !== 'all' ? Number(selectedParametre) : undefined,
    0,
    500
  );

  const parametres = parametresData?.content || [];
  const mesures = mesuresData?.content || [];
  const totalPages = mesuresData?.totalPages || 0;

  // Get selected parameter details
  const selectedParam = parametres.find(
    (p) => p.id === Number(selectedParametre)
  );

  // Chart data formatted
  const formattedChartData = useMemo(() => {
    if (!chartData?.content) return [];
    return chartData.content
      .slice()
      .reverse()
      .map((m) => ({
        timestamp: m.dateMesure,
        value: m.valeur,
      }));
  }, [chartData]);

  // Stats
  const stats = useMemo(() => {
    const values = mesures.map((m) => m.valeur);
    if (values.length === 0) return null;
    return {
      min: Math.min(...values),
      max: Math.max(...values),
      avg: values.reduce((a, b) => a + b, 0) / values.length,
      count: values.length,
      alerts: mesures.filter((m) => m.alerte).length,
    };
  }, [mesures]);

  // Export handler
  const handleExport = () => {
    const exportData = mesures.map((m) => ({
      ID: m.id,
      Parametre_ID: m.parametreId,
      Type: m.parametreType || '',
      Valeur: m.valeur,
      Unite: m.unite || '',
      Date: formatDate(m.dateMesure),
      Alerte: m.alerte ? 'Oui' : 'Non',
    }));
    exportToCSV(exportData, 'mesures');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Visualisez et analysez les mesures de votre serre
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => refetch()} className="gap-2">
            <RefreshCw className="h-4 w-4" />
            Actualiser
          </Button>
          <Button onClick={handleExport} className="gap-2">
            <Download className="h-4 w-4" />
            Exporter CSV
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Select value={selectedParametre} onValueChange={setSelectedParametre}>
              <SelectTrigger>
                <Filter className="mr-2 h-4 w-4" />
                <SelectValue placeholder="Paramètre" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Tous les paramètres</SelectItem>
                {parametres.map((p) => (
                  <SelectItem key={p.id} value={p.id.toString()}>
                    {getParameterTypeIcon(p.type)} {p.nom}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <div className="relative">
              <Calendar className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
              <Input
                type="datetime-local"
                value={dateRange.start}
                onChange={(e) => setDateRange({ ...dateRange, start: e.target.value })}
                className="pl-9"
              />
            </div>

            <div className="relative">
              <Calendar className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
              <Input
                type="datetime-local"
                value={dateRange.end}
                onChange={(e) => setDateRange({ ...dateRange, end: e.target.value })}
                className="pl-9"
              />
            </div>

            <Button
              variant={alertsOnly ? 'default' : 'outline'}
              onClick={() => setAlertsOnly(!alertsOnly)}
              className="gap-2"
            >
              <AlertTriangle className="h-4 w-4" />
              Alertes uniquement
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Stats Cards */}
      {stats && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
          <Card>
            <CardContent className="p-4">
              <p className="text-sm text-gray-500">Nombre de mesures</p>
              <p className="text-2xl font-bold">{stats.count}</p>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <p className="text-sm text-gray-500">Minimum</p>
              <p className="text-2xl font-bold text-blue-500">
                {formatNumber(stats.min)}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <p className="text-sm text-gray-500">Maximum</p>
              <p className="text-2xl font-bold text-red-500">
                {formatNumber(stats.max)}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <p className="text-sm text-gray-500">Moyenne</p>
              <p className="text-2xl font-bold text-green-500">
                {formatNumber(stats.avg)}
              </p>
            </CardContent>
          </Card>
          <Card className={cn(stats.alerts > 0 && 'border-red-500/50')}>
            <CardContent className="p-4">
              <p className="text-sm text-gray-500">Alertes</p>
              <p className={cn('text-2xl font-bold', stats.alerts > 0 ? 'text-red-500' : 'text-gray-500')}>
                {stats.alerts}
              </p>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Chart */}
      {selectedParam && formattedChartData.length > 0 && (
        <ParameterChart
          title={`${selectedParam.nom} - Évolution`}
          data={formattedChartData}
          parametreType={selectedParam.type}
          unit={selectedParam.unite}
          seuilMin={selectedParam.seuilMin}
          seuilMax={selectedParam.seuilMax}
        />
      )}

      {/* Measurements Table */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-base font-medium">
              Historique des Mesures
            </CardTitle>
            <Badge variant="secondary">
              {mesuresData?.totalElements || 0} mesures
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          {loadingMesures ? (
            <div className="space-y-2">
              {[1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-12 w-full" />
              ))}
            </div>
          ) : mesures.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-gray-500">
              <Activity className="h-12 w-12 mb-4" />
              <p>Aucune mesure trouvée</p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-left">
                      <th className="pb-3 font-medium text-gray-500">Date</th>
                      <th className="pb-3 font-medium text-gray-500">Type</th>
                      <th className="pb-3 font-medium text-gray-500">Valeur</th>
                      <th className="pb-3 font-medium text-gray-500">Seuils</th>
                      <th className="pb-3 font-medium text-gray-500">Statut</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y">
                    {mesures.map((mesure) => (
                      <tr key={mesure.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="py-3">
                          <span className="font-medium">
                            {formatDate(mesure.dateMesure)}
                          </span>
                        </td>
                        <td className="py-3">
                          <div className="flex items-center gap-2">
                            <span>{getParameterTypeIcon(mesure.parametreType || '')}</span>
                            <span>{getParameterTypeLabel(mesure.parametreType || '')}</span>
                          </div>
                        </td>
                        <td className="py-3">
                          <span
                            className="font-semibold"
                            style={{ color: getParameterTypeColor(mesure.parametreType || '') }}
                          >
                            {formatNumber(mesure.valeur)} {mesure.unite}
                          </span>
                        </td>
                        <td className="py-3 text-gray-500">
                          {mesure.seuilMin} - {mesure.seuilMax}
                        </td>
                        <td className="py-3">
                          {mesure.alerte ? (
                            <Badge variant="destructive" className="gap-1">
                              <AlertTriangle className="h-3 w-3" />
                              Alerte
                            </Badge>
                          ) : (
                            <Badge variant="success">Normal</Badge>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="mt-4 flex items-center justify-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(Math.max(0, page - 1))}
                    disabled={page === 0}
                  >
                    Précédent
                  </Button>
                  <span className="text-sm text-gray-500">
                    Page {page + 1} sur {totalPages}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                    disabled={page >= totalPages - 1}
                  >
                    Suivant
                  </Button>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
