'use client';

import { useState, useEffect } from 'react';
import {
  Search,
  Filter,
  RefreshCw,
  Zap,
  Clock,
  CheckCircle,
  XCircle,
  AlertTriangle,
  Play,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import { useActions, useEquipements } from '@/hooks/use-controle';
import {
  cn,
  getActionStatusLabel,
  getActionStatusColor,
  getEquipmentTypeIcon,
  formatDate,
  formatRelativeTime,
} from '@/lib/utils';
import { StatutAction, TypeAction, Action } from '@/types';

export default function ActionsPage() {
  const [page, setPage] = useState(0);
  const [equipementId, setEquipementId] = useState<string>('__all__');
  const [statut, setStatut] = useState<string>('__all__');
  const [typeAction, setTypeAction] = useState<string>('__all__');
  const [autoRefresh, setAutoRefresh] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Queries
  const { data: actionsData, isLoading, refetch } = useActions({
    equipementId: equipementId !== '__all__' ? Number(equipementId) : undefined,
    statut: statut !== '__all__' ? statut as StatutAction : undefined,
    page,
    size: 20,
  });
  const { data: equipementsData } = useEquipements(0, 100);

  const actions = actionsData?.content || [];
  const totalPages = actionsData?.totalPages || 0;
  const equipements = equipementsData?.content || [];

  // Auto-refresh every 10 seconds
  useEffect(() => {
    if (!autoRefresh) return;
    const interval = setInterval(() => {
      refetch();
    }, 10000);
    return () => clearInterval(interval);
  }, [autoRefresh, refetch]);

  // Filter by type action locally
  const filteredActions = actions.filter((action) => {
    if (typeAction && typeAction !== '__all__' && action.typeAction !== typeAction) return false;
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      const equipement = equipements.find((e) => e.id === action.equipementId);
      const equipementNom = equipement?.nom?.toLowerCase() || '';
      return (
        equipementNom.includes(query) ||
        action.typeAction.toLowerCase().includes(query) ||
        action.statut.toLowerCase().includes(query)
      );
    }
    return true;
  });

  // Stats
  const stats = {
    total: actions.length,
    pending: actions.filter((a) => a.statut === 'EN_ATTENTE').length,
    inProgress: actions.filter((a) => a.statut === 'EN_COURS').length,
    completed: actions.filter((a) => a.statut === 'TERMINEE').length,
    failed: actions.filter((a) => a.statut === 'ECHOUEE').length,
  };

  const getStatusIcon = (status: StatutAction) => {
    switch (status) {
      case 'EN_ATTENTE':
        return <Clock className="h-4 w-4" />;
      case 'EN_COURS':
        return <Play className="h-4 w-4" />;
      case 'TERMINEE':
        return <CheckCircle className="h-4 w-4" />;
      case 'ECHOUEE':
        return <XCircle className="h-4 w-4" />;
      case 'ANNULEE':
        return <AlertTriangle className="h-4 w-4" />;
      default:
        return null;
    }
  };

  const getActionTypeIcon = (type: TypeAction) => {
    switch (type) {
      case 'ACTIVER':
        return <Play className="h-4 w-4 text-green-500" />;
      case 'DESACTIVER':
        return <XCircle className="h-4 w-4 text-gray-500" />;
      case 'AJUSTER':
        return <Zap className="h-4 w-4 text-blue-500" />;
      case 'URGENCE':
        return <AlertTriangle className="h-4 w-4 text-red-500" />;
      default:
        return null;
    }
  };

  const getEquipmentName = (equipementId: number) => {
    const equipement = equipements.find((e) => e.id === equipementId);
    return equipement?.nom || `Équipement #${equipementId}`;
  };

  const getEquipmentType = (equipementId: number) => {
    const equipement = equipements.find((e) => e.id === equipementId);
    return equipement?.type;
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Historique et suivi des actions sur les équipements
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
          <Button variant="outline" onClick={() => refetch()} className="gap-2">
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
        <Card className="border-yellow-500/30">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">En attente</p>
              <p className="text-2xl font-bold text-yellow-500">{stats.pending}</p>
            </div>
            <Clock className="h-8 w-8 text-yellow-500/50" />
          </CardContent>
        </Card>
        <Card className="border-blue-500/30">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">En cours</p>
              <p className="text-2xl font-bold text-blue-500">{stats.inProgress}</p>
            </div>
            <Play className="h-8 w-8 text-blue-500/50" />
          </CardContent>
        </Card>
        <Card className="border-green-500/30">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Terminées</p>
              <p className="text-2xl font-bold text-green-500">{stats.completed}</p>
            </div>
            <CheckCircle className="h-8 w-8 text-green-500/50" />
          </CardContent>
        </Card>
        <Card className="border-red-500/30">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Échouées</p>
              <p className="text-2xl font-bold text-red-500">{stats.failed}</p>
            </div>
            <XCircle className="h-8 w-8 text-red-500/50" />
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap gap-4">
            <div className="relative flex-1 min-w-[200px]">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
              <Input
                placeholder="Rechercher..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
            <Select value={equipementId} onValueChange={setEquipementId}>
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="Équipement" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="__all__">Tous les équipements</SelectItem>
                {equipements.map((e) => (
                  <SelectItem key={e.id} value={String(e.id)}>
                    {getEquipmentTypeIcon(e.type)} {e.nom}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={statut} onValueChange={setStatut}>
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Statut" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="__all__">Tous les statuts</SelectItem>
                <SelectItem value="EN_ATTENTE">En attente</SelectItem>
                <SelectItem value="EN_COURS">En cours</SelectItem>
                <SelectItem value="TERMINEE">Terminée</SelectItem>
                <SelectItem value="ECHOUEE">Échouée</SelectItem>
                <SelectItem value="ANNULEE">Annulée</SelectItem>
              </SelectContent>
            </Select>
            <Select value={typeAction} onValueChange={setTypeAction}>
              <SelectTrigger className="w-[140px]">
                <SelectValue placeholder="Type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="__all__">Tous les types</SelectItem>
                <SelectItem value="ACTIVER">Activer</SelectItem>
                <SelectItem value="DESACTIVER">Désactiver</SelectItem>
                <SelectItem value="AJUSTER">Ajuster</SelectItem>
                <SelectItem value="URGENCE">Urgence</SelectItem>
              </SelectContent>
            </Select>
            <Button
              variant="outline"
              onClick={() => {
                setEquipementId('__all__');
                setStatut('__all__');
                setTypeAction('__all__');
                setSearchQuery('');
              }}
            >
              <Filter className="h-4 w-4 mr-2" />
              Réinitialiser
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Actions List */}
      {isLoading ? (
        <div className="space-y-4">
          {[1, 2, 3, 4, 5].map((i) => (
            <Card key={i}>
              <CardContent className="p-4">
                <Skeleton className="h-16 w-full" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : filteredActions.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Zap className="h-12 w-12 text-gray-400 mb-4" />
            <p className="text-lg font-medium">Aucune action trouvée</p>
            <p className="text-sm text-gray-500">
              Les actions apparaîtront ici
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {filteredActions.map((action) => (
            <ActionCard
              key={action.id}
              action={action}
              equipmentName={getEquipmentName(action.equipementId)}
              equipmentType={getEquipmentType(action.equipementId)}
              getStatusIcon={getStatusIcon}
              getActionTypeIcon={getActionTypeIcon}
            />
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <Button
            variant="outline"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
          >
            Précédent
          </Button>
          <span className="text-sm text-gray-500">
            Page {page + 1} sur {totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
          >
            Suivant
          </Button>
        </div>
      )}
    </div>
  );
}

// Action Card Component
function ActionCard({
  action,
  equipmentName,
  equipmentType,
  getStatusIcon,
  getActionTypeIcon,
}: {
  action: Action;
  equipmentName: string;
  equipmentType?: string;
  getStatusIcon: (status: StatutAction) => React.ReactNode;
  getActionTypeIcon: (type: TypeAction) => React.ReactNode;
}) {
  return (
    <Card className={cn(
      action.statut === 'EN_COURS' && 'border-blue-500/50',
      action.statut === 'ECHOUEE' && 'border-red-500/50',
      action.typeAction === 'URGENCE' && 'border-red-500'
    )}>
      <CardContent className="p-4">
        <div className="flex items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            {/* Equipment Icon */}
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gray-100 dark:bg-gray-800 text-xl">
              {equipmentType ? getEquipmentTypeIcon(equipmentType) : '🔧'}
            </div>

            {/* Action Info */}
            <div>
              <div className="flex items-center gap-2">
                {getActionTypeIcon(action.typeAction)}
                <span className="font-medium">{action.typeAction}</span>
                {action.typeAction === 'URGENCE' && (
                  <Badge variant="destructive">Urgence</Badge>
                )}
              </div>
              <p className="text-sm text-gray-500">{equipmentName}</p>
            </div>
          </div>

          {/* Right side info */}
          <div className="flex items-center gap-4">
            {/* Target Value */}
            {action.valeurCible !== undefined && action.valeurCible !== null && (
              <div className="text-right hidden sm:block">
                <p className="text-xs text-gray-500">Valeur cible</p>
                <p className="font-medium">{action.valeurCible}</p>
              </div>
            )}

            {/* Trigger */}
            <div className="text-right hidden sm:block">
              <p className="text-xs text-gray-500">Déclencheur</p>
              <p className="text-sm">
                {action.declencheurType === 'AUTOMATIQUE' ? '🤖 Auto' : '👤 Manuel'}
              </p>
            </div>

            {/* Time */}
            <div className="text-right">
              <p className="text-xs text-gray-500">
                {formatRelativeTime(action.createdAt)}
              </p>
              {action.executedAt && (
                <p className="text-xs text-gray-400">
                  Exécuté: {formatRelativeTime(action.executedAt)}
                </p>
              )}
            </div>

            {/* Status */}
            <Badge className={cn('min-w-[100px] justify-center', getActionStatusColor(action.statut))}>
              {getStatusIcon(action.statut)}
              <span className="ml-1">{getActionStatusLabel(action.statut)}</span>
            </Badge>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
