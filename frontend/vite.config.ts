import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const actuatorProxy = (target: string, prefix: string) => ({
  target,
  changeOrigin: true,
  rewrite: (path: string) => path.replace(prefix, ''),
});

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/configurations': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/analyses': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/monitor/configuration': actuatorProxy(
        'http://localhost:8081',
        '/monitor/configuration',
      ),
      '/monitor/analysis-management': actuatorProxy(
        'http://localhost:8082',
        '/monitor/analysis-management',
      ),
      '/monitor/fluid': actuatorProxy(
        'http://localhost:8083',
        '/monitor/fluid',
      ),
      '/monitor/thermal': actuatorProxy(
        'http://localhost:8084',
        '/monitor/thermal',
      ),
      '/monitor/electrical': actuatorProxy(
        'http://localhost:8085',
        '/monitor/electrical',
      ),
      '/monitor/engine-management': actuatorProxy(
        'http://localhost:8086',
        '/monitor/engine-management',
      ),
    },
  },
});
