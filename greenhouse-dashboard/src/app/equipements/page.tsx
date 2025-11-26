'use client';

import { useState } from 'react';
import {
  Plus,
  Power,
  Settings2,
  MapPin,
  Clock,
  AlertTriangle,
  Wrench,
  LayoutGrid,
  List,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Skeleton } from '@/components/ui/skeleton';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  useEquipements,
  useUpdateEquipementStatut,
  useCreateEquipement,
  useActionsByEquipement,
  useCreateAction,
} from '@/hooks/use-controle';
import {
  cn,
  getEquipmentTypeLabel,
  getEquipmentTypeIcon,
  getEquipmentStatusLabel,
  getEquipmentStatusColor,
  getActionStatusLabel,
  getActionStatusColor,
  formatDate,
  formatRelativeTime,
} from '@/lib/utils';
import { Equipement, EquipementType, EquipementStatut, TypeAction } from '@/types';
import toast from 'react-hot-toast';

const equipmentTypes: EquipementType[] = [
  'VENTILATEUR',
  'CHAUFFAGE',
  'ECLAIRAGE',
  'ARROSAGE',
  'HUMIDIFICATEUR',
];

export default function EquipementsPage() {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedEquipment, setSelectedEquipment] = useState<Equipement | null>(null);
  const [isActionModalOpen, setIsActionModalOpen] = useState(false);

  // Form state
  const [createForm, setCreateForm] = useState({
    nom: '',
    type: 'VENTILATEUR' as EquipementType,
    localisation: '',
    description: '',
  });

  const [actionForm, setActionForm] = useState({
    typeAction: 'ACTIVER' as TypeAction,
    valeurCible: '',
  });

  // Queries
  const { data: equipementsData, isLoading } = useEquipements(0, 100);
  const updateStatutMutation = useUpdateEquipementStatut();
  const createMutation = useCreateEquipement();
  const createActionMutation = useCreateAction();

  const equipements = equipementsData?.content || [];

  // Stats
  const stats = {
    total: equipements.length,
    active: equipements.filter((e) => e.statut === 'ACTIF').length,
    inactive: equipements.filter((e) => e.statut === 'INACTIF').length,
    maintenance: equipements.filter((e) => e.statut === 'MAINTENANCE').length,
    broken: equipements.filter((e) => e.statut === 'EN_PANNE').length,
  };

  // Handlers
  const handleToggleStatus = async (equipment: Equipement) => {
    const newStatut: EquipementStatut =
      equipment.statut === 'ACTIF' ? 'INACTIF' : 'ACTIF';
    try {
      await updateStatutMutation.mutateAsync({ id: equipment.id, statut: newStatut });
      toast.success(`${equipment.nom} ${newStatut === 'ACTIF' ? 'activé' : 'désactivé'}`);
    } catch {
      toast.error('Erreur lors du changement de statut');
    }
  };

  const handleCreate = async () => {
    try {
      await createMutation.mutateAsync(createForm);
      toast.success('Équipement créé avec succès');
      setIsCreateOpen(false);
      setCreateForm({ nom: '', type: 'VENTILATEUR', localisation: '', description: '' });
    } catch {
      toast.error('Erreur lors de la création');
    }
  };

  const handleCreateAction = async () => {
    if (!selectedEquipment) return;
    try {
      await createActionMutation.mutateAsync({
        equipementId: selectedEquipment.id,
        typeAction: actionForm.typeAction,
        valeurCible: actionForm.valeurCible ? Number(actionForm.valeurCible) : undefined,
      });
      toast.success('Action créée avec succès');
      setIsActionModalOpen(false);
    } catch {
      toast.error('Erreur lors de la création de l\'action');
    }
  };

  const openActionModal = (equipment: Equipement) => {
    setSelectedEquipment(equipment);
    setActionForm({ typeAction: 'ACTIVER', valeurCible: '' });
    setIsActionModalOpen(true);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Gérez et contrôlez les équipements de votre serre
          </p>
        </div>
        <div className="flex gap-2">
          <div className="flex rounded-lg border p-1">
            <Button
              variant={viewMode === 'grid' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => setViewMode('grid')}
            >
              <LayoutGrid className="h-4 w-4" />
            </Button>
            <Button
              variant={viewMode === 'list' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => setViewMode('list')}
            >
              <List className="h-4 w-4" />
            </Button>
          </div>
          <Button onClick={() => setIsCreateOpen(true)} className="gap-2">
            <Plus className="h-4 w-4" />
            Nouvel Équipement
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
        <Card className="border-green-500/30">
          <CardContent className="p-4">
            <p className="text-sm text-gray-500">Actifs</p>
            <p className="text-2xl font-bold text-green-500">{stats.active}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <p className="text-sm text-gray-500">Inactifs</p>
            <p className="text-2xl font-bold text-gray-500">{stats.inactive}</p>
          </CardContent>
        </Card>
        <Card className="border-yellow-500/30">
          <CardContent className="p-4">
            <p className="text-sm text-gray-500">Maintenance</p>
            <p className="text-2xl font-bold text-yellow-500">{stats.maintenance}</p>
          </CardContent>
        </Card>
        <Card className="border-red-500/30">
          <CardContent className="p-4">
            <p className="text-sm text-gray-500">En panne</p>
            <p className="text-2xl font-bold text-red-500">{stats.broken}</p>
          </CardContent>
        </Card>
      </div>

      {/* Equipment Grid/List */}
      {isLoading ? (
        <div className={cn('grid gap-4', viewMode === 'grid' && 'sm:grid-cols-2 lg:grid-cols-3')}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <Skeleton className="h-32 w-full" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : equipements.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Settings2 className="h-12 w-12 text-gray-400 mb-4" />
            <p className="text-lg font-medium">Aucun équipement</p>
            <p className="text-sm text-gray-500">
              Commencez par ajouter un équipement
            </p>
          </CardContent>
        </Card>
      ) : viewMode === 'grid' ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {equipements.map((equipment) => (
            <EquipmentCard
              key={equipment.id}
              equipment={equipment}
              onToggle={() => handleToggleStatus(equipment)}
              onAction={() => openActionModal(equipment)}
              onDetails={() => setSelectedEquipment(equipment)}
            />
          ))}
        </div>
      ) : (
        <Card>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-left">
                    <th className="p-4 font-medium text-gray-500">Équipement</th>
                    <th className="p-4 font-medium text-gray-500">Type</th>
                    <th className="p-4 font-medium text-gray-500">Localisation</th>
                    <th className="p-4 font-medium text-gray-500">Statut</th>
                    <th className="p-4 font-medium text-gray-500">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y">
                  {equipements.map((equipment) => (
                    <tr key={equipment.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                      <td className="p-4">
                        <div className="flex items-center gap-3">
                          <span className="text-xl">
                            {getEquipmentTypeIcon(equipment.type)}
                          </span>
                          <span className="font-medium">{equipment.nom}</span>
                        </div>
                      </td>
                      <td className="p-4">{getEquipmentTypeLabel(equipment.type)}</td>
                      <td className="p-4 text-gray-500">
                        {equipment.localisation || '-'}
                      </td>
                      <td className="p-4">
                        <Badge className={getEquipmentStatusColor(equipment.statut)}>
                          {getEquipmentStatusLabel(equipment.statut)}
                        </Badge>
                      </td>
                      <td className="p-4">
                        <div className="flex items-center gap-2">
                          <Switch
                            checked={equipment.statut === 'ACTIF'}
                            onCheckedChange={() => handleToggleStatus(equipment)}
                            disabled={equipment.statut === 'EN_PANNE'}
                          />
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => openActionModal(equipment)}
                          >
                            Action
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Create Equipment Modal */}
      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Nouvel Équipement</DialogTitle>
            <DialogDescription>
              Ajoutez un nouvel équipement à votre serre
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Nom</Label>
              <Input
                value={createForm.nom}
                onChange={(e) => setCreateForm({ ...createForm, nom: e.target.value })}
                placeholder="ex: Ventilateur Zone A"
              />
            </div>
            <div className="space-y-2">
              <Label>Type</Label>
              <Select
                value={createForm.type}
                onValueChange={(value) =>
                  setCreateForm({ ...createForm, type: value as EquipementType })
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {equipmentTypes.map((type) => (
                    <SelectItem key={type} value={type}>
                      {getEquipmentTypeIcon(type)} {getEquipmentTypeLabel(type)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Localisation</Label>
              <Input
                value={createForm.localisation}
                onChange={(e) =>
                  setCreateForm({ ...createForm, localisation: e.target.value })
                }
                placeholder="ex: Zone A, Allée 3"
              />
            </div>
            <div className="space-y-2">
              <Label>Description</Label>
              <Input
                value={createForm.description}
                onChange={(e) =>
                  setCreateForm({ ...createForm, description: e.target.value })
                }
                placeholder="Description optionnelle..."
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
              Annuler
            </Button>
            <Button onClick={handleCreate} loading={createMutation.isPending}>
              Créer
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Action Modal */}
      <Dialog open={isActionModalOpen} onOpenChange={setIsActionModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Créer une Action</DialogTitle>
            <DialogDescription>
              {selectedEquipment?.nom} - Définir une action manuelle
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Type d&apos;action</Label>
              <Select
                value={actionForm.typeAction}
                onValueChange={(value) =>
                  setActionForm({ ...actionForm, typeAction: value as TypeAction })
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ACTIVER">Activer</SelectItem>
                  <SelectItem value="DESACTIVER">Désactiver</SelectItem>
                  <SelectItem value="AJUSTER">Ajuster</SelectItem>
                  <SelectItem value="URGENCE">Urgence</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {actionForm.typeAction === 'AJUSTER' && (
              <div className="space-y-2">
                <Label>Valeur cible</Label>
                <Input
                  type="number"
                  value={actionForm.valeurCible}
                  onChange={(e) =>
                    setActionForm({ ...actionForm, valeurCible: e.target.value })
                  }
                  placeholder="ex: 50"
                />
              </div>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsActionModalOpen(false)}>
              Annuler
            </Button>
            <Button onClick={handleCreateAction} loading={createActionMutation.isPending}>
              Exécuter
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Equipment Details Modal */}
      {selectedEquipment && !isActionModalOpen && (
        <EquipmentDetailsModal
          equipment={selectedEquipment}
          onClose={() => setSelectedEquipment(null)}
        />
      )}
    </div>
  );
}

// Equipment Card Component
function EquipmentCard({
  equipment,
  onToggle,
  onAction,
  onDetails,
}: {
  equipment: Equipement;
  onToggle: () => void;
  onAction: () => void;
  onDetails: () => void;
}) {
  const isActive = equipment.statut === 'ACTIF';
  const isBroken = equipment.statut === 'EN_PANNE';

  return (
    <Card className={cn('group', isBroken && 'border-red-500/50')}>
      <CardHeader className="pb-2">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div
              className={cn(
                'flex h-10 w-10 items-center justify-center rounded-lg text-xl',
                isActive ? 'bg-green-500/10' : 'bg-gray-500/10'
              )}
            >
              {getEquipmentTypeIcon(equipment.type)}
            </div>
            <div>
              <CardTitle className="text-base">{equipment.nom}</CardTitle>
              <p className="text-xs text-gray-500">
                {getEquipmentTypeLabel(equipment.type)}
              </p>
            </div>
          </div>
          <Switch
            checked={isActive}
            onCheckedChange={onToggle}
            disabled={isBroken}
          />
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          <Badge className={getEquipmentStatusColor(equipment.statut)}>
            {getEquipmentStatusLabel(equipment.statut)}
          </Badge>

          {equipment.localisation && (
            <div className="flex items-center gap-2 text-sm text-gray-500">
              <MapPin className="h-4 w-4" />
              {equipment.localisation}
            </div>
          )}

          {equipment.derniereAction && (
            <div className="flex items-center gap-2 text-xs text-gray-400">
              <Clock className="h-3 w-3" />
              Dernière action: {formatRelativeTime(equipment.derniereAction)}
            </div>
          )}

          <div className="flex gap-2 pt-2">
            <Button variant="outline" size="sm" className="flex-1" onClick={onDetails}>
              Détails
            </Button>
            <Button size="sm" className="flex-1" onClick={onAction}>
              Action
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

// Equipment Details Modal
function EquipmentDetailsModal({
  equipment,
  onClose,
}: {
  equipment: Equipement;
  onClose: () => void;
}) {
  const { data: actionsData, isLoading } = useActionsByEquipement(equipment.id, 0, 10);
  const actions = actionsData?.content || [];

  return (
    <Dialog open onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <span className="text-2xl">{getEquipmentTypeIcon(equipment.type)}</span>
            {equipment.nom}
          </DialogTitle>
          <DialogDescription>
            Détails et historique des actions
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4 py-4">
          {/* Info */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-500">Type</p>
              <p className="font-medium">{getEquipmentTypeLabel(equipment.type)}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Statut</p>
              <Badge className={getEquipmentStatusColor(equipment.statut)}>
                {getEquipmentStatusLabel(equipment.statut)}
              </Badge>
            </div>
            <div>
              <p className="text-sm text-gray-500">Localisation</p>
              <p className="font-medium">{equipment.localisation || '-'}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Créé le</p>
              <p className="font-medium">{formatDate(equipment.createdAt)}</p>
            </div>
          </div>

          {equipment.description && (
            <div>
              <p className="text-sm text-gray-500">Description</p>
              <p className="text-sm">{equipment.description}</p>
            </div>
          )}

          {/* Actions History */}
          <div>
            <h4 className="font-medium mb-2">Historique des Actions</h4>
            {isLoading ? (
              <Skeleton className="h-20 w-full" />
            ) : actions.length === 0 ? (
              <p className="text-sm text-gray-500">Aucune action enregistrée</p>
            ) : (
              <div className="space-y-2 max-h-48 overflow-y-auto">
                {actions.map((action) => (
                  <div
                    key={action.id}
                    className="flex items-center justify-between rounded-lg border p-2"
                  >
                    <div>
                      <p className="font-medium text-sm">{action.typeAction}</p>
                      <p className="text-xs text-gray-500">
                        {formatDate(action.createdAt)}
                      </p>
                    </div>
                    <Badge className={getActionStatusColor(action.statut)}>
                      {getActionStatusLabel(action.statut)}
                    </Badge>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Fermer
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
