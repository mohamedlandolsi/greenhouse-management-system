import { useState, useEffect, useCallback, useRef } from 'react';

// Types for SSE events
export interface SSEMessage<T = unknown> {
  eventType: string;
  eventId: string;
  data: T;
  timestamp: string;
}

export interface MeasurementEvent {
  id: string;
  capteurId: string;
  capteurName: string;
  parameterType: string;
  value: number;
  unit: string;
  timestamp: string;
  greenhouseId: string;
  greenhouseName: string;
  zoneId: string;
  zoneName: string;
}

export interface AlertEvent {
  id: string;
  type: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  message: string;
  source: string;
  sourceId: string;
  parameterType: string;
  currentValue: number;
  thresholdValue: number;
  greenhouseId: string;
  greenhouseName: string;
  timestamp: string;
  acknowledged: boolean;
}

export interface EquipmentStatusEvent {
  id: string;
  equipmentId: string;
  equipmentName: string;
  equipmentType: string;
  status: string;
  previousStatus: string;
  greenhouseId: string;
  greenhouseName: string;
  zoneId: string;
  zoneName: string;
  timestamp: string;
  triggeredBy: string;
}

export type SSEEventType = 'measurement' | 'alert' | 'equipment-status' | 'keep-alive' | 'error';

export interface UseSSEOptions {
  // Auto-reconnect options
  autoReconnect?: boolean;
  maxRetries?: number;
  initialRetryDelay?: number;
  maxRetryDelay?: number;
  
  // Filtering options
  parameterType?: string;
  severity?: string;
  equipmentType?: string;
  greenhouseId?: string;
  
  // Callbacks
  onMessage?: (message: SSEMessage) => void;
  onError?: (error: Event) => void;
  onOpen?: () => void;
  onClose?: () => void;
  
  // Buffer options
  bufferSize?: number;
  throttleMs?: number;
}

export interface UseSSEReturn<T> {
  data: T[];
  latestData: T | null;
  isConnected: boolean;
  isConnecting: boolean;
  error: string | null;
  retryCount: number;
  connect: () => void;
  disconnect: () => void;
  clearData: () => void;
}

