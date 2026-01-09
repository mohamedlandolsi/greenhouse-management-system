'use client';

import { useState, useEffect } from 'react';
import {
  Bell,
  AlertTriangle,
  AlertCircle,
  Info,
  CheckCircle2,
  RefreshCw,
  Filter,
  Trash2,
  Check,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  cn,
  getParameterLabel,
  formatDate,
  formatRelativeTime,
} from '@/lib/utils';
import { Alert, AlertSeverity } from '@/types';
import toast from 'react-hot-toast';

// Mock alerts data (until real API is available)
const mockAlerts: Alert[] = [
  {
    id: 1,
    eventId: 'alert-1',
    mesureId: 101,
    parametreId: 1,
    parametreType: 'TEMPERATURE',
    message: 'Température élevée détectée dans la Zone A',
    severity: 'CRITICAL',
    valeur: 35.5,
    seuil: 30,
    seuilMin: 15,
    seuilMax: 30,
    dateMesure: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
    eventTimestamp: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
    acknowledged: false,
    createdAt: new Date(Date.now() - 5 * 60 * 1000).toISOString(),
  },
  {
    id: 2,
    eventId: 'alert-2',
    mesureId: 102,
    parametreId: 2,
    parametreType: 'HUMIDITE',
    message: 'Humidité trop basse dans la Zone B',
    severity: 'WARNING',
    valeur: 30,
    seuil: 40,
    seuilMin: 40,
    seuilMax: 80,
    dateMesure: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
    eventTimestamp: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
    acknowledged: false,
    createdAt: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
  },
  {
    id: 3,
    eventId: 'alert-3',
    mesureId: 103,
    parametreId: 3,
    parametreType: 'LUMINOSITE',
    message: 'Niveau de luminosité faible',
    severity: 'INFO',
    valeur: 200,
    seuil: 500,
    seuilMin: 500,
    seuilMax: 10000,
    dateMesure: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
    eventTimestamp: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
    acknowledged: true,
    createdAt: new Date(Date.now() - 60 * 60 * 1000).toISOString(),
  },
  {
    id: 4,
    eventId: 'alert-4',
    mesureId: 104,
    parametreId: 1,
    parametreType: 'TEMPERATURE',
    message: 'Pic de température critique',
    severity: 'CRITICAL',
    valeur: 38.2,
    seuil: 30,
    seuilMin: 15,
    seuilMax: 30,
    dateMesure: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    eventTimestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    acknowledged: true,
    createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 5,
    eventId: 'alert-5',
    mesureId: 105,
    parametreId: 2,
    parametreType: 'HUMIDITE',
    message: 'Humidité en dessous du seuil minimal',
    severity: 'WARNING',
    valeur: 25,
    seuil: 35,
    seuilMin: 40,
    seuilMax: 80,
    dateMesure: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
    eventTimestamp: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
    acknowledged: false,
    createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
  },
];

