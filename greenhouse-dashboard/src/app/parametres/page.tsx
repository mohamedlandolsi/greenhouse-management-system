'use client';

import { useState } from 'react';
import {
  Plus,
  Pencil,
  Trash2,
  Search,
  Filter,
  Thermometer,
  Droplets,
  Sun,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  useParametres,
  useCreateParametre,
  useUpdateParametre,
  useDeleteParametre,
} from '@/hooks/use-environnement';
import { Parametre, ParametreRequest, ParametreType } from '@/types';
import { cn, getParameterTypeLabel, getParameterTypeIcon, formatDate } from '@/lib/utils';
import toast from 'react-hot-toast';

const typeIcons = {
  TEMPERATURE: Thermometer,
  HUMIDITE: Droplets,
  LUMINOSITE: Sun,
};

const typeColors = {
  TEMPERATURE: 'text-red-500 bg-red-500/10',
  HUMIDITE: 'text-blue-500 bg-blue-500/10',
  LUMINOSITE: 'text-yellow-500 bg-yellow-500/10',
};

export default function ParametresPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState<ParametreType | 'ALL'>('ALL');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingParametre, setEditingParametre] = useState<Parametre | null>(null);
  const [deleteConfirm, setDeleteConfirm] = useState<Parametre | null>(null);

  // Form state
  const [formData, setFormData] = useState<ParametreRequest>({
    nom: '',
    type: 'TEMPERATURE',
    unite: '',
    seuilMin: 0,
    seuilMax: 100,
    description: '',
  });

  // Queries
  const { data: parametresData, isLoading } = useParametres(0, 100);
  const createMutation = useCreateParametre();
  const updateMutation = useUpdateParametre();
  const deleteMutation = useDeleteParametre();

  const parametres = parametresData?.content || [];

  // Filter parametres
  const filteredParametres = parametres.filter((p) => {
    const matchesSearch =
      p.nom.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.type.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = filterType === 'ALL' || p.type === filterType;
    return matchesSearch && matchesType;
  });

  // Handlers
  const openCreateModal = () => {
    setEditingParametre(null);
    setFormData({
      nom: '',
      type: 'TEMPERATURE',
      unite: '°C',
      seuilMin: 15,
      seuilMax: 30,
      description: '',
    });
    setIsModalOpen(true);
  };

  const openEditModal = (parametre: Parametre) => {
    setEditingParametre(parametre);
    setFormData({
      nom: parametre.nom,
      type: parametre.type,
      unite: parametre.unite,
      seuilMin: parametre.seuilMin,
      seuilMax: parametre.seuilMax,
      description: parametre.description || '',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      if (editingParametre) {
        await updateMutation.mutateAsync({ id: editingParametre.id, data: formData });
        toast.success('Paramètre mis à jour avec succès');
      } else {
        await createMutation.mutateAsync(formData);
        toast.success('Paramètre créé avec succès');
      }
      setIsModalOpen(false);
    } catch (error) {
      toast.error('Une erreur est survenue');
    }
  };

  const handleDelete = async () => {
    if (!deleteConfirm) return;
    try {
      await deleteMutation.mutateAsync(deleteConfirm.id);
      toast.success('Paramètre supprimé');
      setDeleteConfirm(null);
    } catch (error) {
      toast.error('Impossible de supprimer ce paramètre');
    }
  };

  const handleTypeChange = (type: ParametreType) => {
    let unite = '';
    let seuilMin = 0;
    let seuilMax = 100;

    switch (type) {
      case 'TEMPERATURE':
        unite = '°C';
        seuilMin = 15;
        seuilMax = 30;
        break;
      case 'HUMIDITE':
        unite = '%';
        seuilMin = 40;
        seuilMax = 80;
        break;
      case 'LUMINOSITE':
        unite = 'lux';
        seuilMin = 1000;
        seuilMax = 50000;
        break;
    }

    setFormData({ ...formData, type, unite, seuilMin, seuilMax });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Gérez les paramètres environnementaux de votre serre
          </p>
        </div>
        <Button onClick={openCreateModal} className="gap-2">
          <Plus className="h-4 w-4" />
          Nouveau Paramètre
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col gap-4 sm:flex-row">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
              <Input
                placeholder="Rechercher un paramètre..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9"
              />
            </div>
            <Select
              value={filterType}
              onValueChange={(value) => setFilterType(value as ParametreType | 'ALL')}
            >
              <SelectTrigger className="w-full sm:w-48">
                <Filter className="mr-2 h-4 w-4" />
                <SelectValue placeholder="Filtrer par type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">Tous les types</SelectItem>
                <SelectItem value="TEMPERATURE">Température</SelectItem>
                <SelectItem value="HUMIDITE">Humidité</SelectItem>
                <SelectItem value="LUMINOSITE">Luminosité</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Parameters Grid */}
      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Card key={i}>
              <CardContent className="p-6">
                <Skeleton className="h-32 w-full" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : filteredParametres.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Thermometer className="h-12 w-12 text-gray-400 mb-4" />
            <p className="text-lg font-medium text-gray-900 dark:text-white">
              Aucun paramètre trouvé
            </p>
            <p className="text-sm text-gray-500">
              {searchTerm || filterType !== 'ALL'
                ? 'Essayez de modifier vos filtres'
                : 'Commencez par créer un paramètre'}
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredParametres.map((parametre) => {
            const TypeIcon = typeIcons[parametre.type] || Thermometer;
            const typeColor = typeColors[parametre.type] || 'text-gray-500 bg-gray-500/10';

            return (
              <Card key={parametre.id} className="group relative">
                <CardHeader className="pb-2">
                  <div className="flex items-start justify-between">
                    <div className={cn('rounded-lg p-2', typeColor)}>
                      <TypeIcon className="h-5 w-5" />
                    </div>
                    <div className="flex gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => openEditModal(parametre)}
                      >
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => setDeleteConfirm(parametre)}
                      >
                        <Trash2 className="h-4 w-4 text-red-500" />
                      </Button>
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <h3 className="font-semibold text-gray-900 dark:text-white">
                    {parametre.nom}
                  </h3>
                  <Badge variant="secondary" className="mt-2">
                    {getParameterTypeLabel(parametre.type)}
                  </Badge>
                  <div className="mt-4 space-y-2 text-sm text-gray-500">
                    <div className="flex justify-between">
                      <span>Seuil min</span>
                      <span className="font-medium">
                        {parametre.seuilMin} {parametre.unite}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span>Seuil max</span>
                      <span className="font-medium">
                        {parametre.seuilMax} {parametre.unite}
                      </span>
                    </div>
                  </div>
                  {parametre.description && (
                    <p className="mt-3 text-xs text-gray-400 line-clamp-2">
                      {parametre.description}
                    </p>
                  )}
                  <p className="mt-3 text-xs text-gray-400">
                    Créé le {formatDate(parametre.createdAt, 'dd/MM/yyyy')}
                  </p>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* Create/Edit Modal */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editingParametre ? 'Modifier le paramètre' : 'Nouveau paramètre'}
            </DialogTitle>
            <DialogDescription>
              {editingParametre
                ? 'Modifiez les informations du paramètre'
                : 'Créez un nouveau paramètre environnemental'}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="nom">Nom</Label>
              <Input
                id="nom"
                value={formData.nom}
                onChange={(e) => setFormData({ ...formData, nom: e.target.value })}
                placeholder="ex: Température Zone A"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="type">Type</Label>
              <Select
                value={formData.type}
                onValueChange={(value) => handleTypeChange(value as ParametreType)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="TEMPERATURE">🌡️ Température</SelectItem>
                  <SelectItem value="HUMIDITE">💧 Humidité</SelectItem>
                  <SelectItem value="LUMINOSITE">☀️ Luminosité</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="seuilMin">Seuil minimum</Label>
                <Input
                  id="seuilMin"
                  type="number"
                  value={formData.seuilMin}
                  onChange={(e) =>
                    setFormData({ ...formData, seuilMin: Number(e.target.value) })
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="seuilMax">Seuil maximum</Label>
                <Input
                  id="seuilMax"
                  type="number"
                  value={formData.seuilMax}
                  onChange={(e) =>
                    setFormData({ ...formData, seuilMax: Number(e.target.value) })
                  }
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="unite">Unité</Label>
              <Input
                id="unite"
                value={formData.unite}
                onChange={(e) => setFormData({ ...formData, unite: e.target.value })}
                placeholder="ex: °C, %, lux"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Description (optionnel)</Label>
              <Input
                id="description"
                value={formData.description}
                onChange={(e) =>
                  setFormData({ ...formData, description: e.target.value })
                }
                placeholder="Description du paramètre..."
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Annuler
            </Button>
            <Button
              onClick={handleSubmit}
              loading={createMutation.isPending || updateMutation.isPending}
            >
              {editingParametre ? 'Enregistrer' : 'Créer'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Modal */}
      <Dialog open={!!deleteConfirm} onOpenChange={() => setDeleteConfirm(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Confirmer la suppression</DialogTitle>
            <DialogDescription>
              Êtes-vous sûr de vouloir supprimer le paramètre &quot;{deleteConfirm?.nom}&quot; ?
              Cette action est irréversible.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteConfirm(null)}>
              Annuler
            </Button>
            <Button
              variant="destructive"
              onClick={handleDelete}
              loading={deleteMutation.isPending}
            >
              Supprimer
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
