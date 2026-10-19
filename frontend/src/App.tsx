import { BrowserRouter, Routes, Route } from 'react-router'
import Dashboard from './pages/Dashboard'
import DocumentDetails from './pages/DocumentDetails'
import './App.css'

function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/documents/:id" element={<DocumentDetails />} />
        </Routes>
      </BrowserRouter>
  )
}

export default App