export default function AlertesPage() {
  const [alerts, setAlerts] = useState<Alert[]>(mockAlerts);
  const [severityFilter, setSeverityFilter] = useState<string>('__all__');
  const [tab, setTab] = useState<'all' | 'unacknowledged' | 'acknowledged'>('all');
  const [autoRefresh, setAutoRefresh] = useState(false);
  const [selectedAlert, setSelectedAlert] = useState<Alert | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // Simulate auto-refresh
  useEffect(() => {
    if (!autoRefresh) return;
    const interval = setInterval(() => {
      // In real app, this would refetch alerts
      console.log('Auto-refreshing alerts...');
    }, 10000);
    return () => clearInterval(interval);
  }, [autoRefresh]);

  // Stats
  const stats = {
    total: alerts.length,
    critical: alerts.filter((a) => a.severity === 'CRITICAL' && !a.acknowledged).length,
    warning: alerts.filter((a) => a.severity === 'WARNING' && !a.acknowledged).length,
    info: alerts.filter((a) => a.severity === 'INFO' && !a.acknowledged).length,
    acknowledged: alerts.filter((a) => a.acknowledged).length,
  };

  // Filter alerts
  const filteredAlerts = alerts.filter((alert) => {
    if (severityFilter && severityFilter !== '__all__' && alert.severity !== severityFilter) return false;
    if (tab === 'unacknowledged' && alert.acknowledged) return false;
    if (tab === 'acknowledged' && !alert.acknowledged) return false;
    return true;
  });

  // Handlers
  const handleAcknowledge = (alert: Alert) => {
    setAlerts((prev) =>
      prev.map((a) => (a.id === alert.id ? { ...a, acknowledged: true } : a))
    );
    toast.success('Alerte acquittée');
  };

  const handleAcknowledgeAll = () => {
    setAlerts((prev) => prev.map((a) => ({ ...a, acknowledged: true })));
    toast.success('Toutes les alertes ont été acquittées');
  };

  const handleDelete = (alert: Alert) => {
    setAlerts((prev) => prev.filter((a) => a.id !== alert.id));
    toast.success('Alerte supprimée');
  };

  const handleClearAcknowledged = () => {
    setAlerts((prev) => prev.filter((a) => !a.acknowledged));
    toast.success('Alertes acquittées supprimées');
  };

  const getSeverityIcon = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return <AlertCircle className="h-5 w-5 text-red-500" />;
      case 'WARNING':
        return <AlertTriangle className="h-5 w-5 text-yellow-500" />;
      case 'INFO':
        return <Info className="h-5 w-5 text-blue-500" />;
      default:
        return <Bell className="h-5 w-5" />;
    }
  };

  const getSeverityBadgeColor = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-red-500 text-white';
      case 'WARNING':
        return 'bg-yellow-500 text-black';
      case 'INFO':
        return 'bg-blue-500 text-white';
      default:
        return 'bg-gray-500 text-white';
    }
  };

  const getSeverityLabel = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return 'Critique';
      case 'WARNING':
        return 'Avertissement';
      case 'INFO':
        return 'Information';
      default:
        return severity;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Surveillez et gérez les alertes de votre serre
          </p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <Switch
              id="auto-refresh"
              checked={autoRefresh}
              onCheckedChange={setAutoRefresh}
            />
            <Label htmlFor="auto-refresh" className="text-sm">
              Auto-refresh
            </Label>
          </div>
          <Button variant="outline" className="gap-2">
            <RefreshCw className={cn('h-4 w-4', autoRefresh && 'animate-spin')} />
            Actualiser
          </Button>
        </div>
      </div>

      {/* Stats */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <Card>
          <CardContent className="p-4">
            <p className="text-sm text-gray-500">Total</p>
            <p className="text-2xl font-bold">{stats.total}</p>
          </CardContent>
        </Card>
        <Card className={cn('transition-colors', stats.critical > 0 && 'border-red-500 animate-pulse-slow')}>
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Critiques</p>
              <p className="text-2xl font-bold text-red-500">{stats.critical}</p>
            </div>
            <AlertCircle className="h-8 w-8 text-red-500/50" />
          </CardContent>
        </Card>
        <Card className={cn('transition-colors', stats.warning > 0 && 'border-yellow-500')}>
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Avertissements</p>
              <p className="text-2xl font-bold text-yellow-500">{stats.warning}</p>
            </div>
            <AlertTriangle className="h-8 w-8 text-yellow-500/50" />
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Informations</p>
              <p className="text-2xl font-bold text-blue-500">{stats.info}</p>
            </div>
            <Info className="h-8 w-8 text-blue-500/50" />
          </CardContent>
        </Card>
        <Card className="border-green-500/30">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Acquittées</p>
              <p className="text-2xl font-bold text-green-500">{stats.acknowledged}</p>
            </div>
            <CheckCircle2 className="h-8 w-8 text-green-500/50" />
          </CardContent>
        </Card>
      </div>

      {/* Filters and Actions */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div className="flex flex-wrap gap-4">
              <Select value={severityFilter} onValueChange={setSeverityFilter}>
                <SelectTrigger className="w-[160px]">
                  <SelectValue placeholder="Sévérité" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="__all__">Toutes les sévérités</SelectItem>
                  <SelectItem value="CRITICAL">Critique</SelectItem>
                  <SelectItem value="WARNING">Avertissement</SelectItem>
                  <SelectItem value="INFO">Information</SelectItem>
                </SelectContent>
              </Select>
              <Button
                variant="outline"
                onClick={() => setSeverityFilter('__all__')}
              >
                <Filter className="h-4 w-4 mr-2" />
                Réinitialiser
              </Button>
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                onClick={handleAcknowledgeAll}
                disabled={stats.critical + stats.warning + stats.info === 0}
              >
                <Check className="h-4 w-4 mr-2" />
                Tout acquitter
              </Button>
              <Button
                variant="outline"
                onClick={handleClearAcknowledged}
                disabled={stats.acknowledged === 0}
              >
                <Trash2 className="h-4 w-4 mr-2" />
                Supprimer acquittées
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs value={tab} onValueChange={(v) => setTab(v as typeof tab)}>
        <TabsList>
          <TabsTrigger value="all">
            Toutes ({alerts.length})
          </TabsTrigger>
          <TabsTrigger value="unacknowledged">
            Non acquittées ({alerts.filter((a) => !a.acknowledged).length})
          </TabsTrigger>
          <TabsTrigger value="acknowledged">
            Acquittées ({alerts.filter((a) => a.acknowledged).length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value={tab} className="mt-4">
          {isLoading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <Card key={i}>
                  <CardContent className="p-4">
                    <Skeleton className="h-16 w-full" />
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : filteredAlerts.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Bell className="h-12 w-12 text-gray-400 mb-4" />
                <p className="text-lg font-medium">Aucune alerte</p>
                <p className="text-sm text-gray-500">
                  {tab === 'unacknowledged'
                    ? 'Toutes les alertes ont été acquittées'
                    : 'Aucune alerte ne correspond aux filtres'}
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-3">
              {filteredAlerts.map((alert) => (
                <AlertCard
                  key={alert.id}
                  alert={alert}
                  getSeverityIcon={getSeverityIcon}
                  getSeverityBadgeColor={getSeverityBadgeColor}
                  getSeverityLabel={getSeverityLabel}
                  onAcknowledge={() => handleAcknowledge(alert)}
                  onDelete={() => handleDelete(alert)}
                  onDetails={() => setSelectedAlert(alert)}
                />
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>

      {/* Alert Details Modal */}
      {selectedAlert && (
        <Dialog open onOpenChange={() => setSelectedAlert(null)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                {getSeverityIcon(selectedAlert.severity)}
                Détails de l&apos;alerte
              </DialogTitle>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-gray-500">Sévérité</p>
                  <Badge className={getSeverityBadgeColor(selectedAlert.severity)}>
                    {getSeverityLabel(selectedAlert.severity)}
                  </Badge>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Paramètre</p>
                  <p className="font-medium">
                    {getParameterLabel(selectedAlert.parametreType)}
                  </p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Valeur mesurée</p>
                  <p className="font-medium text-lg">{selectedAlert.valeur}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Seuil</p>
                  <p className="font-medium text-lg">{selectedAlert.seuil}</p>
                </div>
                <div className="col-span-2">
                  <p className="text-sm text-gray-500">Message</p>
                  <p className="font-medium">{selectedAlert.message}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Date</p>
                  <p className="font-medium">{formatDate(selectedAlert.createdAt || selectedAlert.dateMesure)}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Statut</p>
                  <Badge
                    variant={selectedAlert.acknowledged ? 'outline' : 'default'}
                    className={selectedAlert.acknowledged ? 'text-green-500' : ''}
                  >
                    {selectedAlert.acknowledged ? 'Acquittée' : 'Non acquittée'}
                  </Badge>
                </div>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setSelectedAlert(null)}>
                Fermer
              </Button>
              {!selectedAlert.acknowledged && (
                <Button
                  onClick={() => {
                    handleAcknowledge(selectedAlert);
                    setSelectedAlert(null);
                  }}
                >
                  <Check className="h-4 w-4 mr-2" />
                  Acquitter
                </Button>
              )}
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}

// Alert Card Component
function AlertCard({
  alert,
  getSeverityIcon,
  getSeverityBadgeColor,
  getSeverityLabel,
  onAcknowledge,
  onDelete,
  onDetails,
}: {
  alert: Alert;
  getSeverityIcon: (severity: AlertSeverity) => React.ReactNode;
  getSeverityBadgeColor: (severity: AlertSeverity) => string;
  getSeverityLabel: (severity: AlertSeverity) => string;
  onAcknowledge: () => void;
  onDelete: () => void;
  onDetails: () => void;
}) {
  return (
    <Card
      className={cn(
        'transition-all',
        !alert.acknowledged && alert.severity === 'CRITICAL' && 'border-red-500 bg-red-500/5',
        !alert.acknowledged && alert.severity === 'WARNING' && 'border-yellow-500/50',
        alert.acknowledged && 'opacity-75'
      )}
    >
      <CardContent className="p-4">
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-start gap-4">
            {/* Icon */}
            <div
              className={cn(
                'flex h-10 w-10 items-center justify-center rounded-lg',
                alert.severity === 'CRITICAL' && 'bg-red-500/10',
                alert.severity === 'WARNING' && 'bg-yellow-500/10',
                alert.severity === 'INFO' && 'bg-blue-500/10'
              )}
            >
              {getSeverityIcon(alert.severity)}
            </div>

            {/* Content */}
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <Badge className={getSeverityBadgeColor(alert.severity)}>
                  {getSeverityLabel(alert.severity)}
                </Badge>
                <span className="text-sm text-gray-500">
                  {getParameterLabel(alert.parametreType)}
                </span>
              </div>
              <p className="font-medium">{alert.message}</p>
              <div className="flex items-center gap-4 text-sm text-gray-500">
                <span>
                  Valeur: <strong>{alert.valeur}</strong> (seuil: {alert.seuil})
                </span>
                <span>{formatRelativeTime(alert.createdAt || alert.dateMesure)}</span>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center gap-2">
            {alert.acknowledged ? (
              <Badge variant="outline" className="text-green-500">
                <CheckCircle2 className="h-3 w-3 mr-1" />
                Acquittée
              </Badge>
            ) : (
              <Button variant="outline" size="sm" onClick={onAcknowledge}>
                <Check className="h-4 w-4 mr-1" />
                Acquitter
              </Button>
            )}
            <Button variant="ghost" size="sm" onClick={onDetails}>
              Détails
            </Button>
            <Button variant="ghost" size="sm" onClick={onDelete}>
              <Trash2 className="h-4 w-4 text-gray-400 hover:text-red-500" />
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
