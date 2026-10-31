import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'

import { SessionProvider } from '@/app/SessionContext'
import { AppRouter } from '@/app/router'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false, refetchOnWindowFocus: false },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <SessionProvider>
        <BrowserRouter>
          <AppRouter />
        </BrowserRouter>
      </SessionProvider>
    </QueryClientProvider>
  )
}