const DEFAULT_OPTIONS: UseSSEOptions = {
  autoReconnect: true,
  maxRetries: 10,
  initialRetryDelay: 1000,
  maxRetryDelay: 30000,
  bufferSize: 100,
  throttleMs: 100,
};

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export function useSSE<T = unknown>(
  endpoint: string,
  options: UseSSEOptions = {}
): UseSSEReturn<T> {
  const mergedOptions = { ...DEFAULT_OPTIONS, ...options };
  
  const [data, setData] = useState<T[]>([]);
  const [latestData, setLatestData] = useState<T | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);
  
  const eventSourceRef = useRef<EventSource | null>(null);
  const retryTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const throttleTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const pendingDataRef = useRef<T[]>([]);
  const mountedRef = useRef(true);
  
  // Build URL with query parameters
  const buildUrl = useCallback(() => {
    const url = new URL(`${API_BASE_URL}${endpoint}`);
    
    if (mergedOptions.parameterType) {
      url.searchParams.set('parameterType', mergedOptions.parameterType);
    }
    if (mergedOptions.severity) {
      url.searchParams.set('severity', mergedOptions.severity);
    }
    if (mergedOptions.equipmentType) {
      url.searchParams.set('equipmentType', mergedOptions.equipmentType);
    }
    if (mergedOptions.greenhouseId) {
      url.searchParams.set('greenhouseId', mergedOptions.greenhouseId);
    }
    
    return url.toString();
  }, [endpoint, mergedOptions.parameterType, mergedOptions.severity, 
      mergedOptions.equipmentType, mergedOptions.greenhouseId]);
  
  // Calculate retry delay with exponential backoff
  const getRetryDelay = useCallback((attempt: number) => {
    const delay = Math.min(
      mergedOptions.initialRetryDelay! * Math.pow(2, attempt),
      mergedOptions.maxRetryDelay!
    );
    // Add jitter to prevent thundering herd
    return delay + Math.random() * 1000;
  }, [mergedOptions.initialRetryDelay, mergedOptions.maxRetryDelay]);
  
  // Throttled data update
  const updateData = useCallback((newItem: T) => {
    pendingDataRef.current.push(newItem);
    
    if (!throttleTimeoutRef.current) {
      throttleTimeoutRef.current = setTimeout(() => {
        if (mountedRef.current) {
          setData(prev => {
            const newData = [...prev, ...pendingDataRef.current];
            // Keep only the last N items based on buffer size
            return newData.slice(-mergedOptions.bufferSize!);
          });
          setLatestData(pendingDataRef.current[pendingDataRef.current.length - 1]);
          pendingDataRef.current = [];
        }
        throttleTimeoutRef.current = null;
      }, mergedOptions.throttleMs);
    }
  }, [mergedOptions.bufferSize, mergedOptions.throttleMs]);
  
  // Handle incoming messages
  const handleMessage = useCallback((event: MessageEvent) => {
    try {
      const message: SSEMessage<T> = JSON.parse(event.data);
      
      // Skip keep-alive messages
      if (message.eventType === 'keep-alive') {
        return;
      }
      
      // Handle error messages
      if (message.eventType === 'error') {
        setError(String(message.data));
        return;
      }
      
      updateData(message.data);
      mergedOptions.onMessage?.(message);
    } catch (e) {
      console.error('Error parsing SSE message:', e);
    }
  }, [updateData, mergedOptions.onMessage]);
  
  // Connect to SSE endpoint
  const connect = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }
    
    if (retryTimeoutRef.current) {
      clearTimeout(retryTimeoutRef.current);
      retryTimeoutRef.current = null;
    }
    
    setIsConnecting(true);
    setError(null);
    
    const url = buildUrl();
    console.log('SSE: Connecting to', url);
    
    const eventSource = new EventSource(url);
    eventSourceRef.current = eventSource;
    
    eventSource.onopen = () => {
      if (mountedRef.current) {
        console.log('SSE: Connected');
        setIsConnected(true);
        setIsConnecting(false);
        setRetryCount(0);
        setError(null);
        mergedOptions.onOpen?.();
      }
    };
    
    eventSource.onmessage = handleMessage;
    
    // Listen for specific event types
    eventSource.addEventListener('measurement', handleMessage);
    eventSource.addEventListener('alert', handleMessage);
    eventSource.addEventListener('equipment-status', handleMessage);
    eventSource.addEventListener('keep-alive', () => {
      // Keep-alive received, connection is healthy
    });
    eventSource.addEventListener('error', (e) => {
      const errorEvent = e as MessageEvent;
      if (errorEvent.data) {
        try {
          const errorData = JSON.parse(errorEvent.data);
          setError(errorData.error || 'Unknown error');
        } catch {
          setError('Stream error');
        }
      }
    });
    
    eventSource.onerror = (e) => {
      console.error('SSE: Connection error', e);
      
      if (mountedRef.current) {
        setIsConnected(false);
        setIsConnecting(false);
        mergedOptions.onError?.(e);
        
        // Auto-reconnect logic
        if (mergedOptions.autoReconnect && retryCount < mergedOptions.maxRetries!) {
          const delay = getRetryDelay(retryCount);
          console.log(`SSE: Reconnecting in ${delay}ms (attempt ${retryCount + 1}/${mergedOptions.maxRetries})`);
          
          setError(`Connection lost. Reconnecting in ${Math.round(delay / 1000)}s...`);
          
          retryTimeoutRef.current = setTimeout(() => {
            if (mountedRef.current) {
              setRetryCount(prev => prev + 1);
              connect();
            }
          }, delay);
        } else if (retryCount >= mergedOptions.maxRetries!) {
          setError('Max reconnection attempts reached. Please refresh the page.');
        }
      }
    };
  }, [buildUrl, handleMessage, mergedOptions, retryCount, getRetryDelay]);
  
  // Disconnect from SSE endpoint
  const disconnect = useCallback(() => {
    console.log('SSE: Disconnecting');
    
    if (retryTimeoutRef.current) {
      clearTimeout(retryTimeoutRef.current);
      retryTimeoutRef.current = null;
    }
    
    if (throttleTimeoutRef.current) {
      clearTimeout(throttleTimeoutRef.current);
      throttleTimeoutRef.current = null;
    }
    
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
    
    setIsConnected(false);
    setIsConnecting(false);
    setRetryCount(0);
    mergedOptions.onClose?.();
  }, [mergedOptions.onClose]);
  
  // Clear data buffer
  const clearData = useCallback(() => {
    setData([]);
    setLatestData(null);
    pendingDataRef.current = [];
  }, []);
  
  // Effect for connection lifecycle
  useEffect(() => {
    mountedRef.current = true;
    connect();
    
    return () => {
      mountedRef.current = false;
      disconnect();
    };
  }, [endpoint]); // Reconnect when endpoint changes
  
  // Handle visibility change (pause when tab is hidden)
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.hidden) {
        // Optionally pause when tab is hidden
        // disconnect();
      } else {
        // Reconnect when tab becomes visible
        if (!isConnected && !isConnecting) {
          setRetryCount(0);
          connect();
        }
      }
    };
    
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [isConnected, isConnecting, connect]);
  
  return {
    data,
    latestData,
    isConnected,
    isConnecting,
    error,
    retryCount,
    connect,
    disconnect,
    clearData,
  };
}

// Specialized hooks for each event type

export function useMeasurementStream(options: Omit<UseSSEOptions, 'severity' | 'equipmentType'> = {}) {
  return useSSE<MeasurementEvent>('/api/stream/measurements', options);
}

export function useAlertStream(options: Omit<UseSSEOptions, 'parameterType' | 'equipmentType'> = {}) {
  return useSSE<AlertEvent>('/api/stream/alerts', options);
}

export function useEquipmentStatusStream(options: Omit<UseSSEOptions, 'parameterType' | 'severity'> = {}) {
  return useSSE<EquipmentStatusEvent>('/api/stream/equipment-status', options);
}

export function useCombinedStream(options: Pick<UseSSEOptions, 'greenhouseId' | 'autoReconnect' | 'onMessage' | 'onError' | 'onOpen' | 'onClose'> = {}) {
  return useSSE('/api/stream/all', options);
}
