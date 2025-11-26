'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Leaf, Eye, EyeOff, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [form, setForm] = useState({
    email: '',
    password: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      // TODO: Implement actual authentication
      // const response = await authApi.login(form.email, form.password);
      // localStorage.setItem('access_token', response.accessToken);
      // localStorage.setItem('refresh_token', response.refreshToken);

      // Simulate login for demo
      await new Promise((resolve) => setTimeout(resolve, 1500));

      // For demo purposes, accept any credentials
      localStorage.setItem('access_token', 'demo_token');
      localStorage.setItem('user', JSON.stringify({
        email: form.email,
        name: 'Utilisateur Demo',
      }));

      toast.success('Connexion réussie!');
      router.push('/');
    } catch (error) {
      toast.error('Identifiants invalides');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-green-50 to-emerald-100 dark:from-gray-900 dark:to-gray-800 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-4">
          <div className="flex justify-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-xl bg-green-600 text-white">
              <Leaf className="h-8 w-8" />
            </div>
          </div>
          <div>
            <CardTitle className="text-2xl">Greenhouse Manager</CardTitle>
            <CardDescription>
              Connectez-vous pour accéder à votre tableau de bord
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="votre@email.com"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
                disabled={isLoading}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Mot de passe</Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required
                  disabled={isLoading}
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="absolute right-2 top-1/2 -translate-y-1/2 h-7 w-7 p-0"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4" />
                  ) : (
                    <Eye className="h-4 w-4" />
                  )}
                </Button>
              </div>
            </div>
            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 text-sm">
                <input type="checkbox" className="rounded" />
                Se souvenir de moi
              </label>
              <a href="#" className="text-sm text-green-600 hover:underline">
                Mot de passe oublié?
              </a>
            </div>
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Connexion...
                </>
              ) : (
                'Se connecter'
              )}
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-gray-500">
            <p>
              Pas encore de compte?{' '}
              <a href="#" className="text-green-600 hover:underline">
                Contactez l&apos;administrateur
              </a>
            </p>
          </div>

          {/* Demo credentials hint */}
          <div className="mt-4 rounded-lg bg-gray-50 dark:bg-gray-800 p-3 text-center text-xs text-gray-500">
            <p className="font-medium">Mode démo</p>
            <p>Utilisez n&apos;importe quels identifiants pour vous connecter</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
