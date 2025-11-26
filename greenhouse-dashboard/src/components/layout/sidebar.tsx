'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  Thermometer,
  Activity,
  Settings2,
  Zap,
  AlertTriangle,
  Leaf,
} from 'lucide-react';
import { cn } from '@/lib/utils';

const navigation = [
  { name: 'Dashboard', href: '/', icon: LayoutDashboard },
  { name: 'Paramètres', href: '/parametres', icon: Thermometer },
  { name: 'Mesures', href: '/mesures', icon: Activity },
  { name: 'Équipements', href: '/equipements', icon: Settings2 },
  { name: 'Actions', href: '/actions', icon: Zap },
  { name: 'Alertes', href: '/alertes', icon: AlertTriangle },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="hidden lg:flex lg:flex-shrink-0">
      <div className="flex w-64 flex-col">
        <div className="flex min-h-0 flex-1 flex-col border-r border-gray-200 bg-white dark:border-gray-700 dark:bg-gray-800">
          {/* Logo */}
          <div className="flex h-16 flex-shrink-0 items-center px-4 border-b border-gray-200 dark:border-gray-700">
            <Link href="/" className="flex items-center gap-2">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-green-500">
                <Leaf className="h-6 w-6 text-white" />
              </div>
              <div>
                <h1 className="text-lg font-bold text-gray-900 dark:text-white">
                  GreenHouse
                </h1>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Management System
                </p>
              </div>
            </Link>
          </div>

          {/* Navigation */}
          <nav className="flex-1 space-y-1 px-3 py-4">
            {navigation.map((item) => {
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'group flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200',
                    isActive
                      ? 'bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400'
                      : 'text-gray-700 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-700'
                  )}
                >
                  <item.icon
                    className={cn(
                      'h-5 w-5 flex-shrink-0 transition-colors',
                      isActive
                        ? 'text-green-600 dark:text-green-400'
                        : 'text-gray-400 group-hover:text-gray-600 dark:group-hover:text-gray-300'
                    )}
                  />
                  {item.name}
                  {isActive && (
                    <span className="ml-auto h-2 w-2 rounded-full bg-green-500" />
                  )}
                </Link>
              );
            })}
          </nav>

          {/* Status Footer */}
          <div className="flex-shrink-0 border-t border-gray-200 p-4 dark:border-gray-700">
            <div className="rounded-lg bg-gray-50 p-3 dark:bg-gray-700/50">
              <div className="flex items-center gap-2">
                <span className="status-dot online" />
                <span className="text-sm text-gray-600 dark:text-gray-300">
                  Système connecté
                </span>
              </div>
              <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                API Gateway: localhost:8080
              </p>
            </div>
          </div>
        </div>
      </div>
    </aside>
  );
}
